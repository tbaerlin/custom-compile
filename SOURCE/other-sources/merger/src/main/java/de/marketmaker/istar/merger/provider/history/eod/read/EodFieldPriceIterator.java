/*
 * EodPairIterator.java
 *
 * Created on 16.01.13 11:55
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.history.eod.read;

import java.nio.ByteBuffer;
import java.util.Iterator;

import de.marketmaker.istar.merger.provider.history.eod.BCD;
import de.marketmaker.istar.merger.provider.history.eod.EodUtil;

/**
 * @author zzhao
 */
public class EodFieldPriceIterator implements Iterator<EodFieldPrice> {
    private final EodFieldPrice eodFieldPrice = new EodFieldPrice();

    private final ByteBuffer bb;

    private short year = 0;

    private short months = 0;

    private int monthCount = 0;

    private int monthIdx = 0;

    private int days = 0;

    private int dayCount = 0;

    private int dayIdx = 0;

    private byte lastByte = BCD.BOUNDARY_BYTE;

    public EodFieldPriceIterator(ByteBuffer bb) {
        this.bb = bb;
    }

    @Override
    public boolean hasNext() {
        return this.bb.hasRemaining();
    }

    @Override
    public EodFieldPrice next() {
        if (this.year == 0) {
            this.year = this.bb.getShort();
        }
        if (this.months == 0) {
            this.months = EodUtil.decodeMonth(this.bb.getInt());
            this.monthCount = EodUtil.countUnits(this.months);
        }
        if (this.days == 0) {
            this.days = this.bb.getInt();
            this.dayCount = EodUtil.countUnits(this.days);
        }

        final int date = EodUtil.calcDate(this.year, this.months, this.monthIdx,
                this.days, this.dayIdx);
        final int pos = this.bb.position();
        byte b = this.bb.get();
        while (!BCD.isBoundary(b)) {
            b = this.bb.get();
        }

        final String str = BCD.toBCDString(this.bb, pos, this.bb.position());
        if (BCD.checkBoundary(this.lastByte) == BCD.Boundary.Low) {
            this.eodFieldPrice.reset(date, str);
        }
        else {
            this.eodFieldPrice.reset(date, BCD.toBCDChar((byte) (this.lastByte & 0x0F)) + str);
        }
        this.lastByte = b;

        this.dayIdx++;
        if (this.dayIdx == this.dayCount) {
            this.lastByte = BCD.BOUNDARY_BYTE;
            this.dayIdx = 0;
            this.days = 0;
            this.monthIdx++;
            if (this.monthIdx == this.monthCount) {
                this.monthIdx = 0;
                this.months = 0;
                this.year = 0;
            }
        }

        return this.eodFieldPrice;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("not implemented");
    }
}
