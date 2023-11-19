/*
 * StockAnalysisResponse.java
 *
 * Created on 11.08.2006 15:00:07
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.stockanalysis;

import de.marketmaker.istar.common.request.AbstractIstarResponse;
import de.marketmaker.istar.domain.data.StockAnalysis;

import java.util.List;
import java.util.Collections;
import java.util.Map;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class StockAnalysisResponse extends AbstractIstarResponse {
    protected static final long serialVersionUID = 1L;

    private int totalCount = 0;

    private List<StockAnalysis> analyses = Collections.emptyList();

    private Map<Long, Map<StockAnalysis.Recommendation, Integer>> countsByInstrumentid
            = Collections.emptyMap();

    public int getTotalCount() {
        return totalCount;
    }

    void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    public List<StockAnalysis> getAnalyses() {
        return analyses;
    }

    void setAnalyses(List<StockAnalysis> analyses) {
        this.analyses = analyses;
    }

    public Map<Long, Map<StockAnalysis.Recommendation, Integer>> getCountsByInstrumentid() {
        return countsByInstrumentid;
    }

    public void setCountsByInstrumentid(
            Map<Long, Map<StockAnalysis.Recommendation, Integer>> countsByInstrumentid) {
        this.countsByInstrumentid = countsByInstrumentid;
    }
}
