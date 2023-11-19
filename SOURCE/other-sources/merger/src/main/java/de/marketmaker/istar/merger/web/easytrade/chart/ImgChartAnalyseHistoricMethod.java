/*
 * ImgChartAnalyseHistoricMethod.java
 *
 * Created on 17.12.2007 13:47:24
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.chart;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import de.marketmaker.istar.merger.web.easytrade.block.HistoricConfigurationMBean;
import org.apache.commons.lang3.ArrayUtils;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.Interval;
import org.joda.time.LocalDate;
import org.joda.time.Period;
import org.joda.time.PeriodType;
import org.springframework.beans.propertyeditors.LocaleEditor;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindException;

import de.marketmaker.istar.chart.component.datadrawstyle.LineDrawStyle;
import de.marketmaker.istar.chart.component.datadrawstyle.VolumeDrawStyle;
import de.marketmaker.istar.chart.data.EventItem;
import de.marketmaker.istar.chart.data.TimeSeries;
import de.marketmaker.istar.chart.data.TimeSeriesCollection;
import de.marketmaker.istar.chart.data.TimeSeriesFactory;
import de.marketmaker.istar.chart.data.TimeperiodDefinitionBuilder;
import de.marketmaker.istar.common.util.DateUtil;
import de.marketmaker.istar.common.util.RatioFormatter;
import de.marketmaker.istar.domain.data.CorporateAction;
import de.marketmaker.istar.domain.data.Price;
import de.marketmaker.istar.domain.data.PriceRecord;
import de.marketmaker.istar.domain.data.PriceRecordFund;
import de.marketmaker.istar.domain.data.TradingPhase;
import de.marketmaker.istar.domain.instrument.InstrumentTypeEnum;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.feed.vwd.VwdFieldDescription;
import de.marketmaker.istar.merger.Constants;
import de.marketmaker.istar.merger.provider.HistoricRatiosProvider;
import de.marketmaker.istar.merger.provider.SymbolQuote;
import de.marketmaker.istar.merger.provider.TradingPhaseRequest;
import de.marketmaker.istar.merger.provider.TradingPhaseResponse;
import de.marketmaker.istar.merger.provider.historic.HistoricRequest;
import de.marketmaker.istar.merger.provider.historic.HistoricRequestImpl;
import de.marketmaker.istar.merger.provider.historic.HistoricTerm;
import de.marketmaker.istar.merger.provider.historic.HistoricTimeseries;
import de.marketmaker.istar.merger.provider.historic.HistoricTimeseriesRequest;
import de.marketmaker.istar.merger.provider.historic.HistoricTimeseriesUtils;
import de.marketmaker.istar.merger.web.easytrade.block.EasytradeCommandController;

import static de.marketmaker.istar.merger.provider.historic.HistoricTimeseriesUtils.getLastDayThatCanBeRequested;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class ImgChartAnalyseHistoricMethod extends ImgChartAnalyseMethod {

    private HistoricConfigurationMBean historicConfigurationMBean;

    private LocalDate atDay;

    private double atValue;

    private final Interval interval;

    private final LocalDate from;

    private final LocalDate to;

    private final LocalDate historicTo;

    private final ChartUtil.Aggregation aggregation;

    private final BigDecimal factor;

    private HistoricTimeseries close;

    private BigDecimal benchmarkNormFactor = null;

    protected PriceRecord[] benchmarkPrices;

    private final Map<Quote, HistoricTimeseries> closes
            = new IdentityHashMap<>();

    public ImgChartAnalyseHistoricMethod(ImgChartAnalyse controller,
            BindException errors, ImgChartAnalyseCommand cmd, HistoricConfigurationMBean historicConfigurationMBean) {
        super(controller, errors, cmd);

        initIntradayData();

        this.historicConfigurationMBean = historicConfigurationMBean;

        this.interval = getInterval();
        this.from = interval.getStart().toLocalDate();
        this.to = interval.getEnd().toLocalDate();
        this.historicTo = DateUtil.min(this.to, getLastDayThatCanBeRequested(this.intradayPrice));
        this.aggregation = getAggregation();
        this.factor = BigDecimal.ONE;
    }

    @Override
    protected void identifyBenchmarks() {
        super.identifyBenchmarks();
        if (hasBenchmarks()) {
            final List<PriceRecord> tmp = getPrices(Arrays.asList(this.benchmarks));
            this.benchmarkPrices = tmp.toArray(new PriceRecord[tmp.size()]);
        }
    }

    protected void fillModel() {
        computeAt();
        this.model.put("isPercent", this.atDay != null && isPercent());

        final HistoricRequest request = createRequest();
        if (this.cmd.isWithPrice()) {
            addIntradayData();
        }
        final Iterator<HistoricTimeseries> it = getTimeseries(request).iterator();

        this.close = it.hasNext() ? it.next() : null;
        // removed for R-85653, re-enable and leave a comment if you know why we need this
        // replaceInitialZerosWithNaN(this.close);
        this.atValue = computeAtClose();

        if (isUndefined(this.close)) {
            logger.warn("<fillModel> no data for " + quote.getId() + ".qid");
            this.errors.reject("historic.unavailable", "no historic data");
            return;
        }

        addMain(it);
        addVolume(it);
        addGDs(it);
        addIndicator(it);

        addSignals();

        if (this.benchmarks.length > 0) {
            if (this.shares.size() <= 1) {
                addBenchmarks();
            }
            else {
                addCompositeBenchmark();
            }
        }

        addCorporateActions();

        addTimeperiodSpec();

        if (this.cmd.getMainField() != null) {
            TimeSeriesCollection main = (TimeSeriesCollection) model.get("main");
            main.add("close", toTimeSeries(it.next(), "close", ChartUtil.Consolidation.LAST));
        }
        else if (this.cmd.isDividendsChart()) {
            if (model.get("main") != null && model.get("main") instanceof TimeSeriesCollection) {
                TimeSeriesCollection main = (TimeSeriesCollection) model.get("main");
                main.add("close", toTimeSeries(getDividendsTimeseries(), "close", ChartUtil.Consolidation.LAST));
            }
        }
    }

    private double computeAtClose() {
        if (this.atDay == null) {
            return Double.NaN;
        }
        return this.close.getValueAtOrBeforeOrAfter(this.atDay);
    }

    // this replaces all zeros if the timeseries consists of only zeros and NaNs
    private void replaceInitialZerosWithNaN(HistoricTimeseries ht) {
        if (ht == null) {
            return;
        }
        final double[] values = ht.getValues();
        for (int i = 0, n = values.length; i < n; i++) {
            if (Double.isNaN(values[i])) {
                continue;
            }
            if (values[i] == 0d) {
                values[i] = Double.NaN;
                continue;
            }
            break;
        }
    }

    private void addCorporateActions() {
        if (this.close == null || (!this.cmd.isSplits() && !this.cmd.isDividends())) {
            return;
        }
        final HistoricRatiosProvider rp = this.controller.getHistoricRatiosProvider();
        if (rp == null) {
            return;
        }
        final List<CorporateAction> actions = rp.getCorporateActions(SymbolQuote.create(this.quote),
                this.interval, false);

        if (this.cmd.isSplits()) {
            addEventItems("splits", getSplits(actions));
        }
        if (this.cmd.isDividends()) {
            addEventItems("dividends", getDividends(actions));
        }
    }

    private void addEventItems(String name, List<EventItem> items) {
        if (!items.isEmpty()) {
            this.model.put(name, items.toArray(new EventItem[items.size()]));
        }
    }

    private List<EventItem> getSplits(List<CorporateAction> actions) {
        final List<EventItem> result = new ArrayList<>();
        final RatioFormatter fmt = createRatioFormatter();
        for (CorporateAction ca : actions) {
            if (ca.getType() == CorporateAction.Type.FACTOR) {
                final String ratio = fmt.format(ca.getFactor());
                final Number value = getCloseValueOn(ca.getDate());
                if (value != null) {
                    result.add(new EventItem(value, ca.getDate(), ratio));
                }
            }
        }
        return result;
    }

    private RatioFormatter createRatioFormatter() {
        final LocaleEditor editor = new LocaleEditor();
        editor.setAsText(getLocale(this.cmd.getNumberLocale(), this.cmd.getLocale()));
        return new RatioFormatter((Locale) editor.getValue());
    }

    private String getLocale(String l1, String l2) {
        return (l1 != null) ? l1 : (l2 != null ? l2 : "de");
    }

    private List<EventItem> getDividends(List<CorporateAction> cas) {
        final List<EventItem> result = new ArrayList<>();
        for (CorporateAction ca : cas) {
            if (ca.getType() == CorporateAction.Type.DIVIDEND) {
                final Number value = getCloseValueOn(ca.getDate());
                if (value != null) {
                    // TODO: convert factor into quote's currency?
                    result.add(new EventItem(value, ca.getDate(), ca.getFactor()));
                }
            }
        }
        return result;
    }

    private Number getCloseValueOn(DateTime dt) {
        final int offset = this.close.getOffset(dt.toLocalDate());
        if (offset == -1) {
            return null;
        }
        final double[] values = this.close.getValues();
        for (int i = 0; i < 5; i++) {
            for (int f = 1; f >= -1; f -= 2) {
                final int index = offset + (f * i);
                if (index >= 0 && index < values.length) {
                    final double d = values[index];
                    if (!Double.isNaN(d) && d > 0) {
                        return d;
                    }
                }
            }
        }
        return null;
    }

    private void addTimeperiodSpec() {
        // timeperiodSpec's to is not inclusive, so add a day to show to
        final LocalDate afterTo = this.to.plusDays(1);
        final TimeperiodDefinitionBuilder builder;
        if (this.aggregation == ChartUtil.Aggregation.DAILY) {
            builder = new TimeperiodDefinitionBuilder(from, afterTo, false);
        }
        else if (this.aggregation == ChartUtil.Aggregation.WEEKLY) {
            final LocalDate ldFrom = from.dayOfWeek().withMinimumValue();
            final LocalDate ldTo = afterTo.dayOfWeek().withMaximumValue().plusDays(1);
            builder = new TimeperiodDefinitionBuilder(toYMD(ldFrom), toYMD(ldTo), false);
        }
        else {
            final LocalDate ldFrom = from.dayOfMonth().withMinimumValue();
            final LocalDate ldTo = afterTo.dayOfMonth().withMaximumValue().plusDays(1);
            builder = new TimeperiodDefinitionBuilder(toYMD(ldFrom), toYMD(ldTo), false);
        }
        this.result.withTimeperiod(builder);
    }

    private LocalDate toYMD(LocalDate ld) {
        return new LocalDate(ld.getYear(), ld.getMonthOfYear(), ld.getDayOfMonth());
    }

    private void addMain(Iterator<HistoricTimeseries> it) {
        final TimeSeriesCollection main = new TimeSeriesCollection("main",
                this.quote.getInstrument().getName(), getStyleName());

        if (isFund && this.cmd.isBviPerformanceForFunds()) {
            roundValues(this.close);
        }
        this.close = mergeLast(this.quote, this.close);

        final TimeSeries closeTs = toTimeSeries(this.close, "close", ChartUtil.Consolidation.LAST);

        main.add("close", closeTs);
        if (isWithOpenHighLow(getStyleKey()) && !(this.isFund && this.cmd.isBviPerformanceForFunds())) {
            if (!this.isFund) {
                addOpenHighLow(main, mergeOpen(it.next()), mergeHigh(it.next()), mergeLow(it.next()));
            }
            else {
                addOpenHighLow(main, this.close, this.close, this.close);
            }
        }

        addTimeseriesCollection(main, "main");
        addColor(this.quote, "main");
        addLabel(this.quote, "main");

        if (this.cmd.isWithInterval() && closeTs.size() > 0) {
            final Interval iv = ChartUtil.getInterval(closeTs);
            if (iv != null) {
                this.model.put("main.interval", iv);
            }
        }
    }

    private void roundValues(HistoricTimeseries close) {
        double[] values = close.getValues();
        for (int i = 0; i < values.length; i++) {
            if (!Double.isNaN(values[i])) {
                values[i] = Math.round(values[i] * 100) / 100d;
            }
        }
    }

    private void merge(Quote q, HistoricTimeseries ts, PriceRecord pr) {
        if (pr != null) {
            merge(q, ts, getPriceForMerge(pr));
        }
    }

    private Price getPriceForMerge(PriceRecord pr) {
        return (pr instanceof PriceRecordFund)
                ? ((PriceRecordFund) pr).getRedemptionPrice()
                : pr.getPrice();
    }

    private void merge(Quote q, HistoricTimeseries ts, Price p) {
        if (!this.historicConfigurationMBean.isEodHistoryEnabled(quote) && p.getDate() != null && p.getValue() != null) {
            final int offset = ts.getOffset(p.getDate().toLocalDate());
            if (offset >= 0 && Double.isNaN(ts.getValue(offset))) {
                ts.getValues()[offset] = p.getValue().doubleValue();
            }
        }
    }

    private HistoricTimeseries mergeOpen(HistoricTimeseries ts) {
        if (this.intradayPrice != null) {
            merge(this.quote, ts, this.intradayPrice.getOpen());
        }
        return (ts != this.close) ? transform(ts) : ts;
    }

    private HistoricTimeseries mergeLow(HistoricTimeseries ts) {
        if (this.intradayPrice != null) {
            merge(this.quote, ts, this.intradayPrice.getLowDay());
        }
        return (ts != this.close) ? transform(ts) : ts;
    }

    private HistoricTimeseries mergeHigh(HistoricTimeseries ts) {
        if (this.intradayPrice != null) {
            merge(this.quote, ts, this.intradayPrice.getHighDay());
        }
        return (ts != this.close) ? transform(ts) : ts;
    }

    private HistoricTimeseries mergeLast(Quote q, HistoricTimeseries ts) {
        if (this.isFund && this.cmd.isBviPerformanceForFunds()) {
            return ts;
        }
        merge(q, ts, this.intradayPrice);
        return transform(ts);
    }

    private HistoricTimeseries transform(HistoricTimeseries ts) {
        if (this.isFund && this.cmd.isBviPerformanceForFunds()) {
            return ts;
        }
        return transform(ts, this.atValue);
    }

    private HistoricTimeseries transform(HistoricTimeseries ts, double referenceValue) {
        if (this.atDay != null && isPercent()) {
            return ts.multiply(100d / referenceValue);
        }
        return ts;
    }

    private void addOpenHighLow(TimeSeriesCollection main, HistoricTimeseries open,
            HistoricTimeseries high, HistoricTimeseries low) {
        // TODO: align!
        main.add("open", toTimeSeries(open, "open", ChartUtil.Consolidation.FIRST));
        main.add("high", toTimeSeries(high, "high", ChartUtil.Consolidation.MAX));
        main.add("low", toTimeSeries(low, "low", ChartUtil.Consolidation.MIN));
    }

    private void addVolume(Iterator<HistoricTimeseries> it) {
        if (cmd.isVolume() && !isFund && !isIgnoreVolumeType) {
            // important to align to htc, as close will be used to determine whether the volume was
            // for raising or falling prices
            final HistoricTimeseries volume = it.next().alignTo(close);
            if (isUndefined(volume)) {
                return;
            }
            final TimeSeriesCollection tsc =
                    new TimeSeriesCollection("volume", "Volumen", VolumeDrawStyle.STYLE_KEY);
            final TimeSeries volumeTs = toTimeSeries(volume, "volume", ChartUtil.Consolidation.SUM);
            tsc.add("volume", volumeTs);
            model.put("volume", tsc);
        }
    }

    private TimeSeries toTimeSeries(HistoricTimeseries ht, String key,
            ChartUtil.Consolidation cons) {
        return ChartUtil.toTimeSeries(ht, key, "", this.aggregation, cons);
    }

    private void addGDs(Iterator<HistoricTimeseries> it) {
        if (this.cmd.getGd() == null) {
            addGD(it, resolveGd1(), "gd1");
            addGD(it, cmd.getGd2(), "gd2");
            addGD(it, cmd.getGd3(), "gd3");
        }
        else {
            for (int i = 0; i < 4; i++) {
                final String gd = this.cmd.getGd(i);
                if (gd == null) {
                    break;
                }
                final String name = (i == 0) ? "gd" : ("gd" + i);
                final HistoricTimeseries ts = it.next();
                addGD(isPercent() ? transform(ts, this.atValue) : ts, gd, name, "gd");
                final String color = cmd.getGdColor(i);
                if (color != null) {
                    this.model.put(name + COLOR_SUFFIX, color);
                }
            }
        }
    }

    private void addGD(Iterator<HistoricTimeseries> it, final String gd, final String name) {
        if (StringUtils.hasText(gd)) {
            HistoricTimeseries ht = it.next();
            if (!isUndefined(ht)) {
                addGD(isPercent() ? transform(ht, this.atValue) : ht, gd, name, name);
            }
        }
    }

    private void addGD(HistoricTimeseries ht, String gd, String name, String subname) {
        final TimeSeriesCollection tsc =
                new TimeSeriesCollection(name, getGdLabel(gd), LineDrawStyle.STYLE_KEY);
        final TimeSeries ts = TimeSeriesFactory.daily(name, "", ht.getValues(), ht.getStartDay());
        tsc.add(subname, ts);
        this.model.put(name, tsc);
    }

    private String getGdLabel(String gd) {
        return getGdAbbreviation() + " " + gd;
    }

    private String getGdAbbreviation() {
        return MESSAGES.getMessage("gdAbbrev", null, this.locale);
    }

    private void addIndicator(Iterator<HistoricTimeseries> it) {
        if (this.cmd.getIndicator() == null) {
            return;
        }
        int i = 0;
        for (String indicatorName : this.cmd.getIndicator()) {
            final Indicator indicator = this.controller.getIndicator(indicatorName);
            if (indicator == null) {
                continue;
            }
            this.model.put("indicator" + (i == 0 ? "" : Integer.toString(i)), indicator.getName());
            final String color = cmd.getIndicatorColor(i++);
            final String[] colors = color != null ? color.split(",") : new String[0];
            final Indicator.Element[] elements = indicator.getElements();
            for (int j = 0; j < elements.length; j++) {
                final Indicator.Element element = elements[j];
                HistoricTimeseries hti = it.next();
                if (isUndefined(hti)) {
                    continue;
                }
                if (isPercent() && this.atDay != null &&
                        ("bb".equals(indicatorName) || "vma".equals(indicatorName))) {
                    hti = transform(hti, this.atValue);
                }
                final TimeSeriesCollection tsci =
                        new TimeSeriesCollection(element.getKey(), element.getName(), LineDrawStyle.STYLE_KEY);
                final TimeSeries tsu = TimeSeriesFactory.daily(element.getKey(), "", hti.getValues(), hti.getStartDay());
                tsci.add(element.getKey(), tsu);
                this.model.put(element.getKey(), tsci);
                if (j < colors.length) {
                    this.model.put(element.getKey() + COLOR_SUFFIX, colors[j]);
                }
                else if (color != null) {
                    this.model.put(element.getKey() + COLOR_SUFFIX, color);
                }
            }
        }
    }

    private void addSignals() {
        if (!StringUtils.hasText(this.cmd.getSignale())) {
            return;
        }
        final TradingPhase.SignalSystem.Strategy strategy = "cons".equals(this.cmd.getSignale())
                ? TradingPhase.SignalSystem.Strategy.conservative
                : TradingPhase.SignalSystem.Strategy.speculative;

        final SymbolQuote sq = SymbolQuote.create(this.quote);
        final TradingPhaseRequest request = new TradingPhaseRequest(sq,
                TradingPhase.SignalSystem.bollinger, strategy, this.interval, Boolean.FALSE);

        final TradingPhaseResponse response = this.controller.getTradingPhaseProvider().getTradingPhases(request);
        final List<TradingPhase> tradingPhases = response.getData(sq);

        if (tradingPhases == null || tradingPhases.isEmpty()) {
            return;
        }

        final HistoricRequest htr = createRequest(this.quote, this.intradayPrice);
        htr.addClose(null);
        final List<HistoricTimeseries> ts = getTimeseries(htr);
        if (ts == null) {
            return;
        }

        final List<EventItem> activations = new ArrayList<>();
        final List<EventItem> signals = new ArrayList<>();

        final HistoricTimeseries ht = ts.get(0);
        for (TradingPhase phase : tradingPhases) {
            final double vStart = ht.getValue(phase.getStartDate());
            if (Double.isNaN(vStart)) {
                continue;
            }
            activations.add(new EventItem(vStart, phase.getStartDate().toDateTimeAtStartOfDay(), ""));

            if (phase.getEndDate() == null) {
                continue;
            }
            final double vEnd = ht.getValue(phase.getEndDate());
            if (Double.isNaN(vEnd)) {
                continue;
            }
            signals.add(new EventItem(vEnd, phase.getEndDate().toDateTimeAtStartOfDay(), formatSignal(vEnd)));
        }

        if (!activations.isEmpty()) {
            this.model.put("activations", activations.toArray(new EventItem[activations.size()]));
        }
        if (!signals.isEmpty()) {
            this.model.put("signals", signals.toArray(new EventItem[signals.size()]));
        }
    }

    private String toMmTalk(String s) {
        return s != null ? ('"' + s + '"') : "_";
    }

    private String toMmTalk(boolean b) {
        return Boolean.valueOf(b).toString();
    }

    private String getCloseFormula(boolean forFund) {
        if (forFund && this.cmd.isBviPerformanceForFunds()) {
            return getBviFormula();
        }
        return getFormula(forFund ? "Rücknahme" : "Close");
    }

    private String getFormula(String name) {
        final boolean blendCorporateActions =
                this.cmd.getBlendCorporateActions() == null || this.cmd.getBlendCorporateActions();
        final boolean blendDividends = this.cmd.getBlendDividends() != null
                ? this.cmd.getBlendDividends() : this.isFund;
        final StringBuilder sb = new StringBuilder(40).append(name).append("[")
                .append(toMmTalk(this.cmd.getCurrency()))
                .append(";").append(toMmTalk(blendCorporateActions))
                .append(";").append(toMmTalk(blendDividends))
                .append("]");
        if (!HistoricTimeseriesUtils.DEFAULT_FACTOR.equals(this.factor)) {
            sb.append("*").append(this.factor.toPlainString());
        }
        return sb.toString();
    }

    private String getBviFormula() {
        return "(BVIPerformanceZR[" + asMmTalkDate(this.atDay) + "]+100)";
    }

    private HistoricRequest createRequest() {
        final HistoricRequest request = createRequest(this.quote, this.intradayPrice);

        final String close;
        if (this.isFund && this.cmd.isBviPerformanceForFunds()) {
            close = getBviFormula();
            request.addMmTalk(close);
        }
        else if (request.isYieldBased()) {
            close = "OpenInterest";
            request.addClose(null);
        }
        else if (request.isSettlementBased()) {
            close = "Kassa";
            request.addClose(null);
        }
        else {
            final String base = getCloseFormula(this.isFund);
            final StringBuilder sb = new StringBuilder(80).append("(");
            sb.append(base);
            close = sb.append(")").toString();
            if (base.startsWith("Rücknahme") || base.startsWith("Close")) {
                request.addClose(null);
            }
            else {
                request.addMmTalk(close);
            }

            if (isWithOpenHighLow(getStyleKey()) && !this.isFund) {
                request.addOpen(null);
                request.addHigh(null);
                request.addLow(null);
            }
        }
        if (this.cmd.isVolume() && !this.isFund && !isIgnoreVolumeType) {
            request.addVolume(null);
        }

        if (this.cmd.getGd() == null) {
            if (StringUtils.hasText(this.cmd.getGd1())) {
                request.addMmTalk(close + getGdExpression(resolveGd1()));
            }
            if (StringUtils.hasText(this.cmd.getGd2())) {
                request.addMmTalk(close + getGdExpression(this.cmd.getGd2()));
            }
            if (StringUtils.hasText(this.cmd.getGd3())) {
                request.addMmTalk(close + getGdExpression(this.cmd.getGd3()));
            }
        }
        else {
            for (int i = 0; i < 4; i++) {
                final String gd = this.cmd.getGd(i);
                if (gd == null) {
                    break;
                }
                request.addMmTalk(close + getGdExpression(gd));
            }
        }

        if (this.cmd.getIndicator() != null) {
            for (String indicatorName : this.cmd.getIndicator()) {
                final Indicator indicator = this.controller.getIndicator(indicatorName);
                if (indicator == null) {
                    continue;
                }
                for (Indicator.Element element : indicator.getElements()) {
                    request.addMmTalk(close + element.getFormula());
                }
            }
        }

        if (this.cmd.getMainField() != null) {
            // vwd fields are only supported as main in EOD-enabled environments
            if (request instanceof HistoricRequestImpl) {
                ((HistoricRequestImpl) request).addHistoricTerm(HistoricTerm.fromVwdField(parseField(cmd.getMainField())));
            }
        }

        return request;
    }

    private VwdFieldDescription.Field parseField(String s) {
        if (s.matches("\\d+")) {
            final int fieldid = Integer.parseInt(s);
            return VwdFieldDescription.getField(fieldid);
        }
        return VwdFieldDescription.getFieldByName(s);
    }

    private String getGdExpression(String gd) {
        return ".GD[" + gd + ";\"linear\"]";
    }

    private String resolveGd1() {
        if (!"0".equals(this.cmd.getGd1())) {
            return this.cmd.getGd1();
        }
        final int daysBetween = DateUtil.daysBetween(this.from, this.to);
        if (daysBetween <= 366) {
            return "38";
        }
        if (daysBetween <= (366 * 3)) {
            return "100";
        }
        return "200";
    }

    private void addBenchmarks() {
        int n = 0;

        for (int i = 0; i < this.benchmarks.length; i++) {
            final Quote bq = this.benchmarks[i];
            if (bq == null) {
                continue;
            }

            final HistoricTimeseries ht = getBenchmarkTimeseries(bq, this.benchmarkPrices[i]);
            if (ht == null) {
                continue;
            }

            final String id = (n == 0) ? "bench" : ("bench" + n);
            n++;
            final TimeSeriesCollection bench = new TimeSeriesCollection(id,
                    qns.getName(bq), LineDrawStyle.STYLE_KEY);
            final TimeSeries closeTs
                    = TimeSeriesFactory.daily(id, "", ht.getValues(), ht.getStartDay());
            bench.add("close", closeTs);
            this.model.put(id, bench);
            addLabel(bq, id);
            addColor(bq, id);
        }
    }

    private HistoricTimeseries getBenchmarkTimeseries(Quote bq, final PriceRecord pr) {
        HistoricTimeseries result = getClose(bq, pr);
        if (isUndefined(result) || (!canHaveNegativeValues(bq) && hasNegativeValues(result))) {
            return null;
        }

        if (this.cmd.isBviPerformanceForFunds() && isFund(bq)) {
            return result;
        }
        merge(bq, result, pr);
        if (isPercent()) {
            result = transform(result, result.getValueAtOrBeforeOrAfter(this.atDay));
        }
        else if (isWithNormalizedBenchmarks()) {
            result = result.multiply(this.benchmarkNormFactor.doubleValue()
                    / result.getValueAtOrBeforeOrAfter(this.atDay));
        }
        return result;
    }

    private boolean canHaveNegativeValues(Quote bq) {
        InstrumentTypeEnum type = bq.getInstrument().getInstrumentType();
        return type == InstrumentTypeEnum.MK
                || type == InstrumentTypeEnum.ZNS  // hack for ISTAR-794 R-80910  ??
                || type == InstrumentTypeEnum.BND;
    }

    private void addCompositeBenchmark() {
        HistoricTimeseries cht = null;
        final StringBuilder name = new StringBuilder(80);

        for (int i = 0; i < this.benchmarks.length; i++) {
            final Quote bq = this.benchmarks[i];
            if (bq == null) {
                return;
            }

            final BigDecimal share = this.shares.get(i);

            if (name.length() > 0) {
                name.append(", ");
            }
            final BigDecimal d = share.multiply(Constants.ONE_HUNDRED, Constants.MC);
            name.append(d.intValue()).append("% ").append(qns.getName(bq));

            final HistoricTimeseries ht = getBenchmarkTimeseries(bq, this.benchmarkPrices[i]);
            if (ht == null) {
                return;
            }

            if (cht == null) {
                cht = ht.multiply(share.doubleValue());
            }
            else {
                cht = cht.add(ht.multiply(share.doubleValue()));
            }
        }

        if (cht == null) {
            return;
        }

        final String id = "bench";
        final TimeSeriesCollection bench = new TimeSeriesCollection("bench",
                name.toString(), LineDrawStyle.STYLE_KEY);
        final TimeSeries closeTs
                = TimeSeriesFactory.daily(id, "", cht.getValues(), cht.getStartDay());
        bench.add("close", closeTs);
        this.model.put(id, bench);
        if (cmd.getBenchmarkLabel(0) != null) {
            this.model.put(id + LABEL_SUFFIX, cmd.getBenchmarkLabel(0));
        }
        if (cmd.getBenchmarkColor(0) != null) {
            this.model.put(id + COLOR_SUFFIX, cmd.getBenchmarkColor(0));
        }
    }

    private void computeAt() {
        if (!isPercent() && !this.cmd.isNormalizeBenchmarks()) {
            return;
        }

        final boolean previousForBenchmarks = this.shares.size() <= 1
                && this.getAggregation() == ChartUtil.Aggregation.DAILY;

        final HistoricTimeseries main = getClose(this.quote, this.intradayPrice);
        this.atDay = getFirstDay(main, this.from, false);
        for (int i = 0; i < this.benchmarks.length; i++) {
            Quote bq = this.benchmarks[i];
            if (bq == null) {
                continue;
            }
            final HistoricTimeseries bench = getClose(bq, this.benchmarkPrices[i]);
            this.atDay = getFirstDay(bench, atDay, previousForBenchmarks);
        }

        if (previousForBenchmarks) {
            ensureBenchmarkCloseAt();
        }

        if (main != null && isWithNormalizedBenchmarks()) {
            computeNormFactor(main, atDay);
        }
    }

    private void ensureBenchmarkCloseAt() {
        // make sure all benchmarks have a value at "atDay" or a previous value
        NEXT_BENCH:
        for (int i = 0; i < this.benchmarks.length; i++) {
            Quote bq = this.benchmarks[i];
            if (bq == null) {
                continue;
            }
            final HistoricTimeseries bench = getClose(bq, this.benchmarkPrices[i]);
            if (bench == null) {
                continue;
            }
            final double[] benchValues = bench.getValues();
            final int atDayOffset = bench.getOffset(this.atDay);
            if (atDayOffset < 0) {
                continue;
            }
            for (int j = atDayOffset; j >= 0; j--) {
                if (!Double.isNaN(benchValues[j])) {
                    continue NEXT_BENCH;
                }
            }
            // no previous value, set it...
            benchValues[atDayOffset] = bench.getPreviousValue();
        }
    }

    private boolean isWithNormalizedBenchmarks() {
        return !isPercent() && this.cmd.isNormalizeBenchmarks();
    }

    private void computeNormFactor(HistoricTimeseries ht, LocalDate atDay) {
        int n = DateUtil.daysBetween(this.from, atDay);
        while (n >= 0) {
            if (n < ht.size() && !Double.isNaN(ht.getValue(n))) {
                // on atDay, benchmark's timeseries will have value=100(%), by multiplying it with
                // benchmarkNormFactor, it will have the same value as ht on that day
                // mmtalk requires german format, so replace . by ,
                this.benchmarkNormFactor = BigDecimal.valueOf(ht.getValue(n));
                return;
            }
            n--;
        }
    }

    private String asMmTalkDate(LocalDate day) {
        return "\"" + day.getDayOfMonth() + "." + day.getMonthOfYear() + "." + day.getYear() + "\"";
    }

    private List<HistoricTimeseries> getTimeseries(HistoricRequest request) {
        return this.historicConfigurationMBean.isEodHistoryEnabled(quote)
                ? this.controller.getHistoricProviderEod().getTimeseries((HistoricRequestImpl) request)
                : this.controller.getHistoricProvider().getTimeseries((HistoricTimeseriesRequest) request);
    }

    private HistoricTimeseries getDividendsTimeseries() {
        final HistoricRatiosProvider rp = this.controller.getHistoricRatiosProvider();
        if (rp == null) {
            return HistoricTimeseriesUtils.emptyTimeSeries(this.from);
        }

        final List<CorporateAction> actions = rp.getCorporateActions(SymbolQuote.create(this.quote), this.interval, false);
        final List<Double> values = new ArrayList<>();
        CorporateAction previousAction = null;
        DateTime chartStartDate = null;
        for (CorporateAction action : actions) {
            if (action.getType() == CorporateAction.Type.DIVIDEND) {
                if (previousAction != null) {
                    int daysBetweenDividends = Days.daysBetween(previousAction.getDate(), action.getDate()).getDays();
                    for (int i = 0; i < daysBetweenDividends; i++) {
                        values.add(Double.NaN);
                    }
                }
                else {
                    chartStartDate = action.getDate();
                }
                values.add(action.getFactor().doubleValue());
                previousAction = action;
            }
        }

        return new HistoricTimeseries(ArrayUtils.toPrimitive(values.toArray(new Double[0])), chartStartDate == null ? this.from : chartStartDate.toLocalDate());
    }

    private HistoricTimeseries getClose(Quote quote, PriceRecord priceRecord) {
        final HistoricTimeseries cached = this.closes.get(quote);
        if (cached != null) {
            return cached;
        }
        final HistoricTimeseries result = doGetClose(quote, priceRecord);
        this.closes.put(quote, result);
        return result;
    }

    private HistoricTimeseries doGetClose(Quote quote, PriceRecord priceRecord) {
        final HistoricRequest request = createRequest(quote, priceRecord);
        if (isFund(quote) && this.cmd.isBviPerformanceForFunds()) {
            request.addBviPerformance(from);
        }
        else if (request.isYieldBased() || request.isSettlementBased()) {
            request.addClose(null);
        }
        else {
            final String closeFormula = getCloseFormula(isFund(quote));
            if (closeFormula.startsWith("Rücknahme") || closeFormula.startsWith("Close")) {
                request.addClose(null);
            }
            else {
                request.addMmTalk(closeFormula);
            }
        }
        final List<HistoricTimeseries> ts = getTimeseries(request);
        return (ts != null && !ts.isEmpty()) ? ts.get(0) : null;
    }

    private HistoricRequest createRequest(Quote q, PriceRecord pr) {
        final HistoricRequest req = doCreateRequest(q, pr);

        final boolean blendCorporateActions =
                this.cmd.getBlendCorporateActions() == null || this.cmd.getBlendCorporateActions();
        req.withSplit(blendCorporateActions);
        final boolean blendDividends = this.cmd.getBlendDividends() != null
                ? this.cmd.getBlendDividends() : this.isFund;
        req.withDividend(blendDividends);

        return req;
    }

    private HistoricRequest doCreateRequest(Quote q, PriceRecord pr) {
        final String currency =
                StringUtils.hasLength(this.cmd.getCurrency()) ? this.cmd.getCurrency() :
                        q.getCurrency().getSymbolIso();
        if (this.historicConfigurationMBean.isEodHistoryEnabled(quote)) {
            return new HistoricRequestImpl(q, this.from, this.historicTo)
                    .withCurrency(currency)
                    .withPriceRecord(pr);
        }
        else {
            final HistoricTimeseriesRequest req =
                    new HistoricTimeseriesRequest(q, this.from, this.historicTo)
                            .withYieldBasedFromQuote()
                            .withCurrency(currency);
            // see also comments in HistoricRequestImpl's constructor
            if (!HistoricTimeseriesUtils.DEFAULT_FACTOR.equals(this.factor)) {
                req.withFactor(this.factor);
            }
            return req;
        }
    }

    private LocalDate getFirstDay(HistoricTimeseries ht, LocalDate from, boolean withPrevious) {
        if (ht == null) {
            return from;
        }
        int n = 0;
        while (n < ht.size() && Double.isNaN(ht.getValue(n))) {
            n++;
        }
        if (n == ht.size()) {
            return from;
        }
        final LocalDate firstValueAt = ht.getStartDay().plusDays(n);

        if (firstValueAt.isAfter(from)) {
            if (withPrevious && ht.hasPreviousValue()) {
                return from;
            }
            return firstValueAt;
        }
        else {
            return from;
        }
    }

    private String formatSignal(double v) {
        BigDecimal bd = BigDecimal.valueOf(v).round(Constants.MC).stripTrailingZeros();
        if (bd.scale() < 0) {
            bd = bd.setScale(0);
        }
        else if (bd.scale() == 1) {
            bd = bd.setScale(2);
        }
        else if (bd.scale() > 4) {
            bd = bd.setScale(4, RoundingMode.HALF_EVEN);
        }
        return "S " + bd.toPlainString();
    }

    @SuppressWarnings({"StringEquality"})
    private ChartUtil.Aggregation getAggregation() {
        if (StringUtils.hasText(this.cmd.getAggregation())) {
            return ChartUtil.Aggregation.valueOf(this.cmd.getAggregation().toUpperCase());
        }

        if (LineDrawStyle.STYLE_KEY != getStyleKey()) {
            final int numWeeks = new Period(this.from, this.to, PeriodType.weeks()).getWeeks();
            if (numWeeks > 104) {
                return ChartUtil.Aggregation.MONTHLY;
            }
            else if (numWeeks > 26 || (VolumeDrawStyle.STYLE_KEY != getStyleKey() && this.isFund)) {
                return ChartUtil.Aggregation.WEEKLY;
            }
        }
        return ChartUtil.Aggregation.DAILY;
    }

    // semantics changed for MMWEB-607:
    //   - an empty timeseries (with zero elements) is no longer considered undefined
    private boolean isUndefined(HistoricTimeseries ht) {
        if (ht == null) {
            return true;
        }
        if (ht.size() == 0) {
            return false;
        }
        // returns false (as in "not undefined") if we can find any value that is not NaN
        for (double v : ht.getValues()) {
            if (!Double.isNaN(v)) {
                return false;
            }
        }
        return true;
    }

    private boolean hasNegativeValues(HistoricTimeseries ht) {
        for (double v : ht.getValues()) {
            if (!Double.isNaN(v) && v < 0d) {
                return true;
            }
        }
        return false;
    }
}
