/*
 * LimitsUpdateEvent.java
 *
 * Created on 04.12.2009 15:02:10
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.events;

import com.google.gwt.event.shared.GwtEvent;

/**
 * Marker event that informs handlers about an update of the limits. The updated limit information
 * is available from {@link de.marketmaker.iview.mmgwt.mmweb.client.AlertController#INSTANCE}.
 * @author oflege
 */
public class LimitsUpdatedEvent extends GwtEvent<LimitsUpdatedHandler> {
    private static Type<LimitsUpdatedHandler> TYPE;

    public static Type<LimitsUpdatedHandler> getType() {
        if (TYPE == null) {
            TYPE = new Type<LimitsUpdatedHandler>();
        }
        return TYPE;
    }

    public Type<LimitsUpdatedHandler> getAssociatedType() {
        return TYPE;
    }

    protected void dispatch(LimitsUpdatedHandler handler) {
        handler.onLimitsUpdated(this);
    }
}
