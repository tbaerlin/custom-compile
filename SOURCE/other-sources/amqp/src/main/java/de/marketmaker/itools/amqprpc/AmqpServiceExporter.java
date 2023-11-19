/*
 * AmqpServiceExporter.java
 *
 * Created on 02.03.2011 17:09:12
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.itools.amqprpc;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.SerializationException;
import org.apache.commons.lang3.SerializationUtils;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.remoting.support.RemoteInvocation;
import org.springframework.remoting.support.RemoteInvocationBasedExporter;
import org.springframework.remoting.support.RemoteInvocationResult;

import de.marketmaker.istar.common.amqp.ForwardRequestException;
import de.marketmaker.itools.amqprpc.connections.AmqpRpcAddress;
import de.marketmaker.itools.amqprpc.connections.AmqpRpcAddressImpl;
import de.marketmaker.itools.amqprpc.connections.PersistentAmqpConnectionManager;
import de.marketmaker.itools.amqprpc.impl.AmqpRpcServer;
import de.marketmaker.itools.amqprpc.impl.AmqpRpcServerConnectionListener;
import de.marketmaker.itools.amqprpc.impl.WireLevelServiceProvider;
import de.marketmaker.itools.amqprpc.monitoring.AmqpMonitor;
import de.marketmaker.itools.amqprpc.monitoring.MonitorSupport;
import de.marketmaker.itools.amqprpc.supervising.SupervisableAndRepairable;
import de.marketmaker.itools.amqprpc.supervising.Supervisor;

/**
 * This class can be used to export a {@link #service}, i.e. a provider of {@link #serviceInterface}
 * as an AMQP remote RPC service. To do so, this class uses a
 * {@link de.marketmaker.itools.amqprpc.connections.PersistentAmqpConnectionManager}
 * for creating a channel of its own. It registers itself as listener in {@link #connectionManager}
 * and starts an {@link de.marketmaker.itools.amqprpc.impl.AmqpRpcServer} on creation of a connection.
 * <p/>
 * Note that the service interface's parameter and return types must be Serializable.
 *
 * @author Sebastian Wild (s.wild@market-maker.de)
 */
