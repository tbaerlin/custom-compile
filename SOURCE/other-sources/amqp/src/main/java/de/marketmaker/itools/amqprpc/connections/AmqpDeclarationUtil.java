/*
 * AmqpDeclarationUtil.java
 *
 * Created on 16.03.2011 12:08:41
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.itools.amqprpc.connections;

import com.rabbitmq.client.Channel;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * This class holds methods used to declare exchanges and queues on a given {@link com.rabbitmq.client.Channel}
 *
 * @author Sebastian Wild (s.wild@market-maker.de)
 */
public class AmqpDeclarationUtil {

    /**
     * This methods declares all the exchange for the RPC request, using
     * the given {@code channel}. Note that the channel has to be created from a connection
     * that has the required rights to declare exchanges etc. on the RabbitMQ broker.
     * <p/>
     * The method will fail, if some entities already exist with incompatible properties set.
     *
     * @param channel channel to use
     * @param address address to use
     * @throws AmqpDeclarationFailureException
     *          if declaration fails
     */
    public static void declareExchange(Channel channel, AmqpRpcAddress address)
            throws AmqpDeclarationFailureException {
        final String exchange = address.getExchange();
        try {
            final AmqpRpcAddress.Settings settings = address.getSettings();
            channel.exchangeDeclare(exchange, "direct",
                    settings.isExchangeDurable(), settings.isExchangeAutoDelete(), null);
        } catch (IOException e) {
            throw new AmqpDeclarationFailureException("Could not declare exchange " + exchange, e);
        }
    }

    /**
     * This methods declares all needed exchanges, queues and bindings for the RPC request, using
     * the given {@code channel}. Note that the channel has to be created from a connection
     * that has the required rights to declare exchanges etc. on the RabbitMQ broker.
     * <p/>
     * The method will fail, if some entities already exist with incompatible properties set.
     *
     * @param channel channel to use
     * @param address address to use
     * @throws AmqpDeclarationFailureException
     *          if declaration fails
     */
    public static void declareExchangeAndQueue(Channel channel, AmqpRpcAddress address)
            throws AmqpDeclarationFailureException {
        declareExchange(channel, address);
        final String queue = address.getRequestQueue();
        final String exchange = address.getExchange();
        final AmqpRpcAddress.Settings settings = address.getSettings();
        final int ttl = settings.getRequestQueueMessageTTL();

        Map<String, Object> props = null;
        if (ttl > 0) {
            props = new HashMap<String, Object>(3);
            props.put("x-message-ttl", ttl);
        }

        try {
            channel.queueDeclare(queue, settings.isQueueDurable(), false,
                    settings.isQueueAutoDelete(), props);
        } catch (IOException e) {
            throw new AmqpDeclarationFailureException("Could not declare queue " + queue, e);
        }

        try {
            channel.queueBind(queue, exchange, queue);
        } catch (IOException e) {
            throw new AmqpDeclarationFailureException("Could not declare binding from queue " +
                    queue + " to exchange " + exchange, e);
        }
    }

}
