/*
 * DzResearchResponse.java
 *
 * Created on 24.03.14 14:27
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.gisresearch;

import java.util.List;

import de.marketmaker.istar.common.request.AbstractIstarResponse;
import de.marketmaker.istar.domainimpl.data.FacetedSearchResult;

/**
 * @author oflege
 */
public class GisResearchResponse extends AbstractIstarResponse {
    static final long serialVersionUID = 1L;

    private List<GisResearchDoc> docs;

    private final FacetedSearchResult facetedSearchResult;

    private final int totalCount;

    GisResearchResponse() {
        this(null, -1, null);
    }

    GisResearchResponse(List<GisResearchDoc> docs, int totalCount, FacetedSearchResult facetedSearchResult) {
        this.totalCount = totalCount;
        this.docs = docs;
        this.facetedSearchResult = facetedSearchResult;
        if (docs == null) {
            setInvalid();
        }
    }

    public FacetedSearchResult getFacetedSearchResult() {
        return facetedSearchResult;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public List<GisResearchDoc> getDocs() {
        return docs;
    }

    @Override
    protected void appendToString(StringBuilder sb) {
        sb.append(", #").append(this.docs.size()).append(" of ").append(this.totalCount);
    }
}
