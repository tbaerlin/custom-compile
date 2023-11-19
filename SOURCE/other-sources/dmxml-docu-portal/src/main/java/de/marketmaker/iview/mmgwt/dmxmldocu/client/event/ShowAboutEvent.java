/*
 * ShowAboutEvent.java
 *
 * Created on 18.09.2012 09:51
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.dmxmldocu.client.event;

import com.google.gwt.event.shared.GwtEvent;

/**
 * @author Markus Dick
 */
public class ShowAboutEvent extends GwtEvent<ShowAboutHandler> {
    public static Type<ShowAboutHandler> TYPE = new Type<ShowAboutHandler>();

    public ShowAboutEvent() {
        super();
    }

    public Type<ShowAboutHandler> getAssociatedType() {
        return TYPE;
    }

    protected void dispatch(ShowAboutHandler showAboutHandler) {
        showAboutHandler.onShowAbout();
    }
}
