/*
 * ChartcenterSnippet.java
 *
 * Created on 28.05.2008 11:24:14
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */

package de.marketmaker.iview.mmgwt.mmweb.client.snippets;

import com.extjs.gxt.ui.client.util.DateWrapper;
import com.google.gwt.i18n.shared.DateTimeFormat;

import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.itools.gwtutil.client.util.date.MmJsDate;
import de.marketmaker.iview.dmxml.ErrorType;
import de.marketmaker.iview.dmxml.IMGResult;
import de.marketmaker.iview.dmxml.IdentifierData;
import de.marketmaker.iview.dmxml.MSCListDetailElement;
import de.marketmaker.iview.dmxml.MSCListDetails;
import de.marketmaker.iview.dmxml.MSCPriceData;
import de.marketmaker.iview.dmxml.MSCPriceDatas;
import de.marketmaker.iview.dmxml.MSCPriceDatasElement;
import de.marketmaker.iview.dmxml.MSCQuoteMetadata;
import de.marketmaker.iview.mmgwt.mmweb.client.FeatureFlags;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.QuoteWithInstrument;
import de.marketmaker.iview.mmgwt.mmweb.client.data.InstrumentTypeEnum;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PlaceChangeEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PricesUpdatedEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PushRegisterEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PushRegisterHandler;
import de.marketmaker.iview.mmgwt.mmweb.client.history.HistoryToken;
import de.marketmaker.iview.mmgwt.mmweb.client.prices.PriceSupport;
import de.marketmaker.iview.mmgwt.mmweb.client.push.PushRenderItem;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DateTimeUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;
import de.marketmaker.iview.mmgwt.mmweb.client.util.PdfOptionSpec;
import de.marketmaker.iview.mmgwt.mmweb.client.util.PriceUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static de.marketmaker.iview.mmgwt.mmweb.client.FeatureFlags.Feature;

/**
 * @author Ulrich Maurer
 */
