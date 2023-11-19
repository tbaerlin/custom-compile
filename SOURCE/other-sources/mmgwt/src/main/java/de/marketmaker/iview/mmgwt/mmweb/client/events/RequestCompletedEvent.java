/*
 * RequestCompletedEvent.java
 *
 * Created on 04.12.2009 09:36:43
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.events;

import com.google.gwt.event.shared.GwtEvent;

import de.marketmaker.iview.mmgwt.mmweb.client.statistics.RequestStatistics;
import de.marketmaker.iview.mmgwt.mmweb.client.MmwebResponse;

/**
 * Informs handlers about the number of currently pending service requests; handlers may
 * update busy indicators etc. based on this information.
 * 
 * @author oflege
 */
public class RequestCompletedEvent extends GwtEvent<RequestCompletedHandler>{

    private static Type<RequestCompletedHandler>TYPE;

    private final RequestStatistics statistics;

    private final MmwebResponse response;

    public static Type<RequestCompletedHandler> getType(){
        if (TYPE == null){
            TYPE = new Type<RequestCompletedHandler>();
        }
        return TYPE;
    }

    public RequestCompletedEvent() {
        this.statistics = null;
        this.response = null;
    }

    public RequestCompletedEvent(MmwebResponse response, RequestStatistics statistics) {
        this.response = response;
        this.statistics = statistics;
    }

    public RequestStatistics getStatistics() {
        return this.statistics;
    }

    public MmwebResponse getResponse() {
        return this.response;
    }

    public boolean isSuccessful() {
        return this.statistics != null && !(this.statistics.isCancelled() || this.statistics.isFailed());
    }

    public Type<RequestCompletedHandler> getAssociatedType() {
        return getType();
    }

    protected void dispatch(RequestCompletedHandler handler) {
        handler.onRequestCompleted(this);
    }
}
