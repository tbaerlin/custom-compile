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
import de.marketmaker.itools.gwtutil.client.util.MultiWidgetFocusSupport;
import de.marketmaker.itools.gwtutil.client.util.WidgetUtil;
import de.marketmaker.itools.gwtutil.client.widgets.PopupPanelFix;
import de.marketmaker.itools.gwtutil.client.widgets.floating.FloatingPopupPanel;
import de.marketmaker.itools.gwtutil.client.widgets.IconImage;
import de.marketmaker.itools.gwtutil.client.widgets.IconImageIcon;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.BindToken;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.Context;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsGroupProperty;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsLeafProperty;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsListProperty;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsProperty;
import de.marketmaker.iview.pmxml.ListWidgetDescColumn;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Author: umaurer
 * Created: 03.04.14
 */
public class SpsListMultiTable extends AbstractSpsListMultiBase<FlexTable> implements HasFocusHandlers, HasBlurHandlers {
    private final List<ListWidgetDescColumn> columns;

    private Set<String> keysSelected = new HashSet<>();
    private FlexTable tableSelected;
    private IconImageIcon buttonAdd;

    private PopupTableSelectionHelper popupTableSelectionHelper = new PopupTableSelectionHelper(this);

    private MultiWidgetFocusSupport multiWidgetFocusSupport = new MultiWidgetFocusSupport();

    public SpsListMultiTable(Context context, BindToken parentToken, BindToken itemsBindToken, String keyField, List<ListWidgetDescColumn> columns) {
        super(context, parentToken, itemsBindToken, keyField);
        this.columns = columns;
    }

    @Override
    public void onPropertyChange() {
        if (!this.spsListBindFeature.isPropertySet()) {
            return;
        }
        this.tableSelected.removeAllRows();
        this.keysSelected.clear();
        final SpsListProperty listProperty = getBindFeature().getSpsProperty();
        for (SpsProperty childProp : listProperty.getChildren()) {
            final String key = ((SpsLeafProperty) childProp).getStringValue();
            final SpsGroupProperty entry = getEntry(key);
            if(entry != null) {
                addTableEntry(entry);
                this.keysSelected.add(key);
            }
        }
        if (listProperty.getChildCount() < this.spsListBindFeature.getSpsProperty().getChildCount()) {
            this.tableSelected.setWidget(this.tableSelected.getRowCount(), this.columns.size(), this.buttonAdd);
        }
        if (this.tableSelected.getRowCount() == 0) {
            for (int i = 0; i < this.columns.size(); i++) {
                this.tableSelected.setHTML(0, i, "&nbsp;"); // $NON-NLS$
            }
        }
        addBorderStyles(this.tableSelected);
    }

    private void addBorderStyles(FlexTable table) {
        final int rowCount = table.getRowCount();
        if (rowCount == 0) {
            return;
        }
        final int lastColumnId = this.columns.size() - 1;
        final FlexTable.FlexCellFormatter formatter = table.getFlexCellFormatter();
        for (int col = 0; col <= lastColumnId; col++) {
            formatter.addStyleName(0, col, "borderTop");
            formatter.addStyleName(rowCount - 1, col, "borderBottom");
        }
        for (int row = 0; row < rowCount; row++) {
            formatter.addStyleName(row, 0, "borderLeft");
            formatter.addStyleName(row, lastColumnId, "borderRight");
        }
    }

    private void addTableEntry(final SpsGroupProperty gp) {
        final int row = this.tableSelected.getRowCount();
        for (int i = 0; i < this.columns.size(); i++) {
            final String fieldName = this.columns.get(i).getFieldName();
            this.tableSelected.setText(row, i, ((SpsLeafProperty) gp.get(fieldName)).getStringValue());
        }
        final IconImageIcon buttonRemove = IconImage.getIcon("sps-minus"); // $NON-NLS$
        buttonRemove.setStyleName("sps-list-remove");
        buttonRemove.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                getBindFeature().getSpsProperty().remove(row, true, true);
            }
        });
        this.popupTableSelectionHelper.makeListEntryFocusable(buttonRemove, row);
        this.multiWidgetFocusSupport.add(buttonRemove);
        this.tableSelected.setWidget(row, this.columns.size(), buttonRemove);
    }

    @Override
    protected FlexTable createWidget() {
        this.tableSelected = new FlexTable();
        this.tableSelected.setStyleName("sps-list");
        this.tableSelected.setCellPadding(0);
        this.tableSelected.setCellSpacing(0);
        this.buttonAdd = IconImage.getIcon("sps-plus"); // $NON-NLS$
        this.buttonAdd.setStyleName("sps-list-add");
        this.buttonAdd.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                showList();
            }
        });
        this.multiWidgetFocusSupport.add(this.buttonAdd);
        this.popupTableSelectionHelper.makeButtonAddFocusable(this.buttonAdd);

        this.tableSelected.setWidget(0, this.columns.size(), this.buttonAdd);
        this.tableSelected.getColumnFormatter().setWidth(this.columns.size(), "20px"); // $NON-NLS$
        return this.tableSelected;
    }

    public void showList() {
        this.buttonAdd.getElement().focus();
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
        for (SpsProperty spsProperty : this.spsListBindFeature.getSpsProperty().getChildren()) {
            if (!(spsProperty instanceof SpsGroupProperty)) {
                throw new IllegalStateException("not group property: " + spsProperty.getBindToken()); // $NON-NLS$
            }
            final SpsGroupProperty gp = (SpsGroupProperty) spsProperty;
            final String key = ((SpsLeafProperty) gp.get(this.keyField)).getStringValue();
            if (this.keysSelected.contains(key)) {
                continue;
            }
            final int row = table.getRowCount();
            for (int i = 0; i < this.columns.size(); i++) {
                final String fieldName = this.columns.get(i).getFieldName();
                table.setText(row, i, ((SpsLeafProperty) gp.get(fieldName)).getStringValue());
            }
            rowFormatter.getElement(row).setAttribute(PopupTableSelectionHelper.ROW_ATTRIBUTE_KEY, key);
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
        pp.showNearby(this.tableSelected, nearbyElement);
        this.popupTableSelectionHelper.onPopupOpened(pp, table);
    }

    @Override
    public int getSelectedEntryCount() {
        return this.keysSelected.size();
    }

    @Override
    public void focusButtonAdd() {
        WidgetUtil.deferredSetFocus(this.buttonAdd);
    }

    @Override
    public void focusSelectedEntry(int index) {
        WidgetUtil.deferredSetFocus(this.tableSelected.getWidget(index, this.columns.size()));
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
