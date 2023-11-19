/*
 * WorkspaceItem.java
 *
 * Created on 09.04.2008 14:43:38
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */

package de.marketmaker.iview.mmgwt.mmweb.client.workspace;

import com.google.gwt.user.client.ui.IsWidget;

/**
 * @author Ulrich Maurer
 */
public interface WorkspaceItem extends IsWidget, StateSupport {
    @SuppressWarnings("unused")
    String getHeaderText();
    void setHeaderVisible(boolean visible);
}
