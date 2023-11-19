package de.marketmaker.iview.mmgwt.mmweb.client.table;

import de.marketmaker.iview.mmgwt.mmweb.client.util.HtmlBuilder;

/**
 * a subheader implementation that watches a table column and outputs a subheader whenever the column value changes,
 * this class is immutable
 */
public class ColumnTriggeredSubheading implements Subheading {
    private final int column;

    public ColumnTriggeredSubheading(int column) {
        this.column = column;
    }

    @Override
    public boolean isSubheaderRow(TableDataModel model, int row) {
        if (row == 0) {
            return true;
        }

        final Object last = model.getValueAt(row - 1, column);
        final Object current = model.getValueAt(row, column);

        if (last == null ^ current == null) {
            return true;
        }

        if (last == null) {  // both null
            return false;
        }

        return ! last.equals(current);
    }

    @Override
    public void buildHeaderRow(HtmlBuilder hb, TableDataModel model, int columnCount, int row) {
        final Object current = model.getValueAt(row, column);
        hb.startTag("tr");  // $NON-NLS$
        hb.startTag("td");  // $NON-NLS$
        hb.addAttribute("colspan", columnCount);       // $NON-NLS$
        hb.addAttribute("class", "x-panel-header");    // $NON-NLS$
        hb.setContent(current == null ? "" : current.toString());   // also closes td    $NON-NLS$
        hb.closeLast(); // close tr
    }
}