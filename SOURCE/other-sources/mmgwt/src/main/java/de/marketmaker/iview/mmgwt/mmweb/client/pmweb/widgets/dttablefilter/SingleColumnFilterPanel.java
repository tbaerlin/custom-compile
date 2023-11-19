/*
 * SingleColumnFilterPanel.java
 *
 * Created on 19.03.2015 08:10
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.widgets.dttablefilter;

import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.DTTableRenderer;
import de.marketmaker.iview.pmxml.DTCell;
import de.marketmaker.iview.pmxml.DTColumnSpec;

/**
 * @author mdick
 */
public class SingleColumnFilterPanel extends Composite implements HasCreateColumnFilter {
    private final int columnIndex;
    private final DTColumnSpec columnSpec;

    private final FlowPanel layout = new FlowPanel();

    private AbstractFilterPanel<?, DTCell> filterPanel;
    private ChangeHandler filterChangeHandler;
    private FilterMetadata<DTCell> metadata;
    private HandlerRegistration handlerRegistration;

    public SingleColumnFilterPanel(int columnIndex, DTColumnSpec columnSpec) {
        this.columnIndex = columnIndex;
        this.columnSpec = columnSpec;
        initWidget(this.layout);
    }

    public int getColumnIndex() {
        return this.columnIndex;
    }

    @SuppressWarnings("UnusedDeclaration")
    public DTColumnSpec getColumnSpec() {
        return this.columnSpec;
    }

    public void setFilterChangeHandler(ChangeHandler filterChangeHandler) {
        if(this.filterChangeHandler == filterChangeHandler) {
            return;
        }

        if(this.handlerRegistration != null) {
            this.handlerRegistration.removeHandler();
        }
        if(this.filterChangeHandler != null) {
            this.handlerRegistration = this.filterPanel.addChangeHandler(this.filterChangeHandler);
        }
        this.filterChangeHandler = filterChangeHandler;
    }

    public void setFilterMetadata(FilterMetadata<DTCell> metadata) {
        if (this.metadata == metadata) {
            return;
        }

        this.metadata = metadata;

        if(this.filterPanel != null) {
            this.layout.remove(this.filterPanel);
        }
        if(this.handlerRegistration != null) {
            this.handlerRegistration.removeHandler();
        }

        if(this.metadata == null) {
            return;
        }

        this.filterPanel = ColumnFilterUtil.createFilterPanel(metadata);
        this.layout.add(this.filterPanel);

        this.filterPanel.updateMetadata(metadata);

        if(this.filterChangeHandler != null) {
            this.handlerRegistration = this.filterPanel.addChangeHandler(this.filterChangeHandler);
        }
    }

    @Override
    public DTTableRenderer.ColumnFilter createColumnFilter() {
        if(this.filterPanel == null) {
            return null;
        }
        return this.filterPanel.createColumnFilter();
    }

    @Override
    public boolean isColumnFilterDefined() {
        return this.filterPanel != null && this.filterPanel.isColumnFilterDefined();
    }

    public FilterData getFilterData() {
        if(this.filterPanel == null) {
            return null;
        }
        return this.filterPanel.getData();
    }

    @SuppressWarnings("unchecked")
    public void setFilterData(FilterData filterData) {
        if(this.filterPanel == null) {
            return;
        }
        this.filterPanel.updateData(filterData);
    }
}
