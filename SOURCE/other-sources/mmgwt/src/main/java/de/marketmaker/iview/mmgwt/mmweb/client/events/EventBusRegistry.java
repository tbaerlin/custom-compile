/*
 * EventBusRegistry.java
 *
 * Created on 04.12.2009 11:08:32
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.events;

import com.google.gwt.event.shared.HandlerManager;

/**
 * This is where components can look up the eventBus. The preferred way of obtaining the eventBus
 * reference would be with constructor injection; use this registry if that is not feasible.
 * @author oflege
 */
public class EventBusRegistry {
    private static HandlerManager eventBus;

    public static boolean isSet() {
        return eventBus != null;
    }

    public static HandlerManager get() {
        if (EventBusRegistry.eventBus == null) {
            throw new IllegalStateException("eventBus not set"); // $NON-NLS-0$
        }
        return eventBus;
    }

    public static void set(HandlerManager eventBus) {
        if (EventBusRegistry.eventBus != null) {
            throw new IllegalStateException("eventBus already set"); // $NON-NLS-0$
        }
        EventBusRegistry.eventBus = eventBus;
    }
}
