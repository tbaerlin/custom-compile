/*
 * ChartRatioUniverseSnippetView.java
 *
 * Created on 31.03.2008 11:23:52
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.snippets;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.gwt.dom.client.Style;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.itools.gwtutil.client.widgets.chart.PieChart;
import de.marketmaker.itools.gwtutil.client.widgets.chart.SelectableChart;
import de.marketmaker.iview.dmxml.CountsListElement;
import de.marketmaker.iview.dmxml.IMGRatioUniverse;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.finder.FinderController;
import de.marketmaker.iview.mmgwt.mmweb.client.finder.FinderControllerRegistry;
import de.marketmaker.iview.mmgwt.mmweb.client.finder.FinderFormConfig;
import de.marketmaker.iview.mmgwt.mmweb.client.finder.FinderFormKeys;
import de.marketmaker.iview.mmgwt.mmweb.client.finder.FinderSection;
import de.marketmaker.iview.mmgwt.mmweb.client.util.BrowserSpecific;
import de.marketmaker.iview.mmgwt.mmweb.client.util.ChartUrlFactory;
import de.marketmaker.iview.mmgwt.mmweb.client.util.PlaceUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.Renderer;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 * @author mdick
 */
public class ChartRatioUniverseSnippetView extends SnippetView<ChartRatioUniverseSnippet> {
    private static final String[] COLORS = new String[]{"#990099", "#009999", "#6699ff", "#ff9933", "#00cc66", "#ffcc00", "#9999ff", "#ff6666" }; // $NON-NLS$

    private FlowPanel panel = new FlowPanel();
    private SimplePanel chartPanel = new SimplePanel();
    private SimplePanel footer = new SimplePanel();
    private final String instrumentType;
    private final String field;

    public ChartRatioUniverseSnippetView(ChartRatioUniverseSnippet snippet, String instrumentType, String field) {
        super(snippet);
        this.instrumentType = instrumentType;
        this.field = field;
        setTitle(getConfiguration().getString("title", I18n.I.groups()));  // $NON-NLS$
        this.chartPanel.setStyleName("mm-chart");
        this.footer.setStylePrimaryName("mm-snippet-chart-footer"); // $NON-NLS$
        this.panel.add(this.chartPanel);
        this.panel.add(this.footer);
    }

    @Override
    protected void onContainerAvailable() {
        super.onContainerAvailable();
        this.container.setContentWidget(this.panel);
    }

    void update(String s, boolean asHtml) {
        if (asHtml) {
            this.footer.setWidget(new HTML(s));
        }
        else {
            this.footer.setWidget(new Label(s));
        }
        this.chartPanel.setWidget(null);
    }

    void update(IMGRatioUniverse result) {
        if (BrowserSpecific.isSvgSupported() && SessionData.isAsDesign()) {
            updateSvg(result);
        }
        else {
            updateNoSvg(result);
        }
    }

