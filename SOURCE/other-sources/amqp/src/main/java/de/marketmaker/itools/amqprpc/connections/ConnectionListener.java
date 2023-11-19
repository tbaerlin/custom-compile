/*
 * ConnectionListener.java
 *
 * Created on 03.03.2011 17:31:10
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.itools.amqprpc.connections;

import com.rabbitmq.client.Connection;

/**
 * Listener interface for classes that need to do something whenever a {@link PersistentAmqpConnectionManager}
 * opens or closes a connection.
 *
 * @author Sebastian Wild (s.wild@market-maker.de)
 */
public interface ConnectionListener {

    /**
     * This method is called right after a new connection was established.
     * {@code newConnection} can be assumed to be a connection that was successfully opened.
     *
     * @param newConnection the currently opened connection
     */
    void onEstablishedNewConnection(Connection newConnection);

    /**
     * This method is called just after the current connection was closed.
     * Can be used to execute cleanup code.
     */
    void onConnectionClosed();
}
