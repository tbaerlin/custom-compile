/*
 * AbstractIstarRequest.java
 *
 * Created on 02.03.2005 14:21:11
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.common.nioframework;


/**
 * Interface used for reading and writing from a socket using
 * non-blocking operations.
 *
 * Classes wishing to be notified when a socket is ready to be written
 * or read should implement this interface in order to receive
 * notifications.
 * @author Oliver Flege
 */
public interface ReadWriteSelectorHandler extends SelectorHandler {

    /**
     * Called when the associated socket is ready to be read.
     */
    public void handleRead();

    /**
     * Called when the associated socket is ready to be written.
     */
    public void handleWrite();
}