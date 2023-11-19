/*
 * NewsIndex.java
 *
 * Created on 16.03.2007 11:12:53
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.news.backend;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.NumericUtils;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import de.marketmaker.istar.common.util.EntitlementsVwd;
import de.marketmaker.istar.common.util.LocalConfigProvider;
import de.marketmaker.istar.common.util.TimeTaker;
import de.marketmaker.istar.news.analysis.NewsAnalyzer;
import de.marketmaker.istar.news.frontend.NewsAttributeEnum;
import de.marketmaker.istar.news.frontend.NewsIndexConstants;
import de.marketmaker.istar.news.frontend.NewsRecord;

/**
 * Encapsulates a single lucene index for news. Since a NewsSearcher requires that the NewsIndex array
 * it uses is ordered with respect to the dates of the news stored in the indexes, this object
 * keeps the timestamp of the oldest and youngest indexed news.
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class NewsIndex {
    private static final Logger logger = LoggerFactory.getLogger(NewsIndex.class);

    private static final DateTimeFormatter DTF = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");

    private static final Analyzer ANALYZER = new NewsAnalyzer();

    private static final int MIN_NEWS_TIMESTAMP
            = News2Document.encodeTimestamp(new LocalDate(2000,1,1).toDateTimeAtStartOfDay());

    /**
     * News records are limited to 65K text, so no text should contain more than 16000 different
     * term values
     */
    static final IndexWriter.MaxFieldLength FIELD_LENGTH = new IndexWriter.MaxFieldLength(16000);

    private IndexWriter writer;

    private IndexSearcher searcher;

    /**
     * timestamp of oldest news in this index
     */
    private DateTime from;

    /**
     * timestamp of youngest news in this index
     */
    private DateTime to;

    /**
     * location of this index, null iff directory is not null
     */
    private File path;

    /**
     * location of this index, null iff path is not null
     */
    private Directory directory;

    /**
     * true iff any document has been added to this index after it was created
     */
    private boolean modified = false;

    private final boolean historic;

    /**
     * Stores the selectors for which at least one news is present in the index.
     * A cleaner design would be to put this field into the
     * {@link DefaultFilterFactory}, because
     * {@link #getIndexedSelectors()} is only used by that class. However, it would be very
     * cumbersome to keep its value up-to-date when new documents are added to the index.  
     */
    private final BitSet indexedSelectors = new BitSet(520);

    /**
     * Opens index in path for both reading and writing; if no index exists, it will be created
     * @param path index location
     * @param from if not null, earliest news timestamp that might exist in the index
     * @return new NewsIndex
     * @throws java.io.IOException if opening the index fails
     */
    static NewsIndex open(File path, DateTime from) throws IOException {
        final IndexWriter iw = new IndexWriter(FSDirectory.open(path), ANALYZER, FIELD_LENGTH);
        return new NewsIndex(iw, path, from);
    }

    /**
     * Creates a new index at location path, possibly deleting a previous index at the location
     * @param path index location
     * @param from earliest news timestamp that might exist in the index
     * @return new NewsIndex
     * @throws java.io.IOException if opening the index fails
     */
    static NewsIndex create(File path, DateTime from) throws IOException {
        ensureNotNull(from);
        final IndexWriter iw = new IndexWriter(FSDirectory.open(path), ANALYZER, true, FIELD_LENGTH);
        return new NewsIndex(iw, path, from);
    }

    /**
     * Creates a temporary index using a RAMDirectory
     * @param from earliest news timestamp that might exist in the index
     * @throws java.io.IOException if opening the index fails
     * @return new NewsIndex
     */
    static NewsIndex createTemporary(DateTime from) throws IOException {
        ensureNotNull(from);
        final IndexWriter iw = new IndexWriter(new RAMDirectory(), ANALYZER, true, FIELD_LENGTH);
        return new NewsIndex(iw, from);
    }

    private static void ensureNotNull(DateTime from) {
        if (from == null) {
            throw new IllegalArgumentException("from cannot be null");
        }
    }

    public String toString() {
        final StringBuilder sb = new StringBuilder(100)
                .append("NewsIndex[docCount=").append(this.writer.maxDoc())
                .append(", modified=").append(this.modified)
                .append(", from=").append(DTF.print(this.from))
                .append(", to=").append(DTF.print(this.to))
                .append(", location=");
        if (this.path != null) {
            sb.append(this.path.getAbsolutePath());
        }
        else {
            sb.append("RAM");
        }
        sb.append(", numDocs=").append(this.searcher.getIndexReader().numDocs())
                .append(", selectors=").append(EntitlementsVwd.asString(this.indexedSelectors));
        return sb.append("]").toString();
    }

    private NewsIndex(IndexWriter writer, File path, DateTime from) throws IOException {
        this.path = path;
        this.writer = writer;
        this.searcher = createSearcher(writer);
        this.historic = (from == null);
        initFromAndTo(from);
        initSelectors();
    }

    private NewsIndex(IndexWriter writer, DateTime from) throws IOException {
        this.directory = writer.getDirectory();
        this.writer = writer;
        this.searcher = createSearcher(writer);
        this.historic = false;
        this.from = from;
        this.to = from;
    }

    private static IndexSearcher createSearcher(IndexWriter iw) throws IOException {
        iw.commit();
        return new IndexSearcher(iw.getDirectory(), true);
    }

    void setMaxMergeDocs(int i) {
        this.writer.setMaxMergeDocs(i);
    }

    void setMergeFactor(int i) {
        this.writer.setMergeFactor(i);
    }

    void initSelectors() throws IOException {
        final IndexReader reader = this.searcher.getIndexReader();
        final String key = NewsIndexConstants.FIELD_SELECTOR;
        final TermEnum enumerator = reader.terms(new Term(key, ""));
        if (enumerator.term() == null) {
            return;
        }
        final List<String> selectors = new ArrayList<>();
        do {
            Term term = enumerator.term();
            if (term != null && term.field().equals(key)) {
                selectors.add(enumerator.term().text());
            }
            else {
                break;
            }
        } while (enumerator.next());
        addSelectors(selectors);
    }

    private void addSelectors(Collection<String> selectors) {
        if (selectors == null) {
            return;
        }
        for (String s : selectors) {
            try {
                this.indexedSelectors.set(EntitlementsVwd.toValue(s));
            } catch (IllegalArgumentException e) {
                logger.error("<addSelectors> failed " + e.getMessage());
            }
        }
    }

    BitSet getIndexedSelectors() {
        return this.indexedSelectors;
    }

    /**
     * compute from and to; if the index is empty, use default value from, otherwise read the
     * from and to timestamps from the index
     * @param requiredFrom the index is not supposed to contain any news before this if not null
     * @throws java.io.IOException if operations on index fail
     */
    private void initFromAndTo(DateTime requiredFrom) throws IOException {
        final IndexReader reader = this.searcher.getIndexReader();
        final TermEnum enumerator = reader.terms(new Term(NewsIndexConstants.FIELD_TIMESTAMP));
        try {
            initFromAndTo(enumerator, requiredFrom);
        } finally {
            enumerator.close();
        }
    }

    private void initFromAndTo(TermEnum enumerator, DateTime requiredFrom) throws IOException {
        if (enumerator.term() == null) {
            // this should only occur for an empty historic index. Since we will not be able to
            // add anything to the current index that is earlier than this timestamp, we
            // choose a date far enough in the past to even allow adding mcrip'ed news
            this.from = (requiredFrom != null) ? requiredFrom : new DateTime().minusYears(5).withTimeAtStartOfDay();
            this.to = this.from;
            return;
        }

        int min = Integer.MAX_VALUE;
        int max = 0;

        do {
            final Term term = enumerator.term();
            //noinspection StringEquality
            if (term == null || term.field() != NewsIndexConstants.FIELD_TIMESTAMP) {
                break;
            }
            final String val = term.text();

            // copied from FieldCache.NUMERIC_UTILS_INT_PARSER to skip values at boundaries
            // that are out of range
            final int shift = val.charAt(0) - NumericUtils.SHIFT_START_INT;
            if (shift > 0 && shift <= 31) {
                break;
            }

            final int value = NumericUtils.prefixCodedToInt(val);
            if (value > MIN_NEWS_TIMESTAMP) {
                min = Math.min(min, value);
                max = Math.max(max, value);
            }
            else {
                logger.info("<initFromAndTo> ?!: " + News2Document.decodeTimestamp(value));
            }
        } while (enumerator.next());

        this.from = News2Document.decodeTimestamp(min);
        this.to = News2Document.decodeTimestamp(max);

        if (requiredFrom != null && this.from.isBefore(requiredFrom)) {
            throw new RuntimeException("<initFromAndTo> required from: " + DTF.print(requiredFrom)
                    + ", BUT found earlier (" + DTF.print(this.from) + ") news in index "
                    + this.path.getAbsolutePath());
        }
    }

    void flush() throws IOException {
        this.writer.commit();
        this.searcher.close();
        this.searcher = createSearcher(this.writer);
        this.modified = false;
    }

    void close() throws IOException {
        closeWriter();
        this.searcher.close();
    }

    void closeWriter() throws IOException {
        this.writer.close();
    }

    void optimize() throws IOException {
        this.writer.optimize();
    }

    DateTime getFrom() {
        return this.from;
    }

    IndexSearcher getSearcher() {
        return this.searcher;
    }

    DateTime getTo() {
        return this.to;
    }

    IndexWriter getWriter() {
        return this.writer;
    }

    boolean isHistoric() {
        return this.historic;
    }

    boolean addDocument(NewsRecord nr, Document document) throws IOException {
        if (this.from.isAfter(nr.getTimestamp())) {
            logger.warn("<addDocument> failed, from= " + DTF.print(this.from) + " is after " + nr);
            return false;
        }
        this.writer.addDocument(document);
        this.modified = true;
        if (nr.getTimestamp().isAfter(this.to)) {
            this.to = nr.getTimestamp();
        }
        addSelectors(nr.getAttributes().get(NewsAttributeEnum.SELECTOR));
        return true;
    }

    boolean isModified() {
        return this.modified;
    }

    void dispose() throws IOException {
        close();
    }

    void addIndex(NewsIndex index) throws IOException {
        this.writer.addIndexesNoOptimize(new Directory[]{getDirectory(index)});
    }

    private Directory getDirectory(NewsIndex index) throws IOException {
        if (index.directory != null) {
            return index.directory;
        }
        return FSDirectory.open(index.path);
    }

    /**
     * deletes all news from the index that match any of the given queries
     * @param queries determine news to be deleted
     * @return ids of all deleted news
     * @throws IOException if index operations fail
     */
    List<String> delete(List<Query> queries) throws IOException {
        final List<String> result = getDocumentIds(queries);
        if (result.isEmpty()) {
            return Collections.emptyList();
        }

        this.writer.deleteDocuments(queries.toArray(new Query[queries.size()]));

        return result;
    }

    List<String> getDocumentIds(List<Query> queries) throws IOException {
        final TimeTaker tt = new TimeTaker();

        final List<String> result = new ArrayList<>(1000);

        final IOException[] ioe = new IOException[1];
        for (Query query : queries) {
            this.searcher.search(query, new Collector() {
                private IndexReader reader;
                @Override
                public void setScorer(Scorer scorer) throws IOException {
                    // ignore
                }

                @Override
                public void collect(int i) throws IOException {
                    try {
                        final String id = this.reader.document(i)
                                .getField(NewsIndexConstants.FIELD_ID).stringValue();
                        result.add(id);
                    } catch (IOException e) {
                        ioe[0] = e;
                    }
                }

                @Override
                public void setNextReader(IndexReader indexReader, int i) throws IOException {
                    this.reader = indexReader;
                }

                @Override
                public boolean acceptsDocsOutOfOrder() {
                    return true;
                }
            });
        }
        if (ioe[0] != null) {
            logger.warn("<getDocumentIds> search caused exception?!", ioe[0]);
        }

        logger.info("<getDocumentIds> found " + result.size() + " documents in " + tt);
        return result;
    }

    public List<String> getDocumentIds() throws IOException {
        flush();
        return getDocumentIds(Collections.singletonList((Query) new MatchAllDocsQuery()));
    }

    public static void main(String[] args) throws IOException {
        NewsIndex.create(LocalConfigProvider.getProductionDir("var/data/news/index"), new DateTime());
    }
}
