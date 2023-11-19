package de.marketmaker.istar.merger.provider.lbbwresearch;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import com.google.common.collect.ImmutableSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.CachingWrapperFilter;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.TermsFilter;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;

import de.marketmaker.istar.common.util.TimeTaker;
import de.marketmaker.istar.domain.profile.Selector;

import static de.marketmaker.istar.merger.provider.lbbwresearch.LbbwResearchIndexConstants.FIELD_ID;
import static de.marketmaker.istar.merger.provider.lbbwresearch.LbbwResearchIndexConstants.FIELD_SELECTOR;

/**
 * Searcher to query the index
 * @author mcoenen
 */
class LbbwIndexSearcher implements AutoCloseable {

    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());

    private final IndexSearcher searcher;

    LbbwIndexSearcher(File indexDir) throws IOException {
        if (!indexDir.isDirectory()) {
            this.searcher = null;
        }
        else {
            this.searcher = new IndexSearcher(FSDirectory.open(indexDir));
        }
    }

    LbbwIndexSearcher(IndexReader reader) throws IOException {
        this.searcher = new IndexSearcher(reader);
    }

    Set<String> search(LbbwResearchRequest r) throws IOException {
        return this.search(r, null);
    }

    Set<String> search(Query q) throws IOException {
        return this.search(null, q);
    }

    Set<String> search(Query q, int n, Sort sort) throws IOException {
        if (this.searcher == null) {
            return ImmutableSet.of();
        }

        TopDocs docs = this.searcher.search(q, null, n, sort);
        Set<String> result = new HashSet<>(docs.scoreDocs.length);
        for (ScoreDoc scoreDoc : docs.scoreDocs) {
            Document doc = this.searcher.doc(scoreDoc.doc);
            result.add(doc.getField(FIELD_ID).stringValue());
        }

        return result;
    }

    private Set<String> search(LbbwResearchRequest r, Query q) throws IOException {
        if (this.searcher == null) {
            return ImmutableSet.of();
        }
        if (r == null && q == null) {
            throw new IllegalArgumentException("Either request or query required");
        }
        final IOException[] ioe = new IOException[1];
        final TimeTaker tt = new TimeTaker();

        Set<String> objectIds = new HashSet<>();

        if (r != null) {
            this.searcher.search(
                    (r.getQuery() != null) ? r.getQuery() : new MatchAllDocsQuery(),
                    createFilter(r),
                    new ObjectIdCollector(objectIds, ioe)
            );
        }
        else {
            this.searcher.search(q, new ObjectIdCollector(objectIds, ioe));
        }

        if (ioe[0] != null) {
            logger.warn("<search> search caused exception?!", ioe[0]);
        }

        if (logger.isDebugEnabled()) {
            logger.debug("<search> query: " + q + ", found " + objectIds.size() + " docs in " + tt);
        }
        return ImmutableSet.copyOf(objectIds);
    }

    private Filter createFilter(LbbwResearchRequest r) {
        TermsFilter termsFilter = new TermsFilter();
        for (Selector selector : r.getSelectors()) {
            termsFilter.addTerm(new Term(FIELD_SELECTOR, String.valueOf(selector.getId())));
        }
        return new CachingWrapperFilter(termsFilter);
    }

    @Override
    public void close() throws IOException {
        this.searcher.close();
    }

    private static class ObjectIdCollector extends Collector {

        private final Set<String> objectIds;

        private final IOException[] ioe;

        private IndexReader reader;

        private ObjectIdCollector(Set<String> objectIds, IOException[] ioe) {
            this.objectIds = objectIds;
            this.ioe = ioe;
        }

        @Override
        public void setScorer(Scorer scorer) throws IOException {
            // ignore
        }

        @Override
        public void collect(int i) throws IOException {
            try {
                Document document = this.reader.document(i);
                final String id = document.getField(FIELD_ID).stringValue();
                this.objectIds.add(id);
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
    }
}
