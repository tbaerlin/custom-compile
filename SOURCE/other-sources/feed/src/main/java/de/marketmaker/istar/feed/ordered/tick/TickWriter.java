/*
 * TickCompressor.java
 *
 * Created on 19.11.12 10:40
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.ordered.tick;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import com.netflix.servo.annotations.Monitor;
import net.jcip.annotations.GuardedBy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.jmx.export.annotation.ManagedAttribute;

import de.marketmaker.istar.feed.FeedData;
import de.marketmaker.istar.feed.FeedMarket;
import de.marketmaker.istar.feed.ordered.OrderedFeedData;

import static com.netflix.servo.annotations.DataSourceType.COUNTER;
import static java.lang.Math.abs;

/**
 * Asynchronously appends tick data to tick files. In order to write as efficient as possible, chunks
 * for the same file will be collected and then written in batches. Synchronization on <tt>FeedData</tt>
 * objects is only necessary a) when data is submitted for writing
 * (see {@link #add(FeedData, OrderedTickData, long)}
 * and b) when {@link #commitWrite(OrderedTickData, int, long, long)} is called to update the
 * <tt>OrderedTickData</tt> object with information about the newly written tick data. Until
 * <tt>commitWrite</tt> is called, the tick data will be read from memory although it has already
 * been compressed and appended to the tick file.
 *
 * @author oflege
 */
public class TickWriter implements DisposableBean {

    static final long FILE_ADDRESS_FLAG = 0x8000000000000000L;

    static final int POSITION_SHIFT = 24;

    static final int LENGTH_MASK = (1 << POSITION_SHIFT) - 1;

    private static final int MIN_REMAINING = 65536;

    static final int MAX_UNCOMPRESSED_CHUNK_SIZE = 0xFFFF;

    /**
     * Batch of data objects for which tick data will be compressed and appended to the same file.
     */
    protected class Task implements Runnable {
        private final FeedData[] data;

        /**
         * MemoryTickStore addresses that define data to be written to disk; if the write was
         * triggered by idle data eviction, we use -addr to be able to detect that later.
         */
        private final long[] addrs;

        private int n = 0;

        private final MarketDay marketDay;

        private boolean idle;

        protected Task(MarketDay marketDay) {
            this.marketDay = marketDay;
            this.data = new FeedData[taskBatchSize];
            this.addrs = new long[data.length];
        }

        @Override
        public String toString() {
            return "Task[" + this.marketDay + ", #" + n + "]";
        }

        public boolean add(FeedData fd, long addr) {
            this.data[this.n] = fd;
            this.addrs[this.n++] = addr;
            this.idle = false;
            return this.n == this.data.length;
        }

        long getMemoryAddress(int i) {
            final long addr = this.addrs[i];
            return addr < 0 ? -addr : addr;
        }

        @Override
        public void run() {
            try {
                if (this.n > 0) {
                    doWrite(this);
                    this.n = 0;
                }
            } catch (IOException e) {
                logger.error("<doWrite> failed", e);
            }
        }
    }

    @GuardedBy("this.taskMap")
    private final Map<MarketDay, Task> taskMap = new HashMap<>();

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private FileTickStore fileTickStore;

    private MemoryTickStore memoryTickStore;

    // has to be direct, otherwise snappy won't work
    private final ByteBuffer output
            = ByteBuffer.allocateDirect(512 * 1024).order(ByteOrder.LITTLE_ENDIAN);

    private volatile int maxWorkQueueLength = 0;

    private final BlockingQueue<Runnable> workQueue;

    private final ExecutorService es;

    @Monitor(type = COUNTER)
    private final AtomicLong numBytesIn = new AtomicLong();

    @Monitor(type = COUNTER)
    private final AtomicLong numBytesOut = new AtomicLong();

    @Monitor(type = COUNTER)
    private final AtomicInteger numWrites = new AtomicInteger();

    private final int taskBatchSize;

    private int idleBits = 0;

    public TickWriter() {
        this(64);
    }

    public void setIdleCount(int n) {
        this.idleBits = TickStats.toIdleBits(n);
    }

    int getIdleBits() {
        return this.idleBits;
    }

