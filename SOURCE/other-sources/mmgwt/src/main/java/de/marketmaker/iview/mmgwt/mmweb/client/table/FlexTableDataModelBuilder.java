/*
 * TableDataModelBuilder.java
 *
 * Created on 03.07.2009 09:31:56
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.table;

import java.util.ArrayList;

/**
 * Table data model builder in which the number of rows does not have to be defined in
 * the constructor.
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class FlexTableDataModelBuilder {
    private final ArrayList<Object[]> rows = new ArrayList<Object[]>();

    public FlexTableDataModelBuilder addRow(Object... row) {
        if (!rows.isEmpty() && rows.get(0).length != row.length) {
            throw new IllegalArgumentException("required " + rows.get(0).length + " rows, got " + row.length); // $NON-NLS-0$ $NON-NLS-1$
        }
        rows.add(row);
        return this;
    }

    public TableDataModel getResult() {
        return DefaultTableDataModel.create(this.rows);
    }
}
