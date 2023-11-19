/*
 * Tick.java
 *
 * Created on 01.03.2005 11:24:18
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domain.data;

import org.joda.time.Instant;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public interface Tick {
    /**
     * Returns the time of this tick as an absolute instant in time. Likely to be much
     * more expensive to compute than {@link #getSecondsInDay()}, but you do not need to
     * get the day of this tick from somewhere else.
     * @return time of this tick.
     */
    Instant getInstant();

    /**
     * Returns the time of this tick; likely to be much faster than {@link #getInstant()},
     * but if you need to determine the tick's day as well this method will not work for you.
     * @return time of this tick
     */
    int getSecondsInDay();

    long getPrice();

    long getVolume();

    String getSupplement();

    String getTradeIdentifier();

    TickType getType();

    TickProperties getProperties();
}
