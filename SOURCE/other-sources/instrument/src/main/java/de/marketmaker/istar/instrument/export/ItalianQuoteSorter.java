/*
 * MarketManagerQuoteSorter.java
 *
 * Created on 20.08.2009 11:58:09
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.instrument.export;

import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.domain.instrument.QuoteComparator;
import de.marketmaker.istar.instrument.IndexConstants;
import org.apache.lucene.search.SortField;

import java.util.Comparator;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class ItalianQuoteSorter extends ComparatorBasedQuoteSorter {
    public static final SortField SORT_FIELD
            = new SortField(IndexConstants.FIELDNAME_SORT_QUOTE_PREFER_IT, SortField.BYTE);

    @Override
    protected Comparator<Quote> getComparator() {
        return QuoteComparator.ITALIAN_SEARCH_QUOTE_COMPARATOR;
    }

    public SortField getSortField() {
        return SORT_FIELD;
    }
}