/*
 * AmqpProxy.java
 *
 * Created on 07.03.2011 16:36:04
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.itools.amqprpc.impl;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.remoting.RemoteAccessException;
import org.springframework.remoting.RemoteConnectFailureException;
import org.springframework.remoting.support.RemoteInvocation;
import org.springframework.remoting.support.RemoteInvocationBasedAccessor;
import org.springframework.remoting.support.RemoteInvocationResult;

import de.marketmaker.istar.common.amqp.ServiceProviderSelection;
import de.marketmaker.itools.amqprpc.CommunicationPattern;
import de.marketmaker.itools.amqprpc.connections.AmqpRpcAddress;
import de.marketmaker.itools.amqprpc.connections.AmqpRpcAddressImpl;
import de.marketmaker.itools.amqprpc.connections.PersistentAmqpConnectionManager;
import de.marketmaker.itools.amqprpc.monitoring.AmqpMonitor;
import de.marketmaker.itools.amqprpc.monitoring.MonitorSupport;
import de.marketmaker.itools.amqprpc.supervising.Supervisable;
import de.marketmaker.itools.amqprpc.supervising.SupervisableAndRepairable;
import de.marketmaker.itools.amqprpc.supervising.Supervisor;

import static de.marketmaker.istar.common.amqp.ServiceProviderSelection.ID_FOR_NEXT_SEND;
import static de.marketmaker.istar.common.amqp.ServiceProviderSelection.ID_FROM_LAST_REPLY;
import static java.util.concurrent.TimeUnit.*;

/**
 * Method interceptor ({@link org.aopalliance.intercept.MethodInterceptor}) for remoting a
 * method call via AMQP. Can be configured as bean via {@link de.marketmaker.itools.amqprpc.AmqpProxyFactoryBean}.
 * <p/>
 * Must be provided with a {@link de.marketmaker.itools.amqprpc.connections.PersistentAmqpConnectionManager}
 * that connects to a RabbitMQ broker.
 * <p/>
 * Note that the service interface's parameter and return types must be Serializable for
 * remoting to work.
 * @author Sebastian Wild (s.wild@market-maker.de)
 */