public class AmqpServiceExporter extends RemoteInvocationBasedExporter implements
        WireLevelServiceProvider, SupervisableAndRepairable,
        InitializingBean, DisposableBean {

    private PersistentAmqpConnectionManager connectionManager;

    private AmqpRpcServerConnectionListener amqpRpcServerConnectionListener;

    private AmqpRpcAddress address;

    private AmqpRpcServer.Settings rpcServerSettings = new AmqpRpcServer.Settings();

    private Set<String> rawByteArrayReplyMethods = new HashSet<>();

    private Supervisor supervisor;

    private AmqpMonitor monitor;

    public void setConnectionManager(PersistentAmqpConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    public void setRpcServerSettings(AmqpRpcServer.Settings rpcServerSettings) {
        this.rpcServerSettings = rpcServerSettings;
    }

    public AmqpRpcServer.Settings getRpcServerSettings() {
        return this.rpcServerSettings;
    }

    public void setAddress(AmqpRpcAddress address) {
        this.address = address;
    }

    /**
     * This property allows to specify method names whose return value is a raw byte[] anyway,
     * and hence (de)serialization can be omitted. While this may safe computation time and memory
     * for large responses, it has the drawback that exceptions thrown on server side cannot be
     * propagated to the client. Instead, null is returned in such cases.
     * <p/>
     * <p/>
     * <b>Make sure to configure this property the same on both server and client side; otherwise you
     * will most probably get into trouble.</b>
     * <p/>
     * <p/>
     * Note that you can only specify this settings on the level of <b>method names</b>, so you
     * cannot disable serialization for an overloaded methods, when another overload requires
     * serialization.
     *
     * @param rawByteArrayReplyMethods the methods to exclude from reply serialization
     */
    public void setRawByteArrayReplyMethods(Set<String> rawByteArrayReplyMethods) {
        this.rawByteArrayReplyMethods = rawByteArrayReplyMethods;
    }

    public void afterPropertiesSet() throws Exception {
        if (this.connectionManager == null) {
            throw new IllegalArgumentException("connectionManager may not be null");
        }
        // default address settings in case none was provided
        if (this.address == null) {
            setAddress(AmqpRpcAddressImpl.createDefault(getServiceInterface()));
        }

        this.amqpRpcServerConnectionListener = new AmqpRpcServerConnectionListener(this.address, this.rpcServerSettings, this);

        // settings the listener will eventually result in call to onEstablishedNewConnection
        this.connectionManager.addConnectionListener(this.amqpRpcServerConnectionListener);

        this.logger.info("<afterPropertiesSet> set up AmqpServiceExporter for Service " +
                getServiceInterface().getCanonicalName() + " on AMQP-address " + this.address);
        if (!this.rawByteArrayReplyMethods.isEmpty()) {
            this.logger.info("<afterPropertiesSet> Omitting reply serialization for the following " +
                    "method names: " + this.rawByteArrayReplyMethods);
        }
        if (this.supervisor != null) {
            supervisor.addSupervisedObject(this);
        }
        this.monitor = MonitorSupport.getExportMonitor(getService().getClass(), this.address);
    }

    public void destroy() throws Exception {
        if (this.supervisor != null) {
            this.supervisor.removeSupervisedObject(this);
        }
        this.connectionManager.removeConnectionListener(this.amqpRpcServerConnectionListener);
        this.amqpRpcServerConnectionListener.shutdownRpcServer();
    }

    public byte[] call(byte[] requestBody) {
        RemoteInvocation invocation = unmarshalRequest(requestBody);
        RemoteInvocationResult result = invokeAndCreateResult(invocation, getService());
        final byte[] response = marshalResponse(invocation, result);
        this.monitor.ack(response != null ? response.length : 0, requestBody.length);
        return response;
    }

    protected RemoteInvocationResult invokeAndCreateResult(RemoteInvocation invocation,
                                                           Object targetObject) {
        try {
            Object value = invoke(invocation, targetObject);
            return new RemoteInvocationResult(value);
        } catch (InvocationTargetException ite) {
            if (ite.getCause() instanceof ForwardRequestException) {
                throw (ForwardRequestException) ite.getCause();
            }
            return new RemoteInvocationResult(ite);
        } catch (Throwable ex) {
            return new RemoteInvocationResult(ex);
        }
    }

    public boolean hasMoreMessages() throws IOException {
        final AmqpRpcServer server = this.amqpRpcServerConnectionListener.getRpcServer().get();
        return (server != null) && (server.getMessageCount() > 0);
    }

    public int getMessageCount() throws IOException {
        final AmqpRpcServer server = this.amqpRpcServerConnectionListener.getRpcServer().get();
        if (server != null) {
            return server.getMessageCount();
        } else {
            return 0;
        }
    }

    protected byte[] marshalResponse(RemoteInvocation invocation, RemoteInvocationResult result) {
        if (this.rawByteArrayReplyMethods.contains(invocation.getMethodName())) {
            return result.hasException() ? null : (byte[]) result.getValue();
        } else {
            return SerializationUtils.serialize(result);
        }
    }

    protected RemoteInvocation unmarshalRequest(byte[] requestBody) {
        try {
            return (RemoteInvocation) SerializationUtils.deserialize(requestBody);
        } catch (SerializationException se) {
            throw se;
        } catch (Throwable t) {
            throw new SerializationException(t);
        }
    }

    /**
     * Inverse wiring for use as prototype bean.
     *
     * @param supervisor
     */
    public void setSupervisor(Supervisor supervisor) {
        this.supervisor = supervisor;
    }

    public boolean everythingOk() {
        if (!this.connectionManager.everythingOk()) {
            return false;
        }
        final AmqpRpcServer server = this.amqpRpcServerConnectionListener.getRpcServer().get();
        return (server != null) && server.everythingOk();
    }

    public void tryToRecover() {
        if (connectionManager.everythingOk()) {
            this.amqpRpcServerConnectionListener.shutdownRpcServer();
            this.amqpRpcServerConnectionListener.forceCreateNewRpcServer(connectionManager.getRunningConnection());
        } else {
            connectionManager.tryToRecover();
        }
    }

    public String logMessageInCaseOfError() {
        return toString() + " not running.";
    }

    public String toString() {
        return getClass().getSimpleName() + " for " + getServiceInterface().getCanonicalName();
    }
}
