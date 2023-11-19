/*
 * WorkItem.java
 *
 * Created on 19.04.13 14:43
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.history.distruptor;

import de.marketmaker.istar.common.util.ByteString;
import de.marketmaker.istar.feed.tick.AbstractTickRecord;

/**
 * @author zzhao
 */
class RequestItem {
    private long sequence;

    private ByteString symbol;

    private byte[] tickData;

    private AbstractTickRecord.TickItem.Encoding encoding;

    private boolean damaged;

    private boolean negativeTicksPossible;

    void reset() {
        this.sequence = Long.MIN_VALUE;
        this.symbol = null;
        this.tickData = null;
        this.encoding = null;
        this.damaged = false;
        this.negativeTicksPossible = false;
    }

    AbstractTickRecord.TickItem.Encoding getEncoding() {
        return encoding;
    }

    RequestItem withSequence(long seq) {
        this.sequence = seq;
        return this;
    }

    ByteString getSymbol() {
        return this.symbol;
    }

    RequestItem withSymbol(ByteString symbol, AbstractTickRecord.TickItem.Encoding encoding,
            boolean negativeTicksPossible) {
        this.symbol = symbol;
        this.encoding = encoding;
        this.negativeTicksPossible = negativeTicksPossible;
        return this;
    }

    RequestItem withTickData(byte[] tickData) {
        this.tickData = tickData;
        return this;
    }

    long getSequence() {
        return sequence;
    }

    byte[] getTickData() {
        return tickData;
    }

    boolean isDamaged() {
        return damaged;
    }

    void setDamaged(boolean damaged) {
        this.damaged = damaged;
    }

    boolean isNegativeTicksPossible() {
        return negativeTicksPossible;
    }
}
