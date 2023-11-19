/*
 * UserListUpdatedEvent.java
 *
 * Created on 04.12.2009 15:02:10
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.events;

import com.google.gwt.event.shared.GwtEvent;

/**
 * Informs handlers about changes in the user's lists (portfolios and watchlists).
 * @author oflege
 */
public class UserListUpdatedEvent extends GwtEvent<UserListUpdatedHandler> {

    private static Type<UserListUpdatedHandler> TYPE;

    public static Type<UserListUpdatedHandler> getType() {
        if (TYPE == null) {
            TYPE = new Type<UserListUpdatedHandler>();
        }
        return TYPE;
    }

    private final boolean watchlists;

    private final boolean structureChanged;

    public static UserListUpdatedEvent forWatchlists(boolean structureChanged) {
        return new UserListUpdatedEvent(true, structureChanged);
    }

    public static UserListUpdatedEvent forPortfolios(boolean structureChanged) {
        return new UserListUpdatedEvent(false, structureChanged);
    }

    private UserListUpdatedEvent(boolean watchlists, boolean structureChanged) {
        this.watchlists = watchlists;
        this.structureChanged = structureChanged;
    }

    public boolean isWatchlists() {
        return this.watchlists;
    }

    public boolean isStructureChanged() {
        return this.structureChanged;
    }

    public Type<UserListUpdatedHandler> getAssociatedType() {
        return TYPE;
    }

    protected void dispatch(UserListUpdatedHandler configChangedHandler) {
        configChangedHandler.onUpdate(this);
    }
}
