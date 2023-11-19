package de.marketmaker.iview.mmgwt.mmweb.client.workspace;

import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.widget.ContentPanel;

/**
 * Created on 04.02.13 15:10
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author Michael Lösch
 */

public abstract class AbstractWorkspaceItem extends ContentPanel implements WorkspaceItem {
    private Listener<ComponentEvent> listenerForCurrentWorkspace = null;

    protected AbstractWorkspaceItem(final String heading) {
        setHeading(heading);
        setAnimCollapse(false);
        setScrollMode(com.extjs.gxt.ui.client.Style.Scroll.AUTO);
        addStyleName("mm-workspace-item"); // $NON-NLS$
    }


    public void setListenerForCurrentWorkspace(final Listener<ComponentEvent> listener) {
        if (this.listenerForCurrentWorkspace != null) {
            removeListener(Events.Expand, listener);
        }
        this.listenerForCurrentWorkspace = listener;
        addListener(Events.Expand, listener);
    }

    @Override
    public String getHeaderText() {
        return getHeader().getText();
    }
}