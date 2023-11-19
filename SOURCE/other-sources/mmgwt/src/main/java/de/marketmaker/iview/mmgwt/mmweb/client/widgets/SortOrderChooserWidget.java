/*
 * SortOrderChooserWidget.java
 *
 * Created on 14.05.13 15:11
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.widgets;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLTable;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;

import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.itools.gwtutil.client.widgets.Button;
import de.marketmaker.itools.gwtutil.client.widgets.LeftRightToolbar;
import de.marketmaker.itools.gwtutil.client.widgets.Separator;
import de.marketmaker.itools.gwtutil.client.widgets.menu.Menu;
import de.marketmaker.itools.gwtutil.client.widgets.menu.MenuItem;
import de.marketmaker.itools.gwtutil.client.widgets.menu.SelectButton;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

/**
 * @author Markus Dick
 */
public class SortOrderChooserWidget extends Composite {
    public static final String WIDGET_STYLE = "mm-sortOrderChooser"; //$NON-NLS$
    public static final String WIDGET_LIST_PANEL_STYLE = "items"; //$NON-NLS$
    public static final String SPACER_STYLE = "spacer"; //$NON-NLS$
    public static final String SELECTED_ROW_STYLE = "selected"; //$NON-NLS$
    public static final SortOrderChooserColumn EMPTY_COLUMN = new SortOrderChooserColumn(I18n.I.noSelectionListEntry(), "");
    private static final int ITEMS_TABLE_COLUMN_COUNT = 3;
    public static final String COLUMN_VALUE = "columnValue";  // $NON-NLS$
    public static final String SORT_ORDER = "sortOrder";  // $NON-NLS$
    private final Button addLevelButton;
    private final Button deleteLevelButton;
    private final Button shiftLevelUpButton;
    private final Button shiftLevelDownButton;
    private final FlexTable itemsTable;
    private final List<SortOrderChooserColumn> columns;
    private final List<SortOrderChooserItem> items;
    private int selectedRowIndex;

    public SortOrderChooserWidget(List<SortOrderChooserColumn> columns, List<SortOrderChooserItem> items) {
        final Panel layout = new FlowPanel();
        initWidget(layout);
        layout.setStyleName(WIDGET_STYLE);

        final LeftRightToolbar toolbar = new LeftRightToolbar();
        layout.add(toolbar);

        this.addLevelButton = Button.text(I18n.I.sortOrderChooserAddLevel())
                .clickHandler(new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {
                        addNewLevel();
                    }
                })
                .build();
        toolbar.addLeft(this.addLevelButton);

