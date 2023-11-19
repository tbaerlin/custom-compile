/*
 * AnalysisRequest.java
 *
 * Created on 21.03.12 08:25
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.analyses.frontend;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.lucene.search.Query;

import de.marketmaker.istar.domain.profile.Selector;

/**
 * @author oflege
 */
public class AnalysesRequest extends AbstractAnalysesRequest {
    protected static final long serialVersionUID = 1L;

    public static final String DATE_SORT_BY = "analysisdate";

    private int offset;

    private int count;

    private String sortBy = DATE_SORT_BY;

    private boolean ascending;

    private Query query;

    private List<String> analysisIds;

    private Set<Long> instrumentIds;

    private boolean ignoreAnalysesWithoutRating = true;

    public AnalysesRequest(Selector selector) {
        super(selector);
    }

    @Override
    protected void appendToString(StringBuilder sb) {
        super.appendToString(sb);
        if (this.analysisIds == null) {
            if (this.instrumentIds == null) {
                sb.append(", query=").append(this.query);
            }
            else {
                sb.append(", iids=").append(this.instrumentIds);
            }
            sb.append(", offset=").append(this.offset);
            sb.append(", count=").append(this.count);
            sb.append(", sort=").append(this.sortBy);
            sb.append(", asc=").append(this.ascending);
        }
        else {
            sb.append(", ids=").append(this.analysisIds);
        }
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getSortBy() {
        return sortBy;
    }

    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }

    public boolean isAscending() {
        return ascending;
    }

    public void setAscending(boolean ascending) {
        this.ascending = ascending;
    }

    public void setQuery(Query query) {
        this.query = query;
    }

    public Query getQuery() {
        return query;
    }

    public List<String> getAnalysisIds() {
        return analysisIds;
    }

    public void setAnalysisIds(List<String> analysisIds) {
        this.analysisIds = analysisIds;
    }

    public void setInstrumentIds(Collection<Long> iids) {
        this.instrumentIds = new HashSet<>(iids);
    }

    public Set<Long> getInstrumentIds() {
        return instrumentIds;
    }

    public boolean isIgnoreAnalysesWithoutRating() {
        return ignoreAnalysesWithoutRating;
    }

    public void setIgnoreAnalysesWithoutRating(boolean ignoreAnalysesWithoutRating) {
        this.ignoreAnalysesWithoutRating = ignoreAnalysesWithoutRating;
    }
}
