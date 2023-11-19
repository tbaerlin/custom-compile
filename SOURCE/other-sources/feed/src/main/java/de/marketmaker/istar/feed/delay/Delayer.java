/*
 * Delayer.java
 *
 * Created on 07.02.2005 16:25:26
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.delay;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.SortedMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import com.netflix.servo.annotations.Monitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.Lifecycle;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;

import de.marketmaker.istar.common.io.DataFile;
import de.marketmaker.istar.common.statistics.HasStatistics;
import de.marketmaker.istar.common.util.NumberUtil;
import de.marketmaker.istar.common.util.TimeTaker;
import de.marketmaker.istar.feed.BinaryRecordHandler;
import de.marketmaker.istar.feed.DateTimeProvider;
import de.marketmaker.istar.feed.DateTimeProviderImpl;
import de.marketmaker.istar.feed.FeedData;
import de.marketmaker.istar.feed.ParsedRecord;
import de.marketmaker.istar.feed.connect.BufferWriter;

import static com.netflix.servo.annotations.DataSourceType.COUNTER;
import static com.netflix.servo.annotations.DataSourceType.GAUGE;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
@ManagedResource
public class Delayer implements InitializingBean, DisposableBean, Lifecycle, Runnable,
        HasStatistics, BinaryRecordHandler {
    private static final DateTimeFormatter TIME = DateTimeFormat.forPattern("HH:mm:ss");

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    protected ByteOrder byteOrder = ByteOrder.BIG_ENDIAN;

    protected DelayProvider delayProvider;

    private final Object writeMutex = new Object();

    private BufferWriter handler;

    private File dumpFile;

    private long poolCapacity;

    private int poolChunkSize = 1 << 20;

    /**
     * Delay buckets sorted by release time (millis since 1970-01-01)
     */
    private final SortedMap<Long, DelayBucket> buckets
            = new ConcurrentSkipListMap<>();

    private AtomicLong currentTime = new AtomicLong();

    private DateTimeProvider dateTimeProvider = DateTimeProviderImpl.INSTANCE;

    private DataChunkPool dataChunkPool;

    private Thread releaseThread;

    private AtomicBoolean stop = new AtomicBoolean(false);

    private AtomicInteger numSendErrors = new AtomicInteger(0);

    private AtomicLong numAdded = new AtomicLong();

    private AtomicLong numReleased = new AtomicLong();

    private AtomicLong previousNumAdded = new AtomicLong();

    private AtomicLong previousNumReleased = new AtomicLong();

    @Monitor(type = COUNTER)
    private AtomicInteger numDiscarded = new AtomicInteger();

    private boolean processUndelayed = true;

    protected boolean ignoreMarketTime = false;

    private final Timer statsTimer = new Timer("Delayer-stats", true);

    private boolean lengthIsInt = true;

    public void setLengthIsInt(boolean lengthIsInt) {
        this.lengthIsInt = lengthIsInt;
    }

    public void setDelayProvider(DelayProvider delayProvider) {
        this.delayProvider = delayProvider;
    }

    public void setIgnoreMarketTime(boolean ignoreMarketTime) {
        this.ignoreMarketTime = ignoreMarketTime;
    }

    public void setHandler(BufferWriter handler) {
        this.handler = handler;
    }

    public void setProcessUndelayed(boolean processUndelayed) {
        this.processUndelayed = processUndelayed;
        this.logger.info("<setProcessUndelayed> = " + this.processUndelayed);
    }

    public void setDayAndTimeProvider(DateTimeProvider dateTimeProvider) {
        this.dateTimeProvider = dateTimeProvider;
    }

    public void setDumpFile(File dumpFile) {
        this.dumpFile = dumpFile;
    }

    public void setPoolCapacity(String poolCapacity) {
        this.poolCapacity = NumberUtil.parseLong(poolCapacity);
    }

    public void setPoolChunkSize(int poolChunkSize) {
        this.poolChunkSize = poolChunkSize;
    }

    public DataChunkPool getDataChunkPool() {
        return dataChunkPool;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        this.dataChunkPool = new DataChunkPool(this.poolCapacity, this.poolChunkSize, this.byteOrder, true);
        readBuckets();
        updateCurrentTime();
    }

    @Override
    public void destroy() throws Exception {
        writeBuckets();
    }

    private void readBuckets() throws IOException {
        if (this.dumpFile == null) {
            this.logger.warn("<readBuckets> dumpFile not set");
            return;
        }
        if (!this.dumpFile.canRead()) {
            this.logger.warn("<readBuckets> cannot read " + this.dumpFile.getAbsolutePath());
            return;
        }

        this.logger.info("<readBuckets> from " + dumpFile.getAbsolutePath() + "...");

        final ByteBuffer bb = ByteBuffer.allocateDirect(8 * this.dataChunkPool.getChunkSize());

        TimeTaker tt = new TimeTaker();

        try (DataFile df = new DataFile(this.dumpFile, true)) {
            this.numAdded.addAndGet(df.readInt());
            int numBuckets = df.readInt();
            for (int i = 0; i < numBuckets; i++) {
                final DelayBucket bucket = getBucketForKey(df.readLong());
                int numChunks = df.readInt();
                for (int j = 0; j < numChunks; j++) {
                    int chunkSize = df.readInt();
                    bb.clear().limit(chunkSize);
                    df.read(bb);
                    bb.flip();
                    doAppend(bucket, bb);
                }
            }
        }
        this.logger.info("<readBuckets> took " + tt);
    }

    private void writeBuckets() throws IOException {
        if (this.dumpFile == null) {
            this.logger.warn("<writeBuckets> no dumpFile set, pending updates will be lost");
            return;
        }
        if (this.dumpFile.canRead() && !dumpFile.delete()) {
            this.logger.error("<writeBuckets> failed to delete " + dumpFile.getAbsolutePath());
            return;
        }

        this.logger.info("<writeBuckets> to " + this.dumpFile.getAbsolutePath());

        final ByteBuffer bb = ByteBuffer.allocateDirect(
                Math.max(1 << 20, 8 * this.dataChunkPool.getChunkSize()));

        TimeTaker tt = new TimeTaker();

        bb.putInt(getNumDelayed());
        int numBuckets = this.buckets.size();
        bb.putInt(numBuckets);
        int numChunks = 0;
        try (RandomAccessFile raf = new RandomAccessFile(this.dumpFile, "rw");
             FileChannel fc = raf.getChannel()) {
            for (Map.Entry<Long, DelayBucket> e : buckets.entrySet()) {
                flipWriteClear(fc, bb);
                bb.putLong(e.getKey());
                DelayBucket bucket = e.getValue();
                bb.putInt(bucket.numChunks());
                DataChunk chunk = bucket.getFirstChunk();
                while (chunk != null) {
                    ByteBuffer dcbb = chunk.getData();
                    if (bb.remaining() < 4 + dcbb.remaining()) {
                        flipWriteClear(fc, bb);
                    }
                    bb.putInt(dcbb.remaining());
                    bb.put(dcbb);
                    numChunks++;
                    chunk = chunk.getNext();
                }
            }
            if (bb.hasRemaining()) {
                flipWriteClear(fc, bb);
            }
        }
        this.logger.info("<writeBuckets> #buckets=" + numBuckets + ", #chunks=" + numChunks
                + ", took " + tt);
    }

    private void flipWriteClear(FileChannel fc, ByteBuffer bb) throws IOException {
        bb.flip();
        fc.write(bb);
        bb.clear();
    }

    @Override
    public boolean isRunning() {
        return this.releaseThread != null && this.releaseThread.isAlive();
    }

    public void start() {
        this.releaseThread = new Thread(this, "Delayer");
        this.releaseThread.start();

        this.statsTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                logStats();
            }
        }, 10000, 10000);
    }

    public void stop() {
        this.statsTimer.cancel();
        if (this.stop.compareAndSet(false, true)) {
            try {
                this.releaseThread.join();
            } catch (InterruptedException e) {
                this.logger.error("<stop> interrupted?!");
                Thread.currentThread().interrupt();
            }
        }
    }

    private void updateCurrentTime() {
        final long millis = this.dateTimeProvider.current().dateTime.getMillis();
        final long mod = millis % 1000;
        this.currentTime.set(millis - mod);
    }

    public void run() {
        while (!this.stop.get()) {
            release();
        }
        this.logger.info("<run> finished, stopping");
    }

    private void release() {
        try {
            updateCurrentTime();
            while (isReleasable()) {
                // after appending each record, numAdded will be incremented; by reading
                // the current value, a "happens-before" relation is ensured, so the release
                // thread is guaranteed to see all effects of the write thread w/o further
                // synchronization
                this.numAdded.get();

                releaseCurrentBucket();
                updateCurrentTime();
            }
            Thread.sleep(100);
        } catch (Throwable t) {
            this.logger.error("<release> failed", t);
        }
    }

    private int diffToPrevious(AtomicLong current, AtomicLong previous) {
        final long c = current.get();
        return (int) (c - previous.getAndSet(c));
    }

    private void logStats() {
        if (this.logger.isDebugEnabled()) {
            final long added = diffToPrevious(this.numAdded, this.previousNumAdded);
            final long released = diffToPrevious(this.numReleased, this.previousNumReleased);
            final String delayed = getNumDelayed() + "/+" + added + "/-" + released;
            this.logger.debug("stats - pool usage: "
                    + this.dataChunkPool.getUsedChunksPct() + "%, #delayed: " + delayed
                    + ", #buckets: " + this.buckets.size() + formatMinMaxDelayTime());
        }
    }

    private String formatMinMaxDelayTime() {
        try {
            final DateTime from = new DateTime(this.buckets.firstKey());
            final DateTime to = new DateTime(this.buckets.lastKey());
            return ", " + TIME.print(from) + " - " + TIME.print(to);
        } catch (NoSuchElementException e) {
            return "";
        }
    }

    private boolean isReleasable() {
        return !this.buckets.isEmpty()
                && this.buckets.firstKey().compareTo(this.currentTime.get()) < 0;
    }

    private void releaseCurrentBucket() {
        final DelayBucket toSend = this.buckets.remove(this.buckets.firstKey());
        final int num = send(toSend);

        this.dataChunkPool.returnChunks(toSend);
        this.numReleased.addAndGet(num);
    }

    private int send(DelayBucket toSend) {
        int result = 0;

        DataChunk c = toSend.getFirstChunk();
        while (c != null) {
            result += send(c);
            c = c.getNext();
        }
        return result;
    }

    private int send(DataChunk chunk) {
        final ByteBuffer bb = chunk.getData();
        final int total = bb.remaining();
        int result = 0;
        while (bb.hasRemaining()) {
            limitBufferToNextRecord(bb);
            int next = bb.limit();
            if (bb.position() == next) {
                this.logger.warn("<send> invalid data " + bb + ", total=" + total);
                return result;
            }
            sendToHandler(bb);
            bb.clear().limit(total).position(next);
            result++;
        }
        return result;
    }

    protected void limitBufferToNextRecord(ByteBuffer bb) {
        final int length = this.lengthIsInt
                ? bb.getInt(bb.position())
                : bb.getShort(bb.position()) & 0xFFFF;
        bb.limit(bb.position() + length);
    }

    @ManagedAttribute
    @Monitor(name = "numDelayed", type = GAUGE)
    public int getNumDelayed() {
        return (int) (this.numAdded.get() - this.numReleased.get());
    }

    @ManagedAttribute
    public int getNumDiscarded() {
        return this.numDiscarded.get();
    }

    @ManagedOperation
    public void resetStatistics() {
        this.numDiscarded.set(0);
        this.numSendErrors.set(0);
    }

    /**
     * Delays the data in buffer as determined by the vendorkey and the marketTime
     * The buffer is expected to contain its own length at position 0.
     */
    public void process(ParsedRecord pr, FeedData data, ByteBuffer buffer) {
        final int delayInSeconds = getDelayInSeconds(pr, data);
        if (delayInSeconds < 0) {
            return;
        }
        if (delayInSeconds == 0) {
            if (this.processUndelayed) {
                sendToHandler(buffer);
            }
            return;
        }
        append(delayInSeconds, buffer);
    }

    protected void append(int delayInSeconds, ByteBuffer buffer) {
        synchronized (this.writeMutex) {
            final DelayBucket bucket = getBucketForDelay(delayInSeconds);
            append(bucket, buffer);
        }
    }

    private DelayBucket getBucketForDelay(int delayInSeconds) {
        final long bucketKey = getBucketKey(delayInSeconds);
        return getBucketForKey(bucketKey);
    }

    private int getDelayInSeconds(ParsedRecord pr, FeedData data) {
        final int result = this.delayProvider.getDelayInSeconds(data);
        if (this.ignoreMarketTime || result <= 0) {
            return result;
        }
        return adjustDelayForMarketTime(result, pr.getMarketTime());
    }

    protected int adjustDelayForMarketTime(int nominalDelay, int marketTime) {
        final int secondOfDay = this.dateTimeProvider.secondOfDay();
        final int result = marketTime + nominalDelay - secondOfDay;

        if (secondOfDay < DateTimeConstants.SECONDS_PER_HOUR
                && marketTime > (23 * DateTimeConstants.SECONDS_PER_HOUR)) {
            // probably creationTime is yesterday's time, currentTime is today
            // so delay is one day off
            return result - DateTimeConstants.SECONDS_PER_DAY;
        }
        return result;
    }

    DelayBucket getBucketForKey(long bucketKey) {
        final DelayBucket existing = this.buckets.get(bucketKey);
        if (existing != null) {
            return existing;
        }
        final DataChunk dataChunk = this.dataChunkPool.getChunk();
        if (dataChunk == null) {
            return null;
        }
        final DelayBucket result = new DelayBucket(dataChunk);
        this.buckets.put(bucketKey, result);
        return result;
    }

    /**
     * append contents of buffer to bucket with index bucketIndex
     * @param bucket target
     * @param buffer source
     */
    void append(DelayBucket bucket, ByteBuffer buffer) {
        if (bucket != null && doAppend(bucket, buffer)) {
            this.numAdded.incrementAndGet();
        }
        else {
            this.numDiscarded.incrementAndGet();
        }
    }

    private boolean doAppend(DelayBucket bucket, ByteBuffer buffer) {
        if (!bucket.canAppend(buffer)) {
            final DataChunk nextChunk = this.dataChunkPool.getChunk();
            if (nextChunk == null) { // no more chunks available
                return false;
            }
            bucket.append(nextChunk);
        }
        return bucket.append(buffer);
    }

    /**
     * This method may be called by two different threads, the release thread and the one
     * that calls this component to submit updates, so we have to ensure thread safety.
     * @param buffer with data to be sent to handler
     */
    protected void sendToHandler(ByteBuffer buffer) {
        try {
            this.handler.write(buffer);
            final int numFailed = this.numSendErrors.getAndSet(0);
            if (numFailed > 0) {
                this.logger.warn("<sendToHandler> failed for " + numFailed + " consecutive sends");
            }
        } catch (IOException e) {
            if (this.numSendErrors.getAndIncrement() == 0) {
                this.logger.error("<sendToHandler> failed", e);
            }
        }
    }

    private long getBucketKey(int delay) {
        final int k = getEffectiveDelay(delay);
        return this.currentTime.get() + (k * DateTimeConstants.MILLIS_PER_SECOND);
    }

    private int getEffectiveDelay(int delay) {
        if (delay < 10) {
            // delay for at least 10s to avoid interference with release thread
            return 10;
        }
        if (delay > 3600) {
            // for delays of more than 1h, round delay to the next second
            // that can be divided by 16 to avoid using too many buckets.
            return (delay + 0xF) & 0x7FFFFFF0;
        }
        return delay;
    }
}
