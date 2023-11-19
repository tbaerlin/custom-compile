/*
 * AmqpRpcClient.java
 *
 * Created on 10.03.2011 11:44:28
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.itools.amqprpc.impl;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.ReturnListener;
import com.rabbitmq.client.ShutdownSignalException;
import com.rabbitmq.utility.BlockingCell;

import de.marketmaker.istar.common.amqp.ServiceProviderSelection;
import de.marketmaker.itools.amqprpc.connections.AmqpConnectionShutdownException;
import de.marketmaker.itools.amqprpc.connections.AmqpDeclarationUtil;
import de.marketmaker.itools.amqprpc.connections.AmqpRpcAddress;

import org.joda.time.DateTimeConstants;
import org.springframework.remoting.RemoteAccessException;
import org.springframework.remoting.RemoteLookupFailureException;

import java.io.IOException;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import static de.marketmaker.istar.common.amqp.ServiceProviderSelection.ID_FOR_NEXT_SEND;
import static de.marketmaker.istar.common.log.LoggingUtil.UNIQUE_ID;

/**
 * Instances of this class offer the method {@link #sendAndWaitForReply(byte[], int)}
 * to send an arbitrary message-payload to the configured AMQP queue and wait for a reply.
 * <p/>
 * This class is designed for concurrent use of {@link #sendAndWaitForReply(byte[], int)}.
 * Internally, it uses the {@link com.rabbitmq.client.AMQP.BasicProperties#correlationId}
 * property to match replies to corresponding requests.
 * <p/>
 * <small>Originally inspired by {@link com.rabbitmq.client.RpcClient} this class was almost completely
 * rewritten to be Thread-safe and more robust.</small>
 * @author Sebastian Wild (s.wild@market-maker.de)
 */
public class AmqpRpcClient extends AmqpRpcSupport implements WireLevelServiceProxy {

    private final static Integer NONPERSISTENT_DELIVERY_MODE = 1;

    private final static String DIRECT_REPLY_TO_QUEUE = "amq.rabbitmq.reply-to";

    private final static boolean USE_DIRECT_REPLY_TO = Boolean.getBoolean("amqp.direct.reply-to");

    private final static AtomicInteger INSTANCE_NO = new AtomicInteger();

    private final Settings settings;

    protected String replyQueue;

    protected DefaultConsumer replyConsumer;

    protected final AtomicInteger nextFreeCorrelationId = new AtomicInteger(0);

    protected final ConcurrentMap<String, BlockingCell<Reply>> continuationMap =
            new ConcurrentHashMap<>(128);

    /**
     * Construct a new AmqpRpcClient and immediately start it, i.e. it will immediately set up
     * its channel to allow calls to {@link #sendAndWaitForReply(byte[], int)}.
     * @param connection the connection our private channel is created from
     * @param address the address to listen for messages
     * @param settings optional settings
     * @throws IOException if consumer registration failed
     */
    public AmqpRpcClient(Connection connection, final AmqpRpcAddress address, Settings settings)
            throws IOException {
        super(connection, address, "AmqpRpcClient-" + INSTANCE_NO.incrementAndGet());
        this.settings = settings;
        configureChannel();
    }

    private void configureChannel() throws IOException {
        AmqpDeclarationUtil.declareExchange(this.channel, this.address);
        this.replyQueue = createReplyQueue();
        if (this.logger.isDebugEnabled()) {
            this.logger.debug("<configureChannel> reply-to " + this.replyQueue
                    + " for " + this.address.getRequestQueue());
        }
        this.replyConsumer = createReplyConsumer();
        registerNotDeliverableCallback(channel);
    }

    /**
     * Creates a server-named exclusive autodelete queue to use for
     * receiving replies to RPC requests.
     * @return the name of the reply queue
     * @throws IOException if an error is encountered
     */
    protected String createReplyQueue() throws IOException {
        if (USE_DIRECT_REPLY_TO) {
            return DIRECT_REPLY_TO_QUEUE;
        }
        final String queueName = this.channel.queueDeclare("", false, true, true, null).getQueue();
        this.channel.queueBind(queueName, this.address.getExchange(), queueName);
        return queueName;
    }

