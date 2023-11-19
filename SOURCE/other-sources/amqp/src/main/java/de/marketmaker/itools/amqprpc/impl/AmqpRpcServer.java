/*
 * AmqpRpcServer.java
 *
 * Created on 09.03.2011 16:39:39
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.itools.amqprpc.impl;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.LongString;
import net.jcip.annotations.ThreadSafe;
import org.apache.commons.lang3.SerializationException;

import de.marketmaker.istar.common.amqp.ForwardRequestException;
import de.marketmaker.itools.amqprpc.CommunicationPattern;
import de.marketmaker.itools.amqprpc.connections.AmqpDeclarationUtil;
import de.marketmaker.itools.amqprpc.connections.AmqpRpcAddress;

import static de.marketmaker.istar.common.log.LoggingUtil.UNIQUE_ID;
import static de.marketmaker.itools.amqprpc.impl.ServiceProviderSelectionUtil.APP_ID;

/**
 * This class handles incoming requests by passing them to a {@link WireLevelServiceProvider}
 * and sending back its reply over an AMQP connection.
 * <p/>
 * Objects of this class are 'disposable one-time objects' in the sense that they are not designed
 * to recover from failure of the underlying connection. Instead, a new instance should be created.
 * <p/>
 * As each instance starts a thread handling incoming requests, you should call {@link #closeChannel()},
 * when the rpc server is no longer needed. The thread is automatically stopped, when the used
 * {@link com.rabbitmq.client.Channel} dies.
 * <p/>
 * <small>Originally inspired by {@link com.rabbitmq.client.RpcServer} this class was almost completely
 * rewritten to be Thread-safe and more robust.</small>
 *
 * @author Sebastian Wild (s.wild@market-maker.de)
 */
public class AmqpRpcServer extends AmqpRpcSupport {

    @ThreadSafe
    private class MyConsumer extends DefaultConsumer {
        public MyConsumer(Channel channel) {
            super(channel);
        }

        @Override
        public void handleDelivery(String consumerTag, Envelope envelope,
                                   AMQP.BasicProperties properties, byte[] body) throws IOException {
            try {
                processRequest(properties, body);
            } finally {
                if (!settings.isUsingAutoAckForConsumer()) {
                    channel.basicAck(envelope.getDeliveryTag(), false);
                }
            }
        }
    }

    private final static Integer NONPERSISTENT_DELIVERY_MODE = 1;

    private final static AtomicInteger INSTANCE_NO = new AtomicInteger();

    private final WireLevelServiceProvider wireLevelServiceProvider;

    private final Settings settings;

    protected String privateRequestQueue;

    protected MyConsumer requestConsumer;

    private CopyOnWriteArrayList<String> consumerTags = new CopyOnWriteArrayList<>();


    /**
     * Construct a new AmqpRpcServer and immediately start it, i.e. it will immediately try and start
     * to answer incoming requests.
     *
     * @param connection               the connection our private channel is created from
     * @param wireLevelServiceProvider the callback to use on message receipt
     * @param address                  the address to listen for messages
     * @param settings                 optional settings
     * @throws IOException if consumer registration failed
     */
    public AmqpRpcServer(Connection connection, WireLevelServiceProvider wireLevelServiceProvider,
                         final AmqpRpcAddress address, Settings settings) throws IOException {
        super(connection, address, "AmqpRpcServer-" + INSTANCE_NO.incrementAndGet());
        this.wireLevelServiceProvider = wireLevelServiceProvider;
        this.settings = settings;

        configureChannel();
    }

    @Override
    public void closeChannel() {
        cancelConsumers();
        super.closeChannel();
    }

    private void cancelConsumers() {
        for (String consumerTag : this.consumerTags) {
            cancelConsumer(consumerTag);
        }
        this.consumerTags.clear();
    }

    private void cancelConsumer(String consumerTag) {
        try {
            if (!this.channel.isOpen()) {
                this.logger.info("<cancelConsumer> aborted for '" + consumerTag
                        + "', channel not open");
                return;
            }
            this.channel.basicCancel(consumerTag);
            this.logger.info("<cancelConsumer> succeeded for '" + consumerTag + "'");
        } catch (IOException e) {
            this.logger.warn("<cancelConsumer> failed for '" + consumerTag + "'", e);
        }
    }

