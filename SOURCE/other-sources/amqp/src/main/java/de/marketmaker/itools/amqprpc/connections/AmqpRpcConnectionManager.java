/*
 * AmqpRpcConnectionManager.java
 *
 * Created on 03.03.2011 17:11:19
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.itools.amqprpc.connections;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.rabbitmq.client.AlreadyClosedException;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.ShutdownListener;
import com.rabbitmq.client.ShutdownSignalException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.support.ApplicationObjectSupport;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.remoting.RemoteConnectFailureException;

import de.marketmaker.itools.amqprpc.supervising.AmqpProblemEvent;
import de.marketmaker.itools.amqprpc.supervising.SupervisableAndRepairable;

/**
 * This class holds a single connection to a RabbitMQ server. It implements
 * {@link PersistentAmqpConnectionManager}. It will establish the connection in
 * {@link #afterPropertiesSet()}.
 * <p/>
 * This class can safely be used by several threads, especially several {@link de.marketmaker.itools.amqprpc.AmqpServiceExporter}s
 * and/or {@link de.marketmaker.itools.amqprpc.AmqpProxyFactoryBean}s
 * since each of those will use a channel of their own. {@link #forceReconnect()} is thread-safe.
 * @author Sebastian Wild (s.wild@market-maker.de)
 */
@ManagedResource
public class AmqpRpcConnectionManager extends ApplicationObjectSupport
        implements AutoCloseable, InitializingBean, DisposableBean, PersistentAmqpConnectionManager,
        SupervisableAndRepairable, ShutdownListener {

    private static class AmqpThreadFactory implements ThreadFactory {
        private static final AtomicInteger poolNumber = new AtomicInteger(1);

        private final AtomicInteger threadNumber = new AtomicInteger(1);

        private final String namePrefix;

        private AmqpThreadFactory() {
            this.namePrefix = "amqp-" + poolNumber.getAndIncrement() + "-thread-";
        }

        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, this.namePrefix + this.threadNumber.getAndIncrement());
        }
    }


    private static final int CLOSE_TIMEOUT_IN_MILLIS = 10 * 1000;

    // copied from amqp-client
    private static final int DEFAULT_NUM_THREADS = Runtime.getRuntime().availableProcessors() * 2;

    private int maxNumThreads = DEFAULT_NUM_THREADS;

    private ConnectionFactory connectionFactory;

    private Connection currentConnection;

    private final List<ConnectionListener> listeners = new CopyOnWriteArrayList<>();

    private volatile boolean destroyed = false;

    private ExecutorService executor;

    public void setConnectionFactory(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    public void setMaxNumThreads(int maxNumThreads) {
        this.maxNumThreads = maxNumThreads;
    }

    public void afterPropertiesSet() throws Exception {
        if (this.connectionFactory == null) {
            throw new IllegalArgumentException("connectionFactory may not be null");
        }
        this.executor = new ThreadPoolExecutor(0, this.maxNumThreads,
                60L, TimeUnit.SECONDS,
                new SynchronousQueue<>(), new AmqpThreadFactory());
        forceReconnect();
    }

    public synchronized void addConnectionListener(ConnectionListener listener) {
        listeners.add(listener);
        if (this.isConnectionOpen()) {
            listener.onEstablishedNewConnection(this.currentConnection);
        }
    }

    public void removeConnectionListener(ConnectionListener listener) {
        listeners.remove(listener);
    }

    public synchronized Connection getRunningConnection() throws RemoteConnectFailureException {
        if (!isConnectionOpen()) {
            forceReconnect();
        }
        return this.currentConnection;
    }

    public synchronized void closeCurrentConnection() {
        if (this.currentConnection != null) {
            try {
                this.currentConnection.close(CLOSE_TIMEOUT_IN_MILLIS);
            } catch (AlreadyClosedException e) {
                // ignore
            } catch (Exception e) {
                this.logger.warn("<closeCurrentConnection> Could not close AMQP connection. ", e);
            } finally {
                this.currentConnection = null;
            }
        }
    }

    public void destroy() throws Exception {
        this.destroyed = true;
        closeCurrentConnection();
        this.executor.shutdown();
        this.logger.info("<destroy> finished");
    }

    /**
     * called when the broker connection is closed/lost (when the broker crashed or is
     * being restarted).
     * @param sse
     */
    public void shutdownCompleted(ShutdownSignalException sse) {
        for (ConnectionListener listener : listeners) {
            listener.onConnectionClosed();
        }
        if (!this.destroyed) {
            getApplicationContext().publishEvent(new AmqpProblemEvent(this));
            this.logger.warn("<shutdownCompleted> for connection, cause " + sse
                            + ", reason=" + sse.getReason()
                            + ", ref=" + sse.getReference()
                            + ", hardError=" + sse.isHardError()
                            + ", fromApp=" + sse.isInitiatedByApplication()
            );
        }
    }

    private void connectCompleted() {
        for (ConnectionListener listener : listeners) {
            listener.onEstablishedNewConnection(this.currentConnection);
        }
    }


    @ManagedOperation(description = "close current AMQP connection and reconnect to given host")
    @ManagedOperationParameters({
            @ManagedOperationParameter(name = "host", description = "AMQP server host name")
    })
    public void forceReconnect(String host) throws RemoteConnectFailureException {
        final String hostname = host.trim();
        // in jconsole, string fields are filled with 'String' as default, which makes no sense when submitted
        if (hostname.isEmpty() || "String".equals(hostname)) {
            return;
        }
        if (!getHost().equals(hostname)) {
            setHost(hostname);
        }
        forceReconnect();
    }

    /**
     * Reset the connection to the AMQP Broker, i.e. closes a connection if one exists and then
     * establishes a new one.
     * @throws RemoteConnectFailureException if connection fails
     */
    @ManagedOperation(description = "close current AMQP connection and reconnect to same host")
    public synchronized void forceReconnect() throws RemoteConnectFailureException {
        closeCurrentConnection();

        try {
            this.currentConnection = this.connectionFactory.newConnection(this.executor);
        } catch (Exception e) {
            throw new RemoteConnectFailureException("Could not connect to AMQP broker " +
                    this.connectionFactory.getHost() + ":" + this.connectionFactory.getPort(), e);
        }

        this.currentConnection.addShutdownListener(this);
        connectCompleted();

        this.logger.info("<forceReconnect> successfully established AMQP connection to " +
                this.connectionFactory.getHost());
    }

    @ManagedAttribute
    public String getHost() {
        return this.connectionFactory.getHost();
    }

    @ManagedAttribute
    public void setHost(String host) {
        this.logger.info("<setHost> to '" + host + "'");
        this.connectionFactory.setHost(host);
    }

    /**
     * @return true iff connection to RabbitMQ is configuered and running fine.
     */
    public synchronized boolean isConnectionOpen() {
        return this.currentConnection != null && this.currentConnection.isOpen();
    }


    public boolean everythingOk() {
        return isConnectionOpen();
    }

    public void tryToRecover() {
        forceReconnect();
    }

    public String logMessageInCaseOfError() {
        return "AmqpRpcConnectionManager lost connection.";
    }

    @Override
    public void close() {
        try {
            if (this.isConnectionOpen()) {
                this.destroy();
            }
        } catch (Exception e) {
            // This suppresses compiler warning for InterruptedException
            // and ignores errors on destruction
        }
    }
}
