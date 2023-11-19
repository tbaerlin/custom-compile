/*
 * TickWithInterval.java
 *
 * Created on 08.01.13 12:09
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.tick;

import org.joda.time.Instant;
import org.joda.time.ReadableInterval;

import de.marketmaker.istar.domain.data.DataWithInterval;
import de.marketmaker.istar.domain.data.Tick;
import de.marketmaker.istar.domain.data.TickProperties;
import de.marketmaker.istar.domain.data.TickType;

/**
 * Combines Interval and TickEvent, delegates to both
 */
class TickWithInterval implements DataWithInterval<Tick>, Tick {
    ReadableInterval interval;

    RawTick rawTick;

    private final TickType type;

    public TickWithInterval(TickType type) {
        this.type = type;
    }

    public Tick getData() {
        return this;
    }

    public ReadableInterval getInterval() {
        return interval;
    }

    public Instant getInstant() {
        return new Instant(this.interval.getStartMillis());
    }

    public int getSecondsInDay() {
        return this.rawTick.getTime();
    }

    public long getPrice() {
        switch (type) {
            case TRADE:
                return this.rawTick.getPrice();
            case BID:
                return this.rawTick.getBidPrice();
            case ASK:
                return this.rawTick.getAskPrice();
            case SYNTHETIC_TRADE:
                return this.rawTick.getPrice();
            default:
                return Long.MIN_VALUE;
        }
    }

    public long getVolume() {
        switch (type) {
            case TRADE:
                return this.rawTick.getVolume();
            case BID:
                return this.rawTick.getBidVolume();
            case ASK:
                return this.rawTick.getAskVolume();
            case SYNTHETIC_TRADE:
                return this.rawTick.getVolume();
            default:
                return Long.MIN_VALUE;
        }
    }

    public String getSupplement() {
        return (this.type == TickType.TRADE) ? this.rawTick.getSupplement() : null;
    }

    public String getTradeIdentifier() {
        return (this.type == TickType.TRADE) ? this.rawTick.getTradeIdentifier() : null;
    }

    public TickType getType() {
        return this.type;
    }

    public TickProperties getProperties() {
        return this.rawTick;
    }

    public String toString() {
        return "TickWithInterval[" + this.rawTick + "," + this.interval + "]";
    }
}
