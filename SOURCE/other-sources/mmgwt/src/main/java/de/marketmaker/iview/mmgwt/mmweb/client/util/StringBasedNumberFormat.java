package de.marketmaker.iview.mmgwt.mmweb.client.util;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.NumberFormat;

/**
 * @author umaurer
 */
public class StringBasedNumberFormat {
    public static final char DEFAULT_GROUPING_SEPARATOR;

    public static final char DEFAULT_DECIMAL_SEPARATOR;

    static {
        if (GWT.isClient()) {
            final String formatted = NumberFormat.getDecimalFormat().format(1234.5);
            DEFAULT_GROUPING_SEPARATOR = formatted.charAt(1);
            DEFAULT_DECIMAL_SEPARATOR = formatted.charAt(5);
        }
        else {
            DEFAULT_GROUPING_SEPARATOR = '.';
            DEFAULT_DECIMAL_SEPARATOR = ',';
        }
    }

    public static final StringBasedNumberFormat DEFAULT = create(2, -1, false);
    public static final StringBasedNumberFormat ROUND_2 = create(2, 2, false);
    public static final StringBasedNumberFormat ROUND_3 = create(3, 3, false);
    public static final StringBasedNumberFormat ROUND_0 = create(0, 0, false);
    public static final StringBasedNumberFormat ROUND_0_1 = create(0, 1, false);
    public static final StringBasedNumberFormat ROUND_0_2 = create(0, 2, false);
    public static final StringBasedNumberFormat ROUND_2_3 = create(2, 3, false);
    public static final StringBasedNumberFormat ROUND_2_5 = create(2, 5, false);
    public static final StringBasedNumberFormat ROUND_0_5 = create(0, 5, false);
    public static final StringBasedNumberFormat ROUND_2_3_PLUS = create(2, 3, true);

    private static final char[] PART_EMPTY = new char[0];
    private static final char[] PART_SIGN_MINUS = new char[]{'-'};
    private static final char[] PART_SIGN_PLUS = new char[]{'+'};
    private static final char[] PART_ZERO = new char[]{'0'};
    private static char[] partManyZeros = "0000000000".toCharArray(); // $NON-NLS-0$

    private final char decimalSeparator;
    private final char groupingSeparator;
    private final int groupingSize = 3;
    private final int minimumFractionSize;
    private final int maximumFractionSize;
    private final boolean addPlusSign;

    private static StringBasedNumberFormat create(int minimumFractionSize, int maximumFractionSize, boolean addPlusSign) {
        return new StringBasedNumberFormat(DEFAULT_DECIMAL_SEPARATOR, DEFAULT_GROUPING_SEPARATOR, 
                minimumFractionSize, maximumFractionSize, addPlusSign);    
    }
    
    private StringBasedNumberFormat(char decimalSeparator, char groupingSeparator, int minimumFractionSize, int maximumFractionSize, boolean addPlusSign) {
        this.decimalSeparator = decimalSeparator;
        this.groupingSeparator = groupingSeparator;
        assert(maximumFractionSize == -1 || minimumFractionSize <= maximumFractionSize);
        assert(minimumFractionSize >= 0);
        this.minimumFractionSize = minimumFractionSize;
        this.maximumFractionSize = maximumFractionSize;
        this.addPlusSign = addPlusSign;
    }

    public String format(double d) {
        return format(Double.toString(d), 0, null);
    }

    public String format(String number) {
        return format(number, 0, null);
    }

    public String format(String number, int shiftRightDecimalPlaces, String suffix) {
        final char[] src = number.toCharArray();
        final char[] signPart;
        final char[] intPart;
        final char[] fracPart;
        final char[] suffixPart = suffix == null ? PART_EMPTY : suffix.toCharArray();

        final int dotPos = number.indexOf('.');
        final int intStart;
        final char firstChar = number.charAt(0);
        if (firstChar == '-') {
            intStart = 1;
            signPart = PART_SIGN_MINUS;
        }
        else {
            intStart = 0;
            signPart = (this.addPlusSign && !"0".equals(number)) ? PART_SIGN_PLUS: PART_EMPTY; // $NON-NLS-0$
        }
        if (dotPos == -1) {
            intPart = new char[src.length - intStart];
            System.arraycopy(src, intStart, intPart, 0, intPart.length);
            fracPart = PART_EMPTY;
        }
        else {
            intPart = new char[dotPos - intStart];
            System.arraycopy(src, intStart, intPart, 0, intPart.length);
            fracPart = new char[src.length - dotPos - 1];
            System.arraycopy(src, dotPos + 1, fracPart, 0, fracPart.length);
        }

        return formatShifted(signPart, intPart, fracPart, suffixPart, shiftRightDecimalPlaces);
    }

