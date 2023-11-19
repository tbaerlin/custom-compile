/*
 * QuoteCategorizer.java
 *
 * Created on 12.05.2010 13:46:11
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.ratios.backend;

import de.marketmaker.istar.domain.instrument.Quote;

/**
 * @author zzhao
 */
public interface QuoteCategorizer {

    /**
     * @param quote a quote
     * @return a category the given quote belongs to.
     */
    QuoteCategory categorize(Quote quote);

    /**
     * @param symbol a symbol
     * @return a category the given symbol belongs to.
     */
    QuoteCategory categorize(String symbol);
}
