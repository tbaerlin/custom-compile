/*
 * DayTick.java
 *
 * Created on 23.07.12 13:30
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.history;

import java.util.EnumMap;

import de.marketmaker.istar.common.util.ByteString;
import de.marketmaker.istar.domain.data.TickType;
import de.marketmaker.istar.feed.tick.AbstractTickRecord;

/**
 * @author zzhao
 */
class DayTick {

    private ByteString symbol;

    private EnumMap<TickType, AbstractTickRecord.TickItem> tickItems =
            new EnumMap<>(TickType.class);

    DayTick reset() {
        this.symbol = null;
        this.tickItems.clear();
        return this;
    }

    boolean isEmpty() {
        return this.tickItems.isEmpty();
    }

    DayTick withSymbol(ByteString symbol) {
        this.symbol = symbol;
        return this;
    }

    DayTick withTickItem(TickType tickType, AbstractTickRecord.TickItem tickItem) {
        this.tickItems.put(tickType, tickItem);
        return this;
    }

    public ByteString getSymbol() {
        return symbol;
    }

    public AbstractTickRecord.TickItem getTickItem(TickType tickType) {
        return this.tickItems.get(tickType);
    }
}