    private String formatShifted(char[] signPart, char[] intPart, char[] fracPart, char[] suffixPart, int shiftRightDecimalPlaces) {
        if (shiftRightDecimalPlaces == 0) {
            return formatAdaptSize(signPart, intPart, fracPart, suffixPart);
        }

        final int intLength = intPart.length;
        final int fracLength = fracPart.length;
        if (shiftRightDecimalPlaces > 0) { // shift right
            if (fracLength > shiftRightDecimalPlaces) {
                char[] newIntPart = new char[intLength + shiftRightDecimalPlaces];
                char[] newFracPart = new char[fracLength - shiftRightDecimalPlaces];
                System.arraycopy(intPart, 0, newIntPart, 0, intLength);
                System.arraycopy(fracPart, 0, newIntPart, intLength, shiftRightDecimalPlaces);
                System.arraycopy(fracPart, shiftRightDecimalPlaces, newFracPart, 0, newFracPart.length);
                return formatAdaptSize(signPart, newIntPart, newFracPart, suffixPart);
            }
            else {
                char[] newIntPart = new char[intLength + shiftRightDecimalPlaces];
                System.arraycopy(intPart, 0, newIntPart, 0, intLength);
                System.arraycopy(fracPart, 0, newIntPart, intLength, fracLength);
                if (fracLength < shiftRightDecimalPlaces) {
                    final char[] zeros = getManyZeros(shiftRightDecimalPlaces);
                    System.arraycopy(zeros, 0, newIntPart, intLength + fracLength, shiftRightDecimalPlaces - fracLength);
                }
                return formatAdaptSize(signPart, newIntPart, PART_ZERO, suffixPart);
            }
        }
        else { // shift left
            final int shiftLeft = -shiftRightDecimalPlaces;
            if (intLength > shiftLeft) {
                char[] newIntPart = new char[intLength - shiftLeft];
                char[] newFracPart = new char[fracLength + shiftLeft];
                System.arraycopy(intPart, 0, newIntPart, 0, newIntPart.length);
                System.arraycopy(intPart, newIntPart.length, newFracPart, 0, shiftLeft);
                System.arraycopy(fracPart, 0, newFracPart, shiftLeft, fracLength);
                return formatAdaptSize(signPart, newIntPart, newFracPart, suffixPart);
            }
            else {
                char[] newFracPart = new char[fracLength + shiftLeft];
                if (intLength < shiftLeft) {
                    final char[] zeros = getManyZeros(shiftLeft);
                    System.arraycopy(zeros, 0, newFracPart, 0, shiftLeft - intLength);
                }
                System.arraycopy(intPart, 0, newFracPart, shiftLeft - intLength, intLength);
                System.arraycopy(fracPart, 0, newFracPart, shiftLeft, fracLength);
                return formatAdaptSize(signPart, PART_ZERO, newFracPart, suffixPart);
            }
        }
    }

    private String formatAdaptSize(char[] signPart, char[] intPart, char[] fracPart, char[] suffixPart) {
        if (fracPart.length < this.minimumFractionSize) {
            return formatAddZero(signPart, intPart, fracPart, suffixPart);
        }
        if ((this.maximumFractionSize >= 0 && fracPart.length > this.maximumFractionSize)) {
            if (fracPart[this.maximumFractionSize] < '5') {
                return formatFixSize(signPart, intPart, fracPart, suffixPart);
            }
            return formatRounded(signPart, intPart, fracPart, suffixPart);
        }
        return format(signPart, intPart, fracPart, suffixPart);
    }


    private String formatAddZero(char[] signPart, char[] intPart, char[] fracPart, char[] suffixPart) {
        final char[] newFracPart = new char[this.minimumFractionSize];
        System.arraycopy(fracPart, 0, newFracPart, 0, fracPart.length);
        for (int i = fracPart.length, len = newFracPart.length; i < len; i++) {
            newFracPart[i] = '0';
        }
        return format(signPart, intPart, newFracPart, suffixPart);
    }

