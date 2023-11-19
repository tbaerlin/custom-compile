/*
 * UserListUpdatedHandler.java
 *
 * Created on 04.12.2009 09:37:11
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.events;

import com.google.gwt.event.shared.EventHandler;

/**
 * @author oflege
 */
public interface UserListUpdatedHandler extends EventHandler {
    void onUpdate(UserListUpdatedEvent event);
}
