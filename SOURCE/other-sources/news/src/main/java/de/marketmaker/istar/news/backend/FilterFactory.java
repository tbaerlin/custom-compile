/*
 * FilterFactory.java
 *
 * Created on 21.06.2007 13:54:13
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.news.backend;

import java.io.IOException;

import org.apache.lucene.search.Filter;

import de.marketmaker.istar.news.frontend.NewsRequestBase;

/**
 * Encapsulates how filters are used for different types of indexed news (e.g., vwd, ots)
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public interface FilterFactory {
    /**
     * Returns a filter for the given request
     * @param index to be searched
     * @param request specifies search
     * @return filter
     */
    Filter getFilter(NewsIndex index, NewsRequestBase request) throws IOException;
}
