/*
 * BeforeRequestEvent.java
 *
 * Created on 04.12.2009 09:36:43
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.events;

import com.google.gwt.event.shared.GwtEvent;

import de.marketmaker.iview.mmgwt.mmweb.client.MmwebRequest;

/**
 * Informs handlers about a request that will be sent to the server as soon as the event
 * handling completes.
 * 
 * @author oflege
 */
public class BeforeRequestEvent extends GwtEvent<BeforeRequestHandler>{

    private static Type<BeforeRequestHandler>TYPE;

    private final MmwebRequest request;

    public static Type<BeforeRequestHandler> getType(){
        if (TYPE == null){
            TYPE = new Type<BeforeRequestHandler>();
        }
        return TYPE;
    }

    public BeforeRequestEvent(MmwebRequest request) {
        this.request = request;
    }

    public MmwebRequest getRequest() {
        return this.request;
    }

    public Type<BeforeRequestHandler> getAssociatedType() {
        return getType();
    }

    protected void dispatch(BeforeRequestHandler handler) {
        handler.onBeforeRequest(this);
    }
}
