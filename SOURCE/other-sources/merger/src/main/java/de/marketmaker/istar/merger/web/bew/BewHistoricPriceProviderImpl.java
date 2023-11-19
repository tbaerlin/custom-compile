/*
 * BewHistoricSnap.java
 *
 * Created on 26.10.2010 13:33:17
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.bew;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.management.ManagementFactory;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.HashSet;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

import net.jcip.annotations.GuardedBy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.store.FSDirectory;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;
import org.springframework.jmx.export.annotation.ManagedResource;

import de.marketmaker.istar.common.util.ByteString;
import de.marketmaker.istar.common.util.DateUtil;
import de.marketmaker.istar.common.util.IoUtils;
import de.marketmaker.istar.feed.FeedData;
import de.marketmaker.istar.feed.ordered.OrderedFeedData;
import de.marketmaker.istar.feed.ordered.OrderedFeedDataFactory;
import de.marketmaker.istar.feed.ordered.OrderedSnapData;
import de.marketmaker.istar.feed.vwd.VendorkeyVwd;

/**
 * Provides historic price data for the BEW service. The prices, or information where to find them,
 * are stored in lucene indexes. This class expects to find directories named after dates
 * (format yyyyMMdd) in its baseDir, each of those subdirs has to contain a lucene index in an
 * "index" subdirectory as it will be created by {@link de.marketmaker.istar.merger.web.bew.BewHistoricIndexBuilder}.
 * The subdir must also contain a snap file ("snap.sd3"), if that file was used to create the
 * index.
 * @author oflege
 */
