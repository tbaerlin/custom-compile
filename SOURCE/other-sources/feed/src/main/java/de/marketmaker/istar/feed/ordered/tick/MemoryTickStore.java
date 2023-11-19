/*
 * TickStore.java
 *
 * Created on 16.11.12 14:40
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.ordered.tick;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import com.netflix.servo.annotations.Monitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;

import de.marketmaker.istar.feed.FeedData;
import de.marketmaker.istar.feed.ordered.OrderedFeedData;
import de.marketmaker.istar.feed.tick.AbstractTickRecord;
import de.marketmaker.istar.feed.tick.TickProvider;

import static com.netflix.servo.annotations.DataSourceType.COUNTER;
import static com.netflix.servo.annotations.DataSourceType.GAUGE;
import static de.marketmaker.istar.feed.tick.AbstractTickRecord.TickItem.Encoding.TICK3;

/**
 * Stores tick data in off heap storage buffers, each of which is maintained by an
 * {@link OffHeapStore}. Each of these stores is divided into chunks of a certain size
 * (which has to be a multiple of 2) that can be requested for storing tick data. Using different
 * sizes ensures that data for symbols with many updates can be stored in larger chunks, which,
 * as those chunks will eventually be written to disk, that fewer seeks are required to retrieve
 * those serialized ticks again.<br>
 * Tick data for a symbol is added to the same chunk as long as it fits; if it does not, a
 * new chunk will be used and the old chunk will be forwarded to a {@link TickWriter}, which
 * asynchronously compresses the old chunk's data and appends it to the tick file.<p>
 * Each data chunk starts with a long that contains the address of the previous chunk. That
 * address is either an in-memory address (if the TickWriter has not yet written
 * it to the tick file), or an address in the tick file. In-memory addresses are encoded as
 * <pre>
 * |63|62..40|39   ..  32|31|30    ..       0|
 * |0 |undef.|store-index|  |address-in-store|
 * </pre>
 * which supports 256 <tt>OffHeapStore</tt>s with at most 2GB each (which would be enough even if we
 * wanted to store all of a day's ticks in memory). The <tt>address-in-store</tt> part points to the
 * end of the data in the chunk. Since the length of chunks in each <tt>OffHeapStore</tt>
 * is a multiple of 2, the base address of the chunk as well as the length of data stored in the
 * chunk can easily be computed by <tt>and</tt>-ing <tt>address-in-store</tt> with a bit mask.
 * <p>
 * A file address is encoded as
 * <pre>
 * |63|62 .. 24| 23 .. 0|
 * | 1| offset | length |
 * </pre>
 * That is 39 bit for the offset (so the max. size of a single tick file is 512GB)
 * and 24 bit (i.e., 16MB) for the length of the data stored at that offset.
 * Although the tick data for a single symbol on a certain day may exceed 16MB, this is not a problem
 * as tha data can easily be splitted into a few chunks smalled than 16MB<p>
 * In-memory and in-file addresses can easily be distinguished as the former have positive and the
 * latter have negative values.
 *
 * @author oflege
 */
@ManagedResource
public class MemoryTickStore implements TickProvider, DisposableBean {

    private static final char[] PCT_UPPER = "0123456789".toCharArray();

    private static final char[] PCT_LOWER = "oabcdefghi".toCharArray();

    private static final int STORAGE_INFO_INCREMENT = 4;

    static final int DEFAULT_CHUNK_LENGTH_SHIFT = 4;

    static final long NOT_AN_ADDRESS = -1L;

    private class Result implements TickProvider.Result {
        private final byte[] ticks;

        private final int[] storageInfo;

        private Result(byte[] ticks, int... storageInfo) {
            this.ticks = ticks;
            this.storageInfo = storageInfo;
        }

        @Override
        public byte[] getTicks() {
            return this.ticks;
        }

        @Override
        public AbstractTickRecord.TickItem.Encoding getEncoding() {
            return MemoryTickStore.this.encoding;
        }

        @Override
        public int[] getStorageInfo() {
            return this.storageInfo;
        }
    }

    static boolean isMemoryAddress(long address) {
        return address > 0;
    }