    public TickWriter(int taskBatchSize) {
        this(taskBatchSize, new ThreadPoolExecutor(1, 1,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(), r -> {
                    return new Thread(r, TickWriter.class.getSimpleName() + "-es");
                }));
    }

    protected TickWriter(int taskBatchSize, ExecutorService es) {
        this.taskBatchSize = taskBatchSize;
        this.es = es;
        if (this.es instanceof ThreadPoolExecutor) {
            this.workQueue = ((ThreadPoolExecutor) this.es).getQueue();
        }
        else {
            this.workQueue = new ArrayBlockingQueue<>(1);
        }
    }

    @ManagedAttribute
    public long getNumBytesIn() {
        return this.numBytesIn.get();
    }

    @ManagedAttribute
    public long getNumBytesOut() {
        return this.numBytesOut.get();
    }

    @ManagedAttribute
    public int getNumWrites() {
        return this.numWrites.get();
    }

    public int getMaxWorkQueueLength() {
        return maxWorkQueueLength;
    }

    public int getCurrentWorkQueueLength() {
        return this.workQueue.size();
    }

    public void setMemoryTickStore(MemoryTickStore memoryTickStore) {
        this.memoryTickStore = memoryTickStore;
    }

    public void setFileTickStore(FileTickStore fileTickStore) {
        this.fileTickStore = fileTickStore;
    }

    @Override
    public void destroy() throws Exception {
        this.logger.info("<destroy>");
        if (this.es != null) {
            this.es.shutdown();
            if (!this.es.awaitTermination(60, TimeUnit.SECONDS)) {
                this.logger.error("<destroy> executor did not join within 60s, exiting anyway");
            }
        }
    }

    private Task removeTask(MarketDay md) {
        synchronized (this.taskMap) {
            return this.taskMap.remove(md);
        }
    }

    Future<?> flushTask(MarketDay md) {
        synchronized (this.taskMap) {
            Task task = this.taskMap.remove(md);
            return (task != null) ? submitTask(task) : null;
        }
    }

    /**
     * to be called by external scheduler
     */
    void flushIdleTasks() {
        final int n;
        int numFlushed = 0;
        synchronized (this.taskMap) {
            n = this.taskMap.size();
            final Iterator<Map.Entry<MarketDay, Task>> it = taskMap.entrySet().iterator();
            while (it.hasNext()) {
                final Map.Entry<MarketDay, Task> e = it.next();
                if (e.getValue().idle) {
                    it.remove();
                    submitTask(e.getValue());
                    numFlushed++;
                }
                else {
                    e.getValue().idle = true;
                }
            }
        }
        if (this.logger.isDebugEnabled()) {
            this.logger.debug("<flushIdleTasks> flushed " + numFlushed + " of " + n + " tasks");
        }
    }

    /**
     * called by parser thread(s) -> TickBuilder -> MemoryTickStore
     */
    void add(FeedData fd, OrderedTickData td, long addr) {
        add(fd, td, addr, MarketDay.create(fd.getMarket().getName(), td.getDate()));
    }

    /**
     * called when data for td should be evicted to free some tick memory
     */
    Future<?> addIdle(FeedData fd, OrderedTickData td, long addr, MarketDay md) {
        // negative addr indicates idle write
        return add(fd, td, -addr, md);
    }

    private Future<?> add(FeedData fd, OrderedTickData td, long addr, MarketDay md) {
        assert Thread.holdsLock(fd);

        Task toSubmit = null;
        synchronized (this.taskMap) {
            Task t = this.taskMap.get(md);
            if (t == null || td.isWritePending()) {
                if (t != null) {
                    submitTask(t);
                }
                this.taskMap.put(md, t = new Task(md));
            }
            if (t.add(fd, addr)) {
                toSubmit = this.taskMap.remove(md);
            }
        }
        return (toSubmit != null) ? submitTask(toSubmit) : null;
    }

    private Future<?> submitTask(Task toSubmit) {
        final Future<?> result = this.es.submit(toSubmit);
        int size = this.workQueue.size();
        if (size > this.maxWorkQueueLength) {
            this.maxWorkQueueLength = size;
        }
        return result;
    }

