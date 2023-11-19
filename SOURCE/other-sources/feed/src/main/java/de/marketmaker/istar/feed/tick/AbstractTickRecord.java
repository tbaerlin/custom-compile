/*
 * AbstractTickRecord.java
 *
 * Created on 03.03.2005 15:18:40
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.tick;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import de.marketmaker.istar.common.util.DateUtil;
import de.marketmaker.istar.feed.ordered.tick.TickDecompressor;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.DurationFieldType;
import org.joda.time.Interval;
import org.joda.time.ReadableDuration;
import org.joda.time.ReadableInstant;
import org.joda.time.ReadableInterval;

import static org.joda.time.DateTimeConstants.MILLIS_PER_DAY;
import static org.joda.time.DateTimeConstants.MILLIS_PER_SECOND;

/**
 * a container for tick items and the duration from first to last tick item,
 * this is the base class for
 * @see de.marketmaker.istar.feed.history.AggregatedHistoryTickRecord
 * @see AggregatedTickRecordImpl and
 * @see TickRecordImpl
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public abstract class AbstractTickRecord implements Serializable {

    protected static final long serialVersionUID = 2205881313295480688L;

    public static boolean isIntradayAggregationDuration(ReadableDuration duration) {
        return duration.getMillis() >= MILLIS_PER_SECOND
                && duration.getMillis() <= (long) MILLIS_PER_DAY
                && ((long) MILLIS_PER_DAY % duration.getMillis()) == 0;
    }

    private final SortedSet<TickItem> items = new TreeSet<>();

    private final ReadableDuration aggregation;

    /**
     * @param aggregation the duration of this record,
     * the time in ms between the first and the last tick in this record or Duration.ZERO
     * if this is a not an aggregated tick
     */
    protected AbstractTickRecord(ReadableDuration aggregation) {
        this.aggregation = aggregation;
    }

    public void add(TickItem item) {
        if (item != null && item.data.length > 0) {
            add(Collections.singleton(item));
        }
    }

    public void add(int date, byte[] data, TickItem.Encoding encoding) {
        add(date, data, -1, encoding);
    }

    public void add(int date, byte[] data, int numTicks, TickItem.Encoding encoding) {
        if (data != null && data.length > 0) {
            add(Collections.singleton(new TickItem(date, data, numTicks, encoding)));
        }
    }

    protected void add(Set<TickItem> items) {
        for (TickItem tickItem : items) {
            if (this.items.contains(tickItem)) {
                // tick items implement equals only based on their date, not on their data.
                // we have to remove the item first to make sure the items item "wins",
                // because a Set's add does not overwrite an existing item.
                this.items.remove(tickItem);
            }
            this.items.add(tickItem);
        }
    }

    public ReadableDuration getAggregation() {
        return this.aggregation;
    }

    public ReadableInterval getInterval() {
        if (this.items.isEmpty()) {
            return null;
        }

        return new Interval(
                this.items.first().getInterval().getStart(),
                this.items.last().getInterval().getEnd());
    }

    public SortedSet<TickItem> getItems() {
        return this.items;
    }

    /**
     * @return true iff target duration is either an intraday duration and aggregation = Duration.ZERO
     * (which seems to indicate that we have raw/single ticks)...
     * ...or the target duration is a multiple of this record's current duration
     */
    boolean isAcceptableAggregationDuration(ReadableDuration duration) {
        return isIntradayAggregationDuration(duration) && ((this.aggregation.getMillis() == 0)
                || (duration.getMillis() % this.aggregation.getMillis() == 0));
    }

    public TickItem getItem(int day) {
        for (TickItem tickItem : items) {
            if (tickItem.yyyymmdd == day) {
                return tickItem;
            }
        }
        return null;
    }


    /**
     * Keeps encoded tick data for a single day.
     */
    public static class TickItem implements Serializable, Comparable<TickItem> {
        protected static final long serialVersionUID = 2L;

        public enum Encoding {
            UNDEFINED, // default for old code, clients just have to know what to do...
            TICK,   // chicago2 tick data, prices are PriceCode encoded
            TICK3,  // chicago3 tick data, zipped with snappy (high speed, low compression)
            PROF,   // old professional trades (chicago3 uses TICK3)
            TICKZ,  // chicago3 tick data, zipped with high compression algorithm
            AGGREGATED,
            AGGREGATED2,
            DUMP3, // chicago3 feed data, zipped with snappy
            DUMPZ  // chicago3 feed data, zipped with high compression algorithm
        }

        private final int yyyymmdd;

        private final byte[] data;

        private final Interval interval;

        // specifies what is stored in data
        private Encoding encoding = Encoding.UNDEFINED;

        private int numTicks = -1;

        private int maxTickTime = 0;

        private int length;

        public String toString() {
            return "TickItem[" + yyyymmdd + ", " + getLength() + "]";
        }

        TickItem(int date, int maxTickTime, byte[] data, int numTicks, Encoding encoding) {
            this(date, data, numTicks, encoding);
            this.maxTickTime = maxTickTime;
        }

        public TickItem(int date, byte[] data, int numTicks, Encoding encoding) {
            this(date, data, data.length, numTicks, encoding);
        }

        public TickItem(int date, byte[] data, int length, int numTicks, Encoding encoding) {
            this.yyyymmdd = date;
            this.data = data;
            this.length = length;
            this.encoding = encoding;

            this.interval = DateUtil.yyyyMmDdToLocalDate(date).toInterval();
            this.numTicks = numTicks;
        }

        public TickItem(int date, byte[] data, Encoding encoding) {
            this(date, data, -1, encoding);
        }

        public <T> T accept(RawTickProcessor<T> rtp) {
            for (Iterator<RawTick> it = createIterator(); it.hasNext(); ) {
                if (!rtp.process(it.next())) {
                    break;
                }
            }
            return rtp.getResult();
        }

        public boolean contains(ReadableInstant ri) {
            return ri != null && this.interval.contains(ri);
        }

        public int getEndSec(final DateTime end) {
            return contains(end)
                    ? end.getSecondOfDay()
                    : DateTimeConstants.SECONDS_PER_DAY;
        }

        public Encoding getEncoding() {
            return encoding;
        }

        /**
         * @return number of ticks in this item or -1 if undefined.
         */
        public int getNumTicks() {
            return this.numTicks;
        }

        public int getDate() {
            return this.yyyymmdd;
        }

        public int getMaxTickTime() {
            return this.maxTickTime;
        }

        public DateTime getMaxTickDateTime() {
            return this.interval.getStart().withFieldAdded(DurationFieldType.seconds(), this.maxTickTime);
        }

        @edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "EI", justification = "client controls this data container")
        public byte[] getData() {
            return this.data;
        }

        public int getLength() {
            return (this.length > 0) ? this.length : this.data.length;
        }

        public Interval getInterval() {
            return this.interval;
        }

        public int compareTo(TickItem cmp) {
            return this.yyyymmdd - cmp.yyyymmdd;
        }

        public boolean equals(Object o) {
            return (this == o) || ((o instanceof TickItem) && (yyyymmdd == ((TickItem) o).yyyymmdd));
        }

        public int hashCode() {
            return yyyymmdd;
        }

        public Iterator<RawTick> createIterator() {
            return createIterator(false);
        }

        public Iterator<RawTick> createIterator(boolean withAdditionalFields) {
            if (this.data == null || this.data.length == 0) {
                return Collections.emptyIterator();
            }
            if (TickDecompressor.canDecompress(this)) {
                return new TickDecompressor(this).rawTickIterator(withAdditionalFields);
            }
//            if (this.encoding == Encoding.TICK) {
            return new TickDecoder(ByteBuffer.wrap(getData(), 0, getLength()));
//            }
//            throw new IllegalStateException(this.encoding.name());
        }
    }
}
