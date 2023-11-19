/*
 * ConnectorService.java
 *
 * Created on 03.03.2006 15:40:34
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.common.nioframework;

import java.net.InetSocketAddress;
import java.io.IOException;

import org.springframework.beans.factory.InitializingBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class ConnectorServiceImpl implements ConnectorService, InitializingBean {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private SelectorThread selectorThread;

    private String host;

    private int port;

    private int receiveBufferSize;

    private int sendBufferSize;

    public void setReceiveBufferSize(int receiveBufferSize) {
        this.receiveBufferSize = receiveBufferSize;
    }

    public void setSendBufferSize(int sendBufferSize) {
        this.sendBufferSize = sendBufferSize;
    }

    public String toString() {
        return "Connector[" + this.host + ":" + this.port + "]";
    }

    public void setSelectorThread(SelectorThread selectorThread) {
        this.selectorThread = selectorThread;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void afterPropertiesSet() throws Exception {
        if (this.host == null) {
            throw new IllegalStateException("property host not set");
        }
        if (this.port == 0) {
            throw new IllegalStateException("property port not set");
        }
        if (this.selectorThread == null) {
            throw new IllegalStateException("property selectorThread not set");
        }
        if (this.receiveBufferSize == 0) {
            throw new IllegalStateException("property receiveBufferSize not set");
        }
        if (this.sendBufferSize == 0) {
            throw new IllegalStateException("property sendBufferSize not set");
        }
    }

    public void connect(ConnectorListener listener) {
        final InetSocketAddress remoteAddress = new InetSocketAddress(this.host, this.port);
        final Connector connector =
                new Connector(this.selectorThread, remoteAddress, listener);
        connector.setReceiveBufferSize(this.receiveBufferSize);
        connector.setSendBufferSize(this.sendBufferSize);
        try {
            connector.connect();
            this.logger.info("<connect> succeeded for " + remoteAddress);
        } catch (IOException e) {
            listener.connectionFailed(remoteAddress, e);
        }
    }
}