    /**
     * Creates and registers a consumer on the reply queue.
     * @return the newly created and registered consumer
     * @throws IOException if an error is encountered
     */
    protected DefaultConsumer createReplyConsumer() throws IOException {
        DefaultConsumer consumer = new DefaultConsumer(this.channel) {
            @Override
            public void handleShutdownSignal(String consumerTag, ShutdownSignalException signal) {
                cancelPending();
            }

            @Override
            public void handleDelivery(String consumerTag, Envelope envelope,
                    AMQP.BasicProperties properties, byte[] body)
                    throws IOException {

                String contentType = properties.getContentType();
                if (contentType != null && contentType.startsWith(ERROR)) {
                    setReply(properties, new RemoteAccessException(contentType.substring(ERROR.length())));
                }
                else {
                    setReply(properties, body);
                }
            }
        };
        this.channel.basicConsume(this.replyQueue, true, consumer);
        return consumer;
    }

    protected void registerNotDeliverableCallback(Channel channel) {
        channel.addReturnListener(new ReturnListener() {
            //            @SuppressWarnings({"ThrowableInstanceNeverThrown"})
            public void handleReturn(int replyCode, String replyText, String exchange,
                    String routingKey, AMQP.BasicProperties properties,
                    byte[] body) {
                /* Message was rejected, because no suitable queue exists or because the immediate
                 * requirement could not be satisfied. There are two cases that have to be
                 * handled differently:
                 *  (a) The message was originally sent to a specific service provider, but
                 *      now this server was not reachable. In this case, we would like to
                 *      re-try sending the request on the general request queue.
                 *  (b) The message could not be delivered to ANY service provider. In that
                 *      case, it makes no sense to re-try.
                 */
                if (isProviderSpecific(routingKey) && settings.isResubmitWithoutProviderPreference()) {
                    resubmitWithoutProviderPreference(routingKey, properties, body);
                }
                else {
                    final Reply reply = new Reply(new RemoteLookupFailureException(
                            "Could not find suitable queue for routingKey " + routingKey +
                                    " on exchange " + exchange + ". Reply Code: " + replyCode +
                                    ": '" + replyText + "'"), null);
                    setReply(properties.getCorrelationId(), reply);
                }

            }
        });
    }

    private void setReply(AMQP.BasicProperties properties, Object result) {
        setReply(properties.getCorrelationId(), new Reply(result, properties.getAppId()));
    }

    private void setReply(String corrId, Reply reply) {
        if (corrId == null) {
            return;
        }
        final BlockingCell<Reply> blockingCell = this.continuationMap.remove(corrId);
        if (blockingCell != null) {
            blockingCell.setIfUnset(reply);
        }
    }

    private void resubmitWithoutProviderPreference(String routingKey,
            AMQP.BasicProperties properties, byte[] body) {
        if (logger.isDebugEnabled()) {
            logger.debug("<resubmitWithoutProviderPreference> Request could not be delivered to "
                    + routingKey + ", trying again on general queue.");
        }
        final String corrId = properties.getCorrelationId();
        final BlockingCell<Reply> blockingCell = continuationMap.get(corrId);
        final PendingRequest pendingRequest = new PendingRequest(
                blockingCell, body, corrId,
                // no serverId --> sent to general queue
                null);

        sendRequest(pendingRequest);
    }

    private boolean isProviderSpecific(String routingKey) {
        return !routingKey.equals(address.getRequestQueue());
    }

    @Override
    public void closeChannel() {
        super.closeChannel();
        cancelPending();
    }

    private void cancelPending() {
        final AmqpConnectionShutdownException result
                = new AmqpConnectionShutdownException("AMQP Connection shut down", getCauseOfShutdown());
        int n = 0;
        for (BlockingCell<Reply> blockingCell : continuationMap.values()) {
            if (blockingCell.setIfUnset(new Reply(result, null))) {
                n++;
            }
        }
        this.continuationMap.clear();
        if (n > 0) {
            this.logger.info("<cancelPending> cancelled " + n + " pending requests on "
                    + this.address.getRequestQueue());
        }
    }

