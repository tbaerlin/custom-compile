/*
 * NumberUtil.java
 *
 * Created on 13.12.2004 13:48:56
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.common.util;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class NumberUtil {

    private static final double LOG_1024 = Math.log(1024d);

    private static long KILO = 1024;

    private static long MEGA = 1024 * KILO;

    private static long GIGA = 1024 * MEGA;

    private static long TERA = 1024 * GIGA;

    private static long PETA = 1024 * TERA;

    private static long EXA = 1024 * PETA;

    private static final DecimalFormat DF;

    static {
        DF = (DecimalFormat) NumberFormat.getInstance(Locale.US);
        DF.applyLocalizedPattern("0.0");
    }


    private NumberUtil() {
    }

    public static int parseInt(String size) {
        long l = parseLong(size);
        if (l < Integer.MIN_VALUE || l > Integer.MAX_VALUE) {
            throw new NumberFormatException("exceeds int range " + l);
        }
        return (int) l;
    }

    public static long parseLong(String size) {
        char ch = Character.toLowerCase(size.charAt(size.length() - 1));
        if (Character.isDigit(ch)) {
            return Long.parseLong(size);
        }
        final long factor;
        switch (ch) {
            case 'k':
                factor = KILO;
                break;
            case 'm':
                factor = MEGA;
                break;
            case 'g':
                factor = GIGA;
                break;
            default:
                throw new IllegalArgumentException(size);
        }
        return Long.parseLong(size.substring(0, size.length() - 1)) * factor;
    }

    public static String prettyPrint(long l) {
        if (l < 1024) {
            return l + " B";
        }
        return prettyPrint(l, 1024);

    }

    private static String prettyPrint(long oldL, long factor) {
        long l = oldL / 1024;
        if ((l < 1024) || (factor == EXA)) {
            final String unit;
            if (factor == EXA) {
                unit = "EB";
            }
            else if (factor == PETA) {
                unit = "PB";
            }
            else if (factor == TERA) {
                unit = "TB";
            }
            else if (factor == GIGA) {
                unit = "GB";
            }
            else if (factor == MEGA) {
                unit = "MB";
            }
            else {
                unit = "KB";
            }
            if (l < 10) {
                final float f = (float) oldL / 1024;
                return DF.format(f) + " " + unit;
            }
            else {
                return l + " " + unit;
            }
        }
        return prettyPrint(l, factor * 1024);
    }

    public static String humanReadableByteCount(long bytes) {
        if (bytes < 1024L) {
            return Long.toString(bytes);
        }
        int exp = (int) (Math.log(bytes) / LOG_1024);
        return String.format(Locale.US, "%.1f%s", bytes / Math.pow(1024d, exp), "kmgtpe".charAt(exp - 1));
    }
}