    void updateSvg(IMGRatioUniverse result) {
        final PieChart pieChart = new PieChart();
        pieChart.setSize(250, 250);
        final Style pieChartStyle = pieChart.getElement().getStyle();
        pieChartStyle.setProperty("margin", "0 auto"); // center in container // $NON-NLS$
        pieChart.setStyleName("as-pieChart");

        final List<CountsListElement> singleList = result.getCounts().getElement();
        Collections.sort(singleList, (o1, o2) -> -(Integer.parseInt(o1.getValue()) - Integer.parseInt(o2.getValue())));

        final int colSpan = getConfiguration().getInt("colSpan", 2); // $NON-NLS$
        final int width = getConfiguration().getInt("width", 560); // $NON-NLS$
        final boolean twoLine = colSpan == 1 || width < 500;

        final int entryCount = singleList.size();
        final boolean tableSplitted = entryCount > 12 || twoLine;
        final int half = tableSplitted ? ((entryCount + 1) / 2) : entryCount;

        final FlexTable grid = new FlexTable();
        grid.setStyleName("as-pieTable-grid");
        final FlowPanel tableLeft = new FlowPanel();
        tableLeft.setStyleName("as-pieTable");
        final FlowPanel tableRight;
        if (tableSplitted) {
            grid.addStyleName("splitted");

            tableRight = new FlowPanel();
            tableRight.setStyleName("as-pieTable");

            grid.setWidget(0, 0, pieChart);
            grid.getFlexCellFormatter().setColSpan(0, 0, 2);
            grid.setWidget(1, 0, tableLeft);
            grid.getFlexCellFormatter().setStyleName(1, 0, "left");
            grid.setWidget(1, 1, tableRight);
            grid.getFlexCellFormatter().setStyleName(1, 1, "right");
        }
        else {
            grid.setWidget(0, 0, tableLeft);
            grid.setWidget(0, 1, pieChart);
            tableRight = null;
        }
        final List<PieChart.Entry> entries = new ArrayList<>(entryCount);
        int i = 0;
        float sum = 0f;
        for (CountsListElement el : singleList) {
            final String style = "color-" + (i % 12); // $NON-NLS$
            final Float value = Float.valueOf(el.getValue());
            sum += value;
            entries.add(new PieChart.Entry(el.getKey(), style, value));
            final FlowPanel table = i < half ? tableLeft : tableRight;
            //noinspection ConstantConditions
            final HTML legendElement = SelectableChart.Util.createLegendElement(pieChart, i, "color " + style, getRenderedCategory(el.getKey()), Renderer.PRICE_0MAX5.render(el.getValue())); // $NON-NLS$
            addClickHandler(legendElement, el.getKey());

            assert table != null;
            table.add(legendElement);
            i++;
        }
        pieChart.drawChart(entries);
        pieChart.addSelectionHandler(new SelectableChart.LegendHighlighter(){
            @Override
            protected Widget getWidget(SelectableChart.Index index) {
                final int row = index.getEntryIndex();
                if (row < half) {
                    return tableLeft.getWidget(row);
                }
                else {
                    assert tableRight != null;
                    return tableRight.getWidget(row - half);
                }
            }
        });
        pieChart.addEntryClickHandler(event -> gotoFinder(event.getClickedEntry().getName()));
        if (tableRight == null) {
            tableLeft.add(createSumElement((long)sum));
        }
        else {
            tableRight.add(createSumElement((long)sum));
        }

        this.chartPanel.getElement().getStyle().setProperty("marginLeft", "auto"); // $NON-NLS$
        this.chartPanel.getElement().getStyle().setProperty("marginRight", "auto"); // $NON-NLS$
        this.chartPanel.setWidget(grid);
        this.footer.setWidget(null);
    }

    private HTML createSumElement(long sum) {
        final SafeHtmlBuilder sb = new SafeHtmlBuilder();
        sb.appendHtmlConstant("<div class=\"name\">");
        sb.appendEscaped(I18n.I.all());
        sb.appendHtmlConstant("</div>");
        sb.appendHtmlConstant("<div class=\"value\">");
        sb.appendEscaped(Renderer.PRICE_0MAX5.render(Long.toString(sum)));
        sb.appendHtmlConstant("</div>");
        final HTML html = new HTML(sb.toSafeHtml());
        html.setStyleName("legendEntry");
        html.addStyleName("sumEntry");
        html.getElement().getStyle().setMarginTop(2, Style.Unit.EM);
        return html;
    }

    void updateNoSvg(IMGRatioUniverse result) {
        final Image image = new Image(ChartUrlFactory.getUrl(result.getRequest()));
        this.chartPanel.setWidget(image);
        
        final List<CountsListElement> singleList = result.getCounts().getElement();
        Collections.sort(singleList, (o1, o2) -> -(Integer.parseInt(o1.getValue()) - Integer.parseInt(o2.getValue())));
        
        final int midIndex = (singleList.size() + 1) / 2;
        final FlexTable table = new FlexTable();
        table.setStyleName("mm-chart-caption");
        table.setCellSpacing(3);
        final FlexTable.FlexCellFormatter formatter = table.getFlexCellFormatter();

        long sum = 0L;
        for (int i = 0; i < midIndex; i++) {
            final CountsListElement e1 = singleList.get(i);
            final String color1 = COLORS[i % COLORS.length];
            table.setHTML(i, 0, "&nbsp;"); // $NON-NLS$
            formatter.getElement(i, 0).getStyle().setProperty("backgroundColor", color1); // $NON-NLS$
            formatter.setWidth(i, 0, "10px"); // $NON-NLS-0$

            table.setWidget(i, 1, getCategoryLabel(e1.getKey()));
            formatter.setStyleName(i, 1, "text");

            table.setText(i, 2, e1.getValue());
            formatter.setStyleName(i, 2, "number");
            sum += Long.parseLong(e1.getValue());

            table.setText(i, 3, " ");
            formatter.setStyleName(i, 3, "divider");

            final int i2 = midIndex + i;
            if (i2 < singleList.size()) {
                final CountsListElement e2 = singleList.get(i2);
                final String color2 = COLORS[i2 % COLORS.length];
                table.setHTML(i, 4, "&nbsp;"); // $NON-NLS$
                formatter.getElement(i, 4).getStyle().setProperty("backgroundColor", color2); // $NON-NLS$
                formatter.setWidth(i, 4, "10px"); // $NON-NLS$

                table.setWidget(i, 5, getCategoryLabel(e2.getKey()));
                formatter.setStyleName(i, 5, "text");

                table.setText(i, 6, e2.getValue());
                formatter.setStyleName(i, 6, "number");
                sum += Long.parseLong(e2.getValue());
            }
        }
        final int rowSum = table.getRowCount();
        formatter.setColSpan(rowSum, 0, 5);

        table.setText(rowSum, 1, I18n.I.all()); 
        formatter.setStyleName(rowSum, 1, "text");

        table.setText(rowSum, 2, String.valueOf(sum));
        formatter.setStyleName(rowSum, 2, "number");

        this.footer.setWidget(table);
    }

