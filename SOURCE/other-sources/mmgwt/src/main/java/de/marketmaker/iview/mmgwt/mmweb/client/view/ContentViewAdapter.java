/*
 * ContentViewAdapter.java
 *
 * Created on 02.09.2008 11:40:01
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.view;

import com.google.gwt.user.client.ui.Widget;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class ContentViewAdapter implements ContentView {
    private Widget widget;

    public ContentViewAdapter(Widget widget) {
        this.widget = widget;
    }

    public Widget getWidget() {
        return this.widget;
    }

    public void onBeforeHide() {
        // empty, subclasses can override
    }
}
