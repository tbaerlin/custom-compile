/*
 * BrowserSpecificGecko.java
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.util;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.IFrameElement;
import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.EventListener;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * @author Markus Dick
 */
public class BrowserSpecificGecko extends BrowserSpecific {
    public static final String TEST_TOKEN = "#25%25";  // $NON-NLS$

    private boolean needsHistoryTokenDecoding = false;

    @Override
    public void initialize(final Command finished) {
        final IFrameElement iFrame = DOM.createIFrame().cast();
        iFrame.getStyle().setVisibility(Style.Visibility.HIDDEN);
        RootPanel.get().getElement().insertFirst(iFrame);
        Event.sinkEvents(iFrame, Event.ONLOAD);
        Event.setEventListener(iFrame, new EventListener() {
            @Override
            public void onBrowserEvent(Event event) {
                needsHistoryTokenDecoding = TEST_TOKEN.equals(getHash(iFrame));
                RootPanel.get().getElement().removeChild(iFrame);
                finished.execute();
            }
        });
        iFrame.setSrc("historyEncodingCheck.html" + TEST_TOKEN);  // $NON-NLS$
    }

    private native String getHash(IFrameElement iFrameElement) /*-{
        return iFrameElement.contentDocument.location.hash
    }-*/;

    @Override
    public boolean isNeedsHistoryTokenDecoding() {
        return this.needsHistoryTokenDecoding;
    }

    @Override
    public int getScrollTopMax(Element element) {
        return element.getPropertyInt("scrollTopMax"); //$NON-NLS$
    }
}
