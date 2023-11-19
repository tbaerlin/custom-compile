/*
 * EndOfDayRulesChangedEvent.java
 *
 * Created on 06.08.15 07:56
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.vwd;

import org.springframework.context.ApplicationEvent;

/**
 * This event will be published whenever an {@link EndOfDayProvider} has read an updated
 * configuration file.
 *
 * @author oflege
 */
class EndOfDayRulesChangedEvent extends ApplicationEvent {
    EndOfDayRulesChangedEvent(Object source) {
        super(source);
    }
}
