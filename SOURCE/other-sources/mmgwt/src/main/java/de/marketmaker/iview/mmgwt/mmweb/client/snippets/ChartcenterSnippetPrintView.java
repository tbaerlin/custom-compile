/*
 * ChartcenterSnippetPrintView.java
 *
 * Created on 28.05.2008 11:24:41
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */

package de.marketmaker.iview.mmgwt.mmweb.client.snippets;

import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.SimplePanel;
import de.marketmaker.iview.dmxml.IMGResult;
import de.marketmaker.iview.mmgwt.mmweb.client.data.InstrumentTypeEnum;
import de.marketmaker.iview.mmgwt.mmweb.client.util.ChartUrlFactory;

import java.util.HashMap;
import java.util.Map;

import de.marketmaker.iview.mmgwt.mmweb.client.I18n;

/**
 * @author Michael Lösch
 */
public class ChartcenterSnippetPrintView {
    final private static String EMPTY_STYLE = "mm-printchartcenter-empty"; // $NON-NLS-0$

    private final Panel panel;

    private class Items {
        private String name;

        private String[][] mappingTable;

        private String preElementStyleName;

        Items(String name, String[][] mappingTable, String preElementStyleName) {
            this.name = name;
            this.mappingTable = mappingTable;
            this.preElementStyleName = preElementStyleName;
        }

        public String getName() {
            return name;
        }

        public String getPreElementStyleName() {
            return preElementStyleName;
        }

        public String[][] getMappingTable() {
            return mappingTable;
        }
    }

    private final Map<String, Items> mapItems = new HashMap<String, Items>();
    private final FlexTable grid;
    final Image image;

    public ChartcenterSnippetPrintView() {
        this.panel = new SimplePanel();
        this.panel.setStyleName("mm-chartcenter"); // $NON-NLS-0$

        addItem(new Items("period", ChartcenterForm.getShortPeriods(), EMPTY_STYLE)); // $NON-NLS-0$
        addItem(new Items("type", ChartcenterForm.getChartTypes(), EMPTY_STYLE)); // $NON-NLS-0$

        this.image = new Image();
        this.image.setVisible(true);
        this.image.setStyleName("mm-chart"); // $NON-NLS-0$

        this.grid = new FlexTable();
        this.grid.setStyleName("mm-chartcenter"); // $NON-NLS-0$
        final FlexTable.FlexCellFormatter cellFormatter = this.grid.getFlexCellFormatter();
        cellFormatter.setAlignment(0, 0, HasHorizontalAlignment.ALIGN_CENTER, HasVerticalAlignment.ALIGN_TOP);
        cellFormatter.setAlignment(1, 0, HasHorizontalAlignment.ALIGN_CENTER, HasVerticalAlignment.ALIGN_TOP);
        this.grid.setWidget(0, 0, image);
        this.grid.getCellFormatter().setAlignment(1, 0, HasHorizontalAlignment.ALIGN_CENTER, HasVerticalAlignment.ALIGN_TOP);
        this.panel.add(this.grid);
    }

    private String getUrl(IMGResult img, int width, int height, String chartStyleSuffix) {
        final String url = ChartUrlFactory.getUrl(img.getRequest());
        final int i = url.indexOf(".png"); // $NON-NLS-0$
        final StringBuilder result = new StringBuilder();
        result.append(url.substring(0, i) + chartStyleSuffix + url.substring(i) + "&legend=true"); // $NON-NLS-0$
        if ("".equals(chartStyleSuffix)) {
            result.append("&minLineWidth=2"); // $NON-NLS-0$
        }
        return result.toString()
                .replaceFirst("width=[0-9]+", "width=" + width) // $NON-NLS-0$ $NON-NLS-1$
                .replaceFirst("height=[0-9]+", "height=" + height); // $NON-NLS-0$ $NON-NLS-1$
    }

    private void addItem(Items items) {
        this.mapItems.put(items.getName(), items);
    }

    private Map<String, String> filterConfig(SnippetConfiguration config, String[][] confDictionary) {
        final Map<String, String> c = config.getCopyOfParameters();
        final Map<String, String> result = new HashMap<String, String>();
        for (final String[] confEntry : confDictionary) {
            final String key = confEntry[0];
            final String value = c.get(key);
            if (value != null) {
                result.put(key, value);
            }
        }
        return result;
    }

