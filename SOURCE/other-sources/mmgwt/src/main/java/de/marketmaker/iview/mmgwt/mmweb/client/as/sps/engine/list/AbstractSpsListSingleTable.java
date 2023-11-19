/*
 * AbstractSpsListSingleTable.java
 *
 * Created on 27.05.2015 08:30
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.list;

import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.HasBlurHandlers;
import com.google.gwt.event.dom.client.HasFocusHandlers;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTMLTable;
import com.google.gwt.user.client.ui.Widget;
import de.marketmaker.itools.gwtutil.client.util.MultiWidgetFocusSupport;
import de.marketmaker.itools.gwtutil.client.util.StringUtility;
import de.marketmaker.itools.gwtutil.client.util.WidgetUtil;
import de.marketmaker.itools.gwtutil.client.widgets.PopupPanelFix;
import de.marketmaker.itools.gwtutil.client.widgets.floating.FloatingPopupPanel;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.BindToken;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.Context;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsGroupProperty;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsLeafProperty;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsListProperty;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsProperty;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.TextUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.DataItemFormatter;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;
import de.marketmaker.iview.pmxml.ListWidgetDescColumn;
import de.marketmaker.iview.pmxml.MM;
import de.marketmaker.iview.pmxml.ParsedTypeInfo;

import java.util.List;

/**
 * @author mdick
 */
public abstract class AbstractSpsListSingleTable<W extends Widget, P extends SpsProperty> extends AbstractSpsListBase<W, P> implements PopupTableSelectionHelper.Callback, HasFocusHandlers, HasBlurHandlers {
    protected final List<ListWidgetDescColumn> columns;
    private final DataItemFormatter[] columnFormatters;
    private final String[] cellStyles;

    private PopupTableSelectionHelper popupTableSelectionHelper = new PopupTableSelectionHelper(this);
    private MultiWidgetFocusSupport multiWidgetFocusSupport = new MultiWidgetFocusSupport();

    public AbstractSpsListSingleTable(Context context, BindToken parentToken, BindToken itemsBindToken, String columnsKeyField, List<ListWidgetDescColumn> columns) {
        super(context, parentToken, itemsBindToken, columnsKeyField);
        this.columns = columns;

        this.columnFormatters = new DataItemFormatter[columns.size()];
        this.cellStyles = new String[columns.size()];

        for (int i = 0; i < this.columns.size(); i++) {
            final ParsedTypeInfo pti = SpsUtil.getListCellDecl(context, itemsBindToken, this.columns.get(i).getFieldName()).getDescription();
            this.columnFormatters[i] = new DataItemFormatter(pti, this.columns.get(i).getColumnStyle()).withTrailingZeros(true);
            if (StringUtility.hasText(this.columns.get(i).getColumnStyle())) {
                this.cellStyles[i] = this.columns.get(i).getColumnStyle();
            }
            else {
                switch (pti.getTypeId()) {
                    case TI_NUMBER:
                        this.cellStyles[i] = "mm-right"; // $NON-NLS$
                        break;
                    case TI_BOOLEAN:
                        this.cellStyles[i] = "mm-center"; // $NON-NLS$
                        break;
                    default:
                        this.cellStyles[i] = null;
                        break;
                }
            }
        }
    }

    protected void initFocusSupport(W widget) {
        this.popupTableSelectionHelper.makeButtonAddFocusable(widget);
        this.multiWidgetFocusSupport.add(widget);
    }

