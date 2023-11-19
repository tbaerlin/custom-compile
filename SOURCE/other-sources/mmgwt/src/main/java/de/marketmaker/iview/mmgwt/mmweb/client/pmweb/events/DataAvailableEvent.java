package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.events;

import com.google.gwt.event.shared.GwtEvent;

/**
 * Created on 26.05.2010 09:14:10
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author Michael Lösch
 */
public class DataAvailableEvent extends GwtEvent<DataAvailableHandler> {

    private static Type<DataAvailableHandler> TYPE = new Type<DataAvailableHandler>();

    public static Type<DataAvailableHandler> getType() {
        return TYPE;
    }

    public DataAvailableEvent() {
    }

    @Override
    public Type<DataAvailableHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(DataAvailableHandler handler) {
        handler.onDataAvailable(this);
    }
}