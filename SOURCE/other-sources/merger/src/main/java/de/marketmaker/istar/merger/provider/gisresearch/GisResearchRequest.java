/*
 * DzResearchRequest.java
 *
 * Created on 24.03.14 14:26
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.gisresearch;

import java.util.Arrays;

import org.apache.lucene.search.Query;

import de.marketmaker.istar.common.request.AbstractIstarRequest;
import de.marketmaker.istar.domain.profile.Profile;
import de.marketmaker.istar.domain.profile.Selector;
import de.marketmaker.istar.merger.context.RequestContextHolder;

/**
 * @author oflege
 */
public class GisResearchRequest extends AbstractIstarRequest {
    static final long serialVersionUID = 1L;

    public static final String DATE_SORT_BY = "date";

    private static final Selector[] NO_SELECTORS = new Selector[0];

    private static Selector[] computeSelectors() {
        Profile p = RequestContextHolder.getRequestContext().getProfile();
        Selector[] tmp = new Selector[4];
        int n = 0;

        if (p.isAllowed(Selector.DZ_FP4)) {
            tmp[n++] = Selector.DZ_FP4;
        }

        if (p.isAllowed(Selector.DZ_HM3)) {
            tmp[n++] = Selector.DZ_HM3;
            tmp[n++] = Selector.DZ_HM2;
            tmp[n++] = Selector.DZ_HM1;
        }
        else if (p.isAllowed(Selector.DZ_HM2)) {
            tmp[n++] = Selector.DZ_HM2;
            tmp[n++] = Selector.DZ_HM1;
        }
        else if (p.isAllowed(Selector.DZ_HM1)) {
            tmp[n++] = Selector.DZ_HM1;
        }

        if (n == 0) {
            return NO_SELECTORS;
        }
        return Arrays.copyOf(tmp, n);
    }

    private int offset;

    private int count;

    private String sortBy = DATE_SORT_BY;

    private boolean ascending;

    private Query query;

    private final Selector[] selectors;

    public GisResearchRequest() {
        this(computeSelectors());
    }

    GisResearchRequest(Selector[] selectors) {
        this.selectors = selectors;
    }

    Selector[] getSelectors() {
        return selectors;
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

    public Query getQuery() {
        return query;
    }

    public void setQuery(Query query) {
        this.query = query;
    }
}
