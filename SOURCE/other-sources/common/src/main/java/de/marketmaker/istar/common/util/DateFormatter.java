/*
 * DateFormat.java
 *
 * Created on 28.04.2005 13:23:08
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.common.util;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class DateFormatter {

    public static class Invoker {
        private Invoker() {
        }

        public String format(int date) {
            return DateFormatter.formatYyyymmdd(date);
        }
    }

    public static final DateFormatter.Invoker FORMATTER = new DateFormatter.Invoker();

    private DateFormatter() {
    }

    /**
     * Formats a value representing yyyymmdd as 'yyyy-mm-dd'
     * @param yyyymmdd to be formatted
     * @return formatted time
     * @throws IllegalArgumentException if secondsInDay is invalid
     */
    public static String formatYyyymmdd(int yyyymmdd) {
        if (yyyymmdd <= 0) {
            return "n/a [" + yyyymmdd + "]";
        }

        final char[] c = new char[10];

        final int yyyy = yyyymmdd / 10000;
        c[0] = (char) ('0' + (yyyy / 1000));
        c[1] = (char) ('0' + ((yyyy / 100) % 10));
        c[2] = (char) ('0' + ((yyyy / 10) % 10));
        c[3] = (char) ('0' + (yyyy % 10));
        c[4] = '-';

        final int mm = (yyyymmdd - yyyy * 10000) / 100;
        c[5] = (char) ('0' + (mm / 10));
        c[6] = (char) ('0' + (mm % 10));
        c[7] = '-';

        final int dd = yyyymmdd % 100;
        c[8] = (char) ('0' + (dd / 10));
        c[9] = (char) ('0' + (dd % 10));

        return new String(c);
    }

}
