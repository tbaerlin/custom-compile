/*
 * SearchFilterFactory.java
 *
 * Created on 18.11.2007 18:06:06
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.instrument.search;

import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.QueryWrapperFilter;
import org.apache.lucene.search.TermsFilter;
import org.apache.lucene.util.Version;
import org.springframework.util.StringUtils;

import de.marketmaker.istar.common.lucene.ChainedFilter;
import de.marketmaker.istar.common.lucene.SmartCachingWrapperFilter;
import de.marketmaker.istar.common.util.EntitlementsVwd;
import de.marketmaker.istar.domain.profile.Profile;
import de.marketmaker.istar.domain.profile.Selector;
import de.marketmaker.istar.instrument.IndexConstants;

import static de.marketmaker.istar.common.lucene.ChainedFilter.Logic.AND;
import static de.marketmaker.istar.domain.KeysystemEnum.MMWKN;
import static de.marketmaker.istar.domain.KeysystemEnum.VWDFEED;
import static de.marketmaker.istar.instrument.IndexConstants.*;
import static de.marketmaker.istar.instrument.search.InstrumentSearcherImpl.NO_FIELD_INDICATOR;

/**
 * Creates filters for instrument searches and also acts as a cache for those filters.
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
class SearchFilterFactory {
    /**
     * TermsFilter subclass with a descriptive toString output.
     */
    private static class MyTermsFilter extends TermsFilter {
        private final Map<String, List<String>> terms = new LinkedHashMap<>();

        public void addTerm(Term term) {
            super.addTerm(term);
            this.terms.computeIfAbsent(term.field(), x -> new ArrayList<>()).add(term.text());
        }

        @Override
        public String toString() {
            return "MyTermsFilter" + this.terms.toString();
        }
    }


    private static final Filter MMXML_BLACKLIST_FILTER
            = createCachedTermFilter(FIELDNAME_IS_MMXML_BLACKLIST, FIELD_VALUE_BOOLEAN_FALSE);

    private static final int OPRA_ENTITLEMENT_INDEX = Selector.OPRA.getId();

    private static final Filter VWDSYMBOL_FILTER
            = createCachedTermFilter(FIELDNAME_QUOTESYMBOLS, VWDFEED.name());

    private static final Filter MMSYMBOL_OR_VWDSYMBOL_FILTER
            = new SmartCachingWrapperFilter(
            ChainedFilter.create(createTermFilter(FIELDNAME_QUOTESYMBOLS, MMWKN.name()),
                    VWDSYMBOL_FILTER, ChainedFilter.Logic.OR));

    private static final Filter ISIN_AND_VWDSYMBOL_FILTER
            = new SmartCachingWrapperFilter(
            ChainedFilter.create(createWithIsinFilter(), VWDSYMBOL_FILTER, ChainedFilter.Logic.AND));

    private final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * Maps filter keys to cached filters. The filters are instances of CachingWrapperFilter that
     * in turn cache filter results per IndexReader in WeakHashMaps, so this cache should not cause
     * any memory bottleneck.
     */
    private final Map<String, Filter> cache = new LinkedHashMap<String, Filter>(128, 0.75f, true) {
        protected boolean removeEldestEntry(Map.Entry<String, Filter> eldest) {
            return size() > 90;
        }
    };

    private final IndexReader indexReader;

    // all entitlements in the index
    private final BitSet indexedEntitlement = new BitSet(520);

    private static Filter createWithIsinFilter() {
        // returns all docs with an isin
        // performance is OK because of caching
        return new QueryWrapperFilter(new PrefixQuery(new Term("isin", "")));
    }

    private static Filter createCachedTermFilter(String key, String value) {
        final TermsFilter f = createTermFilter(key, value);
        return new SmartCachingWrapperFilter(f, SmartCachingWrapperFilter.MAX_PERF_FACTOR);
    }

    private static TermsFilter createTermFilter(String key, String value) {
        final TermsFilter result = new MyTermsFilter();
        result.addTerm(new Term(key, value.toLowerCase()));
        return result;
    }

    public SearchFilterFactory(IndexReader indexReader) throws IOException {
        this.indexReader = indexReader;
        initEntitlements();
    }

    private void initEntitlements() throws IOException {
        final String key = IndexConstants.FIELDNAME_ENTITLEMENT_VWD;
        final TermEnum enumerator = this.indexReader.terms(new Term(key, ""));
        if (enumerator.term() == null) {
            return;
        }
        do {
            Term term = enumerator.term();
            if (term == null || !term.field().equals(key)) {
                break;
            }
            this.indexedEntitlement.set(EntitlementsVwd.toValue(enumerator.term().text()));
        } while (enumerator.next());
        this.logger.info("<initEntitlements> " + EntitlementsVwd.asString(this.indexedEntitlement));
    }

    Filter createFilter(SuggestRequest sr, String constraints) throws Exception {
        final List<Filter> filters = new ArrayList<>(2);
        addProfileFilter(filters, sr.getProfile(), true);
        addQueryFilter(filters, constraints);
        return ChainedFilter.create(filters, AND);
    }

    Filter createFilter(SearchRequest sr, String constraints) throws Exception {
        final List<Filter> filters = new ArrayList<>(5);
        addQueryFilter(filters, sr.getSearchConstraints());
        addQueryFilter(filters, constraints);
        addMmxmlFilter(filters, sr);
        addSymbolsFilter(filters, sr);
        if (sr.getAbos() != null) {
            addAboFilter(filters, sr.getAbos());
        }
        else {
            addProfileFilter(filters, sr.getProfile(), sr.isFilterOpraMarkets());
        }

        return ChainedFilter.create(filters, AND);
    }

    private void addQueryFilter(List<Filter> filters, final String constraints)
            throws ParseException, IOException {
        if (StringUtils.hasText(constraints)) {
            filters.add(getQueryFilter(constraints));
        }
    }

    private void addSymbolsFilter(List<Filter> filters, SearchRequest sr)
            throws IOException, ParseException {
        switch (sr.getResultType()) {
            case QUOTE_ANY:
                return;
            case QUOTE_WITH_MMSYMBOL_OR_VWDSYMBOL:
                filters.add(MMSYMBOL_OR_VWDSYMBOL_FILTER);
                break;
            case QUOTE_WITH_VWDSYMBOL_AND_INSTRUMENT_WITH_ISIN:
                filters.add(ISIN_AND_VWDSYMBOL_FILTER);
                break;
            case QUOTE_WITH_VWDSYMBOL:
            default:
                filters.add(VWDSYMBOL_FILTER);
        }
    }

    private void addProfileFilter(List<Filter> filters,
            final Profile profile, boolean filterOpraMarkets) throws IOException, ParseException {
        final Filter filter = getProfileFilter(profile, filterOpraMarkets);
        if (filter != null) {
            filters.add(filter);
        }
    }

    private void addAboFilter(List<Filter> filters,
            final List<String> abo) throws IOException, ParseException {
        final Filter filter = getAboFilter(abo);
        if (filter != null) {
            filters.add(filter);
        }
    }

    private Filter getProfileFilter(final Profile profile, boolean filterOpraMarkets) {
        if (profile == null) {
            return null;
        }

        final BitSet bs = getEntitlements(profile);

        if (filterOpraMarkets) {
            bs.clear(OPRA_ENTITLEMENT_INDEX);
        }

        if (bs.equals(this.indexedEntitlement)) {
            return null;
        }

        final String key = EntitlementsVwd.asString(bs);
        final Filter cachedFilter = getCachedFilter(key);
        if (cachedFilter != null) {
            return cachedFilter;
        }

        final TermsFilter tf = new MyTermsFilter();
        for (int i = bs.nextSetBit(0); i != -1; i = bs.nextSetBit(i + 1)) {
            tf.addTerm(new Term(FIELDNAME_ENTITLEMENT_VWD, EntitlementsVwd.toEntitlement(i).toLowerCase()));
        }

        final SmartCachingWrapperFilter result
                = new SmartCachingWrapperFilter(tf, SmartCachingWrapperFilter.MAX_PERF_FACTOR);
        cacheFilter(key, result);
        return result;
    }

    private Filter getAboFilter(final List<String> abos) {
        if (abos == null) {
            return null;
        }

        final String key = abos.toString();
        final Filter cachedFilter = getCachedFilter(key);
        if (cachedFilter != null) {
            return cachedFilter;
        }

        final TermsFilter tf = new MyTermsFilter();
        for (String abo : abos) {
            tf.addTerm(new Term(FIELDNAME_ENTITLEMENT_ABO, abo.toLowerCase()));
        }
        // quotes w/o mminstrumentclass have never been exported and should therefore not be searchable
        tf.addTerm(new Term(FIELDNAME_WITH_MMTYPE, FIELD_VALUE_BOOLEAN_TRUE));

        final SmartCachingWrapperFilter result
                = new SmartCachingWrapperFilter(tf, SmartCachingWrapperFilter.MAX_PERF_FACTOR);
        cacheFilter(key, result);
        return result;
    }

    private BitSet getEntitlements(final Profile profile) {
        // for instruments we don't care about rt or nt, so use null PriceQuality
        final BitSet result = profile.toEntitlements(Profile.Aspect.PRICE, null);
        // remove anything that is not indexed
        result.and(this.indexedEntitlement);
        return result;
    }

    private void addMmxmlFilter(List<Filter> filters,
            SearchRequest sr) throws ParseException, IOException {
        if ("mm-xml".equals(sr.getClientInfo()) || sr.isFilterBlacklistMarkets()) {
            filters.add(MMXML_BLACKLIST_FILTER);
        }
    }

    private Filter getCachedFilter(String key) {
        synchronized (this.cache) {
            return this.cache.get(key);
        }
    }

    private void cacheFilter(String key, Filter f) {
        synchronized (this.cache) {
            this.cache.put(key, f);
        }
    }

    private Filter getQueryFilter(String queryExpr) throws ParseException, IOException {
        return getQueryFilter(queryExpr, queryExpr);
    }

    private Filter getQueryFilter(String key, String queryExpr) throws ParseException, IOException {
        final Filter f = getCachedFilter(key);
        if (f != null) {
            return f;
        }

        final SmartCachingWrapperFilter result = createNewFilter(queryExpr);
        cacheFilter(key, result);
        return result;
    }

    private SmartCachingWrapperFilter createNewFilter(String queryExpr)
            throws ParseException, IOException {
        final QueryParser qp = new QueryParser(Version.LUCENE_30, NO_FIELD_INDICATOR,
                AnalyzerFactory.getQueryAnalyzer());
        final Query query = qp.parse(queryExpr);

        return new SmartCachingWrapperFilter(new QueryWrapperFilter(query),
                SmartCachingWrapperFilter.MAX_PERF_FACTOR);
    }
}
