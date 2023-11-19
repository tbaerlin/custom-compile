/*
 * HistoricTimeseriesUtils.java
 *
 * Created on 16.07.2007 16:13:12
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.historic;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.math.stat.descriptive.moment.StandardDeviation;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import de.marketmaker.istar.common.util.DateUtil;
import de.marketmaker.istar.domain.data.AggregatedValue;
import de.marketmaker.istar.domain.data.PriceRecord;
import de.marketmaker.istar.domain.instrument.InstrumentTypeEnum;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.domainimpl.CurrencyDp2;
import de.marketmaker.istar.merger.Constants;
import de.marketmaker.istar.merger.provider.SymbolQuote;
import de.marketmaker.istar.merger.web.easytrade.TickDataCommand;
import org.joda.time.Period;

import static de.marketmaker.istar.domain.data.PriceQuality.REALTIME;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public final class HistoricTimeseriesUtils {

    public static final LocalDate FIRST_DAY = new LocalDate(1900, 1, 1);

    public static final DateTime FIRST_DATE_TIME = FIRST_DAY.toDateTimeAtStartOfDay();

    private HistoricTimeseriesUtils() {
        throw new AssertionError("not for instantiation or inheritance");
    }

    public static final BigDecimal DEFAULT_FACTOR = BigDecimal.ONE;

    public static int daysFromBegin(LocalDate date) {
        return DateUtil.daysBetween(FIRST_DAY, date);
    }

    public static int daysTillToday() {
        return DateUtil.daysBetween(FIRST_DAY, new LocalDate());
    }

    public static BigDecimal getFactor(Quote q) {
        return q.getCurrency().isCent() ? Constants.ONE_HUNDRED : DEFAULT_FACTOR;
    }

    public static BigDecimal getFactor(SymbolQuote q) {
        return CurrencyDp2.isCent(q.getCurrencyIso()) ? Constants.ONE_HUNDRED : DEFAULT_FACTOR;
    }

    public static HistoricTimeseries emptyTimeSeries(LocalDate startDay) {
        return new HistoricTimeseries(new double[0], startDay);
    }

    /**
     * Returns the latest day for which historic data can be requested. Historic data always refers
     * to a complete day and therefore we have to make sure that the current user is allowed to
     * see data for the whole day. For example, if a user has delayed access to prices at market GMF
     * with a 24h delay period, data for yesterday cannot be requested, although it may already
     * be available in the historic timeseries provider.
     *
     * @param pr contains current price quality and the nominal delay for that symbol
     * @return last day for which historic data can be requested
     */
    public static LocalDate getLastDayThatCanBeRequested(PriceRecord pr) {
        DateTime dt = new DateTime();
        if (pr != null && pr.getPriceQuality() != REALTIME && pr.getNominalDelayInSeconds() > 0) {
            DateTime dtDelayed = dt.minusSeconds(pr.getNominalDelayInSeconds());
            if (dtDelayed.getDayOfYear() != dt.getDayOfYear()) {
                // we are not allowed to see all of yeserday's data which might already be in pm
                return dtDelayed.toLocalDate().minusDays(1);
            }
        }
        return dt.toLocalDate();
    }

    public enum EntryTypeEnum {
        open, high, low, close, issuePrice, repurchasePrice, value
    }

    public static List<Outlier> searchOutlier(String outlierRule,
            TickDataCommand.ElementDataType type, List<AggregatedValue> myTicks) {
        switch (type) {
            case OHLC:
            case OHLCV:
                return searchOutlier(outlierRule, myTicks,
                        EntryTypeEnum.open,
                        EntryTypeEnum.high,
                        EntryTypeEnum.low,
                        EntryTypeEnum.close);
            case CLOSE:
                return searchOutlier(outlierRule, myTicks, EntryTypeEnum.close);
            case PERFORMANCE:
                return searchOutlier(outlierRule, myTicks, EntryTypeEnum.value);
            case FUND:
                return searchOutlier(outlierRule, myTicks,
                        EntryTypeEnum.issuePrice,
                        EntryTypeEnum.repurchasePrice);
            default:
                return Collections.emptyList();
        }
    }

    private static List<Outlier> searchOutlier(String outlierRule, List<AggregatedValue> myTicks,
            EntryTypeEnum... entryTypes) {
        if (null == entryTypes || entryTypes.length == 0) {
            return Collections.emptyList();
        }

        if ("5-sigma".equals(outlierRule)) {
            return searchOutlier5Sigma(myTicks, entryTypes);
        }
        else {
            throw new UnsupportedOperationException("no support for outlier rule: " + outlierRule);
        }
    }

    private static List<Outlier> searchOutlier5Sigma(List<AggregatedValue> myTicks,
            EntryTypeEnum[] entryTypes) {
        final Double[] sigma5 = calculate5Sigma(myTicks, entryTypes);
        final ArrayList<Outlier> ret = new ArrayList<>();
        final EnumSet<EntryTypeEnum> set = EnumSet.noneOf(EntryTypeEnum.class);

        // take the predecessor as pivot for absolute deviation
        for (int i = 1; i < myTicks.size(); i++) {
            set.clear();
            for (int j = 0; j < entryTypes.length; j++) {
                final BigDecimal value = getValue(myTicks.get(i), entryTypes[j]);
                final BigDecimal valueV = getValue(myTicks.get(i - 1), entryTypes[j]);
                if (null != sigma5[j] && isOutlier(value, valueV, sigma5[j])) {
                    set.add(entryTypes[j]);
                }
            }
            if (!set.isEmpty()) {
                ret.add(new Outlier(i, set));
            }
        }

        return ret;
    }

    private static Double[] calculate5Sigma(List<AggregatedValue> myTicks,
            EntryTypeEnum[] entryTypes) {
        final StandardDeviation[] sds = new StandardDeviation[entryTypes.length];
        for (int i = 0; i < sds.length; i++) {
            sds[i] = new StandardDeviation();
        }

        for (AggregatedValue myTick : myTicks) {
            for (int i = 0; i < entryTypes.length; i++) {
                final BigDecimal dv = getValue(myTick, entryTypes[i]);
                if (null != dv) {
                    sds[i].increment(dv.doubleValue());
                }
            }
        }

        final Double[] sigma5 = new Double[entryTypes.length];
        for (int i = 0; i < entryTypes.length; i++) {
            final double sigma = sds[i].getResult();
            if (isValidStat(sigma)) {
                sigma5[i] = 5 * sigma;
            }
        }
        return sigma5;
    }

    private static BigDecimal getValue(AggregatedValue myTick, EntryTypeEnum entryType) {
        switch (entryType) {
            case open:
                return myTick.getOpen();
            case high:
            case issuePrice:
                return myTick.getHigh();
            case low:
                return myTick.getLow();
            case close:
            case repurchasePrice:
            case value:
                return myTick.getClose();
            default:
                throw new UnsupportedOperationException("no support for: " + entryType);
        }
    }

    private static boolean isOutlier(BigDecimal val, BigDecimal pivot, double sigma5) {
        return null != val && null != pivot
                && Math.abs(val.doubleValue() - pivot.doubleValue()) > sigma5;
    }

    private static boolean isValidStat(double val) {
        return !(Double.isInfinite(val) || Double.isNaN(val));
    }

    public static class Outlier {
        private final int index;

        private final Set<EntryTypeEnum> entryTypes;

        public Outlier(int index, EnumSet<EntryTypeEnum> entryTypes) {
            this.index = index;
            this.entryTypes = EnumSet.copyOf(entryTypes);
        }

        public int getIndex() {
            return index;
        }

        public Set<EntryTypeEnum> getEntryTypes() {
            return EnumSet.copyOf(this.entryTypes);
        }
    }

    public static void addBviPerformance(HistoricRequestImpl req, LocalDate referencedate) {
        if (req.getQuote().getInstrument().getInstrumentType() == InstrumentTypeEnum.FND) {
            req.addHistoricTerm(HistoricTerm.fromMmTalk("BVIPerformanceZR[" +
                    HistoricTimeseriesRequest.mmtalk(referencedate) + "]+100"));
        }
    }

    public static HistoricTimeseries merge(HistoricTimeseries base, HistoricTimeseries delta) {
        LocalDate startDay = base.getStartDay().isBefore(delta.getStartDay())
                ? base.getStartDay() : delta.getStartDay();

        LocalDate baseED = base.getStartDay().plusDays(base.size());
        LocalDate deltaED = delta.getStartDay().plusDays(delta.size());
        LocalDate endDay = deltaED.isAfter(baseED) ? deltaED : baseED;

        int size = DateUtil.daysBetween(startDay, endDay);
        double[] values = new double[size];
        Arrays.fill(values, Double.NaN);

        int lenFromDeltaStart = DateUtil.daysBetween(startDay, base.getStartDay());
        System.arraycopy(delta.getValues(), 0, values, 0, lenFromDeltaStart);
        System.arraycopy(base.getValues(), 0, values, lenFromDeltaStart, base.size());
        int lenFromDeltaEnd = Math.min(delta.size(), DateUtil.daysBetween(baseED, endDay));
        System.arraycopy(delta.getValues(), delta.size() - lenFromDeltaEnd, values,
                values.length - lenFromDeltaEnd, lenFromDeltaEnd);

        return new HistoricTimeseries(values, startDay);
    }

    public static DateTime getDate(PriceRecord pr) {
        final DateTime date = pr.getPrice().getDate();
        if (date != null) {
            return date;
        }
        return pr.getDate();
    }

    public static LocalDate computeBegin(HistoricRequest request) {
        LocalDate result = request.getFrom();
        if (isWithAggregation(request)
                && request.isAlignStartWithAggregationPeriod()
                && request.getAggregationPeriod().getDays() == 0) { // no align for day-based periods
            final Period p = request.getAggregationPeriod();
            result = LocalDateSequence.create(result, p).getNext().minus(p);
        }
        return clampToMaxPast(result);
    }

    public static LocalDate computeEnd(HistoricRequest request) {
        LocalDate result = request.getTo();
        if (isWithAggregation(request)
                && request.isAlignEndWithAggregationPeriod()
                && request.getAggregationPeriod().getDays() == 0) { // no align for day-based periods
            final Period p = request.getAggregationPeriod();
            result = LocalDateSequence.create(result, p).getNext().minusDays(1);
        }
        return clampToMaxFuture(result);
    }

    public static LocalDate clampToMaxPast(LocalDate date) {
        return (date.isBefore(FIRST_DAY)) ? FIRST_DAY : date;
    }

    public static LocalDate clampToMaxFuture(LocalDate date) {
        final LocalDate now = LocalDate.now();
        return (date.isAfter(now)) ? now : date;
    }

    public static boolean isWithAggregation(HistoricRequest request) {
        final Period period = request.getAggregationPeriod();
        return (period != null && !Constants.ONE_DAY.equals(period));
    }

}
