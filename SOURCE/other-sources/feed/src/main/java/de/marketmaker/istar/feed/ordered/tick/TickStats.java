/*
 * TickStats.java
 *
 * Created on 22.01.15 08:20
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.ordered.tick;

/**
 * Functions on tick statistics. Tick statistics are stored in a single int value for performance
 * reasons {@link de.marketmaker.istar.feed.ordered.tick.OrderedTickData#tickStats}.
 * In addition to storing the approximate tick size of the past 6 days, which requires 30 bit,
 * the uppermost 2 bits can be used to store an "idle" count from 0..3, which can be used to
 * adjust the size returned by {@link OrderedTickData#getAvgLength()}.
 *
 * @author oflege
 */
final class TickStats {

    static final int STATS_MASK = 0x3FFFFFFF;

    static String toString(int stats) {
        StringBuilder sb = new StringBuilder(16);
        sb.append(idleCount(stats) & 0x3);
        int n = withoutIdleBits(stats);
        while (n != 0) {
            sb.append(sb.length() == 1 ? ':' : ',').append(n & 0x1F);
            n >>= 5;
        }
        return sb.toString();
    }
    
    static int maxLength(int stats, int length) {
        return 1 << max(stats, lengthLog2(length));
    }

    static int max(int stats) {
        return max(stats, 0);
    }

    private static int max(int stats, int initialValue) {
        int result = initialValue;
        int n = withoutIdleBits(stats);
        while (n != 0) {
            result = Math.max(result, n & 0x1F);
            n >>= 5;
        }
        return result;
    }

    static int withoutIdleBits(int stats) {
        return stats & STATS_MASK;
    }

    static int withIdleBits(int stats, int idleBits) {
        return (stats & STATS_MASK) | idleBits;
    }

    static int update(int stats, int length) {
        final int encodedLength = lengthLog2(length);
        if (encodedLength == 0) {
            return stats;
        }
        return ((stats << 5) & STATS_MASK) + encodedLength;
    }

    static int lengthLog2(int size) {
        if (size <= 0) {
            return 0;
        }
        return 32 - Integer.numberOfLeadingZeros(size);
    }

    static int toIdleBits(int n) {
        if (n < 0 || n > 3) {
            throw new IllegalArgumentException(n + "not in [0..3]");
        }
        return n << 30;
    }

    static int idleCount(int n) {
        return n >>> 30;
    }

    static int decIdleBits(int n) {
        int m = idleCount(n);
        if (m == 0) {
            return n;
        }
        return ((m - 1) << 30) | (n & STATS_MASK);
    }

    private TickStats() {
        throw new UnsupportedOperationException();
    }
}
