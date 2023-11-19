package de.marketmaker.iview.mmgwt.mmweb.client.dashboard;

/**
 * Author: umaurer
 * Created: 29.07.15
 */
public class TableCellPos {
    final int row;
    final int tCol;
    final int mCol;

    public TableCellPos(int row, int tCol, int mCol) {
        this.row = row;
        this.tCol = tCol;
        this.mCol = mCol;
    }

    public int getRow() {
        return row;
    }

    public int getTCol() {
        return tCol;
    }

    public int getMCol() {
        return mCol;
    }

    @Override
    public String toString() {
        return "c(" + row + ", t" + tCol + "=m" + mCol + ")"; // $NON-NLS$
    }
}
