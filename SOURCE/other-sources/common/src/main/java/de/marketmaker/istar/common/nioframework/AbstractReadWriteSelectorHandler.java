/*
 * AbstractReadWriteSelectorHandler.java
 *
 * Created on 03.03.2006 16:21:04
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.common.nioframework;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for objects implementing {@link ReadWriteSelectorHandler} to perform
 * non-blocking IO. Encapsulates the
 * details of registering and unregistering read and write interest, the selector thread
 * and the acceptor. After an instance of this class has been created, its
 * {@link #registerChannel(boolean, boolean)} method should be called to associate this
 * handler with its channel in the SelectorThread.
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public abstract class AbstractReadWriteSelectorHandler
        implements ReadWriteSelectorHandler, CallbackErrorHandler {

    private static final String CONNECTION_RESET_BY_PEER = "Connection reset by peer";

    private static final String BROKEN_PIPE = "Broken pipe";

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private final SelectorThread selectorThread;

    private SocketChannel sc;

    private Acceptor acceptor;

    private final AtomicBoolean closed = new AtomicBoolean(false);

    /**
     * To be used if this is a client component created by a {@link ConnectorListener} upon
     * successful connection to a server socket.
     * @param selectorThread io thread to be used
     * @param sc used for reads and writes
     */
    protected AbstractReadWriteSelectorHandler(SelectorThread selectorThread, SocketChannel sc) {
        this.selectorThread = selectorThread;
        this.sc = sc;
    }

    /**
     * To be used if this is a server component, that is it was created as response to an
     * accepted socket connection. Once this component is closed, the acceptor's
     * {@link de.marketmaker.istar.common.nioframework.Acceptor#handleDisconnect()} method
     * will be invoked.
     * @param acceptor established connection
     * @param sc used for reads and writes
     */
    protected AbstractReadWriteSelectorHandler(Acceptor acceptor, SocketChannel sc) {
        this.acceptor = acceptor;
        this.sc = sc;
        this.selectorThread = this.acceptor.getSelectorThread();
    }

    public SocketAddress getRemoteAddress() {
        return this.sc.socket().getRemoteSocketAddress();
    }

    public SocketAddress getLocalAddress() {
        return this.sc.socket().getLocalSocketAddress();
    }

    /**
     * Registers this object with its channel in the SelectorThread, should be called after
     * instantiating an object of this class. Both read and write may be false, in that case
     * a client should later invoke either {@link #enableReading()} or {@link #enableWriting()}
     * in order to do s.th. with the socket channel.
     * @param read whether a read interest should be registered.
     * @param write whether a write interest should be registered.
     * @throws IOException if registration fails.
     */
    public void registerChannel(boolean read, boolean write) throws IOException {
        final int op = (read ? SelectionKey.OP_READ : 0) | (write ? SelectionKey.OP_WRITE : 0);
        this.selectorThread.registerChannelNow(this.sc, op, this);
    }

    /**
     * Enables interest in reading. Must only be called from either doRead or doWrite, as
     * those are invoked by the selector thread
     * @throws IOException
     */
    protected void enableReadingNow() throws IOException {
        this.selectorThread.addChannelInterestNow(this.sc, SelectionKey.OP_READ);
    }

    /**
     * Enables interest in writing. Must only be called from either doRead or doWrite, as
     * those are invoked by the selector thread
     * @throws IOException
     */
    protected void enableWritingNow() throws IOException {
        this.selectorThread.addChannelInterestNow(this.sc, SelectionKey.OP_WRITE);
    }

    /**
     * Enables interest in reading, can be called from any thread
     */
    protected void enableReading() {
        this.selectorThread.addChannelInterestLater(this.sc, SelectionKey.OP_READ, this);
    }

    /**
     * Enables interest in writing, can be called from any thread
     */
    protected void enableWriting() {
        this.selectorThread.addChannelInterestLater(this.sc, SelectionKey.OP_WRITE, this);
    }

    @Override
    public void handleError(String message, Exception ex) {
        this.logger.error("<handleError> " + ((message != null) ? message : ""), ex);
    }

    public void handleRead() {
        try {
            if (doRead(this.sc)) {
                enableReadingNow();
            }
        } catch (IOException e) {
            if (CONNECTION_RESET_BY_PEER.equals(e.getMessage())) {
                this.logger.warn("<handleRead> " + CONNECTION_RESET_BY_PEER);
            }
            else {
                this.logger.error("<handleRead> failed", e);
            }
            onError(e);
            close();
        }
    }

    public void handleWrite() {
        try {
            if (doWrite(this.sc)) {
                enableWritingNow();
            }
        } catch (IOException e) {
            // HACK, but there is no other way to detect a disconnected client
            if (BROKEN_PIPE.equals(e.getMessage()) || CONNECTION_RESET_BY_PEER.equals(e.getMessage())) {
                this.logger.info("<handleWrite> " + e.getMessage() + ", client closed connection");
            }
            else {
                this.logger.error("<handleWrite> failed", e);
            }
            onError(e);
            close();
        }
    }

    /**
     * To be called from clients when they no longer need the socket channel or detect that
     * a server has been closed. Also called internally whenever {@link #doWrite(java.nio.channels.SocketChannel)}
     * or {@link #doRead(java.nio.channels.SocketChannel)} throw an exception.
     */
    protected void close() {
        if (!this.closed.compareAndSet(false, true)) {
            this.logger.warn("<close> already closed " + this);
            return;
        }

        try {
            this.selectorThread.removeChannelAndWait(this.sc, true, (message, ex) -> logger.error("<close> failed to remove channel", ex));
        } catch (InterruptedException e) {
            this.logger.error("<close> interrupted?!", e);
        }

        if (this.acceptor != null) {
            this.acceptor.handleDisconnect();
        }

        this.logger.info("<close> disconnected from "
                + this.sc.socket().getRemoteSocketAddress() + ": " + this);
    }

    public boolean isClosed() {
        return this.closed.get();
    }

    /**
     * Data is ready to be read from <code>sc</code>, this op runs in a SelectorThread's thread
     * @return true if further data should be read.
     * @param sc to read from; client should <em>never</em> invoke close() on <code>sc</code> directly,
     * but instead call the {@link #close()} method.
     * @throws IOException
     */
    protected abstract boolean doRead(SocketChannel sc) throws IOException;

    /**
     * Data is ready to be written to <code>sc</code>, this op runs in a SelectorThread's thread
     * @return true if further data should be written.
     * @param sc to write to; client should <em>never</em> invoke close() on <code>sc</code> directly,
     * but instead call the {@link #close()} method.
     * @throws IOException
     */
    protected abstract boolean doWrite(SocketChannel sc) throws IOException;

    /**
     * called whenever reading or writing failed. Subclasses can use this to handle closed
     * connections etc; will be called <em>before</em> the socket is actually closed.
     * @param ex Exception to handle
     */
    protected void onError(IOException ex) {
        // empty
    }
}
