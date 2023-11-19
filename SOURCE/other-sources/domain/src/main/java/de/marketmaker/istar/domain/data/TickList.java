/*
 * TickList.java
 *
 * Created on 30.11.2009 12:56:35
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domain.data;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.joda.time.DateTime;
import org.joda.time.ReadableInterval;

import de.marketmaker.istar.common.featureflags.FeatureFlags;
import de.marketmaker.istar.common.util.PriceCoder;

/**
 * Ticks that will be put into the model; the only important aspect is that we return a
 * new iterator each time; putting an iterator into the model would cause problems if the
 * model elements were cached and reused for another block that would then just see an
 * exhausted iterator. Since stringtemplate makes exclusive use of the iterator method,
 * we can use dummy implementations for size() and get(int i)
 */
public class TickList extends AbstractList<TickImpl> {

    /**
     * permissions for additional fields; the main fields (i.e., ADF_(Geld|Brief|Bezahlt)) have to be
     * allowed for the respective tick types.
     */
    public static final class FieldPermissions {
        final boolean trade;

        final boolean bid;

        final boolean ask;

        final boolean bidVolume;

        final boolean askVolume;

        final boolean tradeVolume;

        final boolean tradeSupplement;

        final boolean tradeIdentifier;

        private FieldPermissions(boolean trade, boolean bid, boolean ask,
                boolean bidVolume, boolean askVolume, boolean tradeVolume,
                boolean tradeSupplement, boolean tradeIdentifier) {
            this.trade = trade;
            this.bid = bid;
            this.ask = ask;
            this.bidVolume = bidVolume;
            this.askVolume = askVolume;
            this.tradeVolume = tradeVolume;
            this.tradeSupplement = tradeSupplement;
            this.tradeIdentifier = tradeIdentifier;
        }

        public static FieldPermissions create(boolean trade, boolean bid, boolean ask,
                boolean bidVolume, boolean askVolume, boolean tradeVolume,
                boolean tradeSupplement, boolean tradeIdentifier) {
            if (trade && bid && ask && bidVolume && askVolume && tradeVolume && tradeSupplement && tradeIdentifier) {
                return null;
            }
            return new FieldPermissions(trade, bid, ask, bidVolume, askVolume, tradeVolume, tradeSupplement, tradeIdentifier);
        }

        public boolean isTrade() {
            return trade;
        }

        public boolean isBid() {
            return bid;
        }

        public boolean isAsk() {
            return ask;
        }

        public boolean isBidVolume() {
            return bidVolume;
        }

        public boolean isAskVolume() {
            return askVolume;
        }

        public boolean isTradeVolume() {
            return tradeVolume;
        }

        public boolean isTradeSupplement() {
            return tradeSupplement;
        }

        public boolean isTradeIdentifier() {
            return tradeIdentifier;
        }
    }

    protected final Iterable<DataWithInterval<TickEvent>> timeseries;

    protected final TickImpl.Type tickType;

    protected final long start;

    protected final long end;

    protected final BitSet additionalFieldIds;

    /**
     * allows to filter certain fields wrt the user's permission
     */
    private final FieldPermissions permissions;

    /**
     * Creates a tick list that iterates over those ticks in timeseries that are of tickType's type
     * and whose interval does not start before startDate
     * @param timeseries source
     * @param tickType only ticks of this type will be iterated over
     * @param startDate only ticks that start on or after this timestamp will be iterated over;
     * use null to start iterating with the first tick in timeseries of appropriate type
     * @return new TickList
     */
    public static TickList withStart(final Iterable<DataWithInterval<TickEvent>> timeseries,
            final TickImpl.Type tickType,
            DateTime startDate) {
        if (tickType == TickImpl.Type.SYNTHETIC_TRADE) {
            return new SyntheticTradeList(timeseries, startDate, null);
        }
        else {
            return new TickList(timeseries, tickType, startDate, null);
        }
    }

    /**
     * Creates a tick list that iterates over those ticks in timeseries that are of tickType's type
     * and whose interval does not start after endDate
     * @param timeseries source
     * @param tickType only ticks of this type will be iterated over
     * @param endDate only ticks up to (incl.) this timestamp will be iterated over; use null to
     * specify an open end list
     * @return new TickList
     */
    public static TickList withEnd(final Iterable<DataWithInterval<TickEvent>> timeseries,
            final TickImpl.Type tickType,
            DateTime endDate) {
        if (tickType == TickImpl.Type.SYNTHETIC_TRADE) {
            return new SyntheticTradeList(timeseries, null, endDate);
        }
        else {
            return new TickList(timeseries, tickType, null, endDate);
        }
    }

