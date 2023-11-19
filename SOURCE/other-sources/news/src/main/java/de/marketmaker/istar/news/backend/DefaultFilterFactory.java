/*
 * DefaultFilterFactory.java
 *
 * Created on 21.06.2007 13:55:49
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.news.backend;

import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanFilter;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.FilterClause;
import org.apache.lucene.search.TermsFilter;
import org.joda.time.DateTime;
import org.joda.time.Period;

import de.marketmaker.istar.common.lucene.ChainedFilter;
import de.marketmaker.istar.common.lucene.SmartCachingWrapperFilter;
import de.marketmaker.istar.common.util.EntitlementsVwd;
import de.marketmaker.istar.domain.profile.Profile;
import de.marketmaker.istar.news.frontend.NewsIndexConstants;
import de.marketmaker.istar.news.frontend.NewsRequestBase;

import static de.marketmaker.istar.common.lucene.ChainedFilter.Logic.AND;
import static de.marketmaker.istar.common.lucene.ChainedFilter.Logic.OR;
import static de.marketmaker.istar.common.lucene.SmartCachingWrapperFilter.MAX_PERF_FACTOR;
import static de.marketmaker.istar.domain.data.PriceQuality.DELAYED;
import static de.marketmaker.istar.domain.data.PriceQuality.REALTIME;

/**
 * Creates news filters according to permissions, requested timespans, and whether or not
 * news without ads must be blocked.
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class DefaultFilterFactory extends AbstractFilterFactory {
    /**
     * Delay for all delayed selectors
     * TODO: allow delay to be specified per selector
     */
    private static final Period DEFAULT_DELAY = Period.minutes(30);

    /**
     * For each known NewsIndex, this map
     * contains filters that filter based on a single field and value and cache the results; will
     * only be used for read-only NewsIndex objects, as otherwise the contents change too much for
     * efficient caching. Use a WeakHashMap so we do not have to be notified when a
     * NewsIndex object becomes obsolete.
     */
    private final Map<NewsIndex, Map<String, SmartCachingWrapperFilter>> filtersByIndex = new WeakHashMap<>();

    private Filter getSelectorFilter(BitSet bs, DateTime mostRecent, NewsIndex index) throws IOException {
        final List<Filter> filters = new ArrayList<>(bs.cardinality());
        for (int n = bs.nextSetBit(0); n >= 0; n = bs.nextSetBit(n + 1)) {
            filters.add(getSelectorFilter(index, EntitlementsVwd.toEntitlement(n)));
        }
        final Filter result = ChainedFilter.create(filters, OR);
        if (mostRecent == null) {
            return result;
        }
        if (mostRecent.isAfter(index.getTo())) {
            return result;
        }
        final Filter tf = createTimestampFilter(null, mostRecent);
        return ChainedFilter.create(result, tf, AND);
    }

    private Filter getSelectorFilter(NewsIndex index, String selector) throws IOException {
        return createFieldFilter(index, NewsIndexConstants.FIELD_SELECTOR, selector.toLowerCase(), false);
    }

    private Filter createFieldFilter(NewsIndex index, final String field, String value, boolean inverse)
            throws IOException {
        if (!index.isHistoric()) {
            // the daily index is so small it does not pay off to cache filter results
            return createFilter(field, value, inverse);
        }

        final String cacheKey = field + (inverse ? "!" : "=") + value;

        Map<String, SmartCachingWrapperFilter> filters = this.filtersByIndex.get(index);
        if (filters == null) {
            filters = new HashMap<>();
            this.filtersByIndex.put(index, filters);
        }

        SmartCachingWrapperFilter f = filters.get(cacheKey);
        if (f == null) {
            final Filter filter = createFilter(field, value, inverse);
            f = new SmartCachingWrapperFilter(filter, MAX_PERF_FACTOR);
            filters.put(cacheKey, f);
        }
        return f;
    }

    private Filter createFilter(String field, String value, boolean inverse) {
        final TermsFilter query = new TermsFilter();
        query.addTerm(new Term(field, value));
        return applyInversion(query, inverse);
    }

    private Filter applyInversion(Filter filter, boolean invert) {
        if (!invert) {
            return filter;
        }
        final BooleanFilter result = new BooleanFilter();
        result.add(new FilterClause(filter, BooleanClause.Occur.MUST_NOT));
        return result;
    }

    public Filter getFilter(NewsIndex index, NewsRequestBase request) throws IOException {
        final Filter tsFilter = createTimestampFilter(index, request);
        final Filter adFilter = createAdFilter(index, request);
        final Filter nonSelectorFilter = ChainedFilter.create(tsFilter, adFilter, AND);

        final BitSet indexedSelectors = index.getIndexedSelectors();
        final BitSet rt = getEntitlements(request, true);
        rt.and(indexedSelectors);

        if (rt.equals(indexedSelectors)) { // everything indexed is realtime available
            return tsFilter;
        }

        final BitSet nt = getEntitlements(request, false);
        nt.and(indexedSelectors);

        if (rt.isEmpty() && nt.isEmpty()) {
            return NewsSearcher.EMPTY_FILTER; // nothing is allowed.
        }

        final DateTime mostRecent = new DateTime().minus(DEFAULT_DELAY);
        if (mostRecent.isBefore(index.getFrom())) {
            nt.clear(); // no delayed news in this index
        }

        final Filter rtFilter = rt.isEmpty() ? null : getSelectorFilter(rt, null, index);
        final Filter ntFilter = nt.isEmpty() ? null : getSelectorFilter(nt, mostRecent, index);
        final Filter qualityFilter = ChainedFilter.create(rtFilter, ntFilter, OR);

        return ChainedFilter.create(nonSelectorFilter, qualityFilter, AND);
    }

    private Filter createAdFilter(NewsIndex index, NewsRequestBase request) throws IOException {
        if (request.isWithAds()) {
            return null;
        }
        return createFieldFilter(index, NewsIndexConstants.FIELD_AD, "1", true);
    }

    private BitSet getEntitlements(NewsRequestBase request, boolean realtime) {
        final Profile profile = request.getProfile();
        return profile.toEntitlements(Profile.Aspect.NEWS, realtime ? REALTIME : DELAYED);
    }
}
