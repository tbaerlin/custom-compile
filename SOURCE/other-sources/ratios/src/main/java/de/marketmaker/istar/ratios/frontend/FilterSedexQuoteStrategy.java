/*
 * FilterSedexQuoteStrategy.java
 *
 * Created on 6/16/14 9:21 AM
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.ratios.frontend;

import de.marketmaker.istar.domain.MarketcategoryEnum;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Stefan Willenbrock
 */
public class FilterSedexQuoteStrategy extends PreferSedexQuoteStrategy {
    public QuoteRatios select(QuoteRatios[] records) {
        return getQuote(records);
    }

    public Type getType() {
        return Type.FILTER_SEDEX_QUOTE;
    }
}
