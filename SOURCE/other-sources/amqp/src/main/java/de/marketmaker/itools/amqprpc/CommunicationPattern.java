/*
 * CommunicationPattern.java
 *
 * Created on 21.03.2011 09:33:40
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.itools.amqprpc;

/**
 * @author Sebastian Wild (s.wild@market-maker.de)
 */
public enum CommunicationPattern {
    /**
     * This is the default communication pattern. Clients send requests to a general Service queue
     * to which all service exporters are attached. The request is processed by any one of the
     * servers and the reply is sent back to the clients private request queue.
     * <p/>
     * Clients are configured to wait for an answer, even for void methods. (In that case, the
     * reply can be seen as an acknowledgment.)
     * <p/>
     * <p/>
     * Additionally, servers may be given a {@link de.marketmaker.itools.amqprpc.impl.AmqpRpcServer.Settings#serverId}.
     * Then they will also listens on a private request queue. A server with configured serverId
     * will send its id in replies and it can be retrieved from {@link de.marketmaker.itools.amqprpc.ServiceProviderSelection#ID_FROM_LAST_REPLY}.
     * If clients set such an id in {@link de.marketmaker.itools.amqprpc.ServiceProviderSelection#ID_FOR_NEXT_SEND},
     * the request will be sent to the private request queue for that id, such that clients may
     * try to communicate again with the same server.
     * <p/>
     * If that server is not available or too busy, the request will automatically be re-sent to
     * the general service queue.
     * <p/>
     * <p/>
     * <p/>
     * This pattern internally uses one general request queue of name {@link de.marketmaker.itools.amqprpc.connections.AmqpRpcAddress#getRequestQueue()}
     * which is bound with its name as routingKey to the exchange ({@link de.marketmaker.itools.amqprpc.connections.AmqpRpcAddress#getExchange()}).
     * <p/>
     * Additionally, servers with id use a queue of name {@code <generalQueue>@<serverId>}, also
     * bound to the exchange using the name as routingKey.
     */
    POINT_TO_POINT_RPC(true),
    /**
     * This communication pattern sends a request to <b>all</b> service providers using the same
     * {@link de.marketmaker.itools.amqprpc.connections.AmqpRpcAddress}. The communication is one
     * way without acknowledgement, i.e. the client thread immediately returns after issuing
     * the request and clients should not rely on delivery of requests.
     * <p/>
     * <p/>
     * It is advisable to set {@link de.marketmaker.itools.amqprpc.impl.AmqpRpcClient.Settings#usingImmediateForRequestMessages}
     * to false (default). Otherwise, messages to currently busy servers will silently be dropped.
     * <p/>
     * <p/>
     * <p/>
     * This pattern internally uses one private request queues per server that are all bound to
     * the exchange ({@link de.marketmaker.itools.amqprpc.connections.AmqpRpcAddress#getExchange()})
     * with routingKey {@link de.marketmaker.itools.amqprpc.connections.AmqpRpcAddress#getRequestQueue()}.
     * <p/>
     * Hence, clients need only send <b>one</b> message with that routingKey and it will be
     * copied to all request queues by the AMQP broker.
     */
    ONEWAY_NOTIFY_ALL(false),
    /**
     * This communication pattern sends a request to <b>any one</b> service providers using the same
     * {@link de.marketmaker.itools.amqprpc.connections.AmqpRpcAddress}. The communication is one
     * way without acknowledgement, i.e. the client thread immediately returns after issuing
     * the request and clients should not rely on delivery of requests.
     * <p/>
     * The client cannot influence which server will receive the message.
     * <p/>
     * <p/>
     * It is advisable to set {@link de.marketmaker.itools.amqprpc.impl.AmqpRpcClient.Settings#usingImmediateForRequestMessages}
     * to false (default). Otherwise, messages to currently busy servers will silently be dropped.
     * <p/>
     * <p/>
     * <p/>
     * This pattern internally uses one general request queue of name {@link de.marketmaker.itools.amqprpc.connections.AmqpRpcAddress#getRequestQueue()},
     * that all servers are listening on. The queue is bound to the exchange
     * ({@link de.marketmaker.itools.amqprpc.connections.AmqpRpcAddress#getExchange()})
     * with its name as routingKey.
     * <p/>
     * Messages are sent to with that routingKey by the client and the AMQP broker will
     * automatically select on of the servers.
     */
    ONEWAY_NOTIFY_ANY(false);


    CommunicationPattern(boolean expectingAnswer) {
        this.expectingAnswer = expectingAnswer;
    }

    private final boolean expectingAnswer;

    /**
     * @return true iff AmqpRpcClients have to wait for an answer in this communication pattern.
     */
    public boolean isExpectingAnswer() {
        return expectingAnswer;
    }
}
