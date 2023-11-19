/*
 * MdpsWriter.java
 *
 * Created on 10.04.15 10:39
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.mdps;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.Pipe;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.Lifecycle;
import org.springframework.util.StringUtils;

import de.marketmaker.istar.common.nioframework.ReadWriteSelectorHandler;
import de.marketmaker.istar.common.nioframework.SelectorThread;
import de.marketmaker.istar.common.util.ByteUtil;
import de.marketmaker.istar.feed.connect.FeedStats;
import de.marketmaker.istar.feed.mux.MuxOut;

/**
 * Base class for classes that create mdps feed records that will be forwarded to a delegate
 * {@link MuxOut} from which the data can be read as a tcp stream. Since the <tt>MuxOut</tt>
 * relies on being manipulated by a single {@link SelectorThread}, this class creates a
 * {@link Pipe} to communicate with that thread.
 * <p>
 * The internal buffer is flushed at regular intervals, so that records are not kept for too long
 * even if no new records arrive. If the buffer is empty on flush, a heartbeat record will be sent.

 * @author oflege
 */
abstract class MdpsWriter implements InitializingBean, Lifecycle, FeedStats.MessageSource {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private final BlockingQueue<ByteBuffer> freeBuffers = new LinkedBlockingQueue<>(16);

    private final BlockingQueue<ByteBuffer> fullBuffers = new LinkedBlockingQueue<>(16);

    protected final Object bufferMutex = new Object();

    private final ByteBuffer sourceBuffer = ByteBuffer.allocate(16);

    private final ByteBuffer sinkBuffer = ByteBuffer.allocate(1);

    protected final AtomicLong numMessagesSent = new AtomicLong();

    protected int protocolVersion = 1;

    protected boolean usePrefix = true;

    protected ByteBuffer bb;

    private ByteOrder byteOrder;

    private Timer flushTimer;

    private int messageBufferSize = 131072;

    protected byte[] heartbeatKey;

    private boolean sent = false;

    private MuxOut muxOut;

    private SelectorThread selectorThread;

    private Pipe.SourceChannel source;

    private Pipe.SinkChannel sink;

    public MdpsWriter(byte[] heartbeatKey) {
        this.heartbeatKey = heartbeatKey;
    }

    abstract void createHeartbeat();

    public void setUsePrefix(boolean usePrefix) {
        this.usePrefix = usePrefix;
        this.logger.info("<setUsePrefix> = " + this.usePrefix);
    }

    public void setHeartbeatKey(String key) {
        if (StringUtils.hasText(key) && !"null".equals(key)) {
            this.heartbeatKey = ByteUtil.toBytes(key);
            this.logger.info("<setHeartbeatKey> using " + key + " for heartbeats");
        }
        else {
            this.heartbeatKey = null;
            this.logger.info("<setHeartbeatKey> empty, will not send heartbeats");
        }
    }

    public void setSelectorThread(SelectorThread selectorThread) {
        this.selectorThread = selectorThread;
    }

    public void setMuxOut(MuxOut muxOut) {
        this.muxOut = muxOut;
    }

    public void setMessageBufferSize(int messageBufferSize) {
        this.messageBufferSize = messageBufferSize;
        this.logger.info("<setMessageBufferSize> " + this.messageBufferSize);
    }

    public void setProtocolVersion(int protocolVersion) {
        if (protocolVersion != 1 && protocolVersion != 3) {
            throw new IllegalArgumentException("unsupported " + protocolVersion);
        }
        this.protocolVersion = protocolVersion;
        this.logger.info("<setProtocolVersion> = " + this.protocolVersion);
    }

    public void afterPropertiesSet() throws Exception {
        this.byteOrder = MdpsFeedUtils.getByteOrder(this.protocolVersion);
        for (int i = 0; i < 16; i++) {
            this.freeBuffers.put(createBuffer());
        }
        this.bb = freeBuffers.remove();

        Pipe pipe = Pipe.open();
        this.sink = pipe.sink();
        this.sink.configureBlocking(true);

        this.source = pipe.source();

        this.selectorThread.registerChannelNow(this.source, this.source.validOps(),
                new ReadWriteSelectorHandler() {
                    @Override
                    public void handleRead() {
                        onFullBuffersAvailable();
                    }

                    @Override
                    public void handleWrite() {
                        // write is not a valid op of source, so we never get here
                    }
                });
    }

    private void onFullBuffersAvailable() {
        this.sourceBuffer.clear();
        try {
            this.selectorThread.addChannelInterestNow(this.source, this.source.validOps());
            this.source.read(this.sourceBuffer);
        } catch (IOException e) {
            this.logger.error("<readNewSequence> failed", e);
            return;
        }
        while (!this.fullBuffers.isEmpty()) {
            ByteBuffer fb = this.fullBuffers.remove();
            this.muxOut.append(fb);
            this.freeBuffers.add(fb);
        }
    }

    @Override
    public boolean isRunning() {
        return this.flushTimer != null;
    }

    public void start() {
        this.flushTimer = new Timer(getClass().getSimpleName() + "-flush", true);
        this.flushTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                flush();
            }
        }, 5000, 5000);
    }

    public void stop() {
        this.flushTimer.cancel();
    }

    void flush() {
        synchronized (this.bufferMutex) {
            if (!this.sent) {
                sendBuffer();
            }
            this.sent = false;
        }
    }

    @Override
    public long numMessagesSent() {
        return this.numMessagesSent.get();
    }

    private ByteBuffer createBuffer() {
        return createBuffer(this.messageBufferSize);
    }

    private ByteBuffer createBuffer(int size) {
        return ByteBuffer.allocateDirect(size).order(this.byteOrder);
    }

    ByteBuffer getCurrentBuffer() {
        return this.bb;
    }

    protected void sendBuffer() {
        assert Thread.holdsLock(this.bufferMutex);

        this.sent = true;

        if (this.bb.position() == 0) {
            if (this.heartbeatKey == null) {
                return;
            }
            createHeartbeat();
        }
        this.bb.flip();
        this.fullBuffers.add(this.bb);
        notifyPipe();
        this.bb = null;

        do {
            try {
                this.bb = this.freeBuffers.take();
            } catch (InterruptedException e) {
                this.logger.error("<sendBuffer> interrupted?!");
            }
        } while (this.bb == null);

        this.bb.clear();
    }

    private void notifyPipe() {
        this.sinkBuffer.clear();
        try {
            while (sinkBuffer.hasRemaining()) {
                this.sink.write(this.sinkBuffer);
            }
        } catch (IOException e) {
            this.logger.error("<notifyPipe> failed", e);
        }
    }
}
