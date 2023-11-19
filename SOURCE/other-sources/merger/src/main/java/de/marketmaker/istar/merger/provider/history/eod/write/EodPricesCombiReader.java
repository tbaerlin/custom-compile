/*
 * ItemBuffer.java
 *
 * Created on 12.12.12 17:15
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.history.eod.write;

import java.io.IOException;

import de.marketmaker.istar.common.util.IoUtils;
import de.marketmaker.istar.feed.history.HistoryUtil;

/**
 * Used when patch is to be reduced by rest and months.
 *
 * @author zzhao
 */
class EodPricesCombiReader implements EodReader<EodPrices> {

    private final EodReader<EodPrices> restReader;

    private final EodReader<EodPrices> monthsReader;

    public EodPricesCombiReader(EodReader<EodPrices> restReader,EodReader<EodPrices> monthsReader) {
        this.restReader = restReader;
        this.monthsReader = monthsReader;
    }

    @Override
    public void close() throws IOException {
        IoUtils.close(this.restReader);
        IoUtils.close(this.monthsReader);
    }

    @Override
    public MyIterator iterator() {
        return new MyIterator(this.restReader.iterator(), this.monthsReader.iterator());
    }

    public final class MyIterator implements EodIterator<EodPrices> {

        private final EodIterator<EodPrices> itA;

        private final EodIterator<EodPrices> itB;

        private EodPrices itemA;

        private EodPrices itemB;

        private EodPrices eodFields;

        private long quote;

        public MyIterator(EodIterator<EodPrices> itA, EodIterator<EodPrices> itB) {
            this.itA = itA;
            this.itB = itB;
        }

        @Override
        public boolean hasNext() {
            return this.itA.hasNext() || this.itB.hasNext();
        }

        public long getQuote() {
            return this.quote;
        }

        @Override
        public EodPrices next() {
            if (null == this.itemA) {
                this.itemA = HistoryUtil.nextItem(this.itA);
            }
            if (null == this.itemB) {
                this.itemB = HistoryUtil.nextItem(this.itB);
            }

            if (null == this.itemA) {
                this.quote = this.itB.getQuote();
                this.eodFields = this.itemB;
                this.itemB = null;
            }
            else if (null == this.itemB) {
                this.quote = this.itA.getQuote();
                this.eodFields = this.itemA;
                this.itemA = null;
            }
            else {
                if (this.itA.getQuote() > this.itB.getQuote()) {
                    this.quote = this.itB.getQuote();
                    this.eodFields = this.itemB;
                    this.itemB = null;
                }
                else if (this.itA.getQuote() < this.itB.getQuote()) {
                    this.quote = this.itA.getQuote();
                    this.eodFields = this.itemA;
                    this.itemA = null;
                }
                else {
                    this.quote = this.itA.getQuote();
                    this.itemA.merge(this.itemB, false);
                    this.eodFields = this.itemA;
                    this.itemA = null;
                    this.itemB = null;
                }
            }
            return this.eodFields;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("not implemented");
        }
    }
}
