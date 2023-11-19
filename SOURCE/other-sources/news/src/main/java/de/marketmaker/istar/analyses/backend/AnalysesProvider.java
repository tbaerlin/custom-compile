/*
* AnalysesProvider.java
*
* Created on 21.03.12 10:22
*
* Copyright (c) market maker Software AG. All Rights Reserved.
*/
package de.marketmaker.istar.analyses.backend;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.CachingWrapperFilter;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.QueryWrapperFilter;
import org.apache.lucene.search.TermQuery;
import org.joda.time.DateTime;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;
import org.springframework.util.StringUtils;

import de.marketmaker.istar.analyses.analyzer.AnalysesCollector;
import de.marketmaker.istar.analyses.backend.Protos.Analysis;
import de.marketmaker.istar.analyses.backend.Protos.Analysis.Builder;
import de.marketmaker.istar.analyses.backend.Protos.Analysis.Provider;
import de.marketmaker.istar.analyses.frontend.AnalysesIndexConstants;
import de.marketmaker.istar.analyses.frontend.AnalysesMetaRequest;
import de.marketmaker.istar.analyses.frontend.AnalysesRequest;
import de.marketmaker.istar.analyses.frontend.AnalysesSummaryResponse;
import de.marketmaker.istar.analyses.frontend.AnalysisImageRequest;
import de.marketmaker.istar.analyses.frontend.AnalysisImageResponse;
import de.marketmaker.istar.analyses.frontend.AnalysisImpl;
import de.marketmaker.istar.analyses.frontend.AnalysisResponse;
import de.marketmaker.istar.common.util.CollectionUtils;
import de.marketmaker.istar.common.util.FileUtil;
import de.marketmaker.istar.common.util.PagedResultSorter;
import de.marketmaker.istar.common.util.TimeTaker;
import de.marketmaker.istar.domain.data.StockAnalysis.Recommendation;
import de.marketmaker.istar.domain.data.StockAnalysisSummary;
import de.marketmaker.istar.domain.profile.Selector;
import de.marketmaker.istar.news.backend.DeleteQueriesParser;
import de.marketmaker.istar.news.frontend.NewsIndexConstants;

/**
 * @author oflege
 */
public abstract class AnalysesProvider implements InitializingBean, DisposableBean {

    protected final Logger logger = LoggerFactory.getLogger(getClass().getName() + "." + getId());

    protected static final Collator GERMAN_COLLATOR = Collator.getInstance(Locale.GERMAN);

    protected static final long INVALID_ANALYSIS_ID = 0L;

    private Filter ignoreWithoutRatingFilter;

    private final Map<Long, AnalysisSummary> summaries = new HashMap<>(1000);

    private final Map<Long, InstrumentAnalyses> instrumentAnalyses = new HashMap<>();

    // data sink for analyzer data processing
    private AnalysesCollector analysesCollector = AnalysesCollector.NULL;

    protected AnalysesDao dao;

    private AnalysesDao backupDao;

    // needed to run a sync with the backup DB when re-starting
    private ExecutorService executor = Executors.newSingleThreadExecutor();

    private AnalsysesIndex index;

    protected File baseDir;

    protected File sourceNameMappingsFile;

    private Resource deleteQueriesSource;

    protected Map<String, String> sourceNameMappings = Collections.emptyMap();

    private final Object mutex = new Object();

    public void setDeleteQueriesSource(Resource deleteQueriesSource) {
        this.deleteQueriesSource = deleteQueriesSource;
    }

    public void setDao(AnalysesDao dao) {
        this.dao = dao;
    }

    public void setCollector(AnalysesCollector analysesCollector) {
        this.analysesCollector = analysesCollector;
    }

    public void setBackupDao(AnalysesDao backupDao) {
        this.backupDao = backupDao;
    }

    public void setExecutor(ExecutorService executor) {
        this.executor = executor;
    }

    static long decodeId(String id) {
        return Long.parseLong(id, Character.MAX_RADIX);
    }

    static String encodeId(long id) {
        return Long.toString(id, Character.MAX_RADIX);
    }

    static String encodeShortId(long id) {
        return (encodeId(id) + "zzzzzz").substring(0, 7);
    }

