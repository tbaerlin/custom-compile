/*
 * SectionListTableViewEntry.java
 *
 * Created on 08.12.2014 10:57
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.listsection.table;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import de.marketmaker.itools.gwtutil.client.widgets.IconImageIcon;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsProperty;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsWidget;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.listsection.SectionListEntry;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.listsection.SpsListSectionUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * @author mdick
 */
class SectionListTableViewEntry implements SectionListEntry {
    private SectionListTableView parent;
    private ArrayList<SpsWidget> spsWidgetList = new ArrayList<>();

    private IconImageIcon deleteButton;

    SectionListTableViewEntry(SectionListTableView parent, SpsProperty property) {
        this.parent = parent;

        parent.createAndAddWidgets(property, this.spsWidgetList);

        if (!parent.isReadonly()) {
            this.deleteButton = createDeleteButton(property);
        }
        else {
            this.deleteButton = null;
        }

        parent.fireAfterPropertiesSet();
        parent.fireChanged(property);
    }

    @Override
    public List<SpsWidget> getSpsWidgetList() {
        return this.spsWidgetList;
    }

    @Override
    public void setDeleteVisible(boolean visible) {
        if(this.deleteButton == null) {
            return;
        }

        this.deleteButton.setVisible(visible);
        this.deleteButton.setEnabled(visible);
    }

    @Override
    public void setCaptionNumber(int n) {
        //not required here
    }

    @Override
    public void clearWidgets() {
        this.spsWidgetList.clear();
    }

    @Override
    public boolean focusFirst() {
        return SpsListSectionUtil.focusFirst(this.spsWidgetList);
    }

    private IconImageIcon createDeleteButton(final SpsProperty p) {
        return SpsListSectionUtil.createDeleteButton(this.parent.getDeleteButtonTooltip())
                .withClickHandler(new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {
                        parent.deleteProperty(p);
                    }
                });
    }

    IconImageIcon getDeleteButton() {
        return this.deleteButton;
    }
}
