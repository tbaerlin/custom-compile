package de.marketmaker.iview.mmgwt.mmweb.client.widgets;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTMLTable;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import de.marketmaker.itools.gwtutil.client.util.CssUtil;
import de.marketmaker.itools.gwtutil.client.widgets.Tooltip;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.TextUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.NaturalComparator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Author: umaurer
 * Created: 20.08.14
 */
public class SortableTableFactory {
    private static final NaturalComparator<String> NAT_COMP = NaturalComparator.createDefault();
    private static final String STYLE_SORTABLE = "sortable"; // $NON-NLS$
    private static final String STYLE_ASCENDING = "ascending"; // $NON-NLS$
    private static final String STYLE_DESCENDING = "descending"; // $NON-NLS$
    private static final String ATT_CELL_ID = "cellId"; // $NON-NLS$

    private static int cellCounter = 0;

    private final String[] headers;
    private final SortSpec[] sortSpecs;
    private final LinkedList<SortSpec> sortSpecList = new LinkedList<>();
    private final List<Cell[]> listRows = new ArrayList<>();
    private final Map<String, Cell> mapCells = new HashMap<>();

    public static class Cell {
        private final String id = String.valueOf(cellCounter++);
        private final String sortString;
        private final Widget widget;
        private String qtip;
        private String style;
        private final ClickHandler clickHandler;

        public Cell(String text, String qtip, ClickHandler clickHandler) {
            this(text, new Label(text), qtip, clickHandler);
        }

        public Cell(String sortString, String text, String qtip, ClickHandler clickHandler) {
            this(sortString, new Label(text), qtip, clickHandler);
        }

        public Cell(String sortString, Widget widget, String qtip, ClickHandler clickHandler) {
            this.sortString = sortString;
            this.widget = widget;
            this.qtip = qtip;
            this.clickHandler = clickHandler;
        }

        public Cell withAutoCompletion() {
            Tooltip.addAutoCompletion(this.widget);
            return this;
        }

        public Cell withStyle(String style) {
            this.style = style;
            return this;
        }
    }

    private class SortSpec {
        private final int col;
        private boolean ascending = true;

        private SortSpec(int col) {
            this.col = col;
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof SortSpec && ((SortSpec)obj).col == this.col;
        }
    }

    public SortableTableFactory(String... headers) {
        this.headers = headers;
        this.sortSpecs = new SortSpec[headers.length];
        for (int col = 0; col < this.sortSpecs.length; col++) {
            this.sortSpecs[col] = new SortSpec(col);
            this.sortSpecList.add(this.sortSpecs[col]);
        }
    }

    public void addRow(Cell... cells) {
        if (cells.length != this.headers.length) {
            throw new IllegalArgumentException("cell count (" + cells.length + ") does not match column header count (" + this.headers.length + ")"); // $NON-NLS$
        }
        this.listRows.add(cells);
        for (Cell cell : cells) {
            this.mapCells.put(cell.id, cell);
        }
    }

    public FlexTable createTable(int sortColumn, boolean ascending) {
        final FlexTable table = new FlexTable();
        table.setCellPadding(0);
        table.setCellSpacing(0);
        table.setStyleName("mm-simpleTable");

        if (sortColumn >= 0 && sortColumn < this.headers.length) {
            sort(this.listRows, sortColumn, ascending);
        }
        renderTable(table, sortColumn, ascending);

        table.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                final HTMLTable.Cell tableCell = table.getCellForEvent(event);
                if (tableCell.getRowIndex() == 0) {
                    sortByColumn(table, tableCell.getCellIndex());
                }
                else {
                    onCellClicked(table.getFlexCellFormatter().getElement(tableCell.getRowIndex(), tableCell.getCellIndex()).getAttribute(ATT_CELL_ID), event);
                }
            }
        });

        return table;
    }

    private void sort(List<Cell[]> listRows, int col, boolean ascending) {
        moveSortSpecToTop(col, ascending);
        Collections.sort(listRows, new ColumnComparator());
    }

    private void moveSortSpecToTop(int col, boolean ascending) {
        this.sortSpecList.remove(this.sortSpecs[col]);
        this.sortSpecList.addFirst(this.sortSpecs[col]);
        this.sortSpecs[col].ascending = ascending;
    }

    private class ColumnComparator implements Comparator<Cell[]> {
        @Override
        public int compare(Cell[] row1, Cell[] row2) {
            for (SortSpec sortSpec : sortSpecList) {
                final int result = sortSpec.ascending
                        ? NAT_COMP.compareIgnoreCase(row1[sortSpec.col].sortString, row2[sortSpec.col].sortString)
                        : NAT_COMP.compareIgnoreCase(row2[sortSpec.col].sortString, row1[sortSpec.col].sortString);
                if (result != 0) {
                    return result;
                }
            }
            return 0;
        }
    }

    private void sortByColumn(FlexTable table, int col) {
        if (this.headers[col] == null) {
            return;
        }
        boolean ascending = !CssUtil.hasStyle(table.getFlexCellFormatter().getElement(0, col).getClassName(), STYLE_ASCENDING);
        sort(this.listRows, col, ascending);
        renderTable(table, col, ascending);
    }

    private void onCellClicked(String cellId, ClickEvent clickEvent) {
        final Cell cell = this.mapCells.get(cellId);
        if (cell == null || cell.clickHandler == null) {
            return;
        }
        cell.clickHandler.onClick(clickEvent);
    }

    private void renderTable(FlexTable table, int sortColumn, boolean ascending) {
        final HTMLTable.RowFormatter rowFormatter = table.getRowFormatter();
        final FlexTable.FlexCellFormatter formatter = table.getFlexCellFormatter();

        table.clear(true);

        for (int col = 0; col < this.headers.length; col++) {
            if (this.headers[col] == null) {
                table.setHTML(0, col, "&nbsp;"); // $NON-NLS$
            }
            else {
                table.setText(0, col, this.headers[col]);
                formatter.getElement(0, col).setClassName(STYLE_SORTABLE);
                if (col == sortColumn) {
                    formatter.getElement(0, col).addClassName(ascending ? STYLE_ASCENDING : STYLE_DESCENDING);
                }
            }
        }
        rowFormatter.setStyleName(0, "header");

        for (int row = 1, rowCount = this.listRows.size(); row <= rowCount; row++) {
            final Cell[] cells = this.listRows.get(row - 1);
            for (int col = 0; col < cells.length; col++) {
                final Cell cell = cells[col];
                if (cell != null && cell.widget != null) {
                    table.setWidget(row, col, cell.widget);
                    final Element element = formatter.getElement(row, col);
                    element.setAttribute(ATT_CELL_ID, cell.id);
                    if (cell.clickHandler != null) {
                        formatter.setStyleName(row, col, "mm-link");
                    }
                    if (cell.qtip != null) {
                        element.setAttribute(Tooltip.ATT_COMPLETION, cell.qtip);
                    }
                    if(cell.style != null) {
                        formatter.addStyleName(row, col, cell.style);
                    }
                }
            }
            rowFormatter.setStyleName(row, "mm-simpleTable-dataRow");
        }
    }
}