    private Label getCategoryLabel(final String category) {
        final Label label = new Label(getRenderedCategory(category));
        addClickHandler(label, category);
        return label;
    }

    private void addClickHandler(Label label, final String category) {
        final boolean hasFinderLinks = "CER".equals(this.instrumentType) || "FND".equals(this.instrumentType); // $NON-NLS-0$ $NON-NLS-1$
        if (hasFinderLinks) {
            label.addStyleName("mm-link");
            label.addClickHandler(clickEvent -> gotoFinder(category));
        }
    }

    private String getRenderedCategory(String category) {
        final String renderedCategory;
        if ("CER".equals(this.instrumentType)) { // $NON-NLS$
            renderedCategory = Renderer.CERTIFICATE_CATEGORY.render(category);
        }
        else if ("WNT".equals(this.instrumentType)) { // $NON-NLS$
            renderedCategory = Renderer.WARRANT_TYPE.render(category, category);
        }
        else {
            renderedCategory = category;
        }
        return renderedCategory;
    }

    private void gotoFinder(String category) {
        final FinderController controller = FinderControllerRegistry.get("L" + this.instrumentType); // $NON-NLS-0$
        if (controller == null) {
            Firebug.log("FinderController not found for instrumentType " + this.instrumentType); // $NON-NLS$
            return;
        }
        final FinderFormConfig ffc = new FinderFormConfig();
        FinderSection.enableBaseSection(ffc, this.instrumentType);
        if ("FND".equals(this.instrumentType)) { // $NON-NLS-0$
            ffc.put(this.field, "true"); // $NON-NLS-0$
            ffc.put(this.field + "-item", category); // $NON-NLS-0$
        }
        else {
            ffc.put(FinderFormKeys.TYPE, "true"); // $NON-NLS-0$
            ffc.put(FinderFormKeys.TYPE + "-item", category); // $NON-NLS-0$
        }
        ffc.put(FinderFormKeys.SORT, "true"); // $NON-NLS-0$
        ffc.put(FinderFormKeys.SORT + "-item", "name"); // $NON-NLS-0$ $NON-NLS-1$
        ffc.put(FinderFormKeys.ISSUER_NAME, "true"); // $NON-NLS-0$
        addIssuerListToConfig(ffc);
        controller.prepareFind(ffc);
        PlaceUtil.goTo("M_LF_" + this.instrumentType); // $NON-NLS-0$
    }

    private void addIssuerListToConfig(FinderFormConfig config) {
        final String[] issuerList = this.snippet.getGuiDefList("issuerlist"); // $NON-NLS-0$
        if (issuerList != null) {
            config.put(FinderFormKeys.ISSUER_NAME, "true"); // $NON-NLS-0$
            final StringBuilder sb = new StringBuilder();
            for (int i = 0; i < issuerList.length; i++) {
                if (i > 0) {
                    sb.append('|');
                }
                sb.append(issuerList[i]);
            }
            config.put(FinderFormKeys.ISSUER_NAME + "-item", sb.toString()); // $NON-NLS-0$
        }
    }
}
