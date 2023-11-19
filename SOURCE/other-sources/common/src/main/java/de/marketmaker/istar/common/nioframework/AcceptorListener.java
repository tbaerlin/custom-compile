/*
 * AbstractIstarRequest.java
 *
 * Created on 02.03.2005 14:21:11
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.common.nioframework;


import java.nio.channels.SocketChannel;
import java.io.IOException;

/**
 * Callback interface for receiving events from an Acceptor.
 * @author Oliver Flege
 */
public interface AcceptorListener {
    /**
     * Called when a connection is established. If the acceptor is limited on the number
     * of connected clients, this listener is expected to call
     * {@link de.marketmaker.istar.common.nioframework.Acceptor#handleDisconnect()}
     * whenever it closes the <code>sc</code>.
     *
     * @param acceptor The acceptor that originated this event.
     * @param sc The newly connected socket.
     * @throws IOException if thrown, the acceptor takes all steps necessary
     * to deregister sc with the selectorThread and to close sc. Otherwise, these tasks
     * are up to the listener as soon as it no longer needs the connection.
     */
    void socketConnected(Acceptor acceptor, SocketChannel sc) throws IOException;
}
