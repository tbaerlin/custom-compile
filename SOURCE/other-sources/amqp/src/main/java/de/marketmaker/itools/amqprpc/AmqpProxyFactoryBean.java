/*
 * AmqpProxyFactoryBean.java
 *
 * Created on 02.03.2011 17:09:31
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.itools.amqprpc;

import de.marketmaker.itools.amqprpc.connections.AmqpRpcAddress;
import de.marketmaker.itools.amqprpc.connections.PersistentAmqpConnectionManager;
import de.marketmaker.itools.amqprpc.impl.AmqpRemotingMethodInterceptor;
import de.marketmaker.itools.amqprpc.impl.AmqpRpcClient;
import de.marketmaker.itools.amqprpc.supervising.SupervisableAndRepairable;
import de.marketmaker.itools.amqprpc.supervising.Supervisor;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;

import java.util.Set;

/**
 * Factory bean for AMQP RPC proxies. Behaves like the proxied service when
 * used as bean reference, exposing the specified service interface
 * {@code serviceInterface}.
 * <p/>
 * Must be provided with a {@link de.marketmaker.itools.amqprpc.connections.PersistentAmqpConnectionManager}
 * that connects to a RabbitMQ broker.
 * <p/>
 * Note that the service interface's parameter and return types must be Serializable.
 *
 * @author Sebastian Wild (s.wild@market-maker.de)
 */
@ManagedResource
public class AmqpProxyFactoryBean implements FactoryBean<Object>, InitializingBean {

    /**
     * cache singleton
     */
    private volatile Object serviceProxy;

    private AmqpRemotingMethodInterceptor interceptor = new AmqpRemotingMethodInterceptor();

    public void setAddress(AmqpRpcAddress address) {
        interceptor.setAddress(address);
    }

    public void setConnectionManager(PersistentAmqpConnectionManager connectionManager) {
        interceptor.setConnectionManager(connectionManager);
    }

    @ManagedAttribute
    public void setLogRequests(boolean logRequests) {
        interceptor.setLogRequests(logRequests);
    }

    @ManagedAttribute
    public boolean isLogRequests() {
        return interceptor.isLogRequests();
    }

    public void setPreferredProvider(String provider) {
        interceptor.setPreferredProvider(provider);
    }

    public void setRpcTimeout(int rpcTimeout) {
        interceptor.setRpcTimeout(rpcTimeout);
    }

    public void setServiceInterface(Class<?> serviceInterface) {
        interceptor.setServiceInterface(serviceInterface);
    }

    public void setSlowInvocationThreshold(long slowInvocationThreshold) {
        interceptor.setSlowInvocationThreshold(slowInvocationThreshold);
    }

    public void setSupervisor(Supervisor supervisor) {
        interceptor.setSupervisor(supervisor);
    }

    public void setRpcClientSettings(AmqpRpcClient.Settings rpcClientSettings) {
        interceptor.setRpcClientSettings(rpcClientSettings);
    }

    public AmqpRpcClient.Settings getRpcClientSettings() {
        return interceptor.getRpcClientSettings();
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
        interceptor.setRawByteArrayReplyMethods(rawByteArrayReplyMethods);
    }


    /**
     * This property allows to configure the communication pattern this proxy will use.
     * If {@link CommunicationPattern#isExpectingAnswer()} this proxy is 'one way', i.e. calls to
     * methods of {@link AmqpRemotingMethodInterceptor#serviceInterface} will result in a
     * request message sent, but the call be will non-blocking and will not wait for replies.
     * <p/>
     * <p/>
     * One-way-proxies only make sense for interfaces, whose methods have return type void.
     * Calls to methods that return a value will always yield {@code null}
     *
     * @param communicationPattern the communication pattern to use
     */
    public void setCommunicationPattern(CommunicationPattern communicationPattern) {
        interceptor.setCommunicationPattern(communicationPattern);
    }


    public void afterPropertiesSet() {
        this.interceptor.afterPropertiesSet();
        ProxyFactory proxyFactory = new ProxyFactory();
        proxyFactory.addInterface(this.interceptor.getServiceInterface());
        proxyFactory.addInterface(SupervisableAndRepairable.class);
        proxyFactory.addAdvice(this.interceptor);
        this.serviceProxy = proxyFactory.getProxy();
    }

    public Object getObject() throws Exception {
        return serviceProxy;
    }

    public Class<?> getObjectType() {
        return (this.serviceProxy != null) ?
                this.serviceProxy.getClass() : interceptor.getServiceInterface();
    }

    public boolean isSingleton() {
        return true;
    }

}
