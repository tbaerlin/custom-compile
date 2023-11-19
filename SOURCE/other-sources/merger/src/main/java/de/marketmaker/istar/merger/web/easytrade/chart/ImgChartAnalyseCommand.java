/*
 * ImgChartAnalyseCommand.java
 *
 * Created on 28.08.2006 16:30:39
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.chart;

import org.joda.time.Period;
import org.springframework.util.StringUtils;

import de.marketmaker.istar.common.dmxmldocu.MmInternal;
import de.marketmaker.istar.common.validator.NotNull;
import de.marketmaker.istar.common.validator.Pattern;
import de.marketmaker.istar.common.validator.Range;
import de.marketmaker.istar.common.validator.RestrictedSet;
import de.marketmaker.istar.common.validator.Size;
import de.marketmaker.istar.chart.component.datadrawstyle.LineDrawStyle;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class ImgChartAnalyseCommand extends BaseImgSymbolCommand {
    /**
     * Used internally to determine whether parameters can be omitted in
     * {@link #appendParameters(StringBuilder)} (generally, they can be omitted if the value
     * is the same as in this object).
     */
    private final static ImgChartAnalyseCommand DEFAULT = new ImgChartAnalyseCommand();

    private String mainField;

    private String aggregation;

    private boolean ask = false;

    private boolean barrier = false;

    private String[] benchmark;

    private String[] benchmarkColor;

    private String[] benchmarkLabel;

    private boolean bid = false;

    // we need 3-way logic, so use Boolean
    private Boolean blendCorporateActions = null;

    // we need 3-way logic, so use Boolean
    private Boolean blendDividends = null;

    private boolean bonus = false;

    private boolean bviPerformanceForFunds = false;

    private boolean bw;

    private boolean cap = false;

    private String currency;

    private String derivative;

    private boolean dividends;

    private boolean dividendsChart;

    private String from;

    private String[] gd;

    private String gd1;

    private String gd2;

    private String gd3;

    private String[] gdColor;

    private String[] ignoreVolumeTypes;

    private String[] indicator;

    private String[] indicatorColor;

    private boolean kursziele;

    private String label;

    private Boolean legend;

    private boolean logScales;

    private boolean normalizeBenchmarks = false;

    // we need 3-way logic, so use Boolean
    private Boolean percent;

    private String signale;

    private boolean splits;

    private boolean hilo;

    private boolean strokeMarkers = false;

    private String to;

    // whether trade line should be included for intraday charts
    private boolean trade = true;

    private String type = LineDrawStyle.STYLE_KEY;

    private boolean volume;

    private boolean withInterval = true;

    // whether the returned block should include current price data
    private boolean withPrice = true;

    private int minLineWidth;

    private boolean adjustFrom = false;

    public ImgChartAnalyseCommand() {
        super(300, 200, Period.months(3));
    }

    public StringBuilder appendParameters(StringBuilder sb) {
        super.appendParameters(sb);
        if (!DEFAULT.type.equals(this.type)) {
            sb.append("&type=").append(this.type);
        }
        appendParameter(sb, this.mainField, "mainField");
        appendParameters(sb, this.benchmark, "benchmark");
        appendParameters(sb, this.benchmarkLabel, "benchmarkLabel");
        appendParameters(sb, this.benchmarkColor, "benchmarkColor");
        appendParameter(sb, this.label, "label");
        appendParameter(sb, this.from, "from");
        appendParameter(sb, this.to, "to");
        appendParameter(sb, this.currency, "currency");
        appendParameter(sb, this.signale, "signale");
        appendParameter(sb, this.aggregation, "aggregation");
        appendParameter(sb, this.label, "label");
        appendParameters(sb, this.ignoreVolumeTypes, "ignoreVolumeTypes");

        if (this.gd == null) {
            appendParameter(sb, this.gd1, "gd1");
            appendParameter(sb, this.gd2, "gd2");
            appendParameter(sb, this.gd3, "gd3");
        } else {
            appendParameters(sb, this.gd, "gd");
            appendParameters(sb, this.gdColor, "gdColor");
        }

        if (this.indicator != null) {
            appendParameters(sb, this.indicator, "indicator");
            appendParameters(sb, this.indicatorColor, "indicatorColor");
        }

        if (this.percent != null) {
            appendParameter(sb, this.percent.toString(), "percent");
        }

        appendParameter(sb, this.volume, DEFAULT.volume, "volume");
        appendParameter(sb, this.splits, DEFAULT.splits, "splits");
        appendParameter(sb, this.hilo, DEFAULT.hilo, "hilo");
        appendParameter(sb, this.dividends, DEFAULT.dividends, "dividends");
        appendParameter(sb, this.dividendsChart, DEFAULT.dividendsChart, "dividendsChart");
        appendParameter(sb, this.kursziele, DEFAULT.kursziele, "kursziele");
        appendParameter(sb, this.withPrice, DEFAULT.withPrice, "withPrice");
        appendParameter(sb, this.withInterval, DEFAULT.withInterval, "withInterval");
        appendParameter(sb, this.bw, DEFAULT.bw, "bw");
        appendParameter(sb, this.logScales, DEFAULT.logScales, "logScales");
        appendParameter(sb, this.normalizeBenchmarks, DEFAULT.normalizeBenchmarks, "normalizeBenchmarks");
        appendParameter(sb, this.ask, DEFAULT.ask, "ask");
        appendParameter(sb, this.bid, DEFAULT.bid, "bid");
        appendParameter(sb, this.trade, DEFAULT.trade, "trade");
        appendParameter(sb, this.strokeMarkers, DEFAULT.strokeMarkers, "strokeMarkers");

        if (this.legend != null) {
            appendParameter(sb, this.legend.toString(), "legend");
        }
        if (this.derivative != null) {
            appendParameter(sb, this.derivative, "derivative");
            appendParameter(sb, this.barrier, DEFAULT.barrier, "barrier");
            appendParameter(sb, this.bonus, DEFAULT.bonus, "bonus");
            appendParameter(sb, this.cap, DEFAULT.cap, "cap");
        }
        appendParameter(sb, this.bviPerformanceForFunds, DEFAULT.bviPerformanceForFunds, "bviPerformanceForFunds");
        if (this.blendCorporateActions != null) {
            appendParameter(sb, this.blendCorporateActions.toString(), "blendCorporateActions");
        }
        if (this.blendDividends != null) {
            appendParameter(sb, this.blendDividends.toString(), "blendDividends");
        }

        return sb;
    }

    /**
     * @return ID of the vwd field that should be used to retrieve the values rendered as the main
     * curve. Defaults to "close" if not specified.
     */
    public String getMainField() {
        return mainField;
    }

    /**
     * @return How to aggregate timeseries values in ohlc charts, default is dynamically calculated
     * based on the chart's interval.
     */
    @RestrictedSet("daily,weekly,monthly")
    public String getAggregation() {
        return aggregation;
    }

    /**
     * @return symbols of benchmark instruments to be added to the chart
     * @sample 846900.ETR
     */
    @Size(min = 0, max = 25)
    public String[] getBenchmark() {
        return this.benchmark;
    }

    /**
     * @return Color used to render the line of the corresponding benchmark (format hex rrggbb)
     * @sample 88ff00
     */
    public String[] getBenchmarkColor() {
        return this.benchmarkColor;
    }

    public String getBenchmarkColor(int i) {
        return getNth(this.benchmarkColor, i);
    }

    /**
     * @return label to be used for the corresponding benchmark
     */
    public String[] getBenchmarkLabel() {
        return benchmarkLabel;
    }

    public String getBenchmarkLabel(int i) {
        return getNth(this.benchmarkLabel, i);
    }

    /**
     * @return whether corporate actions (e.g., splits) will be blended into the timeserieses.
     */
    public Boolean getBlendCorporateActions() {
        return blendCorporateActions;
    }

    public void setBlendCorporateActions(Boolean blendCorporateActions) {
        this.blendCorporateActions = blendCorporateActions;
    }

    /**
     * @return whether dividends will be blended into the timeserieses.
     */
    public Boolean getBlendDividends() {
        return blendDividends;
    }

    public void setBlendDividends(Boolean blendDividends) {
        this.blendDividends = blendDividends;
    }

    /**
     * @return convert all timeserieses into this currency (except for those that are quoted
     * in points (e.g., DAX) or percent). Specify the currency's ISO code.
     * @sample USD
     */
    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    /**
     * symbol of a certificate that is a derivative of the chart's main instrument, so that the
     * certificate's cap, bonus, and barrier can be added to the chart if requested
     */
    public String getDerivative() {
        return derivative;
    }

    public void setDerivative(String derivative) {
        this.derivative = derivative;
    }

    /**
     * Request chart starting at this day. In addition to values in the format <tt>yyyy-MM-dd</tt>,
     * it is also possible to use "today" and "start" (i.e., start with the date of the first
     * available price for the chart's main instrument)
     */
    public String getFrom() {
        return this.from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    /**
     * Request up to 3 moving averages to be added to the chart, the values are the number of
     * days for each such ma.
     */
    @Size(min = 0, max = 3)
    @Range(min = 1, max = 366)
    public String[] getGd() {
        return gd;
    }

    public String getGd(int i) {
        return getNth(this.gd, i);
    }

    @MmInternal
    @Range(min = 0, max = 366)
    public String getGd1() {
        return this.gd1;
    }

    @MmInternal
    @Range(min = 1, max = 366)
    public String getGd2() {
        return this.gd2;
    }

    @MmInternal
    @Range(min = 1, max = 366)
    public String getGd3() {
        return this.gd3;
    }

    /**
     * Specify a color for the corresponding moving average line.
     */
    public String[] getGdColor() {
        return gdColor;
    }

    public String getGdColor(int i) {
        return getNth(this.gdColor, i);
    }

    @MmInternal
    public String[] getIgnoreVolumeTypes() {
        return ignoreVolumeTypes;
    }

    /**
     * @return which indicators should be added
     */
    @Size(min = 0, max = 4)
    @RestrictedSet("momentum,roc,rsi,macd,ss,fs,vma,bb,obos,vola")
    public String[] getIndicator() {
        return this.indicator;
    }

    /**
     * @return specify color for the corresponding indicator
     */
    public String[] getIndicatorColor() {
        return indicatorColor;
    }

    public String getIndicatorColor(int i) {
        return getNth(this.indicatorColor, i);
    }

    /**
     * @return specify label for the chart's main line
     */
    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    /**
     * @return whether a legend should be added to the chart. Useful if a chart should be
     * displayed w/o a legend in a browser and printed with legend. The chart has to be configured
     * to support this option, if it is not, this option does nothing.
     */
    public Boolean getLegend() {
        return this.legend;
    }

    public void setLegend(Boolean legend) {
        this.legend = legend;
    }

    @MmInternal
    @RestrictedSet("cons,spec")
    public String getSignale() {
        return signale;
    }

    /**
     * @return the end date of the chart's interval, format <tt>yyyy-MM-dd</tt>, default is "today"
     */
    public String getTo() {
        return this.to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    /**
     * The type of the chart's main line, used for two purposes: First, to determine which timeseries
     * objects need to be provided in the chart's model (e.g., close for a line chart and ohlc for
     * a candle chart). Second, to specify the renderer used to render the main chart line. Usually,
     * the renderer's name is the same as the type (e.g., a "line" renderer renders the close data
     * provided with the "line" type). Sometimes, it is required to request a different renderer,
     * and then the type should be specified as <tt>type.name</tt> (e.g., "line.mountain" to
     * render the close line with a LineDrawStyleRenderer with the style key "mountain"). The <tt>.name</tt>
     * part is optional and can be left out if the name equals the type ("line.line" == "line")
     *
     * @return the type of the chart's main line.
     */
    @NotNull
    @Pattern(regex = "(line|bar|ohlc|candle|hilo)(\\.\\S+)?")
    public String getType() {
        return this.type;
    }

    /**
     * @return whether intraday/interday charts should include a line for ask prices
     */
    public boolean isAsk() {
        return this.ask;
    }

    public void setAsk(boolean ask) {
        this.ask = ask;
    }

    /**
     * @return whether a certificate's barrier should included, certificate is specified with
     * the <tt>derivative</tt> parameter
     */
    public boolean isBarrier() {
        return this.barrier;
    }

    public void setBarrier(boolean barrier) {
        this.barrier = barrier;
    }

    /**
     * @return whether intraday/interday charts should include a line for ask prices
     */
    public boolean isBid() {
        return this.bid;
    }

    public void setBid(boolean bid) {
        this.bid = bid;
    }

    /**
     * @return whether a certificate's bonus level should included, certificate is specified with
     * the <tt>derivative</tt> parameter
     */
    public boolean isBonus() {
        return this.bonus;
    }

    public void setBonus(boolean bonus) {
        this.bonus = bonus;
    }

    /**
     * @return if a fund's timeseries is rendered, use the performance timeseries computed
     * by the BVI method
     */
    public boolean isBviPerformanceForFunds() {
        return this.bviPerformanceForFunds;
    }

    public void setBviPerformanceForFunds(boolean bviPerformanceForFunds) {
        this.bviPerformanceForFunds = bviPerformanceForFunds;
    }

    /**
     * @return if black-and-white style is requested. Only available for charts that support it.
     */
    public boolean isBw() {
        return this.bw;
    }

    public void setBw(boolean bw) {
        this.bw = bw;
    }

    /**
     * @return whether a certificate's cap should included, certificate is specified with
     * the <tt>derivative</tt> parameter
     */
    public boolean isCap() {
        return this.cap;
    }

    public void setCap(boolean cap) {
        this.cap = cap;
    }

    /**
     * @return whether dividends should be rendered
     */
    public boolean isDividends() {
        return dividends;
    }

    public void setDividends(boolean dividends) {
        this.dividends = dividends;
    }

    /**
     * @return whether chart should display dividends data instead of price data
     */
    public boolean isDividendsChart() {
        return dividendsChart;
    }

    public void setDividendsChart(boolean dividendsChart) {
        this.dividendsChart = dividendsChart;
    }

    /**
     * whether minimum and maximum target price based on analyses should be included
     */
    public boolean isKursziele() {
        return this.kursziele;
    }

    public void setKursziele(boolean kursziele) {
        this.kursziele = kursziele;
    }

    /**
     * @return request logarithmic scale for y-axis.
     */
    public boolean isLogScales() {
        return this.logScales;
    }

    public void setLogScales(boolean logScales) {
        this.logScales = logScales;
    }

    /**
     * If true, a benchmark's timeseries will be normalized to match the main timeseries (it appears
     * as if the benchmark would start with the same value as main). Useful if the chart shows
     * the "real" values of main on the left axis and the percent values on the right axis, as the
     * percent values can now also be used for the benchmarks. This parameter is only evaluated
     * if {@link #isPercent()} returns {@link Boolean#FALSE}, as otherwise all timeserieses
     * will be in percent and mutually comparable anyway.
     *
     * @return true if benchmarks should be normalized; default is false
     */
    public boolean isNormalizeBenchmarks() {
        return this.normalizeBenchmarks;
    }

    public void setNormalizeBenchmarks(boolean normalizeBenchmarks) {
        this.normalizeBenchmarks = normalizeBenchmarks;
    }

    /**
     * @return whether all timeseries should be converted to percent values, starting at 100%.
     * If unspecified, percent will be used if any benchmarks are defined.
     */
    public Boolean isPercent() {
        return this.percent;
    }

    public void setPercent(Boolean percent) {
        this.percent = percent;
    }

    /**
     * @return whether stock splits should be rendered
     */
    public boolean isSplits() {
        return splits;
    }

    public void setSplits(boolean splits) {
        this.splits = splits;
    }

    @MmInternal
    public boolean isHilo() {
        return hilo;
    }

    public void setHilo(boolean hilo) {
        this.hilo = hilo;
    }

    @MmInternal
    public boolean isStrokeMarkers() {
        return strokeMarkers;
    }

    public void setStrokeMarkers(boolean strokeMarkers) {
        this.strokeMarkers = strokeMarkers;
    }

    /**
     * @return for intra/interday charts: if either or both <tt>ask</tt> and <tt>bid</tt> is true,
     * <tt>trade</tt> can be set to false to not render trades.
     */
    public boolean isTrade() {
        // R-48460 HACK: return at least one line
        return this.trade || (!this.bid && !this.ask);
    }

    public void setTrade(boolean trade) {
        this.trade = trade;
    }

    /**
     * @return whether trade volume should be rendered
     */
    public boolean isVolume() {
        return this.volume;
    }

    public void setVolume(boolean volume) {
        this.volume = volume;
    }

    @MmInternal
    public boolean isWithInterval() {
        return withInterval;
    }

    public void setWithInterval(boolean withInterval) {
        this.withInterval = withInterval;
    }

    @MmInternal
    public boolean isWithPrice() {
        return withPrice;
    }

    public void setWithPrice(boolean withPrice) {
        this.withPrice = withPrice;
    }

    public void setMainField(String mainField) {
        if (StringUtils.hasText(mainField)) {
            this.mainField = mainField;
        }
    }

    public void setAggregation(String aggregation) {
        if (StringUtils.hasText(aggregation)) {
            this.aggregation = aggregation;
        }
    }

    public void setBenchmark(String[] benchmark) {
        this.benchmark = separate(benchmark);
    }

    public void setBenchmarkColor(String[] benchmarkColor) {
        this.benchmarkColor = separate(benchmarkColor);
    }

    public void setBenchmarkLabel(String[] benchmarkLabel) {
        this.benchmarkLabel = separate(benchmarkLabel);
    }

    public void setGd(String[] gd) {
        this.gd = separate(gd);
    }

    public void setGd1(String gd1) {
        if (StringUtils.hasText(gd1)) {
            this.gd1 = gd1;
        }
    }

    public void setGd2(String gd2) {
        if (StringUtils.hasText(gd2)) {
            this.gd2 = gd2;
        }
    }

    public void setGd3(String gd3) {
        if (StringUtils.hasText(gd3)) {
            this.gd3 = gd3;
        }
    }

    public void setGdColor(String[] gdColor) {
        this.gdColor = separate(gdColor);
    }

    public void setIgnoreVolumeTypes(String[] ignoreVolumeTypes) {
        if (ignoreVolumeTypes != null && ignoreVolumeTypes.length == 1) {
            this.ignoreVolumeTypes = ignoreVolumeTypes[0].split(",");
        } else {
            this.ignoreVolumeTypes = ignoreVolumeTypes;
        }
    }

    public void setIndicator(String[] indicator) {
        this.indicator = separate(indicator);
    }

    public void setIndicatorColor(String[] indicatorColor) {
        this.indicatorColor = separate(indicatorColor);
    }

    public void setSignale(String signale) {
        if (StringUtils.hasText(signale) && !"false".equals(signale)) {
            this.signale = signale;
        }
    }

    public void setType(String type) {
        if (StringUtils.hasText(type)) {
            this.type = type;
        }
    }

    private String getNth(String[] s, int n) {
        return (s != null && n < s.length) ? s[n] : null;
    }

    /**
     * Can be used to enforce a minimum width for chart lines, useful for color printing, when
     * thicker lines are needed to be able to distinguish the colors better. Should be set in
     * zone properties for specific controller.
     */
    @MmInternal
    public int getMinLineWidth() {
        return minLineWidth;
    }

    public void setMinLineWidth(int minLineWidth) {
        this.minLineWidth = minLineWidth;
    }

    @MmInternal
    public boolean isAdjustFrom() {
        return adjustFrom;
    }

    public void setAdjustFrom(boolean adjustFrom) {
        this.adjustFrom = adjustFrom;
    }
}
