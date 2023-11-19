/*
 * AsciiBytes.java
 *
 * Created on 04.04.2005 10:13:07
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed;

import java.nio.charset.Charset;

import de.marketmaker.istar.common.util.PriceCoder;
import de.marketmaker.istar.feed.mdps.MdpsFeedUtils;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class AsciiBytes {
    public static final Charset CP_1252 = Charset.forName("windows-1252");

    private final static byte[] NUMBERS = new byte[]{
            (byte) '0', (byte) '1', (byte) '2', (byte) '3', (byte) '4',
            (byte) '5', (byte) '6', (byte) '7', (byte) '8', (byte) '9'
    };

    private static final byte DOT = (byte) '.';

    private static final byte DASH = (byte) '-';

    private static final byte COLON = (byte) ':';


    private int length = -1;

    private int offset;

    private byte[] data = new byte[PRICE_NUM_DIGITS];

    private StringBuilder sb;

    private static final byte MINUS = (byte) '-';

    private static final int PRICE_NUM_DIGITS = 20;

    public AsciiBytes() {
    }

    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "EI", justification = "client controls this data container")
    public byte[] getData() {
        return data;
    }

    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "EI", justification = "client controls this data container")
    public void setData(byte[] data) {
        this.data = data;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public void setUndefined() {
        this.length = -1;
    }

    public boolean isUndefined() {
        return (this.length == -1);
    }

    public String toString() {
        return new String(this.data, this.offset, this.length, CP_1252);
//        return ByteUtil.toString(this.data, this.offset, this.length);
    }

    /**
     * Formats date as yyyy-mm-dd
     */
    public void setIsoDate(int value) {
        setYyyyMmDd(value, DASH);
    }

    /**
     * Formats date in Dp-File Format: yyyy.mm.dd
     */
    public void setDate(int value) {
        setYyyyMmDd(value);
    }

    public void setIsoYyyyMmDd(int value) {
        setYyyyMmDd(value, DASH);
    }

    public void setYyyyMmDd(int value) {
        setYyyyMmDd(value, DOT);
    }

    private void setYyyyMmDd(int value, byte sep) {
        this.offset = 0;
        this.length = 10;

        for (int i = 10; i > 0; ) {
            switch (i) {
                case 8:
                case 5:
                    this.data[--i] = sep;
                default:
                    this.data[--i] = NUMBERS[value % 10];
            }
            value /= 10;
        }
    }

    public void setTime(int value) {
        setSecondOfDay(MdpsFeedUtils.decodeTime(value));
    }

    public void setSecondOfDay(int value) {
        this.offset = 0;
        this.length = 8;

        // value is seconds in day (0..86399)
        int sec = value % 60;
        int min = (value / 60) % 60;
        int hour = (int) (value / 3600L);

        this.data[7] = NUMBERS[sec % 10];
        this.data[6] = NUMBERS[sec / 10];
        this.data[5] = COLON;
        this.data[4] = NUMBERS[min % 10];
        this.data[3] = NUMBERS[min / 10];
        this.data[2] = COLON;
        this.data[1] = NUMBERS[hour % 10];
        this.data[0] = NUMBERS[hour / 10];
    }

    public void setPrice(int unscaled, int exponent) {
        this.offset = 0;
        if (unscaled == 0) {
            data[0] = NUMBERS[0];
            this.length = 1;
            return;
        }
        if (sb == null) {
            sb = new StringBuilder(PRICE_NUM_DIGITS);
        }
        this.sb.setLength(0);
        this.sb.append(unscaled);
        if (exponent >= 0) {
            if (PRICE_NUM_DIGITS <= sb.length() + exponent) {
                // 20 or more digits, not really...
                this.offset = 0;
                this.length = 0;
                return;
            }
            // easy, just append 0s
            final int n = sb.length();
            for (int i = 0; i < n; i++) {
                this.data[i] = (byte) sb.charAt(i);
            }
            for (int i = 0; i < exponent; i++) {
                this.data[n + i] = NUMBERS[0];
            }
            this.length = n + exponent;
        }
        else {
            final int k = sb.charAt(0) == '-' ? 1 : 0;
            if (sb.length() - k + exponent <= -10) {
                // starts with (-)0.000000000, just use 0
                this.data[0] = '0';
                this.offset = 0;
                this.length = 1;
                return;
            }
            int dotPos = sb.length() + exponent;
            int n = 0, m = 0;
            if (dotPos <= k) {
                if (k == 1) {
                    data[n++] = (byte) '-';
                    m = 1;
                }
                data[n++] = NUMBERS[0];
                data[n++] = (byte) '.';
                while (dotPos++ < k) {
                    data[n++] = NUMBERS[0];
                }
            }
            else {
                while (n < dotPos) {
                    data[n++] = (byte) sb.charAt(m++);
                }
                data[n++] = (byte) '.';
            }
            while (m < sb.length()) {
                data[n++] = (byte) sb.charAt(m++);
            }
            // remove trailing 0s
            while (data[n - 1] == NUMBERS[0]) {
                n--;
            }
            // and trailing dot
            if (data[n - 1] == (byte) '.') {
                n--;
            }
            this.length = n;
        }
    }

    public void setPrice(long value) {
        doSetPrice(PriceCoder.encode((int) value, MdpsFeedUtils.getMdpsPriceScale(value)));
    }

    private void doSetPrice(long value) {
        if (!PriceCoder.isEncodedWithDefaultFractions(value)) {
            // some price with more fractions as usual; since this happens very seldomly,
            // just create a String from a BigDecimal decoded value.
            final String str = PriceCoder.decode(value).toString();
            this.offset = 0;
            this.length = str.length();
            for (int i = 0; i < this.length; i++) {
                this.data[i] = (byte) str.charAt(i);
            }
            return;
        }

        int i = this.data.length;
        final boolean negative = (value < 0L);
        final long n = Math.abs(value);

        long decimal = n % 100000L;
        long whole = n / 100000L;

        int m = (n % 1000 == 0) ? 3 : (n % 100 == 0) ? 2 : (n % 10 == 0) ? 1 : 0;
        for (int j = 0; j < 5; j++) {
            if (j >= m) {
                this.data[--i] = NUMBERS[(int) (decimal % 10L)];
            }
            decimal /= 10L;
        }
        this.data[--i] = DOT;

        this.data[--i] = NUMBERS[(int) (whole % 10L)];
        whole /= 10L;
        while (whole != 0L) {
            this.data[--i] = NUMBERS[(int) (whole % 10L)];
            whole /= 10L;
        }

        if (negative) {
            this.data[--i] = MINUS;
        }

        this.offset = i;
        this.length = this.data.length - offset;
    }

    public void setNumber(long value) {
        int i = this.data.length;
        final boolean negative = (value < 0L);
        long n = Math.abs(value);

        this.data[--i] = NUMBERS[(int) (n % 10L)];
        n /= 10L;
        while (n != 0L) {
            this.data[--i] = NUMBERS[(int) (n % 10L)];
            n /= 10L;
        }

        if (negative) {
            this.data[--i] = MINUS;
        }
        this.offset = i;
        this.length = this.data.length - offset;
    }

    public void setString(byte[] src, int offset, int length) {
        if (this.data.length < length) {
            this.data = new byte[length];
        }
        System.arraycopy(src, offset, this.data, 0, length);
        this.offset = 0;
        this.length = (length == 1 && this.data[0] == 0x00) ? 0 : length;
    }

    public void appendTo(StringBuilder sb) {
        for (int i = this.offset, n = this.offset + this.length; i < n; i++) {
            sb.append((char) (this.data[i] & 0xFF));
        }
    }
}
