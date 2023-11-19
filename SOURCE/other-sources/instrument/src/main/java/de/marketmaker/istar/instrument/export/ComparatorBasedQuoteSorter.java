/*
 * ComparatorQuoteSorter.java
 *
 * Created on 12.05.2010 09:42:37
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.instrument.export;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.marketmaker.istar.domain.instrument.Instrument;
import de.marketmaker.istar.domain.instrument.Quote;

/**
 * @author oflege
 */
public abstract class ComparatorBasedQuoteSorter implements QuoteSorter {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    // avoid expensive equals(..) calls by using IdentityHashMap
    private final Map<Quote, Integer> orders = new IdentityHashMap<>();

    public void prepare(Instrument instrument) {
        this.orders.clear();

        final List<Quote> quotes = new ArrayList<>(instrument.getQuotes());
        if (quotes.size() > 127) {
            // if this is encountered, we must use SortField.SHORT in this class and its subclasses
            this.logger.error("<prepare> too many quotes in " + instrument);
        }
        quotes.sort(getComparator());
        for (int i = 0; i < quotes.size(); ) {
            final Quote quote = quotes.get(i);
            this.orders.put(quote, Math.min(++i, 127));
        }
    }

    public int getOrder(Quote quote) {
        return this.orders.get(quote);
    }

    protected abstract Comparator<Quote> getComparator();
}