    private String formatFixSize(char[] signPart, char[] intPart, char[] fracPart, char[] suffixPart) {
        final char[] newFracPart = new char[this.maximumFractionSize];
        System.arraycopy(fracPart, 0, newFracPart, 0, this.maximumFractionSize);
        return formatRemoveSignIfZero(signPart, intPart, newFracPart, suffixPart);
    }

    private String formatRounded(char[] signPart, char[] intPart, char[] fracPart, char[] suffixPart) {
        final char[] newFracPart = new char[this.maximumFractionSize];
        boolean overflow = true;
        for (int i = this.maximumFractionSize - 1; i >= 0; i--) {
            newFracPart[i] = overflow ? (char)(fracPart[i] + 1) : fracPart[i];
            overflow = newFracPart[i] > '9';
            if (overflow) {
                newFracPart[i] = '0';
            }
        }
        if (overflow) {
            for (int i = intPart.length - 1; i >= 0; i--) {
                intPart[i] = overflow ? (char)(intPart[i] + 1) : intPart[i];
                overflow = intPart[i] > '9';
                if (overflow) {
                    intPart[i] = '0';
                }
            }
            if (overflow) {
                final char[] newIntPart = new char[intPart.length + 1];
                newIntPart[0] = '1';
                System.arraycopy(intPart, 0, newIntPart, 1, intPart.length);
                intPart = newIntPart;
            }
        }
        return formatRemoveSignIfZero(signPart, intPart, newFracPart, suffixPart);
    }

    private char[] getManyZeros(int count) {
        if (partManyZeros.length >= count) {
            return partManyZeros;
        }
        char[] buf = new char[count];
        for (int i = buf.length - 1; i >= 0; i--) {
            buf[i] = '0';
        }
        partManyZeros = buf;
        return buf;
    }

    private String formatRemoveSignIfZero(char[] signPart, char[] intPart, char[] fracPart, char[] suffixPart) {
        if (signPart.length > 0 && isZero(intPart, fracPart)) {
            signPart = PART_EMPTY;
        }
        return format(signPart, intPart, fracPart, suffixPart);
    }

    private boolean isZero(char[] intPart, char[] fracPart) {
        if (intPart.length != 1 || intPart[0] != '0') {
            return false;
        }
        for (char c : fracPart) {
            if (c != '0') {
                return false;
            }
        }
        return true;
    }

    private String format(char[] signPart, char[] intPart, char[] fracPart, char[] suffixPart) {
        int intLength = intPart.length;
        int intStart = 0;
        // remove leading zeros
        while (intLength > 1 && intPart[intStart] == '0') {
            intStart++;
            intLength--;
        }
        final int signLength = signPart.length;
        final int groupingSeparatorCount = (intLength - 1) / this.groupingSize;
        int fracLength = fracPart.length;
        // remove trailing zeros
        while (fracLength > this.minimumFractionSize && fracPart[fracLength - 1] == '0') {
            fracLength--;
        }
        int suffixLength = suffixPart.length;
        final int decimalSeparatorLength = fracLength == 0 ? 0 : 1;
        int nextSeparator = intLength % this.groupingSize;
        if (nextSeparator == 0) {
            nextSeparator = this.groupingSize;
        }
        char[] dest = new char[signLength + intLength + groupingSeparatorCount + decimalSeparatorLength + fracLength + suffixLength];
        int pos = 0;
        if (signLength > 0) {
            System.arraycopy(signPart, 0, dest, 0, signLength);
            pos += signLength;
        }
        int sourcePos = intStart;
        System.arraycopy(intPart, sourcePos, dest, pos, nextSeparator);
        pos += nextSeparator;
        sourcePos += nextSeparator;
        while (sourcePos < intLength) {
            dest[pos++] = this.groupingSeparator;
            System.arraycopy(intPart, sourcePos, dest, pos, this.groupingSize);
            pos += this.groupingSize;
            sourcePos += this.groupingSize;
        }

        if (fracLength > 0) {
            dest[pos++] = this.decimalSeparator;
            System.arraycopy(fracPart, 0, dest, pos, fracLength);
            pos += fracLength;
        }

        if (suffixLength > 0) {
            System.arraycopy(suffixPart, 0, dest, pos, suffixLength);
        }
        return new String(dest);
    }

    public String parseToString(String value) {
        return value.replace(String.valueOf(this.groupingSeparator), ""); // $NON-NLS-0$
    }
}
