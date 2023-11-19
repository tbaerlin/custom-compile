/*
 * LogoutEvent.java
 *
 * Created on 10.01.2011 17:27:31
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.dmxmldocu.client.event;

import com.google.gwt.event.shared.GwtEvent;

/**
 * @author oflege
 */
public class LogoutEvent extends GwtEvent<LogoutHandler> {
    public static Type<LogoutHandler> TYPE = new Type<LogoutHandler>();

    public Type<LogoutHandler> getAssociatedType() {
        return TYPE;
    }

    protected void dispatch(LogoutHandler logoutHandler) {
        logoutHandler.onLogout();
    }
}
