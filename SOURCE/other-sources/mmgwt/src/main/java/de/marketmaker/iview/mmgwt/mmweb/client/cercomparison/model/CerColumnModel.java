package de.marketmaker.iview.mmgwt.mmweb.client.cercomparison.model;

import de.marketmaker.iview.mmgwt.mmweb.client.cercomparison.data.CerDataSection;
import de.marketmaker.iview.mmgwt.mmweb.client.table.CellData;

import java.util.HashMap;

/**
 * @author umaurer
 */
public class CerColumnModel {
    private final HashMap<CerDataSection, CellData[]> mapCellDatas;

    public CerColumnModel(HashMap<CerDataSection, CellData[]> mapCellDatas) {
        this.mapCellDatas = mapCellDatas;
    }

    public CellData[] getCellDatas(CerDataSection section) {
        return this.mapCellDatas.get(section);
    }
}
