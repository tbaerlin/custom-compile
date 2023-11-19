/*
 * RequestCompletedEvent.java
 *
 * Created on 04.12.2009 09:36:43
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.events;

import com.google.gwt.event.shared.GwtEvent;

import de.marketmaker.iview.mmgwt.mmweb.client.MmwebResponse;

/**
 * Informs handlers about an MmwebResponse that was just received. Fired <b>before</b> any other
 * response handling is done. Only fired for successful responses.
 *
 * @author oflege
 */
public class ResponseReceivedEvent extends GwtEvent<ResponseReceivedHandler>{

    private static Type<ResponseReceivedHandler>TYPE;

    private final MmwebResponse response;

    public static Type<ResponseReceivedHandler> getType(){
        if (TYPE == null){
            TYPE = new Type<ResponseReceivedHandler>();
        }
        return TYPE;
    }

    public ResponseReceivedEvent(MmwebResponse response) {
        this.response = response;
    }

    public MmwebResponse getResponse() {
        return this.response;
    }

    public Type<ResponseReceivedHandler> getAssociatedType() {
        return getType();
    }

    protected void dispatch(ResponseReceivedHandler handler) {
        handler.onResponseReceived(this);
    }
}
