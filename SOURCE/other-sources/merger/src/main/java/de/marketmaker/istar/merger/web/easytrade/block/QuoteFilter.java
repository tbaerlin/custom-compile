/*
 * QuoteFilter.java
 *
 * Created on 25.11.2009 14:46:02
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.block;

import java.io.Serializable;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import de.marketmaker.istar.domain.instrument.Quote;

/**
 * @author oflege
 */
public interface QuoteFilter extends Predicate<Quote>, Serializable {
    /**
     * Filters the given quotes
     * @param quotes source list, implementations should never try to modify this list directly.
     * @return filtered quotes
     */
    default List<Quote> apply(List<Quote> quotes) {
        return quotes.stream().filter(this).collect(Collectors.toList());
    }
}
