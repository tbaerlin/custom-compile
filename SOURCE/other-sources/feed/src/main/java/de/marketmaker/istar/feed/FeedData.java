/*
 * FeedData.java
 *
 * Created on 25.10.2004 15:15:38
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed;

import java.util.Comparator;

import de.marketmaker.istar.common.util.ByteString;
import de.marketmaker.istar.feed.snap.SnapData;


/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public interface FeedData {

    /**
     * Indicates that object is considered active
     * Transitions:
     * - to DELETED if a delete is received
     * - to STALE during mark phase of FeedDataGC
     */
    int STATE_UPDATED = 0x00;

    /**
     * Indicates that the object should be removed from repository by FeedDataGC sweep phase
     * Transitions:
     * - to UPDATED if an update is received
     * - to DELETED if a delete is received
     */
    int STATE_GARBAGE = 0x02;

    /**
     * Indicates that a static or dynamic delete message has been received.
     * Transitions:
     * - to UPDATED if an update is received
     */
    int STATE_DELETED = 0x03;

    int STATE_NEW = 0x04;

    int STATE_MASK = 0x07;

    int NOT_STATE_MASK = ~STATE_MASK;

    int FLAG_READY_FOR_PUSH = 0x10;

    int TYPE_BIT_OFFSET = 24;

    Comparator<FeedData> COMPARATOR_BY_VENDORKEY = new Comparator<FeedData>() {
        @Override
        public int compare(FeedData o1, FeedData o2) {
            final int cmp = compareTypeLexicographically(o1.getVendorkeyType(), o2.getVendorkeyType());
            return (cmp != 0) ? cmp : o1.getVwdcode().compareTo(o2.getVwdcode());
        }

        int compareTypeLexicographically(final int t1, final int t2) {
            if (t1 < 10) {
                return (t2 < 10) ? (t1 - t2) : (t1 * 10 - t2);
            }
            else {
                return (t2 < 10) ? (t1 - t2 * 10) : (t1 -t2);
            }
        }
    };

    Comparator<FeedData> COMPARATOR_BY_VWDCODE
            = (o1, o2) -> o1.getVwdcode().compareTo(o2.getVwdcode());

    FeedMarket getMarket();

    SnapData getSnapData(boolean realtime);

    Vendorkey getVendorkey();

    ByteString getVwdcode();

    default boolean isGarbage() {
        return getState() == STATE_GARBAGE;
    }

    default boolean isDeleted() {
        return getState() == STATE_DELETED;
    }

    boolean isReadyForPush();

    void setReadyForPush(boolean value);

    void setState(int update);

    int getState();

    void setVendorkeyType(int type);

    int getVendorkeyType();

    default void dispose() {
        disposeSnapData(true);
        disposeSnapData(false);
    }

    default void disposeSnapData(boolean realtime) {
        final SnapData sd = getSnapData(realtime);
        if (sd != null) {
            sd.dispose();
        }
    }

    default boolean isToBeDisposed() {
        return isGarbage() || isDeleted();
    }

    default String getStateStr() {
        final int s = getState();
        switch (s) {
            case FeedData.STATE_UPDATED:
                return "U";
            case FeedData.STATE_DELETED:
                return "D";
            case FeedData.STATE_GARBAGE:
                return "G";
            case FeedData.STATE_NEW:
                return "N";
            default:
                return "0x" + Integer.toHexString(s);
        }
    }
}
