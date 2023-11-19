package de.marketmaker.istar.merger.provider.lbbwresearch;

import java.util.EnumSet;
import java.util.Set;

import org.apache.lucene.search.Query;

import de.marketmaker.istar.common.request.AbstractIstarRequest;
import de.marketmaker.istar.domain.profile.Profile;
import de.marketmaker.istar.domain.profile.Selector;
import de.marketmaker.istar.merger.context.RequestContext;
import de.marketmaker.istar.merger.context.RequestContextHolder;

/**
 * Request DTO
 * @author mcoenen
 */
public class LbbwResearchRequest extends AbstractIstarRequest {

    public static final String DEFAULT_SORT_BY = "";

    private Query query;

    private int offset;

    private int count;

    private boolean ascending;

    private String sortBy = DEFAULT_SORT_BY;

    private Set<Selector> selectors;

    public LbbwResearchRequest() {
        this.selectors = LbbwResearchRequest.computeSelectors();
    }

    public Query getQuery() {
        return query;
    }

    public void setQuery(Query query) {
        this.query = query;
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

    public boolean isAscending() {
        return ascending;
    }

    public void setAscending(boolean ascending) {
        this.ascending = ascending;
    }

    public String getSortBy() {
        return sortBy != null ? sortBy : "";
    }

    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }

    public Set<Selector> getSelectors() {
        return selectors;
    }

    void setSelectors(Set<Selector> selectors) {
        this.selectors = selectors;
    }

    private static EnumSet<Selector> computeSelectors() {
        EnumSet<Selector> selectors = EnumSet.noneOf(Selector.class);
        RequestContext requestContext = RequestContextHolder.getRequestContext();
        if (requestContext == null) {
            return selectors;
        }
        Profile p = requestContext.getProfile();
        if (p == null) {
            return selectors;
        }
        if (p.isAllowed(Selector.LBBW_RESEARCH_MAERKTE_IM_BLICK)) {
            selectors.add(Selector.LBBW_RESEARCH_MAERKTE_IM_BLICK);
        }
        if (p.isAllowed(Selector.LBBW_RESEARCH_RESTRICTED_REPORTS)) {
            selectors.add(Selector.LBBW_RESEARCH_RESTRICTED_REPORTS);
        }
        return selectors;
    }
}
