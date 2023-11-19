/*
 * AggregatedTick.java
 *
 * Created on 01.03.2005 13:59:51
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domain.data;

import org.joda.time.Period;
import org.joda.time.Interval;
import org.joda.time.ReadableInterval;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public interface AggregatedTick {
    ReadableInterval getInterval();
    long getOpen();
    long getHigh();
    long getLow();
    long getClose();
    long getVolume();
    int getNumberOfAggregatedTicks();
}
