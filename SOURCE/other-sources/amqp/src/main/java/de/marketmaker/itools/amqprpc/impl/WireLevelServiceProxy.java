/*
 * WireLevelServiceProxy.java
 *
 * Created on 11.03.2011 17:53:36
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.itools.amqprpc.impl;

import com.rabbitmq.client.ShutdownSignalException;
import org.springframework.remoting.RemoteAccessException;

/**
 * This interface represents wire-level remote method calls (client side)
 * @author Sebastian Wild (s.wild@market-maker.de)
 */
public interface WireLevelServiceProxy {

    /**
     * Performs a simple byte-array-based RPC roundtrip.
     *
     * @param request the byte[] request to send
     * @param timeout number of milliseconds to wait for answer
     * @return the byte array response received
     * @throws RemoteAccessTimeoutException if timeout expires
     * @throws com.rabbitmq.client.ShutdownSignalException
     *                                      if the connection dies during our wait
     */
    byte[] sendAndWaitForReply(byte[] request, int timeout)
            throws RemoteAccessException, ShutdownSignalException;
}
