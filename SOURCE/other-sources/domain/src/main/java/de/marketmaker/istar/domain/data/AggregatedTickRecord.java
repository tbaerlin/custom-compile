/*
 * AggregatedTickRecord.java
 *
 * Created on 03.03.2005 15:12:58
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domain.data;

import de.marketmaker.istar.domain.timeseries.Timeseries;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.joda.time.ReadableDuration;
import org.joda.time.ReadableInterval;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public interface AggregatedTickRecord {

    /**
     * @return an interval consisting of this record's data first and last timestamp
     * ...or null if this record contains no data
     */
    @Nullable
    ReadableInterval getInterval();

    /**
     * @return the duration of this record, defined at construction time
     */
    ReadableDuration getAggregation();

    /**
     * @return TickType defined at construction time
     */
    TickType getTickType();

    /**
     * @param interval contains the start and end time instance for the requested timeseries
     * @return a new instance of a timeseries that is based on this records tick data
     */
    Timeseries<AggregatedTick> getTimeseries(ReadableInterval interval);

    /**
     * @param requestedAggregation the new aggregation duration
     * @return true if the record can be aggregated in the provided interval
     */
    boolean canAggregateTo(ReadableDuration requestedAggregation);

    /**
     * @param duration the max length of the aggregation
     *                 note that first and last aggregation might be cut off and shorter
     * @param interval the total time span for all aggregations,
     * @return a new instance of AggregatedTickRecord
     */
    AggregatedTickRecord aggregate(ReadableDuration duration, ReadableInterval interval);

    /**
     * @param that the record of tick items to be merged into this instance
     * @return a new instance including this and that's tickItems
     */
    AggregatedTickRecord merge(AggregatedTickRecord that);

}
