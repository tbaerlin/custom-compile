/*
 * EodPriceHistory.java
 *
 * Created on 11.12.12 15:15
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.history.eod.write;

import java.io.IOException;

import it.unimi.dsi.fastutil.ints.Int2ObjectSortedMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectAVLTreeMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectSortedMap;
import it.unimi.dsi.fastutil.objects.ObjectBidirectionalIterator;
import it.unimi.dsi.fastutil.objects.ObjectSortedSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.marketmaker.istar.merger.provider.history.eod.EodUtil;

/**
 * @author zzhao
 */
class EodPriceHistory implements EodReader<EodPrices> {

    private static final Logger log = LoggerFactory.getLogger(EodPriceHistory.class);

    private final Long2ObjectSortedMap<byte[]> data = new Long2ObjectAVLTreeMap<>();

    private int fromDate = Integer.MAX_VALUE;

    private int toDate = Integer.MIN_VALUE;

    int getFromDate() {
        return fromDate;
    }

    int getToDate() {
        return toDate;
    }

    private void reset() {
        this.data.clear();
        log.info("<reset> cleared eod prices");
        this.fromDate = Integer.MAX_VALUE;
        this.toDate = Integer.MIN_VALUE;
    }

    @Override
    public EodIterator<EodPrices> iterator() {
        return new MyIterator(this.data.long2ObjectEntrySet());
    }

    @Override
    public void close() throws IOException {
        reset();
    }

    boolean isEmpty() {
        return this.data.isEmpty();
    }

    void update(long quote, int date, Int2ObjectSortedMap<byte[]> data) {
        if (!this.data.containsKey(quote)) {
            this.data.put(quote, EodPrices.withTick(EodUtil.EMPTY_BA, date, data));
        }
        else {
            this.data.put(quote, EodPrices.withTick(this.data.get(quote), date, data));
        }

        if (this.fromDate > date) {
            this.fromDate = date;
        }
        if (this.toDate < date) {
            this.toDate = date;
        }
    }

    private static final class MyIterator implements EodIterator<EodPrices> {

        private EodPrices eodPrices = new EodPrices();

        private final ObjectBidirectionalIterator<Long2ObjectMap.Entry<byte[]>> it;

        private long quote;

        MyIterator(ObjectSortedSet<Long2ObjectMap.Entry<byte[]>> set) {
            this.it = set.iterator();
        }

        @Override
        public long getQuote() {
            return this.quote;
        }

        @Override
        public boolean hasNext() {
            return this.it.hasNext();
        }

        @Override
        public EodPrices next() {
            final Long2ObjectMap.Entry<byte[]> entry = this.it.next();
            this.quote = entry.getLongKey();
            this.eodPrices.withData(entry.getValue());
            return this.eodPrices;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("not implemented");
        }
    }
}
