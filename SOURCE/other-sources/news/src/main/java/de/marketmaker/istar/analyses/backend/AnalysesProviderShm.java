/*
 * AnalysesProviderWebSim.java
 *
 * Created on 22.03.12 09:53
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.analyses.backend;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import de.marketmaker.istar.analyses.backend.Protos.Analysis.Provider;
import de.marketmaker.istar.analyses.frontend.AnalysesMetaRequest;
import de.marketmaker.istar.domain.data.StockAnalysis.Recommendation;
import de.marketmaker.istar.domain.profile.Selector;

/**
 * // TODO: not yet fully implemented
 * @author oflege
 */
public class AnalysesProviderShm extends AnalysesProvider {

    @Override
    protected String getId() {
        return "shm";
    }

    @Override
    Map<String, Map<String, String>> doGetMetaData(AnalysesMetaRequest request) {
        final HashMap<String, Map<String, String>> result = new HashMap<>();
        result.put("ratings", createRatingsMetaMap());
        result.put("analysts", getSources(request.isIgnoreAnalysesWithoutRating()));
        return result;
    }

    @SuppressWarnings("Duplicates")
    private Map<String, String> createRatingsMetaMap() {
        final Map<String, String> result = new LinkedHashMap<>();
        result.put(Recommendation.BUY.name(), "Kaufen");
        result.put(Recommendation.HOLD.name(), "Halten");
        result.put(Recommendation.SELL.name(), "Verkaufen");
        return result;
    }


    @Override
    public Selector getSelector() {
        return Selector.SHM_ANALYSES;
    }

    @Override
    public Provider getProvider() {
        return Provider.SHM;
    }

}