public class AmqpRemotingMethodInterceptor extends RemoteInvocationBasedAccessor implements
        MethodInterceptor, SupervisableAndRepairable, InitializingBean, DisposableBean {

    private AmqpRpcClientConnectionListener amqpRpcClientConnectionListener;

    private PersistentAmqpConnectionManager connectionManager;

    private AmqpRpcAddress address;

    private AmqpRpcClient.Settings rpcClientSettings = new AmqpRpcClient.Settings();

    private volatile boolean logRequests = false;

    private long slowInvocationThreshold = 3000L;

    private int rpcTimeout = 10000;

    private final Log requestLogger = LogFactory.getLog("amqp-rpc.logger");

    private Set<String> rawByteArrayReplyMethods = new HashSet<String>();

    private CommunicationPattern communicationPattern = CommunicationPattern.POINT_TO_POINT_RPC;

    private Supervisor supervisor;

    private String preferredProvider;

    private volatile String preferredProviderId;

    private AmqpMonitor monitor;

    public void setAddress(AmqpRpcAddress address) {
        this.address = address;
    }

    public boolean isLogRequests() {
        return logRequests;
    }

    public void setLogRequests(boolean logRequests) {
        this.logRequests = logRequests;
    }

    /**
     * Time in milliseconds before a warning is issued to log
     * @param slowInvocationThreshold
     */
    public void setSlowInvocationThreshold(long slowInvocationThreshold) {
        this.slowInvocationThreshold = slowInvocationThreshold;
    }

    /**
     * timeout in milliseconds to wait for answer of RPC server. After that delay, a
     * {@link org.springframework.remoting.RemoteAccessException} is thrown.
     * @param rpcTimeout
     */
    public void setRpcTimeout(int rpcTimeout) {
        this.rpcTimeout = rpcTimeout;
    }

    public void setConnectionManager(PersistentAmqpConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    public AmqpRpcClient.Settings getRpcClientSettings() {
        return this.rpcClientSettings;
    }

    public void setRpcClientSettings(AmqpRpcClient.Settings rpcClientSettings) {
        this.rpcClientSettings = rpcClientSettings;
    }

    public void setRawByteArrayReplyMethods(Set<String> rawByteArrayReplyMethods) {
        this.rawByteArrayReplyMethods = rawByteArrayReplyMethods;
    }

    public void setCommunicationPattern(CommunicationPattern communicationPattern) {
        this.communicationPattern = communicationPattern;
    }

    public void setPreferredProvider(String preferredProvider) {
        this.preferredProvider = preferredProvider;
    }

    @Override
    public void afterPropertiesSet() {
        // have to set url to something to satisfy UrlBasedRemoteAccessor ...
        setServiceUrl("rabbitMQ");
        super.afterPropertiesSet();

        if (this.connectionManager == null) {
            throw new IllegalArgumentException("connectionManager may not be null");
        }
        // default address settings
        if (this.address == null) {
            setAddress(AmqpRpcAddressImpl.createDefault(getServiceInterface()));
        }
        this.amqpRpcClientConnectionListener = new AmqpRpcClientConnectionListener(this.address, this.rpcClientSettings);
        // settings the listener will eventually result in call to onEstablishedNewConnection
        this.connectionManager.addConnectionListener(this.amqpRpcClientConnectionListener);

        this.logger.info("<afterPropertiesSet> set up AmqpServiceProxy for Service " +
                getServiceInterface().getCanonicalName() + " on AMQP-address " + this.address);
        if (!this.rawByteArrayReplyMethods.isEmpty()) {
            this.logger.info("<afterPropertiesSet> Omitting reply serialization for the following " +
                    "method names: " + this.rawByteArrayReplyMethods);
        }

        if (this.supervisor != null) {
            supervisor.addSupervisedObject(this);
        }

        this.monitor = MonitorSupport.getExportMonitor(getServiceInterface(), this.address);
    }

    public void destroy() throws Exception {
        if (this.supervisor != null) {
            this.supervisor.removeSupervisedObject(this);
        }
        this.connectionManager.removeConnectionListener(this.amqpRpcClientConnectionListener);
        amqpRpcClientConnectionListener.shutdownRpcClient(true);
    }

    public Object invoke(MethodInvocation methodInvocation) throws Throwable {
        if (AopUtils.isToStringMethod(methodInvocation.getMethod())) {
            return "AMQP invoker proxy for service " + getServiceInterface().getCanonicalName();
        }
        if (isSupervisableAndRepairableMethod(methodInvocation)) {
            return methodInvocation.getMethod().invoke(this, methodInvocation.getArguments());
        }
        return invokeService(methodInvocation);
    }

    private boolean isSupervisableAndRepairableMethod(MethodInvocation methodInvocation) {
        final Class<?> aClass = methodInvocation.getMethod().getDeclaringClass();
        return aClass == Supervisable.class || aClass == SupervisableAndRepairable.class;
    }

    private Object invokeService(MethodInvocation methodInvocation) throws Throwable {
        if (this.logRequests) {
            this.requestLogger.info(methodInvocation + ", " + getArgStr(methodInvocation));
        }

        final long then = System.nanoTime();
        final int[] numBytes = new int[2];
        Object result = null;
        try {
            result = doInvoke(methodInvocation, numBytes);
            return result;
        } finally {
            final long took = System.nanoTime() - then;
            afterInvokeService(methodInvocation, numBytes, result, took);
        }
    }

    private void afterInvokeService(MethodInvocation methodInvocation, int[] numBytes,
            Object result, long took) {
        this.monitor.ack(numBytes[0], numBytes[1]);
        if (this.logger.isDebugEnabled()) {
            long µs = MICROSECONDS.convert(took, NANOSECONDS);
            this.logger.debug("<rpc> " + methodInvocation
                    + ", arguments=" + getArgStr(methodInvocation)
                    + ", result=" + result
                    + ", b/in=" + numBytes[0] + ", b/out=" + numBytes[1]
                    + " took " + µs + "µs");
            return;
        }
        final long ms = MILLISECONDS.convert(took, NANOSECONDS);
        if (ms > this.slowInvocationThreshold) {
            this.logger.warn("<rpc> slow: " + methodInvocation
                    + ", arguments=" + getArgStr(methodInvocation)
                    + ", result=" + result
                    + " took " + TimeUtil.formatMillis(ms));
        }
    }

    protected Object doInvoke(MethodInvocation methodInvocation,
            int[] numBytes) throws RemoteAccessException {
        try {
            final byte[] requestBody = marshalRequest(methodInvocation);
            numBytes[0] = requestBody.length;

            if (this.communicationPattern.isExpectingAnswer()) {
                applyProviderPreference();

                final byte[] resultBody
                        = getRpcClient().sendAndWaitForReply(requestBody, this.rpcTimeout);
                numBytes[1] = resultBody.length;

                ackIdFromLastReply();
                return unmarshalResult(methodInvocation, resultBody);
            }
            else {
                getRpcClient().send(requestBody);
                return null;
            }
        } catch (RemoteAccessException e) {
            throw e;
        } catch (Throwable t) {
            throw new RemoteAccessException(methodInvocation
                    + ", arguments=" + getArgStr(methodInvocation) + " failed ON REMOTE HOST "
                    + ID_FROM_LAST_REPLY.get(), t);
        }
    }

    private void applyProviderPreference() {
        String providerId = this.preferredProviderId;
        if (providerId != null && ID_FOR_NEXT_SEND.get() == null) {
            ID_FOR_NEXT_SEND.set(providerId);
        }
    }
    private void ackIdFromLastReply() {
        if (this.preferredProvider == null) {
            return;
        }
        String lastId = ServiceProviderSelection.ID_FROM_LAST_REPLY.get();
        String currentId = this.preferredProviderId;
        if (lastId.equals(currentId)) {
            return;
        }
        if (lastId.contains(this.preferredProvider)) {
            this.logger.info("<ackIdFromLastReply> preferredProviderId = " + lastId);
            this.preferredProviderId = lastId;
        }
        // received a reply from a non-preferred provider, don't care
    }

    private AmqpRpcClient getRpcClient() {
        final AmqpRpcClient result = this.amqpRpcClientConnectionListener.getRpcClient().get();
        if (result != null) {
            return result;
        }
        throw new RemoteConnectFailureException("Service Proxy not connected to broker for "
                + this.address, null);
    }

    protected byte[] marshalRequest(MethodInvocation methodInvocation) {
        RemoteInvocation invocation = createRemoteInvocation(methodInvocation);
        return SerializationUtils.serialize(invocation);
    }

    protected Object unmarshalResult(MethodInvocation methodInvocation, byte[] result)
            throws Throwable {
        if (isWithRawByteReply(methodInvocation)) {
            return result;
        }
        else {
            final RemoteInvocationResult invocationResult =
                    (RemoteInvocationResult) SerializationUtils.deserialize(result);
            return recreateRemoteInvocationResult(invocationResult);
        }
    }

    private boolean isWithRawByteReply(MethodInvocation methodInvocation) {
        return this.rawByteArrayReplyMethods.contains(methodInvocation.getMethod().getName());
    }

    /**
     * Needed to inverse wiring, since this bean cannot be used as a bean (FactoryBean!)
     * @param supervisor
     */
    public void setSupervisor(Supervisor supervisor) {
        this.supervisor = supervisor;
    }

    private String getArgStr(MethodInvocation methodInvocation) {
        return Arrays.deepToString(methodInvocation.getArguments());
    }

    public boolean everythingOk() {
        final AmqpRpcClient client = this.amqpRpcClientConnectionListener.getRpcClient().get();
        return this.connectionManager.everythingOk() && client != null && client.everythingOk();
    }

    public void tryToRecover() {
        if (connectionManager.everythingOk()) {
            amqpRpcClientConnectionListener.shutdownRpcClient(true);
            amqpRpcClientConnectionListener.forceCreateNewRpcClient(connectionManager.getRunningConnection());
        }
        else {
            connectionManager.tryToRecover();
        }
    }

    public String logMessageInCaseOfError() {
        return "AmqpProxyFactoryBean for " + getServiceInterface().getCanonicalName() + " lost connection.";
    }
}
