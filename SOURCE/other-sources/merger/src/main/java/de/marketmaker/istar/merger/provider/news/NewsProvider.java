/*
 * NewsProvider.java
 *
 * Created on 17.07.2006 10:07:24
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.news;

import de.marketmaker.istar.news.frontend.NewsRequest;
import de.marketmaker.istar.news.frontend.NewsResponse;
import de.marketmaker.istar.news.frontend.LatestNewsRequest;


/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public interface NewsProvider {

    NewsResponse getNews(NewsRequest nr, boolean withInstruments);

    NewsResponse getLatestNews(LatestNewsRequest request, boolean withInstruments);
}