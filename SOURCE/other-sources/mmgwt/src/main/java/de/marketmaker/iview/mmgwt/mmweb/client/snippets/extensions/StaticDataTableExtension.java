/*
 * StaticDataTableExtensionPoint.java
 *
 * Created on 23.10.2012 14:00:00
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.snippets.extensions;

import de.marketmaker.iview.mmgwt.mmweb.client.data.InstrumentTypeEnum;
import de.marketmaker.iview.mmgwt.mmweb.client.table.CellMetaData;
import de.marketmaker.iview.mmgwt.mmweb.client.table.RowData;

import java.util.List;

/**
 * @author Markus Dick
 */
public interface StaticDataTableExtension extends Extension {
    void setSymbol(InstrumentTypeEnum type, String symbol, String name);
    void addData(List<RowData> list, List<CellMetaDataEntry> metaDataEntries, int columnCount);

    class CellMetaDataEntry {
        private int row;
        private int column;
        private CellMetaData cellMetaData;

        public CellMetaDataEntry(int row, int column, CellMetaData cellMetaData) {
            this.row = row;
            this.column = column;
            this.cellMetaData = cellMetaData;
        }

        public int getRow() {
            return row;
        }

        public int getColumn() {
            return column;
        }

        public CellMetaData getCellMetaData() {
            return cellMetaData;
        }
    }
}
