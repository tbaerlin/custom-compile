/*
 * AmqpConnectionShutdownException.java
 *
 * Created on 04.03.2011 17:31:10
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.itools.amqprpc.connections;

import org.springframework.remoting.RemoteAccessException;

/**
 * This method is thrown if an AMQP connection to the broker was unexpectedly closed.
 * @author Sebastian Wild (s.wild@market-maker.de)
 */
public class AmqpConnectionShutdownException extends RemoteAccessException {
    private static final long serialVersionUID = 1941547140344253454L;

    public AmqpConnectionShutdownException(String msg) {
        super(msg);
    }

    public AmqpConnectionShutdownException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
