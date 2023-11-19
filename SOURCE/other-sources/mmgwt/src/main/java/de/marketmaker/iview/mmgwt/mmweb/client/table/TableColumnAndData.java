package de.marketmaker.iview.mmgwt.mmweb.client.table;

import java.util.List;

/**
 * Knows about a TableColumnModel and how to turn data of type D into a list of RowData objects.
 *
 * Created on Nov 5, 2008 3:30:36 PM
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author mloesch
 */
public interface TableColumnAndData<D> {

    public TableColumnModel getTableColumnModel();

    public List<RowData> getRowData(D data);

}
