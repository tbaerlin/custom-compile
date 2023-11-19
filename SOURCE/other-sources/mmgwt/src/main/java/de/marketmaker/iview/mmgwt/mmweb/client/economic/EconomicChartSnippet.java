/*
 * PortraitChartSnippet.java
 *
 * Created on 28.03.2008 16:11:58
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.economic;

import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.BasicChartSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.Snippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SnippetClass;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SnippetConfiguration;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;

import java.util.List;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class EconomicChartSnippet extends
        BasicChartSnippet<EconomicChartSnippet, EconomicChartSnippetView> {
    public static class Class extends SnippetClass {
        public Class() {
            super("economic.Chart", I18n.I.chart()); // $NON-NLS-0$
        }

        public Snippet newSnippet(DmxmlContext context, SnippetConfiguration config) {
            return new EconomicChartSnippet(context, config);
        }
    }

    private static final String EMPTY_MESSAGE = I18n.I.selectUpTo5Values();
    private static final String[] LABEL_COLOR_STYLES = new String[]{
            "mm-chartcenter-main",       // $NON-NLS$
            "mm-chartcenter-benchmark1", // $NON-NLS$
            "mm-chartcenter-benchmark2", // $NON-NLS$
            "mm-chartcenter-benchmark3", // $NON-NLS$
            "mm-chartcenter-benchmark4"  // $NON-NLS$
    };
    private static final String[] BENCHMARK_COLORS = new String[]{
            "bm2", // $NON-NLS$
            "bm2;bm3", // $NON-NLS$
            "bm2;bm3;bm4", // $NON-NLS$
            "bm2;bm3;bm4;bm5" // $NON-NLS$
    };

    private List<String> names;

    private EconomicChartSnippet(DmxmlContext context, SnippetConfiguration config) {
        super(context, config);

        this.block.setParameter("percent", "false"); // $NON-NLS$
        config.putDefault("percent", "false"); // $NON-NLS$
        config.putDefault("period", "P1Y"); // $NON-NLS$

        setView(new EconomicChartSnippetView(this));
        onParametersChanged();
    }

    protected String getChartBlockName() {
        return "IMG_Chartcenter"; // $NON-NLS-0$
    }

    protected String getEmptyMessage() {
        if (this.names != null && !this.names.isEmpty()) {
            return EMPTY_MESSAGE + I18n.I.numberValuesSelectedCurrently(this.names.size()); 
        }
        return EMPTY_MESSAGE;
    }

    public void setSymbols(List<String> names, String type, List<String> symbols) {
        this.names = names;
        final SnippetConfiguration config = getConfiguration();
        config.put("titleSuffix", type); // $NON-NLS-0$
        if (symbols.isEmpty() || symbols.size() > 5) {
            config.remove("symbol"); // $NON-NLS-0$
            config.remove("benchmark"); // $NON-NLS-0$
            config.remove("benchmarkColor"); // $NON-NLS-0$
        }
        else {
            config.put("symbol", symbols.get(0)); // $NON-NLS-0$
            if (symbols.size() == 1) {
                config.remove("benchmark"); // $NON-NLS-0$
                config.remove("benchmarkColor"); // $NON-NLS-0$
            }
            else {
                final StringBuilder sbBenchmark = new StringBuilder();
                for (int i = 1; i < symbols.size(); i++) {
                    append(sbBenchmark, symbols.get(i));
                }
                config.put("benchmark", sbBenchmark.toString()); // $NON-NLS-0$
                config.put("benchmarkColor", BENCHMARK_COLORS[symbols.size() - 2]); // $NON-NLS-0$
            }
        }
        ackParametersChanged();
    }

    private void append(StringBuilder sb, String value) {
        if (sb.length() > 0) {
            sb.append(";"); // see de.marketmaker.istar.merger.web.easytrade.chart.BaseImgCommand#VALUE_SEPARATOR // $NON-NLS-0$
        }
        sb.append(value);
    }

    public String getFooterMessage() {
        if (this.names == null || this.names.isEmpty()) {
            return getEmptyMessage();
        }
        final StringBuilder sb = new StringBuilder();
        sb.append("<table align=\"center\" class=\"mm-eco-chartLegend\">"); // $NON-NLS-0$
        for (int i = 0; i < this.names.size(); i++) {
            final String name = this.names.get(i);
            sb.append("<tr><td class=\"").append(LABEL_COLOR_STYLES[i]).append("\">&nbsp;&nbsp;</td><td>").append(name).append("</td></tr>"); // $NON-NLS$
        }
        sb.append("</table>"); // $NON-NLS-0$
        return sb.toString();
    }

    protected void onParametersChanged() {
        final SnippetConfiguration config = getConfiguration();
        final String symbol = config.getString("symbol", null); // $NON-NLS-0$
        this.block.setEnabled(symbol != null);
        this.block.setParameter("symbol", symbol); // $NON-NLS-0$
        this.block.setParameter("benchmark", config.getString("benchmark", null)); // $NON-NLS-0$ $NON-NLS-1$
        this.block.setParameter("benchmarkColor", config.getString("benchmarkColor", null)); // $NON-NLS-0$ $NON-NLS-1$
        this.block.setParameter("period", config.getString("period", null)); // $NON-NLS-0$ $NON-NLS-1$
        this.block.setParameter("from", config.getString("from", null)); // $NON-NLS-0$ $NON-NLS-1$
        this.block.setParameter("to", config.getString("to", null)); // $NON-NLS-0$ $NON-NLS-1$
    }

    public void updateView() {
        getView().reloadTitle();
        super.updateView();
    }

    @Override
    protected void ackParametersChanged() {
        super.ackParametersChanged();
    }
}
