/*
 * DateUtil.java
 *
 * Created on 02.12.2004 13:10:40
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.common.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.DateTimeZone;
import org.joda.time.Days;
import org.joda.time.DurationFieldType;
import org.joda.time.Interval;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.joda.time.Period;
import org.joda.time.PeriodType;
import org.joda.time.ReadablePartial;
import org.joda.time.Weeks;
import org.joda.time.YearMonthDay;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.joda.time.format.ISOPeriodFormat;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class DateUtil {
    public static final DateTimeZone DTZ_BERLIN = DateTimeZone.forID("Europe/Berlin");

    private static final DateTimeFormatter DTF
            = ISODateTimeFormat.localDateOptionalTimeParser().withZone(DTZ_BERLIN);

    public static final DateTimeFormatter DTF_GERMAN = DateTimeFormat.forPattern("dd.MM.yyyy");

    public static final DateTimeFormatter DTF_XML = DateTimeFormat.forPattern("yyyy-MM-dd");

    /**
     * offset between local time zone and GMT.
     * Required for comDateToJavaDate()
     */
    final static long zoneOffset = Calendar.getInstance().get(Calendar.ZONE_OFFSET);

    /**
     * Convert a COM Date (a double) to a Java date.
     * <pre>
     * From MSDN documentation:
     * "A variant time is stored as an 8-byte real count (double), representing a
     * date between January 1, 100 and December 31, 9999, inclusive. The count
     * 2.0 represents January 1, 1900; 3.0 represents January 2, 1900, and so on.
     * Adding 1 to the count increments the date by a day. The fractional part of the
     * count represents the time of day. Therefore, 2.5 represents noon on January 1,
     * 1900; 3.25 represents 6:00 A.M. on January 2, 1900, and so on. Negative numbers
     * represent the dates prior to December 30, 1899."
     * </pre>
     * Java dates are based around a Java long representing the number of milliseconds since
     * January 1, 1970, 00:00:00 GMT
     *
     * @param comDate The COM date to be converted
     * @return The equivalent Java Date
     */
    public static Date comDateToDate(double comDate) {
        // subtract 25569.0  This is the number of days between 1 Jan 1970 and 1 Jan 1899
        comDate = comDate - 25569D;

        // multiply by no of milliseconds in a day
        long millis = Math.round(86400000L * comDate) - zoneOffset;


        final Calendar cal = Calendar.getInstance();
        cal.setTime(new Date(millis));

        // Subtract the number of milliseconds between GMT and the local time. COM dates are based on the
        // local time, whereas the long representing the Java date is based on GMT.
        millis -= cal.get(Calendar.DST_OFFSET);

        return new Date(millis);
    }

    public static DateTime comDateToDateTime(double comDate) {
        return new DateTime(comDateToDate(comDate).getTime());
    }

    public static double javaDateToComDate(Date javaDate) {
        final Calendar cal = Calendar.getInstance();
        cal.setTime(javaDate);
        long gmtOffset = (cal.get(Calendar.ZONE_OFFSET) + cal.get(Calendar.DST_OFFSET));
        long millis = javaDate.getTime() + gmtOffset;
        return 25569D + millis / 86400000D;
    }

    public static int dateToYyyyMmDd() {
        return toYyyyMmDd(new DateTime());
    }

    public static int toYyyyMmDd(DateTime dt) {
        return dt.getYear() * 10000 + dt.getMonthOfYear() * 100 + dt.getDayOfMonth();
    }

    public static int dateToYyyyMmDd(Date date) {
        return toYyyyMmDd(new DateTime(date.getTime()));
    }

    public static long dateToYyyyMmDdHhMmSs(Date date) {
        final DateTime dt = new DateTime(date.getTime());
        return toYyyyMmDd(dt) * 1000000L
                + dt.getHourOfDay() * 10000 + dt.getMinuteOfHour() * 100 + dt.getSecondOfMinute();
    }

    public static int dateToSecondsInDay(Date date) {
        return new DateTime(date).getSecondOfDay();
    }

    public static Calendar yyyyMmDdToCalendar(int yyyyMmDdDate) {
        return toCalendar(yyyyMmDdDate, 0);
    }

    public static YearMonthDay yyyyMmDdToYearMonthDay(int yyyyMmDd) {
        return new YearMonthDay(yyyyMmDd / 10000, (yyyyMmDd % 10000) / 100, yyyyMmDd % 100);
    }

    public static LocalDate yyyyMmDdToLocalDate(int yyyyMmDd) {
        return new LocalDate(yyyyMmDd / 10000, (yyyyMmDd % 10000) / 100, yyyyMmDd % 100);
    }

    public static LocalTime secondsInDayToLocalTime(int secondsInDay) {
        return new LocalTime(secondsInDay / 3600, (secondsInDay % 3600) / 60, secondsInDay % 60);
    }

    public static int toYyyyMmDd(YearMonthDay ymd) {
        return ymd.getYear() * 10000 + ymd.getMonthOfYear() * 100 + ymd.getDayOfMonth();
    }

    public static int toYyyyMmDd(LocalDate ymd) {
        return ymd.getYear() * 10000 + ymd.getMonthOfYear() * 100 + ymd.getDayOfMonth();
    }

    private static Calendar toCalendar(int yyyyMmDd, int secondsInDay) {
        return toDateTime(yyyyMmDd, secondsInDay).toGregorianCalendar();
    }

    public static Date yyyyMmDdToDate(int yyyyMmDdDate) {
        return yyyyMmDdToCalendar(yyyyMmDdDate).getTime();
    }

    public static int getDaysToToday(int yyyyMmDdDate) {
        final LocalDate today = new LocalDate();
        final LocalDate then = yyyyMmDdToLocalDate(yyyyMmDdDate);

        if (then.isBefore(today)) {
            return daysBetween(then, today);
        }
        return daysBetween(today, then);
    }

    /**
     * Returns date in format yyyymmdd that is periodInDays days after today
     *
     * @param offsetFromToday number of days to add to today's date, may be negative
     * @return date in format yyyymmdd
     */
    public static int getDate(int offsetFromToday) {
        final LocalDate ld = new LocalDate().plusDays(offsetFromToday);
        return ld.getYear() * 10000 + ld.getMonthOfYear() * 100 + ld.getDayOfMonth();
    }

    public static DateTime yyyymmddToDateTime(int yyyymmdd) {
        return new DateTime(yyyymmdd / 10000, (yyyymmdd % 10000) / 100, yyyymmdd % 100, 0, 0, 0, 0);
    }

    public static DateTime yyyymmddhhmmssToDateTime(long yyyymmddhhmmss) {
        return new DateTime((int) (yyyymmddhhmmss / 10000000000L),
                (int) (((yyyymmddhhmmss % 10000000000L) / 100000000)),
                (int) ((yyyymmddhhmmss % 100000000) / 1000000),
                (int) ((yyyymmddhhmmss % 1000000) / 10000),
                (int) ((yyyymmddhhmmss % 10000) / 100),
                (int) (yyyymmddhhmmss % 100),
                0);
    }

    public static DateTime toDateTime(int yyyymmdd, int secondsInDay) {
        return new DateTime(yyyymmdd / 10000, (yyyymmdd % 10000) / 100, yyyymmdd % 100,
                secondsInDay / DateTimeConstants.SECONDS_PER_HOUR,
                (secondsInDay % DateTimeConstants.SECONDS_PER_HOUR) / DateTimeConstants.SECONDS_PER_MINUTE,
                secondsInDay % DateTimeConstants.SECONDS_PER_MINUTE, 0);
    }

    public static DateTime toDateTime(int yyyymmdd, int secondsInDay, DateTimeZone dtz) {
        return new DateTime(yyyymmdd / 10000, (yyyymmdd % 10000) / 100, yyyymmdd % 100,
                secondsInDay / DateTimeConstants.SECONDS_PER_HOUR,
                (secondsInDay % DateTimeConstants.SECONDS_PER_HOUR) / DateTimeConstants.SECONDS_PER_MINUTE,
                secondsInDay % DateTimeConstants.SECONDS_PER_MINUTE, 0, dtz);
    }

    public static int mmDayToYyyymmdd(float mmday) {
        final int date = (int) mmday;
        final int year = date / 1000;
        final int day = date % 1000;
        return toYyyyMmDd(new LocalDate(year, 1, 1).withDayOfYear(day));
    }

    /**
     * Get current calendar of default locale with hour, minute, second and millisecond
     * set to 0.
     */
    public static Calendar getMidnightOfToday() {
        return new LocalDate().toDateTimeAtStartOfDay().toGregorianCalendar();
    }

    /**
     * Return number of days between first and second parameter (yyyymmdd) ignoring
     * saturdays and sundays.
     */
    public static int getWorkingDaysBetween(int startYyyymmdd, int endYyyymmdd) {
        final LocalDate start = yyyyMmDdToLocalDate(startYyyymmdd);
        final LocalDate end = yyyyMmDdToLocalDate(endYyyymmdd);
        if (!end.isAfter(start)) {
            return 0;
        }

        LocalDate monStart = start.withDayOfWeek(DateTimeConstants.MONDAY);
        LocalDate monEnd = end.withDayOfWeek(DateTimeConstants.MONDAY);

        return (Weeks.weeksBetween(monStart, monEnd).getWeeks() * 5
                - Math.min(4, Days.daysBetween(monStart, start).getDays())
                + Math.min(4, Days.daysBetween(monEnd, end).getDays()));
    }

    private static int getNumTypes(Period p) {
        int n = 0;
        for (DurationFieldType type : p.getFieldTypes()) {
            if (p.get(type) > 0) {
                n++;
            }
        }
        return n;
    }

    public static boolean canAggregateTo(Period from, Period to) {
        // TODO: does not cover all possible cases
        if (getNumTypes(from) != 1 || getNumTypes(to) != 1) {
            return false;
        }

        if (from.getSeconds() > 0) {
            if (to.getSeconds() > 0) {
                return to.getSeconds() <= DateTimeConstants.SECONDS_PER_DAY
                        && (to.getSeconds() % from.getSeconds()) == 0;
            }
            if (to.getMinutes() > 0) {
                return ((to.getMinutes() * DateTimeConstants.SECONDS_PER_MINUTE)
                        % from.getSeconds()) == 0;
            }
            if (to.getHours() > 0) {
                return ((to.getHours() * DateTimeConstants.SECONDS_PER_HOUR)
                        % from.getSeconds()) == 0;
            }
            return to.getDays() > 0 || to.getWeeks() > 0
                    || to.getMonths() > 0 || to.getYears() > 0;
        }
        if (from.getHours() > 0) {
            if (to.getHours() > 0) {
                return to.getHours() <= DateTimeConstants.HOURS_PER_DAY
                        && DateTimeConstants.HOURS_PER_DAY % to.getHours() == 0
                        && to.getHours() % from.getHours() == 0;
            }

        }
        if (from.getDays() > 0) {
            if (from.getDays() == 1) {
                return to.getDays() > 0 || to.getWeeks() > 0
                        || to.getMonths() > 0 || to.getYears() > 0;
            }
            return false;
        }
        if (from.getWeeks() > 0) {
            return to.getWeeks() > 0
                    && to.getWeeks() % from.getWeeks() == 0;
        }
        if (from.getMonths() > 0) {
            return (to.getMonths() > 0
                    && to.getMonths() % from.getMonths() == 0)
                    || (to.getYears() > 0 && (12 % from.getMonths()) == 0);
        }
        if (from.getYears() > 0) {
            return to.getYears() > 0
                    && to.getYears() % from.getYears() == 0;
        }
        return false;
    }

    public static Interval getInterval(String definition) {
        return new Interval(getPeriod(definition), new DateTime());
    }

    public static List<Interval> getIntervals(String... defs) {
        final DateTime now = new DateTime();
        final List<Interval> result = new ArrayList<>(defs.length);
        for (String def : defs) {
            result.add(new Interval(getPeriod(def), now));
        }
        return result;
    }

    /**
     * Creates a list of intervals that span at most a full day. The first interval will start at from,
     * all others at midnight. The last ends at min(now(), to), all others at midnight.
     *
     * @param from start for first interval
     * @param to end of last interval
     * @return list of intervals covering the time between from and to
     */
    public static List<Interval> getDailyIntervals(DateTime from, DateTime to) {
        final DateTime now = new DateTime();
        if (to.isBefore(from) || from.isAfter(now)) {
            return Collections.emptyList();
        }

        final DateTime end = to.isBefore(now) ? to : now;
        final List<Interval> result = new ArrayList<>();

        DateTime dt = from;
        while (dt.isBefore(end)) {
            DateTime next = dt.withTimeAtStartOfDay().plusDays(1);
            result.add(new Interval(dt, next.isAfter(end) ? end : next));
            dt = next;
        }
        return result;
    }

    public static Period getPeriod(String definition) {
        return ISOPeriodFormat.standard().parsePeriod(definition.startsWith("P") ? definition : "P" + definition);
    }

    public static int daysBetween(YearMonthDay ymd1, YearMonthDay ymd2) {
        return new Period(ymd1, ymd2, PeriodType.days()).getDays();
    }

    public static int daysBetween(ReadablePartial ymd1, ReadablePartial ymd2) {
        return new Period(ymd1, ymd2, PeriodType.days()).getDays();
    }

    public static int daysBetween(LocalDate ymd1, LocalDate ymd2) {
        return new Period(ymd1, ymd2, PeriodType.days()).getDays();
    }

    public static LocalDate min(LocalDate ld1, LocalDate ld2) {
        return ld1.isBefore(ld2) ? ld1 : ld2;
    }

    public static LocalDate max(LocalDate ld1, LocalDate ld2) {
        return ld1.isAfter(ld2) ? ld1 : ld2;
    }

    public static YearMonthDay toYearMonthDay(LocalDate date) {
        return new YearMonthDay(date.getYear(), date.getMonthOfYear(), date.getDayOfMonth());
    }

    public static DateTime parseDate(String date) {
        if (date.indexOf('-') > 0) {
            return DTF_XML.parseDateTime(date);
        }

        return DTF_GERMAN.parseDateTime(date);
    }

    public static DateTime max(DateTime dt1, DateTime dt2) {
        if (dt1 == null) {
            return dt2;
        }
        if (dt2 == null) {
            return dt1;
        }
        return (dt1.isAfter(dt2)) ? dt1 : dt2;
    }

    public static boolean canUseTickHistory(Interval interval) {
        DateTime histEnd = getHistoryEnd();
        DateTime histStart = getHistoryStart(histEnd);
        return !interval.getStart().isBefore(histStart) && !interval.getEnd().isAfter(histEnd);
    }

    public static DateTime getHistoryStart(DateTime historyEnd) {
        return historyEnd.minusYears(1).withDayOfMonth(1);
    }

    public static DateTime getHistoryEnd() {
        return new LocalDate().toDateTimeAtStartOfDay();
    }

    /**
     * Returns either<ul>
     * <li>[from..midnight, midnight..to] if both of those intervals are valid</li>
     * <li>[from..to] if from is before to</li>
     * <li>[] otherwise</li>
     * </ul>
     * If <tt>to</tt> is a date for a day after today, it will be replaced by today@24:00:00
     *
     * @param from start of interval
     * @param to end of nterval
     * @return list of intervals covering the time between from and to
     */
    public static List<Interval> toHistoryIntervals(DateTime from, DateTime to) {
        final DateTime now = new DateTime();
        if (to.isBefore(from) || from.isAfter(now)) {
            return Collections.emptyList();
        }

        final DateTime todayStart = now.withTimeAtStartOfDay();
        final DateTime nextDayStart = todayStart.plusDays(1);

        final DateTime end = to.isBefore(now) ? to : to.isAfter(nextDayStart) ? nextDayStart : to;

        if (!from.isBefore(todayStart)) {
            return Collections.singletonList(new Interval(from, end));
        }

        // from is before today's start
        DateTime historyEnd = todayStart;
        DateTime historyStart = getHistoryStart(historyEnd);

        if (!from.isBefore(historyStart)) {
            return end.isAfter(historyEnd)
                    ? Arrays.asList(new Interval(from, historyEnd), new Interval(historyEnd, end))
                    : Collections.singletonList(new Interval(from, end));
        }
        else {
            List<Interval> list = DateUtil.getDailyIntervals(from, end.isAfter(historyStart) ? historyStart : end);

            if (end.isAfter(historyStart)) {
                if (end.isAfter(historyEnd)) {
                    list.add(new Interval(historyStart, historyEnd));
                    list.add(new Interval(historyEnd, end));
                }
                else {
                    list.add(new Interval(historyStart, end));
                }
            }

            return list;
        }
    }
}
