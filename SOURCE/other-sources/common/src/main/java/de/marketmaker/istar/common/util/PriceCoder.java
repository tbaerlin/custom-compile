/*
 * PriceUtils.java
 *
 * Created on 12.05.2006 10:15:17
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.common.util;

import java.math.BigDecimal;

/**
 * Encodes price values as long values, provides methods to decode those values. The default
 * encoding stores a price n.m as n * 100000 + m, thus allowing 5 digits for the fraction. However,
 * if more fraction digits are needed, this coder will use a different encodings scheme; in general,
 * clients should either use {@link #toDefaultEncoding(long)} to transform an encoded value into the
 * default encoding (possibly ignoring unsignificant fraction digits), or {@link #decode(long)}
 * to obtain a {@link java.math.BigDecimal} with all encoded fractional digits.
 * <p/>
 * <b>Note</b> Implementation details of this class should NOT be exposed to any external classes,
 * so all private static fields should remain private.
 *
 * @deprecated
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class PriceCoder {
    public static final int DEFAULT_FRACTION = 5;

    private static final int MAX_FRACTION = 8;

    private static final int[] FACTORS = new int[]{
            1, 10, 100, 1000, 10000, 100000, 1000000, 10000000, 100000000, 1000000000
    };
    private static final long[] MAX_VALUE_FOR_NUM_FRACTIONS = new long[FACTORS.length];

    static {
        for (int i = 0; i < FACTORS.length; i++) {
            MAX_VALUE_FOR_NUM_FRACTIONS[i] = (1L << 61) / FACTORS[i];
        }
    }

    // scale is in bits 62 and 61
    private static final long FRACT_MASK = 0x6000000000000000L;

    private static final long NO_FRACT_MASK = 0x1FFFFFFFFFFFFFFFL;

    private static final long[] FRACT_FLAGS = new long[]{
            0L,                  // default (5) fraction digits
            0x2000000000000000L, // 6
            0x4000000000000000L, // 7
            0x6000000000000000L, // 8 fraction digits
    };

    /**
     * Decodes a price encoded with {@link #encode(long,int,int)} as a BigDecimal value.
     */
    public static BigDecimal decode(long n) {
        if (isEncodedWithDefaultFractions(n)) {
            return BigDecimal.valueOf(n, DEFAULT_FRACTION);
        }
        return BigDecimal.valueOf(clearFractionFlags(n), getFractionDigits(n));
    }

    public static double decodeAsDouble(long n) {
        if (isEncodedWithDefaultFractions(n)) {
            return ((double) n) / 100000d;
        }
        return ((double) clearFractionFlags(n)) / FACTORS[getFractionDigits(n)];
    }

    public static long clearFractionFlags(long n) {
        if (n < 0) {
            return n | FRACT_MASK;
        }
        else {
            return n & NO_FRACT_MASK;
        }
    }


    public static long toDefaultEncoding(long n) {
        if (isEncodedWithDefaultFractions(n)) {
            return n;
        }
        final long m = clearFractionFlags(n);
        final long result = m / FACTORS[getExcessFractions(n)];
        return result;
    }

    /**
     * Returns the number of n's fractions in excess of {@long #MIN_FRACTION}.
     */
    private static int getExcessFractions(long n) {
        if (n < 0) {
            return (((int) (n >>> 61)) ^ 0x03) & 0x03;
        }
        else {
            return (int) (n >>> 61) & 0x03;
        }
    }

    public static int getFractionDigits(long n) {
        return DEFAULT_FRACTION + getExcessFractions(n);
    }

    public static boolean isEncodedWithDefaultFractions(long n) {
        return n < 0 ? (n | FRACT_MASK) == n : (n & FRACT_MASK) == 0L;
    }

    /**
     * @return minimum of the two encoded prices p1 and p2
     */
    public static long min(long p1, long p2) {
        if (isEncodedWithDefaultFractions(p1 | p2)) {
            return Math.min(p1, p2);
        }
        return PriceCoder.decodeAsDouble(p1) < PriceCoder.decodeAsDouble(p2) ? p1 : p2;
    }

    /**
     * @return maximum of the two encoded prices p1 and p2
     */
    public static long max(long p1, long p2) {
        if (isEncodedWithDefaultFractions(p1 | p2)) {
            return Math.max(p1, p2);
        }
        return PriceCoder.decodeAsDouble(p1) > PriceCoder.decodeAsDouble(p2) ? p1 : p2;
    }

    /**
     * Encodes a decimal number without a fraction.
     *
     * @param decimal
     * @return encoded price
     */
    public static long encode(long decimal) {
        return decimal * FACTORS[DEFAULT_FRACTION];
    }

    /**
     * Encodes a decimal number according to a given scale. encode(123, 2) would yield 12300
     * and encode(123, -2) would yield 1.23
     *
     * @param decimal
     * @return encoded price
     */
    public static long encode(long decimal, int scale) {
        if (decimal == 0) {
            return 0;
        }
        if (scale == 0) {
            return encode(decimal);
        }
        if (scale > 0) {
            if (scale > FACTORS.length - 1) {
                throw new IllegalArgumentException("scale too large: " + scale);
            }
            return encode(decimal * FACTORS[scale]);
        }
        int numFractions = -scale;
        if (numFractions > MAX_FRACTION) {
            // NOBODY needs more than 8 fraction digits, remove trailing digits
            if (numFractions - MAX_FRACTION >= FACTORS.length) {
                return 0;
            }
            decimal /= FACTORS[numFractions - MAX_FRACTION];
            numFractions = MAX_FRACTION;
        }
        final int d = (int) (decimal / FACTORS[numFractions]);
        final int fraction = (int) (decimal % FACTORS[numFractions]);
        return encode(d, fraction, numFractions);
    }

    /**
     * Encodes a price in a long value; for example, the parameters 42, 35, 4 would yield 42.0035
     * and -42, -78, 2 would yield -42.78.
     *
     * @param decimal      the non-fractional part of the price to be encoded, may be negative
     * @param fraction     number representing the fraction, <b>should always have the same signum as
     *                     decimal, may only differ if decimal is 0</b>.
     * @param numFractions number of faction digits, from 0 to {@link #MAX_FRACTION} incl.
     * @return encoded price.
     */
    public static long encode(long decimal, int fraction, int numFractions) {
        if (numFractions <= 0 || fraction == 0) {
            check(decimal, DEFAULT_FRACTION);
            return decimal * (long) FACTORS[DEFAULT_FRACTION];
        }

        if (numFractions <= DEFAULT_FRACTION) {
            check(decimal, DEFAULT_FRACTION);
            return decimal * (long) FACTORS[DEFAULT_FRACTION]
                    + (fraction * FACTORS[DEFAULT_FRACTION - numFractions]);
        }

        // now we might have s.th. like fraction = 140000000, numFractions = 9 => 0.14
        // this may be stupid, but it happens. We try to get to f=14, nF=2. Since
        // getting here is rather exceptional, performance does not suffer from trying
        int i = 0;
        while (fraction % FACTORS[i + 1] == 0) {
            i++;
        }

        if (i > 0) {
            numFractions -= i;
            fraction = fraction / FACTORS[i];
            // maybe we have a "normal" number for fractions now:
            if (numFractions <= DEFAULT_FRACTION) {
                check(decimal, DEFAULT_FRACTION);
                return decimal * (long) FACTORS[DEFAULT_FRACTION]
                        + (fraction * FACTORS[DEFAULT_FRACTION - numFractions]);
            }
        }

        while (numFractions > MAX_FRACTION) {
            fraction /= 10;
            numFractions--;
        }

        final long result = decimal * (long) FACTORS[numFractions] + fraction;
        check(decimal, numFractions);
        if (result < 0) {
            return result ^ FRACT_FLAGS[numFractions - DEFAULT_FRACTION];
        }
        else {
            return result | FRACT_FLAGS[numFractions - DEFAULT_FRACTION];
        }
    }

    private static void check(long decimal, int numFractions) {
        if (decimal > 0) {
            if (decimal > MAX_VALUE_FOR_NUM_FRACTIONS[numFractions]) {
                throw new IllegalArgumentException("value too large to encode: " + decimal);
            }
        }
        else if (decimal < -MAX_VALUE_FOR_NUM_FRACTIONS[numFractions]) {
            throw new IllegalArgumentException("value too large to encode: " + decimal);
        }
    }

    /**
     * Convenience method to encode a string as a price. The string is expected to be of the
     * form n.m, where n is the decimal part, m the fractional part.
     */
    public static long encode(String s) {
        s = s.trim();
        final int dotPos = s.indexOf('.');
        final long decimal = Long.parseLong(s.substring(0, (dotPos != -1 ? dotPos : s.length())));
        if (dotPos == -1 || s.charAt(s.length() - 1) == '.') {
            return PriceCoder.encode(decimal, 0, 0);
        }
        String fStr = s.substring(dotPos + 1);
        if (fStr.length() > 5) {
            do {
                // sometimes a longer fraction ends with 0s, which we strip:
                if (fStr.charAt(fStr.length() - 1) == '0') {
                    fStr = fStr.substring(0, fStr.length() - 1);
                }
                else {
                    break;
                }
            }
            while (fStr.length() > 5);

            if (fStr.length() > 8) {
                fStr = fStr.substring(0, 8);
            }
        }
        int fraction = Integer.parseInt(fStr);
        if (s.charAt(0) == '-') {
            fraction = -fraction;
        }
        return PriceCoder.encode(decimal, fraction, fStr.length());
    }
}
