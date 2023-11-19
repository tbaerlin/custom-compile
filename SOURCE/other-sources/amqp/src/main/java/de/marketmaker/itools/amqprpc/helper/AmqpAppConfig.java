package de.marketmaker.itools.amqprpc.helper;

import com.rabbitmq.client.ConnectionFactory;
import de.marketmaker.istar.common.amqp.AmqpAddress;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import de.marketmaker.itools.amqprpc.AmqpProxyFactoryBean;
import de.marketmaker.itools.amqprpc.connections.AmqpRpcAddress;
import de.marketmaker.itools.amqprpc.connections.AmqpRpcAddressImpl;
import de.marketmaker.itools.amqprpc.connections.AmqpRpcConnectionManager;
import de.marketmaker.itools.amqprpc.supervising.PeriodicSupervisor;

/**
 * Base class for istar RPC consumers.
 *
 * Consider to:
 * <ul>
 *   <li>Queue names are provided by the {@link AmqpAddress} of the proxy bean interfaces</li>
 *   <li>The {@link AmqpRpcConnectionManager} has to be {@link AmqpRpcConnectionManager#close()}d</li>
 * </ul>
 *
 * @author tkiesgen
 * @author mcoenen
 */
@Configuration
public abstract class AmqpAppConfig {

    @Value("#{systemProperties['amqpBroker.host'] ?: 'temsgsrv'}")
    protected String amqpBrokerHost;

    @Value("#{systemProperties['amqpBroker.username'] ?: 'merger'}")
    protected String amqpBrokerUsername;

    @Value("#{systemProperties['amqpBroker.password'] ?: 'merger'}")
    protected String amqpBrokerPassword;

    @Value("#{systemProperties['amqpBroker.port'] ?: 5672}")
    protected int amqpBrokerPort;

    @Value("#{systemProperties['amqpBroker.virtualHost'] ?: 'istar'}")
    protected String amqpBrokerVirtualHost;

    @Value("#{systemProperties['amqpBroker.requestedHeartbeat'] ?: 15}")
    protected int amqpBrokerRequestedHeartbeat;

    @Value("#{systemProperties['amqpBrokerExchange'] ?: 'istar.rpc'}")
    protected String amqpBrokerExchange;

    @Bean
    protected ConnectionFactory connectionFactory() {
        final ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(this.amqpBrokerHost);
        factory.setUsername(this.amqpBrokerUsername);
        factory.setPassword(this.amqpBrokerPassword);
        factory.setPort(this.amqpBrokerPort);
        factory.setVirtualHost(this.amqpBrokerVirtualHost);
        factory.setRequestedHeartbeat(this.amqpBrokerRequestedHeartbeat);
        return factory;
    }

    @Bean
    protected AmqpRpcConnectionManager connectionManager() {
        final AmqpRpcConnectionManager cm = new AmqpRpcConnectionManager();
        cm.setConnectionFactory(connectionFactory());
        return cm;
    }

    @Bean
    protected PeriodicSupervisor periodicSupervisor() {
        final PeriodicSupervisor supervisor = new PeriodicSupervisor();
        supervisor.setCheckInterval(60);
        return supervisor;
    }

    protected <T> AmqpRpcAddressImpl ampqRpcAdress(Class<T> clazz) {
        final String queueName = clazz.getAnnotation(AmqpAddress.class).queue();
        final AmqpRpcAddressImpl amqpRpcAddress = new AmqpRpcAddressImpl();
        final AmqpRpcAddress.Settings settings = new AmqpRpcAddress.Settings();
        settings.setRequestQueueMessageTTL(10_000);
        amqpRpcAddress.setSettings(settings);
        amqpRpcAddress.setExchange(this.amqpBrokerExchange);
        amqpRpcAddress.setRequestQueue(queueName);
        return amqpRpcAddress;
    }

    @SuppressWarnings("unchecked")
    protected <T> T proxy(Class<T> clazz) throws Exception {
        final AmqpProxyFactoryBean proxy = new AmqpProxyFactoryBean();
        proxy.setConnectionManager(connectionManager());
        proxy.setRpcTimeout(20_000);
        proxy.setSlowInvocationThreshold(2_000);
        proxy.setSupervisor(periodicSupervisor());
        proxy.setServiceInterface(clazz);
        proxy.setAddress(ampqRpcAdress(clazz));
        proxy.afterPropertiesSet();
        return (T) proxy.getObject();
    }
}
