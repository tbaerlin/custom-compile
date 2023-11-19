/*
 * ActionPerformedEvent.java
 *
 * Created on 04.12.2009 15:02:10
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.events;

import com.google.gwt.event.shared.GwtEvent;

/**
 * Event that informs handlers about certain action that has been performed.
 * @author oflege
 */
public class ActionPerformedEvent extends GwtEvent<ActionPerformedHandler> {
    private static Type<ActionPerformedHandler> TYPE;

    private final String key;

    public static Type<ActionPerformedHandler> getType() {
        if (TYPE == null) {
            TYPE = new Type<ActionPerformedHandler>();
        }
        return TYPE;
    }

    public static void fire(String key) {
        EventBusRegistry.get().fireEvent(new ActionPerformedEvent(key));
    }

    public ActionPerformedEvent(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    public Type<ActionPerformedHandler> getAssociatedType() {
        return TYPE;
    }

    protected void dispatch(ActionPerformedHandler handler) {
        handler.onAction(this);
    }
}
