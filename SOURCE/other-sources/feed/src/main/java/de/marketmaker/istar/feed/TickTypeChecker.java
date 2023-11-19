/*
 * TickTypeChecker.java
 *
 * Created on 09.12.2004 15:34:39
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public interface TickTypeChecker {

    /**
     * @param record to be tested
     * @return an int with corresponding bits set for each applicable flag in
     * {@link de.marketmaker.istar.feed.FeedUpdateFlags}
     */
    int getTickFlags(final ParsedRecord record);

    /**
     * Returns either this object or a variant of this object that has the same trade/bid/ask checks
     * and is possibly adapted to specific aspects of the given market.
     * See {@link de.marketmaker.istar.feed.vwd.TickTypeCheckerVwd} for an example.
     * @param marketName name of market for which checker is requested
     * @return adapted checker
     */
    TickTypeChecker forMarket(String marketName);
}
