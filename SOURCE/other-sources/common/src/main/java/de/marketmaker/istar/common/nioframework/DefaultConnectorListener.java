/*
 * AbstractConnectorListener.java
 *
 * Created on 03.03.2006 16:05:34
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.common.nioframework;

import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

import java.nio.channels.SocketChannel;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.jcip.annotations.Immutable;

/**
 * A ConnectorListener that can be used for exactly one connection attempt. Client can use
 * the {@link #isSuccessfulConnect()} method to determine whether that attempt succeeded
 * or not. If this object is reused for another connection attempt, the results of invoking
 * that method are undefined.
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
@Immutable
public final class DefaultConnectorListener implements ConnectorListener, CallbackErrorHandler {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private final ConnectionHandler handler;

    private final CountDownLatch connectLatch = new CountDownLatch(1);

    private final AtomicBoolean successfulConnect = new AtomicBoolean();

    public DefaultConnectorListener(ConnectionHandler handler) {
        this.handler = handler;
    }

    public void handleError(String message, Exception ex) {
        if (handler instanceof CallbackErrorHandler) {
            ((CallbackErrorHandler) handler).handleError(message, ex);
        }
        else {
            this.logger.error(message, ex);
        }
        this.successfulConnect.set(false);
        this.connectLatch.countDown();
    }

    public void connectionEstablished(SocketChannel sc) throws IOException {
        this.handler.handleConnection(sc);
        this.successfulConnect.set(true);
        this.connectLatch.countDown();
    }

    @Override
    public void connectionFailed(InetSocketAddress remoteAddress, IOException ex) {
        if (ex instanceof ConnectException) {
            handleError("finishConnect failed for " + remoteAddress + ": " + ex.getMessage(), null);
        }
        else {
            handleError("finishConnect failed for " + remoteAddress, ex);
        }
    }

    /**
     * Can be used to detect whether the connect succeeded. This call should block until
     * an authoritative answer is available, that is either {@link #connectionEstablished}
     * or {@link CallbackErrorHandler#handleError(String, Exception)} has been called. This method can not be used to
     * detect whether the connection is still alive.
     * @return true if connect succeeded.
     */
    public boolean isSuccessfulConnect() {
        ensureNotSelectorThread();
        try {
            this.connectLatch.await();
        } catch (InterruptedException e) {
            // never happens, ignore
        }
        return successfulConnect.get();
    }

    /**
     * Can be used to detect whether the connect succeeded. This call should block until
     * an authoritative answer is available (i.e., either {@link #connectionEstablished}
     * or {@link CallbackErrorHandler#handleError(String, Exception)} has been called), OR the timeout elapsed.
     * @return true if connect succeeded.
     * @throws TimeoutException if timeout elapsed
     */
    public boolean isSuccessfulConnect(long timeout, TimeUnit unit) throws TimeoutException {
        ensureNotSelectorThread();
        try {
            if (!this.connectLatch.await(timeout, unit)) {
                throw new TimeoutException();
            }
        } catch (InterruptedException e) {
            // never happens, ignore
        }
        return successfulConnect.get();
    }

    private void ensureNotSelectorThread() {
        this.handler.getSelectorThread().checkNotSelectorThread();
    }
}
