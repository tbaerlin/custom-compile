/*
 * FileKeySource.java
 *
 * Created on 17.12.2009 17:24:32
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.dp;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import de.marketmaker.istar.common.util.ByteString;
import de.marketmaker.istar.feed.FeedData;
import de.marketmaker.istar.feed.FeedDataRepository;
import de.marketmaker.istar.feed.FeedMarket;
import de.marketmaker.istar.feed.vwd.VendorkeyVwd;

/**
 * @author oflege
 */
class MarketKeySource implements KeySource {
    enum TypeMapping { NONE, NEW, OLD}

    private final TypeMapping typeMapping;

    private final Iterator<FeedMarket> marketIterator;

    private Iterator<FeedData> feedDataIterator;

    private FeedData last;

    MarketKeySource(FeedDataRepository repository, Set<ByteString> marketNames, TypeMapping typeMapping) {
        final List<FeedMarket> markets = new ArrayList<>();
        for (ByteString marketName : marketNames) {
            final FeedMarket market = repository.getMarket(marketName);
            if (market != null) {
                markets.add(market);
            }
        }
        this.marketIterator = markets.iterator();
        this.typeMapping = typeMapping;
    }

    public boolean hasNext() throws IOException {
        if (this.feedDataIterator != null && this.feedDataIterator.hasNext()) {
            return true;
        }
        while (this.marketIterator.hasNext()) {
            final FeedMarket market = this.marketIterator.next();
            final List<FeedData> datas = market.collect(fd -> !fd.isDeleted());
            if (!datas.isEmpty()) {
                this.feedDataIterator = datas.iterator();
                return true;
            }
        }
        return false;
    }

    public FeedData nextFeedData() {
        this.last = this.feedDataIterator.next();
        return this.last;
    }

    public ByteString getAlias() {
        VendorkeyVwd vkey = (VendorkeyVwd) this.last.getVendorkey();
        switch (this.typeMapping) {
            case NONE:
                return vkey.toVwdcode();
            case NEW:
                return vkey.toByteString(false);
            case OLD:
                return vkey.toByteString(true);
            default:
                throw new IllegalArgumentException();
        }
    }

    public void close() throws IOException {
        // empty
    }
}