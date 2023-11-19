/*
 * InstrumentServer.java
 *
 * Created on 22.12.2004 14:04:03
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.instrument;

import de.marketmaker.istar.common.amqp.AmqpAddress;
import de.marketmaker.istar.instrument.search.SearchMetaRequest;
import de.marketmaker.istar.instrument.search.SearchMetaResponse;
import de.marketmaker.istar.instrument.search.SearchRequest;
import de.marketmaker.istar.instrument.search.SearchResponse;
import de.marketmaker.istar.instrument.search.SuggestRequest;
import de.marketmaker.istar.instrument.search.SuggestResponse;
import de.marketmaker.istar.instrument.search.ValidationRequest;
import de.marketmaker.istar.instrument.search.ValidationResponse;

/**
 * Provides basic instrument related query methods.
 * <p>
 * An instrument server takes some specific instrument related requests and produces corresponding
 * instrument related responses. To facilitate client side usage, some convenient higher level
 * interfaces are provided too. Please refer to: {@link de.marketmaker.istar.instrument.IstarInstrumentConnector}
 * and InstrumentProvider.
 * <p>
 * To find instrument(s), follow steps:
 * <ul>
 * <li>known instrument id(s), create an {@link de.marketmaker.istar.instrument.InstrumentRequest}
 * and identify the instrument(s) directly using {@link #identifyNew(InstrumentRequest)}</li>
 * <li>unknown instrument id(s), but known vwd code, vwd feed or other relevant instrument/quote fields
 * , create a {@link de.marketmaker.istar.instrument.search.SearchRequest} and search the instrument(s)
 * using {@link #searchNew(de.marketmaker.istar.instrument.search.SearchRequest)}</li>
 * <li>unknown instrument id(s), unknown relevant instrument/quote fields, but known instrument name or
 * part of it, create a {@link de.marketmaker.istar.instrument.search.SearchRequest} and search the
 * instrument(s) using {@link #simpleSearchNew(de.marketmaker.istar.instrument.search.SearchRequest)}</li>
 * <li>known instrument name or part of it, one can also get a list of suggested instruments through
 * {@link #getSuggestions(de.marketmaker.istar.instrument.search.SuggestRequest)} by creating and sending
 * a {@link de.marketmaker.istar.instrument.search.SuggestRequest}</li>
 * </ul>
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
@AmqpAddress(queue = "istar.instrument")
public interface InstrumentServer {
    /**
     * Queries instruments through instrument ids specified in the given request.
     * <p>
     * If for a given instrument id no instrument can be found, the instrument with the same position
     * as specified in the instrument request would be null in the returned instrument response.
     *
     * @param ir an instrument request
     * @return an instrument response
     * @see de.marketmaker.istar.instrument.InstrumentRequest
     * @see de.marketmaker.istar.instrument.InstrumentResponse
     */
    InstrumentResponse identifyNew(InstrumentRequest ir);

    InstrumentResponse identify(InstrumentRequest ir);

    /**
     * Queries instruments through one or more specific relevant instrument/quote fields, like vwd
     * feed, vwd code etc. For more descriptions please refer to
     * {@link de.marketmaker.istar.instrument.search.SearchRequest}
     * <p>
     * Unlike {@link #simpleSearchNew(de.marketmaker.istar.instrument.search.SearchRequest)} an exact
     * match is expected.
     *
     * @param ir a search request
     * @return a search response
     * @see de.marketmaker.istar.instrument.search.SearchRequest
     * @see de.marketmaker.istar.instrument.search.SearchResponse
     */
    SearchResponse searchNew(SearchRequest ir);

    SearchResponse search(SearchRequest ir);

    /**
     * Queries instruments through relaxed search criteria.
     * <p>
     * Unlike {@link #searchNew(de.marketmaker.istar.instrument.search.SearchRequest)} an exact
     * match is not required.
     * <p>
     * XXX: should we differentiate the two request types for search and simple search??
     *
     * @param ir a search request
     * @return a search response
     * @see de.marketmaker.istar.instrument.search.SearchRequest
     * @see de.marketmaker.istar.instrument.search.SearchResponse
     */
    SearchResponse simpleSearchNew(SearchRequest ir);

    SearchResponse simpleSearch(SearchRequest ir);

    /**
     * Queries meta-data of this instrument server.
     * <p>
     * XXX: do we really need a request??
     *
     * @param ir a meta-data search request
     * @return a response with this instrument server's meta-data
     */
    SearchMetaResponse getMetaData(SearchMetaRequest ir);

    /**
     * Queries suggestions for instruments a user may be looking for. Usually, this method will be
     * called with a very short query string (even 1 character is possible) and is expected to
     * return the "best" suggestions, where "best" can be controlled by specifying a strategy
     * in the request.
     * <p>
     * For detailed description of how to specify the suggest request, please refer to
     * {@link de.marketmaker.istar.instrument.search.SuggestRequest}.
     *
     * @param request a suggest request
     * @return suggestions a suggest response
     * @see de.marketmaker.istar.instrument.search.SuggestRequest
     * @see de.marketmaker.istar.instrument.search.SuggestResponse
     */
    SuggestResponse getSuggestions(SuggestRequest request);

    /**
     * Used to identify invalid iids. If a service maintains data by iid but cannot be sure for which
     * iids Instrument objects are actually available from the InstrumentServer, this method should
     * be used to check the validity of the iids. Iid availability should never be checked with
     * search requests as those would take too much time.
     * @param request contains iids to be validated
     * @return validation result with invalid iids
     */
    ValidationResponse validate(ValidationRequest request);
}
