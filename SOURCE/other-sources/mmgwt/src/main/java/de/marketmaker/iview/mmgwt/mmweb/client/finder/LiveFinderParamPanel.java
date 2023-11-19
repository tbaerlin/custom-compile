package de.marketmaker.iview.mmgwt.mmweb.client.finder;

import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.button.ToolButton;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Created on 04.08.11 10:16
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author Michael Lösch
 */

public class LiveFinderParamPanel extends ContentPanel {

    private final ScrollPanel scrollPanel;

    LiveFinderParamPanel() {
        this.scrollPanel = new ScrollPanel();
        setLayout(new FitLayout());
        setWidth(400);
        init();
    }

    private void init() {
        setStyleName("mm-workspace"); // $NON-NLS-0$
    }

    public void setWidget(Widget widget, ToolButton configToolBtn) {
        removeAll();
        this.scrollPanel.clear();
        if (!getHeader().getTools().contains(configToolBtn)) {
            getHeader().addTool(configToolBtn);
        }
        this.scrollPanel.add(widget);
        add(this.scrollPanel);
        layout();
    }

    public int getVerticalScrollPosition() {
        return this.scrollPanel.getVerticalScrollPosition();
    }

    public void setVerticalScrollPosition(int verticalScrollPosition) {
        this.scrollPanel.setVerticalScrollPosition(verticalScrollPosition);
    }
}