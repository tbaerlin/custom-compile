package de.marketmaker.istar.domainimpl;

import de.marketmaker.istar.domain.data.DataWithInterval;
import de.marketmaker.istar.domain.data.TickEvent;
import de.marketmaker.istar.domain.data.TickImpl;
import de.marketmaker.istar.domain.data.TickList;
import org.joda.time.DateTime;

import java.util.BitSet;
import java.util.Iterator;

/**
 * Caching of models containing iterator instances would lead to errors on repeated evaluation of the model
 * due to exhausted iterators. Therefore this {@link Iterable} creates on each evaluation a new {@link Iterator}.
 */
public class AggregatedTickEventIterable implements Iterable<AggregatedTickEvent> {

    protected final Iterable<DataWithInterval<TickEvent>> timeseries;

    protected final TickImpl.Type tickType;

    protected final int numTicks;

    protected final long start;

    protected final long end;

    protected final BitSet additionalFieldIds;

    private final TickList.FieldPermissions permissions;

    public static AggregatedTickEventIterable withStart(final Iterable<DataWithInterval<TickEvent>> timeseries,
                                     final TickImpl.Type tickType, int numTicks,
                                     DateTime startDate) {
        return new AggregatedTickEventIterable(timeseries, tickType, numTicks, startDate, null);
    }

    public static AggregatedTickEventIterable withEnd(final Iterable<DataWithInterval<TickEvent>> timeseries,
                                   final TickImpl.Type tickType, int numTicks,
                                   DateTime endDate) {
        return new AggregatedTickEventIterable(timeseries, tickType, numTicks, null, endDate);
    }

    protected AggregatedTickEventIterable(final Iterable<DataWithInterval<TickEvent>> timeseries, final TickImpl.Type tickType, int numTicks,
                       DateTime startDateTime, DateTime endDateTime) {
        this(timeseries, tickType, numTicks,
                (startDateTime != null) ? startDateTime.getMillis() : 0,
                (endDateTime != null) ? endDateTime.getMillis() : Long.MAX_VALUE,
                null, null);
    }

    private AggregatedTickEventIterable(final Iterable<DataWithInterval<TickEvent>> timeseries, final TickImpl.Type tickType, int numTicks,
                     long start, long end, BitSet additionalFieldIds, TickList.FieldPermissions permissions) {
        this.timeseries = timeseries;
        this.tickType = tickType;
        this.numTicks = numTicks;
        this.start = start;
        this.end = end;
        this.additionalFieldIds = additionalFieldIds;
        this.permissions = permissions;
    }

    @Override
    public Iterator<AggregatedTickEvent> iterator() {
        return new AggregatedTickEventIterator(this.permissions, this.timeseries, this.tickType, this.numTicks,
                this.start, this.end, false);
    }
}
