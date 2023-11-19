/*
 * Field.java
 *
 * Created on 11.01.13 12:09
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.history.eod.write;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.shorts.ShortArrayList;
import it.unimi.dsi.fastutil.shorts.ShortList;

import de.marketmaker.istar.merger.provider.history.eod.EodUtil;

/**
 * @author zzhao
 */
class EodField {

    private int fieldId;

    private byte[] data;

    private int offset;

    private int length;

    EodField(int fieldId) {
        this(fieldId, EodUtil.EMPTY_BA, 0, 0);
    }

    EodField(int fieldId, byte[] data, int offset, int length) {
        this.fieldId = fieldId;
        this.data = data;
        this.offset = offset;
        this.length = length;
    }

    void with(EodField another) {
        withData(another.fieldId, another.data, another.offset, another.length);
    }

    void clear() {
        this.fieldId = EodUtil.ZERO_BYTE;
        this.data = null;
        this.offset = 0;
        this.length = 0;
        this.years.clear();
        this.months.clear();
        this.offsets.clear();
        this.lengths.clear();
        this.yearDataList.clear();
        this.loaded = false;
    }

    void withData(int field, byte[] data, int offset, int length) {
        this.fieldId = field;
        this.data = data;
        this.offset = offset;
        this.length = length;
    }

    int getFieldId() {
        return fieldId;
    }

    byte[] getBytes(int pivot) {
        if (pivot == 0 && !this.loaded) {
            return Arrays.copyOfRange(this.data, this.offset, this.offset + this.length);
        }

        loadIfNecessary();
        final short py = (short) (pivot / 10000);
        final int rest = pivot % 10000;
        final short pm = (short) (rest / 100);
        final int pd = rest % 100;

        final ByteBuffer bb = ByteBuffer.allocate(getTotalLength());
        for (int i = yearsSize(); i-- != 0; ) {
            final short year = getYear(i);
            if (year > py) {
                final int len = getLengthAt(i);
                if (len > 0) {
                    bb.putShort(year);
                    bb.putInt(EodUtil.encodeLengthMonth(len, getMonths(i)));
                    bb.put(getYearData(i), getOffsetAt(i), getLengthAt(i));
                }
            }
            else if (year == py) {
                final byte[] dat = EodYear.pivot(wrap(i), getMonths(i), pm, pd);
                if (dat.length > 0) {
                    bb.putShort(year);
                    bb.put(dat);
                }
            }
            else {
                break;
            }
        }

        return EodUtil.fromBuffer(bb);
    }

    private int getTotalLength() {
        int len = 0;
        for (int i = 0; i < this.lengths.size(); i++) {
            final int subLen = this.lengths.getInt(i);
            if (subLen > 0) {
                len += subLen + 4 + 2;
            }
        }
        return len;
    }

    private boolean loaded = false;

    private ShortList years = new ShortArrayList();

    private ShortList months = new ShortArrayList();

    private IntList offsets = new IntArrayList();

    private IntList lengths = new IntArrayList();

    private List<byte[]> yearDataList = new ArrayList<>();

    private int yearsSize() {
        return this.years.size();
    }

    private short getYear(int idx) {
        return getShortValue(this.years, idx);
    }

    short getMonths(int idx) {
        return getShortValue(this.months, idx);
    }

    private int getOffsetAt(int idx) {
        return getIntValue(this.offsets, idx);
    }

    private int getLengthAt(int idx) {
        return getIntValue(this.lengths, idx);
    }

    private byte[] getYearData(int idx) {
        return this.yearDataList.get(idx);
    }

    private static short getShortValue(ShortList list, int idx) {
        return list.getShort(idx);
    }

    private static int getIntValue(IntList list, int idx) {
        return list.getInt(idx);
    }

    private void loadIfNecessary() {
        if (this.loaded) {
            return;
        }
        if (this.length > 0) {
            final ByteBuffer bb = ByteBuffer.wrap(this.data, this.offset, this.length);
            while (bb.hasRemaining()) {
                final short year = bb.getShort();
                final int len20Months12 = bb.getInt();
                final int len = EodUtil.decodeYearLength(len20Months12);
                final short monthBits = EodUtil.decodeMonth(len20Months12);
                this.years.add(year);
                this.months.add(monthBits);
                this.offsets.add(bb.position());
                this.lengths.add(len);
                this.yearDataList.add(this.data);
                bb.position(bb.position() + len);
            }
            if (this.years.size() > 1) {
                Collections.reverse(this.years);
                Collections.reverse(this.months);
                Collections.reverse(this.offsets);
                Collections.reverse(this.lengths);
                Collections.reverse(this.yearDataList);
            }
        }
        this.loaded = true;
    }

    void merge(EodField another) {
        another.loadIfNecessary();
        loadIfNecessary();

        for (int i = 0; i < another.yearsSize(); i++) {
            final short year = another.getYear(i);
            final int idx = Collections.binarySearch(this.years, year);
            if (idx < 0) {
                // no data for year
                final int pos = -idx - 1;
                this.years.add(pos, year);
                this.months.add(pos, another.getMonths(i));
                this.offsets.add(pos, another.getOffsetAt(i));
                this.lengths.add(pos, another.getLengthAt(i));
                this.yearDataList.add(pos, another.getYearData(i));
            }
            else {
                final byte[] dat = EodYear.merge(wrap(idx), getMonths(idx), another.wrap(i),
                        another.getMonths(i));
                setYearData(idx, dat, another.getMonths(i));
            }
        }
    }

    private void setYearData(int idx, byte[] dat, short monthBits) {
        this.months.set(idx, (short) (getMonths(idx) | monthBits));
        this.offsets.set(idx, 0);
        this.lengths.set(idx, dat.length);
        this.yearDataList.set(idx, dat);
    }

    void replaceYearData(int idx, byte[] dat, short monthBits) {
        this.months.set(idx, monthBits);
        this.offsets.set(idx, 0);
        this.lengths.set(idx, dat.length);
        this.yearDataList.set(idx, dat);
    }


    ByteBuffer wrap(int idx) {
        return ByteBuffer.wrap(getYearData(idx), getOffsetAt(idx), getLengthAt(idx));
    }

    void setPrice(int date, byte[] bytes) {
        loadIfNecessary();

        final short year = (short) (date / 10000);
        final int rest = date % 10000;
        final short month = (short) (rest / 100);
        final int day = rest % 100;

        final int idx = Collections.binarySearch(this.years, year);
        final short monthBit = EodUtil.monthBit(month);
        if (idx < 0) {
            if (bytes.length > 0) {
                // no data for year
                final int pos = -idx - 1;
                this.years.add(pos, year);
                final byte[] dat = EodYear.forPrice(day, bytes);
                addYearData(pos, dat, monthBit);
            }
        }
        else {
            final short monthBits = getMonths(idx);
            if ((monthBits & monthBit) == monthBit || bytes.length > 0) {
                EodYear.setPrice(this, idx, monthBit, EodUtil.dayBit(day), bytes);
            }
        }
    }

    private void addYearData(int pos, byte[] dat, short monthBits) {
        this.months.add(pos, monthBits);
        this.offsets.add(pos, 0);
        this.lengths.add(pos, dat.length);
        this.yearDataList.add(pos, dat);
    }
}
