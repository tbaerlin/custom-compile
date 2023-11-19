/*
 * TableModel.java
 *
 * Created on 19.03.2008 16:29:01
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.table;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public interface TableDataModel {

    public enum MetaData {
        BLANK, HEADER
    }

    int getColumnCount();

    int getRowCount();

    Object getValueAt(int row, int column);

    CellMetaData getMetaData(int row, int column);

    String getFlipId(int row);
    
    String getRowClass(int row);

    TableColumn.Sort getSort(String columnKey);

    /**
     * Returns a specific order of columns for this data set. The returned array may contain only
     * a subset of the columns and in that case only this subset will be rendered. Elements of
     * the array can be numbers from <tt>0 .. getColumnCount()-1</tt><p>
     * Example: returning [2, 3, 1] will render columns 2, 3, 1 in that order.
     * @return column order or null if default order is ok
     */
    int[] getColumnOrder();

    /**
     * If not null, a message that will be rendered instead of the table (e.g., an explanatory
     * text)
     * @return message, may be null
     */
    String getMessage();
}
