/*
 * MidnightEvent.java
 *
 * Created on 29.08.15 06:49
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.common.spring;

import org.springframework.context.ApplicationEvent;

/**
 * @author oflege
 */
public class MidnightEvent extends ApplicationEvent {

    /**
     * Create a new ApplicationEvent.
     * @param source the component that published the event (never {@code null})
     */
    public MidnightEvent(Object source) {
        super(source);
    }
}
