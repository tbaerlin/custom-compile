/*
 * AmqpProblemEvent.java
 *
 * Created on 05.04.11 13:31
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.itools.amqprpc.supervising;

import org.springframework.context.ApplicationEvent;

/**
 * @author oflege
 */
public class AmqpProblemEvent extends ApplicationEvent {
    private static final long serialVersionUID = -5471207541251050145L;

    public AmqpProblemEvent(Object source) {
        super(source);
    }
}