    private static final long MAX_STORE_SIZE = 1L << 31;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Monitor(type = COUNTER)
    private final AtomicLong numFailedAdds = new AtomicLong();

    private TickWriter tickWriter;

    private FileTickStore fileTickStore;

    private final OffHeapStore[] stores;

    // Performance optimization: we frequently have to iterate over all sizes of (almost) all stores
    // to find the most appropriate store for a desired chunk size. This array keeps all that info
    // in consecutive memory and is as small as it can possibly get.
    private final byte[] storeChunkSizesLog2;

    private final long numStoreBytes;

    private final int numStoreChunks;

    private final int numLogicalStores;

    private int newChunkLengthShift = DEFAULT_CHUNK_LENGTH_SHIFT;

    AbstractTickRecord.TickItem.Encoding encoding = TICK3;

    /**
     * Creates a new store backed by (at least) <tt>args.length/2</tt> off heap stores, each of which
     * offers memory chunks of a given size.<p>
     * <ul>
     * <li><tt>args[i]</tt> is the total size of a store in bytes (this may exceed 2GB, in which case
     * multiple off heap stores with the same chunk size will be created)
     * <li><tt>args[i+1]</tt> the size of chunks in that store.
     * </ul>
     * Each total store size must be a multiple of the respective chunk size,
     * each chunk size must be a power of 2 and at least 64.<p>
     * In order to simplify spring xml configurations, stores with total size 0 will be ignored.
     * @param args store specifications
     */
    public MemoryTickStore(long... args) {
        this.stores = createStores(args);
        this.storeChunkSizesLog2 = new byte[this.stores.length];
        for (int i = 0; i < stores.length; i++) {
            this.storeChunkSizesLog2[i] = (byte) log2(stores[i].getChunkSize());
        }
        this.numLogicalStores = countLogicalStores();
        this.numStoreBytes = getNumBytesFree();
        this.numStoreChunks = getNumChunksFree();
        this.logger.info("<init> #bytes=" + this.numStoreBytes + ", #chunks=" + this.numStoreChunks);
    }

    public void setEncoding(AbstractTickRecord.TickItem.Encoding encoding) {
        this.encoding = encoding;
    }

    public void setNewChunkLengthShift(int newChunkLengthShift) {
        this.newChunkLengthShift = newChunkLengthShift;
    }

    private int countLogicalStores() {
        int n = 0;
        int lastChunkSize = 0;
        for (OffHeapStore store : stores) {
            if (lastChunkSize != store.getChunkSize()) {
                lastChunkSize = store.getChunkSize();
                n++;
            }
        }
        return n;
    }

    private static int log2(final int n) {
        return 31 - Integer.numberOfLeadingZeros(n);
    }

    private OffHeapStore[] createStores(long[] args) {
        final List<OffHeapStore> tmp = new ArrayList<>();

        for (int i = 0; i < args.length; i += 2) {
            final int chunkSize = (int) args[i + 1];
            long size = args[i];
            while (size > 0) {
                final int storeSize = (int) Math.min(size, (MAX_STORE_SIZE - (chunkSize * 2)));
                OffHeapStore store = new OffHeapStore(storeSize, chunkSize);
                tmp.add(store);
                this.logger.info("<createStores> " + store);
                size -= storeSize;
            }
        }

        final OffHeapStore[] result = tmp.toArray(new OffHeapStore[tmp.size()]);
        Arrays.sort(result, (o1, o2) -> {
            final int cmp = o1.getChunkSize() - o2.getChunkSize();
            return (cmp != 0) ? cmp : (o1.getCapacity() - o2.getCapacity());
        });
        return result;
    }

    @ManagedAttribute
    public long getNumFailedAdds() {
        return numFailedAdds.get();
    }

