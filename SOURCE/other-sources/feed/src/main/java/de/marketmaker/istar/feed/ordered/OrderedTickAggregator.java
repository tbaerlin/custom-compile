/*
 * RawTickAggregator.java
 *
 * Created on 10.04.13 10:37
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.ordered;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Iterator;

import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.LocalDate;

import de.marketmaker.istar.domain.data.TickType;
import de.marketmaker.istar.feed.FeedUpdateFlags;
import de.marketmaker.istar.feed.mdps.MdpsFeedUtils;
import de.marketmaker.istar.feed.mdps.MdpsPriceUtils;
import de.marketmaker.istar.feed.ordered.tick.TickDecompressor;
import de.marketmaker.istar.feed.tick.AbstractTickRecord;
import de.marketmaker.istar.feed.tick.AggregatedTickEncoder;
import de.marketmaker.istar.feed.tick.RawTick;
import de.marketmaker.istar.feed.tick.RawTickAggregator;
import de.marketmaker.istar.feed.tick.TickRecordImpl;
import de.marketmaker.istar.feed.vwd.VwdFieldDescription;
import de.marketmaker.istar.feed.vwd.VwdFieldOrder;

import static de.marketmaker.istar.common.util.DateUtil.yyyyMmDdToLocalDate;
import static de.marketmaker.istar.common.util.TimeFormatter.formatSecondsInDay;
import static de.marketmaker.istar.feed.mdps.MdpsFeedUtils.*;
import static de.marketmaker.istar.feed.tick.AbstractTickRecord.TickItem.Encoding.TICKZ;
import static java.lang.Integer.compareUnsigned;
import static org.joda.time.DateTimeConstants.SECONDS_PER_DAY;

/**
 * Aggregates daily ticks based on ticks stored by
 * {@link de.marketmaker.istar.feed.ordered.tick.TickBuilder}. Uses stored tick data directly,
 * no intermediate conversion into {@link RawTick} objects, which makes aggregation more than 40%
 * faster than with {@link RawTickAggregator}
 * @author oflege
 */
public class OrderedTickAggregator {

    private static final int MAX_UNSIGNED_INT = -1;

    private static class AggregatedIntradayTick {
        private final int idx;

        private long open;

        private long high;

        private long low;

        private long close;

        private long volume;

        private int openTime;

        private int highTime;

        private int lowTime;

        private int closeTime;

        private int numberOfAggregatedTicks;

        private AggregatedIntradayTick(int idx, int mdpsTime, long price, long volume) {
            this.idx = idx;
            this.open = price;
            this.openTime = mdpsTime;
            this.high = price;
            this.highTime = mdpsTime;
            this.low = price;
            this.lowTime = mdpsTime;
            this.close = price;
            this.closeTime = mdpsTime;
            this.volume = volume;
            this.numberOfAggregatedTicks = 1;
        }

        private AggregatedIntradayTick(int idx, AggregatedIntradayTick t) {
            this.idx = idx;
            this.open = t.open;
            this.openTime = t.openTime;
            this.high = t.high;
            this.highTime = t.highTime;
            this.low = t.low;
            this.lowTime = t.lowTime;
            this.close = t.close;
            this.closeTime = t.closeTime;
            this.volume = t.volume;
            this.numberOfAggregatedTicks = t.numberOfAggregatedTicks;
        }

        void add(AggregatedIntradayTick t) {
            if (compareUnsigned(this.openTime, t.openTime) > 0) {
                this.open = t.open;
                this.openTime = t.openTime;
            }
            if (MdpsPriceUtils.compare(this.high, t.high) < 0) {
                this.high = t.high;
                this.highTime = t.highTime;
            }
            if (MdpsPriceUtils.compare(this.low, t.low) > 0) {
                this.low = t.low;
                this.lowTime = t.lowTime;
            }
            if (compareUnsigned(this.closeTime, t.closeTime) < 0) {
                this.close = t.close;
                this.closeTime = t.closeTime;
            }
            this.volume += t.volume;
            this.numberOfAggregatedTicks += t.numberOfAggregatedTicks;
        }

        public void add(int mdpsTime, long price, long volume) {
            if (compareUnsigned(mdpsTime, this.openTime) < 0) {
                this.open = price;
                this.openTime = mdpsTime;
            }
            else if (compareUnsigned(mdpsTime, this.closeTime) > 0) {
                this.close = price;
                this.closeTime = mdpsTime;
            }
            final int hc = MdpsPriceUtils.compare(this.high, price);
            if (hc < 0 || (hc == 0 && compareUnsigned(mdpsTime, this.highTime) < 0)) {
                this.high = price;
                this.highTime = mdpsTime;
            }
            final int lc = MdpsPriceUtils.compare(this.low, price);
            if (lc > 0 || (lc == 0 && compareUnsigned(this.lowTime, mdpsTime) < 0)) {
                this.low = price;
                this.lowTime = mdpsTime;
            }
            this.volume += volume;
            this.numberOfAggregatedTicks++;
        }

