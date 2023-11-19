/*
 * CachingHighLowProvider.java
 *
 * Created on 03.10.2006 18:14:06
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider;

import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.io.Serializable;

import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.joda.time.Interval;
import org.joda.time.YearMonthDay;

import de.marketmaker.istar.common.util.DateUtil;
import de.marketmaker.istar.domain.data.HighLow;
import de.marketmaker.istar.domain.data.Price;
import de.marketmaker.istar.domain.data.PriceRecord;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.domainimpl.data.HighLowImpl;
import de.marketmaker.istar.domainimpl.data.NullHighLow;
import de.marketmaker.istar.domainimpl.data.NullPrice;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class CachingHighLowProvider implements HighLowProvider {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private Ehcache cache;

    private HighLowProvider highLowProvider;

    public void setCache(Ehcache cache) {
        this.cache = cache;
    }

    public void setHighLowProvider(HighLowProvider highLowProvider) {
        this.highLowProvider = highLowProvider;
    }

    public List<List<HighLow>> getHighLows(List<SymbolQuote> quotes, List<Interval> intervals) {
        final List<List<HighLow>> result = new ArrayList<>(quotes.size());
        for (final SymbolQuote quote : quotes) {
            result.add(getHighLow(quote, intervals));
        }
        return result;
    }

    public HighLow getHighLow52W(Quote quote, PriceRecord pr) {
        return getHighLows52W(Collections.singletonList(quote), Collections.singletonList(pr)).get(0);
    }

    public List<HighLow> getHighLows52W(List<Quote> quotes, List<PriceRecord> prs) {
        final List<HighLow> result = new ArrayList<>(quotes.size());
        final Interval interval = DateUtil.getInterval("P1Y");

        final List<SymbolQuote> symbolQuotes = new ArrayList<>();

        for (int i = 0; i < prs.size(); i++) {
            final PriceRecord record = prs.get(i);
            final Price p = record.getHigh52W();
            if (p != NullPrice.INSTANCE) {
                result.add(new HighLowImpl(interval, p, record.getLow52W()));
            }
            else {
                result.add(null);
                symbolQuotes.add(SymbolQuote.create(quotes.get(i)));
            }
        }

        if (!symbolQuotes.isEmpty()) {
            final List<List<HighLow>> highLows = getHighLows(symbolQuotes, Collections.singletonList(interval));
            int j = 0;
            for (int i = 0; i < prs.size(); i++) {
                if (result.get(i) == null) {
                    final HighLow hl = highLows.get(j++).get(0);
                    result.set(i, hl.copy(prs.get(i)));
                }
            }
        }

        return result;
    }

    public List<HighLow> getHighLow(SymbolQuote quote, List<Interval> intervals) {
        if(quote==null) {
            return Collections.nCopies(intervals.size(), NullHighLow.INSTANCE);
        }
        
        final List<HighLow> result = new ArrayList<>(intervals.size());

        final List<Interval> missingIntervals = new ArrayList<>(intervals.size());
        final List<Key> missingKeys = new ArrayList<>(intervals.size());

        for (final Interval interval : intervals) {
            final Key key = new Key(quote.getId(), interval);
            final Element ce = this.cache.get(key);

            if (ce == null) {
                if (this.logger.isDebugEnabled()) {
                    this.logger.debug("<getHighLow> not in cache: " + key);
                }

                missingIntervals.add(interval);
                missingKeys.add(key);
                result.add(null);
            }
            else {
                if (this.logger.isDebugEnabled()) {
                    this.logger.debug("<getHighLow> found in cache: " + key);
                }

                result.add((HighLow) ce.getValue());
            }
        }

        if (missingIntervals.isEmpty()) {
            return result;
        }

        final List<HighLow> backendData = this.highLowProvider.getHighLow(quote, missingIntervals);
        int backendIndex = 0;
        for (int i = 0; i < result.size(); i++) {
            if (result.get(i) != null) {
                continue;
            }

            final HighLow highLow = backendData.get(backendIndex);
            result.set(i, highLow);

            final Key key = missingKeys.get(backendIndex);
            final Element element = new Element(key, highLow);
            this.cache.put(element);

            backendIndex++;
        }

        return result;
    }

    public static class Key implements Serializable {
        protected static final long serialVersionUID = 1L;

        private final long quoteid;
        // todo: replace with int values (yyyymmdd)
        private final YearMonthDay start;
        private final YearMonthDay end;

        public Key(long quoteid, Interval interval) {
            this.quoteid = quoteid;
            this.start = interval.getStart().toYearMonthDay();
            this.end = interval.getEnd().toYearMonthDay();
        }

        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            final Key key = (Key) o;

            if (quoteid != key.quoteid) {
                return false;
            }
            if (!end.equals(key.end)) {
                return false;
            }
            return start.equals(key.start);
        }

        public int hashCode() {
            int result;
            result = (int) (quoteid ^ (quoteid >>> 32));
            result = 29 * result + start.hashCode();
            result = 29 * result + end.hashCode();
            return result;
        }

        public String toString() {
            return "Key[qid=" + quoteid
                    + ", start=" + start
                    + ", end=" + end
                    + "]";
        }
    }}