    /**
     * Return a String that encodes the percentage of free chunks for each logical store. A fully occupied
     * store is denoted as "0-", an unused store as "9+" and for all other values of xy% free
     * x is rendered as the number x and y is mapped to the corresponding char in <tt>[oabcdefghi]</tt>,
     * so that, e.g., the status 3c4o denotes a memory store with chunks of two different sizes,
     * the smaller one has 33% free chunks, the larger one 40%.
     */
    String status() {
        final char[] chars = new char[this.numLogicalStores * 2];
        for (int c = 0, i = 0; c < chars.length; c += 2) {
            long num = 0;
            long numFree = 0;
            int chunkSize = this.stores[i].getChunkSize();
            do {
                num += this.stores[i].numChunks;
                numFree += this.stores[i].getNumChunksFree();
            } while (++i < this.stores.length && this.stores[i].getChunkSize() == chunkSize);

            if (numFree == 0) {
                chars[c] = '0';
                chars[c + 1] = '-';
            }
            else if (numFree == num) {
                chars[c] = '9';
                chars[c + 1] = '+';
            }
            else {
                int pct = (int) (numFree * 100 / num);
                chars[c] = PCT_UPPER[pct / 10];
                chars[c + 1] = PCT_LOWER[pct % 10];
            }
        }
        return new String(chars);
    }

    @Override
    public void destroy() throws Exception {
        for (int i = 0; i < this.stores.length; i++) {
            this.logger.info("<destroy> " + this.stores[i]);
            this.stores[i].destroy();
            this.stores[i] = null;
        }
    }

    public void setFileTickStore(FileTickStore fileTickStore) {
        this.fileTickStore = fileTickStore;
    }

    public void setTickWriter(TickWriter tickWriter) {
        this.tickWriter = tickWriter;
    }

    /**
     * called by parser thread(s) -> TickBuilder, we are synchronized on fd
     */
    public boolean add(FeedData fd, ByteBuffer bb) {
        assert Thread.holdsLock(fd);

        final OrderedTickData td = ((OrderedFeedData) fd).getOrderedTickData();

        final int length = bb.remaining();
        long addr = td.getStoreAddress();
        long oldAddr = 0;

        OffHeapStore store;
        if (!isMemoryAddress(addr) || length > getRemainingAt(addr)) {
            oldAddr = addr;
            addr = getNextAddress(td.getAvgLength(), bb.remaining() + 8);
            if (addr == NOT_AN_ADDRESS) {
                this.numFailedAdds.incrementAndGet();
                return false;
            }
            store = getStore(addr);
            addr = store.putLong(addr, oldAddr);
            td.incLength(4); // for the length of this chunk
        }
        else {
            store = getStore(addr);
        }
        td.setStoreAddress(store.put(addr, bb));
        td.incLength(length);

        if (isMemoryAddress(oldAddr) && this.tickWriter != null) {
            if (td.isCorrectionPending()) {
                return false;
            }
            if (td.isEvictionPending()) {
                td.unsetEvictionPending();
                // we changed td's store address, which effectively cancels the eviction write, even
                // if it was supposed to write from oldAddr, so we have to add oldAddr for
                // writing again: the 'else' used in a previous version was wrong
            }
            this.tickWriter.add(fd, td, oldAddr);
            td.setWritePending();
        }
        return true;
    }

    int getRemainingAt(long addr) {
        return getStore(addr).getRemaining(addr);
    }

    long getNextAddress(final int desiredChunkSize, int minSize) {
        final int idx = getStoreIndex(desiredChunkSize >> this.newChunkLengthShift);
        final int minIdx = getMinStoreIndex(minSize);
        final int base = Math.max(idx, minIdx);

        int i = base;
        // first: look for an available chunk in stores[idx]..stores[minIdx], decreasing index,
        // so we will accept all smaller chunks that are available
        while (i >= minIdx) {
            final long result = getAddress(i);
            if (result >= 0) {
                return result;
            }
            i--;
        }

        // chunks seem to be in high demand: search for a chunk that is larger than desired, but only
        // up to a limit to avoid exhausting the relatively few largest chunks
        int cs = this.stores[base].getChunkSize();
        int numChunkSizeIncreases = 0;
        for (i = base + 1; i < this.stores.length && numChunkSizeIncreases < 3; i++) {
            final long result = getAddress(i);
            if (result >= 0) {
                return result;
            }
            if (this.stores[i].getChunkSize() != cs) {
                cs = this.stores[i].getChunkSize();
                numChunkSizeIncreases++;
            }
        }

        this.logger.warn("<getNextAddress> Unable to get address to store data: desired: {}, min: {}, status: {}", desiredChunkSize, minSize, this.status());

        return NOT_AN_ADDRESS;
    }

