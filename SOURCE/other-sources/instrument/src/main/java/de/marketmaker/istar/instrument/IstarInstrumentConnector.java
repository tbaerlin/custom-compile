/*
 * IstarFeedConnector.java
 *
 * Created on 17.02.2005 13:44:44
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.instrument;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.marketmaker.istar.instrument.search.SearchMetaRequest;
import de.marketmaker.istar.instrument.search.SearchMetaResponse;
import de.marketmaker.istar.instrument.search.SearchRequest;
import de.marketmaker.istar.instrument.search.SearchResponse;

/**
 * An i-star instrument connector serves as a client side proxy to an instrument server and makes
 * sure that each request is processed and answered with a response:
 * <ul>
 * <li>an invalid request(e.g. null request) results in a response with invalid flag set to true</li>
 * <li>if any exceptions occurred during the underlying instrument server's processing of a valid request,
 * the exceptions would be caught by this connector and the client gets back an response with invalid
 * flag set to true</li>
 * <li>in case of valid request and successful processing a valid response would be handed back</li>
 * </ul>
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 * @see de.marketmaker.istar.instrument.InstrumentServer
 * @see de.marketmaker.istar.instrument.IndexCompositionProvider
 */
public class IstarInstrumentConnector {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private InstrumentServer instrumentServer;

    public IstarInstrumentConnector() {
    }

    public void setInstrumentServer(InstrumentServer instrumentServer) {
        this.instrumentServer = instrumentServer;
    }

    /**
     * Please refer to {@link de.marketmaker.istar.instrument.InstrumentServer#identifyNew(InstrumentRequest)}
     */
    public InstrumentResponse identify(InstrumentRequest request) {
        if (this.instrumentServer == null) {
            final InstrumentResponse response = new InstrumentResponse();
            response.setInvalid();
            return response;
        }

        try {
            return this.instrumentServer.identifyNew(request);
        }
        catch (Exception e) {
            this.logger.error("<identify> failed", e);

            final InstrumentResponse result = new InstrumentResponse();
            result.setInvalid();
            return result;
        }
    }

    /**
     * Please refer to {@link de.marketmaker.istar.instrument.InstrumentServer#searchNew(de.marketmaker.istar.instrument.search.SearchRequest)}
     */
    public SearchResponse search(SearchRequest request) {
        if (this.instrumentServer == null) {
            final SearchResponse response = new SearchResponse();
            response.setInvalid();
            return response;
        }

        try {
            return this.instrumentServer.searchNew(request);
        }
        catch (Exception e) {
            this.logger.error("<search> failed", e);

            final SearchResponse result = new SearchResponse();
            result.setInvalid();
            return result;
        }
    }

    /**
     * Please refer to {@link de.marketmaker.istar.instrument.InstrumentServer#simpleSearchNew(de.marketmaker.istar.instrument.search.SearchRequest)}
     */
    public SearchResponse simpleSearch(SearchRequest request) {
        if (this.instrumentServer == null) {
            final SearchResponse response = new SearchResponse();
            response.setInvalid();
            return response;
        }

        try {
            return this.instrumentServer.simpleSearchNew(request);
        }
        catch (Exception e) {
            this.logger.error("<simpleSearch> failed", e);

            final SearchResponse result = new SearchResponse();
            result.setInvalid();
            return result;
        }
    }

    /**
     * Please refer to {@link de.marketmaker.istar.instrument.InstrumentServer#getMetaData(de.marketmaker.istar.instrument.search.SearchMetaRequest)}
     */
    public SearchMetaResponse getMetaData(SearchMetaRequest request) {
        if (this.instrumentServer == null) {
            final SearchMetaResponse response = new SearchMetaResponse();
            response.setInvalid();
            return response;
        }

        try {
            return this.instrumentServer.getMetaData(request);
        }
        catch (Exception e) {
            this.logger.error("<simpleSearch> failed", e);

            final SearchMetaResponse result = new SearchMetaResponse();
            result.setInvalid();
            return result;
        }
    }
}
