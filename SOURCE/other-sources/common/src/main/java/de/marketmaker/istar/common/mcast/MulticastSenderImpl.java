/*
 * MulticastSender.java
 *
 * Created on 14.11.2005 10:59:21
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.common.mcast;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.concurrent.atomic.AtomicLong;

import com.netflix.servo.annotations.Monitor;
import com.netflix.servo.annotations.MonitorTags;
import com.netflix.servo.tag.BasicTagList;
import com.netflix.servo.tag.TagList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.util.StringUtils;

import de.marketmaker.istar.common.lifecycle.Disposable;
import de.marketmaker.istar.common.lifecycle.Initializable;

import static com.netflix.servo.annotations.DataSourceLevel.CRITICAL;
import static com.netflix.servo.annotations.DataSourceType.COUNTER;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
@ManagedResource
public class MulticastSenderImpl implements Disposable, MulticastSender, Initializable {
    private volatile boolean doSend = true;

    private int timeToLive = 2;

    private int sendBufferSize = 256 * 1024;

    @Monitor(type = COUNTER, level = CRITICAL)
    private AtomicLong numPacketsSent = new AtomicLong();

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    private String groupname;

    private int port;

    private String interfaceName;

    private MulticastSocket msocket;

    @MonitorTags
    private TagList tags() {
        return BasicTagList.of("port", Integer.toString(this.port));
    }

    public void setTimeToLive(int timeToLive) {
        this.timeToLive = timeToLive;
    }

    public void setSendBufferSize(int sendBufferSize) {
        this.sendBufferSize = sendBufferSize;
    }

    public void setGroupname(String groupname) {
        this.groupname = groupname;
        this.logger.info("<setGroupname> " + this.groupname);
    }

    public void setPort(int port) {
        this.port = port;
        this.logger.info("<setPort> " + this.port);
    }

    public void setInterfaceName(String interfaceName) {
        this.interfaceName = interfaceName;
        this.logger.info("<setInterfaceName> " + this.interfaceName);
    }

    public void initialize() throws Exception {
        this.msocket = new MulticastSocket();
        if (StringUtils.hasText(this.interfaceName)) {
            this.msocket.setNetworkInterface(NetworkInterface.getByName(this.interfaceName));
        }

        this.msocket.setSendBufferSize(this.sendBufferSize);
        this.msocket.setTimeToLive(this.timeToLive);
        this.msocket.connect(getAddress(), this.port);

        if (this.msocket.getSendBufferSize() != sendBufferSize) {
            logger.warn("<initSocket> send buffer size could not be set correctly, requested "
                + sendBufferSize + " but got " + this.msocket.getSendBufferSize());
        }

        this.logger.info("<initialize> " + this);
    }

    public String toString() {
        final StringBuilder sb = new StringBuilder(50)
                .append("MulticastSender[")
                .append(this.groupname)
                .append(':')
                .append(this.port);
        if (StringUtils.hasText(this.interfaceName)) {
            sb.append('/').append(this.interfaceName);
        }
        sb.append(", sendBufferSize=").append(this.sendBufferSize);
        sb.append(", ttl=").append(this.timeToLive);
        sb.append(']');
        return sb.toString();
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

    public void sendPacket(final byte[] buf, final int offset, final int length) throws IOException {
        if (this.doSend) {
            final DatagramPacket dp = new DatagramPacket(buf, offset, length);
            send(dp);
            this.numPacketsSent.incrementAndGet();
        }
    }

    @ManagedAttribute
    public long getNumPacketsSent() {
        return this.numPacketsSent.get();
    }

    protected InetAddress getAddress() throws UnknownHostException {
        return InetAddress.getByName(this.groupname);
    }

    public void dispose() throws Exception {
        this.msocket.disconnect();
        this.msocket.close();
    }

    protected void send(DatagramPacket dp) throws IOException {
        this.msocket.send(dp);
    }
}
