/*
 * AbstractIstarRequest.java
 *
 * Created on 02.03.2005 14:21:11
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.common.nioframework;


import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages a non-blocking connection attempt to a remote host. Internal framework class
 * that is not supposed to be exposed to framework users.
 *
 * @author Oliver Flege
 */
public final class Connector implements ConnectorSelectorHandler {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    // The socket being connected.
    private SocketChannel sc;

    // The address of the remote endpoint.
    private final InetSocketAddress remoteAddress;

    // The selector used for receiving events.
    private final SelectorThread selectorThread;

    // The listener for the callback events.
    private final ConnectorListener listener;

    private int receiveBufferSize;

    private int sendBufferSize;

    /**
     * Creates a new instance. The connection is not attempted here.
     * Use connect() to start the attempt.
     * @param remoteAddress The remote endpoint where to connect.
     * @param listener The object that will receive the callbacks from
     * this Connector.
     * @param selectorThread The selector to be used.
     */
    public Connector(SelectorThread selectorThread, InetSocketAddress remoteAddress,
            ConnectorListener listener) {
        this.selectorThread = selectorThread;
        this.remoteAddress = remoteAddress;
        this.listener = listener;
    }

    public void setReceiveBufferSize(int receiveBufferSize) {
        this.receiveBufferSize = receiveBufferSize;
    }

    public void setSendBufferSize(int sendBufferSize) {
        this.sendBufferSize = sendBufferSize;
    }

    /**
     * Starts a non-blocking connection attempt.
     * @throws IOException
     */
    public void connect() throws IOException {
        if (this.selectorThread.isDone()) {
            throw new IllegalStateException("selectorThread not running, program restart required");
        }

        this.sc = SocketChannel.open();
        // Very important. Set to non-blocking. Otherwise a call
        // to connect will block until the connection attempt fails
        // or succeeds.
        this.sc.configureBlocking(false);
        if (this.receiveBufferSize > 0) {
            this.sc.socket().setReceiveBufferSize(this.receiveBufferSize);
        }
        if (this.sendBufferSize > 0) {
            this.sc.socket().setSendBufferSize(this.sendBufferSize);
        }
        boolean connected = this.sc.connect(this.remoteAddress);

        if (this.receiveBufferSize> 0
                && this.sc.socket().getReceiveBufferSize() != this.receiveBufferSize) {
            this.logger.warn("<connect> failed to set receiveBufferSize, got "
                    + this.sc.socket().getReceiveBufferSize());
        }
        if (this.sendBufferSize > 0
            && this.sc.socket().getSendBufferSize() != this.sendBufferSize) {
            this.logger.warn("<connect> failed to set sendBufferSize, got "
                    + this.sc.socket().getSendBufferSize());
        }

        if (connected) {
            connectListener();
        }
        else {
            // Registers itself to receive the connect event.
            this.selectorThread.registerChannelLater(sc, SelectionKey.OP_CONNECT, this, getErrorHandler());
        }
    }

    private CallbackErrorHandler getErrorHandler() {
        if (this.listener instanceof CallbackErrorHandler) {
            return (CallbackErrorHandler) listener;
        }
        else {
            return (message, ex) -> logger.error("<connect> failed: " + message, ex);
        }
    }

    /**
     * Called by the selector thread when the connection is
     * ready to be completed.
     */
    public void handleConnect() {
        try {
            if (!sc.finishConnect()) {
                this.listener.connectionFailed(this.remoteAddress, null);
                return;
            }
            connectListener();
        } catch (IOException ex) {
            this.listener.connectionFailed(this.remoteAddress, ex);
        }
    }

    private void connectListener() throws IOException {
        this.listener.connectionEstablished(sc);
    }

    public void cancel() {
        try {
            doCancel();
        } catch (InterruptedException e) {
            this.logger.warn("<cancel> interrupted?!");
            Thread.currentThread().interrupt();
        }
    }

    private void doCancel() throws InterruptedException {
        this.selectorThread.removeChannelAndWait(this.sc, true,
                (message, ex) -> logger.warn("<handleError> cancel failed for SocketChannel on " + this.remoteAddress, ex));
    }
}
