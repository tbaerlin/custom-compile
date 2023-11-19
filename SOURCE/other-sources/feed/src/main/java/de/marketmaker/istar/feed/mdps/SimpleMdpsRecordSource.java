/*
 * SimpleMdpsRecordSource.java
 *
 * Created on 25.08.2006 10:57:37
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.mdps;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.joda.time.DateTimeConstants;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;

import de.marketmaker.istar.common.util.ByteBufferUtils;
import de.marketmaker.istar.feed.FeedRecord;
import de.marketmaker.istar.feed.RecordSource;
import de.marketmaker.istar.feed.connect.BufferWriter;
import de.marketmaker.istar.feed.connect.FeedStats;
import de.marketmaker.istar.feed.mux.MuxOutput;

import static de.marketmaker.istar.feed.mdps.MdpsMessageConstants.HEADER_BODY_LENGTH_OFFSET;
import static de.marketmaker.istar.feed.mdps.MdpsMessageConstants.HEADER_LENGTH;
import static org.joda.time.DateTimeConstants.MILLIS_PER_SECOND;

/**
 * A RecordSource that uses an simple wait/notify scheme to coordinate the threads that
 * provide the feed data and the thread that requests the feed data. The provider will always
 * wait for the receiver to request data. Thus, we do not create any objects that will be
 * queued and may cause problems with GC.
 * However, parallelization suffers and the tcp buffers might overflow unnoticed if the
 * data requestor is too slow.
 * <p>
 * Expected to be used by 2 threads: A consumer thread calls {@link #getFeedRecord()} repeatedly
 * to obtain successive FeedRecords. A producer thread calls {@link #write(java.nio.ByteBuffer)}
 * to provide more data. Both threads may be suspended to wait for the other to produce/consume data.
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
@ManagedResource
public class SimpleMdpsRecordSource implements BufferWriter, MuxOutput, RecordSource, InitializingBean,
        FeedStats.MessageSource {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private static final int DEFAULT_BUFFERSIZE = 96 * 1024;

    private int preRecordOffset = 0;

    private int postRecordOffset = 0;

    private boolean needMoreData = false;

    private final ByteBuffer bb;

    /**
     * optional secondary output (can be used to dump the feed etc.)
     */
    private BufferWriter teeWriter;

    private long lastTeeWriteFailedAt;

    private FeedRecord feedRecord;

    private int protocolVersion = 1;

    private AtomicBoolean sendSyncRecord = new AtomicBoolean(false);

    private AtomicLong numMessagesSent = new AtomicLong();

    private boolean forceNewFeedRecords = false;

    private static final String GAUGE = "simple_mdps_record_source_gauge";
    private MeterRegistry meterRegistry;

    public void setMeterRegistry(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    public void setForceNewFeedRecords(boolean flag) {
        this.forceNewFeedRecords = flag;
    }

    public SimpleMdpsRecordSource() {
        this.bb = ByteBuffer.allocate(DEFAULT_BUFFERSIZE);
        this.bb.flip(); // make sure buffer has no remaining bytes to read
    }

    public SimpleMdpsRecordSource(ByteBuffer bb) {
        this.bb = bb;
        this.feedRecord = new FeedRecord(this.bb.array(), 0, 0).withOrder(this.bb.order());
        this.protocolVersion = (this.bb.order() == MdpsFeedUtils.getByteOrder(1)) ? 1 : 3;
    }

    public void setTeeWriter(BufferWriter teeWriter) {
        this.teeWriter = teeWriter;
    }

    public void setProtocolVersion(int protocolVersion) {
        if (protocolVersion != 1 && protocolVersion != 3) {
            throw new IllegalArgumentException("unsupported " + protocolVersion);
        }
        this.protocolVersion = protocolVersion;
        this.logger.info("<setProtocolVersion> = " + this.protocolVersion);
    }

    public void setPreRecordOffset(int preRecordOffset) {
        this.preRecordOffset = preRecordOffset;
        this.logger.info("<setPreRecordOffset> = " + this.preRecordOffset);
    }

    public void setPostRecordOffset(int postRecordOffset) {
        this.postRecordOffset = postRecordOffset;
        this.logger.info("<setPostRecordOffset> = " + this.postRecordOffset);
    }

    public void afterPropertiesSet() throws Exception {
        this.bb.order(MdpsFeedUtils.getByteOrder(this.protocolVersion));
        if (this.forceNewFeedRecords) {
            this.logger.info("<afterPropertiesSet> Not using fixed FeedRecord");
        } else {
          if (this.preRecordOffset == 0 && this.postRecordOffset == 0) {
            this.feedRecord = new FeedRecord(this.bb.array(), 0, 0).withOrder(this.bb.order());
            this.logger.info("<afterPropertiesSet> using fixed FeedRecord");
          }
        }

        if (this.meterRegistry != null) {
            Gauge.builder(GAUGE, () -> this.numMessagesSent)
                .tags(Tags.of("t", "num_messages_sent")).register(this.meterRegistry);
        }
    }

    @Override
    public long numMessagesSent() {
        return this.numMessagesSent.get();
    }

    public boolean hasRemaining() {
        return bb.hasRemaining();
    }

    public FeedRecord getFeedRecord() throws InterruptedException {
        if (!this.bb.hasRemaining()) {
            if (this.sendSyncRecord.compareAndSet(true, false)) {
                return FeedRecord.SYNC;
            }
            getMoreData();
        }

        final int p = this.bb.position();
        final int msgLen = MdpsFeedUtils.getUnsignedShort(this.bb, p);
        this.numMessagesSent.incrementAndGet();
        return getNextFeedRecord(p, msgLen);
    }

    private FeedRecord getNextFeedRecord(int p, int msgLen) {
        final int length = (this.protocolVersion == 1) ? getMessageLengthV1(p) : msgLen;

        // create FeedRecord so that its getAsByteBuffer method returns a Buffer
        // that wraps around |header|body|
        if (this.feedRecord != null) {
            this.feedRecord.reset(this.feedRecord.getData(), p, length);
            this.bb.position(p + length);
            return this.feedRecord;
        }

        return createNewFeedRecord(msgLen, length);
    }

    private FeedRecord createNewFeedRecord(int msgLen, int length) {
        final byte[] data = copyData(msgLen);
        return new FeedRecord(data, this.preRecordOffset, length).withOrder(this.bb.order());
    }

    private byte[] copyData(int msgLen) {
        final byte[] result = new byte[this.preRecordOffset + msgLen + this.postRecordOffset];
        this.bb.get(result, this.preRecordOffset, msgLen);
        return result;
    }

    private int getMessageLengthV1(int p) {
        return HEADER_LENGTH + MdpsFeedUtils.getUnsignedShort(this.bb, p + HEADER_BODY_LENGTH_OFFSET);
    }

    private synchronized void getMoreData() throws InterruptedException {
        this.needMoreData = true;
        notify();
        while (this.needMoreData) {
            wait();
        }
    }

    @Override
    public boolean isAppendOnlyCompleteRecords() {
        return false;
    }

    @Override
    public void append(ByteBuffer in) throws IOException {
        write(in);
    }

    public void write(ByteBuffer toWrite) throws IOException {
        final int oldLimit = toWrite.limit();
        toWrite.limit(findLimit(toWrite));
        if (!toWrite.hasRemaining()) {
            toWrite.limit(oldLimit);
            return;
        }

        try {
            if (this.teeWriter != null) {
                teeWrite(toWrite);
            }
            doWrite(toWrite);
        } finally {
            toWrite.limit(oldLimit);
        }
    }

    private void teeWrite(ByteBuffer toWrite) {
        try {
            this.teeWriter.write(ByteBufferUtils.duplicate(toWrite));
            this.lastTeeWriteFailedAt = 0;
        } catch (Exception e) {
            long now = System.currentTimeMillis();
            if (now - this.lastTeeWriteFailedAt > MILLIS_PER_SECOND) {
                this.logger.error("<teeWrite> failed", e);
                this.lastTeeWriteFailedAt = now;
            }
        }
    }

    private synchronized void doWrite(ByteBuffer toWrite) throws IOException {
        int n = 0;
        while (!this.needMoreData && ++n < DateTimeConstants.SECONDS_PER_MINUTE) {
            try {
                wait(1000L);
            } catch (InterruptedException e) {
                this.logger.info("<write> interrupted, returning");
                Thread.currentThread().interrupt();
                toWrite.clear();
                return;
            }
        }
        if (!this.needMoreData) {
            throw new IOException("wait time exceeeded");
        }

        this.bb.clear();
        this.bb.put(toWrite);
        this.bb.flip();
        this.needMoreData = false;
        notify();
    }

    /**
     * Find the position p in toWrite such that all data between toWrite's current position and
     * p fits into this.bb, and that data contains only complete mdps records.
     * @param toWrite incoming data
     * @return limit
     */
    private int findLimit(ByteBuffer toWrite) throws IOException {
        int available = Math.min(this.bb.capacity(), toWrite.remaining());
        int pos = toWrite.position();
        while (pos < toWrite.limit() - 2) {
            final int msgLen = MdpsFeedUtils.getUnsignedShort(toWrite, pos);
            if (msgLen <= 0) {
                throw new IOException("invalid record length: " + msgLen);
            }
            if (available < msgLen) {
                break;
            }
            available -= msgLen;
            pos += msgLen;
        }
        return pos;
    }

    @ManagedOperation
    public void sendSyncRecord() {
        this.sendSyncRecord.compareAndSet(false, true);
    }
}
