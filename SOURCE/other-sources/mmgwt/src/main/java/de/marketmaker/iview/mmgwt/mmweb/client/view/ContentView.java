/*
 * ContentView.java
 *
 * Created on 02.09.2008 11:35:22
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.view;

import com.google.gwt.user.client.ui.Widget;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public interface ContentView {
    /**
     * @return the widget that represents the view
     */
    Widget getWidget();

    /**
     * Called before the widget that represents this view will be replaced by another one
     */
    void onBeforeHide();
}
