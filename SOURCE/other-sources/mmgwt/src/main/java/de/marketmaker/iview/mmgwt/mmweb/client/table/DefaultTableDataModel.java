/*
 * DefaultTableModel.java
 *
 * Created on 19.03.2008 16:30:10
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.table;

import java.util.HashMap;
import java.util.List;

import de.marketmaker.iview.dmxml.Sort;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class DefaultTableDataModel implements TableDataModel {
    public static final String[] ROW_CLASSES = new String[]{null, "uneven-row"}; // $NON-NLS-0$

    final Object[][] data;

    private CellMetaData[][] cellMetaData;

    private final String[] flipId;

    private final String[] rowClass;

    private final int rowCount;

    private final int columnCount;

    private HashMap<String, TableColumn.Sort> sorts;

    public static DefaultTableDataModel NULL = new DefaultTableDataModel(0, 0);

    private int[] columnOrder;

    private String message;

    public class CellPointer {
        private final int row;
        private final int column;

        CellPointer(int row, int column) {
            this.row = row;
            this.column = column;
        }

        public CellMetaData withMetaData() {
            final CellMetaData metaData = new CellMetaData();
            DefaultTableDataModel.this.setMetaData(this.row, this.column, metaData);
            return metaData;
        }
    }

    public static DefaultTableDataModel create(String message) {
        return (message != null) ? new DefaultTableDataModel(message) : NULL;
    }

    public DefaultTableDataModel(String message) {
        this(0, 0, null);
        this.message = message;
    }

    public DefaultTableDataModel(int rowCount, int columnCount, Sort s) {
        this.rowCount = rowCount;
        this.columnCount = columnCount;
        this.data = new Object[this.rowCount][];
        this.flipId = new String[this.rowCount];
        this.rowClass = new String[this.rowCount];
        withSort(s);
    }

    public DefaultTableDataModel(int rowCount, int columnCount) {
        this(rowCount, columnCount, null);
    }

    public static <V> DefaultTableDataModel create(List<V> elements, RowMapper<V> mapper) {
        return create(elements, mapper, 0, elements.size());
    }

    public static DefaultTableDataModel create(List<Object[]> rows) {
        return create(rows, new AbstractRowMapper<Object[]>() {
            public Object[] mapRow(Object[] objects) {
                return objects;
            }

            public String getFlipId(Object[] objects) {
                return null;
            }
        });
    }

    public static DefaultTableDataModel createWithRowData(List<RowData> rows) {
        return create(rows, new AbstractRowMapper<RowData>() {
            public Object[] mapRow(RowData rowData) {
                return rowData.getData();
            }

            public String getFlipId(RowData rowData) {
                return rowData.getFlipId();
            }
        });
    }

    public static <V> DefaultTableDataModel create(List<V> elements, RowMapper<V> mapper, int offset, int count) {
        if (elements == null || elements.isEmpty()) {
            return NULL;
        }
        final int end = Math.min(offset + count, elements.size());

        DefaultTableDataModel result = null;
        int row = 0;
        for (int i = offset; i < end; i++) {
            final V v = elements.get(i);
            final Object[] values = mapper.mapRow(v);
            if (result == null) {
                result = new DefaultTableDataModel(end - offset, values.length);
            }
            result.setValuesAt(row, values, mapper.getFlipId(v));
            result.setRowClass(row, mapper.getRowClass(row, v));
            row++;
        }

        return result != null ? result : NULL;
    }

    public DefaultTableDataModel withRowClasses(String[] classes) {
        for (int i = 0; i < this.rowCount; i++) {
            this.rowClass[i] = classes[i % classes.length];
        }
        return this;
    }

    public DefaultTableDataModel withSort(Sort s) {
        if (s == null) {
            this.sorts = null;
        }
        else {
            this.sorts = new HashMap<String, TableColumn.Sort>();
            for (String field : s.getField()) {
                this.sorts.put(field, TableColumn.Sort.SORTABLE);
            }
            this.sorts.put(s.getSortedBy().getValue(),
                    s.getSortedBy().isAscending() ? TableColumn.Sort.SORTED_ASC : TableColumn.Sort.SORTED_DESC);
        }
        return this;
    }

    public int getColumnCount() {
        return this.columnCount;
    }

    public int getRowCount() {
        return this.rowCount;
    }

    public Object getValueAt(int row, int column) {
        if (this.data[row] != null) {
            return this.data[row][column];
        }
        return null;
    }

    public CellPointer withValue(int row, int column, Object value) {
        setValueAt(row, column, value);
        return new CellPointer(row, column);
    }

    public void setValueAt(int row, int column, Object value) {
        if (this.data[row] == null) {
            this.data[row] = new Object[this.columnCount];
        }
        this.data[row][column] = value;
    }

    public void setValuesAt(int row, Object[] values) {
        setValuesAt(row, values, null);
    }

    public void setValuesAt(int row, Object[] values, String flipId) {
        assert row >= 0 && row < this.rowCount
                : "the row count or index is invalid, index value was " + row + " row count is " + this.rowCount;  // $NON-NLS$
        assert values.length == this.columnCount
                : "the column count of data and view doesn't match, data columns: " + values.length + ", renderer columns: " + this.columnCount;  // $NON-NLS$
        this.data[row] = values;
        this.flipId[row] = flipId;
    }

    public void setMetaData(int row, int column, CellMetaData metaData) {
        assert row >= 0 && row < this.rowCount
                : "the row count or index is invalid, index value was " + row + " row count is " + this.rowCount;  // $NON-NLS$
        if (this.cellMetaData == null) {
            this.cellMetaData = new CellMetaData[this.rowCount][this.columnCount];
        }
        this.cellMetaData[row][column] = metaData;
    }

    public CellMetaData getMetaData(int row, int column) {
        return (this.cellMetaData != null) ? this.cellMetaData[row][column] : null;
    }

    public void setFlipId(int row, String flipId) {
        this.flipId[row] = flipId;
    }

    public String getFlipId(int row) {
        return this.flipId[row];
    }


    public void setRowClass(int row, String rowClass) {
        this.rowClass[row] = rowClass;
    }

    public String getRowClass(int row) {
        return this.rowClass[row];
    }


    public TableColumn.Sort getSort(String columnKey) {
        if (this.sorts == null || columnKey == null) {
            return TableColumn.Sort.NOTSORTABLE;
        }
        final TableColumn.Sort result = this.sorts.get(columnKey);
        return (result != null) ? result : TableColumn.Sort.NOTSORTABLE;
    }

    @Override
    public String getMessage() {
        return this.message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public int[] getColumnOrder() {
        return this.columnOrder;
    }

    public void setColumnOrder(int[] columnOrder) {
        this.columnOrder = columnOrder;
    }
}
