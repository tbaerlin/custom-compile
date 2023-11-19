/*
 * EndOfDayProvider.java
 *
 * Created on 29.01.2009 17:04:37
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.vwd;

import java.util.BitSet;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import de.marketmaker.istar.feed.Vendorkey;

/**
 * Provides information for a vendorkey whether at the given time the realtime snap data can be
 * used as is for clients that are only allowed end-of-day prices.
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public interface EndOfDayProvider {
    /**
     * Convenience method, calls {@link #getEodFilter(de.marketmaker.istar.feed.Vendorkey, org.joda.time.DateTime)}
     * with <code>vkey</code> and <code>DateTime.now()</code>
     */
    EndOfDayFilter getEodFilter(Vendorkey v);

    /**
     * Returns null iff the realtime snap for the vendorkey can be used as is for a client that is only
     * allowed to see end of day data or if the data has to be filtered and otherwise a BitSet
     * with ids of fields that are not allowed.
     * @param vkey vwd feed symbol
     * @param now reference timestamp
     * @return BitSet if filtering is required, null otherwise
     */
    EndOfDayFilter getEodFilter(Vendorkey vkey, DateTime now);

    /**
     * In all cases where {@link #getEodFilterStart(de.marketmaker.istar.feed.Vendorkey, org.joda.time.DateTime)}
     * returns a non-null result, this method, if invoked with the same parameters, returns the
     * timestamp prior to <code>now</code> at which the current eod period began.
     */
    DateTime getEodFilterStart(Vendorkey vkey, DateTime now);

    /**
     * Returns time zone for given market or null if market is not configured
     * @param vwdmarket vwd market symbol
     * @return time zone for market or null                            
     */
    DateTimeZone getTimeZone(String vwdmarket);
}
