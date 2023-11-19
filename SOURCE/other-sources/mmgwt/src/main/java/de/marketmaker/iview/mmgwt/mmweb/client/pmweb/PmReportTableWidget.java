/*
 * PmReportTableWidget.java
 *
 * Created on 15.05.13 15:34
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.pmweb;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.ResizeComposite;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.SimpleLayoutPanel;
import com.google.gwt.user.client.ui.SplitLayoutPanel;

import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.analysis.AnalysisDisplay;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.chart.ChartFactory;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.chart.HcDiagram;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.chart.MaximizePanel;
import de.marketmaker.iview.pmxml.DTAxisSpec;
import de.marketmaker.iview.pmxml.DTDiagram;
import de.marketmaker.iview.pmxml.DTFooterCell;
import de.marketmaker.iview.pmxml.DTSeries;
import de.marketmaker.iview.pmxml.DTTable;
import de.marketmaker.iview.tools.i18n.NonNLS;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @author Markus Dick
 * @author Ulrich Maurer
 */
public class PmReportTableWidget extends ResizeComposite implements HasColumnHeaderHandlers {
    public static final int HEIGHT_OF_HEADER = 23;
    private static final int MARGIN_PADDING_ETC = 20;
    public static final double HEIGHT_OF_ROW_COUNT_WIDGET = 17d;

    private final SimpleLayoutPanel panel = new SimpleLayoutPanel();

    private final DTTableWidget dtTableWidget = new DTTableWidget();
    private final HTML rowCountWidget = new HTML();
    private final DockLayoutPanel tableContentPanel = new DockLayoutPanel(Style.Unit.PX);
    private FlexTable footerTable = null;
    private Panel footerCharts = null;

    private DTTable dtTable;
    private DTTableRenderer.Options options;
    private String historyContextName;
    private int indexOfMaximizedDiagram = -1;

    private final AnalysisDisplay.Presenter presenter;
    private RowCount rowCount;

    public PmReportTableWidget(AnalysisDisplay.Presenter presenter) {
        this.presenter = presenter;
        this.tableContentPanel.addSouth(this.rowCountWidget, HEIGHT_OF_ROW_COUNT_WIDGET);
        this.tableContentPanel.add(this.dtTableWidget);
        initWidget(this.panel);
    }

    public PmReportTableWidget withTableRendererSupplier(Supplier<DTTableRenderer> dtTableRendererSupplier) {
        this.dtTableWidget.withSupplier(dtTableRendererSupplier);
        return this;
    }

    public PmReportTableWidget withClearTableBodyConsumers() {
        this.dtTableWidget.withClearTableBodyConsumers();
        return this;
    }

    public PmReportTableWidget withTableBodyConsumer(Consumer<Element> consumer) {
        this.dtTableWidget.withTableBodyConsumer(consumer);
        return this;
    }

    @Override
    public HandlerRegistration addColumnHeaderHandler(ColumnHeaderHandler handler) {
        return this.dtTableWidget.addColumnHeaderHandler(handler);
    }

    public void setPopupHookVisible(int columnIndex, boolean visible) {
        this.dtTableWidget.setPopupHookVisible(columnIndex, visible);
    }

    public void update(DTTable dtTable, DTTableRenderer.Options options, Integer indexOfMaximizedDiagram, String historyContextName, RowCount rowCount) {
        this.dtTable = dtTable;
        this.options = options;
        this.historyContextName = historyContextName;
        this.rowCount = rowCount;
        if (indexOfMaximizedDiagram != null) {
            setIndexOfMaximizedDiagram(indexOfMaximizedDiagram);
        }
        update();
    }

    private void setIndexOfMaximizedDiagram(Integer indexOfMaximizedDiagram) {
        this.indexOfMaximizedDiagram = indexOfMaximizedDiagram;
        if (this.presenter != null) {
            this.presenter.setMaxDiagramIdx(indexOfMaximizedDiagram);
        }
    }

