/*
 * RawAggregatedTick.java
 *
 * Created on 04.03.2005 09:26:14
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.tick;

import de.marketmaker.istar.common.util.PriceCoder;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class RawAggregatedTick {

    private long open;

    private long high;

    private long low;

    private long close;

    private int numberOfAggregatedTicks;

    private long volume;

    private int time;

    public RawAggregatedTick() {
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RawAggregatedTick)) return false;

        final RawAggregatedTick rawAggregatedTick = (RawAggregatedTick) o;

        if (close != rawAggregatedTick.close) return false;
        if (high != rawAggregatedTick.high) return false;
        if (low != rawAggregatedTick.low) return false;
        if (numberOfAggregatedTicks != rawAggregatedTick.numberOfAggregatedTicks) return false;
        if (open != rawAggregatedTick.open) return false;
        if (time != rawAggregatedTick.time) return false;
        if (volume != rawAggregatedTick.volume) return false;

        return true;
    }

    public int hashCode() {
        int result;
        result = (int) (open ^ (open >>> 32));
        result = 29 * result + (int) (high ^ (high >>> 32));
        result = 29 * result + (int) (low ^ (low >>> 32));
        result = 29 * result + (int) (close ^ (close >>> 32));
        result = 29 * result + numberOfAggregatedTicks;
        result = 29 * result + (int) (volume ^ (volume >>> 32));
        result = 29 * result + time;
        return result;
    }

    public long getClose() {
        return this.close;
    }

    void setClose(long close) {
        this.close = close;
    }

    public long getHigh() {
        return this.high;
    }

    void setHigh(long high) {
        this.high = high;
    }

    void setHighAsMax(long high) {
        this.high = PriceCoder.max(this.high, high);
    }

    public long getLow() {
        return this.low;
    }

    void setLow(long low) {
        this.low = low;
    }

    void setLowAsMin(long low) {
        this.low = PriceCoder.min(this.low, low);
    }

    public int getNumberOfAggregatedTicks() {
        return this.numberOfAggregatedTicks;
    }

    void incNumberOfAggregatedTicks(int amount) {
        this.numberOfAggregatedTicks += amount;
    }

    void setNumberOfAggregatedTicks(int numberOfAggregatedTicks) {
        this.numberOfAggregatedTicks = numberOfAggregatedTicks;
    }

    public long getOpen() {
        return this.open;
    }

    void setOpen(long open) {
        this.open = open;
    }

    public int getTime() {
        return this.time;
    }

    void setTime(int time) {
        this.time = time;
    }

    public long getVolume() {
        return this.volume;
    }

    void setVolume(long volume) {
        this.volume = volume;
    }

    void incVolume(long amount) {
        this.volume += amount;
    }

    private long definedVolume(long v) {
        return (Long.MIN_VALUE == v) ? 0 : v;
    }

    /**
     * Add given price and volume to the current aggregation
     */
    void addTick(long price, long volume) {
        incNumberOfAggregatedTicks(1);
        incVolume(definedVolume(volume));
        setHighAsMax(price);
        setLowAsMin(price);
        setClose(price);
    }

    /**
     * Start new aggregation beginning at startTime, first price and volume as parameters
     */
    public void resetTick(int startTime, long price, long volume) {
        setNumberOfAggregatedTicks(1);
        setVolume(definedVolume(volume));
        setOpen(price);
        setHigh(price);
        setLow(price);
        setClose(price);
        setTime(startTime);
    }

    public String toString() {
        return "RawAggregatedTick[time=" + time
                + ", #ticks=" + numberOfAggregatedTicks
                + ", open=" + open
                + ", high=" + high
                + ", low=" + low
                + ", close=" + close
                + ", volume=" + volume
                + "]";
    }
}
