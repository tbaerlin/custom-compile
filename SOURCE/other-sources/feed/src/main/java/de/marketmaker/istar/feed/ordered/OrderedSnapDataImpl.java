/*
 * OrderedSnapData.java
 *
 * Created on 30.08.12 14:54
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.ordered;

import java.util.Arrays;

import de.marketmaker.istar.domain.data.NullSnapRecord;
import de.marketmaker.istar.domain.data.SnapRecord;
import de.marketmaker.istar.feed.snap.IndexAndOffset;

/**
 * @author oflege
 */
public final class OrderedSnapDataImpl implements OrderedSnapData {
    private byte[] data;

    private int lastUpdateTimestamp;

    public int getLastUpdateTimestamp() {
        return lastUpdateTimestamp;
    }

    public void setLastUpdateTimestamp(int lastUpdateTimestamp) {
        this.lastUpdateTimestamp = lastUpdateTimestamp;
    }

    @Override
    public void init(OrderedSnapRecord src) {
        this.lastUpdateTimestamp = src.getLastUpdateTimestamp();
        this.data = src.getData();
    }

    @Override
    public byte[] getData(boolean copy) {
        return copy ? Arrays.copyOf(this.data, this.data.length) : this.data;
    }

    @Override
    public IndexAndOffset getIndexAndOffset() {
        return null;
    }

    @Override
    public boolean isInitialized() {
        return this.data != null;
    }

    @Override
    public void init(IndexAndOffset indexAndOffset, byte[] data) {
        this.data = data;
    }

    @Override
    public SnapRecord toSnapRecord(int nominalDelayInSeconds) {
        if (!isInitialized()) {
            return NullSnapRecord.INSTANCE;
        }
        return new OrderedSnapRecord(getData(true), this.lastUpdateTimestamp, nominalDelayInSeconds);
    }

    @Override
    public void dispose() {
        this.data = null;
    }
}
