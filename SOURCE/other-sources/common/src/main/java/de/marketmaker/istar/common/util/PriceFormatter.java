/*
 * PriceFormatter.java
 *
 * Created on 28.04.2005 13:35:27
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.common.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class PriceFormatter {
    private final static long[] DECIMAL_BASE = new long[]{
        1,
        1000,
        1000 * 1000,
        1000 * 1000 * 1000
    };

    private final static int MAX_SIZE = 32;

    private char[] chars = new char[MAX_SIZE];

    private final int scale;

    /** number of least significant digits to be discarded */
    private final int ignore;

    private final int numDecimal;

    private char decimalSeparator = '.';

    private char groupingSymbol = ',';

    private boolean useGroupingSymbol = false;

    private boolean showTrailingZeros = true;

    private int groupingSize = 3;

    private String na = "n/a";
    private int decimalBaseIndex;

    public PriceFormatter() {
        this(5, 3);
    }

    public PriceFormatter(int scale, int numDecimal) {
        this(scale, numDecimal, 0);
    }

    public PriceFormatter(int scale, int numDecimal, int decimalBaseIndex) {
        if (scale < 0 || numDecimal < 0) {
            throw new IllegalArgumentException();
        }
        this.scale = scale;
        this.numDecimal = numDecimal;
        this.ignore = Math.max(0, scale - numDecimal);

        this.decimalBaseIndex = Math.max(0,
                Math.min(DECIMAL_BASE.length - 1, decimalBaseIndex));
    }

    public String toString() {
        return "PriceFormatter["
                + "scale=" + this.scale
                + ", ignore=" + this.ignore
                + ", decimalBaseIndex=" + this.decimalBaseIndex
                + ", decSep=" + this.decimalSeparator
                + ", groupSym=" + this.groupingSymbol
                + ", useGroup=" + (this.useGroupingSymbol ? "y" : "n")
                + ", groupSize=" + this.groupingSize
                + ", trailingZeros=" + (this.showTrailingZeros ? "y" : "n")
                + ", n/a=" + this.na
                + "]";
    }

    public void setNa(String na) {
        this.na = na;
    }

    public void setDecimalSeparator(String s) {
        this.decimalSeparator = s.charAt(0);
    }

    public void setGroupingSymbol(String s) {
        this.groupingSymbol = s.charAt(0);
    }

    public boolean isUseGroupingSymbol() {
        return this.useGroupingSymbol;
    }

    public void setGroupingSize(int groupingSize) {
        this.groupingSize = groupingSize;
    }

    public void setUseGroupingSymbol(boolean useGroupingSymbol) {
        this.useGroupingSymbol = useGroupingSymbol;
    }

    public void setShowTrailingZeros(boolean showTrailingZeros) {
        this.showTrailingZeros = showTrailingZeros;
    }

    public boolean isShowTrailingZeros() {
        return this.showTrailingZeros;
    }

    public String formatPrice(Number price) {
        if (isNa(price)) {
            return this.na;
        }
        return format(price.longValue());
    }

    public String formatPrice(BigDecimal bd) {
        if (bd.scale() > this.numDecimal) {
            bd = bd.setScale(this.numDecimal, RoundingMode.HALF_UP);
        }
        final StringBuilder sb = new StringBuilder(bd.toPlainString());
        final int dotPos = sb.indexOf(".");
        if (this.decimalSeparator != '.' && dotPos != -1) {
            sb.setCharAt(dotPos, this.decimalSeparator);
        }
        if (!showTrailingZeros && dotPos != -1) {
            int i = sb.length();
            while (i > dotPos && (sb.charAt(i - 1) == '0' || sb.charAt(i - 1) == this.decimalSeparator)) {
                i--;
            }
            sb.setLength(i);
        }
        final int start = sb.charAt(0) == '-' ? 1 : 0;
        final int end = (dotPos != -1) ? dotPos : sb.length();
        if (useGroupingSymbol && end > (start + this.groupingSize)) {
            int n = end - this.groupingSize;
            do {
                sb.insert(n, this.groupingSymbol);
                n -= this.groupingSize;
            } while (n > start);
        }
        return sb.toString();
    }

    private boolean isNa(Number price) {
        return price == null
                || price.longValue() == Long.MAX_VALUE
                || price.longValue() == Long.MIN_VALUE
                || price.intValue() == Integer.MAX_VALUE
                || price.intValue() == Integer.MIN_VALUE;
    }

    public String formatPercent(Number n) {
        if (isNa(n)) {
            return this.na;
        }
        return format(n, 100) + "%";
    }

    public String formatTimes100(Number n) {
        if (isNa(n)) {
            return this.na;
        }
        return format(n, 100);
    }

    public String format(Number n, int factor) {
        if (isNa(n)) {
            return this.na;
        }
        return format(n.longValue() * factor);
    }

    private String format(long value) {
        final long l = value / DECIMAL_BASE[this.decimalBaseIndex];

        int offset = MAX_SIZE - 1;
        int count = 0;
        int nextGroup = this.scale + this.groupingSize;
        int decimalSeparatorPos = -1;

        long tmp = l < 0L ? -l : l;

        while (tmp != 0L) {
            if (this.useGroupingSymbol && count == nextGroup) {
                this.chars[offset--] = this.groupingSymbol;
                nextGroup += this.groupingSize;
            }
            count++;
            long digit = tmp % 10L;

            if (count > this.ignore) {
                this.chars[offset--] = (char) (digit + '0');
            }
            if (count == this.scale && this.scale != this.ignore) {
                decimalSeparatorPos = offset;
                this.chars[offset--] = this.decimalSeparator;
            }
            tmp /= 10L;
        }

        while (count <= this.scale) {
            if (count++ == this.scale && this.scale != this.ignore && this.chars[offset + 1] != this.decimalSeparator) {
                decimalSeparatorPos = offset;
                this.chars[offset--] = this.decimalSeparator;
            }
            if (count > this.ignore) {
                this.chars[offset--] = '0';
            }
        }

        if (l < 0L) {
            // if the format results in a zero value, do not prepend a '-'. The format results in
            // zero if every char formatted so far is either '0' or the decimalSeparator.
            int i = MAX_SIZE - 1;
            while (i > offset && (this.chars[i] == '0' || this.chars[i] == this.decimalSeparator)) {
                i--;
            }
            if (i > offset) {
                this.chars[offset--] = '-';
            }
        }

        int length = MAX_SIZE - offset - 1;

        if (!this.showTrailingZeros && (decimalSeparatorPos != -1)) {
            int i = MAX_SIZE;
            do {
                i--;
            }
            while ((this.chars[i] == '0' || this.chars[i] == this.decimalSeparator)
                    && i >= decimalSeparatorPos);
            length = i - offset;
        }

        return new String(this.chars, offset + 1, length);
    }

    public String getDecimalBaseLabel(String[] labels) {
        return (this.decimalBaseIndex < labels.length)
            ? labels[this.decimalBaseIndex] : "";
    }
}