    protected TickList(final Iterable<DataWithInterval<TickEvent>> timeseries, final TickImpl.Type tickType,
            DateTime startDateTime, DateTime endDateTime) {
        this(timeseries, tickType,
                (startDateTime != null) ? startDateTime.getMillis() : 0,
                (endDateTime != null) ? endDateTime.getMillis() : Long.MAX_VALUE,
                null, null);
    }

    private TickList(final Iterable<DataWithInterval<TickEvent>> timeseries, final TickImpl.Type tickType,
            long start, long end, BitSet additionalFieldIds, FieldPermissions permissions) {
        this.timeseries = timeseries;
        this.tickType = tickType;
        this.start = start;
        this.end = end;
        this.additionalFieldIds = additionalFieldIds;
        this.permissions = permissions;
    }

    public TickList withAdditionalFields(BitSet additionalFieldIds) {
        if (Objects.equals(this.additionalFieldIds, additionalFieldIds)) {
            return this;
        }
        return new TickList(this.timeseries, this.tickType, this.start, this.end, additionalFieldIds, this.permissions);
    }

    public TickList withPermissions(FieldPermissions permissions) {
        if (Objects.equals(this.permissions, permissions)) {
            return this;
        }
        return new TickList(this.timeseries, this.tickType, this.start, this.end, additionalFieldIds, permissions);
    }

    public TickImpl get(int index) {
        throw new UnsupportedOperationException();
    }

    public int size() {
        throw new UnsupportedOperationException();
    }

    private TickImpl toTick(TickEvent t, DateTime dt) {
        if (this.tickType == TickImpl.Type.BID_ASK_TRADE) {
            return toBidAskTrade(t, dt);
        }
        if (this.tickType == TickImpl.Type.BID_ASK) {
            return toBidAsk(t, dt);
        }
        final List<SnapField> fields = getFields(t);
        switch (tickType) {
            case TRADE:
                if (t.isTrade()) {
                    final boolean tradeVolume = (permissions == null || permissions.tradeVolume);
                    final boolean tradeSupp = (permissions == null || permissions.tradeSupplement);
                    final boolean tradeId = (permissions == null || permissions.tradeIdentifier);

                    return new TickImpl(dt, PriceCoder.decode(t.getPrice()),
                            tradeVolume ? t.getVolume() : Long.MIN_VALUE,
                            tradeSupp ? t.getSupplement() : null,
                            tradeId ? t.getTradeIdentifier() : null, TickImpl.Type.TRADE, fields);
                }
                break;
            case BID:
                if (t.isBid()) {
                    final boolean bidVolume = (permissions == null || permissions.bidVolume);
                    return new TickImpl(dt, PriceCoder.decode(t.getBidPrice()),
                            bidVolume ? t.getBidVolume() : Long.MIN_VALUE,
                            null, null, TickImpl.Type.BID, fields);
                }
                break;
            case ASK:
                if (t.isAsk()) {
                    final boolean askVolume = (permissions == null || permissions.askVolume);
                    return new TickImpl(dt, PriceCoder.decode(t.getAskPrice()),
                            askVolume ? t.getAskVolume() : Long.MIN_VALUE,
                            null, null, TickImpl.Type.ASK, fields);
                }
                break;
            case ADDITIONAL_FIELDS:
                return new TickImpl(dt, fields);
        }
        if (fields != null) {
            return new TickImpl(dt, fields);
        }
        throw new IllegalStateException(this.tickType + ": " + t);
    }

    private TickImpl toBidAskTrade(TickEvent t, DateTime dt) {
        // ask/bid/trade w/o a price make no sense, so require those at least
        final boolean ask = t.isAsk() && (permissions == null || permissions.ask);
        final boolean bid = t.isBid() && (permissions == null || permissions.bid);
        final boolean trade = t.isTrade() && (permissions == null || permissions.trade);

        final boolean tradeVolume = trade && (permissions == null || permissions.tradeVolume);
        final boolean tradeSupp = trade && (permissions == null || permissions.tradeSupplement);
        final boolean tradeId = trade && (permissions == null || permissions.tradeIdentifier);
        final boolean askVolume = ask && (permissions == null || permissions.askVolume);
        final boolean bidVolume = bid && (permissions == null || permissions.bidVolume);

        return new BidAskTradeTickImpl(dt,
                trade ? PriceCoder.decode(t.getPrice()) : null,
                tradeVolume ? t.getVolume() : Long.MIN_VALUE,
                tradeSupp ? t.getSupplement() : null,
                tradeId ? t.getTradeIdentifier() : null,
                this.tickType,
                bid ? PriceCoder.decode(t.getBidPrice()) : null,
                bidVolume ? t.getBidVolume() : Long.MIN_VALUE,
                ask ? PriceCoder.decode(t.getAskPrice()) : null,
                askVolume ? t.getAskVolume() : Long.MIN_VALUE,
                getFields(t));
    }

