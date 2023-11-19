package de.marketmaker.iview.mmgwt.mmweb.client.cercomparison.data;

import de.marketmaker.iview.dmxml.CERDetailedStaticData;
import de.marketmaker.iview.dmxml.CERRatioData;
import de.marketmaker.iview.dmxml.EDGData;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.StaticDataCER;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.StaticDataCERTabConfig;
import de.marketmaker.iview.mmgwt.mmweb.client.table.CellData;
import de.marketmaker.iview.mmgwt.mmweb.client.table.RowData;
import de.marketmaker.iview.mmgwt.mmweb.client.table.StringCellData;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableColumnAndData;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;

import java.util.List;

/**
 * Created on 08.09.2010 14:27:15
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author Michael Lösch
 */
public class CerStaticData implements CerData {
    private final TableColumnAndData<StaticDataCER> colAndData;
    private final DmxmlContext.Block<CERDetailedStaticData> blockCerStatic;
    private final DmxmlContext.Block<CERRatioData> blockRatios;
    private final DmxmlContext.Block<EDGData> blockEdg;

    public CerStaticData(DmxmlContext.Block<CERDetailedStaticData> blockCerStatic,
                         DmxmlContext.Block<CERRatioData> blockRatios,
                         DmxmlContext.Block<EDGData> blockEdg,
                         String certType) {
        this.blockCerStatic = blockCerStatic;
        this.blockRatios = blockRatios;
        this.blockEdg = blockEdg;

        this.colAndData = StaticDataCERTabConfig.INSTANCE.getTableColumnAndData(certType);
    }

    public CellData[] getValues() {
        if (!this.blockRatios.isResponseOk() || !this.blockEdg.isResponseOk() || !this.blockCerStatic.isResponseOk()) {
            return null;
        }
        final StaticDataCER data =
                new StaticDataCER(this.blockCerStatic.getResult(), this.blockRatios.getResult(), this.blockEdg.getResult());
        final List<RowData> rowDatas = this.colAndData.getRowData(data);

        final CellData[] values = new CellData[rowDatas.size() + 1];
        int i = 0;
        values[i++] = new StringCellData(this.blockCerStatic.getResult().getInstrumentdata().getName());

        for (RowData rowData : rowDatas) {
            final Object[] row = rowData.getData();
            values[i++] = (CellData) row[1];
        }
        return values;

    }

    public String[] getNames() {
        if (!this.blockRatios.isResponseOk() || !this.blockEdg.isResponseOk() || !this.blockRatios.isResponseOk()) {
            return null;
        }

        final StaticDataCER data = new StaticDataCER(this.blockCerStatic.getResult(), this.blockRatios.getResult(), this.blockEdg.getResult());
        final List<RowData> rowDatas = this.colAndData.getRowData(data);

        final String[] names = new String[rowDatas.size() + 1];
        int i = 0;
        names[i++] = "Name"; // $NON-NLS$

        for (RowData rowData : rowDatas) {
            final Object[] row = rowData.getData();
            names[i++] = row[0].toString();
        }

        return names;

    }
}
