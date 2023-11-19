/*
 * FeedConnectionEvent.java
 *
 * Created on 12.06.12 08:50
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.connect;

import org.springframework.context.ApplicationEvent;

/**
 * An event that will be published whenever a connection to a feed source has been established
 * or that connection has been closed.
 * @author oflege
 */
public class FeedConnectionEvent extends ApplicationEvent {

    private final boolean connected;

    public FeedConnectionEvent(Object source, boolean connected) {
        super(source);
        this.connected = connected;
    }

    public boolean isConnected() {
        return this.connected;
    }
}
