/*
 * FileTickStore.java
 *
 * Created on 19.11.12 08:04
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.ordered.tick;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.netflix.servo.annotations.Monitor;
import net.jcip.annotations.GuardedBy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import de.marketmaker.istar.common.util.ByteString;
import de.marketmaker.istar.common.util.DateUtil;
import de.marketmaker.istar.common.util.FileUtil;
import de.marketmaker.istar.feed.FeedData;
import de.marketmaker.istar.feed.FeedDataRegistry;
import de.marketmaker.istar.feed.ordered.OrderedFeedData;
import de.marketmaker.istar.feed.tick.AbstractTickRecord;
import de.marketmaker.istar.feed.tick.TickFiles;
import de.marketmaker.istar.feed.tick.TickProvider;
import de.marketmaker.istar.feed.vwd.VendorkeyVwd;

import static com.netflix.servo.annotations.DataSourceType.COUNTER;
import static de.marketmaker.istar.feed.tick.AbstractTickRecord.TickItem.Encoding.DUMP3;
import static de.marketmaker.istar.feed.tick.AbstractTickRecord.TickItem.Encoding.TICK3;

/**
 * Keeps track of all tick files for a certain day. Tick files will be created as necessary in a
 * directory named yyyyMMdd relative to {@link #baseDir} and the files in the directory will be
 * named <em>Market-yyyyMMdd</em>.tmp or <em>Market-yyyyMMdd</em>.td3. A file whose name ends with
 * <tt>.tmp</tt> is currently open for writing and data can be appended at any time. The data in
 * such a file can only be interpreted using in memory data stored in {@link OrderedTickData} objects.
 * If a chicago process crashes, the data stored in its <tt>.tmp</tt> files is effectively lost, as
 * it does not contain any information about which tick data chunks belong to which symbol.<p>
 * During regular shutdown or whenever data for a previous day is finally written, a B*-like index
 * structure will be appended to the tickfile which contains information about the symbols for
 * which tick data is contained in the file, where that data can be found and the total number of
 * tick data bytes for that symbol. After the index has been written, the file's suffix will be
 * changed to <tt>.td3</tt><p>
 * During startup, the index information from <tt>.td3</tt> files will be read and used to initialize
 * <tt>OrderedTickData</tt> objects. After that, the file will be truncated at the starting position
 * of the B*-index and the suffix will be changed to <tt>.tmp</tt>.
 *
 *
 * @author oflege
 */
public class FileTickStore implements InitializingBean {
    public static final String TD3 = ".td3";

    public static final String TDZ = ".tdz";

    public static final String DD3 = ".dd3";

    public static final String DDZ = ".ddz";

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private FeedDataRegistry registry;

    private String snappySuffix = TD3;

    private AbstractTickRecord.TickItem.Encoding encoding = TICK3;

    private File baseDir;

    private int typeMapping;

    @GuardedBy("this.files")
    private final Map<MarketDay, TickFile> files = new HashMap<>();

    @Monitor(type = COUNTER)
    private static final AtomicInteger numReads = new AtomicInteger();

    @Monitor(type = COUNTER)
    private static final AtomicLong numBytesRead = new AtomicLong();

    public void setUseDumpFileSuffixes(boolean useDumpFileSuffixes) {
        this.snappySuffix = useDumpFileSuffixes ? DD3 : TD3;
        this.encoding = useDumpFileSuffixes ? DUMP3 : TICK3;
    }

    public void setBaseDir(File baseDir) {
        this.baseDir = baseDir;
    }

    public void setRegistry(FeedDataRegistry registry) {
        this.registry = registry;
    }

    public void setTypeMapping(int typeMapping) {
        this.typeMapping = typeMapping;
    }

    public int getNumReads() {
        return numReads.get();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (this.registry != null) {
            if (!FileUtil.isDirWriteable(this.baseDir)) {
                throw new IllegalArgumentException("cannot use baseDir: " + this.baseDir);
            }
            addExistingFiles(DateUtil.getDate(0));
        }
    }

    private void addExistingFiles(int yyyymmdd) throws IOException {
        final File todaysDir = getDirectory(yyyymmdd);
        if (!FileUtil.isDirWriteable(todaysDir)) {
            throw new IllegalArgumentException("cannot use dir: " + todaysDir.getAbsolutePath());
        }

        final Pattern p = Pattern.compile("\\S+-" + yyyymmdd + Pattern.quote(this.snappySuffix));

        final File[] files = todaysDir.listFiles((dir, name) -> {
            return p.matcher(name).matches();
        });

        for (File file : files) {
            try {
                addFile(file, yyyymmdd);
            } catch (Exception e) {
                throw new IOException("failed to add " + file.getName(), e);
            }
        }
    }

