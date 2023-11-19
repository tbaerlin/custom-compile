/*
 * AnalysesServerImpl.java
 *
 * Created on 21.03.12 09:26
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.analyses.backend;

import java.io.IOException;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.joda.time.DateTime;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;

import de.marketmaker.istar.analyses.frontend.AnalysesMetaRequest;
import de.marketmaker.istar.analyses.frontend.AnalysesMetaResponse;
import de.marketmaker.istar.analyses.frontend.AnalysesRequest;
import de.marketmaker.istar.analyses.frontend.AnalysesServer;
import de.marketmaker.istar.analyses.frontend.AnalysesSummaryResponse;
import de.marketmaker.istar.analyses.frontend.AnalysisImageRequest;
import de.marketmaker.istar.analyses.frontend.AnalysisImageResponse;
import de.marketmaker.istar.analyses.frontend.AnalysisResponse;
import de.marketmaker.istar.common.util.TimeTaker;
import de.marketmaker.istar.domain.profile.Selector;

/**
 * @author oflege
 */
@ManagedResource
public class AnalysesServerImpl implements AnalysesServer {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private final Map<Selector, AnalysesProvider> providerMap = new EnumMap<>(Selector.class);

    public void setProviders(Iterable<AnalysesProvider> providers) {
        for (AnalysesProvider provider : providers) {
            this.providerMap.put(provider.getSelector(), provider);
        }
    }

    private AnalysesProvider getProvider(Selector s) {
        return this.providerMap.get(s);
    }

    @ManagedOperation
    public void syncWithBackup() {
        final DateTime from = new DateTime().minusYears(2);
        for (AnalysesProvider provider : this.providerMap.values()) {
            provider.syncWithBackup(from);
        }
    }

    @ManagedOperation
    public void midnight() {
        for (Entry<Selector, AnalysesProvider> entry : this.providerMap.entrySet()) {
            try {
                entry.getValue().midnight();
            } catch (Exception e) {
                this.logger.error("<midnight> failed for " + entry.getKey(), e);
            }
        }
    }

    @Override
    public AnalysesMetaResponse getMetaData(AnalysesMetaRequest request) {
        try {
            return doGetMetaData(request);
        } catch (Exception e) {
            this.logger.error("<getMetaData> failed", e);
            AnalysesMetaResponse result = new AnalysesMetaResponse(null);
            result.setInvalid();
            return result;
        }
    }

    private AnalysesMetaResponse doGetMetaData(AnalysesMetaRequest request) {
        final AnalysesProvider provider = getProvider(request.getSelector());
        if (provider == null) {
            return new AnalysesMetaResponse(Collections.emptyMap());
        }
        try {
            return new AnalysesMetaResponse(provider.getMetaData(request));
        } catch (IOException e) {
            this.logger.error("<doGetMetaData> failed", e);
            return new AnalysesMetaResponse(null);
        }
    }

    @Override
    public AnalysesSummaryResponse getSummary(AnalysesRequest request) {
        try {
            return doGetSummary(request);
        } catch (Exception e) {
            this.logger.error("<getSummary> failed", e);
            AnalysesSummaryResponse result = new AnalysesSummaryResponse();
            result.setInvalid();
            return result;
        }
    }

    private AnalysesSummaryResponse doGetSummary(AnalysesRequest request) {
        final AnalysesProvider provider = getProvider(request.getSelector());
        if (provider == null) {
            return invalidSummaryResponse();
        }

        try {
            return provider.getSummaries(request);
        } catch (IOException e) {
            this.logger.error("<doGetSummary> failed", e);
            return invalidSummaryResponse();
        }
    }

    private AnalysesSummaryResponse invalidSummaryResponse() {
        final AnalysesSummaryResponse result = new AnalysesSummaryResponse();
        result.setInvalid();
        return result;
    }

    @Override
    public AnalysisResponse getAnalyses(AnalysesRequest request) {
        try {
            return doGetAnalyses(request);
        } catch (Exception e) {
            this.logger.error("<getAnalyses> failed", e);
            AnalysisResponse result = new AnalysisResponse();
            result.setInvalid();
            return result;
        }
    }

    @Override
    public AnalysisImageResponse getImage(AnalysisImageRequest request) {
        final AnalysesProvider provider = getProvider(request.getSelector());
        if (provider == null) {
            return new AnalysisImageResponse(null);
        }

        return provider.getImage(request);
    }

    private AnalysisResponse doGetAnalyses(AnalysesRequest request) {
        final TimeTaker tt = new TimeTaker();

        final AnalysesProvider provider = getProvider(request.getSelector());
        if (provider == null) {
            final AnalysisResponse result = new AnalysisResponse();
            result.setInvalid();
            return result;
        }

        final AnalysisResponse response = provider.getAnalyses(request);

        if (this.logger.isDebugEnabled()) {
            this.logger.debug("<getAnalyses> for " + request + " is " + response.toString() + ", took " + tt);
        }
        return response;
    }
}
