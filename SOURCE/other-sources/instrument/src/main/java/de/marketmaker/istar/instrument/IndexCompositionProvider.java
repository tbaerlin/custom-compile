/*
 * IndexCompositionProvider.java
 *
 * Created on 14.08.2006 08:20:40
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.instrument;

import de.marketmaker.istar.common.amqp.AmqpAddress;
import de.marketmaker.istar.domain.data.NamedIdSet;
import de.marketmaker.istar.domain.instrument.Instrument;

import java.util.List;
import java.util.Set;

/**
 * Provides methods for stock market index related queries.
 * <p>
 * A stock market index is a method of measuring a section of the stock market, which contains a selected
 * group of stocks. An index is denoted inside Istar as an <b>index composition</b>. Each stock inside
 * an index is represented by an <b>index position</b>.
 * <p>
 * In Istar an index composition is an instrument, which can be identified by either its instrument
 * id or quote id.
 * <p>
 * <b>Note that non-official indexes can be defined for customers. These indexes are identified within
 * Istar by (folder) names.</b>
 * <p>
 * <b><em>Document based on example codes, needs verification</em></b>
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
@AmqpAddress(queue = "istar.provider.indexcomposition")
public interface IndexCompositionProvider {

    /**
     * Queries index positions listed in the index identified by the given index quote id.
     *
     * @param quoteId an index' quote id.
     * @return a set of quote ids, which are listed in the index identified by the given quote id.
     * @deprecated
     */
    Set<Long> getIndexPositionsByQuoteid(long quoteId);

    /**
     * Queries index positions listed in the index identified by the given index quote id.
     *
     * @param quoteId an index' quote id
     * @return a set of instrument ids, which are listed in the index identified by the given quote id.
     * @deprecated
     */
    Set<Long> getIndexPositionIidsByQuoteid(long quoteId);

    /**
     * Queries index positions of a non-offical index identified by the given name.
     *
     * @param name the name of a non-official index (folder name) e.g. dax30ffm, lbbw_devise_europa etc.
     * @return a set of quote ids listed on that non-official index.
     * @deprecated
     */
    Set<Long> getIndexPositionsByName(String name);

    /**
     * Queries index positions of a non-official index identified by the given name.
     *
     * @param name the name of a non-official index (folder name) e.g. dax30ffm, lbbw_devise_europa etc.
     * @return A named id set, whose ids are quote ids contained in that non-official index.
     * @see de.marketmaker.istar.domain.data.NamedIdSet
     * @deprecated
     */
    NamedIdSet getIndexDefintionByName(String name);

    IndexCompositionResponse getIndexComposition(IndexCompositionRequest request);

    IndexMembershipResponse getIndexMembership(IndexMembershipRequest request);

    /**
     * Queries indexes in which the stocks identified by the given list of quote ids are listed.
     *
     * @param quoteIds a list of stock quote ids.
     * @return a set of index quote ids.
     * @deprecated use {@link #getIndexMembership(IndexMembershipRequest)}
     */
    Set<Long> getIndexQuoteidsForQuoteids(List<Long> quoteIds);

    /**
     * Queries the benchmark id for the given instrument.
     * <p>
     * Use this method if the IndexCompositionProvider is known to be a local component, so that
     * instrument does not have to be serialized
     *
     * @param instrument for which benchmark is requested
     * @return the quote id of the benchmark or null if none is available
     */
    Long getBenchmarkId(Instrument instrument);
}
