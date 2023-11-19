/*
 * PricesUpdatedEvent.java
 *
 * Created on 02.02.2010 12:55:10
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.events;

import com.google.gwt.event.shared.GwtEvent;

/**
 * Marker event that informs handlers about an update of prices in the
 * {@link de.marketmaker.iview.mmgwt.mmweb.client.prices.PriceStore}.
 * @author oflege
 */
public class PricesUpdatedEvent extends GwtEvent<PricesUpdatedHandler> {
    private static Type<PricesUpdatedHandler> TYPE;

    public static Type<PricesUpdatedHandler> getType() {
        if (TYPE == null) {
            TYPE = new Type<PricesUpdatedHandler>();
        }
        return TYPE;
    }

    private final boolean pushedUpdate;

    public PricesUpdatedEvent(boolean pushedUpdate) {
        this.pushedUpdate = pushedUpdate;
    }

    public Type<PricesUpdatedHandler> getAssociatedType() {
        return TYPE;
    }

    protected void dispatch(PricesUpdatedHandler handler) {
        handler.onPricesUpdated(this);
    }

    public boolean isPushedUpdate() {
        return this.pushedUpdate;
    }
}
