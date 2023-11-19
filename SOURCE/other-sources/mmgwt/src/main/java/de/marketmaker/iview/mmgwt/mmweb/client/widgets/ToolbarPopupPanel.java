/*
 * ToolbarPopupPanel.java
 *
 * Created on 16.05.13 13:36
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.widgets;

import com.google.gwt.user.client.ui.PopupPanel;

/**
 * @author Markus Dick
 */
public class ToolbarPopupPanel extends PopupPanel {

    public static final String TOOLBAR_POPUP_STYLE = "mm-toolbar-popup"; // $NON-NLS$

    public ToolbarPopupPanel() {
        this(true);
    }

    public ToolbarPopupPanel(boolean autoHideEnabled) {
        setStyleName(TOOLBAR_POPUP_STYLE);
        setAutoHideEnabled(autoHideEnabled);
    }
}