    /**
     * @return index of the smallest store whose chunk size is at least <tt>minSize</tt>
     * @throws java.lang.IllegalArgumentException if no store exists with chunk size &gt;= <tt>minSize</tt>
     */
    int getMinStoreIndex(int minSize) {
        for (int i = 0, n = this.storeChunkSizesLog2.length; i < n; i++) {
            if ((1 << this.storeChunkSizesLog2[i]) >= minSize) {
                return i;
            }
        }
        throw new IllegalArgumentException(Integer.toString(minSize));
    }

    /**
     * @return index of the smallest store whose chunk size is at least <tt>size</tt>, or the
     * index of the largest chunk if no such store exists. If that index belongs to a group of
     * equal sized stores, the maximum index within that group is returned.
     */
    int getStoreIndex(int size) {
        for (int i = 0, n = this.storeChunkSizesLog2.length; i < n; i++) {
            if ((1 << this.storeChunkSizesLog2[i]) >= size) {
                int j = i;
                while (++j < n && this.storeChunkSizesLog2[i] == this.storeChunkSizesLog2[j]) {
                    i = j;
                }
                return i;
            }
        }
        return this.storeChunkSizesLog2.length - 1;
    }

    long getAddress(int idx) {
        final int nextFree = this.stores[idx].getNextFree();
        return (nextFree < 0) ? NOT_AN_ADDRESS : (((long) idx) << 32) + nextFree;
    }

    void setFree(long addr) {
        getStore(addr).setAsFree(addr - 1);
    }

    public TickProvider.Result getTicks(FeedData fd, int day, int[] storageInfo) {
        //noinspection SynchronizationOnLocalVariableOrMethodParameter
        synchronized (fd) {
            final OrderedTickData td = ((OrderedFeedData) fd).getOrderedTickData();
            if (td == null) {
                return null;
            }
            final int length = td.getLength(day);
            if (length == 0) {
                return null;
            }

            return getTicks(fd, day, storageInfo, td, length);
        }
    }

    private TickProvider.Result getTicks(FeedData fd, int day, int[] storageInfo,
            OrderedTickData td, int totalLength) {

        long address = td.getStoreAddress(day);
        final int[] myStorageInfo = getStorageInfo(address, totalLength);

        int length = totalLength;

        if (storageInfo != null) {
            if (storageInfo[0] == myStorageInfo[0]) {
                // persistent data unchanged, only return what changed
                return getTicksIncrement(address, myStorageInfo, storageInfo);
            }
            length -= storageInfo[0]; // persistent part already stored by client
        }

        final ByteBuffer dst = ByteBuffer.allocate(length).order(ByteOrder.LITTLE_ENDIAN);
        int offset = dst.remaining();
        while (isMemoryAddress(address) && offset > 0) {
            final int chunkLength = getChunkLength(address);
            offset -= chunkLength;
            dst.position(offset);
            address = copyChunk(address, dst);
        }
        if (address == 0 || offset == 0) {
            return new Result(dst.array(), myStorageInfo);
        }
        dst.limit(offset);
        return this.fileTickStore.getTicks(fd, day, address, dst, myStorageInfo);
    }

    /**
     * Returns array with tick storage information. result[0] is the length of data that has already
     * been saved in a file, the remaining elements (if any) contain the length of tick chunks
     * that are still held in memory.
     * @param lastAddress address of current tick data end
     * @param length total length of tick data in bytes
     * @return storage info
     */
    private int[] getStorageInfo(long lastAddress, int length) {
        int[] info = new int[4]; // common case: length = 1, write-pending: 2, multiple writes (rarely): more
        int i = info.length;
        int inMemoryLength = 0;
        long address = lastAddress;
        while (isMemoryAddress(address)) {
            info[--i] = getChunkLength(address);
            inMemoryLength += info[i];
            address = getPrevious(address);
            if (i == 0) {
                info = growStorageInfo(info);
                i = STORAGE_INFO_INCREMENT;
            }
        }
        info[--i] = (length - inMemoryLength);
        return Arrays.copyOfRange(info, i, info.length);
    }

