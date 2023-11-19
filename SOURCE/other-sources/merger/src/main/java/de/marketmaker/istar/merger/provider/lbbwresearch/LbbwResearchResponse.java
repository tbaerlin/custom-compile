/*
 * DzResearchResponse.java
 *
 * Created on 24.03.14 14:27
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.lbbwresearch;

import java.util.List;

import de.marketmaker.istar.common.request.AbstractIstarResponse;
import de.marketmaker.istar.domainimpl.data.FacetedSearchResult;

/**
 * @author mcoenen
 */
public class LbbwResearchResponse extends AbstractIstarResponse {
    static final long serialVersionUID = 1L;

    private List<ResultDocument> resultDocuments;

    private final FacetedSearchResult facetedSearchResult;

    private final int totalCount;

    LbbwResearchResponse() {
        this(null, -1, null);
    }

    LbbwResearchResponse(List<ResultDocument> resultDocuments, int totalCount, FacetedSearchResult facetedSearchResult) {
        this.totalCount = totalCount;
        this.resultDocuments = resultDocuments;
        this.facetedSearchResult = facetedSearchResult;
        if (resultDocuments == null) {
            setInvalid();
        }
    }

    public FacetedSearchResult getFacetedSearchResult() {
        return facetedSearchResult;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public List<ResultDocument> getResultDocuments() {
        return resultDocuments;
    }

    @Override
    protected void appendToString(StringBuilder sb) {
        sb.append(", #").append(this.resultDocuments.size()).append(" of ").append(this.totalCount);
    }
}
