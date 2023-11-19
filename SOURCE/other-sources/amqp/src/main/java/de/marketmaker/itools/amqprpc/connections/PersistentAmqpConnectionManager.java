/*
 * PersistentAmqpConnection.java
 *
 * Created on 03.03.2011 17:29:19
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.itools.amqprpc.connections;

import com.rabbitmq.client.Connection;
import de.marketmaker.itools.amqprpc.supervising.SupervisableAndRepairable;
import org.springframework.remoting.RemoteConnectFailureException;

/**
 * Interface that specifies methods to control a persistent connection to an AMQP broker.
 * This interface can be used by a scheduled maintenance bean to monitor connectivity.
 * Implementations of this class must call the handlers in {@link ConnectionListener}
 * when connections are opened and closed.
 *
 * @author Sebastian Wild (s.wild@market-maker.de)
 */
public interface PersistentAmqpConnectionManager extends SupervisableAndRepairable {

    /**
     * This methods closes any current connections and opens a new one.
     *
     * @throws RemoteConnectFailureException if attempt to open connection failed
     */
    void forceReconnect() throws RemoteConnectFailureException;

    /**
     * @return true iff there is an active connection
     */
    boolean isConnectionOpen();

    /**
     * @return a {@link com.rabbitmq.client.Connection} to a RabbitMQ broker. If none was open,
     *         tries to open a new one.
     * @throws RemoteConnectFailureException if there was no current connection and the attempt
     *                                       to open a new one failed
     */
    Connection getRunningConnection() throws RemoteConnectFailureException;

    /**
     * Add listener that gets notified whenever a new connection was established and when a
     * connection is about to be closed.
     * <p/>
     * <p/>
     * <b>On the newly added listener, {@link ConnectionListener#onEstablishedNewConnection(com.rabbitmq.client.Connection)}
     * will be called immediately with the current connection, if an open connection exists.</b>
     *
     * @param listener the listener to register
     */
    void addConnectionListener(ConnectionListener listener);

    void removeConnectionListener(ConnectionListener listener);


    /**
     * Close any open connection, e.g. to make AMQP connection threads terminate.
     * Implementations should still allow to establish new connections via
     * {@link #forceReconnect()} after this method has been called.
     */
    void closeCurrentConnection();

}
