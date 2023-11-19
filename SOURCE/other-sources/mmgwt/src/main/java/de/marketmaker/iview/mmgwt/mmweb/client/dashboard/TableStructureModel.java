package de.marketmaker.iview.mmgwt.mmweb.client.dashboard;

import de.marketmaker.itools.gwtutil.client.util.Firebug;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: umaurer
 * Created: 15.06.15
 */
public class TableStructureModel<U> {
    class Cell {
        private final U userObject;
        private final int row;
        private final int tCol;
        private final int rowSpan;
        private final int colSpan;
        private final boolean masterCell;

        public Cell(int row, int tCol) {
            this(null, row, tCol, 1, 1);
        }

        public Cell(U userObject, int row, int tCol, int rowSpan, int colSpan) {
            this(userObject, row, tCol, rowSpan, colSpan, true);
        }

        private Cell(U userObject, int row, int tCol, int rowSpan, int colSpan, boolean masterCell) {
            this.userObject = userObject;
            this.row = row;
            this.tCol = tCol;
            this.rowSpan = rowSpan;
            this.colSpan = colSpan;
            this.masterCell = masterCell;
        }

        public Cell asSpanCell() {
            return this.masterCell
                    ? new Cell(this.userObject, this.row, this.tCol, this.rowSpan, this.colSpan, false)
                    : this;
        }

        public boolean isMasterCell() {
            return this.masterCell;
        }

        public int getRow() {
            return this.row;
        }

        public int getTCol() {
            return this.tCol;
        }

        public Cell with(int row, int col) {
            return new Cell(this.userObject, row, col, this.rowSpan, this.colSpan);
        }

        public U getUserObject() {
            return this.userObject;
        }

        @Override
        public String toString() {
            return "Cell{" + // $NON-NLS$
                    "row=" + row + // $NON-NLS$
                    ", tCol=" + tCol + // $NON-NLS$
                    ", rowSpan=" + rowSpan + // $NON-NLS$
                    ", colSpan=" + colSpan + // $NON-NLS$
                    ", masterCell=" + masterCell + // $NON-NLS$
                    '}';
        }
    }

    private final List<List<Cell>> rows = new ArrayList<>();
    private int colCount = 0;

    public void add(U userObject, String sRow, String sCol, String sRowSpan, String sColSpan) {
        final int row = sRow == null ? this.rows.size() : Integer.parseInt(sRow);
        final int col = sCol == null ? 0 : Integer.parseInt(sCol);
        final int rowSpan = sRowSpan == null ? 1 : Integer.parseInt(sRowSpan);
        final int colSpan = sColSpan == null ? 1 : Integer.parseInt(sColSpan);

        add(userObject, row, col, rowSpan, colSpan);
    }

    public void add(U userObject, int row, int tCol, int rowSpan, int colSpan) {
        Cell cell = new Cell(userObject, row, tCol, rowSpan, colSpan);

        ensureRowCount(this.rows, row + rowSpan);

        final int mCol = toModelCol(row, tCol);
        final int neededColCount = mCol + colSpan;
        if (this.colCount < neededColCount) {
            this.colCount = neededColCount;
        }

        for (int r = row, rmax = row + rowSpan; r < rmax; r++) {
            final List<Cell> cells = this.rows.get(r);
            ensureColCount(cells, neededColCount);

            for (int mc = mCol, cmax = mCol + colSpan; mc < cmax; mc++) {
                final Cell previous = cells.set(mc, cell);
                if (previous != null) {
                    Firebug.warn("TableStructureModel <add> place for " + cell + " already occupied by " + previous);
                }
                cell = cell.asSpanCell();
            }
        }
    }

    private void ensureRowCount(List<List<Cell>> rows, int rowCount) {
        if (rows.size() < rowCount) {
            final int moreRows = rowCount - rows.size();
            for (int i = 0; i < moreRows; i++) {
                rows.add(new ArrayList<Cell>());
            }
        }
    }

    private void ensureColCount(List<Cell> cells, int colCount) {
        if (cells.size() < colCount) {
            final int moreEntries = colCount - cells.size();
            for (int i = 0; i < moreEntries; i++) {
                cells.add(null);
            }
        }
    }

    public List<Cell> getCells() {
        final ArrayList<Cell> list = new ArrayList<>();
        for (int row = 0, rowCount = this.rows.size(); row < rowCount; row++) {
            final List<Cell> cols = this.rows.get(row);
            int tCol = 0;
            int mCol = 0;
            while (mCol < cols.size()) {
                final Cell cell = cols.get(mCol);
                if (cell == null) {
                    list.add(new Cell(row, tCol));
                    tCol++;
                }
                else if (cell.isMasterCell()) {
                    list.add(cell.with(row, tCol));
                    tCol++;
                }
                mCol++;
            }
            while (mCol <= this.colCount) { // add one more column (<= instead of <)
                list.add(new Cell(row, tCol));
                tCol++;
                mCol++;
            }
        }

        // add one more row
        int row = this.rows.size();
        for (int tCol = 0; tCol <= this.colCount; tCol++) {
            list.add(new Cell(row, tCol));
        }
        return list;
    }

    private Cell getCellByTCol(int row, int tCol) {
        if (row >= this.rows.size()) {
            return null;
        }
        final List<Cell> cells = this.rows.get(row);
        final int mCol = toModelCol(cells, tCol);
        return mCol >= cells.size()
                ? null
                : cells.get(mCol);
    }