    protected void doWrite(Task t) throws IOException {
        final TickFile tf = this.fileTickStore.getOrCreateTickFile(t.marketDay);
        long position = tf.position();
        this.output.clear();

        final long[] addresses = new long[t.n];
        final int[] lengthIncrements = new int[t.n];

        for (int i = 0; i < t.n; i++) {
            if (this.output.remaining() < MIN_REMAINING) {
                flushOutput(tf);
            }

            final long addr = t.getMemoryAddress(i);
            final ByteBuffer uncompressed = this.memoryTickStore.getBuffer(addr, 0);

            // pointer to previous, always uncompressed
            final long previous = uncompressed.getLong();
            if (!isFileAddress(previous)) {
                // log as much info as possible to be able to track down the cause of this error
                // we still write the corrupted data as repairing it is not so easy...
                String fdStr = t.data[i].toString();
                this.logger.error("<doWrite> invalid previous address for " + fdStr
                    + ", memory=" + this.memoryTickStore.explain(addr));
            }
            this.output.putLong(previous);

            final int num = uncompressed.remaining();
            this.numBytesIn.addAndGet(num);

            int positionIncrement = 8; // ptr to previous

            if (num <= 63) {
                this.output.putInt(num);
                this.output.put(uncompressed);
                positionIncrement += (num + 4);
            }
            else {
                final int limit = uncompressed.limit();
                int numCompressed = 0;
                int numChunks = 0;
                do {
                    if (this.output.remaining() < MIN_REMAINING) {
                        flushOutput(tf);
                    }
                    int end = chunkEnd(uncompressed);
                    uncompressed.limit(end);
                    numCompressed += compressChunk(uncompressed);
                    numChunks++;
                    uncompressed.limit(limit).position(end);
                } while (uncompressed.hasRemaining());

                positionIncrement += (numCompressed + (4 * numChunks));
                // the 4 bytes for the header of the first chunk was already added in
                // MemoryTickStore#add
                lengthIncrements[i] = numCompressed - num + (4 * (numChunks - 1));
            }

            addresses[i] = toFileAddress(position, positionIncrement);
            position += positionIncrement;
        }

        flushOutput(tf);

        commitWrite(t, addresses, lengthIncrements);
    }

    public static boolean isFileAddress(long addr) {
        return addr <= 0;
    }

    private int compressChunk(ByteBuffer uncompressed) {
        final int lengthPos = output.position();
        output.position(output.position() + 4);
        compress(uncompressed);
        final int numCompressed = output.limit() - output.position();
        output.putInt(lengthPos, -numCompressed);
        output.position(output.limit()).limit(output.capacity());
        return numCompressed;
    }

    /**
     * @param bb contains uncompressed messages
     * @return a message boundary p so that p - bb.position() <= MAX_UNCOMPRESSED_CHUNK_SIZE
     */
    private int chunkEnd(ByteBuffer bb) {
        if (bb.remaining() <= MAX_UNCOMPRESSED_CHUNK_SIZE) {
            return bb.limit();
        }
        int p = bb.position();
        // Math.min ensures that the final chunk is not a very small one 
        final int limit = p + Math.min(MAX_UNCOMPRESSED_CHUNK_SIZE, bb.remaining() >> 1);
        int tickLength = 0;
        while (p + tickLength < limit) {
            p += tickLength;
            tickLength = getTickLength(bb, p);
        }
        return p;
    }

    protected int getTickLength(ByteBuffer bb, int p) {
        return bb.get(p) & 0xFF;
    }

