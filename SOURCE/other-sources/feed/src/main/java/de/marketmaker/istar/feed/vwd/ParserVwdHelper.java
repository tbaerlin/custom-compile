/*
 * ParserVwdHelper.java
 *
 * Created on 31.07.2002 14:43:31
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.vwd;

import de.marketmaker.istar.common.util.StringAssembler;
import de.marketmaker.istar.common.util.PriceCoder;

/**
 * Static helper methods for parsing fields from the vwd feed.
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 * @version $Id: ParserVwdHelper.java,v 1.1 2004/11/17 15:14:14 tkiesgen Exp $
 */
public class ParserVwdHelper {

    /** constant for character 0 */
    private static final byte ZERO = (byte) '0';
    /** constant for character - */
    private static final byte MINUS = (byte) '-';
    /** constant for character : */
    private static final byte COLON = (byte) ':';
    /** constant for character . */
    private static final byte DOT = (byte) '.';
    /** constant for character ' ' */
    private static final byte SPACE = (byte) ' ';

    /** private constructor, no instance allowed */
    private ParserVwdHelper() {
    }

    /**
     * Extract time value from the given byte array, which is supposed to contain
     * 8 bytes: HH.mm.ss
     * @param b contains time to be parsed
     * @param len length of parse array, should be 8
     * @return parsed time as seconds in day (i.e., in 0..86399)
     * @throws IllegalArgumentException if value in parsearray is not well formed.
     */
    public static int getTimeAsSecondsInDay(final byte[] b, final int start,
            final int len) {
        if (len != 8 || b[start + 2] != COLON || b[start + 5] != COLON) {
            throw new IllegalArgumentException("Expected HH:mm:ss but got: -"
                    + getString(b, start, len) + "-");
        }

        final int hh = getInt(b, start, 2);
        final int mm = getInt(b, start + 3, 2);
        final int ss = getInt(b, start + 6, 2);

        if (hh > 23 || mm > 59 || ss > 59) {
            throw new IllegalArgumentException("Illegal time: -"
                    + getString(b, start, len) + "-");
        }

        return (hh * 3600) + (mm * 60) + ss;
    }

    /**
     * Extract date value from the given byte array, which is supposed to contain
     * 10 bytes: dd.MM.yyyy or yyyy.MM.dd
     * @param b contains date to be parsed
     * @param len length of parse array, should be 10
     * @return parsed date as an int yyyyMMdd
     * @throws IllegalArgumentException if value in parsearray is not well formed.
     */
    public static int getDateAsYyyymmdd(final byte[] b, final int start, final int len) {
        if (len != 10) {
            throw new IllegalArgumentException("Expected dd.MM.yyyy but got: "
                + getString(b, start, len));
        }

        final int dd;
        final int mm;
        final int yyyy;

        if (b[start+2] == DOT && b[start+5] == DOT) {
            if (b[start] == SPACE) {
                b[start] = ZERO;
            }
            dd = getInt(b, start, 2);
            mm = getInt(b, start + 3, 2);
            yyyy = getInt(b, start + 6, 4);
        }
        else if (b[start+4] == DOT && b[start+7] == DOT) {
            yyyy = getInt(b, start, 4);
            mm = getInt(b, start + 5, 2);
            dd = getInt(b, start + 8, 2);
        }
        else {
            throw new IllegalArgumentException("Expected dd.MM.yyyy but got: "
                + getString(b, start, len));
        }


        if (dd > 31 || mm > 12 || (yyyy < 1800 && yyyy > 0)) {
            throw new IllegalArgumentException("Illegal date: -"
                + getString(b, start, len) + "-");
        }

        return (yyyy * 10000) + (mm * 100) + dd;
    }
    /**
     * Extract date value from the given byte array, which is supposed to contain
     * 10 bytes: yyyy.MM.dd
     * @param b contains date to be parsed
     * @param len length of parse array, should be 10
     * @return parsed date as an int yyyyMMdd
     * @throws IllegalArgumentException if value in parsearray is not well formed.
     */
    public static int getDateChicagoDpAsYyyymmdd(final byte[] b, final int start, final int len) {
        if (len != 10 || b[start+4] != DOT || b[start+7] != DOT) {
            throw new IllegalArgumentException("Expected yyyy.MM.dd but got: "
                + getString(b, start, len));
        }

        if (b[start] == SPACE) {
            b[start] = ZERO;
        }

        final int yyyy = getInt(b, start, 4);
        final int mm = getInt(b, start + 5, 2);
        final int dd = getInt(b, start+8, 2);

        if (dd > 31 || mm > 12 || (yyyy < 1800 && yyyy > 0)) {
            throw new IllegalArgumentException("Illegal date: -"
                + getString(b, start, len) + "-");
        }

        return (yyyy * 10000) + (mm * 100) + dd;
    }