    private void configureChannel() throws IOException {
        this.channel.basicQos(this.settings.getNumberOfMessagesToPrefetch());
        this.requestConsumer = createRequestConsumer();
        switch (this.settings.communicationPattern) {
            case POINT_TO_POINT_RPC:
                // use general and additional private queue if serverId is set
                AmqpDeclarationUtil.declareExchangeAndQueue(this.channel, this.address);
                registerRequestConsumerOn(this.address.getRequestQueue());
                if (this.settings.isUsePrivateRequestQueue()) {
                    // set up private queue and consumer for requests directly delivered to us
                    this.privateRequestQueue = createPrivateRequestQueue(
                            this.settings.getServerIdQueueName(), true);
                    registerRequestConsumerOn(this.privateRequestQueue);
                }
                break;
            case ONEWAY_NOTIFY_ALL:
                // only use private queue
                AmqpDeclarationUtil.declareExchange(this.channel, this.address);
                this.privateRequestQueue = createPrivateRequestQueue(null, false);
                registerRequestConsumerOn(this.privateRequestQueue);
                break;
            case ONEWAY_NOTIFY_ANY:
                // only use general queue
                AmqpDeclarationUtil.declareExchangeAndQueue(this.channel, this.address);
                registerRequestConsumerOn(this.address.getRequestQueue());
                break;
        }
    }

    public int getMessageCount() throws IOException {
        switch (this.settings.communicationPattern) {
            case ONEWAY_NOTIFY_ALL:
                AMQP.Queue.DeclareOk declareOk = this.channel.queueDeclarePassive(privateRequestQueue);
                logger.trace("<getMessageCount> declareOk=" + declareOk);
                return declareOk.getMessageCount();
            default:
                throw new RuntimeException("Unsupported communicationPattern: " + this.settings.communicationPattern);
        }
    }


    /**
     * Registers a consumer on the reply queue.
     *
     * @return the newly created and registered consumer
     * @throws IOException if an error is encountered
     */
    protected MyConsumer createRequestConsumer() throws IOException {
        return new MyConsumer(this.channel);
    }

    private void registerRequestConsumerOn(final String queue) throws IOException {
        final String consumerTag = this.channel.basicConsume(queue,
                this.settings.isUsingAutoAckForConsumer(), this.requestConsumer);
        this.logger.info("<registerRequestConsumerOn> tag='" + consumerTag + "', queue=" + queue);
        this.consumerTags.add(consumerTag);
    }

    /**
     * Creates an exclusive autodelete queue for requests intended for this server only
     *
     * @param useSuffixInRoutingKey if true, the combined queue name is used as routingKey,
     *                              if false, {@code this.address.getRequestQueue()} is taken
     * @return the name of the reply queue
     * @throws IOException if an error is encountered
     */
    protected String createPrivateRequestQueue(String requestQueueOverride,
                                               boolean useSuffixInRoutingKey)
            throws IOException {
        final String requestQueue = (requestQueueOverride == null)
                ? this.address.getRequestQueue() : requestQueueOverride;
        final String queue
                = ServiceProviderSelectionUtil.getRoutingKeyFor(requestQueue, APP_ID);
        try {
            this.channel.queueDeclare(queue, false, true, true,
                    this.address.getSettings().getRequestQueueProperties()).getQueue();
        } catch (IOException e) {
            throw new IOException("Error declaring private request queue. You probably already " +
                    "started a server with ID " + APP_ID + " for queue " +
                    this.address.getRequestQueue() + ".", e);
        }

        final String routingKey = useSuffixInRoutingKey ? queue : requestQueue;
        this.channel.queueBind(queue, this.address.getExchange(), routingKey);

        this.logger.info("<createPrivateRequestQueue> " + queue + " on "
                + this.address.getExchange() + ", routingKey=" + routingKey);

        return queue;
    }

    private void processRequest(AMQP.BasicProperties rps, byte[] body) throws IOException {
        if (this.settings.communicationPattern.isExpectingAnswer()) {
            if (rps.getCorrelationId() == null || rps.getReplyTo() == null) {
                this.logger.warn("<processRequest> received AMQP-RPC request on " +
                        this.address.getRequestQueue()
                        + " without correlationId or replyTo " + toDebugString(rps));
                return; // it makes no sense to throw exception as client won't get informed anyway ...
            }
        }

        final String unique_id = getUniqueId(rps);
        if (unique_id != null) {
            MDC.INSTANCE.put(UNIQUE_ID, unique_id);
        }


        try {
            byte[] replyBody = this.wireLevelServiceProvider.call(body);
            if (this.settings.communicationPattern.isExpectingAnswer()) {
                sendAnswer(rps, replyBody, null);
            }
        } catch (ForwardRequestException fre) {
            this.channel.basicPublish(this.address.getExchange(), fre.getQueueName(),
                    this.settings.isUsingMandatoryForForwardedMessages(),
                    rps, body);
        } catch (SerializationException se) {
            this.logger.error("<processRequest> failed to unmarshal request received on " +
                    this.address.getRequestQueue() + ": " + se.getMessage()
                    + ", " + toDebugString(rps));
            if (this.settings.communicationPattern.isExpectingAnswer()) {
                sendAnswer(rps, new byte[0], se.getMessage());
            }
        } finally {
            if (unique_id != null) {
                MDC.INSTANCE.put(UNIQUE_ID, null);
            }
        }

    }