    private int[] growStorageInfo(int[] info) {
        final int[] result = new int[info.length + STORAGE_INFO_INCREMENT];
        System.arraycopy(info, 0, result, STORAGE_INFO_INCREMENT, info.length);
        return result;
    }

    int getData(long addr, byte[] dst) {
        final ByteBuffer bb = getBuffer(addr, 8);
        int result = bb.remaining();
        bb.get(dst, 0, result);
        return result;
    }

    /**
     * @param addrFrom any address; if not a memory address, all in-memory ticks will be returned.
     * @param addrTo a valid memory address
     * @return all ticks stored between <code>addrFrom</code> and <code>addrTo</code>. There are no
     * chunk lengths in the result, just ticks. Each tick starts with a 1 byte length, followed by
     * a flag byte, the tick time (int), and the tick fields.
     */
    byte[] getData(long addrFrom, long addrTo) {
        assert isMemoryAddress(addrTo);

        final OffHeapStore storeFrom = isMemoryAddress(addrFrom) ? getStore(addrFrom) : null;
        final int baseFrom = (storeFrom != null) ? storeFrom.getBaseAddress(addrFrom) : -1;

        long addr = addrTo;
        int n = 0;
        do {
            OffHeapStore store = getStore(addr);
            if (store == storeFrom && baseFrom == store.getBaseAddress(addr)) {
                // addr and addrFrom refer to the same chunk
                n += (store.getSize(addr) - store.getSize(addrFrom));
            }
            else {
                n += (store.getSize(addr) - 8);
            }
            addr = getPrevious(addr);
        } while (isMemoryAddress(addr));

        final byte[] result = new byte[n];

        addr = addrTo;
        int offset = result.length;
        do {
            ByteBuffer buffer = getBuffer(addr, 8);
            OffHeapStore store = getStore(addr);
            if (store == storeFrom && baseFrom == store.getBaseAddress(addr)) {
                buffer.position(buffer.position() + store.getSize(addrFrom) - 8);
            }
            offset -= buffer.remaining();
            buffer.get(result, offset, buffer.remaining());
            addr = getPrevious(addr);
        } while (isMemoryAddress(addr));

        return result;
    }

    /**
     * To be used if s.th. goes horribly wrong when using address <code>addr</code>. Creates a
     * string that describes the current address and then follows the previous address pointers
     * to describe those, too, until a non-memory or invalid address is encountered.
     * Callers should be
     * synchronized on the FeedData object that contains the TickData object that refers to that
     * address. This method will not throw an exception, even if an invalid address is encountered
     * @param addr to be explained
     * @return explanatory string
     */
    String explain(long addr) {
        if (addr == 0L) {
            return "0L";
        }
        StringBuilder sb = new StringBuilder(128);
        long end = explain(addr, sb);
        if (end < 0 && end != NOT_AN_ADDRESS) {
            FileTickStore.append(end, sb);
        }
        return sb.toString();
    }

    /**
     * Appends explanation of tick memory pointed at by <code>addr</code> to <code>sb</code>
     * and returns the first non-memory tick-address encountered, which can later be used to
     * explain the ticks already stored in a file.
     * @param addr current memory tick data address
     * @param sb dest for explanation
     * @return non memory tick address
     */
    long explain(long addr, StringBuilder sb) {
        long k = addr;
        int n = 0;
        while (isMemoryAddress(k) && ++n < 256) {
            if (sb.length() > 0) sb.append(" -> ");
            sb.append("0x").append(Long.toHexString(k));
            int idx = decodeStoreIndex(k);
            sb.append("[").append(idx).append("]");
            if (idx < 0 || idx >= this.stores.length) {
                sb.append(" not in [0..").append(this.stores.length - 1).append("]!");
                return NOT_AN_ADDRESS;
            }
            OffHeapStore s = this.stores[idx];
            sb.append("{0x").append(Long.toHexString(s.getBaseAddress(k)))
                    .append(", ").append(s.getChunkSize())
                    .append("/").append(s.getSize(k))
                    .append("/").append(s.getRemaining(k))
                    .append("}");

            k = getPrevious(k);
        }
        if (n == 256) {
            sb.append(" cannot find end!");
            return NOT_AN_ADDRESS;
        }
        return k;
    }

