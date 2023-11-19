/*
 * FeedData.java
 *
 * Created on 25.10.2004 15:15:38
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed;

import net.jcip.annotations.GuardedBy;

import de.marketmaker.istar.common.util.ByteString;
import de.marketmaker.istar.feed.snap.SnapData;
import de.marketmaker.istar.feed.snap.SnapDataDefault;
import de.marketmaker.istar.feed.vwd.VendorkeyVwd;

/**
 * FeedData implementation.<br>
 * <b>Important:</b> Objects of this class are expected to be used by multiple threads (at least
 * for reading and writing properties) and using them requires <em>external</em> synchronization
 * on the FeedDataImpl object. For example:<p>
 * <pre>
 * final FeedData fd = getFeedData();
 * synchronized(fd) {
 *      SnapData sd = fd.getSnapData(true);
 *      // update or read data from sd
 *      ...
 * }
 * </pre>
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class FeedDataImpl implements FeedData {
    private static final boolean WITH_DELAY =
            Boolean.valueOf(System.getProperty("istar.feedData.withDelay", "true"));

    private final ByteString vwdcode;

    private final SnapData snapData;

    private final SnapData snapDataDelay;

    private final FeedMarket market;

    private int flags;

    public FeedDataImpl(final Vendorkey vendorkey, final FeedMarket market) {
        this.vwdcode = vendorkey.toVwdcode();
        this.flags = vendorkey.getType() << TYPE_BIT_OFFSET;
        this.market = market;
        this.snapData = new SnapDataDefault();
        this.snapDataDelay = WITH_DELAY ? new SnapDataDefault() : null;
        setState(STATE_NEW);
    }

    @GuardedBy("this")
    public void setState(int update) {
        this.flags = (this.flags & NOT_STATE_MASK) | update;
    }

    @GuardedBy("this")
    public int getState() {
        return this.flags & FeedData.STATE_MASK;
    }

    @GuardedBy("this")
    public void setVendorkeyType(int type) {
        this.flags = (this.flags & 0x00FFFFFF) | (type << TYPE_BIT_OFFSET);
    }

    @GuardedBy("this")
    public int getVendorkeyType() {
        return this.flags >>> TYPE_BIT_OFFSET;
    }

    @GuardedBy("this")
    public boolean isGarbage() {
        return getState() == FeedData.STATE_GARBAGE;
    }

    @GuardedBy("this")
    public boolean isDeleted() {
        return getState() == FeedData.STATE_DELETED;
    }

    public void setReadyForPush(boolean value) {
        if (value) {
            this.flags |= FLAG_READY_FOR_PUSH;
        }
        else if (isReadyForPush()) {
            this.flags ^= FLAG_READY_FOR_PUSH;
        }
    }

    public boolean isReadyForPush() {
        return (this.flags & FLAG_READY_FOR_PUSH) != 0;
    }

    @GuardedBy("this")
    public Vendorkey getVendorkey() {
        return VendorkeyVwd.getInstance(this.vwdcode, getVendorkeyType());
    }

    public ByteString getVwdcode() {
        return this.vwdcode;
    }

    @GuardedBy("this")
    public SnapData getSnapData(boolean realtime) {
        return realtime ? this.snapData : this.snapDataDelay;
    }

    @GuardedBy("this")
    public FeedMarket getMarket() {
        return this.market;
    }

    @GuardedBy("this")
    public String toString() {
        return "FeedDataImpl["
                + getVendorkeyType() + "." + this.vwdcode
                + ", " + this.market
                + ", " + this.snapData
                + "]";
    }
}
