/*
 * DividendChartSnippet.java
 *
 * Created on 15.10.2014 13:24
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.snippets;

import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.data.InstrumentTypeEnum;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;

/**
 * @author jkirchg
 */
public class DividendChartSnippet extends BasicChartSnippet<DividendChartSnippet, DividendChartSnippetView> implements SymbolSnippet {

    public static class Class extends SnippetClass {
        public Class() {
            super("DividendChart", I18n.I.dividendPayments()); // $NON-NLS$
        }

        public Snippet newSnippet(DmxmlContext context, SnippetConfiguration config) {
            return new DividendChartSnippet(context, config);
        }
    }

    private DividendChartSnippet(DmxmlContext context, SnippetConfiguration config) {
        super(context, config);

        this.block.setParameter("dividendsChart", "true"); // $NON-NLS$

        this.setView(new DividendChartSnippetView(this));
        onParametersChanged();
    }

    @Override
    protected String getChartBlockName() {
        return "IMG_DividendsChart"; // $NON-NLS$
    }

    @Override
    public void setSymbol(InstrumentTypeEnum type, String symbol, String name, String... compareSymbols) {
        this.block.setParameter("symbol", symbol); // $NON-NLS$
        super.setSymbol(type, symbol, name);
    }

    @Override
    protected void onParametersChanged() {
        final SnippetConfiguration config = getConfiguration();

        this.block.setParameter("width", getConfiguration().getInt("chartwidth", 550)); // $NON-NLS$
        this.block.setParameter("height", getConfiguration().getInt("chartheight", 260)); // $NON-NLS$
        this.block.setParameter("period", config.getString("period", "P10Y")); // $NON-NLS$
        this.block.setParameter("splits", config.getBoolean("splits", true)); // $NON-NLS$
        this.block.removeParameter("chartlayout"); // $NON-NLS$
    }

}