    private void compress(ByteBuffer uncompressed) {
        try {
            org.xerial.snappy.Snappy.compress(uncompressed, this.output);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void flushOutput(TickFile tf) throws IOException {
        this.numWrites.incrementAndGet();
        this.numBytesOut.addAndGet(this.output.position());

        this.output.flip();
        tf.write(this.output);
        this.output.clear();
    }

    private void commitWrite(Task t, long[] addresses, int[] lengthIncrements) {
        final int day = t.marketDay.day;
        for (int i = 0; i < t.n; i++) {
            long addr = t.addrs[i];

            final FeedData fd = t.data[i];
            synchronized (fd) {
                OrderedTickData td = ((OrderedFeedData) fd).getOrderedTickData();
                try {
                    if (!commitWrite(td, day, addr, addresses[i])) {
                        continue;
                    }
                    if (lengthIncrements[i] != 0) {
                        td.incLength(day, lengthIncrements[i]);
                    }
                } catch (ArrayIndexOutOfBoundsException e) {
                    this.logger.error("<commitWrite> failed for " + fd + " " + td
                            + ", addr=0x" + Long.toHexString(addr)
                            + this.memoryTickStore.explain(abs(addr)));
                    td.reset();
                }
            }
            this.memoryTickStore.setFree(abs(addr));
        }
    }

    /**
     * Commits a write for td on day
     * @param td tick data owner
     * @param day tick data day
     * @param srcAddr address that had been submitted for writing. If td's data was evicted,
     * -srcAddr pointed to the current live chunk at the time of eviction; otherwise, a chunk
     * was too full to accept new tick data, so that a new chunk had been allocated and the now
     * previous chunk had been submitted for writing.
     * @param writeAddress address of the written chunk in the file
     * @return true iff the commit succeeded and therefore the chunk referred to by srcAddr should be
     * released; otherwise, the commit has been aborted and the data at srcAddr will be written
     * later and must not yet be released.
     */
    private boolean commitWrite(final OrderedTickData td, int day,
            long srcAddr, long writeAddress) {
        long storeAddr = td.getStoreAddress(day);
        if (srcAddr < 0) { // write was triggered by eviction
            td.unsetEvictionPending();
            if (-srcAddr != storeAddr) {
                // data was appended to the chunk that we just wrote; since that chunk will again be
                // written later, we abort the eviction and just ignore the write (i.e., do not
                // store writeAddress) and do not free the chunk pointed to be srcAddr.
                return false;
            }
        }
        if (abs(srcAddr) == storeAddr) {
            // happens on eviction and when tick data is stored at and of day or shutdown
            td.setStoreAddress(day, writeAddress);
            td.setIdleBits(this.idleBits);
        }
        else {
            // "normal" case for chunks that are written after they became the previous chunk
            boolean firstPrevious = true;
            long previous = this.memoryTickStore.getPrevious(storeAddr);
            while (previous != srcAddr) {
                storeAddr = previous;
                previous = this.memoryTickStore.getPrevious(storeAddr);
                firstPrevious = false;
            }
            this.memoryTickStore.setPrevious(storeAddr, writeAddress);
            if (firstPrevious && day == td.getDate()) {
                td.unsetWritePending();
            }
        }
        return true;
    }

    static long toFileAddress(long position, int num) {
        return FILE_ADDRESS_FLAG | (position << POSITION_SHIFT) | num;
    }

    /**
     * Schedules a task that will write all tick data for all of m's FeedDatas on day (that are
     * still in the MemoryTickStore) into the respective tick file.
     * @param m market to be processed
     * @param day yyyyMMdd of day to be written
     * @return if m has any FeedData with ticks for day, the list of m's FeedDatas, null otherwise
     */
    Future<List<FeedData>> write(final FeedMarket m, final int day) {
        return es.submit(() -> doWrite(m, day));
    }

    private List<FeedData> doWrite(FeedMarket m, int day) {
        final MarketDay md = MarketDay.create(m.getName(), day);
        final Task pending = removeTask(md);
        if (pending != null) {
            pending.run();
        }

        final Task t = new Task(md);

        final List<FeedData> elements = m.getElements(true);
        for (FeedData fd : elements) {
            final long addr;
            final OrderedTickData td = ((OrderedFeedData) fd).getOrderedTickData();
            synchronized (fd) {
                if (td.getLength(day) == 0) {
                    continue;
                }
                addr = td.getStoreAddress(day);
                if (isFileAddress(addr)) {
                    continue;
                }
            }
            if (t.add(fd, addr)) {
                // we do already run in es's single thread, so do no use submitTask to run t
                t.run();
            }
        }
        t.run();
        return elements;
    }
}
