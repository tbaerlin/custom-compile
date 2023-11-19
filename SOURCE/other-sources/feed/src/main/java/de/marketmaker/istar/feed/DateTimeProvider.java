/*
 * DayProvider.java
 *
 * Created on 08.12.2004 12:32:09
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed;

import net.jcip.annotations.Immutable;
import org.joda.time.DateTime;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public interface DateTimeProvider {
    @Immutable
    final class Timestamp {
        public final DateTime dateTime;

        public final int yyyyMmDd; // cached for performance

        public final int secondOfDay; // cached for performance

        public final int feedTimestamp;

        public Timestamp(DateTime dateTime) {
            this.dateTime = dateTime;
            this.secondOfDay = dateTime.getSecondOfDay();
            this.yyyyMmDd = dateTime.getYear() * 10000
                    + dateTime.getMonthOfYear() * 100
                    + dateTime.getDayOfMonth();
            this.feedTimestamp = encodeTimestamp(dateTime.getYear() - 2000,
                    dateTime.getMonthOfYear(), dateTime.getDayOfMonth(), this.secondOfDay);
        }

        private static int encodeTimestamp(int year, int month, int day, int secondOfDay) {
            return year << 26 | month << 22 | day << 17 | secondOfDay;
        }

        public static int encodeTimestamp(long ms) {
            final DateTime dt = new DateTime(ms);
            return encodeTimestamp(dt.getYear() - 2000,
                    dt.getMonthOfYear(), dt.getDayOfMonth(), dt.getSecondOfDay());
        }

        public static int encodeTimestamp(int yyyyMmDd, int secondOfDay) {
            return encodeTimestamp(yyyyMmDd / 10000 - 2000,
                    (yyyyMmDd % 10000) / 100, yyyyMmDd % 100, secondOfDay);
        }

        /**
         * Decodes the date in an encoded timestamp
         * @param value timestamp encoded just as feedTimestamp
         * @return value's date as yyyymmdd
         */
        public static int decodeDate(int value) {
            return (2000 + (value >> 26 & 0x3F)) * 10000
                    + (value >> 22 & 0xF) * 100
                    + (value >> 17 & 0x1F);
        }

        /**
         * Decodes the time in an encoded timestamp
         * @param value timestamp encoded just as feedTimestamp
         * @return value's time as second of day
         */
        public static int decodeTime(int value) {
            return value & 0x1FFFF;
        }

        /**
         * Converts an encoded feedTimestamp into a DateTime value
         * @param value timestamp encoded just as feedTimestamp
         * @return feedTimestamp as DateTime
         */
        public static DateTime toDateTime(int value) {
            if (value == 0) {
                return null;
            }
            return new DateTime(2000 + (value >> 26 & 0x3F), (value >> 22 & 0xF), value >> 17 & 0x1F
                    , 0, 0).withMillisOfDay(decodeTime(value) * 1000);
        }

        public String toString() {
            return this.dateTime.toString();
        }
    }

    Timestamp current();

    int dayAsYyyyMmDd();

    int secondOfDay();
}
