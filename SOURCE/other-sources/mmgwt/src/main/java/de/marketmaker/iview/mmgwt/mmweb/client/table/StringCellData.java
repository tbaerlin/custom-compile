package de.marketmaker.iview.mmgwt.mmweb.client.table;

import java.util.List;

/**
 * Created on 22.09.2010 09:42:14
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author Michael Lösch
 */
public class StringCellData extends CellData<String> {
    public StringCellData(String value) {
        super(null, value, CellData.Sorting.NONE);
    }

    public StringCellData(String value, boolean asHtml) {
        super(null, value, CellData.Sorting.NONE, asHtml);
    }

    public StringCellData(String value, String nullValue) {
        super(null, value, nullValue, CellData.Sorting.NONE);
    }

    @Override
    public void computeRanking(List<? extends CellData> data) {
        // nothing to do
    }

}
