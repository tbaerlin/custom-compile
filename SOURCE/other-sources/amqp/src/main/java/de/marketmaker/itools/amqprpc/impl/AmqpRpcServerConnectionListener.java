package de.marketmaker.itools.amqprpc.impl;

import java.util.concurrent.atomic.AtomicReference;

import com.rabbitmq.client.Connection;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.marketmaker.itools.amqprpc.connections.AmqpRpcAddress;
import de.marketmaker.itools.amqprpc.connections.ConnectionListener;

public class AmqpRpcServerConnectionListener implements ConnectionListener {

    protected final Log logger = LogFactory.getLog(getClass());

    private AmqpRpcAddress address;

    private AmqpRpcServer.Settings settings;

    private final AtomicReference<AmqpRpcServer> rpcServer = new AtomicReference<>(null);

    private WireLevelServiceProvider wireLevelServiceProvider;

    public AmqpRpcServerConnectionListener(AmqpRpcAddress address, AmqpRpcServer.Settings settings, WireLevelServiceProvider wireLevelServiceProvider) {
        this.address = address;
        this.settings = settings;
        this.wireLevelServiceProvider = wireLevelServiceProvider;
    }

    public AtomicReference<AmqpRpcServer> getRpcServer() {
        return rpcServer;
    }

    public void onEstablishedNewConnection(Connection newConnection) {
        forceCreateNewRpcServer(newConnection);
    }

    public void onConnectionClosed() {
        shutdownRpcServer();
    }

    /**
     * Shuts down any running rpcServer. Save to call when already closed.
     */
    public void shutdownRpcServer() {
        this.logger.info("<shutdownRpcServer> " + toString());
        AmqpRpcServer current = this.rpcServer.getAndSet(null);
        if (current != null) {
            current.closeChannel();
        }
    }

    public void forceCreateNewRpcServer(Connection newConnection) {
        try {
            AmqpRpcServer current = this.rpcServer.getAndSet(
                    new AmqpRpcServer(newConnection, this.wireLevelServiceProvider, this.address, this.settings)
            );
            this.logger.info("<forceCreateNewRpcServer> " + this.toString());
            if (current != null) {
                current.closeChannel();
            }
        } catch (Exception e) {
            this.logger.warn("<forceCreateNewRpcServer> Could not create RPC server.", e);
        }
    }
}