    @Override
    public void showList() {
        getWidget().getElement().focus();
        this.popupTableSelectionHelper.hidePopup();
        final FloatingPopupPanel pp = new FloatingPopupPanel(true);
        pp.setStyleName("as-popup-menu");
        PopupPanelFix.addFrameDummy(pp);

        final FlexTable table = new FlexTable();
        table.setStyleName("sps-list-popup-table");
        final HTMLTable.RowFormatter rowFormatter = table.getRowFormatter();
        table.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                final HTMLTable.Cell cell = table.getCellForEvent(event);
                if (cell != null) {
                    pp.hide();
                    final int rowIndex = cell.getRowIndex();
                    popupTableSelectionHelper.setLastSelectedRow(rowIndex);
                    final String key = rowFormatter.getElement(rowIndex).getAttribute(PopupTableSelectionHelper.ROW_ATTRIBUTE_KEY);
                    addSelection(key);
                }
            }
        });

        final HTMLTable.CellFormatter cellFormatter = table.getCellFormatter();

        boolean hasNullValue = false;
        for (SpsProperty spsProperty : this.spsListBindFeature.getSpsProperty().getChildren()) {
            if (!(spsProperty instanceof SpsGroupProperty)) {
                throw new IllegalStateException("not group property: " + spsProperty.getBindToken()); // $NON-NLS$
            }
            final SpsGroupProperty gp = (SpsGroupProperty) spsProperty;
            final String key = ((SpsLeafProperty) gp.get(this.keyField)).getStringValue();
            final int row = table.getRowCount();
            for (int i = 0; i < this.columns.size(); i++) {
                final String fieldName = this.columns.get(i).getFieldName();
                final MM mm = ((SpsLeafProperty) gp.get(fieldName)).getDataItem();
                table.setText(row, i, this.columnFormatters[i].format(mm));
                if(StringUtil.hasText(this.cellStyles[i])) {
                    cellFormatter.addStyleName(row, i, this.cellStyles[i]);
                }
            }
            rowFormatter.getElement(row).setAttribute(PopupTableSelectionHelper.ROW_ATTRIBUTE_KEY, key);
            if (key == null) {
                hasNullValue = true;
            }
        }
        if (!(isMandatory() || hasNullValue)) {
            table.insertRow(0);
            table.setHTML(0, 0, TextUtil.NO_SELECTION_TEXT);
            table.getFlexCellFormatter().setColSpan(0, 0, this.columns.size());
        }

        boolean hasColumnNames = false;
        for (ListWidgetDescColumn column : columns) {
            if(StringUtil.hasText(column.getColumnName())) {
                hasColumnNames = true;
                break;
            }
        }
        if(hasColumnNames) {
            table.insertRow(0);
            for (int i = 0; i < this.columns.size(); i++) {
                table.setText(0, i, this.columns.get(i).getColumnName());
                if(StringUtil.hasText(this.cellStyles[i])) {
                    cellFormatter.addStyleName(0, i, this.cellStyles[i]);
                }
            }
        }

        pp.setWidget(table);
        final Element nearbyElement;
        if (table.getRowCount() == 0) {
            nearbyElement = table.getElement();
        }
        else if (popupTableSelectionHelper.getLastSelectedRow() < table.getRowCount()) {
            nearbyElement = table.getRowFormatter().getElement(popupTableSelectionHelper.getLastSelectedRow());
        }
        else {
            nearbyElement = table.getRowFormatter().getElement(table.getRowCount() - 1);
        }
        pp.showNearby(getWidget(), nearbyElement);
        this.popupTableSelectionHelper.onPopupOpened(pp, table);
    }

    @Override
    public void addSelection(String key) {
        final SpsGroupProperty entry = getEntry(key);

        if(entry == null) {
            if(!isMandatory()) { // allow s.b. to explicitly select the "<no entry>", which is present if a value is not mandatory.
                addValueToSelection(null);
            }
            return;
        }

        final SpsLeafProperty spsProperty =(SpsLeafProperty)entry.get(this.keyField);
        final MM value = spsProperty.getDataItem();

        addValueToSelection(value);
    }

    private void addValueToSelection(MM value) {
        final P p = getBindFeature().getSpsProperty();
        if (p instanceof SpsLeafProperty) {
            ((SpsLeafProperty) p).setValue(value, true, true);
        }
        else if (p instanceof SpsListProperty) {
            final SpsListProperty lp = (SpsListProperty) p;
            lp.clear(false);
            final SpsLeafProperty leafProperty = new SpsLeafProperty("", lp, SpsUtil.getChildParsedTypeInfo(this.context, lp));
            leafProperty.setValue(value, true, true);
            lp.add(leafProperty, true);
        }
    }


    @Override
    public int getSelectedEntryCount() {
        return 1;
    }

    @Override
    public void focusButtonAdd() {
        WidgetUtil.deferredSetFocus(getWidget());
    }

    @Override
    public void focusSelectedEntry(int index) {
        WidgetUtil.deferredSetFocus(getWidget());
    }

    @Override
    public void removeProperty(int index) {
        // ignore
    }

    @Override
    public HandlerRegistration addFocusHandler(FocusHandler handler) {
        return this.multiWidgetFocusSupport.addFocusHandler(handler);
    }

    @Override
    public HandlerRegistration addBlurHandler(BlurHandler handler) {
        return this.multiWidgetFocusSupport.addBlurHandler(handler);
    }

    @Override
    public void fireEvent(GwtEvent<?> event) {
        this.multiWidgetFocusSupport.fireEvent(event);
    }
}
