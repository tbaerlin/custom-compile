/*
 * ImgChartKennzahlen.java
 *
 * Created on 28.08.2006 16:29:04
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.chart;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.joda.time.Interval;
import org.joda.time.LocalDate;
import org.springframework.validation.BindException;

import de.marketmaker.istar.chart.ChartModelAndView;
import de.marketmaker.istar.chart.data.TimeSeries;
import de.marketmaker.istar.chart.data.TimeSeriesCollection;
import de.marketmaker.istar.chart.data.TimeperiodDefinitionBuilder;
import de.marketmaker.istar.merger.provider.PortfolioRatiosRequest;
import de.marketmaker.istar.merger.provider.historic.HistoricTimeseries;
import de.marketmaker.istar.merger.provider.historic.HistoricTimeseriesProvider;
import de.marketmaker.istar.merger.web.easytrade.block.EasytradeInstrumentProvider;
import de.marketmaker.istar.merger.web.easytrade.block.MscPortfolioRatios;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class ImgPortfolioHistory extends AbstractImgChart {

    private EasytradeInstrumentProvider instrumentProvider;

    private HistoricTimeseriesProvider historicProvider;

    public ImgPortfolioHistory() {
        super(ImgPortfolioHistoryCommand.class);
    }

    public void setInstrumentProvider(EasytradeInstrumentProvider instrumentProvider) {
        this.instrumentProvider = instrumentProvider;
    }

    public void setHistoricProvider(HistoricTimeseriesProvider historicProvider) {
        this.historicProvider = historicProvider;
    }

    protected ChartModelAndView createChartModelAndView(HttpServletRequest request,
            HttpServletResponse response, Object object,
            BindException bindException) throws Exception {

        final ImgPortfolioHistoryCommand cmd = (ImgPortfolioHistoryCommand) object;

        Interval interval = null;
        if (cmd.getPeriod() != null) {
            interval = new Interval(cmd.getPeriod(), cmd.getDate().toDateTimeAtStartOfDay());
        }

        final ChartModelAndView result = createChartModelAndView(cmd);
        final Map<String, Object> model = result.getModel();

        final PortfolioRatiosRequest pr = MscPortfolioRatios.buildRequest(cmd.getDate(), cmd.getCurrency(), new String[]{cmd.getPeriod().toString()}, cmd.getPosition(), cmd.getSymbolStrategy(), cmd.getMarketStrategy(), this.instrumentProvider);
        final List<HistoricTimeseries> timeseries = this.historicProvider.getTimeseries(pr, interval.getStart().toLocalDate(), interval.getEnd().toLocalDate());

        final TimeSeriesCollection main = new TimeSeriesCollection("main", "Portfolio", "line");

        final TimeSeries closeTs = ChartUtil.toTimeSeries(timeseries.get(0), "close", "", ChartUtil.Aggregation.DAILY, ChartUtil.Consolidation.LAST);
        main.add("close", closeTs);

        model.put("main", main);
        model.put("isPercent", false);

        // timeperiodSpec's to is not inclusive, so add a day to show to
        final LocalDate afterTo = interval.getEnd().plusDays(1).toLocalDate();
        final TimeperiodDefinitionBuilder builder = new TimeperiodDefinitionBuilder(interval.getStart().toLocalDate(), afterTo, false);
        result.withTimeperiod(builder);

        return result;
    }
}
