/*
 * ResultItem.java
 *
 * Created on 19.04.13 14:43
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.history.distruptor;

import de.marketmaker.istar.common.util.ByteString;

/**
 * @author zzhao
 */
class ResultItem {
    private ByteString symbol;

    private boolean damaged;

    private byte[] aggregatedTickData;

    void reset() {
        this.symbol = null;
        this.damaged = false;
        this.aggregatedTickData = null;
    }

    ByteString getSymbol() {
        return symbol;
    }

    void setSymbol(ByteString symbol) {
        this.symbol = symbol;
    }

    byte[] getAggregatedTickData() {
        return aggregatedTickData;
    }

    void setAggregatedTickData(byte[] aggregatedTickData) {
        this.aggregatedTickData = aggregatedTickData;
    }

    boolean isDamaged() {
        return damaged;
    }

    void setDamaged(boolean damaged) {
        this.damaged = damaged;
    }
}
