/*
 * MuxOut.java
 *
 * Created on 08.10.12 13:43
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.mux;

import static de.marketmaker.istar.common.util.NumberUtil.humanReadableByteCount;

import de.marketmaker.istar.common.nioframework.AbstractReadWriteSelectorHandler;
import de.marketmaker.istar.common.nioframework.Acceptor;
import de.marketmaker.istar.common.nioframework.AcceptorListener;
import de.marketmaker.istar.common.nioframework.ClientConnectionInfo;
import de.marketmaker.istar.common.util.Mementos;
import de.marketmaker.istar.common.util.NumberUtil;
import de.marketmaker.istar.feed.connect.FeedStats;
import dev.infrontfinance.dm.common.io.RingBuffer;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * Output side of a mux. Implements {@link AcceptorListener}, so it can be registered as a listener
 * for one or multiple {@link Acceptor}s. As soon as a client connects to an Acceptor's port, it
 * will be added to the list of currently connected clients and receives data provided by the
 * {@link MuxIn} that uses this object as its delegate.
 * <p>
 * All actions run in a single {@link de.marketmaker.istar.common.nioframework.SelectorThread},
 * which has to be the same that is used by the connected <code>MuxIn</code>.  Using
 * the <code>SelectorThread</code> also ensures that all I/O-opertations are non-blocking.
 * </p>
 * <p>
 * Messages are passed to this object by calling {@link #append(java.nio.ByteBuffer)} and it is
 * required that that buffer only contains <em>complete</em> messages (i.e., must not end with
 * an incomplete fragment). Incoming messages are copied into a ring buffer from which all
 * connected clients read when they write to the output socket. This is in contrast to what
 * happens in a {@link de.marketmaker.istar.common.nioframework.ByteArrayServer}, where each
 * client keeps a copy of the data it has to send.
 * </p>
 *
 * @author oflege
 * @author ytas
 * @author zzhao
 */
