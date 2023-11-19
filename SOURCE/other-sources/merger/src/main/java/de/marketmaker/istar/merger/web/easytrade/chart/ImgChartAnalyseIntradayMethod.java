/*
 * ImgChartAnalyseIntradayMethod.java
 *
 * Created on 17.12.2007 13:44:32
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.chart;

import de.marketmaker.istar.domain.data.AggregatedTickImpl;
import de.marketmaker.istar.domain.data.TickType;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.domainimpl.data.NullPriceRecord;
import de.marketmaker.istar.merger.provider.IntradayProvider;
import de.marketmaker.istar.merger.provider.IsoCurrencyConversionProviderImpl;
import de.marketmaker.istar.chart.component.datadrawstyle.LineDrawStyle;
import de.marketmaker.istar.chart.component.datadrawstyle.VolumeDrawStyle;
import de.marketmaker.istar.chart.data.TimeSeries;
import de.marketmaker.istar.chart.data.TimeSeriesCollection;
import de.marketmaker.istar.chart.data.TimeSeriesFactory;
import org.joda.time.DateTimeConstants;
import org.joda.time.Duration;
import org.joda.time.Interval;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class ImgChartAnalyseIntradayMethod extends ImgChartAnalyseMethod {
    private static final Duration AGG_15MIN = new Duration(DateTimeConstants.MILLIS_PER_MINUTE * 15);

    private static final Duration AGG_1H = new Duration(DateTimeConstants.MILLIS_PER_HOUR);

    private static final Duration AGG_1MIN = new Duration(DateTimeConstants.MILLIS_PER_MINUTE);

    private static final Duration AGG_5MIN = new Duration(DateTimeConstants.MILLIS_PER_MINUTE * 5);

    private static final String CLOSE_BEFORE = "closeBefore";

    private static final String ASK = "ask";

    private static final String BID = "bid";

    private Duration aggregation;

    private Interval interval;

    private ChartTimeperiodMethod ctm;

    private Map<String, BigDecimal> currencyFactors = null;

    /** Aggregated timeseries data for a single quote */
    private class TimeseriesData {
        private double[][] ohlcv;

        private long[] times;

        private final Quote quote;

        private final Interval[] intervals;

        private TickType type;

        private TimeseriesData(List<AggregatedTickImpl> ts, Quote quote, TickType type,
                Interval[] intervals) {
            this.quote = quote;
            this.type = type;
            this.intervals = intervals;

            this.ohlcv = new double[5][ts.size()];
            this.times = new long[ts.size()];
            for (int i = 0; i < ts.size(); i++) {
                final AggregatedTickImpl at = ts.get(i);
                this.ohlcv[0][i] = at.getOpen().doubleValue();
                this.ohlcv[1][i] = at.getHigh().doubleValue();
                this.ohlcv[2][i] = at.getLow().doubleValue();
                this.ohlcv[3][i] = at.getClose().doubleValue();
                this.ohlcv[4][i] = at.getVolume();
                this.times[i] = at.getInterval().getStartMillis();
            }
        }

        private void convertToPercent() {
            convertToPercent(getFirst());
        }

        private double getFirst() {
            return this.ohlcv[0][0]; // open of first aggregation
        }

        private void convertToPercent(double first) {
            for (int i = 0; i < 4; i++) {
                convertToPercent(i, first);
            }
        }

        private void convertToPercent(int i, double first) {
            for (int j = 0, n = this.ohlcv[i].length; j < n; j++) {
                this.ohlcv[i][j] = this.ohlcv[i][j] / first * 100d;
            }
        }

        private TimeSeries createSimpleTimeseries(final String name, final int i) {
            return TimeSeriesFactory.simple(name, "", this.ohlcv[i], this.times, aggregation.getMillis());
        }

        private TimeSeries createSimpleTimeseries(final String name, final int i, int from, int to,
                long[] fromToTimes) {
            return TimeSeriesFactory.simple(name, "",
                    Arrays.copyOfRange(this.ohlcv[i], from, to), fromToTimes, aggregation.getMillis());
        }

        private TimeSeriesCollection toCollection(String name, String styleName) {
            final TimeSeriesCollection result = createTimeSeriesCollection(name, styleName);
            result.add("open", createSimpleTimeseries("open", 0));
            result.add("high", createSimpleTimeseries("high", 1));
            result.add("low", createSimpleTimeseries("low", 2));
            result.add("close", createSimpleTimeseries("close", 3));
            return result;
        }

        private TimeSeriesCollection createTimeSeriesCollection(String name, String styleName) {
            return new TimeSeriesCollection(name, getName(), styleName);
        }

        private String getName() {
            final String name = qns.getName(this.quote);
            final boolean shortName = model.containsKey("main");
            switch (this.type) {
                case ASK:
                    return shortName ? "Brief" : (name + "(Brief)");
                case BID:
                    return shortName ? "Geld" : (name + "(Geld)");
                default:
                    return name;
            }
        }

        private int getTimeIndex(long aTime) {
            final int result = Arrays.binarySearch(this.times, aTime);
            return (result >= 0) ? result : -result - 1;
        }

        private TimeSeriesCollection[] toCollections(String name, String styleName) {
            if (this.intervals.length == 1) {
                return new TimeSeriesCollection[]{toCollection(name, styleName)};
            }

            final List<TimeSeriesCollection> list
                    = new ArrayList<>(this.intervals.length);

            for (Interval session : this.intervals) {
                final int from = getTimeIndex(session.getStartMillis());
                final int to = getTimeIndex(session.getEndMillis());
                if (from >= to) {
                    continue;
                }

                // same for all, save some serialization bytes by reusing it
                final long[] fromToTimes = getTimes(from, to);

                final TimeSeriesCollection result = createTimeSeriesCollection(name, styleName);
                result.add("open", createSimpleTimeseries("open", 0, from, to, fromToTimes));
                result.add("high", createSimpleTimeseries("high", 1, from, to, fromToTimes));
                result.add("low", createSimpleTimeseries("low", 2, from, to, fromToTimes));
                result.add("close", createSimpleTimeseries("close", 3, from, to, fromToTimes));

                list.add(result);
            }


            return list.toArray(new TimeSeriesCollection[list.size()]);
        }

        private TimeSeriesCollection[] toVolume() {
            if (this.intervals.length == 1) {
                return new TimeSeriesCollection[]{createVolumeTimeSeriesCollection()
                        .add("volume", createSimpleTimeseries("volume", 4))};
            }

            final List<TimeSeriesCollection> list
                    = new ArrayList<>(this.intervals.length);

            for (Interval session : this.intervals) {
                final int from = getTimeIndex(session.getStartMillis());
                final int to = getTimeIndex(session.getEndMillis());
                if (from < to) {
                    list.add(createVolumeTimeSeriesCollection().add("volume",
                            createSimpleTimeseries("volume", 4, from, to, getTimes(from, to))));
                }
            }

            return list.toArray(new TimeSeriesCollection[list.size()]);
        }

        private long[] getTimes(int from, int to) {
            return Arrays.copyOfRange(this.times, from, to);
        }

        private TimeSeriesCollection createVolumeTimeSeriesCollection() {
            return new TimeSeriesCollection("volume", "Volumen", VolumeDrawStyle.STYLE_KEY);
        }
    }

    public ImgChartAnalyseIntradayMethod(ImgChartAnalyse controller,
            BindException errors, ImgChartAnalyseCommand cmd) {
        super(controller, errors, cmd);
    }

    protected void fillModel() {
        this.model.put("isIntraday", true);
        this.model.put("isPercent", isPercent());

        this.ctm = new ChartTimeperiodMethod(this.controller.getTradingCalendarProvider(), this.quote,
                this.benchmarks, cmd.getPeriod().getDays());

        this.interval = this.ctm.getInterval();
        this.result.withTimeperiod(this.ctm.getTimeperiodDefinition());
        computeAggregation();

        addIntradayData();
        if (this.intradayPrice == null || this.intradayPrice == NullPriceRecord.INSTANCE) {
            return;
        }

        if (!isPercent()) {
            addCloseBefore();
        }

        final Interval[] intervals = this.ctm.getQuoteIntervals();
        this.model.put("main.sessions", intervals);

        final TimeseriesData mainData = getMainTimeSeries(TickType.TRADE);
        final TimeseriesData askData = getMainTimeSeries(TickType.ASK);
        final TimeseriesData bidData = getMainTimeSeries(TickType.BID);

        if (isPercent()) {
            convertToPercent(mainData, askData, bidData);
        }

        if (mainData != null) {
            addTimeSeries(mainData, "main", getStyleKey());
            if (this.cmd.isVolume() && !this.isIgnoreVolumeType) {
                this.model.put("volume", mainData.toVolume());
            }
        }
        if (askData != null) {
            addTimeSeries(askData, ASK, LineDrawStyle.STYLE_KEY);
        }
        if (bidData != null) {
            addTimeSeries(bidData, BID, LineDrawStyle.STYLE_KEY);
        }

        if (isValidModel()) {
            addBenchmarks();
        }
    }

    private void convertToPercent(TimeseriesData mainData,
            TimeseriesData askData, TimeseriesData bidData) {
        final double first = getFirst(mainData, askData, bidData);
        convertToPercent(mainData, first);
        convertToPercent(askData, first);
        convertToPercent(bidData, first);
    }

    private void convertToPercent(TimeseriesData data, double first) {
        if (data != null) {
            data.convertToPercent(first);
        }
    }

    private double getFirst(TimeseriesData mainData,
            TimeseriesData askData, TimeseriesData bidData) {
        if (mainData != null) {
            return mainData.getFirst();
        }
        if (askData != null && bidData != null) {
            return (askData.getFirst() + bidData.getFirst()) / 2;
        }
        if (askData != null) {
            return askData.getFirst();
        }
        if (bidData != null) {
            return bidData.getFirst();
        }
        return 0;
    }

    @Override
    protected boolean isWithOpenHighLow(String aStyleKey) {
        // since creating chart data is based on aggregated ticks, it is always
        // necessary to include ohl as well, as people expect to see all lows and highs
        // in intraday charts
        return true;
    }

    private void addTimeSeries(final Quote q, final String name, String styleName,
            Interval[] intervals, final TickType type) {
        final TimeseriesData data = getTimeSeries(q, type, intervals);
        if (data == null) {
            return;
        }

        if (isPercent()) {
            data.convertToPercent();
        }

        addTimeSeries(data, name, styleName);
    }

    private void addTimeSeries(TimeseriesData data, String name, String styleName) {
        final TimeSeriesCollection[] tscs = data.toCollections(name, styleName);
        this.model.put(name, (tscs.length == 1) ? tscs[0] : tscs);

        addLabel(data.quote, name);
        addColor(data.quote, name);
    }

    private TimeseriesData getMainTimeSeries(final TickType type) {
        if (type == TickType.TRADE && this.cmd.isTrade()) {
            return getTimeSeries(this.quote, TickType.TRADE, this.ctm.getQuoteIntervals());
        }
        if (type == TickType.ASK && this.cmd.isAsk()) {
            return getTimeSeries(this.quote, TickType.ASK, this.ctm.getQuoteIntervals());
        }
        if (type == TickType.BID && this.cmd.isBid()) {
            return getTimeSeries(this.quote, TickType.BID, this.ctm.getQuoteIntervals());
        }
        return null;
    }

    private TimeseriesData getTimeSeries(final Quote q, final TickType type, Interval[] intervals) {
        final List<AggregatedTickImpl> ticks = getAggregatedTicks(q, type);
        if (ticks == null || ticks.isEmpty() || intervals.length == 0) {
            return null;
        }

        if (!adaptForCurrency(q, ticks)) {
            return null;
        }

        return new TimeseriesData(ticks, q, type, intervals);
    }

    private boolean adaptForCurrency(Quote q, List<AggregatedTickImpl> ticks) {
        final String source = q.getCurrency().getSymbolIso();
        if (!isWithCurrencyConversion(source)) {
            return true;
        }

        final BigDecimal factor = getCurrencyFactor(source, this.cmd.getCurrency());
        if (factor == null) {
            return false;
        }

        if (factor.equals(BigDecimal.ONE)) {
            return true;
        }

        for (int i = 0; i < ticks.size(); i++) {
            AggregatedTickImpl aggregatedTick = ticks.get(i);
            ticks.set(i, aggregatedTick.multiply(factor));
        }

        return true;
    }

    private boolean isWithCurrencyConversion(String source) {
        return StringUtils.hasText(this.cmd.getCurrency())
                && StringUtils.hasText(source)
                && !this.cmd.getCurrency().equals(source)
                && !source.equals("XXP");
    }

    private BigDecimal getCurrencyFactor(String source, String target) {
        final String key = source + "-" + target;
        if (this.currencyFactors == null) {
            this.currencyFactors = new HashMap<>();
        }
        if (this.currencyFactors.containsKey(key)) {
            return this.currencyFactors.get(key);
        }
        final BigDecimal result = getConversion(source, target);
        this.currencyFactors.put(key, result);
        return result;
    }

    private BigDecimal getConversion(String source, String target) {
        try {
            final IsoCurrencyConversionProviderImpl.ConversionResult conversionResult
                    = this.controller.getIsoCurrencyConversionProvider().getConversion(source, target);
            return (conversionResult != null) ? conversionResult.getFactor() : null;
        } catch (Exception e) {
            this.logger.warn("<getConversion> failed to convert from " + source + " to " + target);
            return null;
        }
    }

    protected boolean isValidModel() {
        return super.isValidModel()
                || this.model.containsKey(CLOSE_BEFORE)
                || this.model.containsKey(LAST_PRICE)
                || this.model.containsKey(ASK)
                || this.model.containsKey(BID);
    }

    private void addBenchmarks() {
        int n = 0;
        for (int i = 0; i < this.benchmarks.length; i++) {
            final Quote bQuote = this.benchmarks[i];
            if (bQuote == null) {
                continue;
            }
            final String id = (n == 0) ? "bench" : ("bench" + n);
            addTimeSeries(bQuote, id, LineDrawStyle.STYLE_KEY,
                    this.ctm.getBenchmarkIntervals(i), TickType.TRADE);
            n++;
        }
    }

    private void addCloseBefore() {
        if (this.cmd.getPeriod().getDays() > 1) {
            return;
        }
        final BigDecimal closeBefore = getCloseBefore();
        if (closeBefore == null) {
            return;
        }

        final String source = this.quote.getCurrency().getSymbolIso();
        if (isWithCurrencyConversion(source)) {
            final BigDecimal factor = getCurrencyFactor(source, this.cmd.getCurrency());
            if (factor != null) {
                addCloseBefore(closeBefore.multiply(factor));
            }
        }
        else {
            addCloseBefore(closeBefore);
        }
    }

    private BigDecimal getCloseBefore() {
        return this.intradayPrice.getCloseBefore(this.interval.getStart().toLocalDate());
    }

    private void addCloseBefore(BigDecimal closeBefore) {
        this.model.put(CLOSE_BEFORE, closeBefore);
    }

    private void computeAggregation() {
        final int numDays = cmd.getPeriod().getDays();
        @SuppressWarnings({"StringEquality"})
        final boolean isLine = (LineDrawStyle.STYLE_KEY == getStyleKey());
        if (numDays == 1) {
            this.aggregation = isLine ? AGG_1MIN : AGG_5MIN;
        }
        else if (numDays <= 5) {
            this.aggregation = isLine ? AGG_5MIN : AGG_15MIN;
        }
        else {
            this.aggregation = isLine ? AGG_15MIN : AGG_1H;
        }
    }

    private List<AggregatedTickImpl> getAggregatedTicks(final Quote q, final TickType type) {
        final IntradayProvider ip = this.controller.getIntradayProvider();
        return ip.getAggregatedTrades(q, this.interval.getStart(), this.interval.getEnd(),
                this.aggregation, type);
    }
}
