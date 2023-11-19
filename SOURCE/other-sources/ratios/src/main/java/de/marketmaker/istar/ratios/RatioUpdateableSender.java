/*
 * RatioUpdateableSender.java
 *
 * Created on 14.04.2010 09:00:29
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.ratios;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.TimeUnit;

import net.jcip.annotations.GuardedBy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.Lifecycle;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.util.ClassUtils;

import de.marketmaker.istar.common.util.ThroughputLimiter;

/**
 * @author oflege
 */
@ManagedResource
abstract class RatioUpdateableSender
        implements BackendUpdateReceiver, InitializingBean, Lifecycle, Runnable {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private int maxPacketsPerSecond = 1000;

    private ThroughputLimiter limiter;

    private volatile boolean stopped = false;

    private volatile boolean paused = false;

    private final Object bufferMutex = new Object();

    @GuardedBy("bufferMutex")
    private long packetCount = 0L;

    /**
     * Collects updates so that multicast packets will be as full as possible.
     */
    @GuardedBy("bufferMutex")
    private ByteBuffer buffer;

    /**
     * Makes sure updates will never be pending in the buffer for more than 1s before they
     * are sent.
     */
    private Thread flushThread;

    public void afterPropertiesSet() throws Exception {
        this.limiter = new ThroughputLimiter(this.maxPacketsPerSecond);
        this.buffer = createBuffer();
        this.logger.info("<afterPropertiesSet> buffer has " + this.buffer.remaining() + " bytes");
    }

    protected abstract ByteBuffer createBuffer();

    @Override
    public boolean isRunning() {
        return this.flushThread != null;
    }

    public void start() {
        this.flushThread = new Thread(this, ClassUtils.getShortName(getClass()) + "-flush");
        this.flushThread.start();
    }

    public void stop() {
        this.stopped = true;
        try {
            this.flushThread.join(); // no need to interrupt, checks every second
        } catch (InterruptedException e) {
            this.logger.error("<stop> interrupted?!");
            Thread.currentThread().interrupt();
        }
    }

    @ManagedAttribute
    public boolean isPaused() {
        return paused;
    }

    @ManagedAttribute
    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    public void run() {
        long lastCounter = 0L;
        while (!this.stopped) {
            sleep1s();
            synchronized(this.bufferMutex) {
                // counter has not changed and data available
                if (this.packetCount == lastCounter && this.buffer.position() > 0) {
                    send(this.buffer);
                }
                lastCounter = this.packetCount;
            }
        }
    }

    private void sleep1s() {
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            this.logger.warn("<sleep1s> interrupted?!");
            Thread.currentThread().interrupt();
        }
    }

    public void setMaxPacketsPerSecond(int maxPacketsPerSecond) {
        this.maxPacketsPerSecond = maxPacketsPerSecond;
        this.logger.info("<setMaxPacketsPerSecond> " + maxPacketsPerSecond);
    }

    public void update(byte[] bytes) {
        synchronized (this.bufferMutex) {
            if (canAppendToBuffer(bytes)) {
                append(this.buffer, bytes);
                return;
            }

            if (this.buffer.position() > 0) {
                send(this.buffer);

                if (canAppendToBuffer(bytes)) {
                    append(this.buffer, bytes);
                    return;
                }
            }

            // bytes is too long to fit in buffer, use tmp and send immediately
            final ByteBuffer tmp = ByteBuffer.allocate(bytes.length + 12);
            append(tmp, bytes);
            send(tmp);
        }
    }

    protected void append(ByteBuffer bb, byte[] bytes) {
        if (bb.position() == 0 && isPacketWithSequenceNumber()) {
            ++this.packetCount;
            bb.putLong(this.packetCount);
        }
        bb.putInt(bytes.length);
        bb.put(bytes);
    }

    private boolean canAppendToBuffer(byte[] bytes) {
        final int statusData = this.buffer.position() == 0 ? 12 : 4;
        return this.buffer.remaining() >= bytes.length + statusData;
    }

    private void send(ByteBuffer bb) {
        if (bb.position() == 0) {
            return;
        }
        this.limiter.ackAction();
        try {
            bb.flip();
            sendBuffer(bb);
        } catch (IOException e) {
            this.logger.error("<send> failed", e);
        } finally{
            bb.clear();
        }

        while (this.paused) {
            // keep the synchronized lock on bufferMutex and block all other threads
            this.logger.info("<send> PAUSED");
            sleep1s();
        }
    }

    protected abstract void sendBuffer(ByteBuffer bb) throws IOException;

    protected abstract boolean isPacketWithSequenceNumber();
}