    static File ensureDir(File dir) {
        if (!FileUtil.isDirWriteable(dir)) {
            throw new IllegalStateException("invalid dir '" + dir.getAbsolutePath() + "'");
        }
        return dir;
    }

    public void setSourceNameMappingsFile(File sourceNameMappingsFile) {
        this.sourceNameMappingsFile = sourceNameMappingsFile;
    }

    public void setBaseDir(File baseDir) {
        this.baseDir = ensureDir(new File(baseDir, getId()));
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (this.sourceNameMappingsFile != null) {
            this.sourceNameMappings = loadMappings();
            this.logger.info("<afterPropertiesSet> sourceNameMappings = " + sourceNameMappings);
        }

        this.ignoreWithoutRatingFilter = createWithoutRecommendationFilter();

        final File indexDir = ensureDir(new File(this.baseDir, "index"));
        this.index = new AnalsysesIndex(indexDir);

        initFromDao();
    }

    protected void dumpState(File f) throws IOException {
        try (final PrintWriter pw = new PrintWriter(f, "UTF-8")) {
            synchronized (this.mutex) {
                final ArrayList<Long> iids = new ArrayList<>(this.instrumentAnalyses.keySet());
                iids.sort(null);
                for (Long iid : iids) {
                    pw.println(iid);
                    final InstrumentAnalyses ia = this.instrumentAnalyses.get(iid);
                    for (String source : ia.getSources()) {
                        pw.append(" ").append(source).println();
                        AnalysisSummary current = ia.getForSource(source);
                        while (current != null) {
                            pw.append("  ").append(Long.toString(current.getId()))
                                    .append(" ").append(new DateTime(current.getDate()).toString());
                            if (!current.getSource().equals(source)) {
                                pw.append(" SOURCE=").append(current.getSource());
                            }
                            if (!summaries.containsKey(current.getId())) {
                                pw.append(" ORPHAN");
                            }
                            pw.println();
                            current = current.getPrevious();
                        }
                    }
                }
            }
        }
    }

    protected int size() {
        synchronized (this.mutex) {
            return this.summaries.size();
        }
    }

    protected int sizeOverInstruments() {
        synchronized (this.mutex) {
            int n = 0;
            for (InstrumentAnalyses ia : instrumentAnalyses.values()) {
                n += ia.size();
            }
            return n;
        }
    }

    protected Filter createWithoutRecommendationFilter() throws ParseException {
        final BooleanQuery query = new BooleanQuery();
        query.add(new MatchAllDocsQuery(), Occur.SHOULD);
        query.add(new TermQuery(new Term(AnalysesIndexConstants.FIELD_RECOMMENDATION,
                Recommendation.NONE.name())), Occur.MUST_NOT);
        return new CachingWrapperFilter(new QueryWrapperFilter(query));
    }

    private void initFromDao() throws IOException {
        final Set<Long> idsInIndex = getAllDocumentIds();
        this.logger.info("<initFromDao> read " + idsInIndex.size() + " ids from index");

        final int[] num = new int[2];

        this.dao.getAllItems(getProvider(), record -> {
            if (!idsInIndex.remove(record.getId())) {
                addToIndex(record);
            } else {
                num[1]++;
            }
            addToSummaries(record);
            num[0]++;
        });

        this.logger.info("<initFromDao> read " + num[0] + " records from dao,"
                + " found " + num[1] + " matching ids in index");
        afterSyncWithDb(idsInIndex);
    }

    /**
     * store incoming analysis, returns the analysis id on success,
     * returning INVALID_ANALYSIS_ID if the analysis couldn't be stored
     */
    protected long addAnalysis(Builder builder) {
        final Analysis analysis = addId(builder).build();
        final boolean success = addAnalysis(analysis);
        return success ? analysis.getId() : INVALID_ANALYSIS_ID;
    }

    // at this point the analyses need to be complete because it is inserted into the DB
    // and search index
    protected boolean addAnalysis(Analysis analysis) {
        synchronized (this.mutex) {
            if (this.dao.insertAnalysis(analysis)) {
                addToIndex(analysis);      // add to search index
                addToSummaries(analysis);  //
                addToCollector(analysis);  // backend for awp analyzer ("summaries-ng")
                return true;
            }
            return false;
        }
    }

