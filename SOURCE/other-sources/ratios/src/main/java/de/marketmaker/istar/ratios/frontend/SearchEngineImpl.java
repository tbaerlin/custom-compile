/*
 * SearchEngineImpl.java
 *
 * Created on 26.10.2005 10:35:49
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.ratios.frontend;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.marketmaker.istar.common.util.TimeTaker;
import de.marketmaker.istar.domainimpl.rating.RatingSystemProvider;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class SearchEngineImpl implements RatioSearchEngine {
    public static final long SLOW_QUERY_THRESHOLD = 2000L;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private RatioDataRepository repository;

    private RatingSystemProvider ratingSystemProvider;

    public void setRepository(RatioDataRepository repository) {
        this.repository = repository;
    }

    public void setRatingSystemProvider(RatingSystemProvider ratingSystemProvider) {
        this.ratingSystemProvider = ratingSystemProvider;
    }

    @Override
    public RatioSearchResponse search(RatioSearchRequest request) {
        final TimeTaker tt = new TimeTaker();

        final SearchParameterParser spp;
        final RatioSearchResponse sr;
        try {
            spp = new SearchParameterParser(request, this.ratingSystemProvider);
            sr = this.repository.search(spp);
        } catch (Exception e) {
            this.logger.error("<search> failed for " + request, e);
            return null;
        }

        tt.stop();
        if (tt.getElapsedMs() > SLOW_QUERY_THRESHOLD) {
            this.logger.warn("<search> slow for " + spp.getSelector()
                    + " in " + request.getType() + ", took " + tt);
        }
        else if (this.logger.isDebugEnabled()) {
            this.logger.debug("<search> for " + spp.getSelector()
                    + " in " + request.getType() + ", took " + tt);
        }

        return sr;
    }

    public RatioSearchMetaResponse getMetaData(RatioSearchMetaRequest request) {
        final TimeTaker tt = new TimeTaker();

        final RatioSearchMetaResponse smr =
                this.repository.getMetaData(request);

        if (this.logger.isDebugEnabled()) {
            this.logger.debug("<getMetaData> took " + tt + " for " + request.getType().name());
        }

        return smr;
    }
}
