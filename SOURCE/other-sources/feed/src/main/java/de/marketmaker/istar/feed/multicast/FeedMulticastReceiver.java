/*
 * MulticastReceiver.java
 *
 * Created on 29.07.14 11:06
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.multicast;

import java.nio.ByteBuffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.TimeoutException;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.jmx.export.annotation.ManagedResource;

import static de.marketmaker.istar.feed.multicast.FeedMulticastSender.MULTICAST_PACKET_SIZE;
import static java.util.concurrent.Executors.newSingleThreadExecutor;

/**
 * AbstractFeedMulticastReceiver that publishes received packets
 * in a {@link com.lmax.disruptor.RingBuffer}
 * from which a worker thread retrieves them and forwards them to a delegate {@link #handler}
 * for processing.
 *
 * @author oflege
 */
@ManagedResource
public class FeedMulticastReceiver extends AbstractFeedMulticastReceiver implements
        EventFactory<ByteBuffer>, BeanNameAware {

    private Disruptor<ByteBuffer> disruptor;

    private RingBuffer<ByteBuffer> ringBuffer;

    private String name = getClass().getSimpleName() + "@" + System.identityHashCode(this);

    private final ExecutorService executor = newSingleThreadExecutor(r -> new Thread(r, getName()));

    private int ringBufferSize = 128;

    private EventHandler<ByteBuffer> handler;

    /**
     * current ring buffer sequence id
     */
    private long seq;

    public FeedMulticastReceiver() {
        this(MULTICAST_PACKET_SIZE);
    }

    @Override
    public void setBeanName(String s) {
        this.name = s;
    }

    private String getName() {
        return this.name;
    }

    // access only for package local testing or from public constructor
    FeedMulticastReceiver(int multicastPacketSize) {
        super(multicastPacketSize);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (this.handler == null) {
            throw new IllegalStateException("no handler set");
        }

        // the internal buffer for storing received packets should be at least as large as
        // the socket's receiveBufferSize
        if (this.receiveBufferSize > 0
                && this.receiveBufferSize > (this.ringBufferSize * this.multicastPacketSize)) {
            throw new IllegalStateException("receiveBufferSize(" + receiveBufferSize
                    + ") > ringBufferSize(" + ringBufferSize + ") * multicastPacketSize(" + multicastPacketSize + ")");
        }

        super.afterPropertiesSet();

        this.disruptor = new Disruptor<>(this, this.ringBufferSize,
                this.executor, ProducerType.SINGLE, new BlockingWaitStrategy());
        //noinspection unchecked
        this.disruptor.handleEventsWith(this.handler);
    }

    public void setRingBufferSize(int ringBufferSize) {
        this.ringBufferSize = ringBufferSize;
    }

    public void setHandler(EventHandler<ByteBuffer> handler) {
        this.handler = handler;
    }


    @Override
    protected void publish() {
        this.ringBuffer.publish(this.seq);
        prepareBuffer();
    }


    @Override
    public void start() {
        this.ringBuffer = this.disruptor.start();
        prepareBuffer();
        super.start();
    }

    @Override
    public void stop() {
        super.stop();
        shutdownDisruptor();
        shutdownExecutor();
    }

    protected void prepareBuffer() {
        this.seq = this.ringBuffer.next();
        this.mcBuffer = this.ringBuffer.get(this.seq);
    }

    private void shutdownExecutor() {
        this.executor.shutdown();
        try {
            if (!this.executor.awaitTermination(10, TimeUnit.SECONDS)) {
                this.logger.error("<shutdownExecutor> timeout");
            }
        } catch (InterruptedException e) {
            this.logger.error("<shutdownExecutor> interrupted!?");
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

    @Override
    public ByteBuffer newInstance() {
        return createBuffer(false);
    }
}
