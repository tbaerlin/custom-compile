/*
 * OrderedTickData.java
 *
 * Created on 19.11.12 07:37
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.ordered.tick;

import static de.marketmaker.istar.feed.ordered.tick.TickStats.update;
import static de.marketmaker.istar.feed.ordered.tick.TickStats.withIdleBits;

/**
 * Keeps track of where the tick data for a certain symbol is stored. As tick data is stored on a
 * day-by-day basis and it will happen that we need to store data for a new day when the data
 * for the previous day has not been persistet yet, objects
 *
 * @author oflege
 */
public class OrderedTickData {

    /**
     * ticks are stored in chunks and the first 8 bytes in each chunk contain the file or memory
     * address of the previous chunk. Once a new memory chunk is allocated, the previous chunk is
     * submitted for writing. When the write actually happens, it is required that the previous
     * pointer in the chunk points to a file address. This flag helps to avoid a situation in which
     * two write requests for the same symbol end up in the same batch (the 2nd chunk would contain
     * a memory address as previous address and it would therefore cause an error to try to append
     * it to the file).
     */
    private static final int FLAG_WRITE_PENDING = 0x01;

    /**
     * set when storeAddress has been submitted for writing, which will be triggered by
     * {@link de.marketmaker.istar.feed.ordered.tick.TickStoreManager#evictIdle()}.
     * Ensures we don't add storeAddress again if updates start to appear while it waits to be
     * written and a new chunk is allocated.
     */
    private static final int FLAG_EVICTION_PENDING = 0x02;

    /**
     * set when a tick correction is about to be applied. While this flag is set, no data must be
     * submitted for writing to disk, so when the current chunk is full and replaced with an
     * empty one, the new chunk points to the old chunk's address but the old chunk will not be
     * submitted for writing. Once the correction has been applied, this object's storeAddress
     * will be replaced and all the unwritten chunks will be freed as their content is obsolete. 
     */
    private static final int FLAG_CORRECTION_PENDING = 0x04;

    /**
     * yyyyMMdd of today's ticks. The value can never be decreased, once we have received ticks
     * for a certain day all ticks received later for a previous day will be ignored.
     */
    private int date;

    /**
     * Storage address of <tt>date</tt>'s ticks. Will be 0 if no ticks have been received yet,
     * negative (i.e., a file address) if all ticks have been stored in a file or positive if some
     * ticks are still kept in memory (see {@link MemoryTickStore} for how those addresses encode
     * file or memory locations)
     */
    private long storeAddress;

    /**
     * length of <tt>date</tt>'s tick data in bytes; includes both in memory and in file ticks.
     */
    private int length;

    /**
     * Storage address of ticks on the date on day prior to <tt>date</tt>,
     * same format as <tt>storeAddress</tt>.
     */
    private long yesterdaysStoreAddress;

    /**
     * length of yesteday's tick data in bytes; includes both in memory and in file ticks.
     */
    private int yesterdaysLength;

    /**
     * to pick an optimal chunk size for storing ticks, this int keeps the approximate length of the
     * tick data for each of the previous six days on which ticks were actually stored. That length
     * is encoded as the log2 of the smallest power of 2 that is larger than the number of tick bytes.
     * Using log2 ensures that 5 bits are sufficient to store the value for each day.
     */
    private int tickStats = 0;

    /**
     * Stores <tt>FLAG_...</tt> values, acts as minimum size bitset
     */
    private byte flags = 0;

    boolean isEvictionPending() {
        return isFlagSet(FLAG_EVICTION_PENDING);
    }

    void setEvictionPending() {
        setFlag(FLAG_EVICTION_PENDING);
    }

    void unsetEvictionPending() {
        clearFlag(FLAG_EVICTION_PENDING);
    }

    boolean isWritePending() {
        return isFlagSet(FLAG_WRITE_PENDING);
    }

    void setWritePending() {
        setFlag(FLAG_WRITE_PENDING);
    }

    void unsetWritePending() {
        clearFlag(FLAG_WRITE_PENDING);
    }

    boolean isCorrectionPending() {
        return isFlagSet(FLAG_CORRECTION_PENDING);
    }

    void setCorrectionPending() {
        setFlag(FLAG_CORRECTION_PENDING);
    }

