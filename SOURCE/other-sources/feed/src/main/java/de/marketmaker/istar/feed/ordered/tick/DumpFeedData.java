/*
 * DumpFeedData.java
 *
 * Created on 20.01.15 09:37
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.ordered.tick;

import de.marketmaker.istar.common.util.ByteString;
import de.marketmaker.istar.domain.data.NullSnapRecord;
import de.marketmaker.istar.domain.data.SnapRecord;
import de.marketmaker.istar.feed.FeedData;
import de.marketmaker.istar.feed.FeedDataFactory;
import de.marketmaker.istar.feed.FeedMarket;
import de.marketmaker.istar.feed.Vendorkey;
import de.marketmaker.istar.feed.ordered.OrderedFeedData;
import de.marketmaker.istar.feed.ordered.OrderedSnapData;
import de.marketmaker.istar.feed.ordered.OrderedSnapRecord;
import de.marketmaker.istar.feed.snap.IndexAndOffset;
import de.marketmaker.istar.feed.vwd.VendorkeyVwd;

import static de.marketmaker.istar.feed.DateTimeProvider.Timestamp.toDateTime;

/**
 * @author oflege
 */
public class DumpFeedData implements OrderedFeedData, OrderedSnapData {
    public static final FeedDataFactory FACTORY = DumpFeedData::new;

    private final ByteString vwdcode;

    private final FeedMarket market;

    private final OrderedTickData tickData;

    private int created;

    private int updated;

    private int flags;

    DumpFeedData(Vendorkey vendorkey, FeedMarket market) {
        this.vwdcode = vendorkey.toVwdcode();
        this.flags = vendorkey.getType() << TYPE_BIT_OFFSET;
        this.market = market;
        this.tickData = new OrderedTickData();
        setState(STATE_NEW);
    }

    @Override
    public synchronized String toString() {
        final StringBuilder sb = new StringBuilder(200);
        sb.append("DumpFeedData{").append(this.vwdcode);
        sb.append(", created=").append(toDateTime(getCreatedTimestamp()));
        sb.append(", flags=").append(getStateStr());
        sb.append(", tick=").append(this.tickData);
        return sb.append('}').toString();
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
        return this;
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
    public int getLastUpdateTimestamp() {
        return this.updated;
    }

    @Override
    public void setLastUpdateTimestamp(int lastUpdateTimestamp) {
        this.updated = lastUpdateTimestamp;
    }

    @Override
    public void init(OrderedSnapRecord src) {
        // empty
    }

    @Override
    public byte[] getData(boolean copy) {
        return null;
    }

    @Override
    public IndexAndOffset getIndexAndOffset() {
        return null;
    }

    @Override
    public boolean isInitialized() {
        return false;
    }

    @Override
    public void init(IndexAndOffset indexAndOffset, byte[] data) {
        // empty
    }

    @Override
    public SnapRecord toSnapRecord(int nominalDelayInSeconds) {
        return NullSnapRecord.INSTANCE;
    }

    @Override
    public void dispose() {
        // inherited from both implemented interfaces, this method resolves the conflict
        // empty, nothing to dispose
    }
}
