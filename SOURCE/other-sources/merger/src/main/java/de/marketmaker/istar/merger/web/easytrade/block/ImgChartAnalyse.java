/*
 * ImgChartAnalyse.java
 *
 * Created on 13.07.2006 07:06:22
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.block;

import de.marketmaker.istar.domainimpl.data.HistoricDataProfiler;
import de.marketmaker.istar.domainimpl.data.HistoricDataProfiler.Entitlement;
import de.marketmaker.istar.merger.web.PermissionDeniedException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import org.joda.time.Interval;
import org.joda.time.LocalDate;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import de.marketmaker.istar.common.util.DateUtil;
import de.marketmaker.istar.domain.data.HighLow;
import de.marketmaker.istar.domain.data.PriceQuality;
import de.marketmaker.istar.domain.data.PriceRecord;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.domain.profile.Profile;
import de.marketmaker.istar.merger.context.RequestContextHolder;
import de.marketmaker.istar.merger.provider.HighLowProvider;
import de.marketmaker.istar.merger.provider.IntradayProvider;
import de.marketmaker.istar.merger.provider.IsoCurrencyConversionProvider;
import de.marketmaker.istar.merger.provider.funddata.FundDataProvider;
import de.marketmaker.istar.merger.web.easytrade.chart.ChartBenchmarkSupport;
import de.marketmaker.istar.merger.web.easytrade.chart.ImgChartAnalyseCommand;
import de.marketmaker.istar.merger.web.easytrade.chart.ImgChartAnalyseMethod;

/**
 * Returns the url of a general purpose timeseries chart. Numerous parameters are available to
 * specify what should be rendered and how, but their applicability for a given chart may
 * vary.
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class ImgChartAnalyse extends EasytradeChartController {

    private EasytradeInstrumentProvider instrumentProvider;

    private FundDataProvider fundDataProvider;

    private IntradayProvider intradayProvider;

    private HighLowProvider highLowProvider;

    private IsoCurrencyConversionProvider isoCurrencyConversionProvider;

    /**
     * A cache shared by this controller and the one that actually draws a chart
     * {@link de.marketmaker.istar.merger.web.easytrade.chart.ImgChartAnalyse}. Used if this
     * controller is requested to return a price together with the chart's url. The cache should
     * ensure in most cases that the price data must only be retrieved once.
     */
    private Ehcache chartPriceCache;

    private HistoricDataProfiler historicDataProfiler;

    private String template = "imgchartanalyse";

    public ImgChartAnalyse() {
        super(ImgChartAnalyseCommand.class, "chartAnalyse.png");
    }

    public void setTemplate(String template) {
        this.template = template;
    }

    public void setHighLowProvider(HighLowProvider highLowProvider) {
        this.highLowProvider = highLowProvider;
    }

    public void setInstrumentProvider(EasytradeInstrumentProvider instrumentProvider) {
        this.instrumentProvider = instrumentProvider;
    }

    public void setFundDataProvider(FundDataProvider fundDataProvider) {
        this.fundDataProvider = fundDataProvider;
    }

    public void setIntradayProvider(IntradayProvider intradayProvider) {
        this.intradayProvider = intradayProvider;
    }

    public void setIsoCurrencyConversionProvider(
            IsoCurrencyConversionProvider isoCurrencyConversionProvider) {
        this.isoCurrencyConversionProvider = isoCurrencyConversionProvider;
    }

    public void setChartPriceCache(Ehcache chartPriceCache) {
        this.chartPriceCache = chartPriceCache;
    }

    public void setHistoricDataProfiler(HistoricDataProfiler historicDataProfiler) {
        this.historicDataProfiler = historicDataProfiler;
    }

    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response,
            Object o, BindException errors) {
        final ImgChartAnalyseCommand cmd = (ImgChartAnalyseCommand) o;

        final Map<String, Object> model = new HashMap<>();

        final Quote quote = this.instrumentProvider.getQuote(cmd);
        if (cmd.isWithPrice()) {
            addPriceToModel(cmd, model, quote);
        }

        final Profile profile = RequestContextHolder.getRequestContext().getProfile();
        final Interval interval = ImgChartAnalyseMethod.getInterval(cmd, quote);
        final Entitlement entitlement = this.historicDataProfiler.getEntitlement(profile, quote);
        if (!entitlement.validateInterval(profile, quote, interval)) {
            throw new PermissionDeniedException(entitlement.getMessage());
        }

        if (cmd.isAdjustFrom() && !cmd.isIntraday()) {
            adjustFrom(cmd, model, quote);
        }

        // addPriceToModel might have changed cmd, so add it only now
        addRequestToModel(cmd, model);
        return new ModelAndView(this.template, model);
    }

    private void adjustFrom(ImgChartAnalyseCommand cmd, Map<String, Object> model, Quote quote) {
        if (quote == null) {
            return;
        }
        int first = quote.getFirstHistoricPriceYyyymmdd();
        if (first == 0) {
            return;
        }

        Interval interval = ImgChartAnalyseMethod.getInterval(cmd, quote);

        final ChartBenchmarkSupport cbs = new ChartBenchmarkSupport(cmd, quote,
                this.instrumentProvider, this.fundDataProvider);
        Quote[] benchmarks = cbs.identifyBenchmarks();
        for (Quote q : benchmarks) {
            if (q != null) {
                first = Math.max(first, q.getFirstHistoricPriceYyyymmdd());
            }
        }
        LocalDate earliestCommonFrom = DateUtil.yyyyMmDdToLocalDate(first);

        if (interval.getStart().toLocalDate().isBefore(earliestCommonFrom)) {
            model.put("adjustedFrom", earliestCommonFrom);
            cmd.setFrom(earliestCommonFrom.toString());
            cmd.setTo(interval.getEnd().toLocalDate().toString());
        }
    }

    private void addPriceToModel(ImgChartAnalyseCommand cmd, Map<String, Object> model, Quote quote) {
        if (quote == null) {
            return;
        }

        // ensure that chart will be requested with qid
        cmd.setSymbol(quote.getId() + EasytradeInstrumentProvider.QID_SUFFIX);
        cmd.setMarket(null);

        PriceRecord price = getPrice(quote);
        HighLow highLow = getHighLow(quote, price);

        if (cmd.getCurrency() != null) {
            // The following two lists need to be mutable as they could be modified inside
            // CurrencyConversionMethod#invoke(), i.e. do not use Collections.singletonList()
            //noinspection ArraysAsListWithZeroOrOneArgument
            final List<PriceRecord> priceRecords = Arrays.asList(price);
            //noinspection ArraysAsListWithZeroOrOneArgument
            final List<HighLow> highLows = Arrays.asList(highLow);
            new CurrencyConversionMethod(this.isoCurrencyConversionProvider,
                    cmd.getCurrency()).invoke(Collections.singletonList(quote), priceRecords, highLows);
            price = priceRecords.get(0);
            highLow = highLows.get(0);
        }

        if (this.chartPriceCache != null) {
            final String key = getCacheKey(quote, price.getPriceQuality(), cmd.getCurrency());
            this.chartPriceCache.put(new Element(key, price));
        }

        model.put("quote", quote);
        model.put("price", price);
        model.put("highLow", highLow);
    }

    private PriceRecord getPrice(Quote quote) {
        final List<PriceRecord> priceRecords
                = this.intradayProvider.getPriceRecords(Collections.singletonList(quote));
        return priceRecords.get(0);
    }

    private HighLow getHighLow(Quote quote, PriceRecord price) {
        return this.highLowProvider.getHighLow52W(quote, price);
    }

    public static String getCacheKey(Quote q, String currency) {
        return getCacheKey(q, RequestContextHolder.getRequestContext().getProfile().getPriceQuality(q), currency);
    }

    private static String getCacheKey(Quote q, PriceQuality pq, String currency) {
        return q.getId() + "." + (currency != null ? currency + "." : "") + pq;
    }
}
