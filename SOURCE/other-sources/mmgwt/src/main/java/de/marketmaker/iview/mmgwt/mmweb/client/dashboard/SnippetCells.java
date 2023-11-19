package de.marketmaker.iview.mmgwt.mmweb.client.dashboard;

/**
 * Author: umaurer
 * Created: 29.07.15
 */
public class SnippetCells {
    final int row;
    final int col;
    final int rowSpan;
    final int colSpan;

    public SnippetCells(int row, int col, int rowSpan, int colSpan) {
        this.row = row;
        this.col = col;
        this.rowSpan = rowSpan;
        this.colSpan = colSpan;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public int getRowSpan() {
        return rowSpan;
    }

    public int getColSpan() {
        return colSpan;
    }

    public int getCellCount() {
        return this.rowSpan * this.colSpan;
    }
}
