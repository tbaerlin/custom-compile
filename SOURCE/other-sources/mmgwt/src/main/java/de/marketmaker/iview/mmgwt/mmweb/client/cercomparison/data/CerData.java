package de.marketmaker.iview.mmgwt.mmweb.client.cercomparison.data;

import de.marketmaker.iview.mmgwt.mmweb.client.table.CellData;
import de.marketmaker.iview.mmgwt.mmweb.client.table.StringCellData;

/**
 * Created on 08.09.2010 14:54:39
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author Michael Lösch
 */
public interface CerData {
    public static final StringCellData EMPTY_CELL_DATA = new StringCellData(null, "--");

    public CellData[] getValues();
    public String[] getNames();
}
