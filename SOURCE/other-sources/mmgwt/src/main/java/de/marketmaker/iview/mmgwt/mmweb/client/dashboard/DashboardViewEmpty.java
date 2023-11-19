package de.marketmaker.iview.mmgwt.mmweb.client.dashboard;

import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.Snippet;

import java.util.List;

/**
 * Created on 26.06.15
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author mloesch
 */
public class DashboardViewEmpty implements DashboardViewIfc {
    @Override
    public boolean isEditMode() {
        return false;
    }

    @Override
    public void show(List<Snippet> snippets) {
        //nothing to do
    }

    @Override
    public void enableEdit() {
        //nothing to do
    }

    @Override
    public void disableEdit() {
        //nothing to do
    }

    @Override
    public void setName(String name) {
        //nothing to do
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public void setHoverStyle(TableCellPos[] cells, String styleName) {
        //nothing to do
    }

    @Override
    public void removeHoverStyle() {
        //nothing to do
    }

    @Override
    public Widget asWidget() {
        final SimplePanel sp = new SimplePanel();
        sp.add(new HTML(I18n.I.empty()));
        return sp;
    }
}
