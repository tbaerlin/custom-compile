/*
 * AmqpDeclarationFailureException.java
 *
 * Created on 04.03.2011 16:54:58
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.itools.amqprpc.connections;

import org.springframework.remoting.RemoteAccessException;

/**
 * This exception is thrown if an exchange or a queue should be declared, but this declaration
 * failed for some reason. Typical causes for this can be
 * <ul>
 * <li>the RabbitMQ user has no rights to declare the entity</li>
 * <li>some entity of the same name already exists, but with incompatible settings.</li>
 * </ul>
 * @author Sebastian Wild (s.wild@market-maker.de)
 */
public class AmqpDeclarationFailureException extends RemoteAccessException {
    private static final long serialVersionUID = -419522130903231985L;

    public AmqpDeclarationFailureException(String msg) {
        super(msg);
    }

    public AmqpDeclarationFailureException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
