/*
 * AmqpRpcAddress.java
 *
 * Created on 04.03.2011 11:52:09
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.itools.amqprpc.connections;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * An AmqpRpcAddress gathers settings needed to implement or use a AMQP-remoted service.
 * It can be seen as the analogue of a URL for a Web Service. An AmqpRpcAddress consists of
 * <ul>
 * <li>an <b>exchange</b> to which messages are published</li>
 * <li>a <b>request queue</b> from which requests are fetched
 * <br/>As we only use <em>direct</em>-type exchanges, the name of the queue coincides with the
 * <em>routingKey</em> for sending requests. Therefore, we will not differentiate between those
 * two terms, even though they are distinct entities in the AMQP world.
 * </li>
 * </ul>
 * Methods {@link de.marketmaker.itools.amqprpc.connections.AmqpDeclarationUtil#declareExchange(com.rabbitmq.client.Channel, AmqpRpcAddress)}  }
 * {@link AmqpDeclarationUtil#declareExchangeAndQueue(com.rabbitmq.client.Channel, AmqpRpcAddress)} )}
 * can be used to ensure
 * that needed entities exist in the AMQP broker. Because AMQP declares are idempotent, this
 * method can safely be called each time a connection is established.
 * <p/>
 * <p/>
 * Additionally, this class allows for some settings influencing the way messages are handled
 * inside the RabbitMQ broker.
 *
 * @author Sebastian Wild (s.wild@market-maker.de)
 */
public interface AmqpRpcAddress {

    String DEFAULT_EXCHANGE = "amqp.rpc";

    /**
     * @return the name of the exchange to publish messages to
     */
    String getExchange();

    /**
     * @return the name of the queue to listen for incoming requests <em>and</em> the routingKey
     *         to use sending requests.
     */
    String getRequestQueue();

    /**
     * @return optional Settings collected in class {@link de.marketmaker.itools.amqprpc.connections.AmqpRpcAddress.Settings}
     */
    Settings getSettings();

    /**
     * @return a String representation of this a address that holds all properties.
     *         The format is inspired by HTTP URIs that contain query parameters:
     *         {@code exchange/queue?key1=value1&key2=value2}
     */
    String getPseudoUrl();

    /**
     * Sets all parameters that occurr in the pseudoUrl, overwriting existing values.
     * The pseudoUrl has to be of the shape {@code exchange/queue?key1=value1&key2=value2}
     * and all occurring keys must be supported by the implementation used.
     *
     * @param pseudoUrl the pseudo URL to set props from
     */
    void setPseudoUrl(String pseudoUrl);

    /**
     * Simple bean class collecting all optional settings for
     * {@link de.marketmaker.itools.amqprpc.connections.AmqpRpcAddress}es.
     */
    public static final class Settings {
        private int requestQueueMessageTTL = 20000;
        private boolean exchangeAutoDelete = false;
        private boolean exchangeDurable = false;
        private boolean queueAutoDelete = false;
        private boolean queueDurable = false;

        public boolean isExchangeAutoDelete() {
            return exchangeAutoDelete;
        }

        public void setExchangeAutoDelete(boolean exchangeAutoDelete) {
            this.exchangeAutoDelete = exchangeAutoDelete;
        }

        public boolean isExchangeDurable() {
            return exchangeDurable;
        }

        public void setExchangeDurable(boolean exchangeDurable) {
            this.exchangeDurable = exchangeDurable;
        }

        public boolean isQueueAutoDelete() {
            return queueAutoDelete;
        }

        public void setQueueAutoDelete(boolean queueAutoDelete) {
            this.queueAutoDelete = queueAutoDelete;
        }

        public boolean isQueueDurable() {
            return queueDurable;
        }

        public void setQueueDurable(boolean queueDurable) {
            this.queueDurable = queueDurable;
        }

        public int getRequestQueueMessageTTL() {
            return requestQueueMessageTTL;
        }

        public Map<String, Object> getRequestQueueProperties() {
            if (this.requestQueueMessageTTL > 0) {
                return new HashMap<String, Object>(
                        Collections.singletonMap("x-message-ttl", this.requestQueueMessageTTL));
            }
            return null;
        }

        /**
         * If positive, the RabbitMQ-specific queue property {@code x-message-ttl} will be set to
         * this value in {@link AmqpDeclarationUtil#declareExchangeAndQueue(com.rabbitmq.client.Channel, AmqpRpcAddress)}. <br/>
         * If zero or negative, no TTL will be set.
         *
         * @param requestQueueMessageTTL
         */
        public void setRequestQueueMessageTTL(int requestQueueMessageTTL) {
            this.requestQueueMessageTTL = requestQueueMessageTTL;
        }
    }

}