    void addFile(File f, final int yyyymmdd) throws IOException {
        final ByteString market = new ByteString(TickFiles.getMarketName(f));
        try (final FileChannel fc = createChannel(f)) {
            long indexStart = TickFileIndexReader.readEntries(fc, new DefaultIndexHandler(f) {
                @Override
                protected void doHandle(ByteString vwdcode, long position, int length) {
                    FileTickStore.this.handle(vwdcode, position, length, yyyymmdd);
                }
            });
            addTickFile(MarketDay.create(market, yyyymmdd), new TickFile(f, indexStart));
            if (this.logger.isDebugEnabled()) {
                this.logger.debug("<addFile> " + f.getName());
            }
        }
    }

    private void handle(ByteString key, long position, int length, int day) {
        FeedData fd = this.registry.get(key);
        if (fd == null && this.typeMapping != 0) {
            fd = this.registry.register(VendorkeyVwd.getInstance(key, this.typeMapping));
        }
        if (fd != null) {
            OrderedTickData td = ((OrderedFeedData) fd).getOrderedTickData();
            td.setDate(day);
            td.setStoreAddress(position);
            td.setLength(length);
        }
    }

    static String toVwdcodeWithoutMarket(String vendorkey) {
        final Matcher m = VendorkeyVwd.KEY_PATTERN.matcher(vendorkey);
        if (!m.matches()) {
            return null;
        }
        return toVwdcodeWithoutMarket(m);
    }

    static String toVwdcodeWithoutMarket(Matcher m) {
        assert m.pattern() == VendorkeyVwd.KEY_PATTERN;
        return (m.group(4) == null) ? m.group(2) : (m.group(2) + m.group(4));
    }

    public byte[] readTicks(File f, String vendorkey) throws IOException {
        final String key = toVwdcodeWithoutMarket(vendorkey);
        if (key == null) {
            this.logger.warn("<readTicks> invalid vendorkey '" + vendorkey + "'");
            return null;
        }

        try (FileChannel fc = createChannel(f)) {
            final long[] addressAndLength = new TickFileIndexReader(fc).find(new ByteString(key));
            if (addressAndLength == null) {
                if (this.logger.isDebugEnabled()) {
                    this.logger.debug("<readTicks> no data for " + vendorkey + " in " + f.getName());
                }
                return null;
            }

            final ByteBuffer bb = ByteBuffer.allocate((int) addressAndLength[1]);
            int numSeeks = fillData(fc, addressAndLength[0], bb);
            if (this.logger.isDebugEnabled()) {
                this.logger.debug("<readTicks> @" + decodePosition(addressAndLength[0])
                        + ", #bytes=" + bb.capacity() + ", #seeks=" + numSeeks);
            }
            return bb.array();
        }
    }

    TickProvider.Result getTicks(final FeedData fd, final int day, final long address,
            final ByteBuffer dst, final int[] storageInfo) {

        return new TickProvider.Result() {
            private byte[] ticks;

            @Override
            public byte[] getTicks() {
                if (this.ticks == null) {
                    this.ticks = doRead(fd, day, address, dst);
                }
                return this.ticks;
            }

            @Override
            public AbstractTickRecord.TickItem.Encoding getEncoding() {
                return encoding;
            }

            @Override
            public int[] getStorageInfo() {
                return storageInfo;
            }
        };
    }

    private byte[] doRead(FeedData fd, int day, long startAddress, ByteBuffer dst) {
        final MarketDay md = MarketDay.create(fd.getMarket().getName(), day);
        final TickFile tf = getTickFile(md);

        if (tf != null) { // default for intraday requests
            try {
                if (tf.fillData(startAddress, dst)) {
                    return dst.array();
                }
            } catch (IOException e) {
                this.logger.error("<doRead> failed for " + fd.getVwdcode() + " day=" + day
                        + ", startAddress=" + startAddress, e);
                return new byte[0];
            }
        }

        // request may be for yesterday's file which is already closed (and has been removed from
        // this.files)
        final File f = getFile(md.market, md.day);

        if (f.canRead()) {
            try (FileChannel fc = createChannel(f)) {
                fillData(fc, startAddress, dst);
                return dst.array();
            } catch (IOException e) {
                this.logger.error("<doRead> failed for " + fd.getVwdcode() + " day=" + day
                        + ", startAddress=" + startAddress, e);
                return new byte[0];
            }
        }
        this.logger.error("<doRead> no TickFile for " + fd + "/" + day);
        return new byte[0];
    }

    private static void ackRead(int limit) {
        numReads.incrementAndGet();
        numBytesRead.addAndGet(limit);
    }

    static void append(long addr, StringBuilder sb) {
        if (sb.length() > 0) {
            sb.append(" -> ");
        }
        sb.append("File@").append(FileTickStore.decodePosition(addr))
                .append('#').append(FileTickStore.decodeLength(addr));
    }