    void unsetCorrectionPending() {
        clearFlag(FLAG_CORRECTION_PENDING);
    }

    private boolean isFlagSet(final int flag) {
        return (this.flags & flag) != 0;
    }

    private void setFlag(int b) {
        this.flags |= (byte) b;
    }

    private void clearFlag(int b) {
        this.flags &= ((byte)(0xFF ^ b));
    }

    boolean canBeEvicted(int day) {
        return this.date == day && this.flags == 0 && MemoryTickStore.isMemoryAddress(this.storeAddress);
    }

    public void setStoreAddress(long storeAddress) {
        this.storeAddress = storeAddress;
    }

    void setStoreAddress(int day, long storeAddress) {
        if (day == this.date) {
            this.storeAddress = storeAddress;
        }
        else {
            this.yesterdaysStoreAddress = storeAddress;
        }
    }

    void releaseYesterday() {
        this.yesterdaysStoreAddress = 0L;
        this.yesterdaysLength = 0;
    }

    public long getStoreAddress() {
        return this.storeAddress;
    }

    public long getStoreAddress(int day) {
        return (day == this.date) ? this.storeAddress : this.yesterdaysStoreAddress;
    }

    public int getLength(int day) {
        return (day == this.date) ? this.length : this.yesterdaysLength;
    }

    public int getLength() {
        return this.length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    void setLength(int day, int length) {
        if (this.date == day) {
            this.length = length;
        }
        else {
            this.yesterdaysLength = length;
        }
    }

    void incLength(int increment) {
        this.length += increment;
    }

    void incLength(int day, int increment) {
        if (day == this.date) {
            this.length += increment;
        }
        else {
            this.yesterdaysLength += increment;
        }
    }

    /**
     * @return approximated length needed to store all ticks for this item on a single day, that is
     * basically the maximum length of the past 6 days and the current length. If this object has
     * been marked as being "idle" by calling {@link #setIdleBits(int)}, that length will be
     * divided by 256, 16, or 4, for an idle count of 3,2, or 1, respectively,
     * and the idle count will be decremented by 1.
     */
    int getAvgLength() {
        final int length = TickStats.maxLength(this.tickStats, this.length);
        int idle = TickStats.idleCount(this.tickStats);
        if (idle == 0) {
            return length;
        }
        this.tickStats = TickStats.decIdleBits(this.tickStats);
        return length >> (1 << idle);
    }

    public void setIdleBits(int idleBits) {
        this.tickStats = withIdleBits(this.tickStats, idleBits);
    }

    public void setTickStats(int tickStats) {
        this.tickStats = tickStats;
    }

    public int getTickStats() {
        return tickStats;
    }

    public boolean setDate(int date) {
        if (date == this.date) { // most common case first
            return true;
        }
        if (date < this.date) {
            return false;
        }

        assert (this.yesterdaysStoreAddress == 0L) : "LEAK " + this.yesterdaysStoreAddress;

        this.yesterdaysStoreAddress = this.storeAddress;
        this.yesterdaysLength = this.length;

        updateTickStats();

        this.storeAddress = 0L;
        this.length = 0;
        this.flags = 0;

        this.date = date;
        return true;
    }

    /**
     * reset this object in case of an error; all ticks will be lost
     */
    void reset() {
        this.date = 0;
        this.storeAddress = 0L;
        this.length = 0;
        this.yesterdaysStoreAddress = 0L;
        this.yesterdaysLength = 0;
        this.flags = 0;
    }

    private void updateTickStats() {
        this.tickStats = update(this.tickStats, this.length);
    }

    public int getDate() {
        return date;
    }

    public boolean hasTicksInMemory() {
        return this.storeAddress > 0L || this.yesterdaysStoreAddress > 0L;
    }

    @Override
    public String toString() {
        return "OrderedTickData{" +
                "date=" + this.date +
                ", addr=0x" + Long.toHexString(this.storeAddress) +
                " #" + this.length +
                ", ydAddr=0x" + Long.toHexString(this.yesterdaysStoreAddress) +
                " #" + this.yesterdaysLength +
                ", stats=" + TickStats.toString(this.tickStats) +
                ", flags=0b" + Integer.toBinaryString(this.flags & 0xFF) +
                '}';
    }
}
