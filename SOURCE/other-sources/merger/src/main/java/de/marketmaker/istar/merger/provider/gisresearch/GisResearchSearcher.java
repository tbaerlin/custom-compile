/*
 * DzResearchSearcher.java
 *
 * Created on 25.03.14 15:51
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.gisresearch;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.CachingWrapperFilter;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.search.TermsFilter;
import org.apache.lucene.store.FSDirectory;

import de.marketmaker.istar.analyses.frontend.AnalysesIndexConstants;
import de.marketmaker.istar.common.util.IoUtils;
import de.marketmaker.istar.common.util.TimeTaker;
import de.marketmaker.istar.domain.profile.Selector;

import static de.marketmaker.istar.merger.provider.gisresearch.GisResearchIndexConstants.FIELD_SELECTOR;

/**
 * @author oflege
 */
class GisResearchSearcher implements AutoCloseable {

    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());

    private final IndexSearcher searcher;

    GisResearchSearcher(File indexDir) throws IOException {
        this.searcher = new IndexSearcher(FSDirectory.open(indexDir));
    }

    Set<String> search(GisResearchRequest r) throws IOException {
        final HashSet<String> result = new HashSet<>();

        final IOException[] ioe = new IOException[1];
        final TimeTaker tt = new TimeTaker();

        final Query q = (r.getQuery() != null) ? r.getQuery() : new MatchAllDocsQuery();

        this.searcher.search(q, createFilter(r), new Collector() {
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

        if (ioe[0] != null) {
            logger.warn("<getDocumentIds> search caused exception?!", ioe[0]);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("<getDocumentIds> query: " + q
                    + ", found " + result.size() + " docs in " + tt);
        }
        return result;
    }

    private Filter createFilter(GisResearchRequest r) {
        TermsFilter termsFilter = new TermsFilter();
        termsFilter.addTerm(new Term(FIELD_SELECTOR, Research2Document.ALLOW_ALL_SELECTOR));
        for (Selector selector : r.getSelectors()) {
            termsFilter.addTerm(new Term(FIELD_SELECTOR, Integer.toString(selector.getId())));
        }
        return new CachingWrapperFilter(termsFilter);
    }

    @Override
    public void close() throws Exception {
        IoUtils.close(this.searcher);
    }
}
