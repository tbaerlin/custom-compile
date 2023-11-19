/*
 * AggregatedHistoryTickRecord.java
 *
 * Created on 27.03.2014 10:27
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.history;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.SortedSet;
import java.util.TreeSet;

import de.marketmaker.istar.common.util.DateUtil;
import de.marketmaker.istar.domain.data.AggregatedTick;
import de.marketmaker.istar.domain.data.AggregatedTickRecord;
import de.marketmaker.istar.domain.data.DataWithInterval;
import de.marketmaker.istar.domain.data.TickType;
import de.marketmaker.istar.domain.timeseries.Timeseries;
import de.marketmaker.istar.feed.tick.AggregatedTickData;
import de.marketmaker.istar.feed.tick.AggregatedTickDecoder;
import de.marketmaker.istar.feed.tick.AggregatedTickEncoder;
import de.marketmaker.istar.feed.tick.AggregatedTickRecordImpl;
import de.marketmaker.istar.feed.tick.RawAggregatedTick;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.lang3.mutable.MutableInt;
import org.joda.time.DateTimeConstants;
import org.joda.time.MutableInterval;
import org.joda.time.ReadableDuration;
import org.joda.time.ReadableInterval;

import static org.joda.time.DateTimeConstants.SECONDS_PER_DAY;

/**
 * @author zzhao
 *
 * @see AggregatedTickRecordImpl for the intraday version of this implementation
 */
public class AggregatedHistoryTickRecord extends AggregatedTickRecordImpl {

    private static final long serialVersionUID = 6241752936810957140L;

    public AggregatedHistoryTickRecord(ReadableDuration aggregation, TickType tickType) {
        super(aggregation, tickType);
    }

    @Override
    public AggregatedHistoryTickRecord merge(AggregatedTickRecord that) {
        if (!(that instanceof AggregatedHistoryTickRecord)) {
            throw new IllegalArgumentException("incompatible class type for merger: "
                    + that.getClass().getName());
        }

        if (!getAggregation().equals(that.getAggregation())) {
            throw new IllegalArgumentException("incompatible aggregations for merger: "
                    + that.getAggregation().getMillis() + " <-> " + getAggregation().getMillis());
        }

        if (getTickType() != that.getTickType()) {
            throw new IllegalArgumentException("incompatible tick types for merger: "
                    + getTickType() + " <-> " + that.getTickType());
        }

        final AggregatedHistoryTickRecord result = new AggregatedHistoryTickRecord(getAggregation(), getTickType());

        result.add(getItems());
        result.add(((AggregatedHistoryTickRecord) that).getItems());

        return result;
    }

    // public for tradescreen!
    public void deepMerge(AggregatedHistoryTickRecord tr) {
        if (!getAggregation().equals(tr.getAggregation())) {
            throw new IllegalArgumentException("incompatible aggregations for merger: "
                    + tr.getAggregation().getMillis() + " <-> " + getAggregation().getMillis());
        }

        if (getTickType() != tr.getTickType()) {
            throw new IllegalArgumentException("incompatible tick types: "
                    + getTickType() + " <-> " + tr.getTickType());
        }

        final Iterator<TickItem> itBase = getItems().iterator();
        final Iterator<TickItem> itDelta = tr.getItems().iterator();
        final SortedSet<TickItem> set = new TreeSet<>();

        HistoryUtil.sortMerge(itBase, new HistoryUtil.SortMergeContext<TickItem>() {
            @Override
            public void onItem(TickItem item) {
                set.add(item);
            }

            @Override
            public TickItem merge(TickItem baseItem, TickItem deltaItem) {
                return mergeAggregatedTickItem(baseItem, deltaItem);
            }

            @Override
            public int compare(TickItem baseItem, TickItem deltaItem) {
                return baseItem.compareTo(deltaItem);
            }
        }, itDelta);

        add(set);
    }

