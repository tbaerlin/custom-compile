/*
 * ServiceProviderSelection.java
 *
 * Created on 19.12.11 15:07
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.common.amqp;

/**
 * @author oflege
 */
public class ServiceProviderSelection {

    /**
     * The service provider ID stored here will be used to send the <em>next</em> request
     * issued from this thread preferably to the service provider with this ID. If {@code null},
     * requests are sent to the general queue from where any available provider can process
     * the request.
     *
     * If (immediate) delivery fails, the request is automatically resent to the general queue,
     * in which case no guarantees can be made as to which provider will process the request.
     *
     *
     * <b>No matter whether the delivery to the specified service provider succeeded or not will
     * this be re-set to {@code null}, after the message has been sent.</b>
     * This means you have to provide this ID each time before you sent a new request.
     */
    public static final ThreadLocal<String> ID_FOR_NEXT_SEND = new ThreadLocal<>();

    /**
     * AMQP-RPC will store the ID of the service provider that the last reply was sent from to
     * a request issued by the current thread.
     */
    public static final ThreadLocal<String> ID_FROM_LAST_REPLY = new ThreadLocal<>();

    /**
     * Shortcut method to send the next request to the server that answered the last request.
     */
    public static void useLastProviderAgain() {
        ID_FOR_NEXT_SEND.set(ID_FROM_LAST_REPLY.get());
    }

    /**
     * Returns the current value of {@link #ID_FOR_NEXT_SEND} and also removes the current value.
     * @return current value of {@link #ID_FOR_NEXT_SEND}
     */
    public static String getAndClearIdForNextSend() {
        final String result = ID_FOR_NEXT_SEND.get();
        if (result != null) {
            ID_FOR_NEXT_SEND.remove();
        }
        return result;
    }
}
