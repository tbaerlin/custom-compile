/*
 * MulticastSender.java
 *
 * Created on 29.07.14 11:06
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.multicast;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.StandardProtocolFamily;
import java.net.StandardSocketOptions;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.concurrent.atomic.AtomicLong;

import com.netflix.servo.annotations.Monitor;
import com.netflix.servo.annotations.MonitorTags;
import com.netflix.servo.tag.BasicTagList;
import com.netflix.servo.tag.TagList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;

import de.marketmaker.istar.feed.connect.FeedStats;

import static com.netflix.servo.annotations.DataSourceType.COUNTER;

/**
 * A multicast sender that uses a {@link java.nio.channels.DatagramChannel}, so that packets
 * can be sent directly from a <code>ByteBuffer</code>, no need for a {@link java.net.DatagramPacket}.
 * The <code>DatagramChannel</code> is used in <em>blocking</em> mode.
 * @author oflege
 */
@ManagedResource
public class FeedMulticastSender implements InitializingBean, FeedStats.PacketSource, FeedStats.ByteSource {

    /**
     * Size of a packet that can be sent without fragmentation
     */
    public static final int MULTICAST_PACKET_SIZE =
            Integer.getInteger("istar.multicast.packetsize", 1250);

    protected Logger logger = LoggerFactory.getLogger(getClass());

    private volatile boolean doSend = true;

    private DatagramChannel channel;

    private NetworkInterface ni;

    private int port;

    private InetAddress group;

    private int sendBufferSize = 1 << 20;

    @Monitor(type = COUNTER)
    private AtomicLong numPacketsSent = new AtomicLong();

    @Monitor(type = COUNTER)
    private AtomicLong numBytesSent = new AtomicLong();

    private int timeToLive = 1;

    private static final String GAUGE = "feed_multicast_sender_gauge";
    private MeterRegistry meterRegistry;

    public void setMeterRegistry(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @MonitorTags
    private TagList tags() {
        return BasicTagList.of("port", Integer.toString(this.port));
    }

    public void setTimeToLive(int timeToLive) {
        this.timeToLive = timeToLive;
    }

    @Override
    public long numBytesSent() {
        return this.numBytesSent.get();
    }

    @Override
    public long numPacketsSent() {
        return getNumPacketsSent();
    }

    @ManagedAttribute
    public long getNumPacketsSent() {
        return this.numPacketsSent.get();
    }

    public void setSendBufferSize(int sendBufferSize) {
        this.sendBufferSize = sendBufferSize;
    }

    public void setGroup(String groupName) throws UnknownHostException {
        this.group = InetAddress.getByName(groupName);
    }

    public void setNetworkInterface(String name) throws SocketException {
        this.ni = NetworkInterface.getByName(name);
        if (this.ni == null) {
            throw new IllegalArgumentException("no such interface '" + name + "'");
        }
    }

    public void setPort(int port) {
        this.port = port;
    }

    @ManagedAttribute
    public void setDoSend(boolean doSend) {
        this.doSend = doSend;
        this.logger.info("<setDoSend> " + doSend);
    }

    @ManagedAttribute
    public boolean isDoSend() {
        return this.doSend;
    }


    @Override
    public void afterPropertiesSet() throws Exception {
        this.channel = DatagramChannel.open(StandardProtocolFamily.INET);
        timeToLive = 1;
        this.channel.setOption(StandardSocketOptions.SO_REUSEADDR, true)
                .setOption(StandardSocketOptions.IP_MULTICAST_TTL, timeToLive);
        if (this.ni != null) {
            this.channel.setOption(StandardSocketOptions.IP_MULTICAST_IF, ni);
        }
        if (this.sendBufferSize > 0) {
            this.channel.setOption(StandardSocketOptions.SO_SNDBUF, this.sendBufferSize);
        }
        this.channel.connect(new InetSocketAddress(this.group, port));
        ackSendBufferSize(this.channel.socket().getSendBufferSize());

        if (this.meterRegistry != null) {
            Gauge.builder(GAUGE, () -> this.numBytesSent)
                .tags(Tags.of("t", "num_bytes_sent")).register(this.meterRegistry);
            Gauge.builder(GAUGE, () -> this.numPacketsSent)
                .tags(Tags.of("t", "num_packets_sent")).register(this.meterRegistry);
        }
    }

    private void ackSendBufferSize(int size) {
        if (this.sendBufferSize > 0 && size < this.sendBufferSize) {
            this.logger.warn("<ackSendBufferSize> requested size " + this.sendBufferSize
                    + " < actual size: " + size);
        }
    }

    void send(ByteBuffer bb) throws IOException {
        if (this.doSend) {
            this.numBytesSent.addAndGet(bb.remaining());
            this.channel.write(bb);
            this.numPacketsSent.incrementAndGet();
        }
    }
}
