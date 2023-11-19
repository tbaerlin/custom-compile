/*
 * NewsDao.java
 *
 * Created on 15.03.2007 11:38:51
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.news.backend;

import java.util.List;

import org.joda.time.DateTime;

import de.marketmaker.istar.news.frontend.NewsRecord;
import de.marketmaker.istar.news.data.NewsRecordImpl;

/**
 * Stores and retrieves NewsRecordImpl objects.
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public interface NewsDao {

    /**
     * Stores a NewsRecordImpl for later retrieval.
     * @param item to be stored
     * @return true iff successful (false might indicate a unique key violation etc).
     */
    boolean insertItem(NewsRecordImpl item);

    /**
     * Retrieves an ordered collection of NewsItems. The returned list should
     * contain <tt>null</tt>-values for invalid keys, so that the size of the parameter list and the
     * returned list are always equal.
     *
     * @param ids list of ids for news to be retrieved
     * @param withText whether news text should be retrieved with the news
     * @param withRawText whether raw text should be uncompressed and returned in NewsRecords
     * @return ordered list of retrieved news items that correspond to the given ids
     */
    List<NewsRecord> getItems(List<String> ids, boolean withText, boolean withRawText);

    /**
     * Deletes all news with the given ids
     * @param ids keys of news to be deleted
     * @return number of deleted news
     */
    int deleteItems(List<String> ids);

    /**
     * @param dt returns ids since
     * @return all news ids on or after dt
     */
    List<String> getIdsSince(DateTime dt);

    /**
     * @param from returns ids since (incl.)
     * @param to returns ids to (excl.)
     * @return all news ids between from and to
     */
    List<String> getIdsFromTo(DateTime from, DateTime to);

    /**
     * Retrieves all news from the database and forwards them to the given handler for processing.
     * Since there are millions of news per year, it would not be feasible to return a List of
     * all news.<p>
     * <b>Important</b> The table will be locked for the entire duration of this call, so do not
     * use this method on a live production system.
     * @param handler will be invoked for each news item
     * @param limit limits the number of results if &gt; 0;
     * @param withRawText whether raw text should be uncompressed and returned in NewsRecords (seems to be always false)
     */
    void getAllItems(NewsRecordHandler handler, int limit, boolean withRawText);

}
