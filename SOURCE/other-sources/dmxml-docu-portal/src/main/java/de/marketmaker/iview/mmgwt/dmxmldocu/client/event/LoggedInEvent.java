/*
 * LoggedInEvent.java
 *
 * Created on 10.01.2011 16:53:57
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.dmxmldocu.client.event;

import com.google.gwt.event.shared.GwtEvent;
import de.marketmaker.iview.mmgwt.dmxmldocu.client.data.User;


/**
 * @author oflege
 */
public class LoggedInEvent extends GwtEvent<LoggedInHandler> {
    public static Type<LoggedInHandler> TYPE = new Type<LoggedInHandler>();

    private User user;

    public LoggedInEvent(User user) {
        this.user = user;
    }

    public Type<LoggedInHandler> getAssociatedType() {
        return TYPE;
    }

    protected void dispatch(LoggedInHandler loggedInHandler) {
        loggedInHandler.onLoggedIn(this.user);
    }
}