    private Cell getCellByMCol(int row, int mCol) {
        if (row >= this.rows.size()) {
            return null;
        }
        final List<Cell> cells = this.rows.get(row);
        return mCol >= cells.size()
                ? null
                : cells.get(mCol);
    }

    public U getUserObject(int row, int tCol) {
        final Cell cell = getCellByTCol(row, tCol);
        return cell == null
                ? null
                : cell.getUserObject();
    }

    public int toModelCol(int row, int tCol) {
        return row >= this.rows.size()
                ? tCol
                : toModelCol(this.rows.get(row), tCol);
    }

    private int toModelCol(List<Cell> cells, int tCol) {
        int mCol = 0;
        int tc = 0;
        while (mCol < cells.size()) {
            final Cell cell = cells.get(mCol);
            if (cell == null) {
                if (tc >= tCol) {
                    return mCol;
                }
                tc++;
                mCol++;
            }
            else {
                if (cell.isMasterCell()) {
                    if (tc >= tCol) {
                        return mCol;
                    }
                    tc++;
                }
                mCol += cell.colSpan;
            }
        }
        mCol += tCol - tc;
        return mCol;
    }

    public int toTableCol(int row, int mCol) {
        return row >= this.rows.size()
                ? mCol
                : toTableCol(this.rows.get(row), mCol);
    }

    private int toTableCol(List<Cell> cells, int mCol) {
        int tCol = 0;
        int mc = 0;
        while (mc < cells.size()) {
            if (mc >= mCol) {
                return tCol;
            }
            final Cell cell = cells.get(mc);
            if (cell == null) {
                tCol++;
                mc++;
            }
            else {
                if (cell.isMasterCell()) {
                    tCol++;
                }
                mc += cell.colSpan;
            }
        }
        return tCol + mCol - mc;
    }

    public void move(int fromRow, int fromTCol, int toRow, int toTCol) {
        final Cell cell = remove(fromRow, fromTCol);
        if (fromTCol < toTCol && toRow >= fromRow && toRow < fromRow + cell.rowSpan) {
            toTCol += cell.colSpan - 1;
        }
        add(cell.getUserObject(), toRow, toTCol, cell.rowSpan, cell.colSpan);
    }

    public Cell remove(int fromRow, int fromTCol) {
        boolean needsFix = false;
        final int fromMCol = toModelCol(fromRow, fromTCol);
        final Cell cell = this.rows.get(fromRow).set(fromMCol, null);
        for (int row = fromRow, endRow = fromRow + cell.rowSpan; row < endRow; row++) {
            for (int col = fromMCol, endCol = fromMCol + cell.colSpan; col < endCol; col++) {
                this.rows.get(row).set(col, null);
                needsFix |= row >= this.rows.size() - 1 || col >= this.colCount - 1;
            }
        }
        if (needsFix) {
            fixDimensions();
        }
        return cell;
    }

    private void fixDimensions() {
        removeEmptyBottomRows();
        this.colCount = removeEmptyRightCols();
    }

    private void removeEmptyBottomRows() {
        int lastRow = this.rows.size() - 1;
        while (lastRow >= 0 && !hasCells(this.rows.get(lastRow))) {
            this.rows.remove(lastRow);
            lastRow --;
        }
    }

    private boolean hasCells(List<Cell> cells) {
        for (Cell cell : cells) {
            if (cell != null) {
                return true;
            }
        }
        return false;
    }

    private int removeEmptyRightCols() {
        int colCount = 0;
        for (List<Cell> cols : this.rows) {
            for (int col = cols.size() - 1; col >= 0; col--) {
                if (cols.get(col) == null) {
                    cols.remove(col);
                }
                else {
                    if (col >= colCount) {
                        colCount = col + 1;
                    }
                    break;
                }
            }
        }
        return colCount;
    }

    public void setSpan(int row, int tCol, int rowSpan, int colSpan) {
        final Cell cell = remove(row, tCol);
        add(cell.getUserObject(), row, tCol, rowSpan, colSpan);
    }

    public boolean isFree(int row, int tCol, int rowSpan, int colSpan) {
        return getFreeCells(row, tCol, rowSpan, colSpan).length == rowSpan * colSpan;
    }

    public TableCellPos[] getFreeCells(int row, int tCol, int rowSpan, int colSpan) {
        final ArrayList<TableCellPos> list = new ArrayList<>(rowSpan * colSpan);
        final int mCol = toModelCol(row, tCol);
        for (int r = row, rmax = row + rowSpan; r < rmax; r++) {
            if (r >= this.rows.size()) {
                // row is not occupied in model (=> mc == tc)
                for (int mc = mCol, cmax = mCol + colSpan; mc < cmax; mc++) {
                    list.add(new TableCellPos(r, mc, mc));
                }
                continue;
            }

            for (int mc = mCol, cmax = mCol + colSpan; mc < cmax; mc++) {
                final Cell cell = getCellByMCol(r, mc);
                if (cell == null) {
                    final int tc = toTableCol(r, mc);
                    list.add(new TableCellPos(r, tc, mc));
                }
            }
        }
        return list.toArray(new TableCellPos[list.size()]);
    }
}
