/*
 * BelgianQuoteSorter.java
 *
 * Created on 21.08.2012 16:40
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.instrument.export;

import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.domain.instrument.QuoteComparator;
import de.marketmaker.istar.instrument.IndexConstants;
import org.apache.lucene.search.SortField;

import java.util.Comparator;

/**
 * @author Markus Dick
 */
public class BelgianQuoteSorter extends ComparatorBasedQuoteSorter {
    public static final SortField SORT_FIELD
            = new SortField(IndexConstants.FIELDNAME_SORT_QUOTE_PREFER_BE, SortField.BYTE);

    @Override
    protected Comparator<Quote> getComparator() {
        return QuoteComparator.BELGIAN_SEARCH_QUOTE_COMPARATOR;
    }

    public SortField getSortField() {
        return SORT_FIELD;
    }
}