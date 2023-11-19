/*
 * HTMLWithLinks.java
 *
 * Created on 29.06.2009 13:43:00
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.view;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.EventListener;
import com.google.gwt.user.client.ui.HTML;

import de.marketmaker.iview.mmgwt.mmweb.client.util.LinkManager;

/**
 * HTML with embedded links that should be handled by a specific EventListener.
 * This class makes sure that the EventListener will be registered as soon as this
 * widget is loaded and unregistered again on unload.
 * 
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class HTMLWithLinks extends HTML {
    private final EventListener listener;

    public HTMLWithLinks(String s, LinkManager linkManager) {
        this(s, true, linkManager);
    }

    public HTMLWithLinks(String s, boolean b, LinkManager linkManager) {
        this(s, b, linkManager.getEventListener());
    }

    public HTMLWithLinks(String s, EventListener listener) {
        this(s, true, listener);
    }

    public HTMLWithLinks(String s, boolean b, EventListener listener) {
        super(s, b);
        this.listener = listener;
    }

    @Override
    protected void onLoad() {
        super.onLoad();
        DOM.sinkEvents(getElement(), Event.ONCLICK | DOM.getEventsSunk(getElement()));
        DOM.setEventListener(getElement(), this.listener);
    }

    @Override
    protected void onUnload() {
        super.onUnload();
        DOM.setEventListener(getElement(), null);
    }    
}
