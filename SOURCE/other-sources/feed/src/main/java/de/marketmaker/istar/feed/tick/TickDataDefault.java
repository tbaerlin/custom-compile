/*
 * TickRecord.java
 *
 * Created on 25.10.2004 15:19:49
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.tick;

import de.marketmaker.istar.common.util.TimeFormatter;


/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class TickDataDefault extends TickData {
    private long lastPrice = Long.MIN_VALUE;

    private int lastVolume = Integer.MIN_VALUE;

    private byte[] lastSupplement = null;

    public String toString() {
        return "TD["
                + super.toString()
                + ", lastPrice=" + this.lastPrice
                + "(" + (this.lastVolume != Integer.MIN_VALUE ? Integer.toString(this.lastVolume) : "-") + ")"
                + (this.lastSupplement != null ? ("'" + new String(this.lastSupplement) + "'") : "")
                + ","
                + TimeFormatter.formatSecondsInDay(getLastTime())
                + ","
                + getLastDate()
                + "]";
    }


    protected void resetMyLast() {
        this.lastPrice = Long.MIN_VALUE;
        this.lastVolume = Integer.MIN_VALUE;
        this.lastSupplement = null;
    }

    public long getLastPrice() {
        return lastPrice;
    }

    public int getLastVolume() {
        return lastVolume;
    }

    public void setLastPrice(long lastPrice) {
        this.lastPrice = lastPrice;
    }

    public void setLastVolume(int lastVolume) {
        this.lastVolume = lastVolume;
    }

    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "EI", justification = "performance")
    public byte[] getLastSupplement() {
        return lastSupplement;
    }

    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "EI", justification = "performance")
    public void setLastSupplement(byte[] lastSupplement) {
        this.lastSupplement = lastSupplement;
    }

}
