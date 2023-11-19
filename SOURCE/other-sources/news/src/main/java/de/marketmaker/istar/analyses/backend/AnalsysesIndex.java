/*
 * AnalsysesIndex.java
 *
 * Created on 20.04.12 13:44
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.analyses.backend;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.store.FSDirectory;

import de.marketmaker.istar.analyses.frontend.AnalysesIndexConstants;
import de.marketmaker.istar.common.util.IoUtils;
import de.marketmaker.istar.common.util.TimeTaker;
import de.marketmaker.istar.news.analysis.NewsAnalyzer;

/**
 * this class is managing a single lucene backend,
 * contains searcher, writer, analyzer for lucene...
 *
 * note we have two backends one for today and one for the historic data
 *
 * @author oflege
 */
class AnalsysesIndex {
    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());

    private static final Analyzer ANALYZER = new NewsAnalyzer();

    /**
     * News records are limited to 65K text, so no text should contain more than 16000 different
     * term values
     */
    static final IndexWriter.MaxFieldLength FIELD_LENGTH = new IndexWriter.MaxFieldLength(16000);

    private IndexSearcher searcher;

    private IndexWriter writer;

    /**
     * true iff any document has been added to this index after it was created
     */
    private boolean modified = false;

    private final AtomicBoolean optimizing = new AtomicBoolean();

    public AnalsysesIndex(File indexDir) throws IOException {
        this.writer = new IndexWriter(FSDirectory.open(indexDir), ANALYZER, FIELD_LENGTH);
        this.searcher = createSearcher();
    }

    private IndexSearcher createSearcher() throws IOException {
        this.writer.commit();
        return new IndexSearcher(this.writer.getDirectory(), true);
    }

    void destroy() throws Exception {
        IoUtils.close(this.searcher);
        IoUtils.close(this.writer);
    }

    void flush() throws IOException {
        if (!this.modified || this.optimizing.get()) {
            return;
        }
        this.writer.commit();
        IoUtils.close(this.searcher);
        this.searcher = createSearcher();
        this.modified = false;
    }

    Set<Long> getDocumentIds(Query query) throws IOException {
        return getDocumentIds(query, null);
    }

    Set<Long> getDocumentIds(Query query, Filter filter) throws IOException {
        final TimeTaker tt = new TimeTaker();
        final Set<Long> result = new HashSet<>(100);

        final IOException[] ioe = new IOException[1];
        this.searcher.search(query, filter, new Collector() {
            private IndexReader reader;

            @Override
            public void setScorer(Scorer scorer) throws IOException {
                // ignore
            }

            @Override
            public void collect(int i) throws IOException {
                try {
                    final String id = this.reader.document(i)
                            .getField(AnalysesIndexConstants.FIELD_ID).stringValue();
                    result.add(AnalysesProvider.decodeId(id));
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
        if (ioe[0] != null) {
            logger.warn("<getDocumentIds> search caused exception?!", ioe[0]);
        }

        if (logger.isDebugEnabled()) {
            logger.debug("<getDocumentIds> query: " + query + ", found " + result.size() + " docs in " + tt);
        }
        return result;
    }

    void add(Document document) {
        try {
            this.writer.addDocument(document);
            this.modified = true;
        } catch (IOException e) {
            this.logger.error("<addToIndex> failed", e);
        }
    }

    void delete(Set<Long> idsInIndex) throws IOException {
        for (Long id : idsInIndex) {
            this.writer.deleteDocuments(new Term(AnalysesIndexConstants.FIELD_ID,
                    AnalysesProvider.encodeId(id)));
        }
        this.modified = true;
    }

    void optimize() throws IOException {
        if (!this.optimizing.compareAndSet(false, true)) {
            return;
        }
        try {
            final TimeTaker tt = new TimeTaker();
            this.writer.optimize();
            this.logger.info("<optimize> took " + tt + ", numDocs=" + this.writer.numDocs());
        } finally {
            this.optimizing.set(false);
        }
    }
}
