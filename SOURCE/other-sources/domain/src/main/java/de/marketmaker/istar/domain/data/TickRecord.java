/*
 * TickRecord.java
 *
 * Created on 02.03.2005 15:45:16
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domain.data;

import org.joda.time.ReadableDuration;
import org.joda.time.ReadableInterval;

import de.marketmaker.istar.domain.timeseries.Timeseries;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public interface TickRecord {
    /**
     * Returns an interval describing the days with tick data contained in this object.
     * @return
     */
    ReadableInterval getInterval();

    /**
     * Aggregates the ticks of the given type with the given duration.
     * @param duration Aggregation interval, has to be at least 1s, at most 86400s, and it must
     * be an even part of a day (i.e., 86400 % duration.inSecs == 0)
     * @param type specifies which ticks should be aggregated
     * @return AggregatedTickRecord with aggregated data for the same days as this object holds
     */
    AggregatedTickRecord aggregate(ReadableDuration duration, TickType type);

    /**
     * Creates a new TickRecord with data for all days in both this record and in tr; if a day is
     * present in this record and in tr, tr's data will be used.
     * @param tr to be merged with this one
     * @return merged record
     */
    TickRecord merge(TickRecord tr);

    /**
     * Returns a Tick-Timeseries for all ticks in the given interval of the given type.
     * @param interval only ticks in this interval will be part of the timeseries
     * @param type only ticks of this type will be part of the timeseries
     * @return timeseries
     */ 
    Timeseries<Tick> getTimeseries(ReadableInterval interval, TickType type);

    /**
     * Returns a TickEvent-Timeseries for all ticks events in the given interval.
     * {@link de.marketmaker.istar.domain.data.TickEvent#getAdditionalFields()} will return
     * null for all events, if those fields are needed, use
     * {@link #getTimeseriesWithAdditionalFields(org.joda.time.ReadableInterval)} instead.
     * @param interval only ticks in this interval will be part of the timeseries
     * @return timeseries
     */
    Timeseries<TickEvent> getTimeseries(ReadableInterval interval);

    /**
     * Returns a TickEvent-Timeseries for all ticks events in the given interval,
     * {@link de.marketmaker.istar.domain.data.TickEvent#getAdditionalFields()} will return
     * additional tick fields if any are present.
     * @param interval only ticks in this interval will be part of the timeseries
     * @return timeseries
     */
    Timeseries<TickEvent> getTimeseriesWithAdditionalFields(ReadableInterval interval);

    /**
     * Returns true if this tickRecord contains delayed tick data.
     */
    boolean isDelayed();

    /**
     * Number of bytes used to store tick data;
     * @return size of ticks
     */
    int tickSize();
}
