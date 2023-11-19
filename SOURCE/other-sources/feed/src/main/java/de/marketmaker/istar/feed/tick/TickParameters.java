/*
 * TickParameters.java
 *
 * Created on 29.11.2004 13:03:49
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.tick;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class TickParameters {
    private boolean trade;
    private boolean bid;
    private boolean ask;
    private boolean withClose;
    private boolean withKassa;
    private long yield=Long.MIN_VALUE;
    private boolean suspendStart;
    private boolean suspendEnd;

    public TickParameters() {
    }

    public boolean isWithKassa() {
        return withKassa;
    }

    public void setWithKassa(boolean withKassa) {
        this.withKassa = withKassa;
    }

    public boolean isTrade() {
        return trade;
    }

    public void setTrade(boolean trade) {
        this.trade = trade;
    }

    public boolean isBid() {
        return this.bid;
    }

    public void setBid(boolean bid) {
        this.bid = bid;
    }

    public boolean isAsk() {
        return this.ask;
    }

    public void setAsk(boolean ask) {
        this.ask = ask;
    }

    public boolean isWithClose() {
        return withClose;
    }

    public void setWithClose(boolean withClose) {
        this.withClose = withClose;
    }

    public boolean isWithYield() {
        return this.yield != Long.MIN_VALUE;
    }

    public void setYield(long yield) {
        this.yield = yield;
    }

    public long getYield() {
        return this.yield;
    }

    public boolean isWithPrices() {
        return this.bid || this.ask || this.trade;
    }

    public boolean isNonStandardTick() {
        return isWithYield() || this.withClose || this.withKassa || this.suspendEnd || this.suspendStart;
    }

    public boolean isSuspendEnd() {
        return suspendEnd;
    }

    public void setSuspendEnd(boolean suspendEnd) {
        this.suspendEnd = suspendEnd;
    }

    public boolean isSuspendStart() {
        return suspendStart;
    }

    public void setSuspendStart(boolean suspendStart) {
        this.suspendStart = suspendStart;
    }
}
