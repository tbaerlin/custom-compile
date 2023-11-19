/*
 * TableDataModelBuilder.java
 *
 * Created on 03.07.2009 09:31:56
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.table;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class TableDataModelBuilder {
    private final DefaultTableDataModel tdm;

    private int row = 0;

    private final int colCount;

    public TableDataModelBuilder(int rowCount, int colCount) {
        this.tdm = new DefaultTableDataModel(rowCount, colCount);
        this.colCount = colCount;
    }

    public TableDataModelBuilder addRow(Object... row) {
        if (row.length != this.colCount) {
            throw new IllegalArgumentException("required " + colCount + " rows, got " + row.length); // $NON-NLS-0$ $NON-NLS-1$
        }
        this.tdm.setValuesAt(this.row++, row);
        return this;
    }

    public TableDataModel getResult() {
        return this.tdm;
    }
}
