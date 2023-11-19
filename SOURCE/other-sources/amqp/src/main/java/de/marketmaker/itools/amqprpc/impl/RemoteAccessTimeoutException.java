/*
 * RemoteAccessTimeoutException.java
 *
 * Created on 04.03.2011 17:26:58
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.itools.amqprpc.impl;

import org.springframework.remoting.RemoteAccessException;

/**
 * This exception is thrown, if a timeout is hit while some remote method invocation was running.
 *
 * @author Sebastian Wild (s.wild@market-maker.de)
 */
public class RemoteAccessTimeoutException extends RemoteAccessException {
    private static final long serialVersionUID = -2066527006998006058L;

    public RemoteAccessTimeoutException(String msg) {
        super(msg);
    }

    public RemoteAccessTimeoutException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
