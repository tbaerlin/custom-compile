/*
 * TotalHitCountCollector.java
 *
 * Created on 03.07.12 08:57
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.news.backend;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.Scorer;

/**
 * Just counts hits, code copied from org.apache.lucene.search.TotalHitCountCollector,
 * which was added to lucene in version 3.1
 * TODO: remove after lucene upgrade
 * @author oflege
 */
class TotalHitCountCollector extends Collector {
    private int totalHits;

    /** Returns how many hits matched the search. */
    public int getTotalHits() {
        return this.totalHits;
    }

    @Override
    public void setScorer(Scorer scorer) {
    }

    @Override
    public void collect(int doc) {
        this.totalHits++;
    }

    @Override
    public void setNextReader(IndexReader reader, int docBase) {
    }

    @Override
    public boolean acceptsDocsOutOfOrder() {
        return true;
    }
}