public class MuxOut implements AcceptorListener, InitializingBean, DisposableBean,
        MuxOutput, FeedStats.ByteSource {

    private static final long ONE_G = 1L << 30;

    /**
     * Constants for the metric names
     */
    private static final String PACKET_SIZE = "mux_out_packet_size";

    private static final String GAUGE = "mux_out_gauge";

    private static final String COUNTER = "mux_out_counter";

    protected class Client extends AbstractReadWriteSelectorHandler
        implements ClientConnectionInfo {
        private final DateTime connectedSince = new DateTime();

        /**
         * Stores a message fragment from the current readPosition to a know message boundary
         * whenever the client is detected to be too slow and data at the current readPosition
         * would be overridden by the next data write
         */
        private final ByteBuffer fragment;

        private final AtomicLong fragmentCount;

        private final AtomicLong fragmentBytes;

        private volatile long fragmentMax;

        private final ByteBuffer[] segments;

        private final ByteBuffer[] segmentDups;

        private final ByteBuffer[] sources;

        /**
         * current read position, will always be less than or equal to {@link MuxOut#writePos}.
         */
        private volatile long readPos;

        private final AtomicLong numBytesWritten = new AtomicLong();

        private volatile long lastWritePos;

        private volatile long lastLossAt;

        private final AtomicLong numBytesLost = new AtomicLong();

        private volatile long maxGapAt;

        private final AtomicLong maxGap = new AtomicLong();

        private final Mementos.Long lastNumBytesLost = new Mementos.Long();

        private final Mementos.Long lastNumBytesWritten = new Mementos.Long();

        private final List<Meter> meters = new ArrayList<>();

        protected Client(Acceptor acceptor, SocketChannel sc, ByteBuffer fragmentBuf, int id)
            throws IOException {
            super(acceptor, sc);
            this.readPos = MuxOut.this.writePos;
            this.lastWritePos = MuxOut.this.writePos;
            this.fragment = (ByteBuffer) fragmentBuf.clear().flip();
            this.fragmentCount = new AtomicLong(0);
            this.fragmentBytes = new AtomicLong(0);

            this.segments = MuxOut.this.ring.getSegments(); // new array and read only buffer
            this.segmentDups = MuxOut.this.ring.getSegments(); // new array and read only buffer
            // sources are used to write to socket.
            // the first place is for missing fragments buffer if it has remaining,
            // otherwise just one of the underlying buffers.
            // plus all or other underlying read only buffers if data is wrapped.
            // plus one additional place for the buffer-duplicate, in which data is wrapped.
            // for single underlying buffer, this buffer-duplicate can only be from that buffer.
            // for multi-underlying buffer, this buffer-duplicate can be any buffer.
            // sources must be set properly before every socket write and reset after.
            // min one buffer, max segment length plus 2 (missing fragments, segments, the dup)
            // we limit sources to length 3, since we limit max. data to write in one go
            // to 1G + fragment
            this.sources = new ByteBuffer[3];

            registerChannel(false, false);

            // register client metrics
            if (MuxOut.this.meterRegistry != null) {
                final Tags tags = Tags.of("r", "C" + id);
                // micrometer maintains a strong reference to gauged object created this way
                // we track all meters created this way and remove them upon client close
                // to release strong reference to client
                this.meters.add(Gauge.builder(GAUGE, () -> this.readPos)
                    .tags(tags.and("t", "c_read_pos")).register(MuxOut.this.meterRegistry));
                this.meters.add(Gauge.builder(GAUGE, () -> this.lastWritePos)
                    .tags(tags.and("t", "c_write_pos")).register(MuxOut.this.meterRegistry));
                this.meters.add(Gauge.builder(GAUGE, this.maxGap::get)
                    .tags(tags.and("t", "c_max_gap")).register(MuxOut.this.meterRegistry));
                this.meters.add(Gauge.builder(GAUGE, this.numBytesWritten::get)
                    .tags(tags.and("t", "c_bytes_sent")).register(MuxOut.this.meterRegistry));
                this.meters.add(Gauge.builder(GAUGE, () -> this.fragmentMax)
                    .tags(tags.and("t", "c_fragment_max")).register(MuxOut.this.meterRegistry));

                this.meters.add(MuxOut.this.meterRegistry.more()
                    .counter(COUNTER, tags.and("t", "c_bytes_lost"), this.numBytesLost));
                this.meters.add(MuxOut.this.meterRegistry.more()
                    .counter(COUNTER, tags.and("t", "c_fragment_count"), this.fragmentCount));
                this.meters.add(MuxOut.this.meterRegistry.more()
                    .counter(COUNTER, tags.and("t", "c_fragment_bytes"), this.fragmentBytes));
            }
        }

        @Override
        public String toString() {
            return "Client[" + getRemoteAddress()
                    + ", local=" + getLocalAddress()
                    + ", since=" + this.connectedSince
                    + ", #b/out=" + this.numBytesWritten.get()
                    + ", #b/lost=" + this.numBytesLost.get()
                    + "]";
        }

        @Override
        protected boolean doRead(SocketChannel sc) {
            throw new UnsupportedOperationException();
        }

        public void reset() {
            this.readPos = 0L;
        }

        @Override
        protected void close() {
            super.close();
            this.meters.forEach(m -> MuxOut.this.meterRegistry.remove(m));
            removeClient(this);
        }

        private void ackAppend() {
            // if readPos < writePos, write interest is already enabled.
            if (this.readPos == MuxOut.this.writePos) {
                enableWriting();
            }
        }

        @Override
        protected boolean doWrite(SocketChannel sc) throws IOException {
            if (this.readPos == MuxOut.this.writePos && !this.fragment.hasRemaining()) {
                // nothing to write
                return false;
            }

            // at ring buffer size 2G and gap 1.9G, from and to in segment 0
            // where to is less than from IllegalArgumentException when called using 4 buffers
            // (missing fragment, segment 0, segment 1, segment 0 dup)
            // sun.nio.ch.NativeDispatcher.writev, line 148
            // sun.nio.ch.FileDispatcherImpl.writev0, line 140, native method
            // reducing the number of buffers so that no more than 2G data remaining works.
            // this is fine, since it's hardly possible to write out 2G in one call anyway
            // this seems to be a JVM JNI code limitation (read C code)
            // just try to write out 1G + fragment
            final long writeTo = Math.min(this.readPos + ONE_G, MuxOut.this.writePos);
            final long from = MuxOut.this.ring.asPosition(this.readPos);
            final long to = MuxOut.this.ring.asPosition(writeTo);

            final int segFrom = MuxOut.this.ring.findSegment(from);
            final int segTo = MuxOut.this.ring.findSegment(to);
            final int posInSegFrom = MuxOut.this.ring.findPosInSegment(from);
            final int posInSegTo = MuxOut.this.ring.findPosInSegment(to);

            try {
                int offset = 0;
                final int fragmentLen = this.fragment.remaining();
                if (fragmentLen > 0) {
                    this.sources[offset++] = this.fragment;
                }

                if (segFrom < segTo) {
                    for (int i = segFrom; i <= segTo && offset < this.sources.length; i++) {
                        final ByteBuffer segment = this.segments[i];
                        segment.clear();
                        if (i == segFrom) {
                            segment.position(posInSegFrom);
                        } else if (i == segTo) {
                            segment.limit(posInSegTo);
                        }
                        this.sources[offset++] = segment;
                    }
                } else if (segFrom == segTo) {
                    if (posInSegFrom == posInSegTo) {
                        // nothing to write
                    } else if (posInSegFrom < posInSegTo) {
                        this.sources[offset++] = this.segments[segFrom];
                        this.segments[segFrom].clear().position(posInSegFrom).limit(posInSegTo);
                    } else {
                        // data wrap
                        offset = wrapSources(offset, segFrom, segTo, posInSegFrom, posInSegTo);
                    }
                } else {
                    // data wrap
                    offset = wrapSources(offset, segFrom, segTo, posInSegFrom, posInSegTo);
                }

                try {
                    final long numWritten = sc.write(this.sources, 0, offset);
                    if (numWritten > fragmentLen) {
                        this.readPos += (numWritten - fragmentLen);
                        this.lastWritePos = this.readPos;
                    }
                    ackWrite(numWritten);
                } catch (Throwable e) {
                    this.logger.warn("<doWrite> failed", e);
                    this.logger.warn("<doWrite> readPos {}, writeTo {}, diff {}, mo writePos {}",
                        this.readPos, writeTo, writeTo - this.readPos, MuxOut.this.writePos);
                    this.logger.warn("<doWrite> pos in ring buffer from {} to {}", from, to);
                    this.logger.warn("<doWrite> from seg {} pos {} to seg {} pos {}", segFrom,
                        posInSegFrom, segTo, posInSegTo);
                    final long totalBytes = getTotalBytes(this.sources, offset);
                    this.logger.warn("<doWrite> segments {}, offset {}, total bytes {}",
                        this.segments.length, offset, totalBytes);
                    Stream.of(this.sources)
                        .filter(Objects::nonNull)
                        .forEach(s -> this.logger.warn("<doWrite> {}", s));
                    throw e; // this will log error and force client reconnect
                }
            } finally {
                Arrays.fill(this.sources, null);
            }

            return this.readPos < MuxOut.this.writePos;
        }

        private long getTotalBytes(ByteBuffer[] bufs, int to) {
            long result = 0;
            for (int i = 0; i < to; i++) {
                result += bufs[i].remaining();
            }
            return result;
        }

        private int wrapSources(int offset, int segFrom, int segTo, int posInSegFrom,
            int posInSegTo) {
            for (int i = segFrom; i < this.segments.length && offset < this.sources.length; i++) {
                final ByteBuffer seg = this.segments[i];
                this.sources[offset++] = seg;
                seg.clear();
                if (i == segFrom) {
                    seg.position(posInSegFrom);
                }
            }
            for (int i = 0; i <= segTo && offset < this.sources.length; i++) {
                final ByteBuffer seg = this.segmentDups[i];
                this.sources[offset++] = seg;
                seg.clear();
                if (i == segTo) {
                    seg.limit(posInSegTo);
                }
            }
            return offset;
        }

        private void ackWrite(long numWritten) {
            this.numBytesWritten.addAndGet(numWritten);
            MuxOut.this.numBytesSent.addAndGet(numWritten);
        }

        private void updateMaxGap(long nextWritePos) {
            final long gap = nextWritePos - this.lastWritePos;
            if (gap > this.maxGap.get()) {
                this.maxGap.set(gap);
                this.maxGapAt = System.currentTimeMillis();
            }
        }

        /**
         * make sure that readPos is not smaller than minReadPos, as data up to minReadPos will
         * be erased by the upcoming write.
         * @param nextWritePos Position that will be overwritten in next write
         * @param minReadPos position of data than can still be read after next write happened
         */
        void beforeAppend(long nextWritePos, long minReadPos) {
            if (this.readPos < minReadPos) {
                if (!this.fragment.hasRemaining()) {
                    if (!copyRecordFragment()) {
                        close();
                        return;
                    }
                    if (this.readPos >= minReadPos) {
                        return;
                    }
                }
                skipTo(recordBoundaryAtOrAfter(minReadPos));
            }
            updateMaxGap(nextWritePos);
        }

        private void skipTo(long newReadPos) {
            final long delta = newReadPos - this.readPos;
            this.lastLossAt = System.currentTimeMillis();
            this.numBytesLost.addAndGet(delta);
            this.readPos = newReadPos;
        }

        private boolean copyRecordFragment() {
            if (this.fragment.hasRemaining() || this.readPos == MuxOut.this.writePos) {
                return true;
            }

            final long fragmentEnd = recordBoundaryAtOrAfter(this.readPos);
            final long fragmentBytes = fragmentEnd - this.readPos;

            if (fragmentBytes == 0) {
                // no fragment to read into fragment buffer, just let it skip to next boundary
                // rare but possible
                this.logger.info("<copyRecordFragment> read pos is the boundary, no fragment");
                return true;
            }

            this.fragment.clear();
            this.fragmentCount.incrementAndGet();
            this.fragmentBytes.addAndGet(fragmentBytes);
            if (fragmentBytes > this.fragmentMax) {
                this.fragmentMax = fragmentBytes;
            }

            long from = MuxOut.this.ring.asPosition(this.readPos);
            final long to = MuxOut.this.ring.asPosition(fragmentEnd);
            try {
                final RingBuffer src =
                    MuxOut.this.ring.duplicate().limit(MuxOut.this.ring.capacity());
                if (to < from) {
                    src.position(from);
                    if (this.fragment.remaining() < src.remaining()) {
                        logFailedCopy(fragmentEnd, src);
                        return false;
                    }
                    src.writeTo(this.fragment);
                    from = 0;
                }
                src.position(from).limit(to);
                if (this.fragment.remaining() < src.remaining()) {
                    logFailedCopy(fragmentEnd, src);
                    return false;
                }
                src.writeTo(this.fragment);
                this.fragment.flip();
                this.readPos = fragmentEnd;

                return true;
            } catch (Throwable t) {
                this.logger.error("<copyRecordFragment> fragment bytes: {}", fragmentBytes, t);
                logFailedCopy(fragmentEnd, MuxOut.this.ring);
                return false; // this will force client reconnect
            }
        }

        /**
         * fragment capacity must be at least twice the size of message buffer size in {@link MuxIn}
         */
        private void logFailedCopy(long fragmentEnd, RingBuffer src) {
            final StringBuilder sb = new StringBuilder().append("<logFailedCopy> ")
                .append("readPos=").append(this.readPos)
                .append(", fragmentEnd=").append(fragmentEnd)
                .append(", from=").append(MuxOut.this.ring.asPosition(this.readPos))
                .append(", to=").append(MuxOut.this.ring.asPosition(fragmentEnd))
                .append(", src=").append(src)
                .append("#").append(src.remaining())
                .append(", missingFragment=").append(this.fragment)
                .append("#").append(this.fragment.remaining());

            final int i = asRecordIndex(this.readPos);
            final int idx = recordBoundaryIndexAtOrAfter(this.readPos);
            for (int n = i; ; n = incRecordEndsIndex(n)) {
                sb.append(", ends[").append(n).append("]=").append(MuxOut.this.recordEnds[n]);
                if (n == idx) {
                    break;
                }
            }

            this.logger.error(sb.toString());
        }

        void resetStats() {
            this.maxGapAt = 0;
            this.maxGap.set(0);
            this.lastLossAt = 0;
            this.numBytesLost.set(0);
        }

        void logStatus() {
            final long currentGap = MuxOut.this.writePos - this.lastWritePos;
            final long lost = getNumBytesLostSinceLastCall();
            final long written = getNumBytesWrittenSinceLastCall();
            if (lost > 0) {
                this.logger.error("<status> {}: #w={}, gap={}, max={}, #lost={}",
                    getRemoteAddress(), humanReadableByteCount(written),
                    humanReadableByteCount(currentGap), humanReadableByteCount(this.maxGap.get()),
                    humanReadableByteCount(lost));
            } else {
                this.logger.info("<status> {}: #w={}, gap={}, max={}", getRemoteAddress(),
                    humanReadableByteCount(written), humanReadableByteCount(currentGap),
                    humanReadableByteCount(this.maxGap.get()));
            }
        }

        private synchronized long getNumBytesWrittenSinceLastCall() {
            return this.lastNumBytesWritten.diffAndSet(this.numBytesWritten.get());
        }

        private synchronized long getNumBytesLostSinceLastCall() {
            return this.lastNumBytesLost.diffAndSet(this.numBytesLost.get());
        }

        void appendStatusTo(PrintWriter pw) {
            final long bytesWritten = this.numBytesWritten.get();
            final long lost = this.numBytesLost.get();
            final long gap = this.maxGap.get();
            pw.printf("  Remote Address  %s%n", getRemoteAddress());
            pw.printf("  Local Address   %s%n", getLocalAddress());
            pw.printf("  Connected Since %s%n", this.connectedSince);
            pw.printf("  #bytes written  %d (%s)%n", bytesWritten, NumberUtil.prettyPrint(bytesWritten));
            pw.printf("  #bytes lost     %d (%s)%n", lost, NumberUtil.prettyPrint(lost));
            if (this.lastLossAt != 0) {
                pw.printf("  last lost at    %s%n", new DateTime(this.lastLossAt));
            }
            pw.printf("  max gap         %d (%s)%n", gap, NumberUtil.prettyPrint(gap));
            if (this.maxGapAt != 0) {
                pw.printf("  max gap at      %s%n", new DateTime(this.maxGapAt));
            }
        }

        @Override
        public DateTime getConnectedSince() {
            return this.connectedSince;
        }

        @Override
        public long getNumDiscarded() {
            return this.numBytesLost.get();
        }

        @Override
        public long getNumSent() {
            return this.numBytesWritten.get();
        }
    }

    private static final int MIN_BUF_SIZE = 1 << 20;

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * since we use direct buffers, we pool them here for reuse
     */
    private final Queue<WeakReference<ByteBuffer>> fragmentBufs = new LinkedList<>();

    /**
     * Stores incoming messages
     */
    private RingBuffer ring;

    /**
     * currently connected clients that will receive all data that arrives after they have connected
     */
    private final List<Client> clients = new CopyOnWriteArrayList<>();

    private long ringBufferSize = 1 << 20;

    /**
     * This must never be less than twice the buffer size used in {@link MuxIn}, never less than
     * 65535 (unsigned short max, the maximum single MDPS message length)
     */
    private int fragmentBufSize = 1 << 18;

    /**
     * This is used to divide the size of the ring buffer and the result is the size of the buckets or
     * record end index array.
     *
     * After each record is received, write (end) position ({@link #writePos}) is used to determine,
     * in which bucket that received record will land. The value of that bucket, namely the array
     * element value of {@link #recordEnds} at that position, is set to write position.
     *
     * This means the maximum difference between two adjacent array elements in {@link #recordEnds} is
     * twice the bucket size.
     *
     * During data drops, record fragment is copied into client's private fragment buffer to ensure
     * only complete records are sent downstream. The size of that private fragment buffer determines
     * how big such a bucket can be, i.e. half of the fragment buffer capacity at most.
     *
     * The maximum length of a MDPS record is 65535 (unsigned short, 2^16 - 1) and minimum length is 6
     * (just the header).
     */
    private int recordIdxShift;

    /**
     * Stores values of writePos at which messages ended. We do not need to store every message
     * end, just enough to make sure a client can copy data between its current readPos and the
     * next message boundary into its private missingFragment buffer.
     */
    private long[] recordEnds;

    /**
     * This is used to calculate the modulo faster by using bit-AND operation.
     * It is used to find the index in the record end array corresponding
     * to {@link MuxOut#writePos} or {@link Client#readPos}. Only useful when ring buffer size
     * is power of 2.
     */
    private int recordEndsIdxMask;

    /**
     * Ever-increasing position in the stream of data that has been written as long as data from
     * the same input source is processed, will be reset to 0 when the input source switches.
     */
    private volatile long writePos;

    private final AtomicLong numBytesSent = new AtomicLong();

    private MeterRegistry meterRegistry;

    /**
     * Message size distribution summary to use in MuxOut.append
     */
    private DistributionSummary packetSizeDist;

    public void setMeterRegistry(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    public void setFragmentBufSize(int fragmentBufSize) {
        this.fragmentBufSize = fragmentBufSize;
    }

    /**
     * @deprecated will be removed after all services using MuxOut is updated.
     */
    public void setMessageBufferSize(int messageBufferSize) {
        this.fragmentBufSize = messageBufferSize;
    }

    public void setRingBufferSize(long ringBufferSize) {
        if (ringBufferSize < MIN_BUF_SIZE) {
            throw new IllegalArgumentException(ringBufferSize + " < " + MIN_BUF_SIZE);
        }
        this.ringBufferSize = ringBufferSize;
    }

    public long getRingBufferSize() {
        return this.ringBufferSize;
    }

    @Override
    public void socketConnected(Acceptor acceptor, SocketChannel sc) throws IOException {
        final int clientId = this.clients.size();
        final Client c = new Client(acceptor, sc, createFragmentBuf(), clientId);
        this.clients.add(c);
        this.logger.info("<socketConnected> added Client[{}] {}", clientId, c.getRemoteAddress());
    }

    private ByteBuffer createFragmentBuf() {
        while (!this.fragmentBufs.isEmpty()) {
            final WeakReference<ByteBuffer> ref = this.fragmentBufs.remove();
            final ByteBuffer buffer = ref.get();
            if (buffer != null) {
                return buffer;
            }
        }
        return createBuffer(this.fragmentBufSize);
    }

    private void removeClient(final Client c) {
        this.clients.remove(c);
        this.fragmentBufs.add(new WeakReference<>(c.fragment));
    }

    @Override
    public long numBytesSent() {
        return this.numBytesSent.get();
    }

    @Override
    public void destroy() throws Exception {
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (Integer.bitCount(this.fragmentBufSize) != 1) {
            throw new IllegalArgumentException("only support power of 2 sized message buffer");
        }
        // !!ATTENTION!!
        // fragment buf size must be at least twice the message buffer size of configured for MuxIn
        if (this.fragmentBufSize < (1 << 18) || this.fragmentBufSize > (1 << 20)) {
            throw new IllegalArgumentException("message buffer size out of range: ("
                + (1 << 18) + ", " + (1 << 20) + "]");
        }
        this.recordIdxShift = Integer.bitCount((this.fragmentBufSize >> 1) - 1);
        this.ring = RingBuffer.create(this.ringBufferSize);
        final int recordEndsSize = (int) (this.ringBufferSize >> this.recordIdxShift);
        this.recordEnds = new long[recordEndsSize];
        Arrays.fill(this.recordEnds, 0L);
        // only useful if ring buffer size is power of 2
        this.recordEndsIdxMask = this.recordEnds.length - 1;
        // read position of client is adjusted during beforeAppend
        // and message boundaries are taken care of
        // however we cannot let that gap ever increase
        // the max allowed gap is used to disconnect client if it lags too much
        // equals 2 folds of ring size if ring size below MAX_ALLOWED_GAP_MARK
        // equals ring size + MAX_ALLOWED_GAP_MARK if otherwise
        //
        // removed maxAllowedGap, since the arbitrary value alone cannot justify mal-functioning
        // client, but a manual disconnect is provided
        if (this.meterRegistry != null) {
            this.packetSizeDist = DistributionSummary
                .builder(PACKET_SIZE)
                .publishPercentileHistogram()
                .minimumExpectedValue(16D)
                .maximumExpectedValue((double) (1 << 17)) // 128K
                .register(this.meterRegistry);

            final Tags tags = Tags.of("r", "MO");
            Gauge.builder(GAUGE, () -> this.writePos)
                .tags(tags.and("t", "mo_write_pos"))
                .register(this.meterRegistry);

            Gauge.builder(GAUGE, this.numBytesSent::get)
                .tags(tags.and("t", "mo_bytes_sent"))
                .register(this.meterRegistry);
        }
        this.logger.info("<afterPropertiesSet> ringBufferSize={}, #recordEnds={}",
            this.ringBufferSize, this.recordEnds.length);
    }

    private ByteBuffer createBuffer(final int capacity) {
        this.logger.info("<createBuffer> direct buffer, cap={}", capacity);
        // the buffer has to be direct. A non-direct buffer would be copied into a new direct one when
        // SocketChannel#write is invoked and since that would happen per client, this could
        // result in an OutOfMemoryError (especially if a large ring is used and clients have a
        // tendency to lag behind).
        return ByteBuffer.allocateDirect(capacity);
    }

    List<Client> getClients() {
        return this.clients;
    }

    public List<? extends ClientConnectionInfo> getClientInfo() {
        return this.clients;
    }

    void resetClientStats() {
        this.clients.forEach(Client::resetStats);
    }

    public void logClientStatus() {
        this.clients.forEach(Client::logStatus);
    }

    @Override
    public void onInClosed() {
        this.clients.stream()
            .filter(client -> !client.copyRecordFragment())
            .forEach(Client::close);
    }

    @Override
    public void reset() {
        this.writePos = 0;
        this.ring.clear();
        this.clients.forEach(Client::reset);
        Arrays.fill(this.recordEnds, 0L);
    }

    void disconnectClient(int which) {
        if (this.clients.size() > which) {
            this.clients.get(which).close();
        } else {
            this.logger.warn("no client at index {}", which);
        }
    }

    @Override
    public boolean isAppendOnlyCompleteRecords() {
        return true;
    }

    /**
     * Receive messages to be forwarded to clients
     * @param in contains complete messages
     */
    @Override
    public void append(ByteBuffer in) {
        final long nextWritePos = doAppend(in);

        this.clients.forEach(Client::ackAppend);

        this.writePos = nextWritePos;
        this.recordEnds[asRecordIndex(this.writePos)] = this.writePos;
    }

    private long doAppend(ByteBuffer in) {
        final int remaining = in.remaining();
        if (this.packetSizeDist != null) {
            this.packetSizeDist.record(remaining);
        }
        final long nextWritePos = this.writePos + remaining;
        final long minReadPos = Math.max(0L, nextWritePos - this.ringBufferSize);

        for (Client client : this.clients) {
            client.beforeAppend(nextWritePos, minReadPos);
        }

        this.ring.put(in);
        return nextWritePos;
    }

    long recordBoundaryAtOrAfter(long readPos) {
        return this.recordEnds[recordBoundaryIndexAtOrAfter(readPos)];
    }

    private int recordBoundaryIndexAtOrAfter(long readPos) {
        assert readPos <= this.writePos;
        final int recordIdxForReadPos = asRecordIndex(readPos);
        final int recordIdxForWritePos = asRecordIndex(this.writePos);
        int recordIdx = recordIdxForReadPos;
        if (recordIdx == recordIdxForWritePos) {
            // writePos is almost a full buffer length ahead of readPos and has already stored
            // the latest record boundary at i, so we have to skip that
            recordIdx = incRecordEndsIndex(recordIdxForReadPos);
        }
        // this loop will terminate as the assertion ensures that the message boundary at
        // writePos will be present in recordEnds and it will be >= readPos
        // theoretically it can loop back
        for (int j = recordIdx; j < this.recordEnds.length; j++) {
            final long end = this.recordEnds[j];
            if (end != 0L && end >= readPos) {
                return j;
            }
        }
        for (int j = 0; j < recordIdx; j++) {
            final long end = this.recordEnds[j];
            if (end != 0L && end >= readPos) {
                return j;
            }
        }

        // as this method is only called determining record boundary in client, an exception
        // will force client reconnect
        this.logger.error("<recordBoundaryIndexAtOrAfter> readPos: {}, writePos{}", readPos,
            this.writePos);
        this.logger.error("<recordBoundaryIndexAtOrAfter> record idx 4ReadPos: {}, 4WritePos: {}",
            recordIdxForReadPos, recordIdxForWritePos);
        this.logger.error("<recordBoundaryIndexAtOrAfter> record idx shift: {}, "
            + "record ends length {}", this.recordIdxShift, this.recordEnds.length);
        throw new IllegalStateException("cannot determine record boundary");
    }

    private int incRecordEndsIndex(int i) {
        return this.ring.isSizePowerOf2()
            ? (i + 1) & this.recordEndsIdxMask
            : (i + 1) % this.recordEnds.length;
    }

    private int asRecordIndex(long pos) {
        return (int) (this.ring.asPosition(pos) >> this.recordIdxShift);
    }

    void appendStatus(PrintWriter pw) {
        pw.println("--OUT-------");
        pw.println(" Clients:");
        int i = 0;
        for (Client client : this.clients) {
            pw.printf("%2d%n", ++i);
            client.appendStatusTo(pw);
        }
        if (i == 0) {
            pw.println("NO CLIENTS CONNECTED");
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{"
            + "ringBufferSize=" + humanReadableByteCount(this.ringBufferSize)
            + ", fragmentBufSize=" + humanReadableByteCount(this.fragmentBufSize)
            + ", numbBytesSent=" + this.numBytesSent.get()
            + ", writePos=" + this.writePos
            + "}";
    }
}
