/*
 * IstarSearchEngineConnector.java
 *
 * Created on 27.10.2005 17:18:06
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.ratios.frontend;

import de.marketmaker.istar.ratios.opra.OpraRatioSearchResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.marketmaker.istar.ratios.opra.OpraSearchEngine;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class IstarSearchEngineConnector {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private RatioSearchEngine ratioSearchEngine;

    private OpraSearchEngine opraSearchEngine;

    private static final RatioSearchResponse INVALID_RATIO_SEARCH_RESPONSE = new RatioSearchResponse() {

        public String getServerInfo() {
            return null;
        }

        public boolean isValid() {
            return false;
        }
    };

    public void setSearchEngine(SearchEngine searchEngine) {
        setRatioSearchEngine(searchEngine);
        setOpraSearchEngine(searchEngine);
    }

    public void setRatioSearchEngine(RatioSearchEngine ratioSearchEngine) {
        this.ratioSearchEngine = ratioSearchEngine;
    }

    public void setOpraSearchEngine(OpraSearchEngine opraSearchEngine) {
        this.opraSearchEngine = opraSearchEngine;
    }

    public RatioSearchResponse search(RatioSearchRequest request) {
        if (this.ratioSearchEngine == null) {
            return INVALID_RATIO_SEARCH_RESPONSE;
        }

        try {
            final RatioSearchResponse sr = this.ratioSearchEngine.search(request);
            return (sr != null) ? sr : INVALID_RATIO_SEARCH_RESPONSE;
        }
        catch (Throwable t) {
            this.logger.error("<search> failed", t);
            return INVALID_RATIO_SEARCH_RESPONSE;
        }
    }

    public OpraRatioSearchResponse getOpraItems(RatioSearchRequest request) {
        return this.opraSearchEngine.getOpraItems(request);
    }

    public MatrixMetadataRatioSearchResponse getOpraMatrix(RatioSearchRequest request) {
        return this.opraSearchEngine.getOpraMatrix(request);
    }

    public RatioSearchMetaResponse getOpraMetaData() {
        return this.opraSearchEngine.getOpraMetaData();
    }

    public RatioSearchMetaResponse getMetaData(RatioSearchMetaRequest request) {
        if (this.ratioSearchEngine == null) {
            return getInvalidMetaResponse();
        }

        try {
            return this.ratioSearchEngine.getMetaData(request);
        }
        catch (Throwable t) {
            this.logger.error("<getMetaData> failed", t);
            return getInvalidMetaResponse();
        }
    }

    private RatioSearchMetaResponse getInvalidMetaResponse() {
        final RatioSearchMetaResponse result = new RatioSearchMetaResponse();
        result.setInvalid();
        return result;
    }
}
