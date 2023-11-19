/*
 * LazyAbstractPageController.java
 *
 * Created on 31.10.2012
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client;

import de.marketmaker.iview.mmgwt.mmweb.client.events.EventBusRegistry;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PageControllerCreatedEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PageControllerCreatedHandler;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PlaceChangeEvent;

/**
 * @author Michael Lösch
 */
public abstract class LazyAbstractPageController extends AbstractPageController implements PageControllerCreatedHandler {
    private AbstractPageController delegate;
    private final String token;

    public LazyAbstractPageController(String token) {
        super();
        EventBusRegistry.get().addHandler(PageControllerCreatedEvent.getType(), this);
        this.token = token;
    }

    public void refresh() {
        if (this.delegate != null) {
            this.delegate.refresh();
        }
    }

    public void onPlaceChange(PlaceChangeEvent event) {
        if (this.delegate == null) {
            init(event);
        }
        else {
            this.delegate.onPlaceChange(event);
        }
    }

    protected abstract void init(final PlaceChangeEvent event);

    @Override
    public void activate() {
        if (this.delegate != null) {
            this.delegate.activate();
        }
    }

    @Override
    public void deactivate() {
        if (this.delegate != null) {
            this.delegate.deactivate();
        }
    }

    public String getPrintHtml() {
        if (this.delegate == null) {
            return null;
        }
        return this.delegate.getPrintHtml();
    }

    @Override
    public void afterCreated(PageControllerCreatedEvent event) {
        if (this.token.equals(event.getToken()) && this.delegate == null) {
            this.delegate = event.getPageController();
        }
    }
}