    /**
     * Helper print for printing contents of byte array
     * @param parsearray what to print
     * @param len how many bytes to print
     * @return String representation of parsearray
     */
    private static String getString(final byte[] parsearray, final int start, final int len) {
        final StringAssembler sa = new StringAssembler(len);
        for (int i = start; i < start+len; i++) {
            sa.append(parsearray[i]);
        }
        return sa.toString();
    }


    /**
     * Returns 3-digit integer that is represented by b1,b2,b3 where
     * b1 is most-significant.<br>
     * Example: getInt('8','5','3') returns 853
     * @param b1 1st digit
     * @param b2 2nd digit
     * @param b3 3rd digit
     * @return int represented by b1,b2,b3
     */
    public static int getFieldId(final byte b1, final byte b2, final byte b3) {
        return (b3 - ZERO) + (b2 - ZERO) * 10 + (b1 - ZERO) * 100;
    }


    /**
     * Returns an int value obtained by parsing bytes that contain an ascii representation
     * of the int, possibly preceded by a '-' for a negative number.
     * @param parsearray contains ascii data
     * @param arrayStart index of value's first byte
     * @param length number of bytes to parse
     * @return parsed number
     */
    public static int getInt(final byte[] parsearray,
                                   final int arrayStart, final int length) {
        int result = 0;
        int factor = 1;

        final int signum = (parsearray[arrayStart] == MINUS) ? -1 : 1;
        final int first = arrayStart + (signum == -1 ? 1 : 0);

        for (int i = arrayStart+length - 1; i >= first; i--) {
            result += ((parsearray[i] - ZERO) * factor);
            factor *= 10;
        }

        return signum * result;
    }

    /**
     * Returns a price value for a price encoded in an ascii representation
     * @param parsearray source for the price, price starts with byte at pos start
     * @param length index of first byte that should not be read
     * @return parsed price
     */
    public static long getPriceAsLong(final byte[] parsearray, final int start, final int length) {
        int dotPos = start + length - 3; // heuristic, usually prices are xyz.ab
        if (dotPos < start || parsearray[dotPos] != DOT) {
            dotPos = start + length - 1;
            while (dotPos >= start && parsearray[dotPos] != DOT) {
                dotPos--;
            }
        }

        final int end = (dotPos < start) ? start + length : dotPos;

        final int decimal = getInt(parsearray, start, end - start);
        final boolean isNegative = (decimal < 0) || parsearray[start] == MINUS;


        if (end != (start + length) && dotPos != (start + length)) {
            // add fraction
            dotPos++; // set to one after dotPos, because dot itself is not of interest
            final int numFracts = Math.min(start + length - dotPos, 8);
            final int fract = isNegative
                    ? -getInt(parsearray, dotPos, numFracts)
                    : getInt(parsearray, dotPos, numFracts);

            return PriceCoder.encode(decimal, fract, numFracts);
        }
        return PriceCoder.encode(decimal, 0, 0);
    }
}