@ManagedResource
public class BewHistoricPriceProviderImpl implements InitializingBean, DisposableBean,
        BewHistoricPriceProvider {
    static final DateTimeFormatter DTF = DateTimeFormat.forPattern("yyyyMMdd");

    private static final String ID
            = System.getProperty("machineid", ManagementFactory.getRuntimeMXBean().getName());

    /**
     * Very simple lucene Collector that just keeps track of the latest document number that has
     * been found. That is ok as we expect at most a single hit per query.
     */
    private static class MyCollector extends Collector {
        private int base = 0;

        private int result = -1;

        public void setScorer(Scorer scorer) throws IOException {
        }

        public void collect(int i) throws IOException {
            this.result = this.base + i;
        }

        public void setNextReader(IndexReader indexReader, int i) throws IOException {
            this.base = i;
        }

        public boolean acceptsDocsOutOfOrder() {
            return true;
        }

        int getResult() {
            return this.result;
        }

        void reset() {
            this.result = -1;
        }
    }

    private static class Snap3Reader implements Closeable {
        private final ByteBuffer bb = ByteBuffer.allocate(8192);

        private final FileChannel fc;

        private final File file;

        private final int version;

        public Snap3Reader(File file) throws IOException {
            this.file = file;
            this.fc = new RandomAccessFile(file, "r").getChannel();
            this.fc.read(bb);
            this.version = this.bb.getInt(0);
        }

        @Override
        public String toString() {
            return this.file.getAbsolutePath();
        }

        @Override
        public void close() throws IOException {
            this.fc.close();
        }

        public FeedData restoreData(long offset) throws IOException {
            this.bb.clear();
            this.fc.position(offset);
            this.fc.read(this.bb);
            this.bb.flip();

            final ByteString key = ByteString.readFrom(this.bb, ByteString.LENGTH_ENCODING_BYTE);
            final VendorkeyVwd vkey = VendorkeyVwd.getInstance(key, this.bb.get());

            OrderedFeedData data = OrderedFeedDataFactory.RT.create(vkey, null);

            if (this.version > 1) {
                this.bb.getInt(); // ignore created timestamp
            }

            this.bb.get(); // ignore rt/nt flag

            final OrderedSnapData sd = data.getSnapData(true);
            sd.setLastUpdateTimestamp(this.bb.getInt());
            final byte[] tmp = new byte[this.bb.getShort() & 0xFFFF];
            this.bb.get(tmp);
            sd.init(null, tmp);

            return data;
        }
    }

    /**
     * One such object is created for each date-based directory in baseDir.
     */
    private static class Index {
        private final Logger logger = LoggerFactory.getLogger(getClass());

        private final Snap3Reader snap3Reader;

        private final IndexSearcher searcher;

        private final File dir;

        private Index(File dir, Snap3Reader snap3Reader, IndexSearcher searcher) {
            this.dir = dir;
            this.snap3Reader = snap3Reader;
            this.searcher = searcher;
        }

        private void destroy() {
            IoUtils.close(this.snap3Reader);
            final File inUseFile = createInUseFile(this.dir);
            if (!inUseFile.delete()) {
                this.logger.warn("<destroy> failed to delete " + inUseFile.getAbsolutePath());
            }

            IoUtils.close(this.searcher);
        }

        private Index update(IndexSearcher searcher) {
            IoUtils.close(this.searcher);
            return new Index(dir, this.snap3Reader, searcher);
        }

        public FeedData restoreData(long offset) throws IOException {
            return this.snap3Reader.restoreData(offset);
        }
    }

    private final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * Index objects ordered by date
     */
    @GuardedBy("this")
    private final NavigableMap<LocalDate, Index> indexes = new TreeMap<>();

    private File baseDir;

    public void setBaseDir(File baseDir) {
        this.baseDir = baseDir;
    }

    public void afterPropertiesSet() throws Exception {
        checkUpdates();
    }

    private Map<LocalDate, File> findDirsToUse() {
        final Map<LocalDate, File> result = new TreeMap<>();
        final LocalDate today = new LocalDate();

        // find the two latest year ultimos
        LocalDate firstOfYear = today.withDayOfYear(1);
        for (int i = 0; i < 5 && result.size() < 2; i++) {
            addDirectory(firstOfYear.minusDays(1), result);
            firstOfYear = firstOfYear.minusYears(1);
        }

        // add the two latest month ultimos not included yet
        LocalDate firstOfMonth = today.withDayOfMonth(1);
        for (int i = 0; i < 4 && result.size() < 4; i++) {
            addDirectory(firstOfMonth.minusDays(1), result);
            firstOfMonth = firstOfMonth.minusMonths(1);
        }

        // add any dirs that contain a must_use.txt file        
        final File[] dirs = this.baseDir.listFiles(BewHistoricIndexBuilder.DIR_FILTER);
        for (File dir : dirs) {
            if (new File(dir, "must_use.txt").isFile()) {
                result.put(DTF.parseDateTime(dir.getName()).toLocalDate(), dir);
            }
        }
        return result;
    }

    private void addDirectory(LocalDate date, Map<LocalDate, File> result) {
        if (!result.containsKey(date)) {
            final File dir = new File(this.baseDir, DTF.print(date));
            if (dir.isDirectory() && new File(dir, "can_use.txt").isFile()) {
                result.put(date, dir);
            }
        }
    }

    public synchronized void destroy() throws Exception {
        for (Index index : indexes.values()) {
            index.destroy();
        }
        this.indexes.clear();
    }

    public synchronized void checkUpdates() {
        final Map<LocalDate, File> dirsToUse = findDirsToUse();

        final HashSet<LocalDate> toAdd = new HashSet<>(dirsToUse.keySet());
        toAdd.removeAll(this.indexes.keySet());

        final HashSet<LocalDate> toRemove = new HashSet<>(this.indexes.keySet());
        toRemove.removeAll(dirsToUse.keySet());

        if (toAdd.isEmpty() && toRemove.isEmpty()) {
            this.logger.info("<checkUpdates> no changes found");
            return;
        }

        for (LocalDate date : toRemove) {
            closeIndex(date);
        }
        for (LocalDate date : toAdd) {
            openIndex(date, dirsToUse.get(date));
        }

        this.logger.info("<checkUpdates> removed " + toRemove + ", added " + toAdd);
    }

    private synchronized void closeIndex(LocalDate date) {
        final Index index = this.indexes.remove(date);
        if (index != null) {
            index.destroy();
        }
        this.logger.info("<closeIndex> for " + date);
    }

    private synchronized Boolean openIndex(LocalDate date, File dir) {
        if (this.indexes.containsKey(date)) {
            this.logger.warn("<openIndex> already open: " + dir.getName());
            return null;
        }
        if (!getIndexDir(dir).isDirectory()) {
            this.logger.warn("<openIndex> no index directory in " + dir.getAbsolutePath());
            return null;
        }

        boolean success = false;
        Snap3Reader snap3Reader = null;
        try {

            final File snap3File = new File(dir, "snap.sd3");
            snap3Reader = new Snap3Reader(snap3File);

            final FSDirectory indexDir = openIndexDir(dir);
            final IndexSearcher is = createSearcher(indexDir);

            final File inUseFile = createInUseFile(dir);
            if (!inUseFile.createNewFile()) {
                this.logger.warn("<openIndex> " + inUseFile.getAbsolutePath() + " already existed");
            }

            this.indexes.put(date, new Index(dir, snap3Reader, is));

            this.logger.info("<openIndex> succeeded for " + dir.getName());
            success = true;
        } catch (Exception e) {
            this.logger.error("<openIndex> failed for " + dir.getName(), e);
        } finally {
            if (!success) {
                IoUtils.close(snap3Reader);
            }
        }

        return success;
    }

    private static File createInUseFile(File dir) {
        return new File(dir, ID + "_in_use.txt");
    }

    private IndexSearcher createSearcher(FSDirectory indexDir) throws IOException {
        return new IndexSearcher(IndexReader.open(indexDir));
    }

    private FSDirectory openIndexDir(File dir) throws IOException {
        return FSDirectory.open(getIndexDir(dir));
    }

    private File getIndexDir(File dir) {
        return new File(dir, "index");
    }

    @ManagedOperation
    @ManagedOperationParameters({
            @ManagedOperationParameter(name = "vwdcode", description = "vwdcode, e.g., 710000.ETR"),
            @ManagedOperationParameter(name = "date", description = "date as yyyyMMdd")
    })
    public String getPriceJmx(String vwdcode, int date) {
        final BewHistoricPriceRequest request = new BewHistoricPriceRequest(DateUtil.yyyyMmDdToLocalDate(date));
        request.addVwdcode(vwdcode);

        final BewHistoricPriceResponse response = getPrices(request);
        if (!response.isValid()) {
            return "failed";
        }
        final BewHistoricPriceResponse.Item item = response.getItem(vwdcode);
        if (item == null) {
            return "no data";
        }
        if (item.getSnapRecord() != null) {
            return item.getSnapRecord().toString().replace("SnapField", "\nSnapField");
        }
        else {
            return item.getPrice().toPlainString() + " / " + item.getPriceDate();
        }
    }

    @ManagedOperation(description = "parses csv file; expected format: vwdcode;price;dd.MM.yyyy")
    @ManagedOperationParameters({
            @ManagedOperationParameter(name = "yyyymmdd", description = "yyyymmdd"),
            @ManagedOperationParameter(name = "filename", description = "name of file to import")
    })
    public String updateIndex(int yyyymmdd, String filename) throws IOException {
        final File f = new File(filename);
        if (!f.canRead()) {
            return "no such file: " + filename;
        }

        final LocalDate date = DateUtil.yyyyMmDdToLocalDate(yyyymmdd);
        synchronized (this) {
            if (!this.indexes.containsKey(date)) {
                return "no index for " + date;
            }
        }

        return doUpdateIndex(date, f);
    }

    @ManagedOperation(description = "adds or reloads the index for a specific day")
    @ManagedOperationParameters({
            @ManagedOperationParameter(name = "yyyymmdd", description = "yyyymmdd")
    })
    public String addOrReloadIndex(int yyyymmdd) throws IOException {
        final LocalDate date = DateUtil.yyyyMmDdToLocalDate(yyyymmdd);
        final File f = new File(this.baseDir, DTF.print(date));
        if (!f.isDirectory()) {
            return "no such directory: " + f.getAbsolutePath();
        }

        synchronized (this) {
            closeIndex(date);
            final Boolean result = openIndex(date, f);
            if (Boolean.TRUE.equals(result)) {
                return "added " + f.getName();
            }
            return "failed, see log for details";
        }
    }

    private String doUpdateIndex(LocalDate date, File f) throws IOException {
        final File dir = new File(this.baseDir, DTF.print(date));
        final FSDirectory indexDir = openIndexDir(dir);

        final BewHistoricPriceUpdater updater = new BewHistoricPriceUpdater(indexDir);
        if (updater.update(f)) {
            synchronized (this) {
                final Index index = this.indexes.remove(date);
                this.indexes.put(date, index.update(createSearcher(indexDir)));
            }
            return "succeeded";
        }
        return "failed";
    }

    public synchronized BewHistoricPriceResponse getPrices(BewHistoricPriceRequest request) {
        final Map.Entry<LocalDate, Index> e = getIndexForDate(request.getDate());
        if (e == null) {
            this.logger.info("<getPrices> no data for " + request.getDate());
            return new BewHistoricPriceResponse();
        }

        if (this.logger.isDebugEnabled()) {
            this.logger.debug("<getPrices> search data for " + request.getDate() + " in " + e.getKey());
        }

        final BewHistoricPriceResponse result = new BewHistoricPriceResponse();

        final Index index = e.getValue();
        try {
            final MyCollector c = new MyCollector();
            for (String code : request.getVwdcodes()) {
                c.reset();
                index.searcher.search(new TermQuery(new Term(BewHistoricIndexBuilder.KEY_TERM, code)), c);
                if (c.result != -1) {
                    result.add(code, createResponseItem(index, c.result));
                }
                else {
                    if (this.logger.isDebugEnabled()) {
                        this.logger.debug("<getPrices> no prices for " + code);
                    }
                }
            }
        } catch (Exception e1) {
            this.logger.warn("<getPrices> failed", e1);
            result.setInvalid();
        }
        return result;
    }

    private Map.Entry<LocalDate, Index> getIndexForDate(final LocalDate date) {
        return this.indexes.floorEntry(date);
    }

    private BewHistoricPriceResponse.Item createResponseItem(Index index,
            int docId) throws IOException {
        final Document d = index.searcher.doc(docId);
        final String offset = d.get(BewHistoricIndexBuilder.OFFSET_TERM);
        if (offset != null) {
            final FeedData data = index.restoreData(Long.parseLong(offset, Character.MAX_RADIX));
            return new BewHistoricPriceResponse.Item(data.getSnapData(true).toSnapRecord(0));
        }
        else {
            final String price = d.get(BewHistoricIndexBuilder.PRICE_TERM);
            final String date = d.get(BewHistoricIndexBuilder.DATE_TERM);
            return new BewHistoricPriceResponse.Item(new BigDecimal(price),
                    DateUtil.yyyyMmDdToLocalDate(Integer.parseInt(date)));
        }
    }

    public static void main(String[] args) {
        final BewHistoricPriceProviderImpl bew = new BewHistoricPriceProviderImpl();
        bew.setBaseDir(new File(args[0]));
        final Map<LocalDate, File> dfm = bew.findDirsToUse();
        for (Map.Entry<LocalDate, File> e : dfm.entrySet()) {
            System.out.println(e.getKey() + " => " + e.getValue());
        }
    }
}
