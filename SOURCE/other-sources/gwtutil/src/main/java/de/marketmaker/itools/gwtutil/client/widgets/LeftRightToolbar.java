package de.marketmaker.itools.gwtutil.client.widgets;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiChild;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTMLTable;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Ulrich Maurer
 *         Date: 12.10.12
 */
public class LeftRightToolbar extends Composite {

    interface MyBinder extends UiBinder<Grid, LeftRightToolbar> {}

    private static MyBinder uiBinder = GWT.create(MyBinder.class);

    @UiField
    Grid grid;

    @UiField
    FlexTable tableLeft;

    @UiField
    FlexTable tableRight;

    public LeftRightToolbar() {
        uiBinder.createAndBindUi(this);

        final HTMLTable.CellFormatter formatter = grid.getCellFormatter();
        formatter.setAlignment(0, 0, HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_MIDDLE);
        formatter.setAlignment(0, 1, HasHorizontalAlignment.ALIGN_RIGHT, HasVerticalAlignment.ALIGN_MIDDLE);

        initWidget(grid);
    }

    @UiChild(tagname = "left")
    public void addLeft(Widget widget) {
        add(this.tableLeft, widget);
    }

    @UiChild(tagname = "right")
    public void addRight(Widget widget) {
        add(this.tableRight, widget);
    }

    private void add(FlexTable table, Widget widget) {
        final int column = table.getRowCount() == 0 ? 0 : table.getCellCount(0);
        table.setWidget(0, column, widget);
        table.getFlexCellFormatter().setStyleName(0, column, "mm-toolbar-cell");
    }

    public void setVisible(boolean visible, Widget... widgets) {
        for (Widget widget: widgets) {
            if (!setVisible(visible, this.tableLeft, widget)) {
                setVisible(visible, this.tableRight, widget);
            }
        }
    }

    private boolean setVisible(boolean visible, FlexTable table, Widget widget) {
        for (int i = 0, count = table.getCellCount(0); i < count; i++) {
            final Widget cellWidget = table.getWidget(0, i);
            if (cellWidget == widget) {
                table.getFlexCellFormatter().setVisible(0, i, visible);
                return true;
            }
        }
        return false;
    }
}
