/*
 * EodYear.java
 *
 * Created on 11.01.13 12:10
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.history.eod.write;

import java.nio.ByteBuffer;
import java.util.Arrays;

import de.marketmaker.istar.merger.provider.history.eod.BCD;
import de.marketmaker.istar.merger.provider.history.eod.EodUtil;

/**
 * @author zzhao
 */
class EodYear {

    static byte[] forPrice(int day, byte[] price) {
        final ByteBuffer bb = ByteBuffer.allocate(4 + price.length);
        bb.putInt(EodUtil.dayBit(day));
        bb.put(price);
        return bb.array();
    }

    static void setPrice(EodField eodField, int idx, short monthBit, int dayBit, byte[] bytes) {
        final ByteBuffer buf = eodField.wrap(idx);
        short monthBits = eodField.getMonths(idx);

        final ByteBuffer bb = ByteBuffer.allocate(buf.remaining() + 4 + bytes.length);
        final int cnt = EodUtil.countUnits(monthBits);
        final int bypassCnt = EodUtil.units2Bypass(monthBits, monthBit);

        transferMonths(bb, buf, bypassCnt);

        if ((monthBits & monthBit) == monthBit) {
            // contains data for same month
            final int days = buf.getInt();
            if ((days & dayBit) == 0 && bytes.length == 0) { // deleting no-existing data
                return;
            }
            else {
                final int pos = buf.position();
                final ByteBuffer monthBuf = buf.slice();
                final int dayCount = EodUtil.countUnits(days);
                BCD.bypassPrices(buf, dayCount);
                monthBuf.limit(buf.position() - pos);
                final byte[] dat = EodMonth.setPrice(monthBuf, days, dayBit, bytes);
                if (dat.length > 0) {
                    bb.putInt(EodUtil.getDayBits(days, dayBit, bytes.length == 0));
                    bb.put(dat);
                }
                else {
                    monthBits &= (~monthBit);
                }
                transferMonths(bb, buf, cnt - bypassCnt - 1); // transfer rest if any
            }
        }
        else {
            // new month
            if (bytes.length > 0) {
                monthBits |= monthBit;
                bb.putInt(dayBit);
                bb.put(bytes);
            }
            transferMonths(bb, buf, cnt - bypassCnt); // transfer rest if any
        }

        eodField.replaceYearData(idx, EodUtil.fromBuffer(bb), monthBits);
    }

    /**
     * @param base a buffer containing one year's data
     * @param msBase month bits of base, where it i's bit is set if month i is present
     * @param delta a buffer containing one year's data to be merged
     * @param msDelta month bits of delta, where it i's bit is set if month i is present
     * @return an array of year data merged from base and delta
     */
    static byte[] merge(ByteBuffer base, short msBase, ByteBuffer delta, short msDelta) {
        final ByteBuffer bb = ByteBuffer.allocate(base.remaining() + delta.remaining());
        try {
            BASE.reset(msBase, base);
            DELTA.reset(msDelta, delta);

            BASE.next();
            DELTA.next();

            while (BASE.month != 0 || DELTA.month != 0) {
                if (BASE.month == 0) {
                    // transfer from delta
                    DELTA.transferRest(bb);
                    break;
                }
                else if (DELTA.month == 0) {
                    // transfer from base
                    BASE.transferRest(bb);
                    break;
                }
                else {
                    if (BASE.month > DELTA.month) {
                        BASE.transferOneMonth(bb);
                        BASE.next();
                    }
                    else if (BASE.month < DELTA.month) {
                        DELTA.transferOneMonth(bb);
                        DELTA.next();
                    }
                    else {
                        bb.put(BASE.merge(DELTA));
                        BASE.next();
                        DELTA.next();
                    }
                }
            }
        } finally {
            BASE.clear();
            DELTA.clear();
        }

        return EodUtil.fromBuffer(bb);
    }

    private static final Item BASE = new Item();

    private static final Item DELTA = new Item();

    private static final class Item {
        private short monthBits;

        private ByteBuffer yearBuf;

        private ByteBuffer monthBuf;

        private int count;

        private int idx;

        private int month;

        private void clear() {
            this.monthBits = 0;
            this.yearBuf = null;
            this.monthBuf = null;
            this.count = 0;
            this.idx = 0;
            this.month = 0;
        }

        private void reset(short monthBits, ByteBuffer buf) {
            this.monthBits = monthBits;
            this.yearBuf = buf.asReadOnlyBuffer();
            this.monthBuf = buf.asReadOnlyBuffer();
            this.count = EodUtil.countUnits(this.monthBits);
            this.idx = 0;
            this.month = 0;
        }

        private void next() {
            if (this.idx < this.count) {
                this.month = EodUtil.decodeMonth(this.monthBits, this.idx);
                this.idx++;

                this.monthBuf.limit(this.yearBuf.limit());
                this.monthBuf.position(this.yearBuf.position());
                final int dayBits = this.yearBuf.getInt();
                BCD.bypassPrices(this.yearBuf, EodUtil.countUnits(dayBits));
                this.monthBuf.limit(this.yearBuf.position());
            }
            else {
                this.month = 0;
            }
        }

        private void transferOneMonth(ByteBuffer bb) {
            bb.put(this.monthBuf);
        }

        private void transferRest(ByteBuffer bb) {
            bb.put(this.monthBuf);
            bb.put(this.yearBuf);
        }

        private byte[] merge(Item delta) {
            return EodMonth.merge(this.monthBuf, delta.monthBuf);
        }
    }

    /**
     * @param buf a buffer containing one year's data
     * @param months month bits where it i's bit is set if month i is present
     * @param pm the pivot month
     * @param pd the pivot day
     * @return an array containing one year's data from the given pivot month and day, with header
     *         Length20Months12. Empty array if no year data from the pivot month and day.
     */
    static byte[] pivot(ByteBuffer buf, short months, short pm, int pd) {
        final ByteBuffer bb = ByteBuffer.allocate(4 + buf.remaining());
        bb.putInt(0); // placeholder for Length20Months12
        final short mb = EodUtil.monthBit(pm);
        final int bypassCnt = EodUtil.units2Bypass(months, mb);

        transferMonths(bb, buf, bypassCnt);
        if ((months & mb) == mb) {
            // contains data for pivot month
            final int dayBits = buf.getInt();
            final int dayBit = EodUtil.dayBit(pd);

            int daysToTransfer = EodUtil.units2Bypass(dayBits, dayBit);
            if ((dayBits & dayBit) == dayBit) {
                daysToTransfer++;
            }
            if (daysToTransfer > 0) {
                bb.putInt(EodUtil.pivot(dayBits, dayBit));
                BCD.transferPrices(bb, buf, daysToTransfer);
            }
            else {
                months &= (~mb);
            }
        }

        if (bb.position() > 4) {
            BCD.replacePriceEnd(bb);
            bb.putInt(0, EodUtil.encodeLengthMonth(bb.position() - 4,
                    (short) EodUtil.pivot(months, mb)));
            return bb.hasRemaining() ? Arrays.copyOfRange(bb.array(), 0, bb.position())
                    : bb.array();
        }
        else {
            return EodUtil.EMPTY_BA;
        }
    }


    private static void transferMonths(ByteBuffer tar, ByteBuffer src, int count) {
        for (int i = 0; i < count; i++) {
            transferMonth(tar, src);
        }
    }

    private static void transferMonth(ByteBuffer tar, ByteBuffer src) {
        final int ds = src.getInt();
        tar.putInt(ds);
        final int cnt = EodUtil.countUnits(ds);
        BCD.transferPrices(tar, src, cnt);
    }
}
