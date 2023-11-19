/*
 * LogoutEventHandler.java
 *
 * Created on 10.01.2011 17:27:48
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.dmxmldocu.client.event;

import com.google.gwt.event.shared.EventHandler;


/**
 * @author oflege
 */
public interface LogoutHandler extends EventHandler {
    void onLogout();
}
