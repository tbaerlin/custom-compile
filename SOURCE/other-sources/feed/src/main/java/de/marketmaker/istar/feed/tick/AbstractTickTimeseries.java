/*
 * AbstractTickTimeseries.java
 *
 * Created on 08.01.13 12:04
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.tick;

/**
 * @author oflege
 */

import org.joda.time.Interval;
import org.joda.time.ReadableInterval;

import de.marketmaker.istar.domain.timeseries.Timeseries;

import static org.joda.time.DateTimeConstants.SECONDS_PER_DAY;

/**
 * Base class for an iterable Timeseries.
 */
abstract class AbstractTickTimeseries<K> implements Timeseries<K> {

    protected final ReadableInterval interval;

    protected final TickRecordImpl tickRecord;

    protected AbstractTickTimeseries(TickRecordImpl tickRecord, ReadableInterval interval) {
        this.tickRecord = tickRecord;
        this.interval = tickRecord.adaptInterval(interval);
    }

    protected int getStartSec(final Interval itemInterval) {
        return (itemInterval.getStartMillis() < interval.getStartMillis())
                ? (int) ((interval.getStartMillis() - itemInterval.getStartMillis()) / 1000)
                : 0;
    }

    protected int getEndSec(final Interval itemInterval) {
        return (interval.getEndMillis() < itemInterval.getEndMillis())
                ? SECONDS_PER_DAY - (int) ((itemInterval.getEndMillis() - interval.getEndMillis()) / 1000)
                : SECONDS_PER_DAY;
    }
}