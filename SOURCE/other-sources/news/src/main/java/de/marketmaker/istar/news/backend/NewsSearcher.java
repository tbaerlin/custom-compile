/*
 * NewsSearcher.java
 *
 * Created on 15.03.2007 12:24:07
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.news.backend;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.DocIdSet;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.NumericRangeQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.TopFieldCollector;
import org.apache.lucene.search.TopFieldDocs;
import org.apache.lucene.store.AlreadyClosedException;
import org.apache.lucene.util.Version;
import org.joda.time.DateTime;
import org.springframework.util.StopWatch;
import org.springframework.util.StringUtils;

import de.marketmaker.istar.domainimpl.profile.VwdProfile;
import de.marketmaker.istar.news.frontend.LatestNewsRequest;
import de.marketmaker.istar.news.frontend.NewsIndexConstants;
import de.marketmaker.istar.news.frontend.NewsRequest;
import de.marketmaker.istar.news.frontend.NewsResponseImpl;

import static de.marketmaker.istar.news.frontend.NewsIndexConstants.FIELD_SEQ_NO;
import static de.marketmaker.istar.news.frontend.NewsIndexConstants.FIELD_TIMESTAMP;

/**
 * Performs actual news search on a number of indexes.
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
class NewsSearcher {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private static final Sort TIMESTAMP_SORT = new Sort(
            new SortField(FIELD_TIMESTAMP, SortField.INT, true), // desc timestamp
            new SortField(FIELD_SEQ_NO, SortField.INT, true));

    /**
     * Indexes such that news at array position i are more recent than those at i + 1.
     * a copy of this array is in th NewsServerImpl and might get modified
     */
    private final NewsIndex[] indexes;

    /**
     * Used to create the appropriate filter for a NewsRequest
     */
    private final FilterFactory filterFactory;

    // a query filter that would not match anything if evaluated
    protected static final Filter EMPTY_FILTER = new Filter() {
        public DocIdSet getDocIdSet(IndexReader indexReader) throws IOException {
            return DocIdSet.EMPTY_DOCIDSET;
        }
    };

    NewsSearcher(NewsIndex[] indexes, FilterFactory filterFactory) {
        this.indexes = indexes;
        this.filterFactory = filterFactory;
    }

    /**
     * search for the latest news for a set of iids in each of the registered indices
     * the newsRecords need to be set by the caller, this method only returns the newsIds
     *
     * @param request search request containing the iids to search for
     * @param sw Stopwatch for timing each index
     * @return response that contains the news ids of the instruments found in the indices
     *         there is exactly one news id for each instrument
     * @throws IOException
     */
    NewsResponseImpl search(LatestNewsRequest request, StopWatch sw) throws IOException {
        final List<String> iids = request.getIids();
        final boolean[] found = new boolean[iids.size()];
        final List<String> nids = new ArrayList<>(iids.size());
        int numFound = 0;

        LOOP:
        for (int i = 0; i < this.indexes.length; i++) {
            final Filter filter = this.filterFactory.getFilter(this.indexes[i], request);
            if (filter == EMPTY_FILTER) {
                continue;
            }

            sw.start("search-" + i);

            final IndexSearcher indexSearcher = this.indexes[i].getSearcher();
            for (int j = 0; j < iids.size(); j++) {
                if (found[j]) {
                    continue; // skip if we already found something in an earlier index
                }
                final String iid = iids.get(j);
                final TopFieldDocs docs = indexSearcher.search(queryForIid(iid), filter, 1, TIMESTAMP_SORT);
                if (docs.totalHits > 0) {
                    found[j] = true;
                    final Document d = indexSearcher.doc(docs.scoreDocs[0].doc);
                    nids.add(d.get(NewsIndexConstants.FIELD_ID));
                    if (++numFound == iids.size()) {
                        sw.stop();
                        break LOOP;
                    }
                }
            }
            sw.stop();
        }

        final NewsResponseImpl response = new NewsResponseImpl();
        response.setHitCount(-1); // fixed later
        response.setIds(nids);
        return response;
    }

    Optional<String> getPreviousId(String agency, String agencyProvidedId, DateTime timestamp) throws IOException {
        if (StringUtils.isEmpty(agency)
                || (StringUtils.isEmpty(agencyProvidedId))
                || timestamp == null ) {
            return Optional.empty();
        }
        for (int i = 0; i < this.indexes.length; i++) {
            final NewsIndex index = indexes[i];
            try {
                final IndexSearcher indexSearcher = index.getSearcher();
                final TopFieldDocs docs = indexSearcher
                        .search(queryForPreviousId(agency, agencyProvidedId, timestamp), null, 1, TIMESTAMP_SORT);
                if (docs.totalHits > 0) {
                    final Document d = indexSearcher.doc(docs.scoreDocs[0].doc);
                    return Optional.of(d.get(NewsIndexConstants.FIELD_ID));
                }
            } catch (AlreadyClosedException ex) {
                logger.error("<getPreviousId> tried to search for previous news-id but indexSearcher is closed,"
                                + " skipping the current index, more context info: "
                                + " agency: " + agency
                                + " agencyProvidedId: " + agencyProvidedId
                                + " timestamp: " + timestamp
                                + " failed index: " + i
                                + " indexes.length: " + indexes.length
                                + " indexes: " + Arrays.toString(indexes)
                        ,ex);
            }
        }
        return Optional.empty();
    }

    private Query queryForPreviousId(String agency, String agencyProvidedId, DateTime timestamp) {
        BooleanQuery result = new BooleanQuery(true);
        result.add(new TermQuery(new Term(NewsIndexConstants.FIELD_AGENCY, agency)),
                BooleanClause.Occur.MUST);
        result.add(new TermQuery(new Term(NewsIndexConstants.FIELD_AGENCY_PROVIDED_ID, agencyProvidedId)),
                BooleanClause.Occur.MUST);
        result.add(NumericRangeQuery.newIntRange(NewsIndexConstants.FIELD_TIMESTAMP,
                0, News2Document.encodeTimestamp(timestamp),
                true, false),
                BooleanClause.Occur.MUST);
        return result;
    }

    private Query queryForIid(String iid) {
        return new TermQuery(new Term(NewsIndexConstants.FIELD_IID, iid));
    }

    private int[] getIndexAndDocumentId(String offsetId, boolean isShortId) throws IOException {
        final TermQuery q = createOffsetIdQuery(offsetId, isShortId);

        for (int i = 0; i < indexes.length; i++) {
            final NewsIndex index = indexes[i];
            final TopDocs topDocs = index.getSearcher().search(q, 1);
            if (topDocs.totalHits == 1) {
                return new int[]{i, topDocs.scoreDocs[0].doc};
            }
        }
        return null;
    }

    private TermQuery createOffsetIdQuery(String offsetId, boolean isShortId) {
        if (!isShortId) {
            return new TermQuery(new Term(NewsIndexConstants.FIELD_ID, offsetId));
        }
        return new TermQuery(new Term(NewsIndexConstants.FIELD_SHORTID,
                News2Document.encodeShortid(Integer.parseInt(offsetId))));
    }

    /**
     * Performs a search backed by a lucene query, a user-profile specific filter is applied when
     * the search is executed
     *
     * @param request search request containing the
     * @param sw Stopwatch for timing each index
     * @return some matching news records
     * @throws IOException
     */
    NewsResponseImpl search(NewsRequest request, StopWatch sw) throws IOException {
        final Query query = getQuery(request);
        if (query == null) {
            return NewsResponseImpl.getInvalid();
        }

        int[] iad = null;
        if (StringUtils.hasText(request.getOffsetId())) {
            iad = getIndexAndDocumentId(request.getOffsetId(), request.isShortOffsetId());
            if (iad == null) {
                this.logger.warn("<search> invalid offsetId " + request.getOffsetId());
                return new NewsResponseImpl();
            }
        }

        final Sort sort = request.isSortByDate() ? TIMESTAMP_SORT : null;

        final boolean needAllHits = (sort != TIMESTAMP_SORT)
                || (request.isWithHitCount())
                || (StringUtils.hasText(request.getOffsetId()));


        final TopDocs[] topDocs = new TopDocs[this.indexes.length];

        int numHits = 0;
        // + 1 so that the nextPageRequestId can be computed
        final int hitsToGet = request.getOffset() + request.getCount() + 1;

        for (int i = 0; i < this.indexes.length; i++) {
            if (request.getTo() != null && request.getTo().isBefore(this.indexes[i].getFrom())) {
                continue;
            }
            if (request.getFrom() != null && request.getFrom().isAfter(this.indexes[i].getTo())) {
                continue;
            }

            final Filter f = this.filterFactory.getFilter(this.indexes[i], request);
            if (f == EMPTY_FILTER) {
                continue;
            }

            sw.start("search-" + i);

            // if we search with offsetId (iad != null), we have no way of knowing
            // how many docs precede the doc with that id and we therefore have to know the
            // max number of possible hits. That would either be IndexReader.numDocs, or
            // we just evaluate query and f w/o sorting, which is what countHints does. Performance
            // should be ok, countHits allows to pre-evaluate and cache all (sub)filters.
            IndexSearcher indexSearcher = this.indexes[i].getSearcher();
            final int stillToGet = (iad == null)
                    ? hitsToGet - numHits
                    : countHits(indexSearcher, query, f);

            final TopFieldCollector collector =
                    TopFieldCollector.create(sort, Math.max(1, stillToGet), true, false, false, false);

            indexSearcher.search(query, f, collector);
            sw.stop();

            if (stillToGet > 0) {
                topDocs[i] = collector.topDocs();
            }
            numHits += collector.topDocs().totalHits;
            if (!needAllHits && numHits > hitsToGet) {
                break;
            }
        }

        sw.start("collect");
        final NewsHitCollector hitCollector = getNewsIds(request, iad, topDocs);
        sw.stop();

        final NewsResponseImpl response = new NewsResponseImpl();
        response.setHitCount(needAllHits ? numHits : -1);
        response.setIds(hitCollector.getIds());
        response.setPrevPageRequestId(hitCollector.getPrevPageRequestId());
        response.setNextPageRequestId(hitCollector.getNextPageRequestId());

        if (this.logger.isDebugEnabled() && !request.isSilent()) {
            logRequest(request, query, response);
        }

        return response;
    }

    private int countHits(IndexSearcher indexSearcher, Query query, Filter f) throws IOException {
        final TotalHitCountCollector collector = new TotalHitCountCollector();
        indexSearcher.search(query, f, collector);
        return collector.getTotalHits();
    }

    private void logRequest(NewsRequest request, Query query, NewsResponseImpl response) {
        final StringBuilder sb = new StringBuilder(200);
        sb.append("query = '").append(query).append("' ");
        if (request.getProfile() instanceof VwdProfile) {
            final VwdProfile vp = (VwdProfile) request.getProfile();
            sb.append(vp.getAppId()).append(":").append(vp.getVwdId());
        }
        else {
            sb.append(request.getProfile().getName());
        }
        sb.append(" #").append(response.getIds().size()).append(" of ").append(response.getHitCount());
        this.logger.debug(sb.toString());
    }

    private Query getQuery(NewsRequest request) {
        if (request.getLuceneQuery() != null) {
            return request.getLuceneQuery();
        }
        if (!StringUtils.hasText(request.getQuery())) {
            return new MatchAllDocsQuery();
        }
        try {
            return new QueryParser(Version.LUCENE_24, NewsIndexConstants.FIELD_ID,
                    NewsQuerySupport.createAnalyzer()).parse(request.getQuery());
        } catch (ParseException e) {
            this.logger.warn("<getQuery> failed to parse query in " + request);
            return null;
        }
    }

    private int getFirstHitIdx(ScoreDoc[] scoreDocs) {
        for (int i = 0; i < scoreDocs.length; i++) {
            if (scoreDocs[i] != null) return i;
        }
        return -1;
    }

    private int getBestHitIdx(ScoreDoc[] scoreDocs) throws IOException {
        int n = -1;
        for (int i = 0; i < scoreDocs.length; i++) {
            if (scoreDocs[i] != null && (n == -1 || scoreDocs[i].score > scoreDocs[n].score)) {
                n = i;
            }
        }
        return n;
    }

    private NewsHitCollector getNewsIds(NewsRequest request, int[] iad,
                                        TopDocs[] topDocs) throws IOException {
        // current index for topDocs[i].scoreDocs
        final int[] n = new int[topDocs.length];
        // max index for topDocs[i].scoreDocs
        final int[] m = new int[topDocs.length];
        // current ScoreDoc for each topDocs, null if n[i] == m[i]
        final ScoreDoc[] scoreDocs = new ScoreDoc[topDocs.length];

        for (int i = 0; i < topDocs.length; i++) {
            m[i] = topDocs[i] != null ? topDocs[i].scoreDocs.length : 0;
            scoreDocs[i] = m[i] > 0 ? topDocs[i].scoreDocs[0] : null;
        }

        final NewsHitCollector nhc
                = NewsHitCollectorFactory.getHitCollector(getSearchers(), request, iad);

        while (nhc.canCollectMore()) {
            final int i = request.isSortByDate()
                    ? getFirstHitIdx(scoreDocs) : getBestHitIdx(scoreDocs);
            if (i == -1) {
                break;
            }

            nhc.add(i, topDocs[i].scoreDocs[n[i]].doc);

            n[i]++;
            scoreDocs[i] = n[i] < m[i] ? topDocs[i].scoreDocs[n[i]] : null;
        }

        return nhc;
    }

    private IndexSearcher[] getSearchers() {
        final IndexSearcher[] result = new IndexSearcher[this.indexes.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = this.indexes[i].getSearcher();
        }
        return result;
    }

}
