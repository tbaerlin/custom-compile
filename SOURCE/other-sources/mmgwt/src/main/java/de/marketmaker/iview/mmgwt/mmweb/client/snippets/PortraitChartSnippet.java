/*
 * PortraitChartSnippet.java
 *
 * Created on 28.03.2008 16:11:58
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.snippets;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.ui.Widget;

import de.marketmaker.iview.dmxml.IMGResult;
import de.marketmaker.iview.dmxml.InstrumentData;
import de.marketmaker.iview.dmxml.MSCPriceDataExtended;
import de.marketmaker.iview.dmxml.MSCPriceDataExtendedElement;
import de.marketmaker.iview.dmxml.MSCQuotes;
import de.marketmaker.iview.dmxml.MSCStaticData;
import de.marketmaker.iview.dmxml.QuoteData;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.QuoteWithInstrument;
import de.marketmaker.iview.mmgwt.mmweb.client.data.InstrumentTypeEnum;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.config.SnippetConfigurationView;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;
import de.marketmaker.iview.mmgwt.mmweb.client.util.PlaceUtil;
import de.marketmaker.iview.tools.i18n.NonNLS;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class PortraitChartSnippet extends
        BasicChartSnippet<PortraitChartSnippet, PortraitChartSnippetView>
        implements SymbolSnippet, SymbolListSnippet, PdfParameterSnippet {

    public static class Class extends SnippetClass {
        public Class() {
            super("PortraitChart", I18n.I.chart()); // $NON-NLS-0$
        }

        public Snippet newSnippet(DmxmlContext context, SnippetConfiguration config) {
            return new PortraitChartSnippet(context, config);
        }

        protected void addDefaultParameters(SnippetConfiguration config) {
            config.put("nameInTitle", true); // $NON-NLS-0$
            config.put("resetTitle", false); // $NON-NLS-0$
        }
    }

    private DmxmlContext.Block<MSCQuotes> quotesBlock = null;

    private DmxmlContext.Block<MSCStaticData> blockStatic = null;

    private DmxmlContext.Block<MSCPriceDataExtended> underlyingPrices;

    private String underlyingName = null;

    private String benchmarkQid = null;

    private final boolean resetTitle;

    private PortraitChartSnippet(DmxmlContext context, SnippetConfiguration config) {
        super(context, config);

        if (isPerformance()) {
            getConfiguration().put("bviPerformanceForFunds", "true"); // $NON-NLS$
        }

        if (getConfiguration().getString("period", null) == null // $NON-NLS$
                && getConfiguration().getString("start", null) == null) { // $NON-NLS$
            getConfiguration().put("period", "P1Y"); // $NON-NLS$
        }

        if (isStandalone()) {
            this.quotesBlock = createBlock("MSC_Quotes"); // $NON-NLS-0$
            this.quotesBlock.setParameter("disablePaging", true); // $NON-NLS-0$
        }

        if (getButtonConfig().contains("U")) { // $NON-NLS-0$
            this.underlyingPrices = createBlock("MSC_PriceDataExtended"); // $NON-NLS-0$
        }

        this.blockStatic = createBlock("MSC_StaticData"); // $NON-NLS-0$

        this.resetTitle = config.getBoolean("resetTitle", true); // $NON-NLS-0$

        this.setView(new PortraitChartSnippetView(this));

        onParametersChanged();
    }

    public void configure(Widget triggerWidget) {
        final SnippetConfigurationView configView = new SnippetConfigurationView(this);
        configView.addSelectSymbol(null);
        configView.show();
    }

    public void destroy() {
        super.destroy();
        destroyBlock(this.quotesBlock);
        destroyBlock(this.blockStatic);
        destroyBlock(this.underlyingPrices);
    }

    public String getDropTargetGroup() {
        return DROP_TARGET_GROUP_INSTRUMENT;
    }

    public void goToPortrait() {
        if (!this.block.isResponseOk()) {
            return;
        }
        final IMGResult result = this.block.getResult();
        PlaceUtil.goToPortrait(result.getInstrumentdata(), result.getQuotedata());
    }

    public boolean isConfigurable() {
        return true;
    }

    public boolean notifyDrop(QuoteWithInstrument qwi) {
        if (qwi == null) {
            return false;
        }
        onSymbolChange(qwi.getName(), qwi.getId());
        return true;
    }

    private void onSymbolChange(final String title, final String symbol) {
        this.quotesBlock.enable();
        getConfiguration().put("title", title); // $NON-NLS-0$
        getConfiguration().put("symbol", symbol); // $NON-NLS-0$
        ackParametersChanged();
    }


    public void setParameters(HashMap<String, String> params) {
        final QuoteWithInstrument qwi = QuoteWithInstrument.getLastSelected();
        if (qwi != null) {
            onSymbolChange(qwi.getName(), qwi.getId());
        }
    }

    public void setSymbol(InstrumentTypeEnum type, String symbol, String name,
            String... compareSymbols) {
        getView().reset(type);
        if (this.underlyingPrices != null) {
            this.underlyingPrices.setEnabled(true);
            this.underlyingName = null;
        }
        super.setSymbol(type, symbol, name);
    }

    public void setSymbols(List<InstrumentData> symbols) {
        if (symbols == null || symbols.isEmpty()) {
            setVisible(false);
        }
        else {
            setVisible(true);
            final InstrumentData id = symbols.get(0);
            setSymbol(InstrumentTypeEnum.valueOf(id.getType()), id.getIid(), null);
        }
    }

    public void setVisible(boolean value) {
        if ((getView() != null) && (getView().container != null)) {
            getView().container.setVisible(value);
        }
    }

    public void updateView() {
        setBenchmarkQid();

        super.updateView();

        if (!this.block.isResponseOk()) {
            return;
        }

        final IMGResult imgResult = this.block.getResult();
        final InstrumentData id = imgResult.getInstrumentdata();
        final QuoteData qd = imgResult.getQuotedata();

        final InstrumentTypeEnum type = InstrumentTypeEnum.valueOf(id.getType());
        if ("fund".equals(getConfiguration().getString("intraday")) && type == InstrumentTypeEnum.FND) { // $NON-NLS-0$ $NON-NLS-1$
            getView().setIntradayEnabled(!"FONDS".equals(qd.getMarketVwd())); // $NON-NLS-0$
        }

        if (isEnabled(this.quotesBlock)) {
            getView().reloadTitle();
            if (this.quotesBlock.isResponseOk()) {
                getView().updateQuotesMenu(this.quotesBlock.getResult().getQuotedata(), qd);
            }
            else {
                getView().disableQuotesMenu();
            }
            this.quotesBlock.disable();
        }
        if (isEnabled(this.underlyingPrices)) {
            if (isWithUnderlyingPrices()) {
                getView().enableUnderlyingButton();
                this.underlyingName = this.underlyingPrices.getResult().getElement().get(0).getInstrumentdata().getName();
            }
            this.underlyingPrices.setEnabled(false);
        }
//        this.block.getParameter("bviPerformanceForFunds")
    }

    @NonNLS
    private void setBenchmarkQid() {
        this.benchmarkQid = getConfiguration().getString("benchmarkQuote");

        if (this.benchmarkQid == null && this.blockStatic.isEnabled() && this.blockStatic.isResponseOk()) {
            this.benchmarkQid = this.blockStatic.getResult().getBenchmark().getQuotedata().getQid();
        }
    }

    private boolean isWithUnderlyingPrices() {
        if (this.underlyingPrices.isResponseOk()) {
            final List<MSCPriceDataExtendedElement> list = this.underlyingPrices.getResult().getElement();
            return list != null && list.size() == 1 && list.get(0).getPricedataExtended() != null;
        }
        return false;
    }

    private boolean isEnabled(DmxmlContext.Block<?> block) {
        return block != null && block.isEnabled();
    }

    private void setCerFlags(String value) {
        for (String name : ChartcenterSnippet.CER_PARAMETERS) {
            this.block.setParameter(name, value);
        }
    }

    @Override
    protected void onParametersChanged() {
        final SnippetConfiguration config = getConfiguration();
        final String symbol = config.getString("symbol", null); // $NON-NLS-0$
        this.block.setEnabled(symbol != null);
        final boolean showUnderlying = config.getBoolean("underlying", false); // $NON-NLS-0$
        if (showUnderlying) {
            config.put("title", this.underlyingName == null ? I18n.I.chart() : I18n.I.chartUnderlyingName(this.underlyingName));  // $NON-NLS-0$
            getView().reloadTitle();
            this.block.setParameter("symbol", "underlying(" + symbol + ")"); // $NON-NLS-0$ $NON-NLS-1$ $NON-NLS-2$
            setCerFlags("true"); // $NON-NLS-0$
            this.block.setParameter("derivative", symbol); // $NON-NLS-0$
            this.block.setParameter("color", "0061b2"); // $NON-NLS-0$ $NON-NLS-1$
        }
        else {
            if (this.resetTitle) {
                config.put("title", I18n.I.chart());  // $NON-NLS-0$
                getView().reloadTitle();
            }
            this.block.setParameter("symbol", symbol); // $NON-NLS-0$
            setCerFlags(null);
            this.block.removeParameter("derivative"); // $NON-NLS-0$
            this.block.removeParameter("color"); // $NON-NLS-0$
        }
        this.block.setParameter("ask", config.getString("ask", null)); // $NON-NLS-0$ $NON-NLS-1$
        this.block.setParameter("bid", config.getString("bid", null)); // $NON-NLS-0$ $NON-NLS-1$
        this.block.setParameter("period", config.getString("period", null)); // $NON-NLS-0$ $NON-NLS-1$
        this.block.setParameter("from", config.getString("from", null)); // $NON-NLS-0$ $NON-NLS-1$
        this.block.setParameter("to", config.getString("to", null)); // $NON-NLS-0$ $NON-NLS-1$
        this.block.setParameter("blendCorporateActions", isCorporateActionsConfigured()); // $NON-NLS-0$ $NON-NLS-1$
        this.block.setParameter("blendDividends", config.getString("blendDividends", null)); // $NON-NLS-0$ $NON-NLS-1$
        this.block.setParameter("percent", isPercentConfigured()); // $NON-NLS$
        final boolean bvi = config.getBoolean("bviPerformanceForFunds", false); // $NON-NLS-0$
        this.block.setParameter("bviPerformanceForFunds", bvi); // $NON-NLS-0$
        if (bvi) {
            this.block.removeParameter("gd1"); // $NON-NLS-0$
        }
        else {
            this.block.setParameter("gd1", "0"); // $NON-NLS-0$ $NON-NLS-1$
        }
        if (isEnabled(this.quotesBlock)) {
            this.quotesBlock.setParameter("symbol", symbol); // $NON-NLS-0$
        }
        if (isEnabled(this.underlyingPrices)) {
            this.underlyingPrices.setParameter("symbol", "underlying(" + symbol + ")"); // $NON-NLS$
        }
        this.blockStatic.setParameter("symbol", symbol); // $NON-NLS-0$
        this.blockStatic.setEnabled(isBenchmarkConfigured());
    }

    @NonNLS
    public String getBenchmarkQid() {
        final String period = getConfiguration().getString("period", null);
        final String from = getConfiguration().getString("from", null);
        final String to = getConfiguration().getString("to", null);
        if ((period == null && from == null && to == null) || "P1D".equals(period) || "P5D".equals(period)) {
            return null;
        }
        if (!isBenchmarkConfigured()) {
            return null;
        }
        return this.benchmarkQid;
    }

    @NonNLS
    protected boolean isBenchmarkConfigured() {
        return getConfiguration().getBoolean("benchmark", false);
    }

    @NonNLS
    protected boolean isPercentConfigured() {
        return getConfiguration().getBoolean("percent", false);
    }

    @NonNLS
    protected boolean isCorporateActionsConfigured() {
        return getConfiguration().getBoolean("blendCorporateActions", true);
    }

    void updateQuote(String qid) {
        // called whenever a different market has been selected, no need to enable quotesBlock
        getConfiguration().put("symbol", qid); // $NON-NLS-0$
        ackParametersChanged();
    }

    boolean isPerformance() {
        return getButtonConfig().contains("P"); // $NON-NLS-0$
    }

    String getButtonConfig() {
        return getConfiguration().getString("buttons", "BKA%"); // $NON-NLS-0$ $NON-NLS-1$
    }

    public void addPdfSnippetParameters(Map<String, String> mapParameters) {
        final String period = getConfiguration().getString("period"); // $NON-NLS$
        if (period != null) {
            mapParameters.put(period.endsWith("D") ? "intradayChartPeriod" : "historicChartPeriod", period); // $NON-NLS$
        }
        else {
            final String from = getConfiguration().getString("from"); // $NON-NLS$
            final String to = getConfiguration().getString("to"); // $NON-NLS$
            if (from != null && to != null) {
                mapParameters.put("historicChartFrom", from); // $NON-NLS$
                mapParameters.put("historicChartTo", to); // $NON-NLS$
            }
        }
    }
}
