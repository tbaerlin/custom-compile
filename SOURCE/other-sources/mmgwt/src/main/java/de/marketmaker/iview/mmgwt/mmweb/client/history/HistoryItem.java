package de.marketmaker.iview.mmgwt.mmweb.client.history;

import com.google.gwt.safehtml.shared.SafeHtml;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PlaceChangeEvent;

/**
 * Created on 13.11.12 09:12
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author Michael Lösch
 */

public class HistoryItem {
    private final Integer hid;
    private final PlaceChangeEvent placeChangeEvent;
    private boolean breadCrumb;
    private SafeHtml debugHint;

    private HistoryItem(PlaceChangeEvent event) {
        this.placeChangeEvent = event;
        this.hid = event.getHistoryToken().getHistoryId();
    }

    static HistoryItem createItem(PlaceChangeEvent event) {
        return new HistoryItem(event);
    }

    public Integer getHid() {
        return this.hid;
    }

    @Override
    public String toString() {
        return "HistoryItem{" + // $NON-NLS$
                "hid=" + hid + // $NON-NLS$
                ", debugHint=" + debugHint + // $NON-NLS$
                ", placeChangeEvent=" + placeChangeEvent + // $NON-NLS$
                '}';
    }

    void setDebugHint(SafeHtml debugHint) {
        this.debugHint = debugHint;
    }

    SafeHtml getDebugHint() {
        return this.debugHint;
    }

    public PlaceChangeEvent getPlaceChangeEvent() {
        return this.placeChangeEvent;
    }

    public void markAsBreadCrumb() {
        this.breadCrumb = true;
    }

    public void markAsNotBreadCrumb() {
        this.breadCrumb = false;
    }

    public boolean isBreadCrumb() {
        return breadCrumb;
    }
}