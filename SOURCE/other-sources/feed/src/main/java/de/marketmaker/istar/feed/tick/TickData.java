/*
 * TickData.java
 *
 * Created on 07.01.2005 13:06:22
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.tick;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.marketmaker.istar.common.lifecycle.Disposable;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class TickData implements Disposable {
    // static, not every single TickData needs its own logger
    private static final Logger LOGGER = LoggerFactory.getLogger(TickData.class);

    public static final int NULL_OFFSET = -1;

    /**
     * Date of last tick stored in this object, format yyyymmdd,
     * which is stored in {@link #todaysStoreKey} (and its predecessors).
     */
    private int lastDate = -1;

    /**
     * Time of last tick.
     */
    private int lastTime = -1;

    private long todaysStoreKey = 0L;

    private long yesterdaysStoreKey = 0L;

    private int offset = NULL_OFFSET;

    private int lengthInFile = 0;
    
    private int lengthInMemory = 0;

    /**
     * Contains indicators that tell whether today we encoded at least one trade, bid, or ask.
     * Since ticks are decoded on a day by day basis, we have to make sure that the first encoded
     * trade/bid/ask contains a valid price and volume. For any additional ticks, these values
     * will, when missing, be taken from the previous tick.
     */
    private byte flags = 0;

    private static final int FLAG_ANY_TRADE = 0x01;

    private static final int FLAG_ANY_BID = 0x02;

    private static final int FLAG_ANY_ASK = 0x04;

    public void clearFlags() {
        this.flags = (byte) 0;
    }

    public boolean isWithoutTrade() {
        return (this.flags & FLAG_ANY_TRADE) == 0;
    }

    public void ackTrade() {
        this.flags |= (byte) FLAG_ANY_TRADE;
    }

    public boolean isWithoutBid() {
        return (this.flags & FLAG_ANY_BID) == 0;
    }

    public void ackBid() {
        this.flags |= (byte) FLAG_ANY_BID;
    }

    public boolean isWithoutAsk() {
        return (this.flags & FLAG_ANY_ASK) == 0;
    }

    public void ackAsk() {
        this.flags |= (byte) FLAG_ANY_ASK;
    }

    public String toString() {
        return "TickData["
                + "lastTime=" + this.lastTime
                + ", lastDate=" + this.lastDate
                + ", tdChunk=" + this.todaysStoreKey
                + ", ydChunk=" + this.yesterdaysStoreKey
                + ", offset=" + this.offset
                + ", lengthInMemory=" + this.lengthInMemory
                + ", lengthInFile=" + this.lengthInFile
                + "]";
    }

    public final void resetLast() {
        this.lastTime = -1;
        clearFlags();
        resetMyLast();
    }

    protected void resetMyLast() {
        // empty, subclasses may override
    }

    public final int getLastDate() {
        return this.lastDate;
    }

    /**
     * Ack that there is a tick on lastDate
     * @param lastDate tick date
     * @return true iff lastDate is larger than any value used to call this method before for
     * this object, indicates that we start a new day's ticks
     */
    public final boolean setLastDate(int lastDate) {
        if (lastDate <= this.lastDate) {
            return false;
        }
        if (this.yesterdaysStoreKey != 0L) {
            LOGGER.error("<setLastTickDate> LEAK yesterday");
        }
        // first tick for today
        setYesterdaysStoreKey(this.todaysStoreKey);
        setTodaysStoreKey(0L, 0);
        this.lastDate = lastDate;
        return true;
    }

    public final long getTodaysStoreKey() {
        return this.todaysStoreKey;
    }

    public final void setTodaysStoreKey(long todaysStoreKey, int lengthIncrement) {
        if (todaysStoreKey == 0L) {
            resetLast();
            this.todaysStoreKey = 0L;
            this.lengthInMemory = 0;
        }
        else {
            this.todaysStoreKey = todaysStoreKey;
            this.lengthInMemory += lengthIncrement;
        }
    }

    public final long getYesterdaysStoreKey() {
        return this.yesterdaysStoreKey;
    }

    public final void setYesterdaysStoreKey(long yesterdaysStoreKey) {
        this.yesterdaysStoreKey = yesterdaysStoreKey;
    }

    public final int getLastTime() {
        return lastTime;
    }

    public final void setLastTime(int lastTime) {
        this.lastTime = lastTime;
    }

    public final int getLengthInFile() {
        return lengthInFile;
    }

    public final void setLengthInFile(int lengthInFile) {
        this.lengthInFile = lengthInFile;
    }

    public final void addToLengthInFile(int value) {
        this.lengthInFile += value;
    }

    public final int getOffset() {
        return this.offset;
    }

    public final void setOffset(int offset) {
        this.offset = offset;
    }

    public final void dispose() {
        if (this.yesterdaysStoreKey != 0L) {
            throw new IllegalStateException("yesterdaysStoreKey != 0, LEAK");
        }
        if (this.todaysStoreKey != 0L) {
            throw new IllegalStateException("todaysStoreKey != 0, LEAK");
        }
    }
}
