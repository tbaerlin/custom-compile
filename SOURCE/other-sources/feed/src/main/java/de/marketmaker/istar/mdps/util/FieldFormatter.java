/*
 * FieldFormatter.java
 *
 * Created on 04.10.13 17:05
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.mdps.util;

import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * @author oflege
 */
public class FieldFormatter {
    // copied & adapted from java.lang.Integer
    final static byte[] DigitTens = {
            '0', '0', '0', '0', '0', '0', '0', '0', '0', '0',
            '1', '1', '1', '1', '1', '1', '1', '1', '1', '1',
            '2', '2', '2', '2', '2', '2', '2', '2', '2', '2',
            '3', '3', '3', '3', '3', '3', '3', '3', '3', '3',
            '4', '4', '4', '4', '4', '4', '4', '4', '4', '4',
            '5', '5', '5', '5', '5', '5', '5', '5', '5', '5',
            '6', '6', '6', '6', '6', '6', '6', '6', '6', '6',
            '7', '7', '7', '7', '7', '7', '7', '7', '7', '7',
            '8', '8', '8', '8', '8', '8', '8', '8', '8', '8',
            '9', '9', '9', '9', '9', '9', '9', '9', '9', '9',
    };

    // copied & adapted from java.lang.Integer
    final static byte[] DigitOnes = {
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
    };

    private static final byte[] MIN_VALUE = "-2147483648".getBytes();

    private static final byte[] ZERO_TIME = "00:00:00.000".getBytes();

    private static int append(int i, int offset, byte[] buf) {
        if (i == Integer.MIN_VALUE) {
            final int start = offset - MIN_VALUE.length;
            System.arraycopy(MIN_VALUE, 0, buf, start, MIN_VALUE.length);
            return start;
        }
        return append(i, buf, offset);
    }

    /**
     * Places ascii bytes representing the integer i into the
     * array buf. The characters are placed into
     * the buffer backwards starting with the least significant
     * digit at offset - 1, and working backwards from there.
     *
     * Will fail if i == Integer.MIN_VALUE
     * @return the position of the first character of the rendered number in buf
     */
    private static int append(int i, byte[] buf, int offset) {
        int q, r;
        int index = offset;
        char sign = 0;

        if (i < 0) {
            sign = '-';
            i = -i;
        }

        // Generate two digits per iteration
        while (i >= 65536) {
            q = i / 100;
            // really: r = i - (q * 100);
            r = i - ((q << 6) + (q << 5) + (q << 2));
            i = q;
            buf[--index] = DigitOnes[r];
            buf[--index] = DigitTens[r];
        }

        // Fall thru to fast mode for smaller numbers
        // assert(i <= 65536, i);
        for (; ; ) {
            q = (i * 52429) >>> (16 + 3);
            r = i - ((q << 3) + (q << 1));  // r = i-(q*10) ...
            buf[--index] = DigitOnes[r];
            i = q;
            if (i == 0) break;
        }
        if (sign != 0) {
            buf[--index] = '-';
        }
        return index;
    }

    /**
     * This is one of the most used functions in iqs: it will be called whenever a field is
     * added to a message, so to speed things up we write directly into the buffer;
     * @param bb target
     * @param i to be appended as decimal string
     */
    public static void appendUnsignedShort(ByteBuffer bb, int i) {
        assert (i & ~0xFFFF) == 0;
        int end = bb.position() + getLength(i), index = end, q, r;
        do {
            q = (i * 52429) >>> (16 + 3);
            r = i - ((q << 3) + (q << 1));  // r = i-(q*10) ...
            bb.put(--index, DigitOnes[r]);
            i = q;
        } while (i != 0);
        bb.position(end);
    }

    private static int getLength(int i) {
        return (i < 100 ? (i < 10 ? 1 : 2) : (i < 10000 ? (i < 1000 ? 3 : 4) : 5));
    }

    private final boolean renderTimeWithMs;

    private final byte[] dateBuffer = new byte[32];

    public FieldFormatter(boolean renderTimeWithMs) {
        this.renderTimeWithMs = renderTimeWithMs;
    }

    public int getTimeLength() {
        return this.renderTimeWithMs ? 12 : 8;
    }

    public void renderDate(int date, byte[] dst) {
        append(date, dateBuffer, 8);

        dst[0] = dateBuffer[6];
        dst[1] = dateBuffer[7];
        dst[2] = (byte) '.';
        dst[3] = dateBuffer[4];
        dst[4] = dateBuffer[5];
        dst[5] = (byte) '.';
        dst[6] = dateBuffer[0];
        dst[7] = dateBuffer[1];
        dst[8] = dateBuffer[2];
        dst[9] = dateBuffer[3];
    }

    public int renderPrice(int base, int exponent, byte[] dst) {
        if (exponent == 0) {
            return append(base, dst.length, dst);
        }
        if (exponent > 0) {
            final int offset = dst.length - exponent;
            Arrays.fill(dst, offset, dst.length, (byte) '0');
            return append(base, offset, dst);
        }

        int from = append(base < 0 ? -base : base, dst.length, dst);
        if (from - dst.length < exponent) {
            final int length = (dst.length - from) + exponent;
            System.arraycopy(dst, from, dst, from - 1, length);
            dst[dst.length + exponent - 1] = '.';
            from--;
        }
        else {
            final int num = (from - dst.length) - exponent + 2;
            Arrays.fill(dst, from - num, from, (byte) '0');
            dst[dst.length + exponent - 1] = '.';
            from -= num;
        }
        if (base < 0) {
            dst[--from] = '-';
        }
        return from;
    }

    public int renderInt(int i, byte[] dst) {
        return append(i, dst.length, dst);
    }

    public void renderSecondOfDay(int s, byte[] dst) {
        System.arraycopy(ZERO_TIME, 0, dst, 0, Math.min(8, dst.length));
        append(s / 3600, dst, 2);
        append((s % 3600) / 60, dst, 5);
        append(s % 60, dst, 8);
    }

    public void renderMdpsTime(int i, byte[] dst) {
        int hh = i >>> 27;
        int mm = (i >>> 21) & 0x3F;
        int ss = (i >>> 15) & 0x3F;

        System.arraycopy(ZERO_TIME, 0, dst, 0, Math.min(12, dst.length));
        append(hh, dst, 2);
        append(mm, dst, 5);
        append(ss, dst, 8);

        if (this.renderTimeWithMs) {
            append(i & 0x7FFF, dst, 12);
        }
    }
}