        @Override
        public String toString() {
            return new StringBuilder(80)
                    .append("O: ").append(fmtPrice(this.open)).append("@").append(fmtTime(this.openTime))
                    .append(", H: ").append(fmtPrice(this.high)).append("@").append(fmtTime(this.highTime))
                    .append(", L: ").append(fmtPrice(this.low)).append("@").append(fmtTime(this.lowTime))
                    .append(", C: ").append(fmtPrice(this.close)).append("@").append(fmtTime(this.closeTime))
                    .append(", V: ").append(this.volume).append(", #").append(this.numberOfAggregatedTicks)
                    .toString();
        }

        protected static String fmtPrice(final long p) {
            return MdpsFeedUtils.decodePrice(p).toPlainString();
        }

        protected static String fmtTime(final int t) {
            return formatSecondsInDay(decodeTime(t), decodeTimeMillis(t));
        }
    }

    private static class IntradayAggregation {
        private final int intervalInSeconds;

        private final AggregatedIntradayTick[] ticks;

        private final LocalDate day;

        private IntradayAggregation(LocalDate day, int intervalInSeconds,
                AggregatedIntradayTick[] ticks) {
            this.day = day;
            this.intervalInSeconds = intervalInSeconds;
            this.ticks = ticks;
        }

        public int getMaxTickTime() {
            return (this.ticks.length > 0)
                    ? decodeTime(this.ticks[this.ticks.length - 1].closeTime)
                    : -1;
        }

        public LocalDate getDay() {
            return this.day;
        }

        IntradayAggregation aggregate(int intervalInSeconds) {
            if (intervalInSeconds < this.intervalInSeconds) {
                throw new IllegalArgumentException(intervalInSeconds + " < " + this.intervalInSeconds);
            }
            if (intervalInSeconds == this.intervalInSeconds) {
                return this;
            }
            AggregatedIntradayTick[] tmp = new AggregatedIntradayTick[SECONDS_PER_DAY / intervalInSeconds];
            for (AggregatedIntradayTick t : this.ticks) {
                final int idx = t.idx * this.intervalInSeconds / intervalInSeconds;
                if (tmp[idx] == null) {
                    tmp[idx] = new AggregatedIntradayTick(idx, t);
                }
                else {
                    tmp[idx].add(t);
                }
            }
            return new IntradayAggregation(this.day, intervalInSeconds,
                    Arrays.stream(tmp).filter(t -> t != null).toArray(AggregatedIntradayTick[]::new));
        }
    }

    private final int intervalInSeconds;

    private final int startTime;

    private final int endTime;

    private final int priceFieldOid;

    private final int volumeFieldOid;

    private final int maxOid;

    private final boolean aggregateOnlyPositivePrices;

    private final int requiredFlag;

    private final AggregatedIntradayTick[] ticks;

    /**
     * Aggregate raw intraday ticks
     * @param item contains encoded/compressed ticks
     * @param aggregationInterval all ticks with a time in this interval will be aggregated
     * @param aggregationInSeconds aggregation interval
     * @param priceField field for which open/high/low/close aggregates will be computed
     * @param volumeField field to be used for volume aggregation
     * @param aggregateOnlyPositivePrices iff true, prices <= 0.0 will be ignored.
     * @return aggregated intraday data
     */
    public static IntradayAggregation create(AbstractTickRecord.TickItem item,
            Interval aggregationInterval, int aggregationInSeconds,
            int priceField, int volumeField, boolean aggregateOnlyPositivePrices) {
        final LocalDate day = yyyyMmDdToLocalDate(item.getDate());

        if (!item.getInterval().overlaps(aggregationInterval)) {
            return new IntradayAggregation(day, aggregationInSeconds, new AggregatedIntradayTick[0]);
        }

        final OrderedTickAggregator ota = new OrderedTickAggregator(item,
                aggregationInterval, aggregationInSeconds, priceField, volumeField, aggregateOnlyPositivePrices);
        new TickDecompressor(item).forEach(ota::add);
        return new IntradayAggregation(day, aggregationInSeconds, ota.getAggregations());
    }

    private static int getRequiredFlag(int oid) {
        switch (oid) {
            case VwdFieldOrder.ORDER_ADF_GELD:
                return FeedUpdateFlags.FLAG_WITH_BID;
            case VwdFieldOrder.ORDER_ADF_BRIEF:
                return FeedUpdateFlags.FLAG_WITH_ASK;
            case VwdFieldOrder.ORDER_ADF_BEZAHLT:
                return FeedUpdateFlags.FLAG_WITH_TRADE;
            default:
                return 0;
        }
    }

    private static int getVolumeOid(int volumeField, int priceOid) {
        if (volumeField < 0) {
            return 0;
        }
        if (volumeField > 0) {
            return VwdFieldOrder.getOrder(volumeField);
        }
        switch (priceOid) {
            case VwdFieldOrder.ORDER_ADF_GELD:
                return VwdFieldOrder.ORDER_ADF_GELD_UMSATZ;
            case VwdFieldOrder.ORDER_ADF_BRIEF:
                return VwdFieldOrder.ORDER_ADF_BRIEF_UMSATZ;
            case VwdFieldOrder.ORDER_ADF_BEZAHLT:
                return VwdFieldOrder.ORDER_ADF_BEZAHLT_UMSATZ;
            default:
                return 0;
        }
    }

