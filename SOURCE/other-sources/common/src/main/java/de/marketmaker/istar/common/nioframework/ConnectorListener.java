/*
 * AbstractIstarRequest.java
 *
 * Created on 02.03.2005 14:21:11
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.common.nioframework;


import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Callback interface for receiving events from a Connector.
 * @author Oliver Flege
 */
public interface ConnectorListener {
    /**
     * Called when the connection is fully established.
     * @param sc The newly connected socket.
     */
    public void connectionEstablished(SocketChannel sc) throws IOException;

    public void connectionFailed(InetSocketAddress remoteAddress, IOException ioe);
}
