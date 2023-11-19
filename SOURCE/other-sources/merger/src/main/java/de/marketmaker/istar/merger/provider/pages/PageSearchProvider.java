package de.marketmaker.istar.merger.provider.pages;

import de.marketmaker.istar.common.amqp.AmqpAddress;

/**
 * PageSearchProvider.java
 * Created on 15.07.2010 15:15:39
 * Copyright (c) market maker Software AG. All Rights Reserved.
 * @author Sebastian Wild
 */
@AmqpAddress(queue = "istar.provider.pagesearch")
public interface PageSearchProvider {

    /**
     * Processes the given request to search pages fulfilling some
     * constraints. The request contains a lucene query.
     * The order of the returned list is by descending scores.
     * @param mergerRequest request to search pages
     * @return list of pages ({@link de.marketmaker.istar.merger.provider.pages.PageSummary}s)
     *         wrapped in MergerPageSearchResponse.
     */
    MergerPageSearchResponse searchPages(MergerPageSearchRequest mergerRequest);
}
