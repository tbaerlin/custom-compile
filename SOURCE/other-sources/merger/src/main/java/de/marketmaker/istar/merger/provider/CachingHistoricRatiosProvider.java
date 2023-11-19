/*
 * CachingHighLowProvider.java
 *
 * Created on 03.10.2006 18:14:06
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import de.marketmaker.istar.domain.data.*;
import de.marketmaker.istar.domainimpl.data.IntervalUnit;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.LocalDate;
import org.joda.time.YearMonthDay;

import de.marketmaker.istar.domain.instrument.Quote;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class CachingHistoricRatiosProvider implements HistoricRatiosProvider {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private Ehcache caCache;

    private Ehcache basicCache;

    private Ehcache extendedCache;

    private HistoricRatiosProvider historicRatiosProvider;

    public void setBasicCache(Ehcache basicCache) {
        this.basicCache = basicCache;
    }

    public void setExtendedCache(Ehcache extendedCache) {
        this.extendedCache = extendedCache;
    }

    public void setCaCache(Ehcache caCache) {
        this.caCache = caCache;
    }

    public void setHistoricRatiosProvider(HistoricRatiosProvider historicRatiosProvider) {
        this.historicRatiosProvider = historicRatiosProvider;
    }

    public List<QuarterlyYield> getQuarterlyYields(Quote quote) {
        return getQuarterlyYields(SymbolQuote.create(quote));
    }

    @Override
    public List<IntervalPerformance> getIntervalPerformances(SymbolQuote quote, IntervalUnit unit, int intervals) {
        return this.historicRatiosProvider.getIntervalPerformances(quote, unit, intervals);
    }

    public List<QuarterlyYield> getQuarterlyYields(SymbolQuote quote) {
        // TODO: add caching
        return this.historicRatiosProvider.getQuarterlyYields(quote);
    }


    public List<BasicHistoricRatios> getBasicHistoricRatios(SymbolQuote quote,
            SymbolQuote benchmarkQuote, List<Interval> intervals) {
        return getBasicHistoricRatios(quote, benchmarkQuote, intervals, null);
    }

    public List<BasicHistoricRatios> getBasicHistoricRatios(SymbolQuote quote,
            SymbolQuote benchmarkQuote, List<Interval> intervals, Double aggregation) {

        final List<BasicHistoricRatios> result = new ArrayList<>(intervals.size());

        final List<Interval> missingIntervals = new ArrayList<>(intervals.size());
        final List<Key> missingKeys = new ArrayList<>(intervals.size());

        final Long benchmarkQid = (benchmarkQuote != null) ? benchmarkQuote.getId() : null;

        for (final Interval interval : intervals) {
            final Key key = new Key(quote.getId(), benchmarkQid, interval, aggregation);
            final Element ce = this.basicCache.get(key);

            if (ce == null) {
                if (this.logger.isDebugEnabled()) {
                    this.logger.debug("<getBasicHistoricRatios> not in cache: " + key);
                }

                missingIntervals.add(interval);
                missingKeys.add(key);
                result.add(null);
            }
            else {
                if (this.logger.isDebugEnabled()) {
                    this.logger.debug("<getBasicHistoricRatios> found in cache: " + key);
                }

                result.add((BasicHistoricRatios) ce.getValue());
            }
        }

        if (missingIntervals.isEmpty()) {
            return result;
        }

        final List<BasicHistoricRatios> backendData =
                this.historicRatiosProvider.getBasicHistoricRatios(quote, benchmarkQuote, missingIntervals, aggregation);
        int backendIndex = 0;
        for (int i = 0; i < result.size(); i++) {
            if (result.get(i) != null) {
                continue;
            }

            final BasicHistoricRatios bhr = backendData.get(backendIndex);
            result.set(i, bhr);

            final Key key = missingKeys.get(backendIndex);
            final Element element = new Element(key, bhr);
            this.basicCache.put(element);

            backendIndex++;
        }

        return result;
    }

    public List<ExtendedHistoricRatios> getExtendedHistoricRatios(SymbolQuote quote,
            SymbolQuote benchmarkQuote, List<Interval> intervals) {
        final List<ExtendedHistoricRatios> result = new ArrayList<>(intervals.size());

        final List<Interval> missingIntervals = new ArrayList<>(intervals.size());
        final List<Key> missingKeys = new ArrayList<>(intervals.size());

        final Long benchmarkQid = (benchmarkQuote != null) ? benchmarkQuote.getId() : null;

        for (final Interval interval : intervals) {
            final Key key = new Key(quote.getId(), benchmarkQid, interval, null);
            final Element ce = this.extendedCache.get(key);

            if (ce == null) {
                if (this.logger.isDebugEnabled()) {
                    this.logger.debug("<getExtendedHistoricRatios> not in cache: " + key);
                }

                missingIntervals.add(interval);
                missingKeys.add(key);
                result.add(null);
            }
            else {
                if (this.logger.isDebugEnabled()) {
                    this.logger.debug("<getExtendedHistoricRatios> found in cache: " + key);
                }

                result.add((ExtendedHistoricRatios) ce.getValue());
            }
        }

        if (missingIntervals.isEmpty()) {
            return result;
        }

        final List<ExtendedHistoricRatios> backendData =
                this.historicRatiosProvider.getExtendedHistoricRatios(quote, benchmarkQuote, missingIntervals);
        int backendIndex = 0;
        for (int i = 0; i < result.size(); i++) {
            if (result.get(i) != null) {
                continue;
            }

            final ExtendedHistoricRatios bhr = backendData.get(backendIndex);
            result.set(i, bhr);

            final Key key = missingKeys.get(backendIndex);
            final Element element = new Element(key, bhr);
            this.extendedCache.put(element);

            backendIndex++;
        }

        return result;
    }

    public List<CorporateAction> getCorporateActions(SymbolQuote quote, Interval interval,
            boolean withFactorizedDividends) {
        final DateTime startOfYear = new DateTime(interval.getStart().getYear(), 1, 1, 0, 0, 0, 0);
        final DateTime nextMidnight = interval.getEnd().plusDays(1).withTimeAtStartOfDay();
        final Interval requestInterval = new Interval(startOfYear, nextMidnight);

        final CAKey key = new CAKey(quote.getId(), requestInterval, withFactorizedDividends);

        final Element ce = this.caCache.get(key);
        final List<CorporateAction> values;
        if (ce != null) {
            values = (List<CorporateAction>) ce.getValue();
        }
        else {
            values = this.historicRatiosProvider.getCorporateActions(quote, requestInterval, withFactorizedDividends);
            this.caCache.put(new Element(key, values));
        }

        final List<CorporateAction> result = new ArrayList<>(values.size());

        for (final CorporateAction ca : values) {
            if (interval.contains(ca.getDate())) {
                result.add(ca);
            }
        }

        return result;
    }

    public PortfolioVaRLightResponse getPortfolioVaRLight(PortfolioVaRLightRequest request) {
        return this.historicRatiosProvider.getPortfolioVaRLight(request);
    }

    public List<BasicHistoricRatios> getPortfolioRatios(PortfolioRatiosRequest request) {
        return this.historicRatiosProvider.getPortfolioRatios(request);
    }

    public List<List<Price>> getHistoricPrices(List<SymbolQuote> quotes, List<LocalDate> dates) {
        return this.historicRatiosProvider.getHistoricPrices(quotes, dates);
    }

    public static class CAKey implements Serializable {
        protected static final long serialVersionUID = 1L;

        private final long quoteid;

        private final Interval interval;

        private final boolean withFactorizedDividends;

        public CAKey(long quoteid, Interval interval, boolean withFactorizedDividends) {
            this.quoteid = quoteid;
            this.interval = interval;
            this.withFactorizedDividends = withFactorizedDividends;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            final CAKey caKey = (CAKey) o;

            if (quoteid != caKey.quoteid) return false;
            if (withFactorizedDividends != caKey.withFactorizedDividends) return false;
            if (!interval.equals(caKey.interval)) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = (int) (quoteid ^ (quoteid >>> 32));
            result = 31 * result + interval.hashCode();
            result = 31 * result + (withFactorizedDividends ? 1 : 0);
            return result;
        }
    }

    public static class Key implements Serializable {
        protected static final long serialVersionUID = 1L;

        private final long quoteid;

        private final Long benchmarkId;

        private final YearMonthDay start;

        private final YearMonthDay end;

        private Double aggregation;

        public Key(long quoteid, Long benchmarkId, Interval interval, Double aggregation) {
            this.quoteid = quoteid;
            this.benchmarkId = benchmarkId;
            this.start = interval.getStart().toYearMonthDay();
            this.end = interval.getEnd().toYearMonthDay();
            this.aggregation = aggregation;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Key key = (Key) o;

            if (quoteid != key.quoteid) return false;
            if (aggregation != null ? !aggregation.equals(key.aggregation) : key.aggregation != null)
                return false;
            if (benchmarkId != null ? !benchmarkId.equals(key.benchmarkId) : key.benchmarkId != null)
                return false;
            if (end != null ? !end.equals(key.end) : key.end != null) return false;
            if (start != null ? !start.equals(key.start) : key.start != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = (int) (quoteid ^ (quoteid >>> 32));
            result = 31 * result + (benchmarkId != null ? benchmarkId.hashCode() : 0);
            result = 31 * result + (start != null ? start.hashCode() : 0);
            result = 31 * result + (end != null ? end.hashCode() : 0);
            result = 31 * result + (aggregation != null ? aggregation.hashCode() : 0);
            return result;
        }

        @Override
        public String toString() {
            return "Key{" +
                    "quoteid=" + quoteid +
                    ", benchmarkId=" + benchmarkId +
                    ", start=" + start +
                    ", end=" + end +
                    ", aggregation=" + aggregation +
                    '}';
        }
    }
}
