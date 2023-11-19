/*
 * AbstractFilterFactory.java
 *
 * Created on 21.06.2007 13:56:57
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.news.backend;

import org.apache.lucene.search.Filter;
import org.apache.lucene.search.NumericRangeFilter;
import org.joda.time.DateTime;

import de.marketmaker.istar.news.frontend.NewsRequestBase;

import static de.marketmaker.istar.news.frontend.NewsIndexConstants.FIELD_TIMESTAMP;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public abstract class AbstractFilterFactory implements FilterFactory {

    protected Filter createTimestampFilter(NewsIndex index, NewsRequestBase request) {
        if (!isPartialPeriodRequest(index, request)) {
            return null;
        }
        return createTimestampFilter(request.getFrom(), request.getTo());
    }

    private boolean isPartialPeriodRequest(NewsIndex index, NewsRequestBase request) {
        return (request.getFrom() != null && request.getFrom().isAfter(index.getFrom()))
                || (request.getTo() != null && request.getTo().isBefore(index.getTo()));
    }

    protected Filter createTimestampFilter(final DateTime from, final DateTime to) {
        final int encodedFrom = News2Document.encodeTimestamp(from, 0);
        final int encodedTo = News2Document.encodeTimestamp(to, Integer.MAX_VALUE);

        return NumericRangeFilter.newIntRange(FIELD_TIMESTAMP, encodedFrom, encodedTo, true, true);
    }
}
