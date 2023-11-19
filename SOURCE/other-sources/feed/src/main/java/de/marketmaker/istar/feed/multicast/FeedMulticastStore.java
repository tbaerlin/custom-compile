/*
 * MulticastStore.java
 *
 * Created on 26.08.14 11:47
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.multicast;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Pipe;
import java.nio.file.Files;
import java.util.Collections;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.ExceptionHandler;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.TimeoutException;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import net.jcip.annotations.GuardedBy;
import net.jcip.annotations.ThreadSafe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.Lifecycle;

import de.marketmaker.istar.common.util.HexDump;
import de.marketmaker.istar.common.util.Mementos;
import de.marketmaker.istar.feed.connect.BufferWriter;

import static java.nio.ByteOrder.LITTLE_ENDIAN;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.StandardOpenOption.*;

/**
 * Collects data to be sent in multicast packets. Each packet starts with an ever increasing
 * <code>long</code> sequence number. Whenever such a packet is full, it will be
 * forwarded to a delegate {@link FeedMulticastSender}.
 * This component stores the n last packets (see {@link #setNumBuffered(int)}) in a memory buffer.
 * A {@link FeedMulticastReceiver} uses the sequence number in the
 * received packets to detect misses. Whenever a miss is detected, the receiver tries to establish
 * a tcp connection to the host from which it received the latest multicast packet on port
 * <code>multicastPort + 1</code>. That connection is used to request/receive the missing packets,
 * which the {@link FeedTcpResender} gets from this object.
 * <p>
 * The thread that adds data by calling {@link #write(java.nio.ByteBuffer)} and the one that processes
 * the complete packets are decoupled using a {@link com.lmax.disruptor.RingBuffer}, which means that
 * this component act as a buffer, especially if the size of the internal buffer is large
 * (e.g., 1GB).
 * </p>
 * @author oflege
 */
