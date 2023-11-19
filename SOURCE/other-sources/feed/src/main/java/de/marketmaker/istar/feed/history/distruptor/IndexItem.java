/*
 * OaL.java
 *
 * Created on 15.04.13 14:49
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.history.distruptor;

import de.marketmaker.istar.common.util.ByteString;

/**
 * @author zzhao
 */
class IndexItem implements Comparable<IndexItem> {
    private ByteString symbol;

    private final long offset;

    private final int length;

    IndexItem(ByteString symbol, long offset, int length) {
        this.symbol = symbol;
        this.offset = offset;
        this.length = length;
    }

    @Override
    public int compareTo(IndexItem o) {
        return this.symbol.compareTo(o.symbol);
    }

    void setSymbol(ByteString symbol) {
        this.symbol = symbol;
    }

    ByteString getSymbol() {
        return symbol;
    }

    long getOffset() {
        return offset;
    }

    int getLength() {
        return length;
    }

    @Override
    public String toString() {
        return "{" + this.symbol + ";" + this.offset + ";" + this.length + "}";
    }
}