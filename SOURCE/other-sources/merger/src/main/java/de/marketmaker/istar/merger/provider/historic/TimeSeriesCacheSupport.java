/*
 * EodPriceHistorySupport.java
 *
 * Created on 20.02.13 09:13
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.historic;

import java.io.Serializable;
import java.util.Arrays;

import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import org.joda.time.LocalDate;

import de.marketmaker.istar.common.util.DateUtil;

/**
 * @author zzhao
 */
public abstract class TimeSeriesCacheSupport {

    private Ehcache cache;

    public void setCache(Ehcache cache) {
        this.cache = cache;
    }

    protected HistoricTimeseries fromCache(String cacheKey, LocalDate from, LocalDate to) {
        if (null != this.cache) {
            final Element element = this.cache.get(cacheKey);
            if (null != element) {
                final CachedTimeSeries ct = (CachedTimeSeries) element.getValue();
                // todo current implementation doesn't work anymore
                if (!from.isBefore(ct.requestedFrom)) {
                    return fromCache(ct, from, to);
                }
            }
        }

        return null;
    }

    protected HistoricTimeseries fromCache(CachedTimeSeries ct, LocalDate from, LocalDate to) {
        final int numDays = DateUtil.daysBetween(from, to);
        if (numDays <= 0) {
            return HistoricTimeseriesUtils.emptyTimeSeries(from);
        }
        final double[] values = new double[numDays];

        if (ct.getFrom().isAfter(to)) {
            Arrays.fill(values, Double.NaN);
            return new HistoricTimeseries(values, from);
        }

        final int srcPos;
        final int destPos;
        if (from.isBefore(ct.getFrom())) {
            srcPos = 0;
            destPos = DateUtil.daysBetween(from, ct.getFrom());
        }
        else {
            srcPos = DateUtil.daysBetween(ct.getFrom(), from);
            destPos = 0;
        }

        final double[] ctValues = ct.getValues();
        final int length = Math.min(ctValues.length - srcPos, values.length - destPos);

        System.arraycopy(ctValues, srcPos, values, destPos, length);
        if (destPos > 0) {
            Arrays.fill(values, 0, destPos, Double.NaN);
        }
        if (destPos + length < values.length) {
            Arrays.fill(values, destPos + length, values.length, Double.NaN);
        }

        final HistoricTimeseries result = new HistoricTimeseries(values, from);
        for (int i = srcPos; i-- > 0; ) {
            if (!Double.isNaN(ctValues[i])) {
                result.setPreviousValue(ctValues[i], from.minusDays(srcPos - i));
                break;
            }
        }
        return result;
    }

    protected boolean isCacheAvailable() {
        return null != this.cache && !this.cache.isDisabled();
    }

    protected CachedTimeSeries cacheTimeSeries(String cacheKey, HistoricTimeseries ht) {
//        if (null != this.cache) {
//            final CachedTimeSeries ct = toCachedTimeSeries(ht);
//            cacheTimeSeries(cacheKey, ct);
//            return ct;
//        }
        return null;
    }

    protected void cacheTimeSeries(String cacheKey, CachedTimeSeries ct) {
//        this.cache.put(new Element(cacheKey, ct));
    }

    protected CachedTimeSeries toCachedTimeSeries(HistoricTimeseries ht) {
        if (null == ht) {
            return new CachedTimeSeries(HistoricTimeseriesUtils.FIRST_DAY, new LocalDate(),
                    new double[0]);
        }

        return toCachedTimeSeries(ht.getValues(), ht.getStartDay());
    }

    protected CachedTimeSeries toCachedTimeSeries(double[] values, LocalDate startDay) {
        if (null == values || values.length == 0) {
            return new CachedTimeSeries(HistoricTimeseriesUtils.FIRST_DAY,
                    new LocalDate(), new double[0]);
        }

        int n = 0;
        while (n < values.length && Double.isNaN(values[n])) {
            n++;
        }
        if (n == values.length) {
            return new CachedTimeSeries(startDay, new LocalDate(), new double[0]);
        }
        if (n < 5) {
            return new CachedTimeSeries(startDay, startDay, values);
        }

        // too many NaNs at beginning, clip them off
        final double[] tmp = new double[values.length - n];
        System.arraycopy(values, n, tmp, 0, tmp.length);
        return new CachedTimeSeries(startDay, startDay.plusDays(n), tmp);
    }

    public static final class CachedTimeSeries implements Serializable {
        static final long serialVersionUID = 1L;

        // start of period this data was requested for; may differ from from if data
        // at the start of that period is not available.
        private LocalDate requestedFrom;

        // day for values[0]
        private LocalDate from;

        // values for consecutive days: NaN for days w/o data.
        private double[] values;

        public CachedTimeSeries(LocalDate requestedFrom, LocalDate from, double[] values) {
            this.requestedFrom = requestedFrom;
            this.from = from;
            this.values = values;
        }

        public LocalDate getFrom() {
            return from;
        }

        public LocalDate getRequestedFrom() {
            return requestedFrom;
        }

        public double[] getValues() {
            return values;
        }
    }
}
