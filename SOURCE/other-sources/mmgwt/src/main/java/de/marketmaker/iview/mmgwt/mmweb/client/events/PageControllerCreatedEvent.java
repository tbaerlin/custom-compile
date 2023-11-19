package de.marketmaker.iview.mmgwt.mmweb.client.events;

import com.google.gwt.event.shared.GwtEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.AbstractPageController;

/**
 * Created on 31.10.12 09:25
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author Michael Lösch
 */

public class PageControllerCreatedEvent extends GwtEvent<PageControllerCreatedHandler> {
    private static Type<PageControllerCreatedHandler> TYPE;
    private final AbstractPageController pageController;
    private final String token;

    public static Type<PageControllerCreatedHandler> getType() {
        if (TYPE == null) {
            TYPE = new Type<PageControllerCreatedHandler>();
        }
        return TYPE;
    }

    public PageControllerCreatedEvent(AbstractPageController pageController, String token) {
        this.pageController = pageController;
        this.token = token;
    }

    public AbstractPageController getPageController() {
        return pageController;
    }

    public String getToken() {
        return token;
    }

    public Type<PageControllerCreatedHandler> getAssociatedType() {
        return TYPE;
    }

    protected void dispatch(PageControllerCreatedHandler handler) {
        handler.afterCreated(this);
    }

}
