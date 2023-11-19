/*
 * OrderedFeedData.java
 *
 * Created on 30.08.12 14:39
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.ordered;

import de.marketmaker.istar.common.util.ByteString;
import de.marketmaker.istar.feed.FeedData;
import de.marketmaker.istar.feed.FeedMarket;
import de.marketmaker.istar.feed.Vendorkey;
import de.marketmaker.istar.feed.ordered.tick.OrderedTickData;
import de.marketmaker.istar.feed.vwd.VendorkeyVwd;

import static de.marketmaker.istar.feed.DateTimeProvider.Timestamp.toDateTime;

/**
 * @author oflege
 */
class OrderedFeedDataImpl implements OrderedFeedData {

    private final ByteString vwdcode;

    private final FeedMarket market;

    private final OrderedSnapData rtSnap;

    private final OrderedSnapData ntSnap;

    private final OrderedTickData tickData;

    private int created;

    private int flags;

    OrderedFeedDataImpl(Vendorkey vendorkey, FeedMarket market,
            OrderedSnapData rt, OrderedSnapData nt, OrderedTickData tickData) {
        this.vwdcode = vendorkey.toVwdcode();
        this.flags = vendorkey.getType() << TYPE_BIT_OFFSET;
        this.market = market;
        this.rtSnap = rt;
        this.ntSnap = nt;
        this.tickData = tickData;
    }

    @Override
    public FeedMarket getMarket() {
        return this.market;
    }

    @Override
    public void setCreatedTimestamp(int ts) {
        this.created = ts;
    }

    @Override
    public int getCreatedTimestamp() {
        return this.created;
    }

    @Override
    public OrderedSnapData getSnapData(boolean realtime) {
        return realtime ? this.rtSnap : this.ntSnap;
    }

    @Override
    public OrderedTickData getOrderedTickData() {
        return this.tickData;
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

    @Override
    public synchronized String toString() {
        StringBuilder sb = new StringBuilder(200);
        sb.append("OrderedFeedDataImpl{").append(this.vwdcode);
        sb.append(", created=").append(toDateTime(getCreatedTimestamp()));
        sb.append(", flags=").append(getStateStr());
        if (this.rtSnap != null) {
            sb.append(", rt=").append(toDateTime(this.rtSnap.getLastUpdateTimestamp()));
        }
        if (this.ntSnap != null) {
            sb.append(", nt=").append(toDateTime(this.ntSnap.getLastUpdateTimestamp()));
        }
        if (this.tickData != null) {
            sb.append(", tick=").append(this.tickData);
        }
        return sb.append('}').toString();
    }
}
