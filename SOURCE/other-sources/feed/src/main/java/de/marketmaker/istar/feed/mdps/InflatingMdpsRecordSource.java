/*
 * SimpleMdpsRecordSource.java
 *
 * Created on 25.08.2006 10:57:37
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.mdps;

import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.TimeoutException;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import com.netflix.servo.annotations.Monitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.SmartLifecycle;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;

import de.marketmaker.istar.feed.FeedRecord;
import de.marketmaker.istar.feed.Parser;
import de.marketmaker.istar.feed.VolatileFeedDataRegistry;
import de.marketmaker.istar.feed.connect.BufferWriter;
import de.marketmaker.istar.feed.connect.FeedInputSource;
import de.marketmaker.istar.feed.mux.MuxOutput;
import de.marketmaker.istar.feed.ordered.OrderedFeedWriter;

import static com.netflix.servo.annotations.DataSourceType.COUNTER;
import static de.marketmaker.istar.feed.mdps.MdpsFeedUtils.getUnsignedShort;
import static de.marketmaker.istar.feed.mdps.MdpsMessageConstants.HEADER_BODY_LENGTH_OFFSET;
import static de.marketmaker.istar.feed.mdps.MdpsMessageConstants.HEADER_LENGTH;

/**
 * Inflates a deflated mdps feed and forwards {@link FeedRecord}s to a {@link Parser}.
 * Two threads are involved:<ul>
 * <li>A writer calls {@link #write(java.nio.ByteBuffer)} with bytes that conform to the format
 * of the deflated mdps feed. Those bytes will be inflated and put into a
 * {@link RingBuffer}.
 * </li>
 * <li>A reader thread keeps track of whatever data is stored in the <tt>RingBuffer</tt> and
 * calls {@link #onEvent(InflatingMdpsRecordSource.Element, long, boolean)};
 * that method extracts {@link FeedRecord}s from the data and forwards them to an {@link MdpsFeedParser}.
 * </li>
 * </ul>
 * <b>Important</b> This class does not implement {@link de.marketmaker.istar.feed.RecordSource},
 * as it cannot be used as a delegate for a Parser. Instead, an instance of this class uses a
 * thread of its own to drive a Parser.
 * <p>
 * Using a {@link RingBuffer} as opposed to an {@link java.util.concurrent.Exchanger} increases
 * the throughput by 20% as threads do not have to wait for each other and more work can be done
 * in parallel.
 * </p>
 * @author Oliver Flege
 * @author Thomas Kiesgen
 * @see <a href="http://code.google.com/p/disruptor/">Disruptor</a>
 */