    protected String sendRequest(PendingRequest request) {
        final AMQP.BasicProperties.Builder builder = new AMQP.BasicProperties.Builder()
                .deliveryMode(NONPERSISTENT_DELIVERY_MODE);
        if (request.correlationId != null) {
            builder.correlationId(request.correlationId).replyTo(this.replyQueue);
        }
        if (this.settings.expiration != null) {
            builder.expiration(this.settings.expiration);
        }

        final String routingKey;
        final boolean mandatory;
        if (request.serverId != null) {
            // If a serverId was provided, sent to that server's private queue ...
            routingKey = ServiceProviderSelectionUtil.getRoutingKeyFor(
                    this.address.getRequestQueue(), request.serverId);
            mandatory = true;
        }
        else {
            // ... otherwise use general request queue
            routingKey = this.address.getRequestQueue();
            mandatory = this.settings.isUsingMandatoryForRequestMessages();
        }

        final String unique_id = MDC.INSTANCE.get(UNIQUE_ID);
        if (unique_id != null) {
            builder.headers(Collections.<String, Object>singletonMap(UNIQUE_ID, unique_id));
        }

        final AMQP.BasicProperties props = builder.build();

        try {
            this.channel.basicPublish(
                    this.address.getExchange(), routingKey, mandatory, props, request.body);
            if (this.logger.isDebugEnabled()) {
                logger.debug("<publish> for " + request.correlationId
                        + ", exchange '" + this.address.getExchange() + "', routingKey " + routingKey);
            }
        } catch (IOException e) {
            final RemoteAccessException result = new RemoteAccessException("basicPublish failed", e);
            if (request.correlationId != null) {
                setReply(request.correlationId, new Reply(result, null));
            }
            else {
                throw result;
            }
        }
        return routingKey;
    }


    public byte[] sendAndWaitForReply(byte[] request, int timeout)
            throws RemoteAccessException, ShutdownSignalException {
        if (!everythingOk()) {
            //noinspection ThrowableResultOfMethodCallIgnored
            throw new AmqpConnectionShutdownException("rpc client is closed.", getCauseOfShutdown());
        }

        if (this.continuationMap.size() > this.settings.getPendingRequestBlockingQueueCapacity()) {
            //noinspection ThrowableInstanceNeverThrown
            throw new AmqpRpcClientPendingRequestBufferOverflow("I can only buffer " +
                    settings.getPendingRequestBlockingQueueCapacity() +
                    " pending AMQP-RPC requests.");
        }

        // create the blocking cell we will wait on for the reply
        final BlockingCell<Reply> blockingCell = new BlockingCell<>();
        final int correlationId = this.nextFreeCorrelationId.incrementAndGet();
        final PendingRequest pendingRequest = new PendingRequest(blockingCell, request,
                correlationId, getAndRemoveNextSendId());

        this.continuationMap.put(pendingRequest.correlationId, blockingCell);

        String routingKey = null;
        try {
            routingKey = sendRequest(pendingRequest);
            final Reply reply = blockingCell.get(timeout);
            ServiceProviderSelection.ID_FROM_LAST_REPLY.set(reply.serverId);
            return getResult(reply);
        } catch (InterruptedException e) {
            // thread got interrupted while waiting
            throw new RemoteAccessException("Thread was interrupted while waiting for reply", e);
        } catch (TimeoutException e) {
            // rpcTimeout expired before response was here
            throw new RemoteAccessTimeoutException("AMQP-RPC Timeout (" + timeout + "ms) on " + routingKey);
        } catch (Throwable t) {
            cancelPending();
            throw t;
        } finally {
            this.continuationMap.remove(pendingRequest.correlationId);
        }
    }

    private String getAndRemoveNextSendId() {
        final String result = ID_FOR_NEXT_SEND.get();
        ID_FOR_NEXT_SEND.remove();
        return result;
    }

    private byte[] getResult(Reply reply) {
        Object replyBody = reply.result;
        if (replyBody instanceof ShutdownSignalException) {
            throw new AmqpConnectionShutdownException("AMQP connection was shut down.",
                    (ShutdownSignalException) replyBody);
        }
        if (replyBody instanceof RemoteAccessException) {
            throw (RemoteAccessException) replyBody;
        }
        if (replyBody instanceof Throwable) {
            throw new RemoteAccessException("failed on REMOTE HOST " + reply.serverId, (Throwable) replyBody);
        }
        return (byte[]) replyBody;
    }

