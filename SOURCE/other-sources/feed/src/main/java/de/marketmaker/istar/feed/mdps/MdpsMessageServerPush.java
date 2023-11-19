/*
 * MdpsPushingMessageClient.java
 *
 * Created on 30.06.2006 06:55:30
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.mdps;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.concurrent.atomic.AtomicReference;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.Lifecycle;

import de.marketmaker.istar.common.nioframework.ByteArrayServer;
import de.marketmaker.istar.common.nioframework.ConnectionHandler;
import de.marketmaker.istar.common.nioframework.ConnectorService;
import de.marketmaker.istar.common.nioframework.DefaultConnectorListener;
import de.marketmaker.istar.common.nioframework.SelectorThread;

/**
 * Send mdps records to a clients by connecting to a socket offered by that client. Is able to
 * detect that a client has been restarted and tries to reconnect to that client.
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class MdpsMessageServerPush extends AbstractMdpsMessageServer implements
        InitializingBean, Lifecycle, ConnectionHandler {

    private final AtomicReference<ByteArrayServer.Client> c
            = new AtomicReference<>();

    private SelectorThread selectorThread;

    private Thread reconnectThread;

    private ConnectorService connectorService;

    private int reconnectIntervalMillis = 15 * 1000;

    public void setConnectorService(ConnectorService connectorService) {
        this.connectorService = connectorService;
    }

    public void setReconnectIntervalMillis(int reconnectIntervalMillis) {
        this.reconnectIntervalMillis = reconnectIntervalMillis;
    }

    public void handleConnection(SocketChannel sc) throws IOException {
        this.logger.info("<handleConnection> creating client for " + sc.socket().getRemoteSocketAddress());
        this.c.set(getServer().addClient(this.selectorThread, sc));
    }

    public SelectorThread getSelectorThread() {
        return selectorThread;
    }

    public void setSelectorThread(SelectorThread selectorThread) {
        this.selectorThread = selectorThread;
    }

    public void afterPropertiesSet() throws Exception {
        reconnect();
    }

    @Override
    public boolean isRunning() {
        return this.reconnectThread != null;
    }

    public void start() {
        this.reconnectThread = new Thread(MdpsMessageServerPush.this::checkConnection, "reconnectThread");
        this.reconnectThread.setDaemon(true);
        this.reconnectThread.start();
    }

    public void stop() {
        this.reconnectThread.interrupt();
        try {
            this.reconnectThread.join(15 * 1000);
            if (this.reconnectThread.isAlive()) {
                this.logger.warn("<stop> reconnectThread did not join, exiting anyway");
            }
        } catch (InterruptedException e) {
            this.logger.warn("<stop> interrupted?!");
        }
        this.reconnectThread = null;
    }

    private void checkConnection() {
        while (true) {
            try {
                Thread.sleep(this.reconnectIntervalMillis);
            } catch (InterruptedException e) {
                this.logger.info("<checkConnection> interrupted, returning");
                return;
            }
            doCheckConnection();
        }
    }

    private void doCheckConnection() {
        final ByteArrayServer.Client client = c.get();
        if (client != null && !client.isClosed()) {
            return;
        }
        c.set(null);
        reconnect();
    }

    private void reconnect() {
        if (this.c.get() != null) {
            return;
        }
        final DefaultConnectorListener listener = new DefaultConnectorListener(this);
        this.connectorService.connect(listener);
        if (listener.isSuccessfulConnect()) {
            this.logger.info("<reconnect> succeeded");
        }
        else {
            this.logger.warn("<reconnect> failed");
        }
    }

}
