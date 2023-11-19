/*
 * RefreshDmxmlContextEvent.java
 *
 * Created on 01.08.2016 14:31
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.events;

import com.google.gwt.event.shared.GwtEvent;

/**
 * Informs handlers that the user triggered a dm[xml] context refresh.
 * @author Markus Dick
 */
public class RefreshDmxmlContextEvent extends GwtEvent<RefreshDmxmlContextHandler> {
    private static Type<RefreshDmxmlContextHandler> TYPE;

    public RefreshDmxmlContextEvent() {
    }

    public static Type<RefreshDmxmlContextHandler> getType() {
        if (TYPE == null) {
            TYPE = new Type<>();
        }
        return TYPE;
    }

    public Type<RefreshDmxmlContextHandler> getAssociatedType() {
        return TYPE;
    }

    protected void dispatch(RefreshDmxmlContextHandler handler) {
        handler.onRefreshDmxmlContext(this);
    }
}
