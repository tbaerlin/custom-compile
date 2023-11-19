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
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTMLTable;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Widget;
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
public class SpsListMultiFloat extends AbstractSpsListMultiBase<Grid> implements PopupTableSelectionHelper.Callback, HasFocusHandlers, HasBlurHandlers {
    private final ListWidgetDescColumn column;
    private Set<String> keysSelected = new HashSet<>();
    private FlowPanel panel;
    private IconImageIcon buttonAdd;

    private PopupTableSelectionHelper popupTableSelectionHelper = new PopupTableSelectionHelper(this);
    private final MultiWidgetFocusSupport multiWidgetFocusSupport = new MultiWidgetFocusSupport();

    public SpsListMultiFloat(Context context, BindToken parentToken, BindToken itemsBindToken, String keyField, ListWidgetDescColumn column) {
        super(context, parentToken, itemsBindToken, keyField);
        this.column = column;
    }

    @Override
    public void onPropertyChange() {
        if (!this.spsListBindFeature.isPropertySet()) {
            return;
        }
        this.panel.clear();
        this.keysSelected.clear();
        final SpsListProperty listProperty = getBindFeature().getSpsProperty();
        List<SpsProperty> children = listProperty.getChildren();
        for (int i = 0; i < children.size(); i++) {
            final SpsProperty childProp = children.get(i);
            final String key = ((SpsLeafProperty) childProp).getStringValue();
            final SpsGroupProperty entry = this.spsListBindFeature.getProperty(this.keyField, key);
            if (entry != null) {
                this.panel.add(createEntryWidget(i, entry));
                this.keysSelected.add(key);
            }
        }
        this.buttonAdd.setVisible(listProperty.getChildCount() < this.spsListBindFeature.getSpsProperty().getChildCount());
    }

    private Widget createEntryWidget(final int propIndex, final SpsGroupProperty gp) {
        final String fieldName = this.column.getFieldName();

        final FlowPanel entryPanel = new FlowPanel();
        entryPanel.setStyleName("sps-list-entry");
        entryPanel.add(new InlineLabel(((SpsLeafProperty) gp.get(fieldName)).getStringValue()));

        final IconImageIcon buttonRemove = IconImage.getIcon("sps-minus"); // $NON-NLS$
        buttonRemove.setStyleName("sps-list-remove");
        buttonRemove.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                getBindFeature().getSpsProperty().remove(propIndex, true, true);
            }
        });
        this.popupTableSelectionHelper.makeListEntryFocusable(buttonRemove, propIndex);
        this.multiWidgetFocusSupport.add(buttonRemove);
        entryPanel.add(buttonRemove);
        return entryPanel;
    }

    @Override
    protected Grid createWidget() {
        final Grid grid = new Grid(1, 2);
        grid.setCellPadding(0);
        grid.setCellSpacing(0);
        grid.setStyleName("sps-list");
        this.panel = new FlowPanel();

        this.buttonAdd = IconImage.getIcon("sps-plus"); // $NON-NLS$
        this.buttonAdd.setStyleName("sps-list-add");
        this.buttonAdd.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                showList();
            }
        });
        this.popupTableSelectionHelper.makeButtonAddFocusable(this.buttonAdd);
        this.multiWidgetFocusSupport.add(this.buttonAdd);

        grid.setWidget(0, 0, this.panel);
        grid.setWidget(0, 1, this.buttonAdd);
        final HTMLTable.CellFormatter cellFormatter = grid.getCellFormatter();
        cellFormatter.setStyleName(0, 0, "sps-list-float");
        cellFormatter.setStyleName(0, 1, "sps-list-add-cell");
        grid.getColumnFormatter().setWidth(1, "20px"); // $NON-NLS$
        return grid;
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
            final String fieldName = this.column.getFieldName();
            table.setText(row, 0, ((SpsLeafProperty) gp.get(fieldName)).getStringValue());
            rowFormatter.getElement(row).setAttribute(PopupTableSelectionHelper.ROW_ATTRIBUTE_KEY, key);
        }

        pp.setWidget(table);
        final Element nearbyElement;
        final int rowCount = table.getRowCount();
        if (rowCount == 0) {
            nearbyElement = table.getElement();
        }
        else if (this.popupTableSelectionHelper.getLastSelectedRow() < rowCount) {
            nearbyElement = table.getRowFormatter().getElement(this.popupTableSelectionHelper.getLastSelectedRow());
        }
        else {
            nearbyElement = table.getRowFormatter().getElement(rowCount - 1);
        }
        pp.showNearby(this.panel, nearbyElement);
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
        WidgetUtil.deferredSetFocus(this.panel.getWidget(index));
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