    private void generateConfigGrid(SnippetConfiguration config, boolean isSTK) {
        final String blendDividends =
                (isSTK ? I18n.I.chartOptionEvaluateDistributionForStocks() : I18n.I.chartOptionEvaluateDistributionForNonStocks()) +
                " " + I18n.I.chartOptionHistoricPricesAdapted();
        String[][] confDictionary = new String[][]{
                {"period", I18n.I.period() + ":"},  // $NON-NLS-0$ $NON-NLS-1$
                {"from", I18n.I.fromUpperCase() + ":"},  // $NON-NLS-0$ $NON-NLS-1$
                {"to", I18n.I.toUpperCase() + ":"},  // $NON-NLS-0$ $NON-NLS-1$
                {"currency", I18n.I.currency() + ":"},  // $NON-NLS-0$ $NON-NLS-1$
                {"percent", I18n.I.relativeValues() + ":"},  // $NON-NLS-0$ $NON-NLS-1$
                {"turnover", I18n.I.volume() + ":"},  // $NON-NLS-0$ $NON-NLS-1$
                {"logScales", I18n.I.chartOptionLogarithmicScaleAbbr() + ":"},  // $NON-NLS-0$ $NON-NLS-1$
                {"blendCorporateActions", I18n.I.evaluateCorporateActions() + ":"},  // $NON-NLS-0$ $NON-NLS-1$
                {"blendDividends", blendDividends + ":"},  // $NON-NLS-0$ $NON-NLS-1$
        };
        final Map<String, String> displayPairs = filterConfig(config, confDictionary);
        final Grid result = new Grid(displayPairs.size(), 2);
        result.setStyleName("mm-printchartcenter-grid"); // $NON-NLS-0$
        int row = 0;
        for (Map.Entry<String, String> entry : displayPairs.entrySet()) {
            final String s = entry.getKey();
            final String value = entry.getValue();

            final Label lbName = new Label(find(s, confDictionary));
            lbName.setStyleName("mm-printchartcenter-meta"); // $NON-NLS-0$

            final String text = filterBool(find(value, findMappingTab(s)));
            if (text != null) {
                final Label lbValue = new Label(text);
                lbValue.setStyleName("mm-printchartcenter-value"); // $NON-NLS-0$
                result.setWidget(row, 0, lbName);
                result.setWidget(row, 1, lbValue);
                row++;
            }
        }
        this.grid.setWidget(1, 0, result);
    }

    public String getPrintHtml(IMGResult img, SnippetConfiguration config) {
        final InstrumentTypeEnum type = InstrumentTypeEnum.valueOf(img.getInstrumentdata().getType());
        generateConfigGrid(config, type == InstrumentTypeEnum.STK);

        final boolean chartSizeLarge = "true".equals(config.getString("chartsizelarge")); //$NON-NLS$
        final int chartWidth;
        final int chartHeight;
        if(chartSizeLarge) {
            chartWidth = 960;
            chartHeight = 440;
        }
        else {
            chartWidth = 620;
            chartHeight = 270
                    + (config.containsKey("turnover") ? 40 : 0) // $NON-NLS-0$
                    + (config.containsKey("indicator") ? 60 : 0); // $NON-NLS-0$
        }

        this.image.setSize(chartWidth + "px", chartHeight + "px"); // $NON-NLS-0$ $NON-NLS-1$
        final String url = getUrl(img, chartWidth, chartHeight, "color".equals(config.getString("chartStyle", null)) ? "" : "-bw"); // $NON-NLS-0$ $NON-NLS-1$ $NON-NLS-2$ $NON-NLS-3$
        this.image.setUrl(url);

        return this.panel.getElement().getInnerHTML();
    }

    private String find(String value, String[][] mappingTab) {
        if (mappingTab != null) {
            for (String[] innerArray : mappingTab) {
                if (value.equals(innerArray[0])) {
                    return innerArray[1];
                }
            }
        }
        return value;
    }

    private String filterBool(String value) {
        if ("true".equals(value)) { // $NON-NLS-0$
            return "aktiv"; // $NON-NLS-0$
        }
        if ("false".equals(value)) { // $NON-NLS-0$
            return null;
        }
        return value;
    }

    private String[][] findMappingTab(String name) {
        final ChartcenterSnippetPrintView.Items items = this.mapItems.get(name);
        return items == null ? null : items.getMappingTable();
    }
}