    void setPrevious(long addr, long value) {
        getBuffer(addr, 0).putLong(value);
    }

    long getPrevious(long addr) {
        return getBuffer(addr, 0).getLong();
    }

    private long copyChunk(long addr, ByteBuffer dst) {
        final ByteBuffer bb = getBuffer(addr, 0);
        final long result = bb.getLong();
        dst.putInt(bb.remaining()); // positive = uncompressed
        dst.put(bb);
        return result;
    }

    private TickProvider.Result getTicksIncrement(long address, int[] myStorageInfo,
            int[] storageInfo) {

        if (storageInfo.length < myStorageInfo.length) {
            return getTicksIncrement(address, myStorageInfo, expand(storageInfo, myStorageInfo.length));
        }

        final int length = computeIncrementLength(myStorageInfo, storageInfo);
        if (length == 0) {
            return new Result(null, myStorageInfo);
        }

        final ByteBuffer dst = ByteBuffer.allocate(length).order(ByteOrder.LITTLE_ENDIAN);
        int offset = dst.remaining();
        for (int i = myStorageInfo.length - 1; offset > 0; i--) {
            final int incLength = (myStorageInfo[i] - storageInfo[i]);
            offset -= incLength;
            dst.position(offset).limit(offset + incLength);
            address = copyIncrement(address, dst);
        }
        return new Result(dst.array(), myStorageInfo);
    }

    private int[] expand(int[] storageInfo, final int length) {
        final int[] result = Arrays.copyOf(storageInfo, length);
        // new 0 at end are replaced with 4; elements from this array will be subtracted from
        // myStorageInfo elements to compute the increment length w/o the 4 byte chunk length
        Arrays.fill(result, storageInfo.length, length, 4);
        return result;
    }

    private int computeIncrementLength(int[] myStorageInfo, int[] storageInfo) {
        int result = 0;
        for (int i = 1; i < myStorageInfo.length; i++) {
            result += (myStorageInfo[i] - storageInfo[i]);
        }
        return result;
    }

    private long copyIncrement(long addr, ByteBuffer dst) {
        final ByteBuffer bb = getBuffer(addr, 0);
        final long result = bb.getLong();
        if (dst.remaining() < bb.remaining()) {
            bb.position(bb.limit() - dst.remaining());
        }
        dst.put(bb);
        return result;
    }

    ByteBuffer getBuffer(long addr, int offset) {
        final OffHeapStore s = getStore(addr);
        return (ByteBuffer) s.duplicateBuffer()
                .position(s.getBaseAddress(addr) + offset).limit((int) addr);
    }

    private int getChunkLength(long address) {
        // 8 = first eight bytes, pointer to previous, 4 = length
        return getStore(address).getSize(address) - 8 + 4;
    }

    private OffHeapStore getStore(long address) {
        return this.stores[decodeStoreIndex(address)];
    }

    static int decodeStoreIndex(long key) {
        return ((int) (key >>> 32)) & 0xFF;
    }

    int getNumChunksFree() {
        int result = 0;
        for (OffHeapStore store : stores) {
            result += store.getNumChunksFree();
        }
        return result;
    }

    long getNumBytesFree() {
        long result = 0;
        for (OffHeapStore store : stores) {
            result += store.getNumBytesFree();
        }
        return result;
    }

    public long getByteCapacity() {
        return numStoreBytes;
    }

    public int getChunkCapacity() {
        return numStoreChunks;
    }

    @Monitor(name = "numBytesFree", type = GAUGE)
    public int getNumBytesFreePct() {
        return (int) (getNumBytesFree() * 100 / this.numStoreBytes);
    }

    @Monitor(name = "numChunksFree", type = GAUGE)
    public int getNumChunksFreePct() {
        return (int) (getNumChunksFree() * 100L / this.numStoreChunks);
    }
}
