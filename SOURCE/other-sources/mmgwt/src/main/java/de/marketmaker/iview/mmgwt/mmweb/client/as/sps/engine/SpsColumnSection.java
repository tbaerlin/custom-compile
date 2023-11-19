package de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine;

import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Widget;
import de.marketmaker.itools.gwtutil.client.util.Firebug;

import static com.google.gwt.dom.client.Style.Unit.PX;

/**
 * Author: umaurer
 * Created: 14.01.14
 */
public class SpsColumnSection extends SpsSection {
    final String[] columnStyle;

    private FlexTable sectionWidget;

    public SpsColumnSection(String... columnStyle) {
        this.columnStyle = columnStyle;
    }

    @Override
    protected Widget createSectionWidget() {
        if (hasStyle("sps-flow")) { // $NON-NLS$
            this.sectionWidget = createSectionWidgetFlow();
        }
        else {
            final Integer columnCount = getStyleValueInt("sps-form"); // $NON-NLS$
            this.sectionWidget = columnCount == null
                    ? createSectionWidgetDefault()
                    : createSectionWidgetTopCaption(columnCount);
        }
        return this.sectionWidget;
    }

    private FlexTable createSectionWidgetDefault() {
        final FlexTable table = new FlexTable();
        final FlexTable.FlexCellFormatter formatter = table.getFlexCellFormatter();
        table.setCellSpacing(0);
        table.setCellPadding(0);
        table.setStyleName("sps-columnSection-table");
        final int colCount = this.columnStyle.length;
        int colIndex = 0;
        int colPos = 0;
        int row = 0;
        for (SpsWidget spsWidget : getChildrenFeature().getChildren()) {
            final int colSpan = spsWidget.getColSpan();
            final Widget[] widgets = spsWidget.asWidgets();
            for (Widget widget : widgets) {
                if (colPos == colCount) {
                    row++;
                    colIndex = 0;
                    colPos = 0;
                }
                table.setWidget(row, colIndex, widget);
                formatter.setStyleName(row, colIndex, this.columnStyle[colPos]);
                if (spsWidget.getCellStyle() != null) {
                    formatter.addStyleName(row, colIndex, spsWidget.getCellStyle());
                }
                colIndex++;
                colPos++;
            }
            if (colSpan > 1) {
                if (widgets.length != 1) {
                    throw new IllegalStateException(getWidgetDescription() + " -> colSpan(" + colSpan + ") > 1 together with widgets.length(" + widgets.length + ") != 1"); // $NON-NLS$
                }
                formatter.setColSpan(row, colIndex - 1, colSpan);
                colPos += colSpan - 1;
            }
        }
        while (colPos < colCount) {
            table.setHTML(row, colIndex, "&nbsp;"); // $NON-NLS$
            colIndex++;
            colPos++;
        }
        return table;
    }

    protected FlexTable createSectionWidgetTopCaption(int colCount) {
        final FlexTable table = new FlexTable();
        final FlexTable.FlexCellFormatter formatter = table.getFlexCellFormatter();
        table.setCellSpacing(0);
        table.setCellPadding(0);
        table.setStyleName("sps-topCaption-table");
        final Integer cellSpacing = getStyleValueInt("cellSpacing"); // $NON-NLS$

        int col = 0;
        int row = 0;
        for (SpsWidget spsWidget : getChildrenFeature().getChildren()) {
            if (col == colCount || spsWidget.hasStyle("col-0")) { // $NON-NLS$
                row += 2;
                col = 0;
            }
            final int rowWidget = row + 1;
            final int colSpan = spsWidget.getColSpan();
            final Widget[] widgets = spsWidget.asWidgets();
            if (widgets.length == 0) {
                Firebug.warn("SpsColumnSection.createWidgetTopCaption() -> SpsWidget does not create gwt widget: " + spsWidget.getDescId());
            }
            else if (widgets.length == 1) {
                table.setWidget(rowWidget, col, widgets[0]);
                if (colSpan > 1) {
                    formatter.setColSpan(row, col, colSpan);
                    formatter.setColSpan(rowWidget, col, colSpan);
                    col += colSpan - 1;
                }
            }
            else {
                table.setWidget(row, col, widgets[0]);
                if (widgets.length == 2) {
                    table.setWidget(rowWidget, col, widgets[1]);
                }
                else {
                    final HorizontalPanel p = new HorizontalPanel();
                    for (int i = 1; i < widgets.length; i++) {
                        p.add(widgets[i]);
                    }
                    table.setWidget(rowWidget, col, p);
                }
            }
            formatter.setStyleName(row, col, "sps-form-x-caption");
            formatter.setStyleName(rowWidget, col, "sps-form-x-widget");
            if (spsWidget.getCellStyle() != null) {
                formatter.addStyleName(rowWidget, col, spsWidget.getCellStyle());
            }

            if (cellSpacing != null) {
                if (row != 0) {
                    formatter.getElement(row, col).getStyle().setPaddingTop(cellSpacing, PX);
                }
                if (col != 0) {
                    formatter.getElement(row, col).getStyle().setPaddingLeft(cellSpacing, PX);
                    formatter.getElement(rowWidget, col).getStyle().setPaddingLeft(cellSpacing, PX);
                }
            }

            col++;
        }
        while (col < colCount) {
            table.setHTML(row, col, "&nbsp;"); // $NON-NLS$
            table.setHTML(row + 1, col, "&nbsp;"); // $NON-NLS$
            col++;
        }
        return table;
    }

