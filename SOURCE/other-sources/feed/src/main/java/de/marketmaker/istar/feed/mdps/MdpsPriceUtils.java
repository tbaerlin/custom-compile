/*
 * MdpsPriceUtils.java
 *
 * Created on 06.06.14 10:59
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.mdps;

import java.math.BigDecimal;
import java.util.regex.Pattern;

/**
 * Static utility methods for mdps prices encoded as long values. 
 * @author oflege
 */
public class MdpsPriceUtils {
    static final long[] FACTORS = new long[]{
            1, 10, 100, 1000, 10000, 100000, 1000000, 10000000, 100000000, 1000000000, 10000000000L
    };

    private static final long N_99999 = MdpsFeedUtils.encodePrice(99999, 0);

    private static final long N_9999 = MdpsFeedUtils.encodePrice(9999, 0);

    private static final long N_9900 = MdpsFeedUtils.encodePrice(9900, 0);

    private static final long N_999 = MdpsFeedUtils.encodePrice(999, 0);

    private static final long N_990 = MdpsFeedUtils.encodePrice(990, 0);

    private static final long N_99 = MdpsFeedUtils.encodePrice(99, 0);

    private static final long N_98 = MdpsFeedUtils.encodePrice(98, 0);

    private static final BigDecimal BD_99999 = new BigDecimal(99999);

    private static final BigDecimal BD_9999 = new BigDecimal(9999);

    private static final BigDecimal BD_9900 = new BigDecimal(9900);

    private static final BigDecimal BD_999 = new BigDecimal(999);

    private static final BigDecimal BD_990 = new BigDecimal(990);

    private static final BigDecimal BD_99 = new BigDecimal(99);

    private static final BigDecimal BD_98 = new BigDecimal(98);
    
    private static final Pattern NINETYNINER = Pattern.compile("99+0*");

    private static final BigDecimal RANGE_PCT = BigDecimal.valueOf(20, 2); // 20%

    public static int compare(long v1, long v2) {
        if (v1 == v2) {
            return 0;
        }
        int v1Scale = MdpsFeedUtils.getMdpsPriceScale(v1);
        int v2Scale = MdpsFeedUtils.getMdpsPriceScale(v2);
        if (v1Scale == v2Scale) {
            return Integer.compare((int) v1, (int) v2);
        }
        else if (v1Scale < v2Scale) {
            return Long.compare(MdpsFeedUtils.getMdpsPriceBase(v1),
                    MdpsFeedUtils.getMdpsPriceBase(v2) * FACTORS[v2Scale - v1Scale]);
        }
        else {
            return Long.compare(MdpsFeedUtils.getMdpsPriceBase(v1) * FACTORS[v1Scale - v2Scale],
                    MdpsFeedUtils.getMdpsPriceBase(v2));
        }
    }

    /**
     * @param ask valid ask price
     * @param bid valid bid price
     * @return mdps price with a value of <tt>(ask + bid) / 2</tt> encoded in a long value
     */
    public static long getSyntheticPrice(long ask, long bid) {
        int askScale = MdpsFeedUtils.getMdpsPriceScale(ask);
        int bidScale = MdpsFeedUtils.getMdpsPriceScale(bid);
        int scale = Math.min(askScale, bidScale);

        long unscaledAsk = MdpsFeedUtils.getMdpsPriceBase(ask);
        long unscaledBid = MdpsFeedUtils.getMdpsPriceBase(bid);

        if (askScale < bidScale) {
            unscaledBid = unscaledBid * FACTORS[bidScale - askScale];
        }
        else if (askScale > bidScale) {
            unscaledAsk = unscaledAsk * FACTORS[askScale - bidScale];
        }

        // dividing an uneven number by 2 requires on more digit to the right
        if ((unscaledAsk & 0x1) != (unscaledBid & 0x1)) {
            unscaledAsk *= 10;
            unscaledBid *= 10;
            scale--;
        }

        long sum = unscaledAsk + unscaledBid;
        while (sum > 0xFFFFFFFFL) { // int overflow
            sum /= 10;
            scale++;
        }
        return MdpsFeedUtils.encodePrice((int) (sum >> 1), scale);
    }

    /**
     * Heuristic to find out whether a given ask price indicates in fact an invalid ask.
     * @param ask to be tested
     * @param bid the corresponding bid of the ask to be checked, use 0 for an undefined bid
     * @return true iff ask = 99.99, 999.0, 9999, etc. T-19699
     */
    public static boolean isInvalidAsk(long ask, long bid) {
        if (compare(ask, N_98) <= 0) {
            return false;
        }
        final String str = Integer.toString((int) ask);
        if (!str.startsWith("99") || containsDigitOtherThanZeroOrNine(str)) {
            return false;
        }

        if (compare(ask, N_99999) >= 0 && str.startsWith("99999")) {
            return true;
        }
        if (compare(ask, N_9999) >= 0 && str.startsWith("9999")
                && (bid == 0 || compare(bid, N_9900) < 0)) {            
            return true;
        }
        if (compare(ask, N_999) >= 0 && str.startsWith("999")
                && (bid == 0 || compare(bid, N_990) < 0)) {            
            return true;
        }
        if (compare(ask, N_99) >= 0 && NINETYNINER.matcher(str).matches()
                && (bid == 0 || compare(bid, N_98) < 0)) {            
            return true;
        }
        return false;
    }

    /**
     * Equivalent of {@link #isInvalidAsk(long, long)} with BigDecimal parameters
     * @param ask to be checked
     * @param bid the corresponding bid of the ask to be checked, may be null
     * @param price the last available price, may be null
     * @return true iff ask is considered to be undefined.
     */
    public static boolean isInvalidAsk(BigDecimal ask, BigDecimal bid, BigDecimal price) {
        if (ask.compareTo(BD_98) <= 0) {
            return false;
        }
        final String str = Integer.toString(ask.unscaledValue().intValue());
        if (!str.startsWith("99") || containsDigitOtherThanZeroOrNine(str)) {
            return false;
        }
        if (price != null && price.signum() != 0 && isWithinRangeAroundPrice(ask, price)) {
            return false;
        }
        if (ask.compareTo(BD_99999) >= 0 && str.startsWith("99999")) {
            return true;
        }
        if (ask.compareTo(BD_9999) >= 0 && str.startsWith("9999")
                && (bid == null || bid.compareTo(BD_9900) < 0)) {
            return true;
        }
        if (ask.compareTo(BD_999) >= 0 && str.startsWith("999")
                && (bid == null || bid.compareTo(BD_990) < 0)) {
            return true;
        }
        if (ask.compareTo(BD_99) >= 0 && NINETYNINER.matcher(str).matches()
                && (bid == null || bid.compareTo(BD_98) < 0)) {
            return true;
        }
        return false;
    }

    private static boolean isWithinRangeAroundPrice(BigDecimal ask, BigDecimal price) {
        BigDecimal min = price.multiply(BigDecimal.ONE.subtract(RANGE_PCT));
        BigDecimal max = price.multiply(BigDecimal.ONE.add(RANGE_PCT));
        return ask.compareTo(min) > 0 && ask.compareTo(max) < 0;
    }

    protected static boolean containsDigitOtherThanZeroOrNine(String str) {
        for (int i = 2; i < str.length(); i++) {
            final char c = str.charAt(i);
            if (c > '0' && c < '9') {
                return true;
            }
        }
        return false;
    }

    public static void main(String[] args) {
        System.out.println(isInvalidAsk(new BigDecimal("999.75"), new BigDecimal("989.75"), new BigDecimal("994")));
    }
}
