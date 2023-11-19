package de.marketmaker.iview.mmgwt.mmweb.client.dashboard;

import com.google.gwt.user.client.ui.IsWidget;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.Snippet;

import java.util.List;

/**
 * Created on 26.06.15
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author mloesch
 */
public interface DashboardViewIfc extends IsWidget {
    boolean isEditMode();

    void show(List<Snippet> snippets);

    void enableEdit();

    void disableEdit();

    void setName(String name);

    String getName();

    void setHoverStyle(TableCellPos[] cells, String styleName);

    void removeHoverStyle();
}