/*
 * RatioFormatter.java
 *
 * Created on 22.09.2005 14:18:16
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.common.util;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class RatioFormatter {
    /**
     * prime numbers from 2 to 100
     */
    private static final int[] PRIMES = new int[]{
            2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37, 41, 43, 47, 53, 59, 61, 67, 71, 73, 79, 83, 89, 97
    };

    /**
     * We use scaled BigDecimals to represent factors, BASE equals "1.0"
     */
    private static final long BASE = 10000;

    /**
     * number or fractional digits
     */
    private static final int SCALE = ("" + BASE).length() - 1;

    private static final int CLOSEST_DIVISION_DEVIATION = 2;

    /**
     * every ratio will be displayed as either 1:m or m:1, m might not be an int
     */
    private boolean useRatiosTo1;

    /**
     * used to format float values
     */
    private final DecimalFormat numberFormat;

    public RatioFormatter() {
        this(Locale.GERMAN);
    }

    public RatioFormatter(Locale l) {
        this.numberFormat = (DecimalFormat) NumberFormat.getInstance(l);
    }

    public void setUseRatiosTo1(boolean useRatiosTo1) {
        this.useRatiosTo1 = useRatiosTo1;
    }

    /**
     * Sets how floats should be formatted for which no acceptable ratio could be
     * determined
     * @param pattern as required by NumberFormat.
     */
    synchronized public void setNumberFormat(String pattern) {
        this.numberFormat.applyLocalizedPattern(pattern);
    }

    /**
     * Formats the given float as a ratio (that is, "n:m") if possible.
     * @param f to be formatted
     * @return formatted representation of f
     */
    public String format(double f) {
        return format(new BigDecimal(f));
    }

    public String format(BigDecimal bd) {
        final long factor = toScaledLong(bd);
        String result = getFractionByPrimeFactors(factor);
        if (result == null) {
            result = getFractionFromClosestDivision(factor);
        }
        return (result != null) ? result : formatNumber(bd);
    }

    synchronized private String formatNumber(double f) {
        return this.numberFormat.format(f);
    }

    synchronized private String formatNumber(BigDecimal f) {
        return this.numberFormat.format(f);
    }

    /**
     * Turns a BigDecimal into a long value, only the first SCALE fractional digits
     * are taken into account (if d is 12.345 the result will be 123450 if scale is 4).
     * @param bd to be converted
     * @return d as a long value.
     */
    private long toScaledLong(BigDecimal bd) {
        bd = bd.setScale(SCALE, BigDecimal.ROUND_HALF_DOWN);
        return bd.movePointRight(SCALE).longValue();
    }

    /**
     * Each split factor n.xyz can be represented as a fraction k/m. Divided by the
     * greatest common denominator (gcd) of k and m, we might get a nice ratio. The
     * result can be found by dividing both k and m by their common prime factors
     * (multiplying all those factors yields the gcd).
     * @param factor
     * @return factor as "n:m" or null if only "weird" results are found
     */
    private String getFractionByPrimeFactors(long factor) {
        long z = factor;
        long n = BASE;
        int i = 0;
        while (i < PRIMES.length && PRIMES[i] <= z && PRIMES[i] <= n) {
            while (z % PRIMES[i] == 0 && n % PRIMES[i] == 0) {
                z /= PRIMES[i];
                n /= PRIMES[i];
            }
            i++;
        }
        // ignore "weird" results
        if (z > 1 && n > 100 || z > 1000) {
            return null;
        }
        return getRatio(z, n);
    }

    /**
     * Find a pair of numbers n,m such that n divided by m is not too far away from
     * the factor we are examining
     * @param factor
     * @return "n:m" such that Math.abs(n/m - factor) < CLOSEST_DIVISION_DEVIATION;
     */
    private String getFractionFromClosestDivision(long factor) {
        long z = 0;
        long n = 0;
        long minDiff = Long.MAX_VALUE;
        for (long i = 1; i < 30; i++) {
            long lower = (i * BASE) / factor;
            if (i > 3 && lower > 100) {
                continue;
            }

            for (long j = lower; j < lower + 2; j++) {
                if (j == 0) {
                    continue;
                }
                long diff = Math.abs((i * BASE) / j - factor);
                if (diff < minDiff) {
                    minDiff = diff;
                    z = i;
                    n = j;
                }
            }
        }
        if (minDiff > CLOSEST_DIVISION_DEVIATION) {
            return null;
        }

        return getRatio(z, n);
    }

    private String getRatio(long z, long n) {
        if (this.useRatiosTo1 && z != 1L && n != 1L) {
            if (z < n) {
                return formatNumber((double) n / (double) z) + ":1";
            }
            else {
                return "1:" + formatNumber((double) z / (double) n);
            }
        }
        // swap n and z as it the standard display style
        return n + ":" + z;
    }
}