    private TickImpl toBidAsk(TickEvent t, DateTime dt) {
        // ask/bid/trade w/o a price make no sense, so require those at least
        final boolean ask = t.isAsk() && (permissions == null || permissions.ask);
        final boolean bid = t.isBid() && (permissions == null || permissions.bid);
        final boolean askVolume = ask && (permissions == null || permissions.askVolume);
        final boolean bidVolume = bid && (permissions == null || permissions.bidVolume);

        return new BidAskTradeTickImpl(dt,
                null, Long.MIN_VALUE, null, null,
                this.tickType,
                bid ? PriceCoder.decode(t.getBidPrice()) : null,
                bidVolume ? t.getBidVolume() : Long.MIN_VALUE,
                ask ? PriceCoder.decode(t.getAskPrice()) : null,
                askVolume ? t.getAskVolume() : Long.MIN_VALUE,
                getFields(t));
    }

    private List<SnapField> getFields(TickEvent t) {
        if (this.additionalFieldIds == null) {
            return null;
        }
        final List<SnapField> additionalFields = t.getAdditionalFields();
        if (additionalFields == null) {
            return null;
        }
        List<SnapField> result = null;
        for (SnapField additionalField : additionalFields) {
            if (this.additionalFieldIds.get(additionalField.getId())) {
                if (result == null) {
                    result = new ArrayList<>(additionalFields.size());
                }
                result.add(additionalField);
            }
        }
        return result;
    }

    private boolean isAcceptable(DataWithInterval<TickEvent> dwi) {
        return TickImpl.hasTickType(tickType, permissions, dwi.getData(), additionalFieldIds)
                && isAcceptable(dwi.getInterval());
    }

    protected boolean isAcceptable(ReadableInterval ri) {
        long ms = ri.getStartMillis();
        return (ms >= this.start && ms <= this.end);
    }

    /**
     * Remove trade volume values from a list of AggregatedTickImpl objects if no permission to see it.
     * @param aggregatedTicks List of objects as base values
     * @param permissions Granted permissions
     * @return Identical list of objects if permission granted, List of new objects without volume else
     */
    public static List<AggregatedTickImpl> applyPermissions(List<AggregatedTickImpl> aggregatedTicks, FieldPermissions permissions) {
        if (permissions == null || permissions.tradeVolume || !FeatureFlags.isEnabled(FeatureFlags.Flag.TICK_VOLUME_PERMISSION_FIX)) {
            return aggregatedTicks;
        }
        return aggregatedTicks.stream()
                .map(aggregatedTick -> applyPermissions(aggregatedTick, permissions))
                .collect(Collectors.toList());
    }

    /**
     * Remove trade volume value from AggregatedTickImpl object if no permission to see it.
     * This will return the same object unchanged if permission is granted or a new object without
     * volume if permission is denied.
     * @param aggregatedTick Object as base value
     * @param permissions Granted permissions
     * @return Identical object if permission granted, new object without volume else
     */
    public static AggregatedTickImpl applyPermissions(AggregatedTickImpl aggregatedTick, FieldPermissions permissions) {
        if (permissions == null || permissions.tradeVolume || !FeatureFlags.isEnabled(FeatureFlags.Flag.TICK_VOLUME_PERMISSION_FIX)) {
            return aggregatedTick;
        }
        return new AggregatedTickImpl(
                aggregatedTick.getInterval(),
                aggregatedTick.getOpen(),
                aggregatedTick.getHigh(),
                aggregatedTick.getLow(),
                aggregatedTick.getClose(),
                null,
                aggregatedTick.getNumberOfAggregatedTicks(),
                aggregatedTick.getType());
    }


    @Override
    public Iterator<TickImpl> iterator() {
        return new Iterator<TickImpl>() {
            private final Iterator<DataWithInterval<TickEvent>> iterator = timeseries.iterator();

            private DataWithInterval<TickEvent> next = getNext();

            public boolean hasNext() {
                return this.next != null;
            }

            public TickImpl next() {
                final TickEvent t = this.next.getData();
                final TickImpl result = toTick(t, this.next.getInterval().getStart());

                this.next = getNext();

                return result;
            }

            private DataWithInterval<TickEvent> getNext() {
                while (this.iterator.hasNext()) {
                    final DataWithInterval<TickEvent> dwi = this.iterator.next();
                    if (isAcceptable(dwi)) {
                        return dwi;
                    }
                }
                return null;
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }
}
