/*
 * FeedUpdateFlags.java
 *
 * Created on 20.09.12 13:19
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed;

/**
 * @author oflege
 */
public interface FeedUpdateFlags {
    int FLAG_WITH_TRADE = 0x01;

    int FLAG_WITH_ASK = 0x02;

    int FLAG_WITH_BID = 0x04;

    /**
     * a tick that is neither bid, ask, nor trade but still has some fields that should
     * be captured on a tick-by-tick basis
     */
    int FLAG_WITH_TICK_FIELD = 0x08;

    int FLAGS_WITH_TICK
            = FLAG_WITH_TRADE | FLAG_WITH_BID | FLAG_WITH_ASK | FLAG_WITH_TICK_FIELD;

    /**
     * tick that has subsequently been deleted or corrected, and is no longer considered to be valid.
     * It will usually be skipped in ordinary processing, but is still available for investigations, etc.
     * <p>
     * <b>Important</b> This flag is only used for ticks, not in feed updates
     */
    int FLAG_TICK_CORRECTION_DELETE = 0x10;

    /**
     * a tick that resulted from a correction action, i.e., it either replaced another
     * tick that was deemed invalid (and was marked with the {@link #FLAG_TICK_CORRECTION_DELETE} flag),
     * or it was merely added as an additional tick.
     * <p>
     * <b>Important</b> This flag is only used for ticks, not in feed updates
     */
    int FLAG_TICK_CORRECTION_INSERT = 0x20;

    int FLAGS_TICK_CORRECTION
            = FLAG_TICK_CORRECTION_DELETE | FLAG_TICK_CORRECTION_INSERT;

    int FLAG_WITH_CLOSE_DATE_YESTERDAY = 0x40;

    int FLAG_WITH_OLD_HANDELSDATUM = 0x80;

    int FLAG_PROFESSIONAL_TRADE = 0x100;

    int FLAG_YESTERDAY = 0x200;

    /**
     * set iff ADF_Quelle should be stored as tick field (usually determined per market)
     */
    int FLAG_WITH_QUELLE = 0x400;

    /**
     * Indicates that special (i.e., market specific) tick fields may be present.
     * If this flag is not set (i.e., normal case),
     * tick field processing will stop when the current field order id equals or exceeds
     * {@link de.marketmaker.istar.feed.vwd.VwdFieldOrder#FIRST_NON_TICK}. If this flag is set,
     * however, the remaining fields need to be scanned for the special tick fields as well.
     */
    int FLAG_WITH_SPECIAL_TICK_FIELDS = 0x800;

    int FLAG_WITH_NON_DYNAMIC_FIELD = 0x1000;
}
