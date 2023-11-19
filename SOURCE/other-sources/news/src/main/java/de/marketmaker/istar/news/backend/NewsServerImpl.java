/*
 * NewsServerImpl.java
 *
 * Created on 15.03.2007 11:34:41
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.news.backend;

import static de.marketmaker.istar.news.backend.News2Document.encodeShortid;

import com.netflix.servo.annotations.DataSourceType;
import com.netflix.servo.annotations.Monitor;
import de.marketmaker.istar.common.util.TimeTaker;
import de.marketmaker.istar.domain.data.PriceQuality;
import de.marketmaker.istar.domain.data.SnapField;
import de.marketmaker.istar.domain.profile.Profile;
import de.marketmaker.istar.domainimpl.TestProfile;
import de.marketmaker.istar.feed.vwd.VwdFieldDescription;
import de.marketmaker.istar.news.data.NewsRecordImpl;
import de.marketmaker.istar.news.frontend.LatestNewsRequest;
import de.marketmaker.istar.news.frontend.NewsIndexConstants;
import de.marketmaker.istar.news.frontend.NewsRecord;
import de.marketmaker.istar.news.frontend.NewsRequest;
import de.marketmaker.istar.news.frontend.NewsRequestBase;
import de.marketmaker.istar.news.frontend.NewsResponse;
import de.marketmaker.istar.news.frontend.NewsResponseImpl;
import de.marketmaker.istar.news.frontend.NewsServer;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import net.jcip.annotations.GuardedBy;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.store.FSDirectory;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.context.SmartLifecycle;
import org.springframework.core.io.Resource;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.util.StopWatch;

/**
 * Controls news indexing and searching. The main problem is that news are added continuously,
 * so that in order to be able to search for the latest news the indexReader has to be
 * recreated frequently. This renders the content of lucene's FieldCacheImpl useless, as
 * it caches fields keyed by readers. The content of the FieldCache is important for sorting
 * index elements based on arbitrary fields such as timestamps in our case. Unfortunately,
 * it is quite expensive to fill the FieldCache with timestamps in case the index is
 * unoptimized (more than 10s if the index contains a year's news); however, frequent
 * optimizing is also not an option as it takes minutes to optimize a somewhat larger
 * index (more than 1GB). The solution is therefore to use TWO indices, one for today's
 * news and one for all previous news. Only the indexReader for today's news has to be
 * updated frequently, but due to its comparatively small size filling the FieldCache will
 * be fast enough. Every midnight, the contents of today's (or rather yesterday's) index
 * will be added to the historic index.
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
@ManagedResource
public class NewsServerImpl implements NewsRecordHandler, NewsServer, InitializingBean,
        DisposableBean, SmartLifecycle {

    // For news from agencies below we lookup the indices for the latest older news with same
    // agency and id to get the hash value (= id ) of that news for the previousId field
    // and save it into the database along with the new record because the client
    // needs to be able to detect updates
    private Collection<String> UPDATING_AGENCIES = Collections.singleton("dpa");

    // R-74763 - sometimes, a pushed news is not immediately searchable even if the
    // index is committed and a new searcher created. This map contains a mapping from
    // shortid to id for recent news items to be able to answer those requests
    private final Map<String, String> recentNews
            = Collections.synchronizedMap(new LinkedHashMap<String, String>() {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, String> eldest) {
            return size() > 256;
        }
    });

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private static final String HISTORIC_INDEX_DIR = "historicIndex";

    private static final String INDEX_DIR = "index";

    /**
     * used to store news indexed by this server
     */
    private NewsDao newsDao;

    /**
     * used to retrieve news that have not been indexed because this object was not running
     */
    private NewsDao backupDao;

    private News2Document news2Document;

    private NewsSymbolIdentifier symbolIdentifier;

    /**
     * Contains either two or three NewsIndex elements, indexes[0] is always the one used to
     * add new news documents, indexes[indexes.length - 1] is always the historic index.
     * The following invariant has to be ensured:<br>
     * <code> indexes[i].getFrom() &gt;= indexes[i + 1].getTo()</code><br>
     */
    @GuardedBy("indexesLock")
    private NewsIndex[] indexes;

    /**
     * Location of directory that contains daily and historic index dirs
     */
    private File newsDir;

    /**
     * Contains rules that determine which news should be deleted at midnight, e.g., all news
     * with selector 4D that are older than a month.
     */
    private Resource deleteQueriesSource;

    private File updatesFile;

    private int maxMergeDocs = 1000000;

    private int mergeFactor = 20;

    private NewsSearcher searcher;

    private NewsAdFinder adFinder;

    @Monitor(type = DataSourceType.COUNTER)
    private AtomicInteger numNewsIndexed = new AtomicInteger();

    @Monitor(type = DataSourceType.COUNTER)
    private AtomicInteger numNewsRejected = new AtomicInteger();

    private AtomicBoolean usedForSearch = new AtomicBoolean();

    private FilterFactory filterFactory;

    private TopicBuilder topicBuilder;

    /**
     * temp store for news that cannot be processed immediately (e.g., while historicIndex
     * is being optimized and we don't have access to the index's writer, or during startup)
     */
    private final Queue<NewsRecordImpl> pendingInserts = new LinkedBlockingQueue<>();

    /**
     * Queries specifying documents to be deleted from today's index
     */
    private final BlockingQueue<Query> deleteFromTodayQueries = new LinkedBlockingQueue<>();

    /**
     * Queries specifying documents to be deleted from the historic index
     */
    private final BlockingQueue<Query> deleteFromHistoryQueries = new LinkedBlockingQueue<>();

    /**
     * Need to aquire this before modifying the indexes array.
     */
    private Lock indexesLock = new ReentrantLock();

    private final AtomicBoolean midnightInProgress = new AtomicBoolean(false);

    static final Integer PAGE_TO_IGNORE = 9999;

    private int maxResultSize = 500;

    private boolean withWarmup = true;

    private volatile boolean started = false;

    // this value has to be greater than the phase of the MuxIn that reads the news from the mdps
    // feed and forwards them to the parser, builder, and finally to us
    private int phase = 100;

    public void setPhase(int phase) {
        this.phase = phase;
    }

    public void disableWarmup() {
        this.withWarmup = false;
    }

    @Required
    public void setNewsDao(NewsDao newsDao) {
        this.newsDao = newsDao;
    }

    public void setAdFinder(NewsAdFinder adFinder) {
        this.adFinder = adFinder;
    }

    public void setBackupDao(NewsDao backupDao) {
        this.backupDao = backupDao;
    }

    @Required
    public void setNews2Document(News2Document news2Document) {
        this.news2Document = news2Document;
    }

    public void setSymbolIdentifier(NewsSymbolIdentifier symbolIdentifier) {
        this.symbolIdentifier = symbolIdentifier;
    }

    public void setFilterFactory(FilterFactory filterFactory) {
        this.filterFactory = filterFactory;
    }

    public void setTopicBuilder(TopicBuilder topicBuilder) {
        this.topicBuilder = topicBuilder;
    }

    public void setMaxResultSize(int maxResultSize) {
        this.maxResultSize = maxResultSize;
        this.logger.info("<setMaxResultSize> maxResultSize = " + this.maxResultSize);
    }

    public void setMergeFactor(int mergeFactor) {
        this.mergeFactor = mergeFactor;
        this.logger.info("<setMergeFactor> mergeFactor = " + this.mergeFactor);
    }

    public void setMaxMergeDocs(int maxMergeDocs) {
        this.maxMergeDocs = maxMergeDocs;
        this.logger.info("<setMaxMergeDocs> maxMergeDocs = " + this.maxMergeDocs);
    }

    @Required
    public void setNewsDir(File newsDir) {
        this.newsDir = newsDir;
    }

    private File getHistoricIndexDir() {
        return new File(this.newsDir, HISTORIC_INDEX_DIR);
    }

    private File getIndexDir() {
        return new File(this.newsDir, INDEX_DIR);
    }

    public void setDeleteQueriesSource(Resource deleteQueriesSource) {
        this.deleteQueriesSource = deleteQueriesSource;
    }

    public void setUpdatesFile(File updatesFile) {
        this.updatesFile = updatesFile;
    }

    Resource getDeleteQueriesSource() {
        return deleteQueriesSource;
    }

    File getUpdatesFile() {
        return this.updatesFile;
    }

    int drainDeleteFromHistoryQueriesTo(Collection<? super Query> c) {
        return deleteFromHistoryQueries.drainTo(c);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        final File indexDir = getIndexDir();
        if (!indexDir.isDirectory()) {
            throw new IllegalStateException("not a directory: " + indexDir);
        }
        final File historicIndexDir = getHistoricIndexDir();
        if (!historicIndexDir.isDirectory()) {
            throw new IllegalStateException("not a directory: " + historicIndexDir);
        }
        if (IndexWriter.isLocked(FSDirectory.open(historicIndexDir))) {
            // happens if the VM crashes while adding current to historic index
            throw new IllegalStateException(historicIndexDir.getAbsolutePath()
                    + " is locked. To unlock stop any writers and remove write.lock");
        }

        if (this.filterFactory == null) {
            this.filterFactory = new DefaultFilterFactory();
        }

        initDefaultIndexes();

        if (this.withWarmup) {
            this.logger.info("<afterPropertiesSet> triggering initial warmup...");
            warmup();
        }
    }

    @Override
    public boolean isAutoStartup() {
        return false;
    }

    @Override
    public void stop(Runnable callback) {
        stop();
        callback.run();
    }

    @Override
    public void stop() {
        this.started = false;
    }

    @Override
    public int getPhase() {
        return this.phase;
    }

    @Override
    public void start() {
        // relies on the fact that the MuxIn that connects to the news source has already been
        // started and connected, which requires that this.phase > muxIn.phase and
        // muxIn.waitForInitialConnectSecs > 0
        indexMissedNews();
        synchronized (this.pendingInserts) {
            this.started = true;
            addPendingInserts(true);
        }
    }

    @Override
    public boolean isRunning() {
        return this.started;
    }

    @ManagedOperation
    public int getNumNewsIndexed() {
        return numNewsIndexed.get();
    }

    @ManagedOperation(description = "delete news by id from index")
    public void requestDelete(String id) {
        final Query q = new TermQuery(new Term(NewsIndexConstants.FIELD_ID, id));
        this.deleteFromTodayQueries.add(q);
        this.deleteFromHistoryQueries.add(q);
        this.logger.info("<requestDelete> submitted for deletion: " + id);
    }

    @ManagedOperation(description = "syncs todays index with news in db")
    @ManagedOperationParameters({
            @ManagedOperationParameter(
                    name = "backupDao",
                    description = "false: local DB, true: backup DB")
    })
    public String syncTodayWithDatabase(boolean backupDao) throws Exception {
        if (this.midnightInProgress.get()) {
            return "cannot sync during daily maintenance";
        }
        if (backupDao && this.backupDao == null) {
            return "no backupDao defined";
        }
        final List<String> indexed = getTodaysDocumentIds();
        final NewsDao dao = backupDao ? this.backupDao : this.newsDao;
        final int num = doIndexMissingNews(indexed, dao);
        return "Indexed " + num + " news";
    }

    private void indexMissedNews() {
        if (this.backupDao == null) {
            this.logger.warn("<indexMissedNews> no backupDao, returning");
            return;
        }

        final List<String> localIds = getIdsSinceMidnight(this.newsDao);
        doIndexMissingNews(localIds, this.backupDao);
    }

    private List<String> getIdsSinceMidnight(final NewsDao dao) {
        return dao.getIdsSince(new LocalDate().toDateTimeAtStartOfDay());
    }

    private int doIndexMissingNews(List<String> localIds, final NewsDao source) {
        final Set<String> ids = new HashSet<>(getIdsSinceMidnight(source));
        ids.removeAll(localIds);

        this.logger.info("<doIndexMissingNews> found " + ids.size() + " news to be indexed");

        final List<String> chunk = new ArrayList<>(10);
        for (Iterator<String> it = ids.iterator(); it.hasNext(); ) {
            chunk.add(it.next());
            if (chunk.size() == 10 || !it.hasNext()) {
                final List<NewsRecord> missedNews = source.getItems(chunk, true, true);
                for (NewsRecord nr : missedNews) {
                    final NewsRecordImpl record = (NewsRecordImpl) nr;
                    assignTopics(record);
                    assignInstrumentsTo(record);
                    handle(record, source == this.backupDao);
                }
                chunk.clear();
            }
        }

        this.logger.info("<doIndexMissingNews> finished for " + ids.size() + " ids");
        return ids.size();
    }

    void assignInstrumentsTo(NewsRecordImpl record) {
        if (this.symbolIdentifier != null) {
            this.symbolIdentifier.assignInstrumentsTo(record);
        }
    }

    private void initDefaultIndexes() throws IOException {
        this.indexesLock.lock();
        try {
            this.indexes = new NewsIndex[2];

            this.indexes[1] = NewsIndex.open(getHistoricIndexDir(), null);
            this.indexes[1].setMergeFactor(2);

            this.indexes[0] = NewsIndex.open(getIndexDir(), this.indexes[1].getTo());
            this.indexes[0].setMergeFactor(this.mergeFactor);
            this.indexes[0].setMaxMergeDocs(this.maxMergeDocs);

            logIndexes();
            initSearcher();
        } finally {
            this.indexesLock.unlock();
        }
    }

    private void initSearcher() {
        this.searcher = new NewsSearcher(this.indexes, this.filterFactory);
    }

    @Override
    public void destroy() throws Exception {
        while (this.midnightInProgress.get()) {
            this.logger.info("<destroy> waiting for midnight update to finish");
            TimeUnit.SECONDS.sleep(5);
        }
        disposeIndexes();
        this.logger.info("<destroy> finished");
    }

    private void disposeIndexes() {
        this.indexesLock.lock();
        try {
            for (int i = 0; i < this.indexes.length; i++) {
                try {
                    indexes[i].dispose();
                } catch (IOException e) {
                    this.logger.error("<disposeIndexes> failed for index " + i);
                }
            }
            this.searcher = new NewsSearcher(new NewsIndex[0], this.filterFactory);
            this.logger.info("<disposeIndexes> finished");
        } finally {
            this.indexesLock.unlock();
        }
    }

    @ManagedOperation(description = "")
    public String refreshIndexes() {
        this.indexesLock.lock();
        try {
            disposeIndexes();
            initDefaultIndexes();
            return "refresh succeeded";
        } catch (IOException e) {
            this.logger.error("<refreshIndexes> failed", e);
            return "refresh failed";
        } finally {
            this.indexesLock.unlock();
        }
    }

    /**
     * Expected to be called from external scheduler
     */
    @ManagedOperation
    public void midnight() {
        if (!this.midnightInProgress.compareAndSet(false, true)) {
            this.logger.warn("<midnight> is running?!, returning");
            return;
        }
        final TimeTaker tt = new TimeTaker();
        this.logger.info("<midnight> ...");
        try {
            // add temp index to be used while we delete/merge/optimize the two main indices
            addTemporaryIndex();

            // merge indices; this optimizes the historic index and also removes all the
            // deleted documents from the index; readers can still read the old version
            final List<String> idsToBeDeleted = dailyIndexMaintenance();

            // remove temporary index and create new readers for all indices
            removeTemporaryIndex();

            deleteFromDb(idsToBeDeleted);

            this.logger.info("<midnight> finished, took " + tt);
        } catch (Throwable t) {
            this.logger.error("<midnight> failed", t);
        } finally {
            this.midnightInProgress.set(false);
        }
    }

    private void deleteFromDb(List<String> ids) {
        if (this.newsDao == null || ids.isEmpty()) {
            return;
        }
        this.logger.info("<deleteFromDb> ...");
        final int n = this.newsDao.deleteItems(ids);
        this.logger.info("<deleteFromDb> finished, deleted " + n + " records");
    }

    private void addTemporaryIndex() throws IOException {
        this.logger.info("<addTemporaryIndex> ...");
        this.indexesLock.lock();
        try {
            this.indexes = new NewsIndex[]{
                    NewsIndex.createTemporary(this.indexes[0].getTo()),
                    this.indexes[0],
                    this.indexes[1]
            };

            logIndexes();
            initSearcher();
        } finally {
            this.indexesLock.unlock();
        }
        this.logger.info("<addTemporaryIndex> finished");
    }

    private void logIndexes() {
        for (int i = 0; i < this.indexes.length; i++) {
            this.logger.info("<logIndexes> [" + i + "] = " + this.indexes[i]);
        }
    }

    private void removeTemporaryIndex() throws IOException {
        this.logger.info("<removeTemporaryIndex> ...");
        this.indexesLock.lock();
        try {
            final int docCount = this.indexes[0].getWriter().numDocs();
            disposeIndexes();

            final NewsIndex index = NewsIndex.create(getIndexDir(), this.indexes[0].getFrom());
            if (docCount > 0) {
                index.addIndex(this.indexes[0]);
            }
            index.dispose();

            initDefaultIndexes();

            addPendingInserts(false);
        } finally {
            this.indexesLock.unlock();
        }
        this.logger.info("<removeTemporaryIndex> finished");
    }

    private void addPendingInserts(boolean insert) {
        NewsRecordImpl record;
        int n = 0;
        while ((record = this.pendingInserts.poll()) != null) {
            handle(record, insert);
            n++;
        }
        if (n > 0) {
            this.logger.info("<addPendingInserts> added " + n + " records");
        }
    }

    private List<String> dailyIndexMaintenance() throws IOException {
        return new DailyIndexMaintenanceMethod(this, indexes).invoke();
    }

    @Override
    public void handle(NewsRecordImpl item) {
        handle(item, true);
    }

    void handle(NewsRecordImpl item, boolean insert) {
        if (isPageToBeIgnored(item)) {
            return;
        }

        if (insert) {
            synchronized (this.pendingInserts) {
                if (!this.started) {
                    addPending(item);
                    return;
                }
            }
            if (this.newsDao != null) {
                findPreviousId(item);
                if (!this.newsDao.insertItem(item)) {
                    if (this.logger.isDebugEnabled()) {
                        this.logger.debug("<handle> insert false for " + item);
                    }
                    // could not store, most likely duplicate key
                    return;
                }
            }
        }

        if (addToIndex(item)) {
            this.numNewsIndexed.getAndIncrement();
        }
        else {
            deleteFromDb(Collections.singletonList(item.getId()));
        }
    }

    private void findPreviousId(NewsRecordImpl item) {
        final String agency = item.getAgency();
        if (UPDATING_AGENCIES.contains(agency)) {
            final String agencyProvidedId = item.getNdbNewsId();
            final DateTime timestamp = item.getTimestamp();
            // we need to make sure "this.indexes[0].flush();" doesn't happen while
            // we search for the previous id in the current index (indexes[0]) otherwise we get a AlreadyClosedException
            // TODO: figure out if we need to flush writer before running the search
            this.indexesLock.lock();
            try {
                searcher.getPreviousId(agency, agencyProvidedId, timestamp).ifPresent(item::setPreviousId);
            } catch (IOException e) {
                this.logger.warn("<findPreviousId> Finding previous Id failed");
            } finally {
                this.indexesLock.unlock();
            }
        }
    }

    static boolean isPageToBeIgnored(NewsRecordImpl item) {
        final SnapField pageNumber = item.getField(VwdFieldDescription.NDB_Page_Number.id());
        if (!pageNumber.isDefined()) {
            return false;
        }
        if (PAGE_TO_IGNORE.equals(pageNumber.getValue())) {
            return true;
        }
        // T-24861: ignore pages with Local_Code = "PP..."
        final SnapField localCode = item.getField(VwdFieldDescription.NDB_Local_Code.id());
        return localCode.isDefined() && localCode.getValue().toString().startsWith("PP");
    }

    private boolean addToIndex(NewsRecordImpl item) {
        final Document document = this.news2Document.toDocument(item);
        this.indexesLock.lock();

        try {
            if (!this.deleteFromTodayQueries.isEmpty()) {
                deleteFromToday();
            }

            addToRecent(item);

            final int n = getIndexForInsert(item);
            if (n == -1) {
                addPending(item);
                return true;
            }

            return addToIndex(item, document, n);
        } catch (Exception e) {
            this.logger.warn("<addToIndex> failed", e);
            return false;
        } finally {
            this.indexesLock.unlock();
        }
    }

    private void addPending(NewsRecordImpl item) {
        if (!this.pendingInserts.offer(item)) {
            this.logger.error("<addPending> rejected " + item);
            this.numNewsRejected.incrementAndGet();
        }
    }

    private void addToRecent(NewsRecordImpl item) {
        this.recentNews.put(encodeShortid(item.getShortId()), item.getId());
    }

    private boolean addToIndex(NewsRecordImpl item, Document document, int n) throws IOException {
        final boolean result = this.indexes[n].addDocument(item, document);
        if (this.logger.isDebugEnabled()) {
            final SnapField sf = item.getField(VwdFieldDescription.NDB_Story_Number.id());
            final String sn = (sf != null && sf.isDefined()) ? sf.getValue().toString() : "x";
            this.logger.debug("<doStore> in [" + n + "] " + item.getId() + ", " + sn + ", '"
                    + item.getHeadline().substring(0, Math.min(item.getHeadline().length(), 20)));
        }
        return result;
    }

    private int getIndexForInsert(NewsRecordImpl item) {
        if (this.indexes.length == 3 && !item.getTimestamp().isAfter(this.indexes[2].getTo())) {
            return 2;
        }
        if (item.getTimestamp().isBefore(this.indexes[0].getFrom())) {
            return (this.indexes.length == 3) ? -1 : 1;
        }
        return 0;
    }

    private void deleteFromToday() throws IOException {
        final List<Query> queries = new ArrayList<>(this.deleteFromTodayQueries.size());
        this.deleteFromTodayQueries.drainTo(queries);
        final List<String> deletedIDs = this.indexes[0].delete(queries);
        deleteFromDb(deletedIDs);
        this.logger.info("<deleteFromToday> deleted " + deletedIDs);
    }

    @Override
    public NewsResponse getNews(NewsRequest request) {
        this.usedForSearch.set(true);
        return doGetNews(request);
    }

    @Override
    public NewsResponse getNews(LatestNewsRequest request) {
        this.usedForSearch.set(true);
        return doGetNews(request);
    }

    private NewsResponse doGetNews(LatestNewsRequest request) {
        final StopWatch sw = new StopWatch();

        final NewsResponseImpl response = doSearch(() -> searcher.search(request, sw), sw);

        // get the news records from DB, gallery is part of the data blob, and needs to be decoded
        addItems(response, request.isWithText(), request.isWithRawText(), sw);

        logSlowRequest(request, sw);
        return response;
    }

    private NewsResponse doGetNews(NewsRequest request) {
        final StopWatch sw = new StopWatch();

        boolean isShortIdRequest = (request.getLuceneQuery() instanceof TermQuery)
                && ((TermQuery) request.getLuceneQuery()).getTerm().field().equals(NewsIndexConstants.FIELD_SHORTID);

        final NewsResponseImpl response = createResponse(request, sw);

        // R-74763 shortid is sometimes not found in index for most recent news
        // this is a HACK that ignores the profile, but shortid requests are only issued by
        // marketmanager and only iff the frontend is allowed to show the news.
        if (isShortIdRequest && (response.getIds().isEmpty())) {
            String sid = ((TermQuery) request.getLuceneQuery()).getTerm().text();
            String id = this.recentNews.get(sid);
            if (id != null) {
                response.setIds(Collections.singletonList(id));
                response.setHitCount(1);
            }
        }

        addItems(response, request.isWithText(), request.isWithRawText(), sw);

        if (this.logger.isDebugEnabled()) {
            this.logger.debug("<getNews> for #" + response.getRecords().size()
                    + " took " + sw.prettyPrint());
        }

        logSlowRequest(request, sw);
        return response;
    }

    private void logSlowRequest(NewsRequestBase request, StopWatch sw) {
        if (sw.getTotalTimeMillis() > 1000) {
            this.logger.warn("<doDispatch> slow: " + request + ", " + sw.prettyPrint());
        }
    }

    private void addItems(NewsResponseImpl response, boolean withText, boolean withRawText, StopWatch sw) {
        if (response.isValid() && this.newsDao != null) {
            sw.start("addItems");
            addItemsToResponse(response, withText, withRawText);
            sw.stop();
        }
    }

    private NewsResponseImpl createResponse(final NewsRequest request, final StopWatch sw) {
        final List<String> nids = request.getNewsids();
        if (nids != null) {
            // since nids cannot be guessed, they must be the result of an earlier search; therefore,
            // the access rights (delay, selectors) have been checked already.
            return createResponseForIdQuery(nids);
        }

        ensureRequestIsValid(request);

        return doSearch(() -> searcher.search(request, sw), sw);
    }

    private void ensureRequestIsValid(NewsRequest request) {
        if (request.getOffset() < 0) {
            request.setOffset(0);
        }
        if (request.getCount() > this.maxResultSize) {
            request.setCount(this.maxResultSize);
        }
        if (request.getCount() <= 0) {
            request.setCount(10);
        }
    }

    private NewsResponseImpl createResponseForIdQuery(List<String> nids) {
        final NewsResponseImpl result = new NewsResponseImpl();
        result.setHitCount(nids.size());
        result.setIds(nids);
        return result;
    }

    // flush the current index, callback when done
    private NewsResponseImpl doSearch(Callable<NewsResponseImpl> callable, StopWatch sw) {
        sw.start("lock");
        this.indexesLock.lock();
        try {
            sw.stop();
            if (this.indexes[0].isModified()) {
                sw.start("flush");
                this.indexes[0].flush();  // this closes the searcher!
                sw.stop();
            }
            return callable.call();
        } catch (Exception e) {
            this.logger.error("<doSearch> failed", e);
        } finally {
            this.indexesLock.unlock();
        }

        return NewsResponseImpl.getInvalid();
    }

    private void addItemsToResponse(NewsResponseImpl response, boolean withText, boolean withRawText) {
        final List<String> ids = response.getIds();
        if (ids.isEmpty()) {
            return;
        }
        final List<NewsRecord> records = getItems(ids, withText, withRawText);
        for (NewsRecord record : records) {
            final NewsRecordImpl nr = (NewsRecordImpl) record;
            findAds(nr);
            assignTopics(nr);
        }
        response.setRecords(records);
    }

    private void findAds(NewsRecordImpl record) {
        if (record != null && this.adFinder != null) {
            this.adFinder.findAds(record);
        }
    }

    private void assignTopics(NewsRecordImpl record) {
        if (record != null && this.topicBuilder != null) {
            this.topicBuilder.handle(record);
        }
    }

    List<NewsRecord> getItems(List<String> ids, final boolean withText, boolean withRawText) {
        return this.newsDao.getItems(ids, withText, withRawText);
    }

    @ManagedOperation(description = "warmup search engine with sample queries")
    public void warmup() {
        if (this.usedForSearch.get()) {
            this.logger.info("<warmup> already in use, returning");
            return;
        }

        final BitSet selectors = getIndexedSelectors();
        if (selectors.isEmpty()) {
            return;
        }
        final Query[] warmupQueries = new Query[]{
                new MatchAllDocsQuery(),
                new TermQuery(new Term(NewsIndexConstants.FIELD_HEADLINE, "dax")),
                new TermQuery(new Term(NewsIndexConstants.FIELD_TEXT, "lower")),
                new TermQuery(new Term(NewsIndexConstants.FIELD_TOPIC, "6f")),
                new TermQuery(new Term(NewsIndexConstants.FIELD_AGENCY, "djn"))
        };

        final TimeTaker tt = new TimeTaker();
        int numQueries = 0;
        long totalNanos = 0;
        final long[] nanos = new long[warmupQueries.length * selectors.cardinality()];

        final NewsRequest nr = new NewsRequest();
        nr.setSilent();
        nr.setCount(20);
        nr.setWithHitCount(true);
        // make sure most of our hits are in historic index
        nr.setTo(new LocalTime(1, 0, 0).toDateTimeToday());

        for (Query query : warmupQueries) {
            nr.setLuceneQuery(query);
            for (int j = selectors.nextSetBit(0); j >= 0; j = selectors.nextSetBit(j + 1)) {
                nr.setProfile(createProfile(j));
                final long then = System.nanoTime();
                doGetNews(nr);
                final long now = System.nanoTime();
                nanos[numQueries++] = (now - then);
                totalNanos += (now - then);
            }
        }
        final long millis = TimeUnit.MILLISECONDS.convert(totalNanos, TimeUnit.NANOSECONDS);
        final long mean = TimeUnit.MILLISECONDS.convert(nanos[nanos.length / 2], TimeUnit.NANOSECONDS);
        this.logger.info("<warmup> took " + tt + ", " + numQueries
                + " queries, avg " + (millis / numQueries) + "ms/query, mean " + mean + "ms");
    }

    private Profile createProfile(final int selector) {
        return new TestProfile(Integer.toString(selector)) {
            @Override
            public BitSet toEntitlements(Aspect aspect, PriceQuality pq) {
                final BitSet result = new BitSet();
                result.set(selector);
                return result;
            }
        };
    }

    private BitSet getIndexedSelectors() {
        final BitSet result = new BitSet();
        this.indexesLock.lock();
        try {
            for (NewsIndex index : indexes) {
                result.or(index.getIndexedSelectors());
            }
        } finally {
            this.indexesLock.unlock();
        }
        return result;
    }

    private List<String> getTodaysDocumentIds() throws IOException {
        this.indexesLock.lock();
        try {
            return this.indexes[0].getDocumentIds();
        } finally {
            this.indexesLock.unlock();
        }
    }
}