    private void addToCollector(Analysis analysis) {
        analysesCollector.addAnalysis(getProvider(), analysis);
    }

    protected void addImage(long id, String name, byte[] data) {
        this.dao.insertImage(getProvider(), id, name, data);
    }

    protected int deleteAnalysis(String agencyId) throws IOException {
        final TermQuery query
                = new TermQuery(new Term(AnalysesIndexConstants.FIELD_AGENCY_ID, agencyId));
        return deleteAnalyses(query);
    }

    private int deleteAnalyses(Query query) throws IOException {
        final Set<Long> documentIds;
        synchronized (this.mutex) {
            documentIds = this.index.getDocumentIds(query);
            if (documentIds.isEmpty()) {
                return 0;
            }
            deleteSummaries(documentIds);
            this.index.delete(documentIds);
        }
        return this.dao.deleteItems(getProvider(), new ArrayList<>(documentIds));
    }

    private void deleteSummaries(Set<Long> documentIds) {
        for (Long documentId : documentIds) {
            final AnalysisSummary summary = this.summaries.remove(documentId);
            if (summary != null) {
                final InstrumentAnalyses ia = this.instrumentAnalyses.get(summary.getInstrumentid());
                if (ia != null) {
                    ia.remove(summary);
                    if (ia.isEmpty()) {
                        this.instrumentAnalyses.remove(summary.getInstrumentid());
                    }
                }
            }
        }
    }

    protected String getMappedSourceName(String name) {
        final String mapped = this.sourceNameMappings.get(name);
        return (mapped != null) ? mapped : name;
    }

