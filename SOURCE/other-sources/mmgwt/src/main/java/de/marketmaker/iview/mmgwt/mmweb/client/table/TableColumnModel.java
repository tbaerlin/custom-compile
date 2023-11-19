/*
 * TableColumnModel.java
 *
 * Created on 20.03.2008 14:49:43
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.table;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public interface TableColumnModel {
    TableColumn getTableColumn(int column);

    int getColumnCount();

    boolean isHeaderVisible();

    /**
     * Returns this model's id. Only models with an id can be configured (i.e. select columns
     * and column order to be shown).
     * @return the model's id
     */
    String getId();

    /**
     * Returns the order of columns in this model. If the user configured the columns/column order
     * for this table, that order will be returned, otherwise the {@link #getDefaultColumnOrder()}
     * @return column order, must never be null.
     */
    int[] getColumnOrder();

    /**
     * Returns the default column order for this table.
     * @return default column order
     */
    int[] getDefaultColumnOrder();

    /**
     * Returns the column order based on the given columnIds
     * @param columnIds ids of columns for which order is requested
     * @return column order
     */
    int[] getColumnOrder(String[] columnIds);

    boolean configContainsColumn(String columnId);
}
