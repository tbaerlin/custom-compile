/*
 * AggregatedTickRecordImpl.java
 *
 * Created on 03.03.2005 15:32:45
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.tick;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.NoSuchElementException;

import de.marketmaker.istar.common.util.DateUtil;
import de.marketmaker.istar.domain.data.AggregatedTick;
import de.marketmaker.istar.domain.data.AggregatedTickRecord;
import de.marketmaker.istar.domain.data.DataWithInterval;
import de.marketmaker.istar.domain.data.TickType;
import de.marketmaker.istar.domain.timeseries.Timeseries;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.Duration;
import org.joda.time.Interval;
import org.joda.time.MutableInterval;
import org.joda.time.ReadableDuration;
import org.joda.time.ReadableInterval;

import static de.marketmaker.istar.feed.tick.AbstractTickRecord.TickItem.Encoding.AGGREGATED;
import static org.joda.time.DateTimeConstants.SECONDS_PER_DAY;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class AggregatedTickRecordImpl extends AbstractTickRecord implements AggregatedTickRecord {

    protected static final long serialVersionUID = 1L;

    private final TickType tickType;

    @Deprecated
    protected DateTime maxTickDateTime;

    public AggregatedTickRecordImpl(ReadableDuration aggregation, TickType tickType) {
        super(aggregation);
        this.tickType = tickType;

        if (getAggregation().equals(Duration.ZERO)) {
            throw new IllegalArgumentException("cannot aggregate with zero duration");
        }
    }

    public void add(int date, byte[] data, int numTicks) {
        add(date, data, numTicks, AGGREGATED);
    }

    @Override
    public TickType getTickType() {
        return this.tickType;
    }

    @Override
    public AggregatedTickRecord merge(AggregatedTickRecord that) {
        if (!(that instanceof AggregatedTickRecordImpl)) {
            throw new IllegalArgumentException("incompatible class type for merger: "
                    + that.getClass().getName());
        }

        if (!getAggregation().equals(that.getAggregation())) {
            throw new IllegalArgumentException("incompatible aggregations for merger: "
                    + that.getAggregation().getMillis() + " <-> " + getAggregation().getMillis());
        }

        if (this.tickType != that.getTickType()) {
            throw new IllegalArgumentException("incompatible tick types for merger: "
                    + this.tickType + " <-> " + that.getTickType());
        }

        final AggregatedTickRecordImpl result = new AggregatedTickRecordImpl(getAggregation(), this.tickType);

        result.add(getItems());
        result.add(((AggregatedTickRecordImpl) that).getItems());

        return result;
    }

    @Override
    public boolean canAggregateTo(ReadableDuration duration) {
        return isAcceptableAggregationDuration(duration);
    }

    @Override
    public AggregatedTickRecord aggregate(ReadableDuration duration, ReadableInterval interval) {
        if (!canAggregateTo(duration)) {
            throw new IllegalArgumentException("cannot aggregate from "
                    + getAggregation().getMillis() + " to " + duration.getMillis());
        }

        if (getAggregation().equals(duration)) {
            return merge(this); // creates a copy of this object.
        }

        final int secs = (int) (duration.getMillis() / 1000L);

        final AggregatedTickRecordImpl result = new AggregatedTickRecordImpl(duration, this.tickType);

        // handle incomplete aggregates at the start and end of the requested interval
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

    protected TickItem aggregate(TickItem item, int secs, int fromSecs, int toSecs) {
        final AggregatedTickEncoder ate = new AggregatedTickEncoder();
        final AggregatedTickData atd = new AggregatedTickData();

        final AggregatedTickWithInterval twi = new AggregatedTickWithInterval();

        final ByteArrayOutputStream baos = new ByteArrayOutputStream(2048);

        int startTime;
        int endTime = -1;

        for (RawAggregatedTick rawTick : new AggregatedTickDecoder(item)) {
            twi.rawTick = rawTick;

            final int tickTime = rawTick.getTime();
            if (tickTime < fromSecs) {
                continue;
            }
            if (tickTime >= toSecs) {
                break;
            }

            if (tickTime > endTime) {
                if (endTime > 0) {
                    addEncodedTick(ate.encode(atd), baos);
                }
                startTime = tickTime - (tickTime % secs);
                endTime = startTime + secs - 1;
                atd.resetOhlc(startTime, twi);
            }
            else {
                atd.addOhlc(twi);
            }
        }

        if (endTime != -1) {
            addEncodedTick(ate.encode(atd), baos);
        }

        return new TickItem(item.getDate(), item.getMaxTickTime(), baos.toByteArray(), -1, AGGREGATED);
    }

    private void addEncodedTick(ByteBuffer bb, ByteArrayOutputStream baos) {
        baos.write(bb.array(), 0, bb.remaining());
    }


    @Override
    public Timeseries<AggregatedTick> getTimeseries(ReadableInterval interval) {
        return new AggregatedTickTimeseries(interval);
    }

    private class AggregatedTickTimeseries implements Timeseries<AggregatedTick>, Serializable {

        protected static final long serialVersionUID = 1L;

        private final ReadableInterval interval;  // start/stop timestamps for the timeseries

        public AggregatedTickTimeseries(ReadableInterval interval) {
            this.interval = interval;
        }

        @Override
        public Iterator<DataWithInterval<AggregatedTick>> iterator() {

            return new Iterator<DataWithInterval<AggregatedTick>>() {

                private final Iterator<TickItem> itemIt = getItems().iterator();

                private final int aggregatedSeconds = (int) (getAggregation().getMillis() / 1000);

                private AggregatedTickWithInterval twi = new AggregatedTickWithInterval();

                private AggregatedTickWithInterval current;

                private Iterator<RawAggregatedTick> rti = null;

                private int startSec;

                private int endSec;

                private long startMillis;

                private long prevTransitionMillis;

                private long transitionOffset;

                private AggregatedTickWithInterval advance() {
                    final TickItem item;
                    if (rti == null) {
                        item = nextItem();
                        if (item == null) {
                            return null;
                        }

                        this.startSec = getIntradayStartSec(item);
                        this.endSec = getIntradayEndSec(item);
                        this.startMillis = item.getInterval().getStartMillis();
                        ackTransition(item.getInterval());

                        this.twi.interval = new MutableInterval(startMillis, startMillis);

                        rti = new AggregatedTickDecoder(item).iterator();
                    }

                    while (rti.hasNext()) {
                        final RawAggregatedTick rawTick = rti.next();

                        if (rawTick.getTime() < startSec) {
                            continue; // to early, skip and try next
                        }
                        if (rawTick.getTime() >= endSec) {
                            break; // past end time, return what we got so far
                        }

                        final long from = getTimeInMillis(rawTick.getTime() * 1000);
                        final long to = getTimeInMillis((rawTick.getTime() + aggregatedSeconds) * 1000);
                        this.twi.interval.setInterval(from, to);

                        this.twi.rawTick = rawTick;
                        return this.twi;
                    }
                    this.rti = null;
                    return advance();
                }

                @Nullable
                private TickItem nextItem() {
                    while (itemIt.hasNext()) {
                        final TickItem tickItem = itemIt.next();
                        if (tickItem.getInterval().overlaps(interval)) {
                            return tickItem;
                        }
                    }
                    return null;
                }

                private long getTimeInMillis(long millisInDay) {
                    final long ms = this.startMillis + millisInDay;
                    if (this.prevTransitionMillis > 0L && ms > this.prevTransitionMillis) {
                        return ms + this.transitionOffset;
                    }
                    return ms;
                }

                private void ackTransition(Interval i) {
                    long nextTransition = i.getStart().getZone().nextTransition(i.getStartMillis());
                    if (nextTransition > i.getStartMillis() && nextTransition < i.getEndMillis()) {
                        this.prevTransitionMillis = i.getEnd().getZone().previousTransition(i.getEndMillis());
                        this.transitionOffset = (new DateTime(prevTransitionMillis).getSecondOfDay()
                                - new DateTime(nextTransition).getSecondOfDay() + 1) * DateTimeConstants.MILLIS_PER_SECOND;
                    }
                    else {
                        this.prevTransitionMillis = 0L;
                    }
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
            long intervalEndMs = this.interval.getEndMillis();
            long itemEndMs = item.getInterval().getEndMillis();
            // if intervalEndMs >= item.getMaxTickDateTime(), all ticks that are currently available
            // have been used for the aggregation and we can return 86400; this enables us to
            // provide (yet incomplete) aggregated data for the last interval.
            if (intervalEndMs < itemEndMs) {
                if (item.getMaxTickTime() == 0 || intervalEndMs < item.getMaxTickDateTime().getMillis()) {
                    return SECONDS_PER_DAY - (int) ((itemEndMs - intervalEndMs) / 1000);
                }
            }
            return SECONDS_PER_DAY;
        }

        // returns offset in sec from start of day
        private int getIntradayStartSec(final TickItem item) {
            long intervalStartMs = this.interval.getStartMillis();
            long itemStartMs = item.getInterval().getStartMillis();
            return (itemStartMs < intervalStartMs)
                    ? (int) ((intervalStartMs - itemStartMs) / 1000)
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
