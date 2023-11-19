/*
 * TimeFormatter.java
 *
 * Created on 29.11.2004 07:52:47
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.common.util;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class TimeFormatter {

    private static final int SECONDS_IN_DAY = 86400;

    /**
     * Used to be able to invoke TimeFormatter static functions from within a
     * Velocity context.
     */
    public static class Invoker {
        private Invoker() {
        }

        public String formatHHMM(Number time) {
            return TimeFormatter.formatHHMM(time);
        }

        public String formatHHMMSS(Number time) {
            return TimeFormatter.formatHHMMSS(time);
        }

        public String formatSecondsInDay(int secondsInDay) {
            return TimeFormatter.formatSecondsInDay(secondsInDay);
        }
    }

    public static final Invoker FORMATTER = new Invoker();


    private TimeFormatter() {
    }

    private static boolean isNa(Number time) {
        return (time == null) || (time.longValue() < 0L) || (time.longValue() >= SECONDS_IN_DAY);
    }

    public static String formatHHMM(Number time) {
        if (isNa(time)) {
            return "--";
        }
        return formatHHMMSS(time).substring(0, 5);
    }

    public static String formatHHMMSS(Number time) {
        if (isNa(time)) {
            return "--";
        }
        return formatSecondsInDay(time.intValue());
    }

    /**
     * Formats a value representing the number of seconds passed in a day as 'hh:mm:ss'
     * @param secondsInDay to be formatted
     * @return formatted time
     * @throws IllegalArgumentException if secondsInDay is invalid
     */
    public static String formatSecondsInDay(int secondsInDay) {
        if (secondsInDay < 0 || secondsInDay >= SECONDS_IN_DAY) {
            return "n/a [" + secondsInDay + "]";
        }
        final char[] c = new char[8];
        appendTime(secondsInDay, c);
        return new String(c);
    }

    public static String formatSecondsInDay(int secondsInDay, int millis) {
        final char[] c = new char[12];
        appendTime(secondsInDay, c);
        c[8] = '.';
        c[9] = (char) ('0' + ((millis / 100) % 10));
        c[10] = (char) ('0' + ((millis / 10) % 10));
        c[11] = (char) ('0' + (millis % 10));
        return new String(c);
    }

    private static void appendTime(int secondsInDay, char[] c) {
        final int hour = secondsInDay / 3600;
        c[0] = (char) ('0' + (hour / 10));
        c[1] = (char) ('0' + (hour % 10));
        c[2] = ':';

        final int min = (secondsInDay % 3600) / 60;
        c[3] = (char) ('0' + (min / 10));
        c[4] = (char) ('0' + (min % 10));
        c[5] = ':';

        final int sec = secondsInDay % 60;
        c[6] = (char) ('0' + (sec / 10));
        c[7] = (char) ('0' + (sec % 10));
    }
}