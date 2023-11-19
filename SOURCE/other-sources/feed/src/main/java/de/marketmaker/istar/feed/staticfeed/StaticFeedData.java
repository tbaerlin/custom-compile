/*
 * OrderedFeedData.java
 *
 * Created on 30.08.12 14:39
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.staticfeed;

import de.marketmaker.istar.common.util.ByteString;
import de.marketmaker.istar.feed.FeedData;
import de.marketmaker.istar.feed.FeedDataFactory;
import de.marketmaker.istar.feed.FeedMarket;
import de.marketmaker.istar.feed.Vendorkey;
import de.marketmaker.istar.feed.ordered.OrderedFeedData;
import de.marketmaker.istar.feed.ordered.OrderedSnapData;
import de.marketmaker.istar.feed.ordered.OrderedSnapDataImpl;
import de.marketmaker.istar.feed.ordered.tick.OrderedTickData;
import de.marketmaker.istar.feed.vwd.VendorkeyVwd;

/**
 * @author oflege
 */
public class StaticFeedData implements OrderedFeedData {

    public static final FeedDataFactory FACTORY = StaticFeedData::new;

    private final ByteString vwdcode;

    private final FeedMarket market;

    private final OrderedSnapData rtSnap;

    private int created;

    private int flags;

    private StaticFeedData(Vendorkey vendorkey, FeedMarket market) {
        this.vwdcode = vendorkey.toVwdcode();
        this.market = market;
        this.flags = vendorkey.getType() << TYPE_BIT_OFFSET;
        this.rtSnap = new OrderedSnapDataImpl();
        setState(FeedData.STATE_NEW);
    }

    @Override
    public int getCreatedTimestamp() {
        return this.created;
    }

    @Override
    public void setCreatedTimestamp(int ts) {
        this.created = ts;
    }

    @Override
    public FeedMarket getMarket() {
        return this.market;
    }

    @Override
    public OrderedSnapData getSnapData(boolean realtime) {
        return realtime ? this.rtSnap : null;
    }

    @Override
    public OrderedTickData getOrderedTickData() {
        return null;
    }


    @Override
    public Vendorkey getVendorkey() {
        return VendorkeyVwd.getInstance(this.vwdcode, getVendorkeyType());
    }

    @Override
    public ByteString getVwdcode() {
        return this.vwdcode;
    }

    @Override
    public boolean isReadyForPush() {
        return hasFlag(FLAG_READY_FOR_PUSH);
    }

    @Override
    public void setReadyForPush(boolean value) {
        if (value) {
            this.flags |= FLAG_READY_FOR_PUSH;
        }
        else if (isReadyForPush()) {
            this.flags ^= FLAG_READY_FOR_PUSH;
        }
    }

    @Override
    public void setState(int update) {
        this.flags = (this.flags & NOT_STATE_MASK) | update;
    }

    @Override
    public int getState() {
        return this.flags & FeedData.STATE_MASK;
    }

    @Override
    public void setVendorkeyType(int type) {
        this.flags = (this.flags & 0x00FFFFFF) | (type << TYPE_BIT_OFFSET);
    }

    @Override
    public int getVendorkeyType() {
        return this.flags >>> TYPE_BIT_OFFSET;
    }

    private boolean hasFlag(final int flag) {
        return (this.flags & flag) != 0;
    }
}
