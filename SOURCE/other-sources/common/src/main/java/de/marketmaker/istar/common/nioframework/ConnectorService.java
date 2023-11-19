/*
 * ConnectorService.java
 *
 * Created on 03.03.2006 15:54:39
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.common.nioframework;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public interface ConnectorService {
    /**
     * Connects the listener to a non-blocking SocketChannel. If connecting fails, the listener's
     * {@link CallbackErrorHandler#handleError(String, Exception)} method will be called. The connect itself
     * is also non-blocking, therefore the listener might still be unconnected when this
     * method returns.
     * @param listener
     */
    void connect(ConnectorListener listener);
}
