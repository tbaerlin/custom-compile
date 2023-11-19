package de.marketmaker.iview.mmgwt.mmweb.client.table;

/**
 * @author Ulrich Maurer
 *         Date: 15.11.11
 */
public class CellMetaData {
    private String cellClass = null;
    private int colSpan = 1;
    private String toolTip = null;

    public CellMetaData withCellClass(String cellClass) {
        this.cellClass = cellClass;
        return this;
    }

    public CellMetaData withColSpan(int colSpan) {
        this.colSpan = colSpan;
        return this;
    }

    public CellMetaData withToolTip(String toolTip) {
        this.toolTip = toolTip;
        return this;
    }

    public String getCellClass() {
        return this.cellClass;
    }

    public int getColSpan() {
        return this.colSpan;
    }

    public String getToolTip() {
        return this.toolTip;
    }
}