public class ChartcenterSnippet extends AbstractSnippet<ChartcenterSnippet, ChartcenterSnippetView>
        implements SymbolSnippet, PdfUriSnippet, PushRegisterHandler {
    static final String CHART_NOT_AVAILABLE = I18n.I.messageChartNotAvailable();

    static final String[] CER_PARAMETERS = new String[]{"certBonus", "barrier", "cap"}; // $NON-NLS-0$ $NON-NLS-1$ $NON-NLS-2$

    private final PriceSupport priceSupport = new PriceSupport(this);

    public static class Class extends SnippetClass {
        public Class() {
            super("Chartcenter"); // $NON-NLS-0$
        }

        public Snippet newSnippet(DmxmlContext context, SnippetConfiguration config) {
            return new ChartcenterSnippet(context, config);
        }
    }

    // HACK: changing the period of a portrait chart is supposed to change the
    // period of a chartcenter chart as well, although both charts are snippets owned by
    // different snippets controllers and therefore can't reference each other.
    // This map acts a a means to share data between those charts.

    public final static Map<InstrumentTypeEnum, String> PERIODS = new HashMap<>();

    private int chartWidth;

    private int chartHeight;

    protected final DmxmlContext.Block<IMGResult> block;

    protected final DmxmlContext.Block<MSCQuoteMetadata> blockQuoteMetadata;

    protected final DmxmlContext.Block<MSCPriceDatas> blockPriceDatas;

    protected final DmxmlContext.Block<MSCListDetails> blockCompareSymbols;

    protected final DmxmlContext.Block<MSCPriceData> blockPriceData;

    private final boolean asPriceEarningsChart;

    private final boolean withUnderlying;

    public ChartcenterSnippet(DmxmlContext context, SnippetConfiguration config) {
        super(context, config);
        updateWidthAndHeight();

        this.block = createBlock("IMG_Chartcenter"); // $NON-NLS-0$

        this.asPriceEarningsChart = config.getBoolean("asPriceEarningsChart", false); // $NON-NLS-0$
        this.withUnderlying = config.getBoolean("withUnderlying", false); // $NON-NLS-0$
        if (this.asPriceEarningsChart || this.withUnderlying || showContributorPortrait()) {
            this.blockQuoteMetadata = createBlock("MSC_QuoteMetadata"); // $NON-NLS-0$
        }
        else {
            this.blockQuoteMetadata = null;
        }

        this.blockPriceDatas = createBlock("MSC_PriceDatas"); // $NON-NLS$
        this.blockPriceDatas.setParameter("disablePaging", "true"); // $NON-NLS$
        this.blockPriceDatas.setParameter("marketStrategy", config.getString("marketStrategy", null)); // $NON-NLS$

        this.blockPriceData = createBlock("MSC_PriceData"); // $NON-NLS-0$
        this.blockPriceData.setParameter("marketStrategy", config.getString("marketStrategy", null)); // $NON-NLS$

        this.blockCompareSymbols = createBlock("MSC_PriceDataMulti"); // $NON-NLS$
        this.blockCompareSymbols.setParameter("disablePaging", "true"); // $NON-NLS$
        this.blockCompareSymbols.setEnabled(false);

        this.block.setParameter("width", this.chartWidth); // $NON-NLS-0$
        this.block.setParameter("height", this.chartHeight); // $NON-NLS-0$
        this.block.setParameter("period", config.getString("period", "P1D")); // $NON-NLS-0$ $NON-NLS-1$ $NON-NLS-2$
        this.block.setParameter("chartlayout", config.getString("layout", "basic")); // $NON-NLS-0$ $NON-NLS-1$ $NON-NLS-2$
        this.block.setParameter("hilo", true); // $NON-NLS$
        this.block.setParameter("adjustFrom", config.getBoolean("adjustFrom", true)); // $NON-NLS$

        this.setView(new ChartcenterSnippetView(this));
    }

    public boolean isEndOfDay(MSCPriceDatasElement data) {
        return PriceUtil.isEndOfDay(data);
    }

    public boolean isFundPriceData(MSCPriceDatasElement data) {
        return data.getFundpricedata() != null;
    }

    private void updateWidthAndHeight() {
        this.chartWidth = getConfiguration().getInt("chartwidth", 600); // $NON-NLS-0$
        this.chartHeight = getConfiguration().getInt("chartheight", 260); // $NON-NLS-0$
    }

    public void destroy() {
        destroyBlock(this.block);
        destroyBlock(this.blockPriceData);
        destroyBlock(this.blockPriceDatas);
    }

    public void updateView() {
        if (!block.isResponseOk()
                || !blockPriceDatas.isResponseOk()
                || !blockPriceData.isResponseOk()) {
            showError();
            return;
        }

        final String period = this.block.getParameter("period"); // $NON-NLS-0$

        final MSCPriceDatasElement priceDatasElement = PriceUtil.getSelectedElement(blockPriceDatas);
        if (isEndOfDay(priceDatasElement)
                && !isFundPriceData(priceDatasElement)
                && (period != null && period.endsWith("D"))) { // $NON-NLS-0$
            getView().setError(BasicChartSnippetView.INTRADAY_NOT_ALLOWED);
            return;
        }

        final IMGResult ipr = this.block.getResult();
        if (this.asPriceEarningsChart) {
            if (!this.blockQuoteMetadata.isResponseOk()) {
                getView().setError(CHART_NOT_AVAILABLE);
                return;
            }
            final IdentifierData kgvQuote = this.blockQuoteMetadata.getResult().getKgvQuote();
            if (kgvQuote == null) {
                getView().setError(I18n.I.messagePriceEarningsRatioNotAvailableAbbr());
                return;
            }
            ipr.setRequest(ipr.getRequest().replace(
                    "symbol=" + this.blockQuoteMetadata.getParameter("symbol"), // $NON-NLS-0$ $NON-NLS-1$
                    "symbol=" + kgvQuote.getQuotedata().getQid())); // $NON-NLS-0$
        }
        final ChartcenterForm form = getView().getChartcenterForm();
        if (this.withUnderlying) {
            if (!this.blockQuoteMetadata.isResponseOk()) {
                form.setUnderlying(null);
            }
            else {
                final List<IdentifierData> listUnderlying = this.blockQuoteMetadata.getResult().getUnderlying();
                if (listUnderlying.size() != 1) {
                    form.setUnderlyingIsBasket();
                }
                else {
                    final IdentifierData underlying = listUnderlying.get(0);
                    form.setUnderlying(new QuoteWithInstrument(underlying.getInstrumentdata(), underlying.getQuotedata()));
                }
            }
        }
        boolean isContributorQuote = false;
        if (showContributorPortrait() && this.blockQuoteMetadata.isResponseOk()) {
            isContributorQuote = this.blockQuoteMetadata.getResult().isContributorQuote();
        }
        getView().setImage(ipr, !isFundPriceData(priceDatasElement) && !isContributorQuote);
        this.priceSupport.activate();

        if (this.blockCompareSymbols.isEnabled() && this.blockCompareSymbols.isResponseOk()) {
            final List<MSCListDetailElement> listElements = this.blockCompareSymbols.getResult().getElement();
            Firebug.log("ChartcenterSnippet - listElements: " + listElements.size());
            final QuoteWithInstrument[] qwis = new QuoteWithInstrument[listElements.size()];
            for (int i = 0, size = listElements.size(); i < size; i++) {
                final MSCListDetailElement element = listElements.get(i);
                qwis[i] = new QuoteWithInstrument(element.getInstrumentdata(), element.getQuotedata(), element.getItemname());
            }
            getView().setCompareItems(qwis);
            getConfiguration().remove("compareSymbols"); // $NON-NLS$
        }
        else {
            Firebug.log("ChartcenterSnippet - listElements: nope");
        }

        if (this.block.isEnabled() && this.block.isResponseOk()) {
            final IMGResult result = this.block.getResult();
            final String adjustedFrom = result.getAdjustedFrom();
            if (adjustedFrom != null && (adjustedFrom.trim().length() > 0)) {
                final Date date = DateTimeFormat.getFormat("dd.MM.yyyy").parse(adjustedFrom);   // $NON-NLS-0$
                adjustFromValue(date);
            }
        }

        if (this.blockPriceData.isEnabled() && this.blockPriceData.isResponseOk()) {
            final String priceType = this.blockPriceData.getResult().getPricedatatype();
            form.setPriceType(priceType);
        }
    }

    private boolean showContributorPortrait() {
        return FeatureFlags.Feature.CONTRIBUTOR_PORTRAIT.isEnabled()
                && SessionData.INSTANCE.getGuiDef("showContributorPortrait").booleanValue(); // $NON-NLS$
    }

    private void adjustFromValue(Date date) {
        final SnippetConfiguration config = getConfiguration();
        config.put("from", DateTimeFormat.getFormat("dd.MM.yyy").format(date)); // $NON-NLS$

        getView().getChartcenterForm().setAdjustedFrom(new MmJsDate(date));
    }


    @Override
    public void deactivate() {
        super.deactivate();
        this.priceSupport.deactivate();
    }

    private void showError() {
        if (getConfiguration().getString("symbol") == null) { // $NON-NLS-0$
            getView().setError(I18n.I.messagePleaseConfigureInstrument());
            return;
        }
        final ErrorType et = this.block.getError();
        // TODO: the error text is not really helpful for the user...
        if (et == null) {
            getView().setError(this.block.toString());
        }
        else {
            getView().setError(et.getCode() + ": " + et.getDescription() + " / " + block.toString()); // $NON-NLS-0$ $NON-NLS-1$
        }
    }

    public void setSymbol(InstrumentTypeEnum type, String symbol, String name,
            String... compareSymbols) {
        getView().setType(type);
        final SnippetConfiguration config = getConfiguration();
        config.put("symbol", symbol); // $NON-NLS-0$
        if (compareSymbols.length > 0) {
            final String joinedSymbols = StringUtil.join(';', compareSymbols);
            Firebug.log("ChartcenterSnippet - cs = " + joinedSymbols);
            config.put("compareSymbols", joinedSymbols); // $NON-NLS$
        }
        onParametersChanged();
    }

    private void setCerFlags(String value) {
        for (String name : CER_PARAMETERS) {
            this.block.setParameter(name, value);
        }
    }

    @Override
    public void onPlaceChange(PlaceChangeEvent event) {
        super.onPlaceChange(event);
        initPercentFromEvent(event);
        initPeriodFromEvent(event);
        initCurrencyFromEvent(event);
    }

    private void initPercentFromEvent(PlaceChangeEvent event) {
        final String percent = event.getProperty("percent"); // $NON-NLS-0$
        if ("true".equals(percent)) { // $NON-NLS$
            getConfiguration().put("percent", percent); // $NON-NLS$
        }
        else {
            // charts that trigger the event don't have an "absolute values" parameter, so
            // percent=false just means: don't use percent
            getConfiguration().remove("percent"); // $NON-NLS$
        }
        getView().updatePercent();
    }

    private void initCurrencyFromEvent(PlaceChangeEvent event) {
        final String currency = event.getProperty("currency"); // $NON-NLS-0$
        if (currency != null) {
            getConfiguration().put("currency", currency); // $NON-NLS$
            // fix for R-63824:
            this.block.setParameter("currency", currency); // $NON-NLS$
            getView().updateCurrency();
        }
    }

    private void initPeriodFromEvent(PlaceChangeEvent event) {
        final String period = getPeriod(event);
        if (period != null) {
            setPeriod(period);
            getView().updatePeriod();
            updatePeriodParameters();
        }
    }

    public void updatePeriod(String period) {
        setPeriod(period);
        getView().updatePeriod();
        updatePeriodParameters();
    }

    private String getPeriod(PlaceChangeEvent event) {
        final HistoryToken historyToken = event.getHistoryToken();
        final String period = event.getProperty("period"); // $NON-NLS-0$
        if (period != null) {
            return period;
        }
        return PERIODS.remove(InstrumentTypeEnum.fromToken(historyToken.getControllerId()));
    }

    void setPeriod(String from, String to) {
        final SnippetConfiguration config = getConfiguration();
        config.remove("period"); // $NON-NLS-0$
        config.put("from", from); // $NON-NLS-0$
        config.put("to", to); // $NON-NLS-0$
    }

    void setPeriod(String period) {
        if (period == null || DateTimeUtil.PERIOD_KEY_ALL.equals(period)) {
            setPeriod("start", "today"); // $NON-NLS-0$ $NON-NLS-1$
        }
        else if (DateTimeUtil.PERIOD_KEY_YEAR_TO_DATE.equals(period)) {
            setPeriod("01.01." + new DateWrapper().getFullYear(), "today"); // $NON-NLS-0$ $NON-NLS-1$
        }
        else {
            final SnippetConfiguration config = getConfiguration();
            config.put("period", period); // $NON-NLS-0$
            config.remove("from"); // $NON-NLS-0$
            config.remove("to"); // $NON-NLS-0$
        }
    }

    private void updatePeriodParameters() {
        final SnippetConfiguration config = getConfiguration();
        this.block.setParameter("period", config.getString("period", null)); // $NON-NLS-0$ $NON-NLS-1$
        this.block.setParameter("from", config.getString("from", null)); // $NON-NLS-0$ $NON-NLS-1$
        this.block.setParameter("to", config.getString("to", null)); // $NON-NLS-0$ $NON-NLS-1$
    }

    @Override
    public void onParametersChanged() {
        updateWidthAndHeight();
        updatePeriodParameters();

        final SnippetConfiguration config = getConfiguration();
        final boolean showUnderlying = config.getBoolean("underlying", false); // $NON-NLS$
        final String symbol = config.getString("symbol", null); // $NON-NLS$
        if (showUnderlying) {
            this.block.setParameter("symbol", "underlying(" + symbol + ")"); // $NON-NLS$
            setCerFlags("true"); // $NON-NLS$
            this.block.setParameter("derivative", symbol); // $NON-NLS$

            this.blockPriceDatas.setParameter("symbol", "underlying(" + symbol + ")"); // $NON-NLS$
        }
        else {
            this.block.setParameter("symbol", symbol); // $NON-NLS$
            setCerFlags(null);
            this.block.removeParameter("derivative"); // $NON-NLS$

            this.blockPriceDatas.setParameter("symbol", symbol); // $NON-NLS$
        }
        this.block.setParameter("type", config.getString("type", "line.mnt")); // $NON-NLS$
        this.block.setParameter("gd", config.getString("gd", null)); // $NON-NLS$
        this.block.setParameter("gdColor", config.getString("gdColor", null)); // $NON-NLS$
        this.block.setParameter("indicator", config.getString("indicator", null)); // $NON-NLS$
        this.block.setParameter("volume", config.getString("volume", null)); // $NON-NLS$
        this.block.setParameter("logScales", config.getString("logScales", null)); // $NON-NLS$
        this.block.setParameter("percent", config.getString("percent", null)); // $NON-NLS$
        this.block.setParameter("currency", config.getString("currency", null)); // $NON-NLS$
        this.block.setParameter("splits", config.getString("splits", null)); // $NON-NLS$
        this.block.setParameter("bviPerformanceForFunds", config.getString("bviPerformanceForFunds", null)); // $NON-NLS$
        this.block.setParameter("blendCorporateActions", config.getString("blendCorporateActions", "true")); // $NON-NLS$
        this.block.setParameter("blendDividends", config.getString("blendDividends", "false")); // $NON-NLS$
        this.block.setParameter("ask", config.getString("ask", null)); // $NON-NLS$
        this.block.setParameter("bid", config.getString("bid", null)); // $NON-NLS$
        this.block.setParameter("width", getChartWidth()); // $NON-NLS$
        this.block.setParameter("height", getChartHeight()); // $NON-NLS$        
        this.block.setParameter("hilo", config.getBoolean("hilo", true)); // $NON-NLS$
        this.block.setParameter("mainField", config.getString("mainField", null)); // $NON-NLS$
        this.block.setParameter("adjustFrom", config.getBoolean("adjustFrom", true)); // $NON-NLS$

        if (this.blockQuoteMetadata != null) {
            this.blockQuoteMetadata.setParameter("symbol", symbol); // $NON-NLS$
        }

        this.blockPriceDatas.setParameter("marketStrategy", config.getString("marketStrategy", null)); // $NON-NLS$

        final String compareSymbols = config.getString("compareSymbols", null); // $NON-NLS$
        if (compareSymbols == null) {
            this.block.setParameter("benchmark", config.getString("benchmark", null)); // $NON-NLS$
            this.block.setParameter("benchmarkColor", config.getString("benchmarkColor", null)); // $NON-NLS$
            this.blockCompareSymbols.setEnabled(false);
        }
        else {
            final String[] splitted = compareSymbols.split("[,;]");
            this.block.setParameter("benchmark", compareSymbols); // $NON-NLS$
            this.block.setParameter("benchmarkColor", joinBenchmarkColors(splitted.length)); // $NON-NLS$
            this.blockCompareSymbols.setEnabled(true);
            this.blockCompareSymbols.setParameters("symbol", splitted); // $NON-NLS$
        }

        // to figure out the pricedatatype
        this.blockPriceData.setParameter("symbol", symbol); // $NON-NLS$
        this.blockPriceData.setParameter("marketStrategy", config.getString("marketStrategy", null)); // $NON-NLS$
    }

    private String joinBenchmarkColors(int length) {
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            if (sb.length() > 0) {
                sb.append(';');
            }
            sb.append(ChartcenterForm.getBenchmarkColor(i));
        }
        return sb.toString();
    }


    public IMGResult getIMG() {
        return this.block.getResult();
    }

    public int getChartWidth() {
        return this.chartWidth;
    }

    public int getChartHeight() {
        return this.chartHeight;
    }

    public boolean isTopToolbarUri() {
        return true;
    }

    public boolean isSameQuote(QuoteWithInstrument qwi) {
        return !this.block.isResponseOk() || (qwi.getQuoteData().getQid().equals(this.block.getResult().getQuotedata().getQid()));
    }

    public PdfOptionSpec getPdfOptionSpec() {
        // TODO: support for showing underlying with bonus/barrier?!
        return new PdfOptionSpec("chartcenter.pdf", createParameterMap(), "pdf_options_format"); // $NON-NLS$
    }

    public Map<String, String> createParameterMap() {
        final SnippetConfiguration config = getConfiguration();
        final Map<String, String> map = new HashMap<>();
        setParameter(config, map, "symbol", null); // $NON-NLS$
        setParameter(config, map, "marketStrategy", null); // $NON-NLS$
        setParameter(config, map, "period", null); // $NON-NLS$
        setParameter(config, map, "from", null); // $NON-NLS$
        setParameter(config, map, "to", null); // $NON-NLS$
        setParameter(config, map, "type", "line.mnt"); // $NON-NLS$
        setParameter(config, map, "gd", null); // $NON-NLS$
        setParameter(config, map, "gdColor", null); // $NON-NLS$
        setParameter(config, map, "benchmark", null); // $NON-NLS$
        setParameter(config, map, "benchmarkColor", null); // $NON-NLS$
        setParameter(config, map, "indicator", null); // $NON-NLS$
        setParameter(config, map, "turnover", null); // $NON-NLS$
        setParameter(config, map, "volume", null); // $NON-NLS$
        setParameter(config, map, "logScales", null); // $NON-NLS$
        setParameter(config, map, "percent", null); // $NON-NLS$
        setParameter(config, map, "currency", null); // $NON-NLS$
        setParameter(config, map, "splits", null); // $NON-NLS$
        setParameter(config, map, "bviPerformanceForFunds", null); // $NON-NLS$
        setParameter(config, map, "blendCorporateActions", "true"); // $NON-NLS$
        setParameter(config, map, "blendDividends", "false"); // $NON-NLS$
        setParameter(config, map, "chartStyle", null); // $NON-NLS$
        setParameter(config, map, "bid", null); // $NON-NLS$
        setParameter(config, map, "ask", null); // $NON-NLS$
        setParameter(config, map, "hilo", null); // $NON-NLS$
        return map;
    }

    private void setParameter(final SnippetConfiguration config, Map<String, String> map,
            final String key, final String defaultValue) {
        final String value = config.getString(key, defaultValue);
        if (value != null) {
            map.put(key, value);
        }
    }

    public ArrayList<PushRenderItem> onPushRegister(PushRegisterEvent event) {
        if (isIntraday() && this.block.isResponseOk()) {
            if (event.addVwdcode(getIMG())) {
                event.addComponentToReload(this.block, this);
            }
        }
        return null;
    }

    private boolean isIntraday() {
        final String p = this.block.getParameter("period"); // $NON-NLS-0$
        return (p != null && p.length() == 3 && p.endsWith("D")); // $NON-NLS-0$
    }

    public void onPricesUpdated(PricesUpdatedEvent event) {
        if (event.isPushedUpdate() && this.block.isResponseOk()) {
            if (this.priceSupport.isNewerPriceAvailable(this.block.getResult().getQuotedata())) {
                this.block.setToBeRequested();
                this.blockPriceDatas.setToBeRequested();
            }
        }
    }

    public List<String> getBenchmarkNames() {
        return getView().getChartcenterForm().getBenchmarkNames();
    }
}