    private Map<String, String> loadMappings() throws IOException {
        final Map<String, String> result = new HashMap<>();
        try (Scanner sc = new Scanner(this.sourceNameMappingsFile, "utf8")) {
            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                if (!StringUtils.hasText(line) || line.startsWith("#")) {
                    continue;
                }
                String[] split = line.split("=", 2);
                if (split.length == 2) {
                    result.put(split[0].trim(), split[1].trim());
                }
            }
        }
        return result;
    }

    @Override
    public void destroy() throws Exception {
        this.index.destroy();
        this.executor.shutdown();
        if (!this.executor.awaitTermination(10, TimeUnit.SECONDS)) {
            this.logger.warn("<destroy> executor did not terminate");
        }
    }

    abstract String getId(); // called during object initialization

    abstract Map<String, Map<String, String>> doGetMetaData(AnalysesMetaRequest request);

    Map<String, Map<String, String>> getMetaData(AnalysesMetaRequest request) throws IOException {
        synchronized (this.mutex) {
            this.index.flush();
            return doGetMetaData(request);
        }
    }

    private InstrumentAnalyses getInstrumentAnalyses(long instrumentid) {
        return this.instrumentAnalyses.get(instrumentid);
    }

    public Builder addId(Builder builder) {
        return AnalysisIdFactory.addId(builder);
    }

    private void addToSummaries(Analysis analysis) {
        synchronized (this.mutex) {
            final AnalysisSummary summary = new AnalysisSummary(analysis, getMappedSourceName(analysis.getSource()));
            add(summary);
        }
    }

    private void add(AnalysisSummary summary) {
        this.summaries.put(summary.getId(), summary);

        final InstrumentAnalyses ia = this.instrumentAnalyses.get(summary.getInstrumentid());
        if (ia == null) {
            this.instrumentAnalyses.put(summary.getInstrumentid(), new InstrumentAnalyses(summary));
        }
        else {
            ia.add(summary);
        }
    }

    private void addToIndex(Analysis analysis) {
        final Document document = Analysis2Document.toDocument(analysis);
        this.index.add(document);
    }


    private Set<Long> getAllDocumentIds() throws IOException {
        return this.index.getDocumentIds(new MatchAllDocsQuery());
    }

    private Set<Long> search(AnalysesRequest request) throws Exception {
        if (request.getOffset() >= this.summaries.size()) {
            return Collections.emptySet();
        }

        if (request.getQuery() != null) {
            final Filter filter = request.isIgnoreAnalysesWithoutRating()
                    ? this.ignoreWithoutRatingFilter : null;
            return this.index.getDocumentIds(request.getQuery(), filter);
        }
        else if (request.getInstrumentIds() != null) {
            final HashSet<Long> result = new HashSet<>();
            for (Long iid : request.getInstrumentIds()) {
                InstrumentAnalyses ia = this.instrumentAnalyses.get(iid);
                if (ia != null) {
                    ia.addDocumentIds(result);
                }
            }
            return result;
        }
        else {
            return searchSummaries(request);
        }
    }

    private Set<Long> searchSummaries(AnalysesRequest request) {
        if (request.isIgnoreAnalysesWithoutRating()) {
            return searchSummariesWithRating();
        }
        else {
            return searchAllSummaries();
        }
    }

    protected HashSet<Long> searchAllSummaries() {
        return new HashSet<>(this.summaries.keySet());
    }

    protected Set<Long> searchSummariesWithRating() {
        final Set<Long> result = new HashSet<>();
        for (Entry<Long, AnalysisSummary> entry : summaries.entrySet()) {
            if (entry.getValue().hasRecommendation()) {
                result.add(entry.getKey());
            }
        }
        return result;
    }


    protected Comparator<AnalysisSummary> createSorter(AnalysesRequest request) {
        String sortBy = request.getSortBy();
        boolean asc = request.isAscending();

        Comparator<AnalysisSummary> c;
        if ("sector".equals(sortBy)) {
            c = AnalysisSummary.BY_BRANCH;
        }
        else if ("source".equals(sortBy)) {
            c = AnalysisSummary.BY_SOURCE;
        }
        else if ("ratingid".equals(sortBy)) {
            c = AnalysisSummary.BY_RATING;
        }
        else {
            c = AnalysisSummary.BY_DATE;
        }

        final boolean addDate = (c != AnalysisSummary.BY_DATE);

        if (!asc) {
            c = Collections.reverseOrder(c);
        }

        if (addDate) {
            //noinspection unchecked
            return CollectionUtils.chain(c, Collections.reverseOrder(AnalysisSummary.BY_DATE));
        }

        return c;
    }

    private void afterSyncWithDb(Set<Long> idsInIndex) throws IOException {
        if (!idsInIndex.isEmpty()) {
            this.index.delete(idsInIndex);
            this.logger.info("<afterSyncWithDb> deleted " + idsInIndex.size() + " from index");
        }
        this.index.optimize();
        this.logger.info("<afterSyncWithDb> " + this.summaries.size() + " summaries for "
                + this.instrumentAnalyses.size() + " instruments");

        final Set<String> sources = new TreeSet<>();
        for (AnalysisSummary summary : this.summaries.values()) {
            sources.add(summary.getSource());
        }
        this.logger.info("<afterSyncWithDb> " + sources.size() + " sources: " + sources);
    }

    protected void addSources(Set<String> dest, boolean ignoreAnalysesWithoutRating) {
        synchronized (this.mutex) {
            for (AnalysisSummary summary : this.summaries.values()) {
                if (!ignoreSummary(summary, ignoreAnalysesWithoutRating)) {
                    dest.add(summary.getSource());
                }
            }
        }
    }

    private boolean ignoreSummary(AnalysisSummary summary, boolean ignoreAnalysesWithoutRating) {
        return ignoreAnalysesWithoutRating && !summary.hasRecommendation();
    }

    protected void addRegions(Set<String> dest, boolean ignoreAnalysesWithoutRating) {
        synchronized (this.mutex) {
            for (AnalysisSummary summary : this.summaries.values()) {
                if (StringUtils.hasText(summary.getRegion())
                        && !ignoreSummary(summary, ignoreAnalysesWithoutRating)) {
                    dest.add(summary.getRegion());
                }
            }
        }
    }

    protected void addBranches(Set<String> dest, boolean ignoreAnalysesWithoutRating) {
        synchronized (this.mutex) {
            for (AnalysisSummary summary : this.summaries.values()) {
                if (StringUtils.hasText(summary.getBranch())
                        && !ignoreSummary(summary, ignoreAnalysesWithoutRating)) {
                    dest.add(summary.getBranch());
                }
            }
        }
    }

    protected LinkedHashMap<String, String> getSources(boolean ignoreAnalysesWithoutRating) {
        final Set<String> sources = new TreeSet<>(GERMAN_COLLATOR);
        addSources(sources, ignoreAnalysesWithoutRating);
        return toMap(sources);
    }


    protected LinkedHashMap<String, String> toMap(Set<String> aSet) {
        final LinkedHashMap<String, String> result = new LinkedHashMap<>();
        for (String element : aSet) {
            result.put(element, element);
        }
        return result;
    }


    public abstract Selector getSelector();

    public abstract Provider getProvider();

    private Set<Long> getIids(Set<Long> ids) {
        final Set<Long> result = new HashSet<>();
        for (Long id : ids) {
            final AnalysisSummary summary = this.summaries.get(id);
            if (summary != null) {
                result.add(summary.getInstrumentid());
            }
        }
        return result;
    }

    AnalysesSummaryResponse getSummaries(AnalysesRequest request) throws IOException {
        synchronized (this.mutex) {
            this.index.flush();
            return doGetSummaries(request);
        }
    }

    private AnalysesSummaryResponse doGetSummaries(AnalysesRequest request) {
        final TimeTaker tt = new TimeTaker();
        final AnalysesSummaryResponse result = new AnalysesSummaryResponse();

        try {
            final Set<Long> ids = search(request);
            final Set<Long> iids = getIids(ids);

            for (Long iid : iids) {
                final InstrumentAnalyses ia = getInstrumentAnalyses(iid);
                if (ia == null) {
                    continue;
                }
                StockAnalysisSummary summary = ia.getSummary(ids);
                if (request.isIgnoreAnalysesWithoutRating() && (summary == null || summary.getNumberOfAnalyses() == 0)) {
                    continue;
                }
                result.add(iid, summary, ia.getAims(ids), getSectorName(ia.getSector()));
            }

            if (this.logger.isDebugEnabled()) {
                this.logger.debug("<getSummaries> for " + request + ": #ids=" + ids.size()
                        + ", #iids=" + iids.size() + ", took " + tt);
            }

        } catch (Exception e) {
            this.logger.error("<getSummary> failed for " + request, e);
            result.setInvalid();
        }

        return result;
    }

    protected String getSectorName(String sector) {
        return sector;
    }

    AnalysisResponse getAnalyses(AnalysesRequest request) {
        final AnalysisResponse response = new AnalysisResponse();

        final List<Long> ids;
        if (request.getAnalysisIds() != null) {
            ids = checkAnalysesIds(request.getAnalysisIds());
            response.setTotalCount(ids.size());
        }
        else {
            try {
                synchronized (this.mutex) {
                    this.index.flush();
                    ids = doGetAnalyses(request, response);
                }
            } catch (Exception e) {
                this.logger.error("<getAnalyses> failed", e);
                response.setInvalid();
                return response;
            }
        }

        if (!ids.isEmpty()) {
            response.setAnalyses(getAnalyses(ids));
        }

        return response;
    }

    private List<Long> checkAnalysesIds(List<String> analysisIds) {
        List<Long> ids = new ArrayList<>(analysisIds.size());
        for (String idStr : analysisIds) {
            if (idStr.length() == 7) {
                final Set<Long> docIds = findByShortId(idStr);
                if (docIds.size() == 1) {
                    ids.add(docIds.iterator().next());
                    continue;
                }
            }

            ids.add(decodeId(idStr));
        }
        return ids;
    }

    private Set<Long> findByShortId(String idStr) {
        final Term t = new Term(NewsIndexConstants.FIELD_SHORTID, idStr);
        synchronized (this.mutex) {
            try {
                this.index.flush();
                return this.index.getDocumentIds(new TermQuery(t));
            } catch (IOException e) {
                this.logger.error("<findByShortId> failed", e);
                return Collections.emptySet();
            }
        }
    }

    private List<Long> doGetAnalyses(AnalysesRequest request, AnalysisResponse response)
            throws Exception {
        final Set<Long> ids = search(request);

        int searchSize = ids.size();
        int retainSize = searchSize;


        if (true) { // todo: this could be optional by request
            // remove analyses that are replaced by a more recent analysis
            retainMostRecentAnalyses(ids);
            retainSize = searchSize;
        }

        response.setTotalCount(ids.size());

        //ignore paging and return all filtered results
        //to allow client sort by instrument name later
        if ("instrumentname".equals(request.getSortBy())) {
            Map<String, Long> analysisIdsToIids = ids.stream().
                    map(summaries::get).
                    collect(Collectors.toMap(
                            summary -> encodeId(summary.getId()),
                            AnalysisSummary::getInstrumentid
                    ));
            response.setAnalysesToIids(analysisIdsToIids);
            return Collections.emptyList();
        }

        final PagedResultSorter<AnalysisSummary> prs = new PagedResultSorter<>(
                request.getOffset(), request.getCount(), ids.size(), createSorter(request));

        for (Long id : ids) {
            prs.add(this.summaries.get(id));
        }

        final ArrayList<Long> result = new ArrayList<>(prs.size());
        for (AnalysisSummary summary : prs.getResult()) {
            result.add(summary.getId());
        }

        if (this.logger.isDebugEnabled()) {
            this.logger.debug("<doGetAnalyses> searchSize=" + searchSize + ", retainSize=" + retainSize);
        }

        return result;
    }

    private void retainMostRecentAnalyses(Set<Long> ids) {
        final Set<Long> iids = getIids(ids);
        for (Long iid : iids) {
            final InstrumentAnalyses ia = getInstrumentAnalyses(iid);
            if (ia != null) {
                ia.retainMostRecent(ids);
            }
        }
    }

    public List<AnalysisImpl> getAnalyses(List<Long> ids) {
        return this.dao.getItems(getProvider(), ids);
    }

    public AnalysisImageResponse getImage(AnalysisImageRequest request) {
        return new AnalysisImageResponse(this.dao.getImage(getProvider(), request.getName()));
    }

    protected void midnight() throws Exception {
        syncWithBackup(new DateTime().minusDays(2)).get();
        if (this.deleteQueriesSource == null) {
            this.logger.warn("<midnight> undefined deleteQueriesSource");
            return;
        }
        if (!this.deleteQueriesSource.exists()) {
            this.logger.warn("<midnight> cannot read " + this.deleteQueriesSource.getFilename());
            return;
        }
        final List<Query> queries = new DeleteQueriesParser(this.deleteQueriesSource).getQueries();
        int numDeleted = 0;
        for (Query query : queries) {
            final int num = deleteAnalyses(query);
            this.logger.info("<midnight> removed " + num + " analyses for " + query);
            numDeleted += num;
        }
        if (numDeleted > 0) {
            this.index.optimize();
        }
    }

    protected Future<?> syncWithBackup(final DateTime from) {
        return this.executor.submit(() -> doSyncWithBackup(from));
    }

    private void doSyncWithBackup(DateTime from) {
        this.logger.info("<syncWithBackup> from " + from);

        if (this.backupDao == null) {
            this.logger.warn("<syncWithBackup> no backup dao, returning");
            return;
        }

        final Set<Long> ids = getIdsSince(from, this.backupDao);
        if (ids.isEmpty()) {
            this.logger.info("<syncWithBackup> no analyses found");
            return;
        }

        ids.removeAll(getIdsSince(from, this.dao));
        if (ids.isEmpty()) {
            this.logger.info("<syncWithBackup> no missed analyses found");
            return;
        }

        this.logger.info("<indexMissedAnalyses> found " + ids.size() + " missing analyses");
        List<AnalysisImpl> items = this.backupDao.getItems(getProvider(), new ArrayList<>(ids));
        int numAdded = 0;
        for (AnalysisImpl item : items) {
            if (addAnalysis(item.getRawAnalysis())) {
                numAdded++;
            }
        }
        this.logger.info("<syncWithBackup> added " + numAdded + " analyses");
    }

    private HashSet<Long> getIdsSince(DateTime from, final AnalysesDao ad) {
        return new HashSet<>(ad.getIdsSince(getProvider(), from));
    }
}
