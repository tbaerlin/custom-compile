/*
 * AbstractIstarRequest.java
 *
 * Created on 02.03.2005 14:21:11
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.common.nioframework;


/**
 * Interface used for establishment a connection using non-blocking
 * operations.
 *
 * Should be implemented by classes wishing to be notified
 * when a Socket finishes connecting to a remote point.
 * @author Oliver Flege
 */
public interface ConnectorSelectorHandler extends SelectorHandler {
    /**
     * Called by SelectorThread when the socket associated with the
     * class implementing this interface finishes establishing a
     * connection.
     */
    public void handleConnect();
}
