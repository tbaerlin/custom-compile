/*
 * ContentContainer.java
 *
 * Created on 24.04.2008 12:53:19
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */

package de.marketmaker.iview.mmgwt.mmweb.client.view;

import com.google.gwt.user.client.ui.Widget;

/**
 * @author Ulrich Maurer
 */
public interface ContentContainer {
    /**
     * Replaces the current content with the content defined by view. Right before that view's
     * content will be replaced again by some other view, its {@link ContentView#onBeforeHide()}
     * method will be invoked to support cleanup.
     * @param view defines content
     */
    void setContent(ContentView view);

    /**
     * Replaces the current content with the given widget. This method can be used if no
     * cleanup is required before w will be replaced again by some other view, otherwise
     * {@link #setContent(ContentView)} has to be used
     * @param w content
     */
    void setContent(Widget w);

    Widget getContent();

    boolean isShowing(Widget w);
}