    private void update() {
        final int initialDiagramIndex = DTTableUtils.getInitialDiagramIndex(this.dtTable);
        logDiagrams(this.dtTable.getDiagrams(), initialDiagramIndex);

        this.dtTableWidget.setHistoryContextName(this.historyContextName);
        this.dtTableWidget.setOptions(this.options);
        this.dtTableWidget.updateTable(this.dtTable);
        this.footerTable = null;
        this.footerCharts = null;

        final List<DTFooterCell> listFooterCells = this.dtTable.getFooterCells();
        final boolean hasSupportedDiagrams = hasSupportedDiagrams(this.dtTable.getDiagrams());
        final boolean hasFooterCells = !listFooterCells.isEmpty();
        final boolean fullscreenDiagram = indexOfMaximizedDiagram >= 0 && indexOfMaximizedDiagram < this.dtTable.getDiagrams().size();

        if (!hasFooterCells && !hasSupportedDiagrams) {
            this.panel.setWidget(this.tableContentPanel);
        }
        else if (fullscreenDiagram) {
            final HcDiagram hcDiagram = new HcDiagram(this.dtTable.getDiagrams().get(indexOfMaximizedDiagram), true);
            final MaximizePanel maximizePanel = new MaximizePanel(hcDiagram, true, clickEvent -> {
                setIndexOfMaximizedDiagram(-1);
                update();
            });
            this.panel.setWidget(maximizePanel);
        }
        else {
            final HorizontalPanel panel = new HorizontalPanel();
            panel.addStyleName("pm-report-footer-panel"); //$NON-NLS$

            int footerHeight = 0;

            if (hasFooterCells) {
                this.footerTable = getDTFooterTable(listFooterCells, !hasSupportedDiagrams);
                panel.add(this.footerTable);
                footerHeight = Math.max(HEIGHT_OF_HEADER + MARGIN_PADDING_ETC + this.footerTable.getRowCount() * 17, footerHeight);
            }

            if (hasSupportedDiagrams) {
                this.footerCharts = getChartsPanel(this.dtTable.getDiagrams(), hasFooterCells);
                panel.add(this.footerCharts);
                footerHeight = Math.max(HEIGHT_OF_HEADER + MARGIN_PADDING_ETC + 165, footerHeight);
            }

            final SplitLayoutPanel splitLayoutPanel = new SplitLayoutPanel();
            final ScrollPanel southPanel = new ScrollPanel(panel);
            southPanel.addStyleName("pm-report-footer"); //$NON-NLS$
            splitLayoutPanel.addSouth(southPanel, footerHeight);
            splitLayoutPanel.setWidgetSnapClosedSize(southPanel, 40);
            splitLayoutPanel.setWidgetToggleDisplayAllowed(southPanel, true);
            splitLayoutPanel.add(this.tableContentPanel);

            updateRowCountWidget();

            this.panel.setWidget(splitLayoutPanel);
        }
    }

    private void updateRowCountWidget() {
        if (this.rowCount != null) {
            this.tableContentPanel.setWidgetHidden(this.rowCountWidget, false);

            if(this.options.isPrefiltered()) {
                this.rowCountWidget.setText(I18n.I.objectCountOf(this.rowCount.getTotal(), this.rowCount.getCurrent()));
            }
            else {
                this.rowCountWidget.setText(I18n.I.objectCount(this.rowCount.getTotal()));
            }
        }
        else {
            this.tableContentPanel.setWidgetHidden(this.rowCountWidget, true);
        }
    }

    private Panel getChartsPanel(List<DTDiagram> diagrams, boolean hasFooterCells) {
        int i = 0;
        final FlowPanel panel = new FlowPanel();
        for (int diagramIndex = 0; diagramIndex < diagrams.size(); diagramIndex++) {
            DTDiagram diagram = diagrams.get(diagramIndex);
            final ChartFactory factory = ChartFactory.getFactory(diagram);
            if (factory != null) {
                final HcDiagram hcDiagram = new HcDiagram(diagram, false);
                final int fDiaIndex = diagramIndex;
                final MaximizePanel maximizePanel = new MaximizePanel(hcDiagram, false, clickEvent -> {
                    setIndexOfMaximizedDiagram(fDiaIndex);
                    update();
                });
                panel.add(maximizePanel);
                if (hasFooterCells || i++ > 0) {
                    maximizePanel.addStyleName("pm-report-footer-panel-divider");
                }
            }
        }
        return panel;
    }

    private boolean hasSupportedDiagrams(List<DTDiagram> diagrams) {
        for (DTDiagram diagram : diagrams) {
            if (ChartFactory.getFactory(diagram) != null) {
                return true;
            }
        }
        return false;
    }

