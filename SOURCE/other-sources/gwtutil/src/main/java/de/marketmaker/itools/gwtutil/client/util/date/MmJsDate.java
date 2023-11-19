package de.marketmaker.itools.gwtutil.client.util.date;

import java.util.Date;

/**
 * @author umaurer
 */
public class MmJsDate extends JsDate {
    private static final long MILLIS_PER_DAY = 86400000l;
    private static final long MILLIS_PER_QARTERDAY = 21600000l;

    public static final int DAY_SUNDAY = 0;
    public static final int DAY_MONDAY = 1;
    public static final int DAY_TUESDAY = 2;
    public static final int DAY_WEDNESDAY = 3;
    public static final int DAY_THURSDAY = 4;
    public static final int DAY_FRIDAY = 5;
    public static final int DAY_SATURDAY = 6;

    public static final int MONTH_JANUARY = 0;
    public static final int MONTH_FEBRUARY = 1;
    public static final int MONTH_MARCH = 2;
    public static final int MONTH_APRIL = 3;
    public static final int MONTH_MAY = 4;
    public static final int MONTH_JUNE = 5;
    public static final int MONTH_JULY = 6;
    public static final int MONTH_AUGUST = 7;
    public static final int MONTH_SEPTEMBER = 8;
    public static final int MONTH_OCTOBER = 9;
    public static final int MONTH_NOVEMBER = 10;
    public static final int MONTH_DECEMBER = 11;

    public MmJsDate() {
        super();
    }

    public MmJsDate(String sDate) {
        super(sDate);
    }

    public MmJsDate(int year, int month, int day) {
        super(year, month, day);
    }

    public MmJsDate(int year, int month, int day, int hours, int minutes, int seconds) {
        super(year, month, day, hours, minutes, seconds);
    }

    public MmJsDate(int year, int month, int day, int hours, int minutes, int seconds, int milliseconds) {
        super(year, month, day, hours, minutes, seconds, milliseconds);
    }

    public MmJsDate(Date date) {
        super(date.getTime());
    }

    public MmJsDate(long millis) {
        super(millis);
    }

    public MmJsDate(JsDate jsDate) {
        super(jsDate.getTime());
    }

    public Date getJavaDate() {
        return new Date(getTime());
    }

    public String toString() {
        return getJavaDate().toString();
    }

    public boolean equals(Object obj) {
        return obj instanceof JsDate && getTime() == ((JsDate) obj).getTime();
    }

    public MmJsDate getFirstOfMonth() {
        final MmJsDate date = new MmJsDate(this);
        date.setDate(1);
        setMidnight(date);
        return date;
    }

    public MmJsDate getPreviousMonday() {
        final MmJsDate date = new MmJsDate(this);
        setMidnight(date);
        int dayOfWeek = (date.getDay() + 6) % 7;
        long mondayMillis = date.getTime() - dayOfWeek * MILLIS_PER_DAY + MILLIS_PER_QARTERDAY;
        date.setTime(mondayMillis);
        date.setHours(0);
        return date;
    }

    public MmJsDate getMidnight() {
        final MmJsDate date = new MmJsDate(this);
        setMidnight(date);
        return date;
    }

    private static void setMidnight(MmJsDate date) {
        date.setHours(0);
        date.setMinutes(0);
        date.setSeconds(0);
        date.setMilliseconds(0);
    }

    public MmJsDate atMidnight() {
        setMidnight(this);
        return this;
    }

    public MmJsDate addDays(int days) {
        // get hours and reset them later, to avoid problems with summer time
        final int hours = getHours();
        long diff = 0;
        if (hours <= 6) {
            diff = MILLIS_PER_QARTERDAY;
        }
        else if (hours >= 18) {
            diff = -MILLIS_PER_QARTERDAY;
        }
        setTime(getTime() + days * MILLIS_PER_DAY + diff);
        setHours(hours);
        return this;
    }

    public MmJsDate addMonths(int months) {
        final int day = getDate();
        setMonth(getMonth() + months);
        if (day != getDate()) {
            final int month = getMonth();
            while (month == getMonth()) {
                addDays(-1);
            }
        }
        return this;
    }

    public MmJsDate addYears(int years) {
        final int day = getDate();
        setFullYear(getFullYear() + years);
        if (day != getDate()) {
            final int month = getMonth();
            while (month == getMonth()) {
                addDays(-1);
            }
        }
        return this;
    }

    public MmJsDate getPreviousMonth() {
        final MmJsDate date = new MmJsDate(this);
        date.addMonths(-1);
        return date;
    }

    public MmJsDate getNextMonth() {
        final MmJsDate date = new MmJsDate(this);
        date.addMonths(1);
        return date;
    }

    public void setDateOrLastOfMonth(int date) {
        final int month = getMonth();
        setDate(date);
        while (month != getMonth()) {
            addDays(-1);
        }
    }

    public boolean isSameDay(MmJsDate date) {
        return getDate() == date.getDate() && getMonth() == date.getMonth() && getFullYear() == date.getFullYear();
    }

    public boolean isBefore(MmJsDate minDate) {
        return getTime() < minDate.getTime();
    }

    public boolean isAfter(MmJsDate minDate) {
        return getTime() > minDate.getTime();
    }
    
    public boolean isToday() {
        return isSameDay(new MmJsDate());
    }

    public boolean isMidnight() {
        return getHours() == 0 && getMinutes() == 0 && getSeconds() == 0 && getMilliseconds() == 0;
    }

    public long getDiffDays(MmJsDate date) {
        final long dateCurrentTimeMillis = getTime();
        final long currentTimeMillis = date.getTime();
        final double diffMillis = Math.abs(currentTimeMillis - dateCurrentTimeMillis);
        return Math.round(diffMillis / MmJsDate.MILLIS_PER_DAY);
    }

    public MmJsDate addMinutes(int minutes) {
        this.setMinutes(this.getMinutes() + minutes);
        return this;
    }

    public MmJsDate addHours(int hours) {
        this.setHours(this.getHours() + hours);
        return this;
    }

    public MmJsDate addSeconds(int seconds) {
        this.setSeconds(this.getSeconds() + seconds);
        return this;
    }
}