    public static int fillData(FileChannel ch, long startAddress, ByteBuffer dst)
            throws IOException {
        final int limit = dst.limit();

//        System.out.printf("dst length %8d%n", dst.remaining());
//        System.out.printf("%16s %8s %6s %6s%n", "address", "pos", "start", "length");
        int numSeeks = 0;

        long address = startAddress;

        final ByteBuffer addressBuffer = createAddressBuffer();
        final ByteBuffer[] dsts = new ByteBuffer[]{addressBuffer, dst};

        // dst.limit is checked as we may only want to read the last chunk(s) of data
        while (address != 0 && dst.limit() > 0) {
            final long p = decodePosition(address);
            final int length = decodeLength(address) - 8;
            final int start = dst.limit() - length;

//            System.out.printf("%16s %8d %6d %6d%n", Long.toHexString(address), p, start, length);
            ch.position(p);
            numSeeks++;

            dst.position(start);
            addressBuffer.clear();

            ch.read(dsts);

            addressBuffer.flip();

            address = addressBuffer.getLong();
            assert TickWriter.isFileAddress(address) : Long.toHexString(address);

            dst.limit(start);
        }
        ackRead(limit);
        return numSeeks;
    }

    private static ByteBuffer createAddressBuffer() {
        return ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN);
    }

    public static long decodePosition(long address) {
        return (address ^ TickWriter.FILE_ADDRESS_FLAG) >>> TickWriter.POSITION_SHIFT;
    }

    public static int decodeLength(long address) {
        return (int) (address & TickWriter.LENGTH_MASK);
    }

    File getFile(final ByteString marketName, int day) {
        final File dir = getDirectory(day);
        return new File(dir, marketName + "-" + day + this.snappySuffix);
    }

    public File getDirectory(int day) {
        return new File(this.baseDir, Integer.toString(day));
    }

    private FileChannel createChannel(File f) throws FileNotFoundException {
        return new RandomAccessFile(f, "r").getChannel();
    }

    TickFile getOrCreateTickFile(MarketDay marketDay) throws IOException {
        synchronized (this.files) {
            TickFile result = this.files.get(marketDay);
            if (result == null) {
                File f = getFile(marketDay.market, marketDay.day);
                if (!FileUtil.isDirWriteable(f.getParentFile())) {
                    this.logger.error("<getOrCreateTickFile> cannot use " + f.getParentFile().getAbsolutePath());
                    throw new IOException();
                }
                if (f.canRead()) {
                    this.logger.error("<getOrCreateTickFile> found existing file " + f.getName());
                    if (!f.delete()) {
                        throw new IOException();
                    }
                }
                addTickFile(marketDay, result = new TickFile(f));
            }
            return result;
        }
    }

    TickFile getTickFile(MarketDay md) {
        synchronized (this.files) {
            return this.files.get(md);
        }
    }

    private void addTickFile(MarketDay md, TickFile tf) {
        synchronized (this.files) {
            this.files.put(md, tf);
        }
    }

    public void removeTickFile(MarketDay md) {
        synchronized (this.files) {
            final TickFile tf = this.files.remove(md);
            assert !tf.isOpen();
        }
    }

    public static boolean canHandle(File file) {
        return file.getName().endsWith(TD3) || file.getName().endsWith(TDZ);
    }

    void explain(FeedData fd, int day, long startAddr, StringBuilder sb) {
        final File dir = getDirectory(day);
        if (!dir.isDirectory()) {
            sb.append(", no such dir ").append(dir.getAbsolutePath());
            return;
        }

        final String prefix = fd.getMarket().getName() + "-" + day;
        File[] tmpFiles = dir.listFiles((dir1, name) -> {
            return name.startsWith(prefix);
        });
        if (tmpFiles.length == 0) {
            sb.append(", no file ").append(prefix).append("* in ").append(dir.getAbsolutePath());
            return;
        }

        explain(tmpFiles[0], startAddr, sb);
    }

    int explain(File f, long startAddr, StringBuilder sb) {
        long addr = startAddr;
        append(addr, sb);

        final ByteBuffer addressBuffer = createAddressBuffer();
        long lengthInFile = 0;
        int numSeeks = 0;
        try (FileChannel ch = createChannel(f)) {
            do {
                if (addr != startAddr) {
                    append(addr, sb);
                }
                numSeeks++;
                lengthInFile += (decodeLength(addr) - 8);

                ch.position(decodePosition(addr));
                addressBuffer.clear();
                ch.read(addressBuffer);
                addressBuffer.flip();
                addr = addressBuffer.getLong();
            } while (addr < 0);
            if (addr > 0) {
                sb.append(", INVALID: memory address 0x").append(Long.toHexString(addr));
            }
            sb.append(", #flength=").append(lengthInFile).append(", #seeks=").append(numSeeks);
            return (int) lengthInFile;
        } catch (IOException e) {
            sb.append(", IOException: ").append(e);
            return -1;
        }
    }

    public static void main(String[] args) throws IOException {
        final FileTickStore fts = new FileTickStore();
        final byte[] bytes = fts.readTicks(new File(args[0]), args[1]);
        System.out.println(bytes == null ? "null" : Integer.toString(bytes.length));
    }
}
