package de.marketmaker.itools.gwtutil.client.widgets.input;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTMLTable;
import com.google.gwt.user.client.ui.Widget;

/**
 * User: umaurer
 * Date: 10.06.13
 * Time: 17:10
 */
public class InputForm extends Composite {
    private final FlexTable table = new FlexTable();

    public InputForm(String labelWidth, String inputWidth) {
        this.table.setStyleName("mm-inputForm");
        final HTMLTable.ColumnFormatter columnFormatter = table.getColumnFormatter();
        if (labelWidth != null) {
            columnFormatter.setWidth(0, labelWidth);
        }
        if (inputWidth != null) {
            columnFormatter.setWidth(1, inputWidth);
        }
        initWidget(this.table);
    }

    public void add(String label, Widget widget) {
        final int row = this.table.getRowCount();
        this.table.setText(row, 0, label);
        this.table.setWidget(row, 1, widget);
        final FlexTable.FlexCellFormatter formatter = this.table.getFlexCellFormatter();
        formatter.setStyleName(row, 0, "mm-inputForm label");
        formatter.setStyleName(row, 1, "mm-inputForm widget");
    }

    public void add(SafeHtml label, Widget widget) {
        final int row = this.table.getRowCount();
        this.table.setHTML(row, 0, label);
        this.table.setWidget(row, 1, widget);
        final FlexTable.FlexCellFormatter formatter = this.table.getFlexCellFormatter();
        formatter.setStyleName(row, 0, "mm-inputForm label");
        formatter.setStyleName(row, 1, "mm-inputForm widget");
    }

    public void addFullSize(Widget widget) {
        final int row = this.table.getRowCount();
        this.table.setWidget(row, 0, widget);
        this.table.getFlexCellFormatter().setColSpan(row, 0, 2);
        final FlexTable.FlexCellFormatter formatter = this.table.getFlexCellFormatter();
        formatter.setStyleName(row, 0, "mm-inputForm widget");
    }
}
