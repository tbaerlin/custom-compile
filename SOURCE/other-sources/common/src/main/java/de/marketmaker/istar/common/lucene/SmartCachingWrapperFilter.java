package de.marketmaker.istar.common.lucene;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.CachingWrapperFilter;
import org.apache.lucene.search.DocIdSet;
import org.apache.lucene.search.DocIdSetIterator;
import org.apache.lucene.search.Filter;
import org.apache.lucene.util.OpenBitSet;
import org.apache.lucene.util.OpenBitSetDISI;
import org.apache.lucene.util.SortedVIntList;

/**
 * An extended {@link org.apache.lucene.search.CachingWrapperFilter} that uses
 * {@link org.apache.lucene.util.SortedVIntList} objects to cache filter results with small
 * cardinality.
 */
public class SmartCachingWrapperFilter extends CachingWrapperFilter {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * sizeFactor to ensure that the cached filter will occupy as little space as possible;
     * however, combining the filter with another one in
     * a {@link de.marketmaker.istar.common.lucene.ChainedFilter} could be quite expensive.
     */
    public static final int MIN_BYTES_FACTOR = 9;

    /**
     * sizeFactor to ensure max. performance when combining the filter with another one in
     * a {@link de.marketmaker.istar.common.lucene.ChainedFilter}; comes at the cost of
     * a higher memory usage.
     */
    public static final int MAX_PERF_FACTOR = 80;

    private final Filter delegate;

    private final int sizeFactor;

    public SmartCachingWrapperFilter(Filter filter) {
        this(filter, MIN_BYTES_FACTOR);
    }

    public SmartCachingWrapperFilter(Filter filter, int sizeFactor) {
        super(filter);
        this.delegate = filter;
        this.sizeFactor = sizeFactor;
    }

    /**
     * a copy of the super method it overrides with additional call
     * of the {@link #compact(org.apache.lucene.search.DocIdSet, org.apache.lucene.index.IndexReader)}
     * method.
     */
    protected DocIdSet docIdSetToCache(DocIdSet docIdSet,
            IndexReader reader) throws IOException {
        if (docIdSet == null) {
            // this is better than returning null, as the nonnull result can be cached
            return DocIdSet.EMPTY_DOCIDSET;
        }
        else if (docIdSet.isCacheable()) {
            return compact(docIdSet, reader);
        }
        else {
            final DocIdSetIterator it = docIdSet.iterator();
            // null is allowed to be returned by iterator(),
            // in this case we wrap with the empty set,
            // which is cacheable.
            return (it == null) ? DocIdSet.EMPTY_DOCIDSET
                    : compact(new OpenBitSetDISI(it, reader.maxDoc()), reader);
        }
    }

    private DocIdSet compact(DocIdSet d, IndexReader reader) {
        if (d instanceof OpenBitSet) {
            final OpenBitSet bs = (OpenBitSet) d;
            final long card = bs.cardinality();
            if (card == 0L) {
                return DocIdSet.EMPTY_DOCIDSET;
            }
            final int maxDoc = reader.maxDoc();
            if (maxDoc > 128 && card * this.sizeFactor < maxDoc) {
                logCreate(card, true);
                return new SortedVIntList(bs);
            }
            else {
                logCreate(card, false);
            }
        }
        return d;
    }

    private void logCreate(long cardinality, boolean svint) {
        if (!this.logger.isDebugEnabled()) {
            return;
        }
        final StringBuilder sb = new StringBuilder(100);
        sb.append("<create> for ");
        sb.append("'").append(this.delegate.toString()).append("': #").append(cardinality);
        if (svint) {
            sb.append(" SVIntList");
        }
        this.logger.debug(sb.toString());
    }
}
