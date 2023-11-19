/*
 * SearchRequest.java
 *
 * Created on 22.12.2004 14:05:41
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.instrument.search;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

import org.apache.lucene.search.Query;

import de.marketmaker.istar.common.request.AbstractIstarRequest;
import de.marketmaker.istar.common.util.ArraysUtil;
import de.marketmaker.istar.domain.instrument.InstrumentTypeEnum;
import de.marketmaker.istar.domain.profile.Profile;
import de.marketmaker.istar.domainimpl.profile.PmAboProfile;

/**
 * A string based implementation of {@link de.marketmaker.istar.instrument.search.SearchRequest}.
 * <p>
 * Specifically, the search expression and constraints are specified using Lucene query syntax. Please
 * refer to {@link #setSearchExpression(String)} and {@link #setSearchConstraints(String)} for detailed
 * description.
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class SearchRequestStringBased extends AbstractIstarRequest implements SearchRequest {
    static final long serialVersionUID = 213127L;

    private String searchExpression;

    private String searchConstraints = "";

    private List<String> defaultFields = Collections.emptyList();

    private SearchRequestResultType resultType = SearchRequestResultType.QUOTE_WITH_VWDSYMBOL;

    private int maxNumResults = Integer.MIN_VALUE;

    private boolean countInstrumentResults = false;

    private Collection<InstrumentTypeEnum> countTypes = null;

    private Collection<InstrumentTypeEnum> filterTypes = null;

    private Profile profile;

    private List<String> abos;

    private int pagingOffset;

    private int pagingCount;

    private boolean usePaging = false;

    private String[] sortFields;

    private EnumSet<InstrumentSearcherImpl.SIMPLESEARCH_STEPS> searchSteps;

    private boolean filterBlacklistMarkets = false;

    private boolean filterOpraMarkets = true;

    public SearchRequestStringBased() {
    }

    /**
     * Constructs a request using the given client info.
     * @param clientInfo a short client info.
     */
    public SearchRequestStringBased(String clientInfo) {
        super(clientInfo, false);
    }

    public EnumSet<InstrumentSearcherImpl.SIMPLESEARCH_STEPS> getSearchSteps() {
        return searchSteps;
    }

    public boolean isFilterBlacklistMarkets() {
        return filterBlacklistMarkets;
    }

    @Override
    public boolean isFilterOpraMarkets() {
        return this.filterOpraMarkets;
    }

    public void setFilterOpraMarkets(boolean filterOpraMarkets) {
        this.filterOpraMarkets = filterOpraMarkets;
    }

    /**
     * @param filterBlacklistMarkets true to exclude markets on market-maker XML black list. Default
     * is false.
     */
    public void setFilterBlacklistMarkets(boolean filterBlacklistMarkets) {
        this.filterBlacklistMarkets = filterBlacklistMarkets;
    }

    /**
     * @param searchSteps a set of {@link de.marketmaker.istar.instrument.search.InstrumentSearcherImpl.SIMPLESEARCH_STEPS}
     * indicating search result matching degree.
     */
    public void setSearchSteps(EnumSet<InstrumentSearcherImpl.SIMPLESEARCH_STEPS> searchSteps) {
        this.searchSteps = searchSteps;
    }

    public String[] getSortFields() {
        return ArraysUtil.copyOf(this.sortFields);
    }

    /**
     * @param sortFields instrument field names on which the results should be sorted.
     */
    public void setSortFields(String... sortFields) {
        this.sortFields = ArraysUtil.copyOf(sortFields);
    }

    public int getMaxNumResults() {
        return maxNumResults;
    }

    /**
     * @param maxNumResults the maximum result number expected.
     */
    public void setMaxNumResults(int maxNumResults) {
        this.maxNumResults = maxNumResults;
    }

    /**
     * Specifies search expression in Apache Lucene query syntax.
     * <p>
     * For a complete description of Lucene query syntax, please refer to
     * {@literal http://lucene.apache.org/java/2_4_1/queryparsersyntax.html}. The field names that
     * can be searched can be found in {@link de.marketmaker.istar.instrument.IndexConstants} if they
     * are not commented as being unable to be searched.
     * @param searchExpression a search expression.
     */
    public void setSearchExpression(String searchExpression) {
        this.searchExpression = searchExpression;
    }

    public String getSearchExpression() {
        return searchExpression;
    }

    public String getSearchConstraints() {
        return searchConstraints;
    }

    /**
     * Specifies search constraints in Apache Lucene query syntax.
     * <p>
     * Search constraints filter the results found by search expression further. Normally one would
     * specify the most relevant search fields in search expression and use search constraints
     * to filter the result only to some sub-sets, for example limiting results only to a specific
     * market or currency(using '+' operator), or the opposite(using '-' operator).
     * <p>
     * For a complete list of constaint possibilities, please refer to
     * {@literal http://lucene.apache.org/java/2_4_1/queryparsersyntax.html#Boolean%20operators}
     * @param searchConstraints
     */
    public void setSearchConstraints(String searchConstraints) {
        this.searchConstraints = searchConstraints;
    }

    /**
     * Specifies the instrument fields that should be returned in the search results.
     * <p>
     * XXX: default fields would be better static, not necessarily to be set. Or rename this method
     * to setFields().
     * @param defaultFields
     */
    public void setDefaultFields(List<String> defaultFields) {
        this.defaultFields = defaultFields;
    }

    public List<String> getDefaultFields() {
        return defaultFields;
    }

    public Query getQuery(Map<String, String> fieldAliases) throws Exception {
        return null;
    }

    public SearchRequestResultType getResultType() {
        return resultType;
    }

    /**
     * Specifies which kind of instrument results are expected. It is a convenient alternative for
     * {@link #setSearchConstraints(String)} with specific constraint on
     * {@link de.marketmaker.istar.instrument.IndexConstants#FIELDNAME_QUOTESYMBOLS}. The effect is
     * only those instruments are returned which have quotes with symbols defined by
     * {@link de.marketmaker.istar.instrument.search.SearchRequestResultType}.
     * @param resultType a {@link de.marketmaker.istar.instrument.search.SearchRequestResultType}
     */
    public void setResultType(SearchRequestResultType resultType) {
        this.resultType = resultType;
    }

    public boolean isCountInstrumentResults() {
        return countInstrumentResults;
    }

    /**
     * XXX: another name would be better.
     * @param countInstrumentResults if set to true the result contains only instrument, otherwise
     * only quotes. Default is false.
     */
    public void setCountInstrumentResults(boolean countInstrumentResults) {
        this.countInstrumentResults = countInstrumentResults;
    }

    public EnumSet<InstrumentTypeEnum> getCountTypes() {
        return toEnumSet(this.countTypes);
    }

    private EnumSet<InstrumentTypeEnum> toEnumSet(Collection<InstrumentTypeEnum> types) {
        if (types == null) {
            return null;
        }
        if (types instanceof EnumSet) {
            return (EnumSet<InstrumentTypeEnum>) types;
        }
        else {
            return types.isEmpty() ? EnumSet.noneOf(InstrumentTypeEnum.class) : EnumSet.copyOf(types);
        }
    }

    /**
     * Specifies which type of instrument in the results should be counted. The count result is then
     * available through {@link SearchResponse#getTypeCounts()}.
     * @param countTypes the instrument types that should be counted. If not set, the search engine
     * won't perform counting on instrument types.
     */
    public void setCountTypes(Collection<InstrumentTypeEnum> countTypes) {
        this.countTypes = countTypes;
    }

    public EnumSet<InstrumentTypeEnum> getFilterTypes() {
        return toEnumSet(this.filterTypes);
    }

    /**
     * Specifies which kind of instruments should be included in the search result.
     * @param filterTypes the instrument types that should be included in the search result. If not
     * set, no filter on instrument types is performed.
     */
    public void setFilterTypes(Collection<InstrumentTypeEnum> filterTypes) {
        this.filterTypes = filterTypes;
    }

    public Profile getProfile() {
        return profile;
    }

    private void setAbos(Collection<String> abos) {
        this.abos = new ArrayList<>(abos);
    }

    @Override
    public List<String> getAbos() {
        return this.abos;
    }

    /**
     * profile to use
     */
    public void setProfile(Profile profile) {
        if (profile instanceof PmAboProfile) {
            setAbos(((PmAboProfile) profile).getAbos());
        }
        else {
            this.profile = profile;
        }
    }

    public int getPagingOffset() {
        return pagingOffset;
    }

    /**
     * @param pagingOffset paging support: specifies a position from which the results are returned.
     * Only has effect if {@link #isUsePaging()} returns true.
     */
    public void setPagingOffset(int pagingOffset) {
        this.pagingOffset = pagingOffset;
    }

    public int getPagingCount() {
        return pagingCount;
    }

    /**
     * @param pagingCount paging support: specifies the number of results included within one page.
     * Only has effect if {@link #isUsePaging()} returns true.
     */
    public void setPagingCount(int pagingCount) {
        this.pagingCount = pagingCount;
    }

    public boolean isUsePaging() {
        return usePaging;
    }

    /**
     * @param usePaging sets to true if need paging support. Default is false.
     */
    public void setUsePaging(boolean usePaging) {
        this.usePaging = usePaging;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder(100)
                .append("SearchRequestStringBased[searchExpression=").append(searchExpression)
                .append(", searchConstraints=").append(searchConstraints)
                .append(", defaultFields=").append(defaultFields)
                .append(", resultType=").append(resultType.name())
                .append(", maxNumResults=").append(maxNumResults)
                .append(", countInstrumentResults=").append(countInstrumentResults)
                .append(", countTypes=").append(countTypes)
                .append(", maxNumResults=").append(maxNumResults)
                .append(", filterTypes=").append(filterTypes)
                .append(", usePaging=").append(usePaging)
                .append(", pagingOffset=").append(pagingOffset)
                .append(", pagingCount=").append(pagingCount)
                .append(", clientInfo=").append(getClientInfo())
                .append(", sortFields=").append(Arrays.toString(this.sortFields));
        if (this.abos != null) {
            sb.append(", abos=").append(this.abos);
        }
        else {
            sb.append(", profile=").append(this.profile);
        }
        return sb.append("]").toString();
    }
}