        this.deleteLevelButton = Button.text(I18n.I.sortOrderChooserDeleteLevel())
                .clickHandler(new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {
                        deleteLevel();
                    }
                })
                .build();
        toolbar.addLeft(this.deleteLevelButton);

        toolbar.addLeft(new Separator());

        this.shiftLevelUpButton = Button.icon("mm-list-move-up") // $NON-NLS$
                .tooltip(I18n.I.sortOrderChooserShiftLevelUp())
                .clickHandler(new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent clickEvent) {
                        shiftLevelUp();
                    }
                })
                .build();
        toolbar.addLeft(this.shiftLevelUpButton);

        this.shiftLevelDownButton = Button.icon("mm-list-move-down") // $NON-NLS$
                .tooltip(I18n.I.sortOrderChooserShiftLevelDown())
                .clickHandler(new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent clickEvent) {
                        shiftLevelDown();
                    }
                })
                .build();
        toolbar.addLeft(this.shiftLevelDownButton);

        this.itemsTable = new FlexTable();
        layout.add(this.itemsTable);
        this.itemsTable.setStyleName(WIDGET_LIST_PANEL_STYLE);

        this.selectedRowIndex = 0;

        this.columns = assertEmptyColumn(new ArrayList<>(columns));
        this.items = new ArrayList<>();
        initItemList(items);
        setSelectedRowIndex(0);

        this.itemsTable.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                final HTMLTable.Cell cell = itemsTable.getCellForEvent(event);
                if (cell != null) {
                    setSelectedRowIndex(cell.getRowIndex());
                }
            }
        });
    }

    private static void shiftRowUp(FlexTable table, int sourceRow, int targetRow) {
        Firebug.log("<shiftRowUp> sourceRow=" + sourceRow + " targetRow=" + targetRow);

        table.insertRow(targetRow);
        for (int i = 0; i < ITEMS_TABLE_COLUMN_COUNT; i++) {
            final Widget w = table.getWidget(sourceRow + 1, i);
            table.setWidget(targetRow, i, w);
        }
        table.removeRow(sourceRow + 1);
    }

    private static List<SortOrderChooserColumn> assertEmptyColumn(List<SortOrderChooserColumn> columns) {
        if (!columns.isEmpty() && !EMPTY_COLUMN.equals(columns.get(0))) {
            columns.add(0, EMPTY_COLUMN);
        }
        return columns;
    }

    private void addNewLevel() {
        final SortOrderChooserItem newItem = new SortOrderChooserItem();
        newItem.setColumnValue(EMPTY_COLUMN.getColumnValue());
        newItem.setSortOrder(SortOrder.ASCENDING);
        addLevel(newItem);
    }

    private void deleteLevel() {
        if (this.items.isEmpty() || this.selectedRowIndex == -1) {
            Firebug.log("<deleteLevelButton> return immediately bec. empty or selected row index -1");
            return;
        }

        final int rowIndex = this.selectedRowIndex;
        Firebug.log("<deleteLevelButton> rowIndex=" + rowIndex + " items.size=" + this.items.size());

        this.itemsTable.removeRow(rowIndex);
        this.items.remove(rowIndex);

        setSelectedRowIndex(rowIndex - 1);
    }

    private void shiftLevelDown() {
        if (this.items.size() <= 1 || this.selectedRowIndex >= this.items.size() - 1) {
            Firebug.log("<shiftLevelDownButton> return immediately bec. size <=1 or rowIndex is lastElement");
            return;
        }

        final int sourceRow = this.selectedRowIndex;
        final int targetRow = sourceRow + 1;

        Firebug.log("<shiftLevelDownButton> items.size=" + this.items.size() + " sourceRow=" + sourceRow + " targetRow=" + targetRow);

        this.items.add(sourceRow, this.items.remove(targetRow));
        shiftRowUp(this.itemsTable, targetRow, sourceRow);
        setSelectedRowIndex(targetRow);
    }

    private void shiftLevelUp() {
        if (this.items.size() < 2 || this.selectedRowIndex < 1) {
            Firebug.log("<shiftLevelUpButton> return immediately bec. items.size < 2 or first or no element selected");
            return;
        }

        final int sourceRow = this.selectedRowIndex;
        final int targetRow = sourceRow - 1;

        Firebug.log("<shiftLevelUpButton> items.size=" + this.items.size() + " sourceRow=" + sourceRow + " targetRow=" + targetRow);

        this.items.add(targetRow, this.items.remove(sourceRow));
        shiftRowUp(this.itemsTable, sourceRow, targetRow);
        setSelectedRowIndex(targetRow);
    }

    private void addLevel(SortOrderChooserItem item) {
        final int row = this.items.size();
        this.items.add(item);
        int col = 0;

        final HTML spacer = new HTML();
        this.itemsTable.setWidget(row, col++, spacer);
        spacer.setStyleName(SPACER_STYLE);

        final SelectButton columnList = createColumnListBoxFor(item);
        this.itemsTable.setWidget(row, col++, columnList);
        columnList.setSelectedData(COLUMN_VALUE, item.getColumnValue());

        final SelectButton sortOrderList = createSortOrderListBoxFor(item);
        this.itemsTable.setWidget(row, col, sortOrderList);
        sortOrderList.setSelectedData(SORT_ORDER, item.getSortOrder());

        setSelectedRowIndex(row);
    }

    private void setSelectedRowIndex(int rowIndex) {
        if (rowIndex >= this.items.size()) {
            rowIndex = this.items.size() - 1;
        }
        if (rowIndex < 0 && this.items.size() > 0) {
            rowIndex = 0;
        }

        if (rowIndex > -1) {
            FlexTable.FlexCellFormatter f = this.itemsTable.getFlexCellFormatter();
            for (int i = 0; i < ITEMS_TABLE_COLUMN_COUNT; i++) {
                if (this.selectedRowIndex > -1 && this.selectedRowIndex < this.itemsTable.getRowCount()) {
                    f.removeStyleName(this.selectedRowIndex, i, SELECTED_ROW_STYLE);
                }
                f.addStyleName(rowIndex, i, SELECTED_ROW_STYLE);
            }
        }
        updateButtons(rowIndex);
        this.selectedRowIndex = rowIndex;
    }

    private void updateButtons(int rowIndex) {
        if (this.items.size() == 0 || rowIndex == -1) {
            enableButtons(true, false, false, false);
            return;
        }
        enableButtons(true, true, !(rowIndex == 0), !(rowIndex == this.items.size() - 1));
    }

    private void enableButtons(boolean add, boolean delete, boolean shiftUp, boolean shiftDown) {
        this.addLevelButton.setEnabled(add);
        this.deleteLevelButton.setEnabled(delete);
        this.shiftLevelUpButton.setEnabled(shiftUp);
        this.shiftLevelDownButton.setEnabled(shiftDown);
    }

    private void initItemList(List<SortOrderChooserItem> items) {
        if (items.isEmpty()) {
            addNewLevel();
            return;
        }

        for (SortOrderChooserItem item : items) {
            addLevel(item);
        }
    }

    private SelectButton createColumnListBoxFor(final SortOrderChooserItem item) {
        final Menu menu = new Menu();
        for (final SortOrderChooserColumn c : this.columns) {
            menu.add(new MenuItem(c.getLabel()).withData(COLUMN_VALUE, c.getColumnValue()));
        }

        return new SelectButton()
                .withMenu(menu, false)
                .withClickOpensMenu()
                .withSelectionHandler(new SelectionHandler<MenuItem>() {
                    @Override
                    public void onSelection(SelectionEvent<MenuItem> event) {
                        item.setColumnValue((String) event.getSelectedItem().getData(COLUMN_VALUE));
                    }
                });
    }

    private SelectButton createSortOrderListBoxFor(final SortOrderChooserItem item) {
        final Menu menu = new Menu();
        for (SortOrder sortOrder : SortOrder.values()) {
            menu.add(new MenuItem(sortOrder.getLabel()).withData(SORT_ORDER, sortOrder));
        }
        return new SelectButton()
                .withMenu(menu, true)
                .withClickOpensMenu()
                .withSelectionHandler(new SelectionHandler<MenuItem>() {
                    @Override
                    public void onSelection(SelectionEvent<MenuItem> event) {
                        item.setSortOrder((SortOrder) event.getSelectedItem().getData(SORT_ORDER));
                    }
                });
    }

    public void setItems(List<SortOrderChooserItem> items) {
        this.items.clear();
        this.itemsTable.removeAllRows();
        initItemList(items);
        setSelectedRowIndex(0);
    }

    public List<SortOrderChooserItem> getResultItems() {
        final ArrayList<SortOrderChooserItem> copyOfItems = new ArrayList<>(this.items);
        final ListIterator<SortOrderChooserItem> it = copyOfItems.listIterator();

        while (it.hasNext()) {
            final SortOrderChooserItem copy = it.next();
            if (EMPTY_COLUMN.getColumnValue().equals(copy.getColumnValue())) {
                it.remove();
            }
        }

        return copyOfItems;
    }

    public static enum SortOrder {
        ASCENDING(I18n.I.sortOrderChooserAscending()),
        DESCENDING(I18n.I.sortOrderChooserDescending());
        private final String label;

        SortOrder(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }

    public static class SortOrderChooserColumn {
        private String label;
        private String columnValue;

        public SortOrderChooserColumn() {
            this(null, null);
        }

        public SortOrderChooserColumn(String label, String columnValue) {
            this.label = label;
            this.columnValue = columnValue;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public String getColumnValue() {
            return columnValue;
        }

        public void setColumnValue(String columnValue) {
            this.columnValue = columnValue;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof SortOrderChooserColumn)) {
                return false;
            }

            final SortOrderChooserColumn that = (SortOrderChooserColumn) o;

            if (columnValue != null ? !columnValue.equals(that.columnValue) : that.columnValue != null) {
                return false;
            }
            if (label != null ? !label.equals(that.label) : that.label != null) {
                return false;
            }

            return true;
        }

        @Override
        public int hashCode() {
            int result = label != null ? label.hashCode() : 0;
            result = 31 * result + (columnValue != null ? columnValue.hashCode() : 0);
            return result;
        }
    }

    public static class SortOrderChooserItem {
        private String columnValue;
        private SortOrder sortOrder;

        public SortOrderChooserItem() {
            this(null, null);
        }

        public SortOrderChooserItem(String columnValue, SortOrder sortOrder) {
            this.columnValue = columnValue;
            this.sortOrder = sortOrder;
        }

        public String getColumnValue() {
            return columnValue;
        }

        public void setColumnValue(String columnValue) {
            this.columnValue = columnValue;
        }

        public SortOrder getSortOrder() {
            return sortOrder;
        }

        public void setSortOrder(SortOrder sortOrder) {
            this.sortOrder = sortOrder;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof SortOrderChooserItem)) {
                return false;
            }

            final SortOrderChooserItem that = (SortOrderChooserItem) o;

            if (columnValue != null ? !columnValue.equals(that.columnValue) : that.columnValue != null) {
                return false;
            }
            if (sortOrder != that.sortOrder) {
                return false;
            }

            return true;
        }

        @Override
        public int hashCode() {
            int result = columnValue != null ? columnValue.hashCode() : 0;
            result = 31 * result + (sortOrder != null ? sortOrder.hashCode() : 0);
            return result;
        }
    }
}
