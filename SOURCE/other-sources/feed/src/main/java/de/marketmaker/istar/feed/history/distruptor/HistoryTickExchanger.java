/*
 * HistoryTickProcessor.java
 *
 * Created on 10.04.13 14:41
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.history.distruptor;

import java.nio.ByteBuffer;
import java.util.EnumSet;

import de.marketmaker.istar.domain.data.TickType;
import de.marketmaker.istar.feed.tick.AbstractTickRecord;
import de.marketmaker.istar.feed.tick.RawTick;
import de.marketmaker.istar.feed.tick.RawTickAggregator;
import de.marketmaker.istar.feed.tick.RawTickProcessor;
import de.marketmaker.istar.feed.tick.SyntheticTradeAggregator;

/**
 * @author zzhao
 */
class HistoryTickExchanger implements RawTickProcessor<byte[]> {

    private static final TickType[] TICK_TYPES = TickType.values();

    static TickType fromOrdinal(byte b) {
        if (b >= 0 && b < TICK_TYPES.length) {
            return TICK_TYPES[b];
        }
        throw new IllegalArgumentException("no tick type with ordinal: " + b);
    }

    private final RawTickAggregator[] aggs;

    public HistoryTickExchanger(AbstractTickRecord.TickItem item, EnumSet<TickType> tickTypes,
            boolean aggregateOnlyPositivePrices) {
        this.aggs = new RawTickAggregator[tickTypes.size()];

        int idx = 0;
        for (TickType tickType : TICK_TYPES) {
            if (tickTypes.contains(tickType)) {
                this.aggs[idx++] = getAggregator(tickType, item, aggregateOnlyPositivePrices);
            }
        }
    }

    private RawTickAggregator getAggregator(TickType tickType, AbstractTickRecord.TickItem item,
            boolean aggregateOnlyPositivePrices) {
        switch (tickType) {
            case TRADE:
            case ASK:
            case BID:
                return new RawTickAggregator(item, null, 60, tickType, aggregateOnlyPositivePrices);
            case SYNTHETIC_TRADE:
                return new SyntheticTradeAggregator(item, null, 60, aggregateOnlyPositivePrices);
            default:
                throw new UnsupportedOperationException("no support for: " + tickType);
        }
    }

    @Override
    public boolean process(RawTick rt) {
        for (RawTickAggregator agg : aggs) {
            agg.process(rt);
        }
        return true;
    }

    @Override
    public byte[] getResult() {
        final AbstractTickRecord.TickItem[] items = new AbstractTickRecord.TickItem[this.aggs.length];
        int len = 0;
        for (int i = 0; i < this.aggs.length; i++) {
            items[i] = this.aggs[i].getResult();
            if (items[i].getNumTicks() > 0) {
                len += 1 + 2 + 4 + items[i].getData().length;
            }
        }

        final ByteBuffer bb = ByteBuffer.allocate(len);
        for (int i = 0; i < this.aggs.length; i++) {
            if (items[i].getNumTicks() > 0) {
                bb.put((byte) this.aggs[i].getType().ordinal());
                bb.putShort((short) items[i].getNumTicks());
                bb.putInt(items[i].getData().length);
                bb.put(items[i].getData());
            }
        }

        return bb.array();
    }
}
