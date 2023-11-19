/*
 * AggregatedTickData.java
 *
 * Created on 04.03.2005 07:55:42
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.tick;

import de.marketmaker.istar.domain.data.AggregatedTick;

/**
 * Performs aggregation of tick data. To start a new aggregation interval, call either
 * {@link #resetTick} or {@link #resetOhlc}, to aggregate further ticks call
 * {@link #addTick} or {@link #addOhlc}. Finally, use the various getters to retrieve
 * the aggregated tick data.
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class AggregatedTickData {
    private int lastTime = -1;

    private long lastOpen = -1;

    private final RawAggregatedTick tick = new RawAggregatedTick();

    public AggregatedTickData() {
        this.tick.setTime(-1);
        this.tick.setOpen(-1);
    }

    public void setLastTime(int time) {
        this.tick.setTime(time);
    }

    public void setLastOpen(long open) {
        this.tick.setOpen(open);
    }

    public String toString() {
        return "AggregatedTickData["
                + "lastTime=" + this.lastTime
                + ", lastOpen=" + this.lastOpen
                + ", time=" + this.tick.getTime()
                + ", open=" + this.tick.getOpen()
                + ", high=" + this.tick.getHigh()
                + ", low=" + this.tick.getLow()
                + ", close=" + this.tick.getClose()
                + ", volume=" + this.tick.getVolume()
                + ", num=" + this.tick.getNumberOfAggregatedTicks()
                + "]";
    }

    /**
     * Add given price and volume to the current aggregation
     */
    public void addTick(long price, long volume) {
        this.tick.addTick(price, volume);
    }

    /**
     * Start new aggregation beginning at startTime, first price and volume as parameters
     */
    public void resetTick(int startTime, long price, long volume) {
        setLastOnReset();

        this.tick.resetTick(startTime, price, volume);
    }

    private long definedVolume(long v) {
        return (Long.MIN_VALUE == v) ? 0 : v;
    }

    /**
     * Add aggregated tick at to the current aggregation
     */
    public void addOhlc(AggregatedTick at) {
        this.tick.incNumberOfAggregatedTicks(at.getNumberOfAggregatedTicks());
        this.tick.incVolume(definedVolume(at.getVolume()));
        this.tick.setHighAsMax(at.getHigh());
        this.tick.setLowAsMin(at.getLow());
        this.tick.setClose(at.getClose());
    }

    /**
     * Start new aggregation beginning at startTime, first data is from at
     */
    public void resetOhlc(int startTime, AggregatedTick at) {
        setLastOnReset();

        this.tick.setNumberOfAggregatedTicks(at.getNumberOfAggregatedTicks());
        this.tick.setVolume(definedVolume(at.getVolume()));
        this.tick.setOpen(at.getOpen());
        this.tick.setHigh(at.getHigh());
        this.tick.setLow(at.getLow());
        this.tick.setClose(at.getClose());
        this.tick.setTime(startTime);
    }

    /**
     * Start new aggregation beginning at startTime, first data is from at
     */
    public void resetOhlc(int startTime, RawAggregatedTick at) {
        setLastOnReset();

        this.tick.setNumberOfAggregatedTicks(at.getNumberOfAggregatedTicks());
        this.tick.setVolume(definedVolume(at.getVolume()));
        this.tick.setOpen(at.getOpen());
        this.tick.setHigh(at.getHigh());
        this.tick.setLow(at.getLow());
        this.tick.setClose(at.getClose());
        this.tick.setTime(startTime);
    }

    private void setLastOnReset() {
        this.lastTime = this.tick.getTime();
        this.lastOpen = this.tick.getOpen();
    }

    long getLastOpen() {
        return this.lastOpen;
    }

    int getLastTime() {
        return this.lastTime;
    }

    public long getClose() {
        return this.tick.getClose();
    }

    public long getHigh() {
        return this.tick.getHigh();
    }

    public long getLow() {
        return this.tick.getLow();
    }

    public int getNumberOfAggregatedTicks() {
        return this.tick.getNumberOfAggregatedTicks();
    }

    public long getOpen() {
        return this.tick.getOpen();
    }

    public int getTime() {
        return this.tick.getTime();
    }

    public long getVolume() {
        return this.tick.getVolume();
    }
}
