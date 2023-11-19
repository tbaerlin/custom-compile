/*
 * MarketManagerQuoteSorter.java
 *
 * Created on 20.08.2009 11:58:09
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.instrument.export;

import java.util.Comparator;

import org.apache.lucene.search.SortField;

import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.domain.instrument.QuoteComparator;
import de.marketmaker.istar.instrument.IndexConstants;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class MarketManagerQuoteSorter extends ComparatorBasedQuoteSorter {
    public static final SortField SORT_FIELD
            = new SortField(IndexConstants.FIELDNAME_SORT_QUOTE_DEFAULT, SortField.BYTE);

    @Override
    protected Comparator<Quote> getComparator() {
        return QuoteComparator.MARKET_MANAGER_SEARCH_QUOTE_COMPARATOR;
    }

    public SortField getSortField() {
        return SORT_FIELD;
    }
}
