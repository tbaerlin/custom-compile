/*
 * FndYieldRiskChart.java
 *
 * Created on 10.12.2007 13:17:15
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.chart;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.marketmaker.istar.merger.web.easytrade.block.HistoricConfigurationMBean;
import org.joda.time.Interval;
import org.joda.time.LocalDate;
import org.springframework.validation.BindException;

import de.marketmaker.istar.common.util.DateUtil;
import de.marketmaker.istar.domain.data.RatioDataRecord;
import de.marketmaker.istar.domain.instrument.InstrumentTypeEnum;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.domain.instrument.QuoteNameStrategy;
import de.marketmaker.istar.merger.context.RequestContextHolder;
import de.marketmaker.istar.merger.provider.RatiosProvider;
import de.marketmaker.istar.merger.provider.UnknownSymbolException;
import de.marketmaker.istar.merger.provider.historic.HistoricRequestImpl;
import de.marketmaker.istar.merger.provider.historic.HistoricTimeseries;
import de.marketmaker.istar.merger.provider.historic.HistoricTimeseriesProvider;
import de.marketmaker.istar.merger.provider.historic.HistoricTimeseriesRequest;
import de.marketmaker.istar.merger.provider.historic.HistoricTimeseriesUtils;
import de.marketmaker.istar.merger.web.easytrade.DefaultSymbolCommand;
import de.marketmaker.istar.merger.web.easytrade.block.EasytradeInstrumentProvider;
import de.marketmaker.istar.merger.web.easytrade.util.FndRatios;
import de.marketmaker.istar.merger.web.easytrade.util.FndSectorSearch;
import de.marketmaker.istar.ratios.RatioFieldDescription;
import de.marketmaker.istar.ratios.frontend.RatioDataResult;
import de.marketmaker.istar.chart.ChartModelAndView;
import de.marketmaker.istar.chart.component.datadrawstyle.LineDrawStyle;
import de.marketmaker.istar.chart.data.TimeSeriesCollection;
import de.marketmaker.istar.chart.data.TimeSeriesFactory;
import de.marketmaker.istar.chart.data.TimeperiodDefinitionBuilder;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class FndSectorChart extends AbstractImgChart {

    private HistoricConfigurationMBean historicConfigurationMBean;

    private RatiosProvider ratiosProvider;

    private EasytradeInstrumentProvider instrumentProvider;

    private HistoricTimeseriesProvider historicProvider;

    protected HistoricTimeseriesProvider historicTimeseriesProviderEod;

    private static final String DEFAULT_PERIOD = "P1Y";

    public FndSectorChart() {
        super(BaseImgSymbolCommand.class);
    }

    public void setInstrumentProvider(EasytradeInstrumentProvider instrumentProvider) {
        this.instrumentProvider = instrumentProvider;
    }

    public void setHistoricConfigurationMBean(HistoricConfigurationMBean historicConfigurationMBean) {
        this.historicConfigurationMBean = historicConfigurationMBean;
    }

    public void setRatiosProvider(RatiosProvider ratiosProvider) {
        this.ratiosProvider = ratiosProvider;
    }

    public void setHistoricProvider(HistoricTimeseriesProvider historicProvider) {
        this.historicProvider = historicProvider;
    }

    public void setHistoricTimeseriesProviderEod(
            HistoricTimeseriesProvider historicTimeseriesProviderEod) {
        this.historicTimeseriesProviderEod = historicTimeseriesProviderEod;
    }

    protected ChartModelAndView createChartModelAndView(HttpServletRequest request,
            HttpServletResponse response, Object object,
            BindException bindException) throws Exception {
        final BaseImgSymbolCommand cmd = (BaseImgSymbolCommand) object;

        final ChartModelAndView result = createChartModelAndView(cmd);
        final Map<String, Object> model = result.getModel();

        final Quote quote = this.instrumentProvider.getQuote(cmd);
        if (quote == null || quote.getInstrument().getInstrumentType() != InstrumentTypeEnum.FND) {
            return null;
        }

        final Map<String, Object> fr = new FndRatios(this.ratiosProvider, quote, bindException).compute();
        if (fr == null) {
            return null;
        }

        final Interval interval = getInterval(DEFAULT_PERIOD);
        final Map<String, Object> main = getHistoricModel(request, quote, interval, "main");
        if (!main.containsKey("main")) {
            return null;
        }

        model.put("isPercent", true);
        final TimeperiodDefinitionBuilder tsb
                = new TimeperiodDefinitionBuilder(interval.getStart().toLocalDate(),
                interval.getEnd().toLocalDate(), false);
        result.withTimeperiod(tsb);

        model.putAll(main);

        final RatioDataRecord fundRatios = (RatioDataRecord) fr.get(FndRatios.FUND_RATIOS_KEY);

        add(request, model, fundRatios, true);
        add(request, model, fundRatios, false);

        return result;
    }

    private void add(HttpServletRequest request, Map<String, Object> model, RatioDataRecord fundRatios, boolean best) {
        final Map<String, Object> result = new FndSectorSearch(this.ratiosProvider, fundRatios)
                .withNumResults(1)
                .withSortDescending(best)
                .withSortField(RatioFieldDescription.bviperformance1y.name()).compute();
        if (!result.containsKey(FndSectorSearch.KEY_ELEMENTS)) {
            return;
        }
        List<RatioDataResult> rdr = (List<RatioDataResult>) result.get(FndSectorSearch.KEY_ELEMENTS);
        if (rdr.isEmpty()) {
            return;
        }
        final RatioDataResult data = rdr.get(0);
        if (data.getQuoteid() == fundRatios.getQuoteId()) {
            return;
        }
        final DefaultSymbolCommand command = new DefaultSymbolCommand();
        command.setSymbol(data.getQuoteid() + ".qid");

        final Quote quote;
        try {
            quote = this.instrumentProvider.getQuote(command);
        } catch (UnknownSymbolException e) {
            return;
        }

        final Map<String, Object> hm =
                getHistoricModel(request, quote, getInterval(DEFAULT_PERIOD), best ? "bench" : "bench1");
        model.putAll(hm);
    }

    private Interval getInterval(final String p) {
        return DateUtil.getInterval(p);
    }

    private Map<String, Object> getHistoricModel(HttpServletRequest httpRequest, Quote quote, Interval interval, final String key) {
        final LocalDate from = interval.getStart().toLocalDate();
        final LocalDate to = interval.getEnd().toLocalDate();

        final List<HistoricTimeseries> hts;
        if (this.historicConfigurationMBean.isEodHistoryEnabled(quote)) {
            this.logger.info("<getHistoricModel> use eod history");
            final HistoricRequestImpl request = new HistoricRequestImpl(quote, from, to);
            HistoricTimeseriesUtils.addBviPerformance(request, from);
            hts = this.historicTimeseriesProviderEod.getTimeseries(request);
        }
        else {
            final HistoricTimeseriesRequest request = new HistoricTimeseriesRequest(quote, from, to);
            request.addBviPerformance(from);
            hts = this.historicProvider.getTimeseries(request);
        }

        final Map<String, Object> result = new HashMap<>();
        final HistoricTimeseries ts = hts.get(0);

        if (ts == null) {
            this.logger.warn("<getHistoricModel> no data for " + quote.getId() + ".qid");
            return result;
        }

        final QuoteNameStrategy qns
                = RequestContextHolder.getRequestContext().getQuoteNameStrategy();

        final TimeSeriesCollection main =
                new TimeSeriesCollection(key, qns.getName(quote), LineDrawStyle.STYLE_KEY);
        main.add("close", TimeSeriesFactory.daily("close", "", ts.getValues(), ts.getStartDay()));
        result.put(key, main);

        return result;
    }

}