package de.marketmaker.iview.mmgwt.dmxmldocu.client.parameter;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;

/**
 * @author Ulrich Maurer
 *         Date: 03.05.12
 */
public class RequestParameterPanel extends Composite {
    private final FlexTable table;
    private final int rowStartBlockParameters;
    private ParameterInputWidget.ChangeListener listener;

    public RequestParameterPanel() {
        this.table = new FlexTable();
        this.table.setStyleName("requestParameterPanel");
        this.table.setCellPadding(0);
        this.table.setCellSpacing(0);
        this.table.getColumnFormatter().setWidth(3, "16px"); // $NON-NLS$
        this.table.getColumnFormatter().setWidth(4, "16px"); // $NON-NLS$
        final FlexTable.FlexCellFormatter formatter = table.getFlexCellFormatter();
        int row = 0;

        /*this.table.setHTML(row, 0, "<h3>Block Parameters</h3>"); // $NON-NLS$
        formatter.setColSpan(row, 0, 4);
        row++;*/

        this.rowStartBlockParameters = row;

        initWidget(this.table);
    }

    public void clear() {
        for (int row = this.table.getRowCount() - 1; row >= this.rowStartBlockParameters; row--) {
            this.table.removeRow(row);
        }
    }

    public void add(ParameterInputWidget piw) {
        piw.addWidgets(this.table, this.table.getRowCount());
    }

    public void setChangeListener(ParameterInputWidget.ChangeListener listener) {
        this.listener = listener;
    }
}