@ManagedResource
public class InflatingMdpsRecordSource implements
        BufferWriter, MuxOutput, InitializingBean, SmartLifecycle, BeanNameAware,
        EventFactory<InflatingMdpsRecordSource.Element>,
        EventHandler<InflatingMdpsRecordSource.Element> {

    static class Element {

        private ByteBuffer bb;

        private Element(final ByteBuffer buffer) {
            this.bb = buffer;
        }

        private ByteBuffer grow() {
            final int newCapacity = bb.capacity() + BUFFER_SIZE_INCREMENT;
            if (newCapacity > MAX_BUFFER_SIZE) {
                throw new IllegalStateException("Cannot grow buffer beyond " + MAX_BUFFER_SIZE);
            }
            final ByteBuffer tmp = ByteBuffer.allocate(newCapacity).order(bb.order());
            System.arraycopy(bb.array(), 0, tmp.array(), 0, bb.capacity());
            this.bb = tmp;
            return this.bb;
        }

    }

    private static final int BUFFER_SIZE_INCREMENT = 32768;

    private static final int DEFAULT_BUFFER_SIZE = BUFFER_SIZE_INCREMENT * 3;

    private static final int MAX_BUFFER_SIZE = DEFAULT_BUFFER_SIZE * 16;

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private final Inflater inf = new Inflater();

    private int bufferSize = DEFAULT_BUFFER_SIZE;

    private FeedRecord feedRecord;

    private int protocolVersion = 1;

    private ByteOrder byteOrder;

    private final AtomicBoolean sendSyncRecord = new AtomicBoolean(false);

    @Monitor(type = COUNTER)
    private final AtomicLong numBytesInflated = new AtomicLong();

    private RingBuffer<Element> ringBuffer;

    private Disruptor<Element> disruptor;

    private String name = InflatingMdpsRecordSource.class.getSimpleName();

    private final ExecutorService executor = Executors.newCachedThreadPool(new ThreadFactory() {
        private final AtomicInteger no = new AtomicInteger();

        public Thread newThread(Runnable r) {
            return new Thread(r, name + "-" + no.incrementAndGet());
        }
    });

    private Consumer<FeedRecord> parser;

    private int ringBufferSize = 16;

    @Override
    public Element newInstance() {
        return new Element(ByteBuffer.allocate(this.bufferSize).order(this.byteOrder));
    }

    public void setBeanName(String name) {
        this.name = name;
    }

    public void setParser(Consumer<FeedRecord> parser) {
        this.parser = parser;
    }

    public void setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
    }

    public void setRingBufferSize(int ringBufferSize) {
        this.ringBufferSize = ringBufferSize;
    }

    public void setProtocolVersion(int protocolVersion) {
        if (protocolVersion != 1 && protocolVersion != 3) {
            throw new IllegalArgumentException("unsupported " + protocolVersion);
        }
        this.protocolVersion = protocolVersion;
        this.logger.info("<setProtocolVersion> = " + this.protocolVersion);
    }

    @Override
    public void afterPropertiesSet() {
        this.byteOrder = MdpsFeedUtils.getByteOrder(this.protocolVersion);

        this.disruptor = new Disruptor<>(
            this,
            this.ringBufferSize,
            this.executor,
            ProducerType.SINGLE,
            new BlockingWaitStrategy());
        this.disruptor.handleEventsWith(this);

        this.logger.info("<afterPropertiesSet> using buffer size " + this.bufferSize);

        this.feedRecord = new FeedRecord().withOrder(this.byteOrder);
    }

    @ManagedAttribute
    public long getNumBytesInflated() {
        return this.numBytesInflated.get();
    }

    @Override
    public boolean isRunning() {
        return this.ringBuffer != null;
    }

    public void start() {
        this.ringBuffer = this.disruptor.start();
    }

    @Override
    public boolean isAutoStartup() {
        return true;
    }

    @Override
    public void stop(Runnable callback) {
        stop();
        callback.run();
    }

    @Override
    public int getPhase() {
        // make sure this object's start() method will be invoked before any others so that the
        // ringBuffer will be initialized once we receive the first data
        return Integer.MIN_VALUE;
    }

    public void stop() {
        this.logger.info("<stop> disruptor shutdown...");
        try {
            this.disruptor.shutdown(10, TimeUnit.SECONDS);
            this.logger.info("<stop> done");
        } catch (TimeoutException e) {
            this.logger.warn("<stop> timeout");
            this.disruptor.halt();
        }
        this.logger.info("<stop> executor shutdown...");
        this.executor.shutdown();
        try {
            if (this.executor.awaitTermination(10, TimeUnit.SECONDS)) {
                this.logger.info("<stop> done");
            }
            else {
                this.logger.warn("<stop> timeout");
            }
        } catch (InterruptedException e) {
            this.logger.error("<stop> interrupted!?");
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void onEvent(Element event, long sequence, boolean endOfBatch) {
        onEvent(event.bb);
        if (endOfBatch && this.sendSyncRecord.compareAndSet(true, false)) {
            this.parser.accept(FeedRecord.SYNC);
        }
    }

    void onEvent(ByteBuffer bb) {
        try {
            while (bb.hasRemaining()) {
                final int length = getLength(bb);
                parse(bb, length);
                bb.position(bb.position() + length);
            }
        } catch (Exception e) {
            this.logger.error("<onEvent> parse failed", e);
        }
    }

    private int getLength(ByteBuffer bb) {
        if (this.protocolVersion == 1) {
            return HEADER_LENGTH + getUnsignedShort(bb, bb.position() + HEADER_BODY_LENGTH_OFFSET);
        }
        return getUnsignedShort(bb, bb.position());
    }

    private void parse(ByteBuffer bb, int length) {
        // create FeedRecord so that its getAsByteBuffer method returns a Buffer
        // that wraps around |header|body|
        this.feedRecord.reset(bb.array(), bb.position(), length);
        this.parser.accept(this.feedRecord);
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
        try {
            doWrite(toWrite);
        } catch (Exception e) {
            if (e instanceof DataFormatException) {
                throw new IllegalStateException("inflate failed", e);
            }
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            if (e instanceof IOException) {
                throw (IOException) e;
            }
            throw new IOException(e);
        }
    }

    private void doWrite(ByteBuffer toWrite) throws Exception {
        while (toWrite.remaining() > 4) {
            toWrite.mark();
            final int length = toWrite.getInt();
            if (length <= 4 || length > toWrite.capacity()) {
                throw new IllegalStateException("Illegal length: " + length);
            }
            if (toWrite.remaining() < length - 4) {
                toWrite.reset();
                return;
            }
            final int chunkEnd = toWrite.position() + length - 4;
            if (this.protocolVersion == 1) {
                inflateChunk(toWrite.array(), toWrite.position() + 1, length - 5);
            }
            else {
                inflateChunk(toWrite.array(), toWrite.position() + 2, length - 6);
            }
            toWrite.position(chunkEnd);
        }
    }

    private void inflateChunk(byte[] bytes, int offset, int length) throws Exception {
        final long sequence = this.ringBuffer.next(1);
        final Element element = this.ringBuffer.get(sequence);
        try {
            ByteBuffer bb = element.bb;
            bb.clear().flip();
            this.inf.reset();
            this.inf.setInput(bytes, offset, length);
            int numInflated = inf.inflate(bb.array());
            while (numInflated == bb.capacity() && !this.inf.finished()) {
                bb = element.grow();
                this.logger.info("<writeChunk> adapted buffer size to " + bb.capacity());
                numInflated += this.inf.inflate(bb.array(), numInflated, bb.capacity() - numInflated);
            }
            this.numBytesInflated.addAndGet(numInflated);
            bb.limit(numInflated);
        } finally {
            // we must publish even if the buffer is empty, otherwise the client would wait forever
            this.ringBuffer.publish(sequence);
        }
    }

    @ManagedOperation
    public void sendSyncRecord() {
        this.sendSyncRecord.compareAndSet(false, true);
    }

    public static void main(String[] args) throws Exception {
        MdpsFeedParser p = new MdpsFeedParser();
        p.setAddToaAndDoa(false);
        p.setKeyConverter(new MdpsKeyConverter(false));
        p.setRepository(new VolatileFeedDataRegistry());
        p.afterPropertiesSet();

        OrderedFeedWriter ofw = new OrderedFeedWriter();
        ofw.setWriter(Buffer::flip);

        p.setFeedBuilders(ofw);

        InflatingMdpsRecordSource rs = new InflatingMdpsRecordSource();
        rs.setProtocolVersion(3);
        rs.setParser(p);
        rs.setBeanName("foo");

        MdpsFeedConnector fc = new MdpsFeedConnector();
        fc.setBufferWriter(rs);
        fc.setBeanName("fc");
        fc.setProtocolVersion(3);
        fc.setReceiveBufferSize(8388608);
        fc.setReconnectIntervalSeconds(3);
        fc.setPrimaryInputSource(new FeedInputSource(args[0], 1, args[0], Integer.parseInt(args[1])));
        fc.afterPropertiesSet();

        rs.afterPropertiesSet();
        rs.start();
        fc.start();

        try {
            Thread.sleep(10000);
        } finally {
            fc.stop();
            rs.stop();
        }
        System.out.println(p.numRecordsParsed());
    }
}