    private OrderedTickAggregator(AbstractTickRecord.TickItem item, Interval aggregationInterval,
            int intervalInSeconds, int priceField, int volumeField, boolean aggregateOnlyPositivePrices) {
        this.priceFieldOid = VwdFieldOrder.getOrder(priceField);
        this.volumeFieldOid = getVolumeOid(volumeField, this.priceFieldOid);
        this.maxOid = Math.max(this.priceFieldOid, this.volumeFieldOid);
        this.aggregateOnlyPositivePrices = aggregateOnlyPositivePrices;
        this.requiredFlag = getRequiredFlag(this.priceFieldOid);

        final DateTime start = aggregationInterval.getStart();
        this.startTime = item.getInterval().contains(start)
                ? encodeTime(start.getSecondOfDay(), start.getMillisOfSecond()) : 0;

        final DateTime end = aggregationInterval.getEnd();
        this.endTime = item.getInterval().contains(end)
                ? encodeTime(end.getSecondOfDay(), end.getMillisOfSecond()) : MAX_UNSIGNED_INT;

        this.intervalInSeconds = intervalInSeconds;
        this.ticks = new AggregatedIntradayTick[SECONDS_PER_DAY / this.intervalInSeconds];
    }

    private AggregatedIntradayTick[] getAggregations() {
        return Arrays.stream(this.ticks).filter(t -> t != null).toArray(AggregatedIntradayTick[]::new);
    }

    private void add(TickDecompressor.Element element) {
        if (this.requiredFlag != 0 && !element.hasFlag(this.requiredFlag)) {
            return;
        }
        final BufferFieldData fd = element.getData();
        final int mdpsTime = fd.getInt();
        if (isTimeOutsideInterval(mdpsTime)) {
            return;
        }
        long price = Long.MIN_VALUE;
        long volume = 0;
        for (int oid = fd.readNext(); oid > 0 && oid <= this.maxOid; oid = fd.readNext()) {
            if (oid == this.priceFieldOid) {
                final int base = fd.getInt();
                if (this.aggregateOnlyPositivePrices && base <= 0) {
                    return;
                }
                price = encodePrice(base, fd.getByte());
            }
            else if (oid == this.volumeFieldOid) {
                volume = fd.getUnsignedInt();
            }
            else {
                fd.skipCurrent();
            }
        }
        if (price != Long.MIN_VALUE) {
            add(mdpsTime, price, volume);
        }
    }

    private boolean isTimeOutsideInterval(int mdpsTime) {
        return compareUnsigned(mdpsTime, this.startTime) < 0
                || compareUnsigned(mdpsTime, this.endTime) >= 0;
    }

    private void add(int mdpsTime, long price, long volume) {
        final int idx = decodeTime(mdpsTime) / this.intervalInSeconds;
        if (this.ticks[idx] == null) {
            this.ticks[idx] = new AggregatedIntradayTick(idx, mdpsTime, price, volume);
        }
        else {
            this.ticks[idx].add(mdpsTime, price, volume);
        }
    }

    public static void main(String[] args) throws IOException {
        final byte[] bytes = Files.readAllBytes(new File(args.length > 0 ? args[0] : "/Users/oflege/tmp/EURJPY.JFD.SPOT.tdz").toPath());
        final TickRecordImpl tr = new TickRecordImpl();
        tr.add(20150713, bytes, TICKZ);
        final AbstractTickRecord.TickItem item = tr.getItem(20150713);
        AggregatedTickEncoder enc = new AggregatedTickEncoder();
        for (int i = 0; i < 1; i++) {
            final long then = System.currentTimeMillis();
            final IntradayAggregation iagg = create(item, item.getInterval(), 60, VwdFieldDescription.ADF_Bezahlt.id(), VwdFieldDescription.ADF_Bezahlt_Umsatz.id(), true);



            final long now = System.currentTimeMillis();
            System.out.println((now - then) + "ms, #" + iagg.ticks.length + " " + Arrays.stream(iagg.ticks)
                    .filter(a -> a != null).skip(i).findFirst().orElse(null));
            final IntradayAggregation agg600 = iagg.aggregate(600);
            System.out.println(Arrays.stream(agg600.ticks).findFirst().orElse(null));
        }
        System.out.println("==============================");
        for (int i = 0; i < 1; i++) {
            final long then = System.currentTimeMillis();
            final RawTickAggregator rta = new RawTickAggregator(item, null, 60, TickType.TRADE, false);
            final TickDecompressor td = new TickDecompressor(item);
            final Iterator<RawTick> it = td.rawTickIterator(false);
            while (it.hasNext()) {
                rta.process(it.next());
            }
            final long now = System.currentTimeMillis();
            System.out.println((now - then) + "ms, #" + rta.getNumTicks());
        }
    }
}
