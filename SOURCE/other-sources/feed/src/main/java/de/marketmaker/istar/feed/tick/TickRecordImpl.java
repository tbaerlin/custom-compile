/*
 * TickRecordImpl.java
 *
 * Created on 02.03.2005 17:01:19
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.tick;

import java.io.Serializable;
import java.util.Collections;
import java.util.Iterator;

import de.marketmaker.istar.domain.data.AggregatedTickRecord;
import de.marketmaker.istar.domain.data.DataWithInterval;
import de.marketmaker.istar.domain.data.SnapRecord;
import de.marketmaker.istar.domain.data.Tick;
import de.marketmaker.istar.domain.data.TickEvent;
import de.marketmaker.istar.domain.data.TickRecord;
import de.marketmaker.istar.domain.data.TickType;
import de.marketmaker.istar.domain.timeseries.Timeseries;
import de.marketmaker.istar.feed.snap.SnapRecordUtils;
import de.marketmaker.istar.feed.vwd.VwdFieldDescription;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Interval;
import org.joda.time.ReadableDuration;
import org.joda.time.ReadableInterval;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class TickRecordImpl extends AbstractTickRecord implements TickRecord {
    protected static final long serialVersionUID = 1L;

    private static final Timeseries EMPTY_TIMESERIES = new Timeseries() {

        public Iterator<DataWithInterval<?>> iterator() {
            return Collections.emptyIterator();
        }

        @Override
        public String toString() {
            return "EmptyTimeseries";
        }
    };

    static class Last implements Serializable {
        protected static final long serialVersionUID = 1L;

        private final int tickId;

        private final long trade;

        private final int tradeVolume;

        private final long bid;

        private final int bidVolume;

        private final long ask;

        private final int askVolume;

        Last(int tickId, long trade, int tradeVolume, long bid, int bidVolume, long ask,
                int askVolume) {
            this.tickId = tickId;
            this.trade = trade;
            this.tradeVolume = tradeVolume;
            this.bid = bid;
            this.bidVolume = bidVolume;
            this.ask = ask;
            this.askVolume = askVolume;
        }

        boolean matches(RawTick t) {
            if (t.isTrade() && t.getPrice() == this.trade && t.getVolume() == this.tradeVolume) {
                return true;
            }
            if (t.isAsk() && t.getAskPrice() == this.ask && t.getAskVolume() == this.askVolume) {
                return true;
            }
            if (t.isBid() && t.getBidPrice() == this.bid && t.getBidVolume() == this.bidVolume) {
                return true;
            }
            return false;
        }
    }

    /**
     * The DateTime of the lastTick that is recognized to be valid. Important only when
     * processing a delayed series of ticks since the contained TickItem objects
     * represent realtime ticks and have to be shortened if necessary;
     * value is null for a realtime TickRecord
     */
    private DateTime lastTickDateTime;

    private Last last = null;

    private boolean aggregateOnlyPositivePrices = true;

    public TickRecordImpl() {
        super(Duration.ZERO);
    }

    public void setAggregateOnlyPositivePrices(boolean aggregateOnlyPositivePrices) {
        this.aggregateOnlyPositivePrices = aggregateOnlyPositivePrices;
    }

    @Override
    public AggregatedTickRecord aggregate(ReadableDuration duration, TickType type) {
        if (type == TickType.SUSPEND_END || type == TickType.SUSPEND_START) {
            throw new IllegalArgumentException("Cannot aggregate ticks of type " + type);
        }

        if (!isAcceptableAggregationDuration(duration)) {
            throw new IllegalArgumentException("Cannot aggregate with duration " + duration.getMillis());
        }

        final AggregatedTickRecordImpl result = new AggregatedTickRecordImpl(duration, type);
        final int secs = (int) (duration.getMillis() / 1000L);

        for (TickItem item : getItems()) {
            result.add(aggregate(item, secs, type));
        }

        return result;
    }

    private TickItem aggregate(TickItem item, int secs, TickType type) {
        final RawTickAggregator rta = createAggregator(item, secs, type);
        return item.accept(rta);
    }

    private RawTickAggregator createAggregator(TickItem item, int secs, TickType type) {
        if (type == TickType.SYNTHETIC_TRADE) {
            return new SyntheticTradeAggregator(item, this.lastTickDateTime,
                    secs, this.aggregateOnlyPositivePrices);
        }
        return new RawTickAggregator(item, this.lastTickDateTime,
                secs, type, this.aggregateOnlyPositivePrices);
    }

    @Override
    public TickRecord merge(TickRecord that) {
        if (that == null) {
            // interface says 'create a new TickRecord'
            final TickRecordImpl result = new TickRecordImpl();
            result.add(getItems());
            return result;
        }
        if (!(that instanceof TickRecordImpl)) {
            throw new IllegalArgumentException("incompatible class type for merger: "
                    + that.getClass().getName());
        }

        final TickRecordImpl result = new TickRecordImpl();

        result.add(getItems());
        result.add(((TickRecordImpl) that).getItems());

        return result;
    }

    private <K> Timeseries<K> empty() {
        //noinspection unchecked
        return (Timeseries<K>) EMPTY_TIMESERIES;
    }

    @Override
    public Timeseries<Tick> getTimeseries(ReadableInterval interval, TickType type) {
        if (startsAfterLastTick(interval)) {
            return empty();
        }
        return new TickTimeseries(this, type, interval);
    }

    @Override
    public Timeseries<TickEvent> getTimeseries(ReadableInterval interval) {
        return doGetTimeseries(interval, false);
    }

    @Override
    public Timeseries<TickEvent> getTimeseriesWithAdditionalFields(ReadableInterval interval) {
        return doGetTimeseries(interval, true);
    }

    private Timeseries<TickEvent> doGetTimeseries(ReadableInterval interval, boolean withAdditionalFields) {
        if (startsAfterLastTick(interval)) {
            return empty();
        }
        return new TickEventTimeseries(this, interval, withAdditionalFields);
    }

    private boolean startsAfterLastTick(ReadableInterval interval) {
        return this.lastTickDateTime != null && interval.getStart().isAfter(this.lastTickDateTime);
    }

    @Override
    public boolean isDelayed() {
        return this.last != null;
    }

    @Override
    public int tickSize() {
        int n = 0;
        for (TickItem item : getItems()) {
            if (item.getData() != null) {
                n += item.getData().length;
            }
        }
        return n;
    }

    public void unsetLast() {
        this.lastTickDateTime = null;
        this.last = null;
    }

    public void setLast(DateTime lastTickDateTime, SnapRecord snap) {
        this.lastTickDateTime = lastTickDateTime;
        this.last = createLastFromSnap(snap);
    }

    private Last createLastFromSnap(SnapRecord snap) {
        return new Last(
                SnapRecordUtils.getInt(snap, VwdFieldDescription.ADF_Tick_ID.id()),
                SnapRecordUtils.getLong(snap, VwdFieldDescription.ADF_Bezahlt.id()),
                SnapRecordUtils.getInt(snap, VwdFieldDescription.ADF_Bezahlt_Umsatz.id()),
                SnapRecordUtils.getLong(snap, VwdFieldDescription.ADF_Geld.id()),
                SnapRecordUtils.getInt(snap, VwdFieldDescription.ADF_Geld_Umsatz.id()),
                SnapRecordUtils.getLong(snap, VwdFieldDescription.ADF_Brief.id()),
                SnapRecordUtils.getInt(snap, VwdFieldDescription.ADF_Brief_Umsatz.id())
        );
    }

    public DateTime getLastTickDateTime() {
        return this.lastTickDateTime;
    }

    public String toString() {
        final StringBuilder stb = new StringBuilder();
        stb.append("TickRecordImpl");
        char sep = '[';
        for (TickItem item : getItems()) {
            stb.append(sep).append(' ').append(item.getDate()).append(": ").append(item.getData().length).append(" bytes");
            sep = ',';
        }
        if (lastTickDateTime != null) {
            stb.append(", lastTickDateTime=").append(this.lastTickDateTime);
        }

        stb.append("]");

        return stb.toString();
    }

    ReadableInterval adaptInterval(ReadableInterval ri) {
        if (this.lastTickDateTime == null || !ri.getEnd().isAfter(this.lastTickDateTime)) {
            return ri;
        }
        return new Interval(ri.getStart(), this.lastTickDateTime);
    }

    boolean matchesLast(RawTick t) {
        return this.last == null || this.last.matches(t);
    }

    boolean isLast(RawTick rt, int endSec) {
        return (endSec == rt.getTime() && matchesLast(rt)) || rt.getTime() > endSec;
    }
}
