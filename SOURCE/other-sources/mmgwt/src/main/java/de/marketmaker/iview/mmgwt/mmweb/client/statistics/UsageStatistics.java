/*
 * UsageStatistics.java
 *
 * Created on 07.12.2009 11:40:02
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.statistics;

import java.util.ArrayList;

import com.google.gwt.event.shared.HandlerManager;

import de.marketmaker.iview.mmgwt.mmweb.client.events.ActionPerformedEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.events.ActionPerformedHandler;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PlaceChangeHandler;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PlaceChangeEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.events.RequestCompletedHandler;
import de.marketmaker.iview.mmgwt.mmweb.client.events.RequestCompletedEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.events.BeforeRequestHandler;
import de.marketmaker.iview.mmgwt.mmweb.client.events.BeforeRequestEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.history.HistoryToken;

/**
 * Captures usage statistics and puts that information into MmwebRequests so that it is
 * submitted to the server.
 * @author oflege
 */
public class UsageStatistics implements PlaceChangeHandler, RequestCompletedHandler,
        BeforeRequestHandler, ActionPerformedHandler {
    private ArrayList<PlaceStatistics> recentPlaces = new ArrayList<PlaceStatistics>();

    private PlaceStatistics currentPlace = new PlaceStatistics(""); // $NON-NLS-0$

    public UsageStatistics(HandlerManager eventBus) {
        eventBus.addHandler(ActionPerformedEvent.getType(), this);
        eventBus.addHandler(BeforeRequestEvent.getType(), this);
        eventBus.addHandler(PlaceChangeEvent.getType(), this);
        eventBus.addHandler(RequestCompletedEvent.getType(), this);
    }

    public void onPlaceChange(PlaceChangeEvent event) {
        final HistoryToken historyToken = event.getHistoryToken();
        final String cId = historyToken.getControllerId();
        //noinspection EqualsBetweenInconvertibleTypes
        if (event.getHistoryToken().equals(this.currentPlace.getHistoryToken())
                || (cId == null) || (cId.startsWith("STA_"))) { // $NON-NLS-0$
            return; // nothing changed or statistics
        }
        this.recentPlaces.add(this.currentPlace);
        this.currentPlace = new PlaceStatistics(historyToken.toString());
    }

    public void onRequestCompleted(RequestCompletedEvent event) {
        final RequestStatistics s = event.getStatistics();
        if (s != null) {
            this.currentPlace.ackRequest(s.getNumBlocks(), (int)(s.getRequestTime()), (int)s.getProcessTime());
        }
    }

    public void onBeforeRequest(BeforeRequestEvent event) {
        if (!this.recentPlaces.isEmpty()) {
            event.getRequest().setUsageStatistics(this.recentPlaces);
            this.recentPlaces = new ArrayList<PlaceStatistics>();
        }
    }

    public void onAction(ActionPerformedEvent event) {
        this.recentPlaces.add(new PlaceStatistics(event.getKey()));
    }
}
