/*
 * AnalysesMetaResponse.java
 *
 * Created on 21.03.12 08:29
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.analyses.frontend;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.marketmaker.istar.common.request.AbstractIstarResponse;
import de.marketmaker.istar.domain.data.StockAnalysis;

/**
 * @author oflege
 */
public class AnalysisResponse extends AbstractIstarResponse {
    protected static final long serialVersionUID = 1L;

    private Map<String, Long> analysesToIids = new HashMap<>();

    private List<StockAnalysis> analyses = Collections.emptyList();

    private int totalCount = 0;

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public void setAnalyses(List<? extends StockAnalysis> analyses) {
        this.analyses = new ArrayList<>(analyses);
    }

    public List<StockAnalysis> getAnalyses() {
        return analyses;
    }

    public Map<String, Long> getAnalysesToIids() {
        return analysesToIids;
    }

    public void setAnalysesToIids(Map<String, Long> analysesToIids) {
        this.analysesToIids = analysesToIids;
    }

    @Override
    protected void appendToString(StringBuilder sb) {
        sb.append(", #result=" + analyses.size() + ", #total=" + totalCount);
    }
}
