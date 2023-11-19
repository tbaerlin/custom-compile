/*
 * NewsServer.java
 *
 * Created on 15.03.2007 12:15:59
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.news.frontend;

import de.marketmaker.istar.common.amqp.AmqpAddress;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
@AmqpAddress(queue = "istar.news")
public interface NewsServer {

    /**
     * Performs a lucene search and return a set of matching news items.
     * @param request contains a lucene query and user profile (includes entitlements)
     * @return a set of news
     */
    NewsResponse getNews(NewsRequest request);

    /**
     * Returns the latest news for each item in a given list of iids (if any news is available
     * for that item).
     * @param request specifies query/iids, profile (includes entitlements)
     * @return latest news
     */
    NewsResponse getNews(LatestNewsRequest request);
}
