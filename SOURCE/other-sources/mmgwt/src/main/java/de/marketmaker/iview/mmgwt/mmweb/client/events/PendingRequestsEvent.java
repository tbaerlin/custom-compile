/*
 * PendingRequestsEvent.java
 *
 * Created on 04.12.2009 09:36:43
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.events;

import com.google.gwt.event.shared.GwtEvent;

/**
 * Informs handlers about the number of currently pending service requests; handlers may
 * update busy indicators etc. based on this information.
 * 
 * @author oflege
 */
public class PendingRequestsEvent extends GwtEvent<PendingRequestsHandler>{

    private static Type<PendingRequestsHandler>TYPE;

    private final int numPending;
    private final int numPmPending;

    public static Type<PendingRequestsHandler> getType(){
        if (TYPE == null){
            TYPE = new Type<PendingRequestsHandler>();
        }
        return TYPE;
    }

    public PendingRequestsEvent(int numPending, int numPmPending) {
        this.numPending = numPending;
        this.numPmPending = numPmPending;
    }

    public int getNumPending() {
        return numPending;
    }

    public int getNumPmPending() {
        return numPmPending;
    }

    public Type<PendingRequestsHandler> getAssociatedType() {
        return getType();
    }

    protected void dispatch(PendingRequestsHandler handler) {
        handler.onPendingRequestsUpdate(this);
    }
}
