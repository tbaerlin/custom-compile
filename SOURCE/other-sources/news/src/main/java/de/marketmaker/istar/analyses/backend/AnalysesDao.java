/*
 * AnalysesDao.java
 *
 * Created on 22.03.12 10:22
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.analyses.backend;

import java.util.List;

import org.joda.time.DateTime;

import de.marketmaker.istar.analyses.frontend.AnalysisImpl;

/**
 * @author oflege
 */
public interface AnalysesDao {

    /**
     * Stores an analysis for later retrieval.
     * @param analysis to be stored
     * @return true iff successful (false might indicate a unique key violation etc).
     */
    boolean insertAnalysis(Protos.Analysis analysis);

    /**
     * Retrieves an ordered collection of NewsItems. The returned list should
     * contain <tt>null</tt>-values for invalid keys, so that the size of the paramter list and the
     * returned list are always equal.
     *
     *
     * @param ids list of ids for news to be retrieved
     * @return ordered list of retrieved news items that correspond to the given ids
     */
    List<AnalysisImpl> getItems(Protos.Analysis.Provider provider, List<Long> ids);

    /**
     * Deletes all news with the given ids
     *
     * @param ids keys of news to be deleted
     * @return number of deleted news
     */
    int deleteItems(Protos.Analysis.Provider provider, List<Long> ids);

    /**
     * Retrieve a list of analysis ids
     *
     * @param dt limit ids to after this DateTime value
     * @param provider limit the ids to a specific provider
     * @return all news ids at or after dt
     */
    List<Long> getIdsSince(Protos.Analysis.Provider provider, DateTime dt);

    /**
     * Retrieves all news from the database and forwards them to the given handler for processing.
     * Since there are millions of news per year, it would not be feasible to return a List of
     * all news.<p>
     * <b>Important</b> The table will be locked for the entire duration of this call, so do not
     * use this method on a live production system.
     *
     * @param provider the id of the provider
     * @param handler will be invoked for each news item
     */
    void getAllItems(Protos.Analysis.Provider provider, AnalysisHandler handler);

    /**
     * Stores an image which is associated with an analysis
     *
     * @param analysis id of corresponding analysis
     * @param name used to retrieve image
     * @param data encoded image data
     * @return true iff successful (false might indicate a unique key violation etc).
     */
    boolean insertImage(Protos.Analysis.Provider provider, long analysis, String name, byte[] data);

    /**
     * Retrieves image data
     *
     * @param provider the id of the provider
     * @param id used when image was stored
     * @return stored image or null if not available
     */
    byte[] getImage(Protos.Analysis.Provider provider, String id);

}
