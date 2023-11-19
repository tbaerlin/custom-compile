/*
 * PlacesService.java
 *
 * Created on 04.12.2009 09:32:57
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.history;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.History;
import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PlaceChangeEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PlaceManager;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PlaceQueryEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PlaceQueryHandler;

/**
 * Bridge between {@link com.google.gwt.user.client.History}-related events and the
 * {@link de.marketmaker.iview.mmgwt.mmweb.client.events.PlaceChangeEvent}s that appear on the
 * eventBus: History changes trigger events, events will be "stored" as new History items.
 * @author oflege
 */
public class HistoryPlaceManager implements PlaceManager, PlaceQueryHandler {

    private final HandlerManager eventBus;

    private HistoryToken pendingHistoryToken;

    private final HistoryThreadManager historyThreadManager;

    private boolean checkForEarliestHid = false;
    private Integer earliestHid;
    private String defaultTokenToGo;

    public HistoryPlaceManager(HandlerManager eventBus, HistoryThreadManager historyThreadManager) {
        History.addValueChangeHandler(this);
        this.historyThreadManager = historyThreadManager;
        this.eventBus = eventBus;
        this.eventBus.addHandler(PlaceChangeEvent.getType(), this);
        this.eventBus.addHandler(PlaceQueryEvent.getType(), this);
    }

    public void onPlaceQuery(PlaceQueryEvent event) {
        event.setPlace(getCurrentPlace());
    }

    public void onValueChange(ValueChangeEvent<String> e) {
        final HistoryToken token = HistoryToken.fromToken(e.getValue());
        if(this.checkForEarliestHid && this.earliestHid.compareTo(token.getHistoryId()) > 0) {
            Firebug.warn("HID " + token.getHistoryId() + " is not allowed!");
            fireEvent(this.defaultTokenToGo);
        }
        else {
            fireEvent(e.getValue());
        }
    }

    public void setCheckForEarliestHid(int earliestHid, String defaultTokenToGo) {
        assert defaultTokenToGo != null && !defaultTokenToGo.isEmpty();

        this.checkForEarliestHid = true;
        this.earliestHid = earliestHid;
        this.defaultTokenToGo = defaultTokenToGo;
    }

    public void resetCheckForEarliestHid() {
        this.checkForEarliestHid = false;
        this.earliestHid = null;
        this.defaultTokenToGo = null;
    }

    private void fireEvent(final String value) {
        this.eventBus.fireEvent(new PlaceChangeEvent(value));
    }

    public void onPlaceChange(final PlaceChangeEvent event) {
        //if app is started with old historyId (or somebody is doing evil things), this id is invalid
        if (this.historyThreadManager.hasInvalidHistoryId(event)) {
            return;
        }

        final HistoryToken historyToken = event.getHistoryToken();
        if ("M_S".equals(historyToken.getControllerId())) { // $NON-NLS$
            this.pendingHistoryToken = historyToken;
            return;
        }

        Firebug.debug("HistoryPlaceManager <onPlaceChange> get history token");
        final String token = historyToken.toStringWithHid();
        Firebug.debug("HistoryPlaceManager <onPlaceChange> add new History Item: " + token);
        History.newItem(token, false);
    }

    public void addPendingHistoryToken() {
        if (this.pendingHistoryToken != null) {
            Firebug.debug("HistoryPlaceManager <addPendingHistoryToken> add new History Item: " + this.pendingHistoryToken);
            History.newItem(this.pendingHistoryToken.toStringWithHid(), false);
            this.pendingHistoryToken = null;
        }
    }

    public void fireCurrentPlace(String defaultPlace) {
        final String s = History.getToken();
        fireEvent(s.length() > 0 ? s : defaultPlace);
    }

    public String getCurrentPlace() {
        return History.getToken();
    }
}
