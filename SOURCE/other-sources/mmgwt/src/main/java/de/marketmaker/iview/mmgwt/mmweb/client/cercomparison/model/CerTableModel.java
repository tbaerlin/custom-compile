package de.marketmaker.iview.mmgwt.mmweb.client.cercomparison.model;

import com.google.gwt.user.client.rpc.AsyncCallback;
import de.marketmaker.iview.dmxml.ResponseType;
import de.marketmaker.iview.mmgwt.mmweb.client.cercomparison.data.CerCompareChartData;
import de.marketmaker.iview.mmgwt.mmweb.client.cercomparison.data.CerDataSection;
import de.marketmaker.iview.mmgwt.mmweb.client.cercomparison.events.UpdateViewEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.table.CellData;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author umaurer
 */
public class CerTableModel {
    private final ArrayList<CerColumnModel> listColumns = new ArrayList<>();
    private String typeKey = null;
    private HashMap<CerDataSection, String[]> mapSectionTitles = null;
    private final CerCompareChartData compareChart;

    public CerTableModel(CerCompareChartData compareChart) {
        this.compareChart = compareChart;
    }

    public void setSectionTitles(HashMap<CerDataSection, String[]> mapSectionTitles) {
        if (this.mapSectionTitles != null) {
            throw new RuntimeException("CerTableModel.setSectionTitles() - mapSectionTitles already initialized"); // $NON-NLS$
        }
        this.mapSectionTitles = mapSectionTitles;
    }

    public String[] getSectionTitles(CerDataSection section) {
        return this.mapSectionTitles.get(section);
    }

    public CerCompareChartData getCompareChart() {
        return this.compareChart;
    }

    public void add(CerColumnModel column, String typeKey) {
        if (this.typeKey == null) {
            this.typeKey = typeKey;
        }
        else if (!this.typeKey.equals(typeKey)) {
            throw new RuntimeException("cannot set typeKey '" + typeKey + "' when typeKey '" + this.typeKey + "' is already present"); // $NON-NLS$
        }
        this.listColumns.add(column);
        UpdateViewEvent.fire();
    }

    public int getColumnCount() {
        return this.listColumns.size();
    }

    public CerColumnModel getColumn(int col) {
        return this.listColumns.get(col);
    }

    public void removeColumn(int col) {
        this.listColumns.remove(col);
        if (this.listColumns.isEmpty()) {
            this.typeKey = null;
            this.mapSectionTitles = null;
        }
        this.compareChart.removeSymbol(col, new AsyncCallback<ResponseType>() {
                public void onFailure(Throwable caught) {
                }

                public void onSuccess(ResponseType result) {
                    UpdateViewEvent.fire();
                }
            });
    }

    public boolean isTypeKeyAllowed(String typeKey) {
        return this.typeKey == null || this.typeKey.equals(typeKey);
    }

    public String getTypeKey() {
        return typeKey;
    }

    public void setTypeKey(String typeKey) {
        this.typeKey = typeKey;
    }

    public void computeRanking() {
        final ArrayList<ArrayList<CellData>> listCellDataRows = new ArrayList<>();
        for (CerDataSection section : CerDataSection.values()) {
            if(section == CerDataSection.CHART || section == CerDataSection.COMPARE_CHART) {
                continue;
            }
            final String[] sectionTitles = this.mapSectionTitles.get(section);
            //noinspection UnusedDeclaration
            for (String title : sectionTitles) {
                listCellDataRows.add(new ArrayList<CellData>());
            }
        }
        for (CerColumnModel column : this.listColumns) {
            int row = 0;
            for (CerDataSection section : CerDataSection.values()) {
                if(section == CerDataSection.CHART || section == CerDataSection.COMPARE_CHART) {
                    continue;
                }

                final CellData[] datas = column.getCellDatas(section);
                for (CellData data : datas) {
                    listCellDataRows.get(row).add(data);
                    row++;
                }
            }
        }
        for (ArrayList<CellData> cellDataRow : listCellDataRows) {
            final CellData first = cellDataRow.get(0);
            first.computeRanking(cellDataRow);
        }
    }
}