    private void logDiagrams(List<DTDiagram> diagrams, int initialDiagramIndex) {
        if (diagrams.isEmpty()) {
            Firebug.log("no diagrams");
            return;
        }
        Firebug.groupStart("Diagrams (count: " + diagrams.size() + ", initial: " + initialDiagramIndex + ")");
        for (DTDiagram diagram : diagrams) {
            Firebug.groupStart("Diagram: " + diagram.getCaption());
            Firebug.groupStart("Axes");
            for (DTAxisSpec axisSpec : diagram.getAxes()) {
                Firebug.log(axisSpec.getTitle() + " IsDateTime=" + axisSpec.isIsDateTime());
            }
            Firebug.groupEnd();

            Firebug.groupStart("Series");
            for (DTSeries dtSeries : diagram.getSeries()) {
                Firebug.log(dtSeries.getTitle() + " SeriesType=" + dtSeries.getSeriesType() + " Points: " + dtSeries.getPoints().size());
            }
            Firebug.groupEnd();
            Firebug.groupEnd();
        }
        Firebug.groupEnd();
    }

    private static <T> List<List<T>> divideFooterToLists(List<T> footer) {
        final int eltCount = footer.size();
        final int colCount = eltCount <= 3 ? 1 : (eltCount <= 6 ? 2 : 3);
        final List<List<T>> list = new ArrayList<>(colCount);
        final int length = (eltCount - eltCount % colCount) / colCount + 1;
        for (int i = 0; i < colCount; i++) {
            final int from = length * i;
            int to = from + length;
            if (to > footer.size()) {
                to = footer.size();
            }
            list.add(footer.subList(from, to));
        }
        return list;
    }

    private static FlexTable getDTFooterTable(List<DTFooterCell> listFooterCells, boolean multiColumn) {
        final FlexTable table = new FlexTable();
        final FlexTable.FlexCellFormatter formatter = table.getFlexCellFormatter();
        table.setStyleName("pm-report-footer");

        final List<List<DTFooterCell>> result;
        if (multiColumn) {
            result = divideFooterToLists(listFooterCells);
        }
        else {
            result = new ArrayList<>();
            result.add(listFooterCells);
        }

        boolean colSpaceX = false;
        int rowCount = result.get(0).size();
        for (int col = 0; col < result.size(); col++) {
            final int tableCol = col * 3;
            final List<DTFooterCell> listColumnRows = result.get(col);
            for (int row = 0; row < rowCount; row++) {
                if (colSpaceX) {
                    table.setHTML(row, tableCol - 1, "&nbsp;"); // $NON-NLS$
                    formatter.setStyleName(row, tableCol - 1, "footerspace");
                }

                final String caption;
                final String value;
                if (row >= listColumnRows.size()) {
                    caption = "";
                    value = "";
                }
                else {
                    final DTFooterCell foot = listColumnRows.get(row);
                    caption = foot.getName();
                    value = foot.getValue();
                }

                table.setText(row, tableCol, caption);
                table.setText(row, tableCol + 1, value);
                formatter.setStyleName(row, tableCol, "footercaption");
                formatter.setStyleName(row, tableCol + 1, "footervalue");
            }
            colSpaceX = true;
        }
        return table;
    }

    public DTTable getDTTable() {
        return this.dtTable;
    }

    public DTTableWidget getDTTableWidget() {
        return this.dtTableWidget;
    }

    public DTTableRenderer.Options getDTTableRendererOptions() {
        return this.options;
    }

    public String getPrintHtml() {
        final SafeHtmlBuilder sb = new SafeHtmlBuilder();
        this.dtTableWidget.addPrintHtml(sb);
        if (this.footerTable != null) {
            sb.appendHtmlConstant("<div style=\"float: left\"><table class=\"pm-report-footer\" cellpadding=\"0\" cellspacing=\"0\">");
            sb.appendHtmlConstant(this.footerTable.getElement().getInnerHTML());
            sb.appendHtmlConstant("</table></div>");
        }
        if (this.footerCharts != null) {
            sb.appendHtmlConstant("<div style=\"float: left\">");
            sb.appendHtmlConstant(this.footerCharts.getElement().getInnerHTML());
            sb.appendHtmlConstant("</div>");
        }
        return sb.toSafeHtml().asString();
    }

    public RowCount getRowCount() {
        return rowCount;
    }

    @NonNLS
    public static class RowCount {
        private final int current;
        private final int total;

        public RowCount(int total, int current) {
            this.total = total;
            this.current = current;
        }

        public int getCurrent() {
            return current;
        }

        public int getTotal() {
            return total;
        }

        @SuppressWarnings("SimplifiableIfStatement")
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof RowCount)) return false;

            final RowCount rowCount = (RowCount) o;

            if (current != rowCount.current) return false;
            return total == rowCount.total;

        }

        @Override
        public int hashCode() {
            int result = current;
            result = 31 * result + total;
            return result;
        }

        @Override
        public String toString() {
            return "RowCount{" +
                    "current=" + current +
                    ", total=" + total +
                    '}';
        }
    }
}