@ThreadSafe
public class FeedMulticastStore implements InitializingBean, Lifecycle, BeanNameAware,
        EventFactory<ByteBuffer>, EventHandler<ByteBuffer>, BufferWriter, DisposableBean {

    protected Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * Size of a packet that can be sent without fragmentation
     */
    private final int multicastPacketSize;

    // 2 + multicastPacketSize
    private int bufferedPacketSize;

    private int maxRecordSize;

    private final Object mutex = new Object();

    /**
     * Regular feed updates are submitted by the same thread, but other threads may occasionally
     * submit updates too (e.g., tick corrections, snap edits), so we need to synchronize write
     * access using this mutex object
     */
    private final Object writeMutex = new Object();

    /**
     * optional file in which the latest used sequence number is stored on shutdown and read again
     * on restart. Useful as soon as multicast packets are also stored in files for re-request and
     * an sequence numbers should be unique even over many restarts
     */
    private File sequenceFile;

    /**
     * base packet sequence number, will be read from {@link #sequenceFile} if possible.
     */
    private long base = 1;

    /**
     * the buffer for storing packets; this object is never used directly for writing/reading,
     * all such ops are invoked on duplicates of this object - and since the buffer's position/limit/etc.
     * is thus never modified, it can be duplicated w/o synchronization.<p>
     * <code>buffer</code> contains {@link #numBuffered} fixed sized chunks which are large enough
     * to store a maximum size multicast packet plus a 2 byte <code>short</code> at the beginning
     * that contains the actual length of the packet. This wastes a few bytes at the end of each
     * record, but it simplifies finding the packet with sequence number x in the buffer, as no
     * additional data structure to store offsets is needed (see {@link #asPosition(long)}).
     */
    private ByteBuffer buffer;

    // used for writing into buffer
    private ByteBuffer wb;

    // used for reading from buffer
    private ByteBuffer rb;

    /**
     * a {@link FeedTcpResender} which uses this object as the
     * backing store for requested packets essentially runs in a
     * {@link de.marketmaker.istar.common.nioframework.SelectorThread}. To tell the <code>TcpResender</code>
     * that new data is available and can be sent to connected clients,
     * we use a {@link java.nio.channels.SelectableChannel} that can be registered with the
     * <code>SelectorThread</code>, so that it will be notified every time data can be read
     * from the other end of this sink.
     */
    private Pipe.SinkChannel sink;

    /**
     * optimization: we only write to <code>sink</code> when the client on the other side of the pipe
     * actually needs the data. It is the client's responsibility to set this flag accordingly
     */
    private volatile boolean doNotifySink = false;

    /** used for writing packet numbers into sink */
    private ByteBuffer seqBuffer = ByteBuffer.allocateDirect(8).order(LITTLE_ENDIAN);

    /**
     * Number of packets buffered in memory, has to be a multiple of 2.
     */
    private int numBuffered = 1 << 16;

    // numBuffered - 1
    private long bufferIndexMask;

    private Disruptor<ByteBuffer> disruptor;

    private RingBuffer<ByteBuffer> ringBuffer;

    private FeedMulticastSender sender;

    /**
     * last sequence number obtained from ringBuffer, <em>not</em> the same as the packet's sequence number
     */
    private long seq = -1;

    /**
     * smallest available packet sequence number, i.e. packets with a smaller number cannot be re-requested
     */
    @GuardedBy("this.mutex")
    private long min;

    /**
     * largest available packet sequence number in the buffer
     */
    @GuardedBy("this.mutex")
    private long max;

    private String name = getClass().getSimpleName();

    private final ExecutorService executor = Executors.newSingleThreadExecutor(this::createThread);

    /**
     * flush is needed if this.seq does not change between to runs of {@link #flush()}. We can assume
     * that some feed records appear every few seconds (CLOCK.VWD or /EEOD.VWD heartbeat), so we
     * just set the {@link #flush} flag, which is evaluated for each incoming buffer.
     */
    private final Mementos.Long seqMemento = new Mementos.Long(0L);

    private boolean flush = false;

    private int flushIntervalMs = 250;

    public FeedMulticastStore() {
        this(FeedMulticastSender.MULTICAST_PACKET_SIZE);
    }

    // access only for package local testing or from public constructor
    FeedMulticastStore(int multicastPacketSize) {
        this.multicastPacketSize = multicastPacketSize;
        this.maxRecordSize = this.multicastPacketSize - 8;
    }

    public void setFlushIntervalMs(int flushIntervalMs) {
        this.flushIntervalMs = flushIntervalMs;
    }

    @Override
    public void setBeanName(String s) {
        this.name = s;
    }

    private Thread createThread(Runnable r) {
        return new Thread(r, this.name + "-executor");
    }

    public void setNumBuffered(int numBuffered) {
        if (Integer.bitCount(numBuffered) != 1) {
            throw new IllegalArgumentException("not a multiple of 2: " + numBuffered);
        }
        this.numBuffered = numBuffered;
    }

    public void setSender(FeedMulticastSender sender) {
        this.sender = sender;
    }

    public void setSequenceFile(File sequenceFile) {
        this.sequenceFile = sequenceFile;
    }

    void setDoNotifySink(boolean doNotifySink) {
        this.doNotifySink = doNotifySink && (this.sink != null);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        this.bufferedPacketSize = 2 + this.multicastPacketSize;

        if (this.sequenceFile != null && this.sequenceFile.canRead()) {
            this.base = Long.parseLong(Files.readAllLines(this.sequenceFile.toPath(), UTF_8).get(0));
            if (this.base < 1) {
                throw new IllegalArgumentException("read invalid base " + this.base
                        + " from " + this.sequenceFile.getAbsolutePath());
            }
            this.logger.info("<afterPropertiesSet> base = " + this.base);
        }
        synchronized (this.mutex) {
            this.min = base;
            this.max = base - 1;
        }

        this.bufferIndexMask = numBuffered - 1;

        this.buffer = ByteBuffer.allocateDirect(this.numBuffered * bufferedPacketSize);
        this.rb = duplicateBuffer();
        this.wb = duplicateBuffer();
        this.wb.flip();

        // ringBufferSize MUST NEVER exceed numBuffered.
        // BlockingWaitStrategy is ok, Parser needs time to fill next packet anyway, spinning
        // some cycles to wait for it is probably not worth it (we expect about 2k events/s)
        this.disruptor =
            new Disruptor<>(
                this,
                Math.min(1 << 14, this.numBuffered),
                executor,
                ProducerType.SINGLE,
                new BlockingWaitStrategy());

        this.disruptor.setDefaultExceptionHandler(new ExceptionHandler<ByteBuffer>() {
            @Override
            public void handleEventException(Throwable ex, long sequence, ByteBuffer event) {
                logger.error("<handleEventException> " + seq + " " + event, ex);
            }

            @Override
            public void handleOnStartException(Throwable ex) {
                throw new RuntimeException(ex);
            }

            @Override
            public void handleOnShutdownException(Throwable ex) {
                throw new RuntimeException(ex);
            }
        });
        //noinspection unchecked
        this.disruptor.handleEventsWith(this);
    }

    private ByteBuffer duplicateBuffer() {
        return this.buffer.duplicate().order(LITTLE_ENDIAN);
    }


    @Override
    public void destroy() throws Exception {
        if (this.sequenceFile != null) {
            storeSequenceFile();
        }
    }

    private void storeSequenceFile() throws IOException {
        final String s = Long.toString(getPacketId() + 1);
        Files.write(this.sequenceFile.toPath(), Collections.singleton(s), UTF_8,
                CREATE, WRITE, TRUNCATE_EXISTING);
    }

    /**
     * Used to obtain a source from which available packet sequence numbers (i.e., <code>long</code>s)
     * can be read. Must not be invoked more than once. <p>
     * <b>Important</b>: The caller has to make sure to actually read data from the returned source
     * as the sink part of the pipe will be used in blocking mode, i.e., once the pipe's internal
     * buffer is full, the multicasting thread would block forever.
     * @return source for packet sequence numbers.
     * @throws IOException
     */
    Pipe.SourceChannel getSource() throws IOException {
        if (this.sink != null) {
            throw new IllegalStateException();
        }
        Pipe p = Pipe.open();
        this.sink = p.sink();
        this.sink.configureBlocking(true);
        return p.source();
    }

    long getMin() {
        synchronized (this.mutex) {
            return min;
        }
    }

    long getMax() {
        synchronized (this.mutex) {
            return max;
        }
    }

    /**
     * @param id packet seq number
     * @return position in <code>buffer</code> at which the packet is stored
     */
    private int asPosition(long id) {
        return (int) (id & this.bufferIndexMask) * this.bufferedPacketSize;
    }

    /**
     * @return this value is never actually used, to return the same object for all invocations.
     * The important aspect is that we limit the area in <code>rb</code> according to the
     * sequence number in {@link #onEvent(java.nio.ByteBuffer, long, boolean)}.
     */
    @Override
    public ByteBuffer newInstance() {
        return this.rb;
    }


    @Override
    public void onEvent(ByteBuffer event, long sequence, boolean endOfBatch) throws Exception {
        assert event == rb;

        final long packetId = getPacketId(sequence);
        notifySink(packetId);
        send(packetId);
    }

    private void send(long packetId) {
        final int p = asPosition(packetId);
        this.rb.clear().position(p).limit(p + (this.rb.getShort() & 0xFFFF));
        try {
            this.sender.send(this.rb);
        } catch (IOException e) {
            this.logger.error("<send> failed", e);
        }
    }

    private void notifySink(long id) {
        if (!this.doNotifySink) {
            return;
        }
        this.seqBuffer.putLong(0, id).clear();
        try {
            while (this.seqBuffer.hasRemaining()) {
                this.sink.write(this.seqBuffer);
            }
        } catch (IOException e) {
            this.logger.error("<notifySink> failed", e);
            this.sink = null;
        }
    }

    long getPacketId() {
        return getPacketId(this.seq);
    }

    long getPacketId(long id) {
        return this.base + id;
    }

    @Override
    public boolean isRunning() {
        return this.ringBuffer != null;
    }

    @Override
    public void start() {
        this.ringBuffer = this.disruptor.start();

        final Timer flushTimer = new Timer(this.name + "-flush", true);
        flushTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                flush();
            }
        }, this.flushIntervalMs, this.flushIntervalMs);
    }

    private void flush() {
        synchronized (this.writeMutex) {
            if (this.seq > -1L) {
                if (this.seqMemento.diffAndSet(this.seq) == 0L) { // seq did not change
                    this.flush = true;
                }
            }
        }
    }

    @Override
    public void stop() {
        // when we get here, we assume that the component that calls our write(ByteBuffer bb) method
        // has already been stopped. According to the memory-model's thread stop rule, we see
        // all the changes made by that thread and don't have to use any extra synchronization

        // if this.seq >= 0, we have obtained a sequence id from the ringBuffer but not yet
        // published it; w/o publishing, disruptor.shutdown() would hang permanently while
        // waiting for the last sequence to be published
        synchronized (this.writeMutex) {
            publish();
        }

        shutdownDisruptor();
        shutdownExecutor();
    }

    private void shutdownExecutor() {
        this.executor.shutdown();
        try {
            if (!this.executor.awaitTermination(10, TimeUnit.SECONDS)) {
                this.logger.error("<shutdownExecutor> timeout");
            }
        } catch (InterruptedException e) {
            this.logger.error("<shutdownExecutor> interrupted?!");
            Thread.currentThread().interrupt();
        }
    }

    private void shutdownDisruptor() {
        try {
            this.disruptor.shutdown(10, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            this.logger.error("<shutdownDisruptor> timeout");
        }
    }

    /**
     * Copies packets starting with sequence number <code>fromId</code>
     * into <code>dst</code> and flips it so that data can be read subsequently.
     * The number of packets is limited by the remaining capacity in <code>dst</code>
     * and the maximum available packet id in the store.
     * @param fromId requested start seq no.
     * @param toId requested end seq no, 0 for undefined
     * @param dst copy destination
     * @return <dl>
     *     <dt>if <code>dst.hasRemaining()</code> is <code>true</code> after the call</dt>
     *     <dd>highest id stored in dst + 1, which can be used when this method is called again later</dd>
     *     <dt>otherwise</dt>
     *     <dd><code>fromId</code> if data for <code>fromId</code> is not yet available
     *     or the minimum available sequence id if data for <code>fromId</code> is no more available</dd>
     * </dl>
     */
    long copyFrom(long fromId, long toId, ByteBuffer dst) {
        synchronized (this.mutex) {
            if (fromId > this.max) {
                dst.clear().flip();
                return fromId;
            }
            if (fromId < this.min) {
                dst.clear().flip();
                return this.min;
            }
            final long myMax = (toId == 0) ? this.max : Math.min(toId, this.max);
            // DO NOT try to move the following code out of the synchronization (e.g., by using
            // a temp variable for max): synchronization ensures that the contents of the buffers
            // we copy will not be written concurrently.
            final ByteBuffer tmp = duplicateBuffer();
            for (long id = fromId; id <= myMax; id++) {
                wrapPacketWithLength(tmp, id);
                if (dst.remaining() < tmp.remaining()) {
                    dst.flip();
                    return id;
                }
                dst.put(tmp);
            }
            dst.flip();
            return myMax + 1;
        }
    }

    private void wrapPacketWithLength(ByteBuffer tmp, long i) {
        final int p = asPosition(i);
        tmp.clear().position(p).limit(p + (tmp.getShort(tmp.position()) & 0xFFFF));
    }

    @Override
    public void write(ByteBuffer bb) throws IOException {
        synchronized (this.writeMutex) {
            doWrite(bb);
        }
    }

    private void doWrite(ByteBuffer bb) {
        if (bb.remaining() > this.wb.remaining() || this.flush) {
            if (bb.remaining() > this.maxRecordSize) {
                byte[] head = new byte[64];
                bb.get(head, 0, head.length);
                this.logger.warn("<write> max record size exceeded: " + bb
                    + ": " + HexDump.toHex(head));
                bb.position(bb.limit());
                return;
            }
            prepareWriteBuffer();
        }
        this.wb.put(bb);
    }

    private void prepareWriteBuffer() {
        publish();

        this.seq = this.ringBuffer.next();
        long id = getPacketId();

        synchronized (this.mutex) {
            this.min = Math.max(this.min, id - this.numBuffered + 1);
        }

        int p = asPosition(id);
        this.wb.clear().position(p + 2).limit(p + bufferedPacketSize);
        this.wb.putLong(id);
    }

    private void publish() {
        if (this.seq >= 0) {
            doPublish();
            this.flush = false;
        }
    }

    private void doPublish() {
        putCurrentPacketLength();

        synchronized (this.mutex) {
            this.max++;
        }
        this.ringBuffer.publish(this.seq);
    }

    private void putCurrentPacketLength() {
        long id = getPacketId();
        this.wb.limit(this.wb.position()).position(asPosition(id));
        this.wb.putShort(this.wb.position(), (short) this.wb.remaining());
    }
}
