/*
 * ShutdownEvent.java
 *
 * Created on 28.04.2014 15:06
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.events;

import com.google.gwt.event.shared.GwtEvent;

/**
 * Informs registered handlers about imminent app shutdown, e.g.,
 * a) when the browser/browser tab is being closed or
 * b) when the logout button was clicked
 * @author Markus Dick
 */
public class ShutdownEvent extends GwtEvent<ShutdownHandler> {
    private static Type<ShutdownHandler> TYPE;

    private final boolean clearUrl;

    private final boolean storeSession;

    public ShutdownEvent(boolean storeSession, boolean clearUrl) {
        this.storeSession = storeSession;
        this.clearUrl = clearUrl;
    }

    public static Type<ShutdownHandler> getType() {
        if (TYPE == null) {
            TYPE = new Type<>();
        }
        return TYPE;
    }

    public Type<ShutdownHandler> getAssociatedType() {
        return TYPE;
    }

    protected void dispatch(ShutdownHandler shutdownHandler) {
        shutdownHandler.onShutdown(this);
    }

    public boolean isClearUrl() {
        return clearUrl;
    }

    public boolean isStoreSession() {
        return storeSession;
    }
}
