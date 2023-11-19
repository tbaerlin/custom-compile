/*
 * AbstractIstarRequest.java
 *
 * Created on 02.03.2005 14:21:11
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.common.nioframework;


import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.SmartLifecycle;
import org.springframework.util.StringUtils;

/**
 * Listens for incoming connections from clients, using a selector
 * to receive connect events. Therefore, instances of this class
 * don't have an associated thread. When a connection is established,
 * it notifies a listener using a callback. The number of open connections can be limited.
 * When the limit is reached, incoming connections will be accepted, but the associated
 * socket channel will be closed immediately. Note that it is not possible to deny
 * a client connection by just not registering for the OP_ACCEPT event.
 * @author Oliver Flege
 */
public final class Acceptor implements AcceptSelectorHandler, InitializingBean, SmartLifecycle {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    // Used to receive incoming connections
    private ServerSocketChannel ssc;

    // The selector used by this instance.
    private SelectorThread selectorThread;

    private int listenPort = 0;

    private String host = null;

    private int receiveBufferSize;

    private int sendBufferSize;

    private int maxNumClients = 64;

    // Listener to be notified of new connections and of errors.
    private AcceptorListener listener;

    private AtomicInteger numClients = new AtomicInteger();

    private AtomicBoolean closed = new AtomicBoolean(false);

    private InetSocketAddress isa;

    private int backlog = 16;

    public int getNumClients() {
        return numClients.get();
    }

    public void setListener(AcceptorListener listener) {
        this.listener = listener;
    }

    public void setListenPort(int listenPort) {
        this.listenPort = listenPort;
    }

    public void setReceiveBufferSize(int receiveBufferSize) {
        this.receiveBufferSize = receiveBufferSize;
    }

    public void setSendBufferSize(int sendBufferSize) {
        this.sendBufferSize = sendBufferSize;
    }

    public void setBacklog(int backlog) {
        this.backlog = backlog;
    }

    /**
     * @param host name or ip-address to bind the socket to. Leave null or set to en empty string
     * to bind the socket to the wildcard address 0
     */
    public void setHost(String host) {
        this.host = host;
    }

    public void setMaxNumClients(int maxNumClients) {
        this.maxNumClients = maxNumClients;
    }

    public void setSelectorThread(SelectorThread selectorThread) {
        this.selectorThread = selectorThread;
    }

    public SelectorThread getSelectorThread() {
        return selectorThread;
    }

    /**
     * Starts listening for incoming connections. This method does
     * not block waiting for connections. Instead, it registers itself
     * with the selector to receive connect events.
     * @throws IOException
     */
    public void afterPropertiesSet() throws Exception {
        if (this.maxNumClients < 1) {
            this.logger.info("<afterPropertiesSet> " + getInetSocketAddress() + " is DISABLED");
            return;
        }
        this.ssc = ServerSocketChannel.open();
        if (this.receiveBufferSize > 0) {
            this.ssc.socket().setReceiveBufferSize(this.receiveBufferSize);
        }
        this.isa = getInetSocketAddress();
        this.ssc.socket().bind(this.isa, this.backlog);
    }

    @Override
    public boolean isAutoStartup() {
        return false;
    }

    @Override
    public void stop(Runnable callback) {
        try {
            stop();
        } finally {
            callback.run();
        }
    }

    @Override
    public void stop() {
        if (!this.closed.compareAndSet(false, true)) {
            this.logger.warn("<stop> already closed");
            return;
        }

        try {
            this.selectorThread.removeChannelAndWait(this.ssc, true,
                    (message, ex) -> logger.error("<destroy> failed to remove channel", ex));
        } catch (InterruptedException e) {
            this.logger.error("<stop> interrupted?!", e);
        }

        this.logger.info("<stop> succeeded, no longer listening on " + this.listenPort);
    }

    @Override
    public void start() {
        enableAccept();
        this.logger.info("<start> now listening on " + this.isa
                + ", will accept at most " + this.maxNumClients + " clients, backlog=" + this.backlog);
    }

    @Override
    public boolean isRunning() {
        return (this.ssc != null) && this.ssc.isRegistered();
    }

