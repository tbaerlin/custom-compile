/*
 * MulticastReceiver.java
 *
 * Created on 14.11.2005 08:50:56
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
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.util.StringUtils;

import static com.netflix.servo.annotations.DataSourceType.COUNTER;

/**
 * A generic receiver for multicast packets that is expected to be used from
 * another thread. {@link #receive(java.net.DatagramPacket)} must be called to fill the
 * given DatagramPacket.
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
@ManagedResource
public class MulticastReceiverImpl implements InitializingBean, MulticastReceiver {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    private int receiveBufferSize = 1024 * 1024;

    private int soTimeout = 0;

    private String groupname;

    private int port;

    private MulticastSocket msocket;

    @Monitor(type = COUNTER)
    private AtomicLong numPacketsReceived = new AtomicLong();

    private String interfaceName;

    @MonitorTags
    private TagList tags() {
        return BasicTagList.of("port", Integer.toString(this.port));
    }

    public void setSoTimeout(int soTimeout) {
        this.soTimeout = soTimeout;
        this.logger.info("<setSoTimeout> " + soTimeout);
    }

    public void setReceiveBufferSize(int receiveBufferSize) {
        this.receiveBufferSize = receiveBufferSize;
    }

    public void setGroupname(String groupname) {
        this.groupname = groupname;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setInterfaceName(String interfaceName) {
        this.interfaceName = interfaceName;
    }

    public void afterPropertiesSet() throws Exception {
        initSocket();
        this.logger.info("<afterPropertiesSet> " + this);
    }

    private void initSocket() throws IOException {
        this.msocket = new MulticastSocket(this.port);
        if (StringUtils.hasText(this.interfaceName)) {
            this.msocket.setNetworkInterface(NetworkInterface.getByName(this.interfaceName));
        }
        if (this.receiveBufferSize > 0) {
            this.msocket.setReceiveBufferSize(this.receiveBufferSize);
        }
        this.msocket.joinGroup(getGroup());
        if (this.soTimeout > 0) {
            this.msocket.setSoTimeout(this.soTimeout);
            this.logger.info("<initSocket> setSoTimeout to " + this.soTimeout + "ms");
        }

        if (this.receiveBufferSize > 0 && this.msocket.getReceiveBufferSize() != this.receiveBufferSize) {
            logger.warn("<initSocket> receive buffer size could not be set correctly, requested "
                    + receiveBufferSize + " but got " + this.msocket.getReceiveBufferSize());
        }
    }

    public String toString() {
        final StringBuilder sb = new StringBuilder(100)
                .append("MulticastReceiver[")
                .append(this.groupname)
                .append(':')
                .append(this.port);
        if (this.receiveBufferSize > 0) {
            sb.append(", receiveBufferSize=").append(this.receiveBufferSize);
        }
        if (this.soTimeout > 0) {
            sb.append(", soTimeout=").append(this.soTimeout).append("ms");
        }
        sb.append(", interfaceName=").append(this.interfaceName);
        sb.append(']');
        return sb.toString();
    }

    public void dispose() throws Exception {
        if (this.msocket != null) {
            this.msocket.leaveGroup(getGroup());
            this.msocket.close();
            this.msocket = null;
        }
    }

    public void receive(DatagramPacket packet) throws IOException {
        this.msocket.receive(packet);
        this.numPacketsReceived.incrementAndGet();
    }

    private InetAddress getGroup() throws UnknownHostException {
        return InetAddress.getByName(this.groupname);
    }

    @ManagedAttribute
    public long getNumPacketsReceived() {
        return this.numPacketsReceived.get();
    }
}
