/*
 * MulticastFeedReceiver.java
 *
 * Created on 29.08.12 15:54
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.connect;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import com.netflix.servo.annotations.Monitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.Lifecycle;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;

import de.marketmaker.istar.common.mcast.MulticastReceiver;
import de.marketmaker.istar.common.mcast.MulticastSender;

import static com.netflix.servo.annotations.DataSourceType.COUNTER;

/**
 * A multicast receiver with a ring buffer, intended to replace {@link OrderedMulticastReceiver}
 * and {@link SimpleMulticastRecordSource}. Clients that want to receive feed data have to
 * implement {@link EventHandler}<tt>&lt;MulticastFeedReceiver.Item&gt;</tt> and to be
 * registered using {@link #setHandlers(com.lmax.disruptor.EventHandler[])}.
 * @author oflege
 */
@ManagedResource
public class MulticastFeedReceiver implements InitializingBean, Lifecycle,
        EventFactory<MulticastFeedReceiver.Item>, EventHandler<MulticastFeedReceiver.Item> {

    static class Item {

        final ByteBuffer buffer = ByteBuffer.allocate(MulticastSender.MULTICAST_PACKET_SIZE);

        final DatagramPacket packet = new DatagramPacket(buffer.array(), 0, buffer.array().length);

        private Item(ByteOrder order) {
            buffer.order(order);
        }

        private void onReceive() {
            this.buffer.position(0);
            this.buffer.limit(this.packet.getLength());
        }

        private void put(byte[] data) {
            this.buffer.clear();
            this.buffer.put(data).flip().position(8);
        }
    }

    private final Runnable workTask = MulticastFeedReceiver.this::work;


    private int ringBufferSize = 128;

    private Future<?> workFuture;

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private final ExecutorService executor = Executors.newCachedThreadPool(new ThreadFactory() {
        private final AtomicInteger no = new AtomicInteger();

        public Thread newThread(Runnable r) {
            return new Thread(r, MulticastFeedReceiver.this.getClass().getSimpleName()
                    + "-" + no.incrementAndGet());
        }
    });

    private Disruptor<Item> disruptor;

    private RingBuffer<Item> ringBuffer;

    private MulticastReceiver receiver;

    private ByteOrder byteOrder = ByteOrder.LITTLE_ENDIAN;

    private final NavigableMap<Long, byte[]> outOfOrderPackets = new TreeMap<>();

    private InetAddress sourceAddress = null;

    private long expectedPacketId = 0;

    @Monitor(type = COUNTER)
    private final AtomicInteger numPacketsMissed = new AtomicInteger();

    private EventHandler<ByteBuffer> handler = null;

    private volatile boolean stop;

    public void setLittleEndian(boolean littleEndian) {
        this.byteOrder = littleEndian ? ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN;
    }

    public void setRingBufferSize(int ringBufferSize) {
        this.ringBufferSize = ringBufferSize;
    }

    public void setReceiver(MulticastReceiver receiver) {
        this.receiver = receiver;
    }

    public void setHandlers(EventHandler<ByteBuffer>... handlers) {
        if (handlers.length != 1) {
            throw new IllegalArgumentException();
        }
        setHandler(handlers[0]);
    }

    public void setHandler(EventHandler<ByteBuffer> handler) {
        this.handler = handler;
    }

    @ManagedAttribute
    public long getNumPacketsMissed() {
        return this.numPacketsMissed.get();
    }

    @ManagedAttribute
    public void resetNumPacketsMissed() {
        this.numPacketsMissed.set(0);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (this.handler == null) {
            throw new IllegalStateException("no handler set");
        }
        this.disruptor = new Disruptor<>(this, this.ringBufferSize,
                this.executor, ProducerType.SINGLE, new BlockingWaitStrategy());
        //noinspection unchecked
        this.disruptor.handleEventsWith(this);
    }

    @Override
    public void onEvent(Item event, long sequence, boolean endOfBatch) throws Exception {
        this.handler.onEvent(event.buffer, sequence, endOfBatch);
    }

    @Override
    public boolean isRunning() {
        return this.ringBuffer != null;
    }

    @Override
    public void start() {
        this.ringBuffer = this.disruptor.start();
        this.workFuture = this.executor.submit(workTask);
        this.logger.info("<start> done");
    }

    @Override
    public void stop() {
        requestStop();
        try {
            this.workFuture.get();

            this.disruptor.shutdown();
            this.executor.shutdown();
            if (!this.executor.awaitTermination(10, TimeUnit.SECONDS)) {
                this.logger.warn("<stop> executor did not stop within 10s, returning");
                return;
            }
        } catch (InterruptedException e) {
            this.logger.error("<stop> interrupted!?");
            Thread.currentThread().interrupt();
        } catch (ExecutionException e) {
            this.logger.error("<stop> workFuture failed", e);
        }
        this.logger.info("<stop> done");
    }

    void requestStop() {
        this.stop = true;
    }

    @Override
    public Item newInstance() {
        return new Item(this.byteOrder);
    }

    private void work() {
        while (!this.stop) {
            try {
                doWork();
            } catch (Exception e) {
                this.logger.error("<work> failed", e);
            }
        }
    }

    private void doWork() {
        long seq = -1L;
        Item mb = null;

        while (!this.stop) {
            if (mb == null) {
                seq = this.ringBuffer.next();
                mb = this.ringBuffer.get(seq);
            }

            if (!this.outOfOrderPackets.isEmpty()
                    && this.outOfOrderPackets.containsKey(this.expectedPacketId)) {
                mb.put(this.outOfOrderPackets.remove(this.expectedPacketId));
                publish(seq);
                mb = null;
                continue;
            }

            if (!receive(mb)) {
                continue;
            }

            final long id = mb.buffer.getLong();

            if (id == this.expectedPacketId || handleUnexpectedId(mb, id)) {
                publish(seq);
                mb = null;
            }
        }
        this.logger.info("<doWork> finished");
    }

    private void publish(long seq) {
        this.ringBuffer.publish(seq);
        this.expectedPacketId++;
    }

    private boolean handleUnexpectedId(Item mb, long id) {
        if (this.sourceAddress == null) {
            this.expectedPacketId = id;
            this.sourceAddress = mb.packet.getAddress();
            this.logger.info("<handle> picking up from " + this.sourceAddress + " at " + id);
            return true;
        }

        if (!this.sourceAddress.equals(mb.packet.getAddress())) {
            this.sourceAddress = mb.packet.getAddress();
            this.expectedPacketId = id;
            this.outOfOrderPackets.clear();
            this.logger.info("<handle> address changed, now receiving from " + sourceAddress
                    + " at " + id);
            return true;
        }

        if (id < this.expectedPacketId) {
            this.logger.info("<handle> address probably started over, continue from " + id);
            this.expectedPacketId = id;
            this.outOfOrderPackets.clear();
            return true;
        }

        return handleOutOfOrderId(mb, id);
    }

    private boolean handleOutOfOrderId(Item mb, long id) {
        this.outOfOrderPackets.put(id,
                Arrays.copyOf(mb.packet.getData(), mb.buffer.limit()));
        if (this.outOfOrderPackets.size() < 10) {
            // maybe we receive the correct packet soon...
            return false;
        }

        final Map.Entry<Long, byte[]> first = this.outOfOrderPackets.pollFirstEntry();
        logMissed(first.getKey());
        this.expectedPacketId = first.getKey();
        mb.put(first.getValue());
        return true;
    }

    private void logMissed(long firstAfterMiss) {
        final int diff = (int) (firstAfterMiss - this.expectedPacketId);
        this.numPacketsMissed.addAndGet(diff);
        if (diff > 1) {
            this.logger.warn(" missed " + expectedPacketId + ".." + (firstAfterMiss - 1)
                    + " (" + diff + ")");
        }
        else {
            this.logger.warn(" missed " + expectedPacketId + " (1)");
        }
    }

    private boolean receive(Item mb) {
        try {
            this.receiver.receive(mb.packet);
            mb.onReceive();
            return true;
        } catch (SocketTimeoutException ste) {
            return false;
        } catch (IOException e) {
            this.logger.error("<receive> failed", e);
            return false;
        }
    }
}
