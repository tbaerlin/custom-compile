/*
 * InstrumentSearcherImpl.java
 *
 * Created on 16.12.2004 13:25:02
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.instrument.search;

import static de.marketmaker.istar.instrument.IndexConstants.FIELDNAME_IID;
import static de.marketmaker.istar.instrument.IndexConstants.FIELDNAME_MPC_COUNT;
import static de.marketmaker.istar.instrument.IndexConstants.FIELDNAME_MPC_VALUE;
import static de.marketmaker.istar.instrument.IndexConstants.FIELDNAME_NAMES;
import static de.marketmaker.istar.instrument.IndexConstants.FIELDNAME_QID;
import static de.marketmaker.istar.instrument.IndexConstants.FIELDNAME_SORT_INSTRUMENT_DEFAULT;
import static de.marketmaker.istar.instrument.IndexConstants.INFRONT_ID;
import static de.marketmaker.istar.instrument.IndexConstants.VWDCODE;

import de.marketmaker.istar.common.lucene.AnalyzerUtils;
import de.marketmaker.istar.common.lucene.ChainedFilter;
import de.marketmaker.istar.common.util.PagedResultSorter;
import de.marketmaker.istar.common.util.TimeTaker;
import de.marketmaker.istar.domain.KeysystemEnum;
import de.marketmaker.istar.domain.instrument.Derivative;
import de.marketmaker.istar.domain.instrument.Instrument;
import de.marketmaker.istar.domain.instrument.InstrumentTypeEnum;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.domain.profile.Profile;
import de.marketmaker.istar.domainimpl.profile.ProfileFactory;
import de.marketmaker.istar.instrument.IndexConstants;
import de.marketmaker.istar.instrument.export.InstrumentDao;
import de.marketmaker.istar.instrument.export.InstrumentDirDao;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TermsFilter;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.TopFieldCollector;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StopWatch;
import org.springframework.util.StringUtils;

/**
 * Can search for instruments/quotes based on a number of search criteria. A new object of
 * this class will be created whenever the index changes.
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class InstrumentSearcherImpl implements InstrumentSearcher, Closeable {
    private static final EnumSet<SIMPLESEARCH_STEPS> ALL_STEPS = EnumSet.allOf(SIMPLESEARCH_STEPS.class);

    private static final int MAX_QUOTES_PER_INSTRUMENT = 42;

    public enum SIMPLESEARCH_STEPS {
        EXACT, FUZZY, FUZZIER, FUZZIEST;

        static final long serialVersionUID = 1L;
    }

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private static final Map<String, Integer> MIN_PREFIX_QUERY_LENGTH
            = new HashMap<>();

    static {
        MIN_PREFIX_QUERY_LENGTH.put(KeysystemEnum.WKN.name().toLowerCase(), 4);
        MIN_PREFIX_QUERY_LENGTH.put(KeysystemEnum.ISIN.name().toLowerCase(), 7);
        MIN_PREFIX_QUERY_LENGTH.put(KeysystemEnum.VALOR.name().toLowerCase(), 4);
        MIN_PREFIX_QUERY_LENGTH.put(KeysystemEnum.VALORSYMBOL.name().toLowerCase(), 2);
        MIN_PREFIX_QUERY_LENGTH.put(KeysystemEnum.SEDOL.name().toLowerCase(), 4);
        MIN_PREFIX_QUERY_LENGTH.put(KeysystemEnum.CUSIP.name().toLowerCase(), 4);
        MIN_PREFIX_QUERY_LENGTH.put(KeysystemEnum.VWDCODE.name().toLowerCase(), 2);
        MIN_PREFIX_QUERY_LENGTH.put(KeysystemEnum.VWDSYMBOL.name().toLowerCase(), 2);
        MIN_PREFIX_QUERY_LENGTH.put(KeysystemEnum.WM_TICKER.name().toLowerCase(), 2);
    }


    private static final Map<String, String> FIELD_ALIASES = new HashMap<>();

    static {
        FIELD_ALIASES.put("mm", "mmwkn");
        FIELD_ALIASES.put("vwd_code", "vwdcode");
        FIELD_ALIASES.put("wnk", "wm_wp_name_kurz");
        FIELD_ALIASES.put("wnl", "wm_wp_name_lang");
        FIELD_ALIASES.put("wnz", "wm_wp_name_zusatz");
        FIELD_ALIASES.put("wn", "wm_wp_name");
        FIELD_ALIASES.put("wpk", "wm_wpk");
    }

    static final String NO_FIELD_INDICATOR = "_xyz_";

    private static final int MAX_NUM_HITS = 1000;

    private IndexReader indexReader;

    private IndexSearcher indexSearcher;

    private IndexReader mpcIndexReader;

    private IndexSearcher mpcIndexSearcher;

    private InstrumentDao instrumentDao;

    private float minimumSimilarity = 0.6f;

    private int numTermsForPseudoPrefixQuery = 20;

    /**
     * limits the number of tokens used for searching after parsing an arbitrary simple search
     * expression. This avoids using too man tokens for expressions such as
     * 'Dryships Inc. Registered Shares DL -.01 /  GQT.FSE'
     */
    private int maxQueryTokens = 4;

    private SearchFilterFactory filterFactory;

    /**
     * A lucene query expression that will be applied to all searches as an additional
     * constraint
     */
    private final String constraints;

    public InstrumentSearcherImpl(File workDir, InstrumentDao instrumentDao,
            String constraints) throws IOException {
        this.indexReader = IndexReader.open(FSDirectory.open(new File(workDir, "index")), true);
        this.indexSearcher = new IndexSearcher(this.indexReader);
        this.mpcIndexReader = IndexReader.open(FSDirectory.open(new File(workDir, "index-mpc")), true);
        this.mpcIndexSearcher = new IndexSearcher(this.mpcIndexReader);
        this.instrumentDao = instrumentDao;

        this.filterFactory = new SearchFilterFactory(this.indexReader);
        this.constraints = constraints;
    }

    public void close() throws IOException {
        this.indexSearcher.close();
        this.mpcIndexSearcher.close();
        this.indexReader.close();
        this.mpcIndexReader.close();
    }

    public void setMinimumSimilarity(float minimumSimilarity) {
        this.minimumSimilarity = minimumSimilarity;
    }

    public void setNumTermsForPseudoPrefixQuery(int numTermsForPseudoPrefixQuery) {
        this.numTermsForPseudoPrefixQuery = numTermsForPseudoPrefixQuery;
    }

    public void setMaxQueryTokens(int maxQueryTokens) {
        this.maxQueryTokens = maxQueryTokens;
    }

    public SearchResponse search(SearchRequest sr) {
        if (this.logger.isDebugEnabled()) {
            this.logger.debug("<search> sr: " + sr);
        }
        try {
            return searchInternal(sr);
        }
        catch (org.apache.lucene.queryParser.ParseException e) {
            this.logger.warn("<search> failed to parse query for " + sr);
        }
        catch (Exception e) {
            this.logger.warn("<search> failed for " + sr, e);
        }
        return SearchResponse.getInvalid();
    }

    private SearchResponse searchInternal(SearchRequest sr) throws Exception {
        Filter constraintsFilter = this.filterFactory.createFilter(sr, this.constraints);

        Query query;
        try {
            query = createQuery(sr);
        } catch (ParseException e) {
            final String se = sr.getSearchExpression();
            if (!se.startsWith("quoteid:(")) {
                throw e;
            }
            // Todo: remove this HACK to circumvent too many clauses exception
            Scanner s = new Scanner(se.substring("quoteid:(".length(), se.lastIndexOf(')')));
            TermsFilter tf = new TermsFilter();
            while (s.hasNext()) {
                tf.addTerm(new Term("quoteid", s.next()));
            }
            constraintsFilter = ChainedFilter.create(constraintsFilter, tf, ChainedFilter.Logic.AND);
            query = new MatchAllDocsQuery();
        }

        return doSearchFiltered(query, constraintsFilter, sr);
    }


    private SearchResponse doSearchFiltered(final Query query, Filter filter,
            SearchRequest sr) throws IOException {
        if (sr.isUsePaging()) {
            return doSearchFilteredPaging(query, filter, sr);
        }
        else {
            return doSearchFilteredOldStyle(query, filter, sr);
        }
    }

    private SearchResponse doSearchFilteredPaging(final Query query, Filter filter,
            final SearchRequest sr) throws IOException {
        final TimeTaker tt = new TimeTaker();

        final int pagingOffset = Math.max(0, sr.getPagingOffset());
        final int pagingCount = Math.max(0, sr.getPagingCount());
        final int numHits = pagingOffset + pagingCount;

        final Sort sort = SortFactory.INSTANCE.getSort(sr);

        final TopFieldCollector collector =
                TopFieldCollector.create(sort, numHits, true, true, true, true);
        final SearchHitCollector hc = new SearchHitCollector(sr, collector);
        this.indexSearcher.search(query, filter, hc);

        final List<Instrument> instruments = new ArrayList<>(pagingCount);
        final List<Quote> quotes = new ArrayList<>(pagingCount);
        final Set<Long> underlyingids = new HashSet<>();

        final TopDocs topDocs = collector.topDocs();
        final ScoreDoc[] scoreDocs = topDocs.scoreDocs;
        Instrument instrument = null;
        for (int i = pagingOffset; i < scoreDocs.length && i < numHits; i++) {
            final Document doc = this.indexReader.document(scoreDocs[i].doc);

            final long instrumentid = Long.parseLong(doc.get(FIELDNAME_IID));
            if (instrument == null || instrument.getId() != instrumentid) {
                instrument = getInstrument(instrumentid);
                addUnderlying(instrument, underlyingids);
            }

            if (sr.isCountInstrumentResults()) {
                instruments.add(instrument);
            }
            else {
                final long quoteid = Long.parseLong(doc.get(FIELDNAME_QID));
                final Quote quote = instrument.getQuote(quoteid);
                quotes.add(quote);
            }
        }

        final SearchResponse response = new SearchResponse();
        response.setTruncatedResultSize(Integer.MIN_VALUE);
        response.setValidObjectCount(hc.getValidCount());
        response.setNumTotalHits(hc.getTotalCount());
        response.setQuerystring(query.toString());
        response.setInstruments(instruments);
        response.setQuotes(quotes);
        response.setTypeCounts(hc.getCountByType());
        response.setRemainingTypesCount(hc.getRemainingCount());
        response.setInstrumentCount(hc.getInstrumentCount());
        response.setUnderlyings(getUnderlyings(underlyingids));

        if (this.logger.isDebugEnabled()) {
            this.logger.debug("<doSearchFilteredPaging> '" + query.toString() + "' found #hits: "
                    + response.getNumTotalHits() + ", #quotes: " + response.getQuotes().size()
                    + ", #instruments: " + response.getInstruments().size() + " in " + tt);
        }

        return response;
    }

    private void addUnderlying(Instrument instrument, Set<Long> underlyingids) {
        if (!(instrument instanceof Derivative)) {
            return;
        }
        final Derivative derivative = (Derivative) instrument;
        if (derivative.getUnderlyingId() > 0) {
            underlyingids.add(derivative.getUnderlyingId());
        }
    }

    private SearchResponse doSearchFilteredOldStyle(final Query query, Filter filter,
            SearchRequest sr) throws IOException {
        StopWatch sw = new StopWatch();
        sw.start("search");

        final int maxNumResults = getMaxNumResults(sr.getMaxNumResults());
        final Sort sort = new Sort(new SortField(FIELDNAME_SORT_INSTRUMENT_DEFAULT, SortField.INT));

        // we find based on quotes but maxNumResults refers to instruments, so multiply
        // by MAX_QUOTES_PER_INSTRUMENT
        final TopFieldCollector collector = TopFieldCollector.create(sort,
                maxNumResults * MAX_QUOTES_PER_INSTRUMENT, true, true, true, true);

        this.indexSearcher.search(query, filter, collector);

        sw.stop();
        sw.start("identify");

        // use LinkedHashSets to maintain sort order
        final Set<Long> instrumentids = new LinkedHashSet<>(maxNumResults);
        final Set<Long> quoteids = new LinkedHashSet<>(maxNumResults);

        final TopDocs topDocs = collector.topDocs();
        final ScoreDoc[] scoreDocs = topDocs.scoreDocs;
        for (ScoreDoc scoreDoc : scoreDocs) {
            final Document doc = this.indexReader.document(scoreDoc.doc);

            final long quoteid = Long.parseLong(doc.get(FIELDNAME_QID));
            quoteids.add(quoteid);

            final long instrumentid = Long.parseLong(doc.get(FIELDNAME_IID));
            instrumentids.add(instrumentid);

            if (instrumentids.size() == maxNumResults) {
                break;
            }
        }

        sw.stop();
        sw.start("read");

        final List<Instrument> instruments = new ArrayList<>(maxNumResults);
        final Map<Long, Instrument> underlyings = new HashMap<>();
        final Set<Long> unknownUnderlyings = new HashSet<>();
        for (Long instrumentid : instrumentids) {
            final Instrument instrument = getInstrument(instrumentid);
            instruments.add(instrument);
            handleUnderlyingRelation(instrument, underlyings, unknownUnderlyings);
        }

        sw.stop();

        final boolean truncatedResultSize =
                (maxNumResults * MAX_QUOTES_PER_INSTRUMENT) < collector.getTotalHits();

        if (truncatedResultSize) {
            if (instrumentids.size() < maxNumResults) {
                this.logger.warn("<doSearchFilteredOldStyle> result truncated for " + sr
                    + ", MAX_QUOTES_PER_INSTRUMENT needs to be adjusted!");
            }
            else if (this.logger.isDebugEnabled()) {
                this.logger.debug("<search> truncated result size to " + maxNumResults + " instruments");
            }
        }

        final SearchResponse response = new SearchResponse();
        response.setTruncatedResultSize(truncatedResultSize ? instruments.size() : Integer.MIN_VALUE);
        response.setNumTotalHits(collector.getTotalHits());
        response.setQuerystring(query.toString());
        response.setInstruments(instruments);
        response.setQuoteidHits(quoteids);
        response.setUnderlyings(underlyings);

        if (sw.getTotalTimeMillis() > 1000) {
            this.logger.warn("<doSearchFilteredOldStyle> '" + query.toString()
                    + "' found #hits: " + response.getNumTotalHits()
                    + ", #quotes: " + response.getQuoteids().size()
                    + ", #instruments: " + response.getInstruments().size()
                    + sw.prettyPrint());
        }
        else if (this.logger.isDebugEnabled()) {
            this.logger.debug("<doSearchFilteredOldStyle> '" + query.toString()
                    + "' found #hits: " + response.getNumTotalHits()
                    + ", #quotes: " + response.getQuoteids().size()
                    + ", #instruments: " + response.getInstruments().size()
                    + " in " + sw.shortSummary());
        }
        return response;
    }

    private Map<Long, Instrument> getUnderlyings(Set<Long> underlyingids) {
        if (underlyingids.isEmpty()) {
            return Collections.emptyMap();
        }

        final Map<Long, Instrument> underlyings = new HashMap<>(underlyingids.size());

        for (final Long uid : underlyingids) {
            final Instrument underlying = getInstrument(uid);
            if (underlying == null) {
                this.logger.warn("<doSearchFiltered> underlyingId " + uid + " not found");
            }
            else {
                underlyings.put(underlying.getId(), underlying);
            }
        }

        return underlyings;
    }

    private void handleUnderlyingRelation(Instrument instrument, Map<Long, Instrument> underlyings,
            Set<Long> unknownUnderlyings) {
        if (!(instrument instanceof Derivative)) {
            return;
        }

        final Derivative derivative = ((Derivative) instrument);
        if (derivative.getUnderlyingId() > 0
                && !underlyings.containsKey(derivative.getUnderlyingId())
                && !unknownUnderlyings.contains(derivative.getUnderlyingId())
                ) {
            final Instrument underlying = getInstrument(derivative.getUnderlyingId());
            if (underlying == null) {
                this.logger.warn("<doSearchFiltered> underlyingId " + derivative.getUnderlyingId()
                        + " for iid " + instrument.getId() + " not found");
                unknownUnderlyings.add(derivative.getUnderlyingId());
            }
            else {
                underlyings.put(underlying.getId(), underlying);
            }
        }
    }

    private Instrument getInstrument(long instrumentid) {
        if (this.instrumentDao != null) {
            return this.instrumentDao.getInstrument(instrumentid);
        }
        return null;
    }

    private int getMaxNumResults(int requestMaxNumResults) {
        if (requestMaxNumResults < 1 || requestMaxNumResults > 5000) {
            return MAX_NUM_HITS;
        }

        return requestMaxNumResults;
    }

    private Query createQuery(SearchRequest sr) throws ParseException {
        final Analyzer analyzer = getQueryAnalyzer();

        final String expr = dealiasExpression(sr.getSearchExpression());

        final QueryParser qp = new QueryParser(Version.LUCENE_24, NO_FIELD_INDICATOR, analyzer);
        final Query baseQuery = qp.parse(expr);

        if (!baseQuery.toString().contains(NO_FIELD_INDICATOR)) {
            return baseQuery;
        }

        final BooleanQuery query = new BooleanQuery();
        final List<String> dealiasedFields = dealiasDefaultFields(sr.getDefaultFields());
        for (final String fieldname : dealiasedFields) {
            final QueryParser fqp = new QueryParser(Version.LUCENE_24, fieldname, analyzer);
            final Query fieldQuery = fqp.parse(expr);
            query.add(fieldQuery, BooleanClause.Occur.SHOULD);
        }

        return query;
    }

    private Analyzer getQueryAnalyzer() {
        return AnalyzerFactory.getQueryAnalyzer();
    }

    private List<String> dealiasDefaultFields(List<String> fields) {
        final List<String> result = new ArrayList<>(fields.size());

        for (final String field : fields) {
            final String alias = FIELD_ALIASES.get(field);
            result.add(alias != null ? alias : field);
        }

        return result;
    }

    private String dealiasExpression(String searchExpression) {
        String result = searchExpression;
        for (final Map.Entry<String, String> entry : FIELD_ALIASES.entrySet()) {
            result = result.replaceAll(entry.getKey() + ":", entry.getValue() + ":");
        }
        return result;
    }

    public SearchMetaResponse getMetaData(SearchMetaRequest request) {
        final SearchMetaResponse response = new SearchMetaResponse();

        response.setCountries(this.instrumentDao.getCountries());
        response.setCurrencies(this.instrumentDao.getCurrencies());
        response.setMarkets(this.instrumentDao.getMarkets());
        response.setInstrumentTypes(Arrays.asList(InstrumentTypeEnum.values()));

        return response;
    }

    public SearchResponse simpleSearch(SearchRequest sr) {
        if (this.logger.isDebugEnabled()) {
            this.logger.debug("<simpleSearch> sr: " + sr);
        }

        try {
            final SearchResponse result = simpleSearchInternal(sr, true);
            if (result.getInstruments().isEmpty() && result.getQuotes().isEmpty()) {
                // log helps to spot search problems
                this.logger.info("<simpleSearch> no result for " + sr);
            }
            return result;
        }
        catch (BooleanQuery.TooManyClauses tmc) {
            this.logger.warn("<simpleSearch> too many clauses for " + sr);
            // if we get here, constraintsFilter, tokens and fields have been initialized
            try {
                return simpleSearchInternal(sr, false);
            } catch (Exception e) {
                this.logger.warn("<simpleSearch> failed for " + sr, e);
            }
        }
        catch (Exception e) {
            this.logger.warn("<simpleSearch> failed for " + sr, e);
        }
        return SearchResponse.getInvalid();
    }

    public long[] validate(long[] iids) {
        return this.instrumentDao.validate(iids);
    }

    @Override
    public Set<Long> getUnderlyingIds() {
        final Term term = new Term(IndexConstants.FIELDNAME_UNDERLYINGID, "0");

        try {
            final TermEnum termEnum = this.indexReader.terms(term);
            if (termEnum.term() == null || !termEnum.term().field().equals(term.field())) {
                this.logger.warn("<getUnderlyingIds> no ids found!?");
                return null;
            }
            final Set<Long> result = new HashSet<>();
            //noinspection StringEquality
            do {
                result.add(Long.parseLong(termEnum.term().text()));
            } while (termEnum.next() && termEnum.term().field() == term.field());
            termEnum.close();
            return result;
        } catch (IOException e) {
            this.logger.error("<getUnderlyingIds> failed", e);
            return null;
        }
    }

    private String getSearchExpression(SearchRequest sr) {
        final String expression = sr.getSearchExpression();
        final String[] parts = expression.split("\\s+");
        if (parts.length > maxQueryTokens) {
            return StringUtils.arrayToDelimitedString(Arrays.copyOf(parts, this.maxQueryTokens), " ");
        }
        return expression;
    }

    public SearchResponse simpleSearchInternal(SearchRequest sr, boolean usePrefix) throws Exception {
        final Filter constraintsFilter = this.filterFactory.createFilter(sr, this.constraints);

        final String expression = getSearchExpression(sr);

        final List<String> terms = parseTerms(expression, AnalyzerFactory.getQueryAnalyzer());

        final List<String> expandedTerms
                = parseTerms(expression, AnalyzerFactory.getIndexAnalyzer());

        final List<String> fields = dealiasDefaultFields(sr.getDefaultFields());

        final EnumSet<SIMPLESEARCH_STEPS> steps = getSteps(sr);

        SearchResponse response = null;

        if (steps.contains(InstrumentSearcherImpl.SIMPLESEARCH_STEPS.EXACT)) {
            // ###########################
            // ## 0) exact vwdcode
            if (fields.contains(VWDCODE) && terms.size() == 1 && terms.get(0).contains(".")) {
                final TermQuery searchQuery = new TermQuery(new Term(VWDCODE, terms.get(0)));

                response = doSearchFiltered(searchQuery, constraintsFilter, sr);
                if (!response.getInstruments().isEmpty() || !response.getQuotes().isEmpty()) {
                    return response;
                }
            }

            // ###########################
            // ## 0.1) exact infront_id, keep for now. Refactor when more exceptions required
            if (fields.contains(INFRONT_ID) && terms.size() == 2 && expression.contains(";")) {
                final TermQuery searchQuery = new TermQuery(new Term(INFRONT_ID, expression));

                response = doSearchFiltered(searchQuery, constraintsFilter, sr);
                if (!response.getInstruments().isEmpty() || !response.getQuotes().isEmpty()) {
                    return response;
                }
            }

            // ###########################
            // ## 1) direct, prefixed, AND
            final BooleanQuery searchQuery = getAndQuery(terms, fields, usePrefix);

            response = doSearchFiltered(searchQuery, constraintsFilter, sr);
            if (!response.getInstruments().isEmpty() || !response.getQuotes().isEmpty()) {
                return response;
            }

            if (terms.size() != expandedTerms.size()) {
                // ###########################
                // ## 2) direct, prefixed, AND, composite-splitted/apostrophe resolved
                final BooleanQuery searchQuery2 = getAndQuery(expandedTerms, fields, usePrefix);

                response = doSearchFiltered(searchQuery2, constraintsFilter, sr);
                if (!response.getInstruments().isEmpty() || !response.getQuotes().isEmpty()) {
                    return response;
                }
            }
        }

        // maybe some term was misspelled, lookup most probable candidate terms:
        final Map<String, FuzzyMatches> mpcs = getFuzzyMatches(expandedTerms);

        if (steps.contains(InstrumentSearcherImpl.SIMPLESEARCH_STEPS.FUZZY)) {
            // ###########################
            // ## 3) each Token based on MPC (or token itself if not contained in mpc),
            // ##    prefixed, AND
            final BooleanQuery searchQuery3 = getMpcQuery(mpcs, fields, 0, BooleanClause.Occur.MUST, usePrefix);
            response = doSearchFiltered(searchQuery3, constraintsFilter, sr);
            if (!response.getInstruments().isEmpty() || !response.getQuotes().isEmpty()) {
                response.setTermSubstitutions(getTermSubstitutions(mpcs, 0));
                return response;
            }
        }

        if (steps.contains(InstrumentSearcherImpl.SIMPLESEARCH_STEPS.FUZZIER)) {
            // ###########################
            // ## 4) each Token based on MPC (or token itself if not contained in mpc),
            // ##    prefixed, AND, more fuzzy
            final BooleanQuery searchQuery4 = getMpcQuery(mpcs, fields, 1, BooleanClause.Occur.MUST, usePrefix);
            response = doSearchFiltered(searchQuery4, constraintsFilter, sr);
            if (!response.getInstruments().isEmpty() || !response.getQuotes().isEmpty()) {
                response.setTermSubstitutions(getTermSubstitutions(mpcs, 1));
                return response;
            }
        }

        if (steps.contains(InstrumentSearcherImpl.SIMPLESEARCH_STEPS.FUZZIEST)) {
            // ###########################
            // ## 5) each Token based on MPC (or token itself if not contained in mpc),
            // ##    prefixed, OR, as fuzzy as 3)
            final BooleanQuery searchQuery5 = getMpcQuery(mpcs, fields, 0, BooleanClause.Occur.SHOULD, usePrefix);
            response = doSearchFiltered(searchQuery5, constraintsFilter, sr);
            if (!response.getInstruments().isEmpty() || !response.getQuotes().isEmpty()) {
                response.setTermSubstitutions(getTermSubstitutions(mpcs, 0));
                return response;
            }
        }

        return response != null ? response : SearchResponse.getInvalid();
    }

    private static List<String> parseTerms(String expression, final Analyzer analyzer) throws IOException {
        return AnalyzerUtils.getTerms(analyzer.tokenStream("name", new StringReader(expression)));
    }

    private EnumSet<SIMPLESEARCH_STEPS> getSteps(SearchRequest sr) {
        final EnumSet<SIMPLESEARCH_STEPS> steps = sr.getSearchSteps();
        return steps == null || steps.isEmpty() ? ALL_STEPS : steps;
    }

    private BooleanQuery getAndQuery(List<String> terms, List<String> fields,
            boolean prefix) throws IOException {
        final BooleanQuery searchQuery = new BooleanQuery();
        for (final String termText : terms) {
            searchQuery.add(getQueryForFields(termText, fields, prefix), BooleanClause.Occur.MUST);
        }
        return searchQuery;
    }

    private Query getQueryForFields(final String text, List<String> fields,
            boolean prefix) throws IOException {
        if (prefix) {
            return getPrefixQueryForFields(text, fields);
        }

        return getPseudoPrefixQueryForFields(text, fields);
    }

    /**
     * Returns a query (some_field:someTermText some_field:someTermText ...), which will
     * not be expanded and therefore cannot cause a TooManyClauses Exception. some_field will
     * always be a member of fields, someTermText.startsWith(text) will always be true.
     * The number of term queries will be less or equal than
     * <tt>{@link #numTermsForPseudoPrefixQuery} + fields.size()</tt>,
     * and they will consist of those terms starting with text that having the highest
     * document count for the given fields plus those the directly match text regardless of
     * the respective document count.
     * @param text the prefix
     * @param fields fields in which to look for prefix
     * @return pseudo prefix query
     * @throws IOException if query construction fails
     */
    private Query getPseudoPrefixQueryForFields(String text, List<String> fields)
            throws IOException {
        final PagedResultSorter<TermWithFreq> prs =
                new PagedResultSorter<>(0, this.numTermsForPseudoPrefixQuery, null);

        final BooleanQuery result = new BooleanQuery();

        // for all fields, look at all terms starting with text
        for (String field : fields) {
            final Term t = new Term(field, text);
            final TermEnum termEnum = this.indexReader.terms(t);
            try {
                if (termEnum.term() == null) {
                    continue;
                }
                if (text.equals(termEnum.term().text())) {
                    // always add perfect match regardless of frequency
                    result.add(new TermQuery(new Term(field, text)), BooleanClause.Occur.SHOULD);
                    if (!termEnum.next()) {
                        continue;
                    }
                }
                while (termEnum.term().text().startsWith(text)) {
                    prs.add(new TermWithFreq(field, termEnum.term().text(), termEnum.docFreq()));
                    if (!termEnum.next()) {
                        break;
                    }
                }
            }
            finally {
                termEnum.close();
            }
        }

        // add field/termTexts with the highest document frequency
        final List<TermWithFreq> terms = prs.getResult();
        for (TermWithFreq termWithFreq : terms) {
            result.add(new TermQuery(new Term(termWithFreq.getField(), termWithFreq.getText())),
                    BooleanClause.Occur.SHOULD);
        }

        return result;
    }

    /**
     * Returns a query (fields[0]:text* fields[1]:text* ... fields[n]:text*), which will
     * be expanded by the searcher and thus may cause a TooManyClauses Exception.
     * @param text prefix to look for
     * @param fields fields in which to look for prefix
     * @return prefix query
     */
    private Query getPrefixQueryForFields(final String text, List<String> fields) {
        final BooleanQuery booleanQuery = new BooleanQuery();
        for (final String field : fields) {
            if (text.length() <= 2) {
                booleanQuery.add(new TermQuery(new Term(field, text)), BooleanClause.Occur.SHOULD);
            }
            else {
                if (text.length() >= getMinPrefixQueryLength(field)) {
                    booleanQuery.add(new PrefixQuery(new Term(field, text)), BooleanClause.Occur.SHOULD);
                }
            }
        }
        return booleanQuery;
    }


    private int getMinPrefixQueryLength(String field) {
        final Integer length = MIN_PREFIX_QUERY_LENGTH.get(field);
        return length == null ? 2 : length;
    }

    private Map<String, FuzzyMatches> getFuzzyMatches(
            final List<String> tokensSplitted) throws IOException {
        final Map<String, FuzzyMatches> mpcs = new HashMap<>();
        for (final String token : tokensSplitted) {
            final FuzzyMatches fm = new FuzzyMatches(token);

            final FuzzyQuery fuzzyQuery =
                    new FuzzyQuery(new Term(FIELDNAME_MPC_VALUE, token), minimumSimilarity);
            final Query query = fuzzyQuery.rewrite(this.mpcIndexReader);
            if (query instanceof BooleanQuery) {
                final BooleanQuery bq = (BooleanQuery) query;
                final BooleanClause[] clauses = bq.getClauses();
                for (final BooleanClause booleanClause : clauses) {
                    final float boost = booleanClause.getQuery().getBoost();
                    final String text = ((TermQuery) booleanClause.getQuery()).getTerm().text();

                    final FuzzyMatch fuzzyMatch = new FuzzyMatch(text, boost);
                    fm.add(fuzzyMatch);
                }
            }
            else if (query instanceof TermQuery) {
                final TermQuery tq = (TermQuery) query;
                final FuzzyMatch fuzzyMatch = new FuzzyMatch(tq.getTerm().text(), tq.getBoost());
                fm.add(fuzzyMatch);
            }
            else {
                this.logger.warn("<getFuzzyMatches> unknown query class " + query.getClass().getName());
                continue;
            }

            final TopDocs hits = this.mpcIndexSearcher.search(query, null, 100);
            for (ScoreDoc scoreDoc : hits.scoreDocs) {
                final Document doc = this.mpcIndexSearcher.doc(scoreDoc.doc);
                final Field countField = doc.getField(FIELDNAME_MPC_COUNT);
                final int count = Integer.parseInt(countField.stringValue());
                fm.setCount(doc.getField(FIELDNAME_MPC_VALUE).stringValue(), count);
            }

            mpcs.put(token, fm);
        }
        return mpcs;
    }

    private BooleanQuery getMpcQuery(final Map<String, FuzzyMatches> mpcs,
            final List<String> defaultFields, int levelIncrement,
            final BooleanClause.Occur useAnd, boolean usePrefix) throws IOException {

        final List<String> fields = new ArrayList<>(defaultFields);
        if (fields.contains(IndexConstants.FIELDNAME_NAME)) {
            fields.add(FIELDNAME_NAMES);
        }

        final BooleanQuery query = new BooleanQuery();
        for (final FuzzyMatches fm : mpcs.values()) {
            if (fm.isEmpty()) {
                query.add(getQueryForFields(fm.getSearchTerm(), fields, usePrefix), useAnd);
            }
            else {
                final List<String> topRanked = getTopRankedMatches(fm, levelIncrement);

                final BooleanQuery fuzzyQuery = new BooleanQuery();
                for (final String str : topRanked) {
                    fuzzyQuery.add(getQueryForFields(str, fields, usePrefix), BooleanClause.Occur.SHOULD);
                }
                fuzzyQuery.add(getQueryForFields(fm.getSearchTerm(), fields, usePrefix), BooleanClause.Occur.SHOULD);
                query.add(fuzzyQuery, useAnd);
            }
        }
        return query;
    }

    private Map<String, List<String>> getTermSubstitutions(final Map<String, FuzzyMatches> mpcs,
            int levelIncrement) {

        final Map<String, List<String>> termSubstitutions = new HashMap<>();
        for (final FuzzyMatches fm : mpcs.values()) {
            final List<String> topRanked = getTopRankedMatches(fm, levelIncrement);
            topRanked.remove(fm.getSearchTerm());
            if (!topRanked.isEmpty()) {
                termSubstitutions.put(fm.getSearchTerm(), topRanked);
            }
        }
        return termSubstitutions;
    }

    private List<String> getTopRankedMatches(final FuzzyMatches fm, int levelIncrement) {
        final float boost = fm.getTopBoost();
        final float difference = (boost * (1 - minimumSimilarity) + minimumSimilarity);
        final int level = (difference > 0.75f ? 1 : difference > 0.6f ? 2 : 3) + levelIncrement;
        return fm.getTopRanked(level);
    }

    public static void main(String[] args) throws Exception {
        final List<String> DEFAULT_FIELDS = Arrays.asList("name",
            KeysystemEnum.WM_WP_NAME_KURZ.name().toLowerCase(),
            KeysystemEnum.WM_WP_NAME.name().toLowerCase(),
            KeysystemEnum.ISIN.name().toLowerCase(),
            KeysystemEnum.WKN.name().toLowerCase(),
            KeysystemEnum.VWDCODE.name().toLowerCase(),
            KeysystemEnum.INFRONT_ID.name().toLowerCase(),
            KeysystemEnum.WM_TICKER.name().toLowerCase());

        final String home = System.getProperty("user.home");

        File f = new File(home + "/produktion/var/data/instrument/work0/data/instruments/");
        InstrumentDirDao dao = new InstrumentDirDao(f);

        final InstrumentSearcherImpl searcher =
            new InstrumentSearcherImpl(new File(home + "/produktion/var/data/instrument/work0"),
                dao, null);

        final Profile p = ProfileFactory.valueOf(true);

        final SearchRequestStringBased r = new SearchRequestStringBased();
        r.setProfile(p);
//        r.setDefaultFields(DEFAULT_FIELDS);
        r.setDefaultFields(DEFAULT_FIELDS);
        r.setCountTypes(EnumSet.allOf(InstrumentTypeEnum.class));
//        r.setFilterTypes(EnumSet.allOf(InstrumentTypeEnum.class));
        r.setCountInstrumentResults(false);
        r.setMaxNumResults(500);
        r.setPagingCount(10);
        r.setPagingOffset(0);
        r.setUsePaging(true);
        r.setSearchExpression("2057;BYW6");
        r.setResultType(SearchRequestResultType.QUOTE_ANY);

        for (int j = 0; j < 1; j++) {
            TimeTaker tt = new TimeTaker();
            final SearchResponse resp = searcher.simpleSearch(r);
            System.out.println("took " + tt + ", " + resp.getTypeCounts());

            for (Instrument instrument : resp.getInstruments()) {
                System.out.println(instrument.getId() + " " + instrument.getInstrumentType().name()
                    + " " + instrument.getName());
            }

            for (Quote q : resp.getQuotes()) {
                System.out.println(q.getInstrument().getId() + "  " + q.getId()
                    + " " + q.getInstrument().getInstrumentType().name()
                    + " " + q.getSymbolVwdcode()
                    + " " + q.getSymbolInfrontId()
                );
            }
            System.out.println(resp.getInstrumentCount());
        }
    }
}
