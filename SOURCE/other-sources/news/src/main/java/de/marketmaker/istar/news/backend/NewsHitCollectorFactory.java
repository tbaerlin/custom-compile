/*
 * NewsHitCollectorFactory.java
 *
 * Created on 11.04.2007 11:27:33
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.news.backend;

import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

import org.apache.lucene.search.IndexSearcher;

import de.marketmaker.istar.news.frontend.NewsRequest;
import de.marketmaker.istar.news.frontend.NewsIndexConstants;

/**
 * Factory for NewsHitCollector objects.
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class NewsHitCollectorFactory {
    private abstract static class AbstractHitCollector implements NewsHitCollector {
        private final List<String> ids;
        private String prevPageRequestId;
        private String nextPageRequestId;
        protected final int count;
        protected final IndexSearcher[] searchers;
        protected int numHits;

        protected AbstractHitCollector(IndexSearcher[] searchers, int count) {
            this.searchers = searchers;
            this.count = count;
            this.ids = new ArrayList<>(count);
        }

        public List<String> getIds() {
            return this.ids;
        }

        protected int size() {
            return ids.size();
        }

        protected void addId(String id) {
            if (size() < this.count) {
                this.ids.add(id);
            }
            else {
                this.nextPageRequestId = id;
            }
        }

        protected String getId(int indexId, int documentId) throws IOException {
            // getting the document is EXPENSIVE, it should be avoided whenever possible.
            return this.searchers[indexId].doc(documentId).get(NewsIndexConstants.FIELD_ID);
        }

        protected void setPrevPageRequestId(String prevPageRequestId) {
            this.prevPageRequestId = prevPageRequestId;
        }

        public String getNextPageRequestId() {
            return this.nextPageRequestId;
        }

        public String getPrevPageRequestId() {
            return this.prevPageRequestId;
        }

        public boolean canCollectMore() {
            return this.nextPageRequestId == null;
        }
    }

    /**
     * Collects news from request.getOffset (incl) to request.getOffset + request.getCount() (excl).
     */
    private static class SimpleHitCollector extends AbstractHitCollector {
        private final int prevHitNumber;

        private final int offset;

        private SimpleHitCollector(IndexSearcher[] searchers, NewsRequest request) {
            super(searchers, request.getCount());
            this.offset = request.getOffset();
            if (this.offset > 0) {
                this.prevHitNumber = Math.max(0, this.offset - this.count);
            }
            else {
                this.prevHitNumber = -1;
            }
        }

        @Override
        public void add(int indexId, int documentId) throws IOException {
            if (this.numHits == this.prevHitNumber) {
                setPrevPageRequestId(getId(indexId, documentId));
            }
            else if (this.numHits >= this.offset) {
                addId(getId(indexId, documentId));
            }
            this.numHits++;
        }
    }

    /**
     * Collects news starting from request.getOffsetId() (incl), at most request.getCount()
     */
    private static class OffsetIdHitCollector extends AbstractHitCollector {
        // searcher-index and document-id of the offset document
        private final int[] iad;
        // whether we have seen the offset doc and can therefore start to collect doc-ids
        private boolean add;
        // each element is (index-id << 32) + document-id
        private final long[] previousIds;

        private OffsetIdHitCollector(IndexSearcher[] searchers, NewsRequest request, int[] iad) {
            super(searchers, request.getCount());
            this.iad = iad;
            this.previousIds = new long[request.getCount()];
        }

        @Override
        public void add(int indexId, int documentId) throws IOException {
            if (!this.add) {
                this.add = this.iad[0] == indexId && this.iad[1] == documentId;
                if (this.add && this.numHits != 0) {
                    final int x = (this.numHits <= this.count)
                            ? 0 : (this.numHits % this.count);
                    setPrevPageRequestId(getId((int)(this.previousIds[x] >> 32),
                            (int) this.previousIds[x]));
                }
            }

            if (this.add) {
                addId(getId(indexId, documentId));
            }
            else {
                final int x = this.numHits % this.count;
                this.previousIds[x] = (((long)indexId) << 32) + documentId;
            }

            this.numHits++;
        }
    }

    /**
     * @param searchers
     * @param request
     * @param iad searcher index and document index for the document that defines the first result,
     * will be null if the request does not define an offsetId
     * @return a NewsHitCollector
     */
    public static NewsHitCollector getHitCollector(IndexSearcher[] searchers, NewsRequest request,
            int[] iad) {
        if (iad != null) {
            return new OffsetIdHitCollector(searchers, request, iad);
        }
        return new SimpleHitCollector(searchers, request);
    }
}
