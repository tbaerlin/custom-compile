package de.marketmaker.itools.amqprpc.impl;

import java.util.concurrent.atomic.AtomicReference;

import com.rabbitmq.client.Connection;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.marketmaker.itools.amqprpc.connections.AmqpRpcAddress;
import de.marketmaker.itools.amqprpc.connections.ConnectionListener;

public class AmqpRpcClientConnectionListener implements ConnectionListener {

    protected final Log logger = LogFactory.getLog(getClass());

    private AmqpRpcAddress address;

    private AmqpRpcClient.Settings rpcClientSettings;

    private final AtomicReference<AmqpRpcClient> rpcClient = new AtomicReference<AmqpRpcClient>(null);

    public AmqpRpcClientConnectionListener(AmqpRpcAddress address, AmqpRpcClient.Settings rpcClientSettings) {
        this.address = address;
        this.rpcClientSettings = rpcClientSettings;
    }

    public AtomicReference<AmqpRpcClient> getRpcClient() {
        return rpcClient;
    }

    public void onConnectionClosed() {
        shutdownRpcClient(false);
    }

    public void onEstablishedNewConnection(Connection newConnection) {
        forceCreateNewRpcClient(newConnection);
    }

    /**
     * Shuts down any running rpcServer.
     */
    public void shutdownRpcClient(boolean closeChannel) {
        AmqpRpcClient current = this.rpcClient.getAndSet(null);
        if (current != null && closeChannel) {
            current.closeChannel();
        }
    }

    public void forceCreateNewRpcClient(Connection newConnection) {
        try {
            AmqpRpcClient previous =
                    this.rpcClient.getAndSet(
                            new AmqpRpcClient(newConnection, address, this.rpcClientSettings)
                    );
            if (previous != null) {
                previous.closeChannel();
            }
        } catch (Exception e) {
            logger.warn("<forceCreateNewRpcClient> Could not create RPC client.", e);
        }
    }
}