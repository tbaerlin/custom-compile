/*
 * FeedDataVkeyOnly.java
 *
 * Created on 09.02.2005 16:24:04
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed;

import net.jcip.annotations.Immutable;

import de.marketmaker.istar.common.util.ByteString;
import de.marketmaker.istar.feed.ordered.OrderedFeedData;
import de.marketmaker.istar.feed.ordered.OrderedSnapData;
import de.marketmaker.istar.feed.ordered.tick.OrderedTickData;

/**
 * A dummy FeedData implementation that supports only the Vendorkey and FeedMarket attributes.
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
@Immutable
public class FeedDataVkeyOnly implements OrderedFeedData {

    public static final FeedDataFactory FACTORY = FeedDataVkeyOnly::new;

    private static final FeedMarketRepository MARKET_REPOSITORY = new FeedMarketRepository(false);

    private final Vendorkey vendorkey;

    private final FeedMarket market;

    public FeedDataVkeyOnly(Vendorkey vendorkey, FeedMarket market) {
        this.vendorkey = vendorkey;
        this.market = (market != null) ? market : MARKET_REPOSITORY.getMarket(vendorkey.getMarketName());
    }

    public FeedDataVkeyOnly(Vendorkey vendorkey) {
        this(vendorkey, null);
    }

    public Vendorkey getVendorkey() {
        return this.vendorkey;
    }

    public ByteString getVwdcode() {
        return this.vendorkey.toVwdcode();
    }

    public OrderedSnapData getSnapData(boolean realtime) {
        return null;
    }

    public FeedMarket getMarket() {
        return this.market;
    }

    public int getState() {
        return 0;
    }

    public void setVendorkeyType(int type) {
        // empty
    }

    public int getVendorkeyType() {
        return this.vendorkey.getType();
    }

    public boolean isGarbage() {
        return false;
    }

    public boolean isDeleted() {
        return false;
    }

    public void setState(int update) {
    }

    public boolean isReadyForPush() {
        return false;
    }

    public void setReadyForPush(boolean value) {
    }

    @Override
    public int getCreatedTimestamp() {
        return 0;
    }

    @Override
    public void setCreatedTimestamp(int ts) {

    }

    @Override
    public OrderedTickData getOrderedTickData() {
        return null;
    }
}
