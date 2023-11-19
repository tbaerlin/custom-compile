/*
 * OrderedMulticastReceiver.java
 *
 * Created on 23.03.2010 13:26:36
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.connect;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicLong;

import com.netflix.servo.annotations.Monitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;

import de.marketmaker.istar.common.mcast.MulticastReceiver;
import de.marketmaker.istar.common.mcast.MulticastSender;

import static com.netflix.servo.annotations.DataSourceType.COUNTER;

/**
 * Wraps a MulticastReceiver and ensures to some extent that packets are received in order. Relies
 * on the fact that each received DatagramPacket starts with a long sequence number as is the case
 * when it was sent using a {@link de.marketmaker.istar.feed.connect.ReliableMulticastSender}.
 *
 * @author oflege
 */
@ManagedResource
public class OrderedMulticastReceiver implements InitializingBean {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private long expectedPacketId = 0;

    private InetSocketAddress sourceAddress;

    @Monitor(type = COUNTER)
    private AtomicLong numPacketsMissed = new AtomicLong(0);

    private final SortedMap<Long, ByteBuffer> outOfOrderPackets = new TreeMap<>();

    private final ByteBuffer buffer
            = ByteBuffer.allocate(MulticastSender.MULTICAST_PACKET_SIZE);

    private final DatagramPacket packet
            = new DatagramPacket(this.buffer.array(), this.buffer.array().length);

    private MulticastReceiver receiver;

    public void setReceiver(MulticastReceiver receiver) {
        this.receiver = receiver;
    }

    public void afterPropertiesSet() throws Exception {
        if (this.receiver == null) {
            throw new IllegalStateException("receiver is null");
        }
    }

    @ManagedAttribute
    public long getNumPacketsMissed() {
        return this.numPacketsMissed.get();
    }

    @ManagedAttribute
    public void resetNumPacketsMissed() {
        this.numPacketsMissed.set(0);
    }

    /**
     * Receives the next packet and returns its content as a ByteBuffer; blocks until packet
     * is available. The buffer's content may be changed when this method is called again and
     * the same buffer is returned, so clients are expected to fully process the buffer's data
     * before calling this method again.
     * @return buffer with next packet's data
     * @throws IOException on error
     */
    public ByteBuffer receive() throws IOException {
        if (this.sourceAddress != null && !this.outOfOrderPackets.isEmpty()) {
            final Long first = this.outOfOrderPackets.firstKey();
            if (first == this.expectedPacketId) {
                return getBuffer(first);
            }
        }

        this.receiver.receive(this.packet);
        this.buffer.position(0);
        this.buffer.limit(this.packet.getLength());

        final long packetId = this.buffer.getLong();

        if (this.sourceAddress == null) {
            onSourceAddressChanged(packetId);
            return this.buffer;
        }

        if (packetId == this.expectedPacketId) {
            this.expectedPacketId++;
            return this.buffer;
        }

        if (!this.packet.getSocketAddress().equals(this.sourceAddress)) {
            onSourceAddressChanged(packetId);
            return this.buffer;
        }

        if (packetId < this.expectedPacketId) {
            this.logger.error("<receive> " + packetId + " < " + this.expectedPacketId
                    + " for " + this.sourceAddress);
            this.outOfOrderPackets.clear();
            this.expectedPacketId = packetId + 1;
            return this.buffer;
        }
        else {
            this.outOfOrderPackets.put(packetId, copy(this.buffer));
            if (this.outOfOrderPackets.size() > 10) {
                return getBufferAfterGap();
            }
            return receive();
        }
    }

    protected void clearSourceAddress() {
        this.sourceAddress = null;
    }

    private void onSourceAddressChanged(long packetId) {
        this.outOfOrderPackets.clear();
        storeSourceAddress(packetId);
        this.expectedPacketId = packetId + 1;
    }

    private void storeSourceAddress(long packetId) {
        final InetSocketAddress oldAddress = this.sourceAddress;
        this.sourceAddress = (InetSocketAddress) this.packet.getSocketAddress();
        this.logger.info("<storeSourceAddress> new source address: " + this.sourceAddress
                + ", was " + oldAddress + ", picking up at " + packetId);
    }

    private ByteBuffer copy(ByteBuffer bb) {
        bb.position(0);
        final ByteBuffer result = ByteBuffer.allocate(bb.remaining());
        result.put(bb);
        result.flip();
        return result;
    }

    private ByteBuffer getBufferAfterGap() {
        final Long first = this.outOfOrderPackets.firstKey();
        final long missed = first - this.expectedPacketId;
        this.logger.warn("<getBufferAfterGap> missed " + this.expectedPacketId
                + (missed > 1 ? (".." + (first - 1) + " (" + missed + ")") : ""));
        this.numPacketsMissed.addAndGet(missed);
        return getBuffer(first);
    }

    private ByteBuffer getBuffer(Long key) {
        this.expectedPacketId = key + 1;
        return (ByteBuffer) this.outOfOrderPackets.remove(key).position(8);
    }
}
