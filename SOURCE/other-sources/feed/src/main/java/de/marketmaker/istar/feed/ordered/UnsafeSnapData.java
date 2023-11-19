/*
 * OrderedSnapData.java
 *
 * Created on 30.08.12 14:54
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.ordered;

import de.marketmaker.istar.domain.data.SnapRecord;
import de.marketmaker.istar.feed.snap.IndexAndOffset;

/**
 * @author oflege
 */
final class UnsafeSnapData implements OrderedSnapData {
    private long address;

    private int size;

    private int lastUpdateTimestamp;

    UnsafeFieldData reset(UnsafeFieldData data) {
        return data.reset(this.address, this.size);
    }

    @Override
    public void init(OrderedSnapRecord src) {
        this.lastUpdateTimestamp = src.getLastUpdateTimestamp();
        init(null, src.getData());
    }

    @Override
    public int getLastUpdateTimestamp() {
        return lastUpdateTimestamp;
    }

    @Override
    public void setLastUpdateTimestamp(int lastUpdateTimestamp) {
        this.lastUpdateTimestamp = lastUpdateTimestamp;
    }

    @Override
    public byte[] getData(boolean copy) {
        if (this.address == 0) {
            return null;
        }
        return UnsafeFieldData.getAsByteArray(this.address, this.size);
    }

    @Override
    public IndexAndOffset getIndexAndOffset() {
        return null;
    }

    @Override
    public boolean isInitialized() {
        return this.address != 0;
    }

    @Override
    public void init(IndexAndOffset indexAndOffset, byte[] data) {
        this.address = UnsafeFieldData.storeOffHeap(data, this.address, this.size);
        this.size = data.length;
    }

    @Override
    public SnapRecord toSnapRecord(int nominalDelayInSeconds) {
        return new OrderedSnapRecord(getData(true), this.lastUpdateTimestamp, nominalDelayInSeconds);
    }

    @Override
    public void dispose() {
        if (this.address != 0) {
            UnsafeFieldData.freeMemory(this.address);
            this.address = 0;
        }
    }
}
