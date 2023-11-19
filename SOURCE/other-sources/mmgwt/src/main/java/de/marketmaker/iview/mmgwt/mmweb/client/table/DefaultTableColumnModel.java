/*
 * DefaultTableColumnModel.java
 *
 * Created on 20.03.2008 14:58:52
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.table;

import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class DefaultTableColumnModel implements TableColumnModel {
    private final TableColumn[] columns;

    private final boolean headerVisible;

    private int[] columnOrder;

    private final String id;

    private String columnOrderStr = "";

    public DefaultTableColumnModel(String id, TableColumn[] columns) {
        this(id, columns, true);
    }

    public DefaultTableColumnModel(TableColumn[] columns) {
        this(null, columns, true);
    }

    public DefaultTableColumnModel(TableColumn[] columns, boolean headerVisible) {
        this(null, columns, headerVisible);
    }

    private DefaultTableColumnModel(String id, TableColumn[] columns, boolean headerVisible) {
        this.id = id;
        this.columns = columns;
        this.headerVisible = headerVisible;
        this.columnOrder = createDefaultColumnOrder(this.columns.length);

        if (this.id != null) {
            assertValidColumnIds();
        }
    }

    private void assertValidColumnIds() {
        final Set<String> columnIds = new HashSet<>();
        for (int i = 0; i < this.columnOrder.length; i++) {
            final String columnId = this.columns[i].getId();
            if (columnId == null) {
                throw new IllegalArgumentException("Column " + i + ": id is null"); // $NON-NLS$
            }
            if (!columnIds.add(columnId)) {
                throw new IllegalArgumentException("Column " + i + ": id already exists '" + columnId + "'"); // $NON-NLS$
            }
        }
    }

    private static int[] createDefaultColumnOrder(int numColumns) {
        final int[] result = new int[numColumns];
        for (int i = 0; i < result.length; i++) {
            result[i] = i;
        }
        return result;
    }

    /**
     * If we have lots of configurable columns, this setter can be used to define the "default" // $NON-NLS-0$
     * column order. This value will NOT be used if the user has a custom column order for this
     * table stored in the AppConfig
     *
     * @param columnOrder default column order
     * @return this
     */
    public DefaultTableColumnModel withColumnOrder(String[] columnOrder) {
        this.columnOrderStr = StringUtil.join(',', columnOrder);
        this.columnOrder = mapColumnOrder(columnOrder);
        return this;
    }

    public TableColumn getTableColumn(int i) {
        return this.columns[i];
    }

    @SuppressWarnings("unused")
    public void setTableColumn(TableColumn column, int i) {
        this.columns[i] = column;
    }

    public int getColumnCount() {
        return this.columns.length;
    }

    public boolean isHeaderVisible() {
        return this.headerVisible;
    }

    /**
     * Returns the column order defined for this table. The column order may be stored in the
     * user's AppConfig.
     *
     * @return column order
     */
    public int[] getColumnOrder() {
        if (this.id != null) {
            final String orderStr = SessionData.INSTANCE.getUserProperty(this.id);
            if (orderStr != null) {
                return getColumnOrder(orderStr.split(","));
            }
        }
        return this.columnOrder;
    }

    public int[] getColumnOrder(String[] columnIds) {
        return mapColumnOrder(columnIds);
    }

    public int[] getDefaultColumnOrder() {
        return this.columnOrder;
    }

    public boolean configContainsColumn(String columnId) {
        final String orderStr = SessionData.INSTANCE.getUserProperty(this.id);
        if (orderStr != null) {
            this.columnOrderStr = orderStr;
        }
        return this.columnOrderStr.contains(columnId);
    }

    public String getId() {
        return this.id;
    }

    public void groupColumns(int from, int to, String name) {
        for (int i = from; i < to; i++) {
            this.columns[i].setGroupTitle(name);
        }
    }

    private int[] mapColumnOrder(String[] columnIds) {
        final int[] result = new int[columnIds.length];
        int m = 0;
        NEXT_ID:
        for (String columnId : columnIds) {
            for (int n = 0; n < this.columns.length; n++) {
                if (columnId.equals(this.columns[n].getId())) {
                    result[m++] = n;
                    continue NEXT_ID;
                }
            }
        }
        if (m < result.length) { // one or more invalid column ids, try to repair things:
            final String validColumnOrder = createOrderStr(result, m);
            SessionData.INSTANCE.getUser().getAppConfig().addProperty(this.id, validColumnOrder);
            return mapColumnOrder(validColumnOrder.split(","));
        }
        return result;
    }

    private String createOrderStr(int[] colOrder, int n) {
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append(this.columns[colOrder[i]].getId());
        }
        return sb.toString();
    }

    public String getOrderString() {
        final int[] order = getColumnOrder();
        return createOrderStr(order, order.length);
    }

    public int findIndexOfColumnById(String id) {
        if(!StringUtil.hasText(id)) {
            return -1;
        }
        for (int i = 0; i < this.columns.length; i++) {
            if(id.equals(this.columns[i].getId())) {
                return i;
            }
        }
        return -1;
    }
}
