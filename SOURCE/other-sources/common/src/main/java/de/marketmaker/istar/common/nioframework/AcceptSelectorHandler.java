/*
 * AbstractIstarRequest.java
 *
 * Created on 02.03.2005 14:21:11
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.common.nioframework;


/**
 * Interface used for accepting incoming connections using non-blocking
 * operations.
 *
 * Classes wishing to be notified when a ServerSocket receives incoming
 * connections should implement this interface.
 * @author Oliver Flege
 */
public interface AcceptSelectorHandler extends SelectorHandler {
    /**
     * Called by SelectorThread when the server socket associated
     * with the class implementing this interface receives a request
     * for establishing a connection.
     */
    public void handleAccept();
}