    private TickItem mergeAggregatedTickItem(TickItem base, TickItem delta) {
        final Iterator<RawAggregatedTick> itBase = new AggregatedTickDecoder(base).iterator();
        final Iterator<RawAggregatedTick> itDelta = new AggregatedTickDecoder(delta).iterator();

        final ByteBuffer bb = ByteBuffer.allocate(2 * (base.getLength() + delta.getLength()));
        final AggregatedTickEncoder ate = new AggregatedTickEncoder();
        final AggregatedTickData atd = new AggregatedTickData();
        final MutableInt tickNum = new MutableInt(0);

        HistoryUtil.sortMerge(itBase, new HistoryUtil.SortMergeContext<RawAggregatedTick>() {
            @Override
            public void onItem(RawAggregatedTick item) {
                bb.put(encodeAggregatedTick(ate, atd, item));
                tickNum.increment();
            }

            @Override
            public RawAggregatedTick merge(RawAggregatedTick baseItem,
                    RawAggregatedTick deltaItem) {
                return deltaItem;
            }

            @Override
            public int compare(RawAggregatedTick baseItem, RawAggregatedTick deltaItem) {
                return Integer.compare(baseItem.getTime(), deltaItem.getTime());
            }
        }, itDelta);

        return new TickItem(base.getDate(), Arrays.copyOfRange(bb.array(), 0, bb.position()),
                tickNum.intValue(), base.getEncoding());
    }

    private ByteBuffer encodeAggregatedTick(AggregatedTickEncoder ate, AggregatedTickData atd, RawAggregatedTick rat) {
        atd.resetOhlc(rat.getTime(), rat);
        return ate.encode(atd);
    }

    @Override
    public AggregatedHistoryTickRecord aggregate(ReadableDuration duration, ReadableInterval interval) {
        if (!canAggregateTo(duration)) {
            throw new IllegalArgumentException("cannot aggregate from "
                    + getAggregation().getMillis() + " to " + duration.getMillis());
        }

        if (getAggregation().equals(duration)) {
            return merge(this); // creates a copy of this object.
        }

        final int secs = (int) (duration.getMillis() / 1000L);

        final AggregatedHistoryTickRecord result = new AggregatedHistoryTickRecord(duration, getTickType());

        final int fromDate = DateUtil.toYyyyMmDd(interval.getStart());
        final int endDate = DateUtil.toYyyyMmDd(interval.getEnd());
        for (TickItem item : getItems()) {
            if (item.getDate() >= fromDate && item.getDate() <= endDate) {
                final int fromSecs = item.getDate() == fromDate
                        ? interval.getStart().getSecondOfDay() : 0;
                final int toSecs = item.getDate() == endDate
                        ? interval.getEnd().getSecondOfDay() : DateTimeConstants.SECONDS_PER_DAY;
                result.add(aggregate(item, secs, fromSecs, toSecs));
            }
        }

        return result;
    }

    @Override
    public Timeseries<AggregatedTick> getTimeseries(ReadableInterval interval) {
        return new AggregatedTickTimeseries(interval);
    }

    private class AggregatedTickTimeseries implements Timeseries<AggregatedTick>, Serializable {

        private static final long serialVersionUID = -3326606597413381616L;

        private final ReadableInterval interval; // start/stop timestamps for the timeseries

        AggregatedTickTimeseries(ReadableInterval interval) {
            this.interval = interval;
        }

