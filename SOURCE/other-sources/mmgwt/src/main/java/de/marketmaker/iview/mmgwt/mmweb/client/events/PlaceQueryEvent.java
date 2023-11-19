/*
 * GoToPageEvent.java
 *
 * Created on 04.12.2009 09:36:43
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.events;

import com.google.gwt.event.shared.GwtEvent;

/**
 * Use {@link #getCurrentPlace()} to query the current history token. Useful if a client does not
 * have a reference to a {@link de.marketmaker.iview.mmgwt.mmweb.client.events.PlaceManager}.
 * @author oflege
 */
public class PlaceQueryEvent extends GwtEvent<PlaceQueryHandler>{

    private static Type<PlaceQueryHandler>TYPE;

    private String place;

    public static Type<PlaceQueryHandler> getType(){
        if (TYPE == null){
            TYPE = new Type<PlaceQueryHandler>();
        }
        return TYPE;
    }

    public static String getCurrentPlace() {
        final PlaceQueryEvent event = new PlaceQueryEvent();
        EventBusRegistry.get().fireEvent(event);
        return event.getPlace();
    }

    private PlaceQueryEvent() {
    }

    public Type<PlaceQueryHandler> getAssociatedType() {
        return getType();
    }

    protected void dispatch(PlaceQueryHandler handler) {
        handler.onPlaceQuery(this);
    }

    public void setPlace(String token) {
        this.place = token;
    }

    private String getPlace() {
        return this.place;
    }
}