    public void setWidgetVisibility(SpsWidget spsWidget, boolean visible) {
        if(spsWidget == null || !getChildrenFeature().getChildren().contains(spsWidget)) {
            Firebug.warn("<SpsColumnSection.setWidgetVisibility> SPS widget is not a child of this section");
            return;
        }

        for (Widget w : spsWidget.getWidgets()) {
            //find tr and set invisible
            try {
                final Style style = w.getElement().getParentElement().getParentElement().getStyle();
                if(visible) {
                    style.clearDisplay();
                }
                else {
                    style.setDisplay(Style.Display.NONE);
                }
            }
            catch (Exception e) {
                Firebug.error("<SpsColumnSection.setWidgetVisibility> failed", e);
            }
        }
    }

    protected FlexTable createSectionWidgetFlow() {
        final FlexTable table = new FlexTable();
        final FlexTable.FlexCellFormatter formatter = table.getFlexCellFormatter();
        table.setCellSpacing(0);
        table.setCellPadding(0);
        table.setStyleName("sps-columnSection-flow");
        final Integer cellSpacing = getStyleValueInt("cellSpacing"); // $NON-NLS$

        int col = 0;
        int row = 0;
        for (SpsWidget spsWidget : getChildrenFeature().getChildren()) {
            if (spsWidget.hasStyle("col-0")) { // $NON-NLS$
                row++;
                col = 0;
            }
            final Widget[] widgets = spsWidget.asWidgets();
            if (widgets.length == 0) {
                Firebug.warn("SpsColumnSection.createSectionWidgetFlow() -> SpsWidget does not create gwt widget: " + spsWidget.getDescId());
                continue;
            }

            final int colSpan = spsWidget.getColSpan();
            final Integer rowSpan = spsWidget.getStyleValueInt("rowSpan"); // $NON-NLS$
            boolean first = true;
            for (Widget widget : widgets) {
                table.setWidget(row, col, widget);
                if (spsWidget.getCellStyle() != null) {
                    formatter.addStyleName(row, col, spsWidget.getCellStyle());
                }
                setCellSpacing(formatter, row, col, cellSpacing);
                if (first) {
                    if (rowSpan != null) {
                        formatter.setRowSpan(row, col, rowSpan);
                    }
                    if (colSpan > 1) {
                        formatter.setColSpan(row, col, colSpan);
                    }
                    first = false;
                }
                col++;
            }
        }
        return table;
    }

    private void setCellSpacing(FlexTable.FlexCellFormatter formatter, int row, int col, Integer cellSpacing) {
        if (cellSpacing != null) {
            if (row != 0) {
                formatter.getElement(row, col).getStyle().setPaddingTop(cellSpacing, PX);
            }
            if (col != 0) {
                formatter.getElement(row, col).getStyle().setPaddingLeft(cellSpacing, PX);
            }
        }
    }
}