    public void send(byte[] message) {
        if (!everythingOk()) {
            //noinspection ThrowableResultOfMethodCallIgnored
            throw new AmqpConnectionShutdownException("rpc client is closed.", getCauseOfShutdown());
        }

        final PendingRequest pendingRequest = new PendingRequest(null, message,
                null, getAndRemoveNextSendId());
        sendRequest(pendingRequest);
    }

    /**
     * Little wrapper class for requests
     */
    protected static class PendingRequest {
        public final byte[] body;

        public final String correlationId;

        public final String serverId;

        public final BlockingCell<Reply> blockingCell;

        public PendingRequest(BlockingCell<Reply> blockingCell, byte[] body,
                String correlationId, String serverId) {
            this.blockingCell = blockingCell;
            this.body = body;
            this.correlationId = correlationId;
            this.serverId = serverId;
        }

        public PendingRequest(BlockingCell<Reply> blockingCell, byte[] body, int correlationId,
                String serverId) {
            this.blockingCell = blockingCell;
            this.body = body;
            this.correlationId = Integer.toString(correlationId);
            this.serverId = serverId;
        }
    }

    /**
     * Little wrapper class for replies.
     */
    protected static class Reply {
        public final Object result;

        public final String serverId;

        public Reply(Object result, String serverId) {
            this.result = result;
            this.serverId = serverId;
        }
    }

    /**
     * Simple bean class holding optional configuration options for {@link AmqpRpcClient}
     */
    public static class Settings {
        private int pendingRequestBlockingQueueCapacity = 1000;

        private boolean usingMandatoryForRequestMessages = true;

        private long timeoutWaitingForNextRequest = DateTimeConstants.MILLIS_PER_SECOND;

        private boolean resubmitWithoutProviderPreference = true;

        private String expiration;

        public Settings() {
        }

        public Settings(int pendingRequestBlockingQueueCapacity,
                boolean usingMandatoryForRequestMessages) {
            this.pendingRequestBlockingQueueCapacity = pendingRequestBlockingQueueCapacity;
            this.usingMandatoryForRequestMessages = usingMandatoryForRequestMessages;
        }

        public int getPendingRequestBlockingQueueCapacity() {
            return pendingRequestBlockingQueueCapacity;
        }

        public void setPendingRequestBlockingQueueCapacity(
                int pendingRequestBlockingQueueCapacity) {
            this.pendingRequestBlockingQueueCapacity = pendingRequestBlockingQueueCapacity;
        }

        public boolean isUsingMandatoryForRequestMessages() {
            return usingMandatoryForRequestMessages;
        }

        public void setUsingMandatoryForRequestMessages(boolean usingMandatoryForRequestMessages) {
            this.usingMandatoryForRequestMessages = usingMandatoryForRequestMessages;
        }

        public long getTimeoutWaitingForNextRequest() {
            return timeoutWaitingForNextRequest;
        }

        /**
         * The number of milliseconds to wait for the next request without checking for status.
         * If this timeout expires before the arrival of a new request, we first let
         * {@link de.marketmaker.itools.amqprpc.impl.AmqpRpcSupport} check whether this
         * AmqpRpcClient has been closed, in which case will we not wait for requests again.
         * <p/>
         * Otherwise, we immediately continue waiting for the next request. Using this timeout
         * makes sure we remain responsive, even if interrupting the mainThread fails on closing.
         * @param timeoutWaitingForNextRequest
         */
        public void setTimeoutWaitingForNextRequest(long timeoutWaitingForNextRequest) {
            this.timeoutWaitingForNextRequest = timeoutWaitingForNextRequest;
        }

        public void setResubmitWithoutProviderPreference(boolean resubmitWithoutProviderPreference) {
            this.resubmitWithoutProviderPreference = resubmitWithoutProviderPreference;
        }

        public boolean isResubmitWithoutProviderPreference() {
            return resubmitWithoutProviderPreference;
        }

        public String getExpiration() {
            return expiration;
        }

        public void setExpiration(String expiration) {
            this.expiration = expiration;
        }
    }

}
