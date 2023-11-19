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

/**
 * @author zzhao
 */
public class EodPriceIterator implements Iterator<EodPrice> {

    private final EodPrice priceMpc = new EodPrice();

    private final ByteBuffer bb;

    public EodPriceIterator(ByteBuffer bb) {
        this.bb = bb;
    }

    @Override
    public boolean hasNext() {
        return this.bb.hasRemaining();
    }

    @Override
    public EodPrice next() {
        final int date = this.bb.getInt();
        this.priceMpc.reset(date);
        final int fc = this.bb.get();
        for (int i = 0; i < fc; i++) {
            this.priceMpc.put(this.bb.get(), BCD.parsePrice(this.bb));
        }

        return this.priceMpc;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("not implemented");
    }
}