        @Override
        public Iterator<DataWithInterval<AggregatedTick>> iterator() {

            return new Iterator<DataWithInterval<AggregatedTick>>() {

                private final Iterator<TickItem> itemIt = getItems().iterator();

                private final int aggregatedSeconds = (int) (getAggregation().getMillis() / 1000);

                private final AggregatedTickWithInterval twi = new AggregatedTickWithInterval();

                private AggregatedTickWithInterval current;

                private Iterator<RawAggregatedTick> rti = null;

                private int startSec;

                private int endSec;

                private long startMillis;

                private AggregatedTickWithInterval advance() {
                    final TickItem item;
                    if (rti == null) {
                        item = nextItem();
                        if (item == null) {
                            return null;    // out of days
                        }

                        this.startSec = getIntradayStartSec(item);
                        this.endSec = getIntradayEndSec(item);
                        this.startMillis = item.getInterval().getStartMillis();

                        this.twi.interval = new MutableInterval(startMillis, startMillis);
                        // iterate over the containing ticks?
                        rti = new AggregatedTickDecoder(item).iterator();
                    }

                    while (rti.hasNext()) {
                        final RawAggregatedTick rawTick = rti.next();

                        if (rawTick.getTime() < startSec) {
                            continue; // skip this raw tick because it is too early in the day
                        }
                        if (rawTick.getTime() >= endSec) {
                            break;    // raw tick is later, nothing left in this rawTickAggregation
                        }  // try the next one recursion style

                        final long from = startMillis + rawTick.getTime() * 1000;
                        final long to = from + (aggregatedSeconds * 1000);
                        this.twi.interval.setInterval(from, to);

                        this.twi.rawTick = rawTick;
                        return this.twi;
                    }
                    this.rti = null;
                    return advance();
                }

                @Nullable
                private TickItem nextItem() {  // returns day aggregates? (so it seems)
                    while (itemIt.hasNext()) {
                        final TickItem tickItem = itemIt.next();
                        if (tickItem.getInterval().overlaps(interval)) {
                            return tickItem;
                        }
                    }
                    return null;
                }

                @Override
                public boolean hasNext() {
                    if (this.current == null) {
                        this.current = advance();
                    }
                    return current != null;
                }

                @Override
                public DataWithInterval<AggregatedTick> next() {
                    if (this.current == null) {
                        throw new NoSuchElementException();
                    }
                    this.current = null;
                    return this.twi;
                }

                @Override
                public void remove() {
                    throw new UnsupportedOperationException();
                }

            };
        }

        private int getIntradayEndSec(final TickItem item) {
            long intervalEndMs = this.interval.getEndMillis();  // end of the requested interval
            long itemEndMs = item.getInterval().getEndMillis(); // end of the day
            // if intervalEndMs >= item.getMaxTickDateTime(), all ticks that are currently available
            // have been used for the aggregation and we can return 86400; this enables us to
            // provide (yet incomplete) aggregated data for the last interval.
            if (intervalEndMs < itemEndMs) {
                // the requested interval ends before the day ends
                if (item.getMaxTickTime() == 0
                        || intervalEndMs < item.getMaxTickDateTime().getMillis()) {
                    return SECONDS_PER_DAY - (int) ((itemEndMs - intervalEndMs) / 1000);
                }
            }
            return SECONDS_PER_DAY;  // we need the whole day
        }

            // returns offset in sec from interval start
        private int getIntradayStartSec(final TickItem item) {
            long intervalStartMs = this.interval.getStartMillis();  // start of the requested interval
            long itemStartMs = item.getInterval().getStartMillis(); // start of the day
            return (itemStartMs < intervalStartMs)
                    // subtract the day start from the interval start to get the intraday interval start
                    ? (int) ((intervalStartMs - itemStartMs) / 1000)
                    // interval starts before the day, so we need the whole day from the beginning
                    : 0;
        }
    }

    private static class AggregatedTickWithInterval implements DataWithInterval<AggregatedTick>, AggregatedTick {

        private MutableInterval interval;

        private RawAggregatedTick rawTick;

        public AggregatedTick getData() {
            return this;
        }

        public ReadableInterval getInterval() {
            return interval;
        }

        public long getClose() {
            return rawTick.getClose();
        }

        public long getHigh() {
            return rawTick.getHigh();
        }

        public long getLow() {
            return rawTick.getLow();
        }

        public int getNumberOfAggregatedTicks() {
            return rawTick.getNumberOfAggregatedTicks();
        }

        public long getOpen() {
            return rawTick.getOpen();
        }

        public long getVolume() {
            return rawTick.getVolume();
        }

        public String toString() {
            return "AggregatedTickWithInterval[interval=" + interval
                    + ", rawTick=" + rawTick
                    + "]";
        }
    }

}
