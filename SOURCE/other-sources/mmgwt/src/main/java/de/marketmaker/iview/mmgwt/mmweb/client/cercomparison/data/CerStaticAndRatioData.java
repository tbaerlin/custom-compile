package de.marketmaker.iview.mmgwt.mmweb.client.cercomparison.data;

import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.iview.dmxml.CERDetailedStaticData;
import de.marketmaker.iview.dmxml.CERRatioData;
import de.marketmaker.iview.dmxml.EDGData;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.StaticDataCER;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.StaticDataCERTypeTabConfig;
import de.marketmaker.iview.mmgwt.mmweb.client.table.CellData;
import de.marketmaker.iview.mmgwt.mmweb.client.table.RowData;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableColumnAndData;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;

import java.util.ArrayList;
import java.util.List;

/**
 * Created on 16.09.2010 15:40:08
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author Michael Lösch
 */
public class CerStaticAndRatioData implements CerData {
    private final TableColumnAndData<StaticDataCER> colAndData;
    private final DmxmlContext.Block<CERDetailedStaticData> blockCerStatic;
    private final DmxmlContext.Block<CERRatioData> blockRatios;
    private final DmxmlContext.Block<EDGData> blockEdg;


    public CerStaticAndRatioData(DmxmlContext.Block<CERDetailedStaticData> blockCerStatic,
                                 DmxmlContext.Block<CERRatioData> blockRatios,
                                 DmxmlContext.Block<EDGData> blockEdg,
                                 String certType) {
        this.blockCerStatic = blockCerStatic;
        this.blockRatios = blockRatios;
        this.blockEdg = blockEdg;

        this.colAndData = StaticDataCERTypeTabConfig.INSTANCE.getTableColumnAndData(certType);
    }


    public String[] getNames() {
        if (!this.blockCerStatic.isResponseOk() || !this.blockEdg.isResponseOk() || !this.blockRatios.isResponseOk()) {
            return null;
        }

        final StaticDataCER data =
                new StaticDataCER(this.blockCerStatic.getResult(), this.blockRatios.getResult(), this.blockEdg.getResult());
        final List<RowData> rowDatas = this.colAndData.getRowData(data);

        final List<String> names = new ArrayList<String>(rowDatas.size() * 2);

        boolean hasColumn = true;
        for (int column = 0; hasColumn; column += 3) {
            hasColumn = false;
            for (RowData rowData : rowDatas) {
                final Object[] row = rowData.getData();
                if (row.length > column) {
                    hasColumn = true;
                    final Object o = row[column];
                    if (o != null && o instanceof String) {
                        names.add((String) o);
                    }
                    else {
                        Firebug.log("CerStaticAndRatioData.getNames() - not a name: " + (o == null ? "null" : o.getClass().getName()) + " - " + rd(rowData));
                    }
                }
            }
        }
        return names.toArray(new String[names.size()]);
    }

    public CellData[] getValues() {
        if (!this.blockCerStatic.isResponseOk() || !this.blockEdg.isResponseOk() || !this.blockRatios.isResponseOk()) {
            return null;
        }

        final StaticDataCER data =
                new StaticDataCER(this.blockCerStatic.getResult(), this.blockRatios.getResult(), this.blockEdg.getResult());
        final List<RowData> rowDatas = this.colAndData.getRowData(data);

        final ArrayList<CellData> values = new ArrayList<CellData>(rowDatas.size() * 2);

        boolean hasColumn = true;
        for (int column = 0; hasColumn; column += 3) {
            hasColumn = false;
            for (RowData rowData : rowDatas) {
                final Object[] row = rowData.getData();
                if (row.length > column) {
                    hasColumn = true;
                    final Object o = row[column + 1];
                    if (o != null && o instanceof CellData) {
                        values.add((CellData) o);
                    }
                    else {
                        Firebug.log("CerStaticAndRatioData.getValues() - not a value: " + (o == null ? "null" : o.getClass().getName()) + " - " + rd(rowData));
                    }
                }
            }
        }
        return values.toArray(new CellData[values.size()]);
    }

    private String rd(RowData rowData) {
        final Object[] data = rowData.getData();
        final StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (Object o : data) {
            if (first) {
                sb.append('[');
                first = false;
            }
            else {
                sb.append(',');
            }
            sb.append(o == null ? "null" : o.toString()); // $NON-NLS$
        }
        sb.append(']');
        return sb.toString();
    }
}
