/*
 * DefaultTableModel.java
 *
 * Created on 19.03.2008 16:30:10
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.table;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.marketmaker.iview.dmxml.Sort;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class FlexTableDataModel implements TableDataModel {
    private final List<Object[]> data;
    private final List<CellMetaData[]> cellMetaData;
    private final List<String> flipId;
    private final List<String> rowClass;
    private final int columnCount;
    private HashMap<String, TableColumn.Sort> sorts;

    public FlexTableDataModel(int columnCount) {
        this.columnCount = columnCount;
        this.data = new ArrayList<Object[]>();
        this.cellMetaData = new ArrayList<CellMetaData[]>();
        this.flipId = new ArrayList<String>();
        this.rowClass = new ArrayList<String>();
    }

    public FlexTableDataModel withSort(Sort s) {
        if (s == null) {
            this.sorts = null;
        }
        else {
            withSort(s.getSortedBy().getValue(), s.getSortedBy().isAscending(), s.getField());
        }
        return this;
    }
    
    public FlexTableDataModel withSort(String sortedBy, boolean ascending, List<String> sortFields) {
        this.sorts = new HashMap<String, TableColumn.Sort>();
        for (String field : sortFields) {
            this.sorts.put(field, TableColumn.Sort.SORTABLE);
        }
        if (sortedBy != null) {
            this.sorts.put(sortedBy, ascending ? TableColumn.Sort.SORTED_ASC : TableColumn.Sort.SORTED_DESC);
        }
        return this;
    }

    public int getColumnCount() {
        return this.columnCount;
    }

    public int[] getColumnOrder() {
        return null;
    }

    @Override
    public String getMessage() {
        return null;
    }

    public int getRowCount() {
        return this.data.size();
    }

    public Object getValueAt(int row, int column) {
        return this.data.get(row)[column];
    }

    public String getFlipId(int row) {
        return this.flipId.get(row);
    }

    public String getRowClass(int row) {
        return row >= this.rowClass.size() ? null : this.rowClass.get(row);
    }

    public TableColumn.Sort getSort(String columnKey) {
        if (this.sorts == null || columnKey == null) {
            return TableColumn.Sort.NOTSORTABLE;
        }
        final TableColumn.Sort result = this.sorts.get(columnKey);
        return (result != null) ? result : TableColumn.Sort.NOTSORTABLE;
    }

    private void ensureRow(int row) {
        for (int r = this.data.size(); r <= row; r++) {
            this.data.add(new Object[this.columnCount]);
            this.flipId.add(null);
            this.rowClass.add(null);
        }
    }

    public void setValueAt(int row, int column, Object value) {
        ensureRow(row);
        this.data.get(row)[column] = value;
    }

    public void setFlipId(int row, String flipId) {
        ensureRow(row);
        this.flipId.set(row, flipId);
    }

    public void setRowClass(int row, String rowClass) {
        ensureRow(row);
        this.rowClass.set(row, rowClass);
    }

    public void setValuesAt(int row, Object[] values) {
        setValuesAt(row, values, null);
    }

    public void setValuesAt(int row, Object[] values, String flipId) {
        assert values.length == this.columnCount;
        if (row >= this.data.size()) {
            ensureRow(row - 1);
            this.data.add(values);
            this.flipId.add(flipId);
        }
        else {
            this.data.set(row, values);
            this.flipId.set(row, flipId);
        }
    }

    public void addValues(Object[] values) {
        addValues(values, null);
    }

    public void addValues(Object[] values, String flipId) {
        assert values.length == this.columnCount;
        this.data.add(values);
        this.flipId.add(flipId);
    }

    public void setMetaData(int row, int column, CellMetaData metaData) {
        for (int r = this.cellMetaData.size(); r <= row; r++) {
            this.cellMetaData.add(new CellMetaData[this.columnCount]);
        }
        this.cellMetaData.get(row)[column] = metaData;
    }

    public CellMetaData getMetaData(int row, int column) {
        return this.cellMetaData.size() <= row ? null : this.cellMetaData.get(row)[column];
    }
}
