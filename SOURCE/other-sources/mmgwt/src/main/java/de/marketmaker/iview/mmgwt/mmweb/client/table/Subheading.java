package de.marketmaker.iview.mmgwt.mmweb.client.table;

import de.marketmaker.iview.mmgwt.mmweb.client.util.HtmlBuilder;

/**
 * implementing sub-headers inside a static table view
 */
public interface Subheading {

    /**
     * @param model table data
     * @param row the row to be inspected,
     * @return return true if the table needs a subheader before this row
     */
    boolean isSubheaderRow(TableDataModel model, int row);

    /**
     * @param hb used to build the table haeder
     * @param model table data
     * @param row the current row, the header will show up before this row
     */
    void buildHeaderRow(HtmlBuilder hb, TableDataModel model, int columnCount, int row);

}
