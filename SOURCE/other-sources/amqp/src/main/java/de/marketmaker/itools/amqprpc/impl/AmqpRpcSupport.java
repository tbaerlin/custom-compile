/*
 * AmqpRpcSupport.java
 *
 * Created on 11.03.2011 11:37:53
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.itools.amqprpc.impl;

import java.io.IOException;

import com.rabbitmq.client.AlreadyClosedException;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ShutdownListener;
import com.rabbitmq.client.ShutdownSignalException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.marketmaker.itools.amqprpc.connections.AmqpRpcAddress;
import de.marketmaker.itools.amqprpc.supervising.Supervisable;

/**
 * Abstract superclass for objects that
 * <ul>
 * <li>need access to a {@link com.rabbitmq.client.Channel} and an {@link de.marketmaker.itools.amqprpc.connections.AmqpRpcAddress}</li>
 * <li>have some recurring task to on this channel.</li>
 * </ul>
 * <p/>
 * {@link #channel} is initialized in the constructor, so subclasses can immediately configure it
 * to their needs.
 * <p/>
 * This class implements {@link de.marketmaker.itools.amqprpc.supervising.Supervisable}, so that its
 * status can be checked with {@link de.marketmaker.itools.amqprpc.supervising.Supervisable#everythingOk()}.
 * If the underlying channel was shut down, {@link #getCauseOfShutdown()} provides the cause for
 * the closeCurrentConnection action.
 * @author Sebastian Wild (s.wild@market-maker.de)
 */
abstract class AmqpRpcSupport implements Supervisable {
    protected static final String ERROR = "error/";

    protected final Log logger = LogFactory.getLog(getClass());

    protected final AmqpRpcAddress address;

    protected final Channel channel;

    protected volatile ShutdownSignalException causeOfShutdown;

    protected final String myName;

    protected AmqpRpcSupport(final Connection connection,
            final AmqpRpcAddress address, final String myName) throws IOException {
        this.address = address;
        this.myName = myName;
        this.channel = connection.createChannel();

        if (this.channel == null) {
            throw new IOException("createChannel returned null");
        }

        // ensure the cause of a shutdown is set
        this.channel.addShutdownListener(new ShutdownListener() {
            public void shutdownCompleted(ShutdownSignalException sse) {
                final String msg = sse.toString();
                if (!msg.contains("clean connection shutdown")) {
                    logger.info("<shutdownCompleted> for channel '" + address.getRequestQueue()
                            + "' with cause " + msg
                            + ", ref=" + sse.getReference()
                            + ", hardError=" + sse.isHardError()
                            + ", fromApp=" + sse.isInitiatedByApplication()
                    );
                }
                causeOfShutdown = sse;
            }
        });
    }

    /**
     * Close the channel, but do not throw exceptions if it fails
     */
    public void closeChannel() {
        try {
            if (this.channel.isOpen()) {
                this.channel.close();
            }
        } catch (AlreadyClosedException e) {
            // ignore
        } catch (Exception e) {
            this.logger.warn("<closeChannel> could not close channel for unexpected reason.", e);
        }
    }


    public String logMessageInCaseOfError() {
        // cause of closeCurrentConnection will be set, when this method is invoked.
        return myName + " was closed because of " + this.causeOfShutdown;
    }

    /**
     * @return when the underlying connection was shut down, this method returns the cause.
     *         Otherwise, returns null.
     */
    public ShutdownSignalException getCauseOfShutdown() {
        return causeOfShutdown;
    }

    @Override
    public boolean everythingOk() {
        return this.channel.isOpen();
    }
}
