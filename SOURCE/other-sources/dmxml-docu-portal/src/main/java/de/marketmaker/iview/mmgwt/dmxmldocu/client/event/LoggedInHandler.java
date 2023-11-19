/*
 * LoggedInHandler.java
 *
 * Created on 10.01.2011 16:55:18
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.dmxmldocu.client.event;

import com.google.gwt.event.shared.EventHandler;
import de.marketmaker.iview.mmgwt.dmxmldocu.client.data.User;

/**
 * @author oflege
 */
public interface LoggedInHandler extends EventHandler {
    void onLoggedIn(User u);
}
