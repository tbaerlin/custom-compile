/*
 * ImgChartAnalyse.java
 *
 * Created on 28.08.2006 16:22:27
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.chart;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.marketmaker.istar.merger.web.easytrade.block.HistoricConfigurationMBean;
import net.sf.ehcache.Ehcache;
import org.springframework.validation.BindException;

import de.marketmaker.istar.analyses.frontend.AnalysesServer;
import de.marketmaker.istar.merger.provider.HistoricRatiosProvider;
import de.marketmaker.istar.merger.provider.IntradayProvider;
import de.marketmaker.istar.merger.provider.IsoCurrencyConversionProvider;
import de.marketmaker.istar.merger.provider.StockAnalysisProvider;
import de.marketmaker.istar.merger.provider.TradingPhaseProvider;
import de.marketmaker.istar.merger.provider.calendar.TradingCalendarProvider;
import de.marketmaker.istar.merger.provider.certificatedata.CertificateDataProvider;
import de.marketmaker.istar.merger.provider.funddata.FundDataProvider;
import de.marketmaker.istar.merger.provider.historic.HistoricTimeseriesProvider;
import de.marketmaker.istar.merger.provider.historic.HistoricTimeseriesProviderCombi;
import de.marketmaker.istar.merger.web.easytrade.block.EasytradeInstrumentProvider;
import de.marketmaker.istar.chart.ChartModelAndView;
import de.marketmaker.istar.chart.ChartModelNames;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class ImgChartAnalyse extends AbstractImgChart {

    private HistoricConfigurationMBean historicConfigurationMBean;

    private IntradayProvider intradayProvider;

    private EasytradeInstrumentProvider instrumentProvider;

    private HistoricTimeseriesProvider historicProvider;

    private HistoricTimeseriesProviderCombi historicProviderEod;

    private TradingCalendarProvider tradingCalendarProvider;

    private TradingPhaseProvider tradingPhaseProvider;

    private AnalysesServer analysesServer;

    private StockAnalysisProvider stockAnalysisProvider;

    private FundDataProvider fundDataProvider;

    private HistoricRatiosProvider historicRatiosProvider;

    private CertificateDataProvider certificateDataProvider;

    private IsoCurrencyConversionProvider isoCurrencyConversionProvider;

    /**
     * A cache shared by this controller and the one that computed the chart's URL
     * {@link de.marketmaker.istar.merger.web.easytrade.block.ImgChartAnalyse}. If that other
     * block already retrieved the intraday price, it is almost certain that we will find it
     * in this cache.
     */
    private Ehcache chartPriceCache;

    public ImgChartAnalyse() {
        super(ImgChartAnalyseCommand.class);
    }

    Indicator getIndicator(String key) {
        return Indicator.getInstance(key);
    }

    public void setTradingCalendarProvider(TradingCalendarProvider tradingCalendarProvider) {
        this.tradingCalendarProvider = tradingCalendarProvider;
    }

    public void setHistoricProvider(HistoricTimeseriesProvider historicProvider) {
        this.historicProvider = historicProvider;
    }

    public void setHistoricRatiosProvider(HistoricRatiosProvider historicRatiosProvider) {
        this.historicRatiosProvider = historicRatiosProvider;
    }

    public void setHistoricProviderEod(HistoricTimeseriesProviderCombi historicProviderEod) {
        this.historicProviderEod = historicProviderEod;
    }

    public HistoricConfigurationMBean getHistoricConfigurationMBean() {
        return historicConfigurationMBean;
    }

    public void setHistoricConfigurationMBean(HistoricConfigurationMBean historicConfigurationMBean) {
        this.historicConfigurationMBean = historicConfigurationMBean;
    }

    public void setIntradayProvider(IntradayProvider intradayProvider) {
        this.intradayProvider = intradayProvider;
    }

    public void setInstrumentProvider(EasytradeInstrumentProvider instrumentProvider) {
        this.instrumentProvider = instrumentProvider;
    }

    public void setAnalysesServer(AnalysesServer analysesServer) {
        this.analysesServer = analysesServer;
    }

    public void setStockAnalysisProvider(StockAnalysisProvider sap) {
        this.stockAnalysisProvider = sap;
    }

    public void setTradingPhaseProvider(TradingPhaseProvider tradingPhaseProvider) {
        this.tradingPhaseProvider = tradingPhaseProvider;
    }

    public void setChartPriceCache(Ehcache chartPriceCache) {
        this.chartPriceCache = chartPriceCache;
    }

    public void setCertificateDataProvider(CertificateDataProvider certificateDataProvider) {
        this.certificateDataProvider = certificateDataProvider;
    }

    public void setIsoCurrencyConversionProvider(
            IsoCurrencyConversionProvider isoCurrencyConversionProvider) {
        this.isoCurrencyConversionProvider = isoCurrencyConversionProvider;
    }

    CertificateDataProvider getCertificateDataProvider() {
        return this.certificateDataProvider;
    }

    FundDataProvider getFundDataProvider() {
        return this.fundDataProvider;
    }

    public void setFundDataProvider(FundDataProvider fundDataProvider) {
        this.fundDataProvider = fundDataProvider;
    }

    HistoricTimeseriesProvider getHistoricProvider() {
        return historicProvider;
    }

    HistoricTimeseriesProviderCombi getHistoricProviderEod() {
        return historicProviderEod;
    }

    EasytradeInstrumentProvider getInstrumentProvider() {
        return instrumentProvider;
    }

    Ehcache getChartPriceCache() {
        return chartPriceCache;
    }

    IntradayProvider getIntradayProvider() {
        return intradayProvider;
    }

    AnalysesServer getAnalysesServer() {
        return analysesServer;
    }

    StockAnalysisProvider getStockAnalysisProvider() {
        return stockAnalysisProvider;
    }

    TradingCalendarProvider getTradingCalendarProvider() {
        return tradingCalendarProvider;
    }

    TradingPhaseProvider getTradingPhaseProvider() {
        return tradingPhaseProvider;
    }

    HistoricRatiosProvider getHistoricRatiosProvider() {
        return historicRatiosProvider;
    }

    IsoCurrencyConversionProvider getIsoCurrencyConversionProvider() {
        return this.isoCurrencyConversionProvider;
    }

    protected String resolveStyleName(BaseImgCommand cmd) {
        final String result = super.resolveStyleName(cmd);
        return (((ImgChartAnalyseCommand) cmd).isBw() && result != null)
                ? result.replace(".style", "-bw.style")
                : result;
    }

    ChartModelAndView createResult(ImgChartAnalyseCommand cmd) {
        return createChartModelAndView(cmd);
    }

    protected ChartModelAndView createChartModelAndView(HttpServletRequest request,
            HttpServletResponse response, Object object,
            BindException errors) throws Exception {

        final ImgChartAnalyseCommand cmd = (ImgChartAnalyseCommand) object;

        final ImgChartAnalyseMethod method = ImgChartAnalyseMethod.create(this, errors, cmd);
        final ChartModelAndView result = method.invoke();

        if (result == null || result.getModel().isEmpty()) {
            return null;
        }

        if (cmd.isStrokeMarkers()) {
            result.withStrokeMarkers();
        }
        if (cmd.getLegend() != null) {
            result.withLegend(cmd.getLegend());
        }
        if (cmd.getMinLineWidth() != 0) {
            result.getModel().put(ChartModelNames.MIN_LINE_WIDTH, cmd.getMinLineWidth());
        }

        return result;
    }
}
