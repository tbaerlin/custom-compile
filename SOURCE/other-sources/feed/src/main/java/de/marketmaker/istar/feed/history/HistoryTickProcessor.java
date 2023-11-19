/*
 * HistoryTickProcessor.java
 *
 * Created on 10.04.13 14:41
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.history;

import java.util.Collection;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;

import de.marketmaker.istar.domain.data.TickType;
import de.marketmaker.istar.feed.tick.AbstractTickRecord;
import de.marketmaker.istar.feed.tick.RawTick;
import de.marketmaker.istar.feed.tick.RawTickAggregator;
import de.marketmaker.istar.feed.tick.RawTickProcessor;
import de.marketmaker.istar.feed.tick.SyntheticTradeAggregator;

/**
 * @author zzhao
 */
public class HistoryTickProcessor
        implements RawTickProcessor<Map<TickType, AbstractTickRecord.TickItem>> {

    private final Map<TickType, RawTickAggregator> map;

    private final Collection<RawTickAggregator> aggs;

    public HistoryTickProcessor(AbstractTickRecord.TickItem item, EnumSet<TickType> tickTypes,
            boolean aggregateOnlyPositivePrices) {
        this.map = new EnumMap<>(TickType.class);
        for (TickType tickType : tickTypes) {
            switch (tickType) {
                case SYNTHETIC_TRADE:
                    this.map.put(tickType, new SyntheticTradeAggregator(item, null, 60,
                            aggregateOnlyPositivePrices));
                    break;
                default:
                    this.map.put(tickType, new RawTickAggregator(item, null, 60, tickType,
                            aggregateOnlyPositivePrices));
                    break;
            }
        }
        this.aggs = this.map.values();
    }

    @Override
    public boolean process(RawTick rt) {
        for (RawTickAggregator agg : aggs) {
            agg.process(rt);
        }
        return true;
    }

    @Override
    public Map<TickType, AbstractTickRecord.TickItem> getResult() {
        final Map<TickType, AbstractTickRecord.TickItem> ret = new EnumMap<>(TickType.class);
        for (Map.Entry<TickType, RawTickAggregator> entry : this.map.entrySet()) {
            final AbstractTickRecord.TickItem item = entry.getValue().getResult();
            if (item.getNumTicks() > 0) {
                ret.put(entry.getKey(), item);
            }
        }
        return ret;
    }
}
