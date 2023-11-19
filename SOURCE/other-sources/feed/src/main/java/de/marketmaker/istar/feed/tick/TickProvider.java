/*
 * TickProvider.java
 *
 * Created on 08.12.2004 11:44:07
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.tick;

import net.jcip.annotations.NotThreadSafe;

import de.marketmaker.istar.feed.FeedData;

import java.io.IOException;

/**
 * Provides methods to retrieve raw encoded tick data.
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public interface TickProvider {
    @NotThreadSafe
    public interface Result {
        /**
         * This method <b>should be invoked WITHOUT being synchronized on the FeedData object</b>; If
         * tick data is stored on disk, it will only be retrieved when this method is called, so
         * not being synchronized is a good thing as it will allow the feed processing to continue
         * even if the disk reads take some time.
         * @return tick data or an array of length 0.
         */
        byte[] getTicks();

        int[] getStorageInfo();

        /**
         * @return how data returned by {@link #getTicks()} is encoded
         */
        AbstractTickRecord.TickItem.Encoding getEncoding();
    }

    /**
     *
     * @param data for which ticks are requested. <b>You must be synchronized on data when
     * calling this method</b>
     * @param date day in format yyyyMMdd
     * @param storageInfo if not <tt>null</tt>: describes which data the client already has:
     * <tt>storageInfo[0]</tt> = length in file, <tt>storageInfo[1] .. storageInfo[n]</tt>
     * length of in-memory chunks.
     * @return tick data result
     */
    Result getTicks(FeedData data, int date, int[] storageInfo);
}
