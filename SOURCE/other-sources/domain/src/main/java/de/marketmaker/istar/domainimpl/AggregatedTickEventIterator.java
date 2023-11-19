package de.marketmaker.istar.domainimpl;

import de.marketmaker.istar.common.featureflags.FeatureFlags;
import de.marketmaker.istar.domain.data.DataWithInterval;
import de.marketmaker.istar.domain.data.TickEvent;
import de.marketmaker.istar.domain.data.TickImpl;
import de.marketmaker.istar.domain.data.TickList;
import org.joda.time.ReadableInterval;

import java.util.Iterator;

public class AggregatedTickEventIterator implements Iterator<AggregatedTickEvent> {

    private final TickList.FieldPermissions permissions;

    private final TickImpl.Type tickType;

    private final int numTicks;

    protected final long start;

    protected final long end;

    private final boolean aggregateOnlyPositivePrices;

    private final Iterator<DataWithInterval<TickEvent>> iterator;

    private AggregatedTickEvent next;

    public boolean hasNext() {
        return this.next.getNumberOfAggregatedTicks() > 0;
    }

    public AggregatedTickEventIterator(TickList.FieldPermissions permissions, Iterable<DataWithInterval<TickEvent>> ticks, TickImpl.Type tickType, int numTicks,
                                       long start, long end, boolean aggregateOnlyPositivePrices) {
        this.permissions = permissions;
        this.tickType = tickType;
        this.numTicks = numTicks;
        this.start = start;
        this.end = end;
        this.aggregateOnlyPositivePrices = aggregateOnlyPositivePrices;
        this.iterator = ticks.iterator();
        this.next = getNext();
    }

    private AggregatedTickEvent getNext() {
        AggregatedTickEvent.Builder builder =
            new AggregatedTickEvent.Builder(this.permissions, this.tickType);

        while (this.iterator.hasNext() && builder.getNumberOfAggregatedTicks() < this.numTicks) {
            final DataWithInterval<TickEvent> dwi = this.iterator.next();
            if (isProcessable(dwi)) {
                builder.add(dwi);
            }
        }
        return builder.build();
    }

    private boolean isProcessable(DataWithInterval<TickEvent> tick) {
        if (!isAcceptable(tick.getInterval())) {
            return false;
        }

        TickEvent tickEvent = tick.getData();
        switch (this.tickType) {
            case TRADE:
            case SYNTHETIC_TRADE:
                return tickEvent.isTrade();
            case BID:
                return tickEvent.isBid();
            case ASK:
                return tickEvent.isAsk();
            default:
                return false;
        }
    }

    private boolean isAcceptable(ReadableInterval interval) {
        long millis = interval.getStartMillis();
        return (millis >= this.start && millis <= this.end);
    }

    public AggregatedTickEvent next() {
        AggregatedTickEvent result = this.next;
        this.next = getNext();
        applyTickEventPermissions(result, this.permissions);
        return result;
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }

    public static AggregatedTickEvent applyTickEventPermissions(AggregatedTickEvent aggregatedTick, TickList.FieldPermissions permissions) {
        if (permissions == null || false || !FeatureFlags.isEnabled(FeatureFlags.Flag.TICK_VOLUME_PERMISSION_FIX)) {
            // TODO
        }

        return aggregatedTick;
    }
}