    @Override
    public int getPhase() {
        // accept connections as late as possible during startup so that all components that forward
        // data to clients are up and running
        return Integer.MAX_VALUE;
    }

    private InetSocketAddress getInetSocketAddress() throws UnknownHostException {
        return new InetSocketAddress(getInetAddress(), this.listenPort);
    }

    private InetAddress getInetAddress() throws UnknownHostException {
        return StringUtils.hasText(this.host) ? InetAddress.getByName(this.host) : null;
    }

    private void enableAccept() {
        this.selectorThread.registerChannelLater(this.ssc, SelectionKey.OP_ACCEPT, this,
                (message, ex) -> logger.error("<enableAccept> failed", ex));
    }

    public String toString() {
        return "Acceptor[" + this.isa
                + ", #cons=" + this.numClients.get()
                + "/" + this.maxNumClients
                + "]";
    }

    /**
     * Called by SelectorThread when the underlying server socket is
     * ready to accept a connection. This method should not be called
     * from anywhere else.
     */
    public void handleAccept() {
        SocketChannel sc = null;
        try {
            sc = ssc.accept();

            initSocketBuffers(sc);

            // Connection established
            listener.socketConnected(this, sc);

            final int numConnected = this.numClients.incrementAndGet();

            this.logger.info("<handleAccept> accepted " + sc.socket().getRemoteSocketAddress()
                    + " on " + sc.socket().getLocalSocketAddress() + ", #connected = " + numConnected
                    + "/" + this.maxNumClients);

        } catch (IOException e) {
            this.logger.warn("<handleAccept> failed on " + this, e);
            handleAcceptError(sc);
        } finally {
            addAcceptInterestNow();
        }
    }

    private void initSocketBuffers(SocketChannel sc) throws SocketException {
        final int rbs = sc.socket().getReceiveBufferSize();
        if (this.receiveBufferSize > 0 && rbs != this.receiveBufferSize) {
            this.logger.warn("<handleAccept> receive buffer size = " + rbs +
                    ", requested: " + this.receiveBufferSize);
        }
        else {
            this.logger.info("<handleAccept> receive buffer size = " + rbs);
        }

        if (this.sendBufferSize > 0) {
            sc.socket().setSendBufferSize(this.sendBufferSize);
        }
        final int sbs = sc.socket().getSendBufferSize();
        if (this.sendBufferSize > 0 && sbs < this.sendBufferSize) {
            this.logger.warn("<handleAccept> send buffer size = " + sbs +
                    ", requested: " + this.sendBufferSize);
        }
        else {
            this.logger.info("<handleAccept> send buffer size = " + sbs);
        }
    }

    private void addAcceptInterest() {
        if (this.selectorThread.isCurrentThreadSelectorThread()) {
            addAcceptInterestNow();
        }
        else {
            this.selectorThread.invokeLater(this::addAcceptInterestNow);
        }
    }
    private void addAcceptInterestNow() {
        // Reactivate interest to receive the next connection. We
        // can use one of the XXXNow methods since this method is being
        // executed on the selector's thread.
        if (this.numClients.get() < this.maxNumClients) {
            try {
                this.selectorThread.addChannelInterestNow(ssc, SelectionKey.OP_ACCEPT);
            } catch (IOException e) {
                this.logger.error("<addAcceptInterestNow> failed to add OP_ACCEPT", e);
            }
        }
    }

    private void handleAcceptError(SocketChannel sc) {
        if (sc == null) {
            // ssc.accept failed, numClients not incremented,
            return;
        }
        try {
            this.selectorThread.removeChannelAndWait(sc, true,
                    (message, ex) -> logger.warn("<handleAcceptError> failed removing channel", ex));
        } catch (InterruptedException e1) {
            logger.warn("<handleAcceptError> interrupted!?", e1);
        }
    }

    /**
     * to be called by the listener when an established connection has been closed. Decrements
     * the counter for established connections that is used to close incoming connections
     * as soon as maxNumClients clients are connected.
     */
    public void handleDisconnect() {
        if (this.numClients.getAndDecrement() == this.maxNumClients) {
            addAcceptInterest();
        }
        this.logger.info("<handleDisconnect> numConnected = " + this.numClients.get());
    }
}
