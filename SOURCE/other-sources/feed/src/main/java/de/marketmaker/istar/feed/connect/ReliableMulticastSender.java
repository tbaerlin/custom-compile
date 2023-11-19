/*
 * MulticastSender.java
 *
 * Created on 09.02.2005 12:00:46
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.connect;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.jmx.export.annotation.ManagedResource;

import de.marketmaker.istar.common.mcast.MulticastSenderImpl;

/**
 * A multicast sender that puts a long packetId at the start of every DatagramPacket; the
 * packetId is incremented by 1 for every packet, so missing packets can be detected.
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
@ManagedResource
public class ReliableMulticastSender extends MulticastSenderImpl implements BufferWriter {

    static final int MAX_RECORD_LENGTH = MULTICAST_PACKET_SIZE - 8;

    private AtomicLong id = new AtomicLong();

    private long lastLogId = 0;

    private final ByteBuffer buffer = ByteBuffer.wrap(new byte[MULTICAST_PACKET_SIZE]);

    private boolean sendOnEveryWrite = false;

    public ReliableMulticastSender() {
    }

    public void setSendOnEveryWrite(boolean sendOnEveryWrite) {
        this.sendOnEveryWrite = sendOnEveryWrite;
    }

    public void initialize() throws Exception {
        super.initialize();
        synchronized (this.buffer) {
            this.buffer.putLong(getNextId());
        }

        if (this.logger.isDebugEnabled()) {
            final Timer t = new Timer("sender-log", true);
            t.scheduleAtFixedRate(new TimerTask() {
                public void run() {
                    logStatus();
                }
            }, 1000, 10000);
        }
        this.logger.info("<initialize> packetLength=" + MULTICAST_PACKET_SIZE);
    }

    public void write(ByteBuffer toSend) throws IOException {
        if (toSend.remaining() > MAX_RECORD_LENGTH) {
            throw new IOException("too large to send: " + toSend.remaining());
        }

        synchronized (this.buffer) {
            if (this.buffer.remaining() < toSend.remaining()) {
                send();
            }
            this.buffer.put(toSend);

            if (this.sendOnEveryWrite) {
                send();
            }
        }
    }

    void send() throws IOException {
        sendPacket(this.buffer.array(), 0, this.buffer.position());

        this.buffer.clear();
        this.buffer.putLong(getNextId());
    }

    public long getId() {
        return id.get();
    }

    private void logStatus() {
        final long diff = (this.id.get() - this.lastLogId);
        this.lastLogId += diff;
        this.logger.debug("<logStatus> " + diff);
    }

    long getNextId() {
        return this.id.incrementAndGet();
    }
}