    private String getUniqueId(AMQP.BasicProperties rps) {
        final Map<String, Object> headers = rps.getHeaders();
        if (headers == null) {
            return null;
        }
        final LongString ls = (LongString) headers.get(UNIQUE_ID);
        return (ls != null) ? ls.toString() : null;
    }

    private String toDebugString(AMQP.BasicProperties rps) {
        final StringBuilder sb = new StringBuilder(100);
        rps.appendPropertyDebugStringTo(sb);
        return sb.toString();
    }

    private void sendAnswer(AMQP.BasicProperties requestProperties,
                            byte[] replyBody, String errorMsg) throws IOException {
        AMQP.BasicProperties.Builder builder = new AMQP.BasicProperties.Builder()
                .deliveryMode(NONPERSISTENT_DELIVERY_MODE)
                .appId(APP_ID)
                .correlationId(requestProperties.getCorrelationId());

        if (errorMsg != null) {
            builder.contentType(ERROR + errorMsg);
        }

        final String exchange = isDirectReplyTo(requestProperties) ? "" : this.address.getExchange();

        this.channel.basicPublish(exchange, requestProperties.getReplyTo(),
                this.settings.isUsingMandatoryForReplyMessages(),
                builder.build(), replyBody);
        if (this.logger.isDebugEnabled()) {
            logger.debug("<sendAnswer> for " + requestProperties.getCorrelationId()
                    + ", exchange '" + exchange +
                    "', reply-to " + requestProperties.getReplyTo());
        }
    }

    private boolean isDirectReplyTo(AMQP.BasicProperties requestProperties) {
        return requestProperties.getReplyTo().startsWith("amq.rabbitmq.reply-to");
    }

    /**
     * Simple bean class holding optional configuration options for {@link AmqpRpcServer}
     */
    public static class Settings {

        public Settings() {
        }

        private CommunicationPattern communicationPattern = CommunicationPattern.POINT_TO_POINT_RPC;

        private boolean usePrivateRequestQueue = false;

        private String serverIdQueueName = null;

        private int numberOfMessagesToPrefetch = 20;

        private boolean usingAutoAckForConsumer = false;

        private boolean usingMandatoryForReplyMessages = true;

        private boolean usingMandatoryForForwardedMessages = true;

        public int getNumberOfMessagesToPrefetch() {
            return numberOfMessagesToPrefetch;
        }

        public void setNumberOfMessagesToPrefetch(int numberOfMessagesToPrefetch) {
            this.numberOfMessagesToPrefetch = numberOfMessagesToPrefetch;
        }

        public String getServerIdQueueName() {
            return serverIdQueueName;
        }

        public void setServerIdQueueName(String serverIdQueueName) {
            this.serverIdQueueName = serverIdQueueName;
        }

        public boolean isUsingAutoAckForConsumer() {
            return usingAutoAckForConsumer;
        }

        public void setUsingAutoAckForConsumer(boolean usingAutoAckForConsumer) {
            this.usingAutoAckForConsumer = usingAutoAckForConsumer;
        }

        public boolean isUsingMandatoryForForwardedMessages() {
            return usingMandatoryForForwardedMessages;
        }

        public void setUsingMandatoryForForwardedMessages(
                boolean usingMandatoryForForwardedMessages) {
            this.usingMandatoryForForwardedMessages = usingMandatoryForForwardedMessages;
        }

        public boolean isUsingMandatoryForReplyMessages() {
            return usingMandatoryForReplyMessages;
        }

        public void setUsingMandatoryForReplyMessages(boolean usingMandatoryForReplyMessages) {
            this.usingMandatoryForReplyMessages = usingMandatoryForReplyMessages;
        }

        /**
         * The server's id is sent to clients in {@link com.rabbitmq.client.AMQP.BasicProperties#appId}
         * such that they can (optionally) prefer sending messages to
         * this service provider implementation again. Allows for incremental updates etc.
         *
         * @param b If {@code false}, no ID will be sent (default)<br>
         *          If {@code true}, the server will create an additional private
         *          queue where it listens for exclusive calls.
         */
        public void setUseServerId(boolean b) {
            setUsePrivateRequestQueue(b);
        }

        public boolean isUsePrivateRequestQueue() {
            return usePrivateRequestQueue;
        }

        public void setUsePrivateRequestQueue(boolean usePrivateRequestQueue) {
            this.usePrivateRequestQueue = usePrivateRequestQueue;
        }

        public CommunicationPattern getCommunicationPattern() {
            return communicationPattern;
        }

        /**
         * This property allows to set the {@link de.marketmaker.itools.amqprpc.CommunicationPattern}
         * used by the underlying rpc server.
         * <p/>
         * <p/>
         * Using different CommunicationPatterns on server and client side is strongly discouraged,
         * the behavior of such a system is unspecified.
         *
         * @param communicationPattern the CommunicationPattern to use
         */
        public void setCommunicationPattern(CommunicationPattern communicationPattern) {
            this.communicationPattern = communicationPattern;
        }
    }
}
