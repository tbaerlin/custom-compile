/*
 * AmqpRpcClientPendingRequestCapacityExceeded.java
 *
 * Created on 10.03.2011 13:06:01
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.itools.amqprpc.impl;

import org.springframework.remoting.RemoteAccessException;

/**
 * This exception is thrown, when an {@link AmqpRpcClient}
 * received so many requests, that it was not able to send them fast enough.
 * @author Sebastian Wild (s.wild@market-maker.de)
 */
public class AmqpRpcClientPendingRequestBufferOverflow extends RemoteAccessException {
    private static final long serialVersionUID = 6508580108173297653L;

    public AmqpRpcClientPendingRequestBufferOverflow(String msg) {
        super(msg);
    }

    public AmqpRpcClientPendingRequestBufferOverflow(String msg, Throwable cause) {
        super(msg, cause);
    }
}
