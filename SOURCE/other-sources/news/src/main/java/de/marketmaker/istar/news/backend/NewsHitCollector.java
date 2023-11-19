/*
 * NewsHitCollector.java
 *
 * Created on 11.04.2007 11:16:35
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.news.backend;

import java.io.IOException;
import java.util.List;

/**
 * A strategy for collecting news hits.
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public interface NewsHitCollector {
    /**
     * @return true iff this collector can collect more documents.
     */
    boolean canCollectMore();

    /**
     * Called for an additional hit document, if a previous call of {@link #canCollectMore()}
     * returned true;
     * @param indexId id of index that contains the document
     * @param documentId id of hit document
     */
    void add(int indexId, int documentId) throws IOException;

    /**
     * Used to retrieve the collected news ids
     * @return collected ids
     */
    List<String> getIds();

    /**
     * Returns the id of the news right after the returned ids, can be used for paging
     * @return id of first news on next news page, null if not enough hits for next page
     */
    String getNextPageRequestId();

    /**
     * Returns the id of the news that would start the previous news page if that page would
     * have as many items as the current page
     * @return id of first news on previous page, null if no previous hits
     */
    String getPrevPageRequestId();
}
