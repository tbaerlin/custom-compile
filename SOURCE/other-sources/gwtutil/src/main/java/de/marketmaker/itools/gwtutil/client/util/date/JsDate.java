package de.marketmaker.itools.gwtutil.client.util.date;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * @author umaurer
 */

@SuppressWarnings({"UnusedDeclaration"})
public class JsDate {

    private JavaScriptObject jsDate;

    public JsDate() {
        _init();
    }

    public JsDate(String sDate) {
        _init(sDate);
    }

    public JsDate(int year, int month, int day) {
        _init(year, month, day);
    }

    public JsDate(int year, int month, int day, int hours, int minutes, int seconds) {
        _init(year, month, day, hours, minutes, seconds);
    }

    public JsDate(int year, int month, int day, int hours, int minutes, int seconds, int milliseconds) {
        _init(year, month, day, hours, minutes, seconds);
        setMilliseconds(milliseconds);
    }

    public JsDate(long millis) {
        _init(millis);
    }

    private native void _init() /*-{
        this.@de.marketmaker.itools.gwtutil.client.util.date.JsDate::jsDate = new Date();
    }-*/;

    private native void _init(String sDate) /*-{
        this.@de.marketmaker.itools.gwtutil.client.util.date.JsDate::jsDate = new Date(sDate);
    }-*/;

    private native void _init(int year, int month, int day) /*-{
        this.@de.marketmaker.itools.gwtutil.client.util.date.JsDate::jsDate = new Date(year, month, day);
    }-*/;

    private native void _init(int year, int month, int day, int hours, int minutes, int seconds) /*-{
        this.@de.marketmaker.itools.gwtutil.client.util.date.JsDate::jsDate = new Date(year, month, day, hours, minutes, seconds);
    }-*/;

    private native void _init(double millis) /*-{
        this.@de.marketmaker.itools.gwtutil.client.util.date.JsDate::jsDate = new Date(millis);
    }-*/;

    /**
     * @return Day of month (1-31)
     */
    public native int getDate() /*-{
        return this.@de.marketmaker.itools.gwtutil.client.util.date.JsDate::jsDate.getDate();
    }-*/;

    /**
     * @return Day of week (0 = Sunday, 1 = Monday, ... , 6 = Saturday)
     */
    public native int getDay() /*-{
        return this.@de.marketmaker.itools.gwtutil.client.util.date.JsDate::jsDate.getDay();
    }-*/;

    /**
     * @return Years as 4-digit number. In IE years after 2000 are identical to getYear() result.
     */
    public native int getFullYear() /*-{
        return this.@de.marketmaker.itools.gwtutil.client.util.date.JsDate::jsDate.getFullYear();
    }-*/;

    /**
     * @return Hours of the day (0-23). e.g. 23 at 23:15:00.
     */
    public native int getHours() /*-{
        return this.@de.marketmaker.itools.gwtutil.client.util.date.JsDate::jsDate.getHours();
    }-*/;

    /**
     * @return Number of milliseconds since last full second.
     */
    public native int getMilliseconds() /*-{
        return this.@de.marketmaker.itools.gwtutil.client.util.date.JsDate::jsDate.getMilliseconds();
    }-*/;

    /**
     * @return Number of minutes since last full hour.
     */
    public native int getMinutes() /*-{
        return this.@de.marketmaker.itools.gwtutil.client.util.date.JsDate::jsDate.getMinutes();
    }-*/;

    /**
     * @return Month of the year (0-11). Caution: January is 0, December is 11.
     */
    public native int getMonth() /*-{
        return this.@de.marketmaker.itools.gwtutil.client.util.date.JsDate::jsDate.getMonth();
    }-*/;

    /**
     * @return Number of seconds since last full minute.
     */
    public native int getSeconds() /*-{
        return this.@de.marketmaker.itools.gwtutil.client.util.date.JsDate::jsDate.getSeconds();
    }-*/;

    /**
     * @return Number of milliseconds since 01.01.1970
     */
    private native double _getTime() /*-{
        return this.@de.marketmaker.itools.gwtutil.client.util.date.JsDate::jsDate.getTime();
    }-*/;

    /**
     * @return Millis since 01.01.1970
     */
    public long getTime() {
        return (long)_getTime();
    }

    /**
     * @return Difference in minutes between system time and GMT (Greenwich Mean Time).
     */
    public native int getTimezoneOffset() /*-{
        return this.@de.marketmaker.itools.gwtutil.client.util.date.JsDate::jsDate.getTimezoneOffset();
    }-*/;

    /**
     * @return Day of month (1-31). UTC (=GMT) version.
     */
    public native int getUTCDate() /*-{
        return this.@de.marketmaker.itools.gwtutil.client.util.date.JsDate::jsDate.getDate();
    }-*/;

    /**
     * @return Years as 4-digit number. In IE years after 2000 are identical to getYear() result. UTC (=GMT) version.
     */
    public native int getUTCFullYear() /*-{
        return this.@de.marketmaker.itools.gwtutil.client.util.date.JsDate::jsDate.getUTCFullYear();
    }-*/;

    /**
     * @return Hours of the day (0-23). e.g. 23 at 23:15:00. UTC (=GMT) version.
     */
    public native int getUTCHours() /*-{
        return this.@de.marketmaker.itools.gwtutil.client.util.date.JsDate::jsDate.getUTCHours();
    }-*/;

    /**
     * @return Number of milliseconds since last full second. UTC (=GMT) version.
     */
    public native int getUTCMilliseconds() /*-{
        return this.@de.marketmaker.itools.gwtutil.client.util.date.JsDate::jsDate.getUTCMilliseconds();
    }-*/;

    /**
     * @return Number of minutes since last full hour. UTC (=GMT) version.
     */
    public native int getUTCMinutes() /*-{
        return this.@de.marketmaker.itools.gwtutil.client.util.date.JsDate::jsDate.getUTCMinutes();
    }-*/;

    /**
     * @return Month of the year (0-11). Caution: January is 0, December is 11. UTC (=GMT) version.
     */
    public native int getUTCMonth() /*-{
        return this.@de.marketmaker.itools.gwtutil.client.util.date.JsDate::jsDate.getUTCMonth();
    }-*/;

    /**
     * @return Number of seconds since last full minute. UTC (=GMT) version.
     */
    public native int getUTCSeconds() /*-{
        return this.@de.marketmaker.itools.gwtutil.client.util.date.JsDate::jsDate.getUTCSeconds();
    }-*/;

    /**
     * @return The result of getYear() differs in the various JavaScript specifications and in browser versions.
     * @deprecated use getFullYear() instead.
     */
    public native int getYear() /*-{
        return this.@de.marketmaker.itools.gwtutil.client.util.date.JsDate::jsDate.getYear();
    }-*/;

    private static native double _parse(String sDate) /*-{
        return Date.parse(sDate);
    }-*/;

    /**
     * Parse the given time string and return the number of milliseconds between 01.01.1970 UTC the parsed time.
     * @param sDate The given time string (e.g."Tue, 1 Jan 2000 00:00:00 GMT")
     * @return the number of milliseconds between 01.01.1970 UTC the parsed time
     */
    public static long parse(String sDate) {
        return (long)_parse(sDate);
    }

    /**
     * @param date day of month (1-31)
     */
    public native void setDate(int date) /*-{
        this.@de.marketmaker.itools.gwtutil.client.util.date.JsDate::jsDate.setDate(date);
    }-*/;

    /**
     * @param fullYear The year as a 4-digit number.
     */
    public native void setFullYear(int fullYear) /*-{
        this.@de.marketmaker.itools.gwtutil.client.util.date.JsDate::jsDate.setFullYear(fullYear);
    }-*/;

    /**
     * @param hours The hours of the day (0-23).
     */
    public native void setHours(int hours) /*-{
        this.@de.marketmaker.itools.gwtutil.client.util.date.JsDate::jsDate.setHours(hours);
    }-*/;

    /**
     * @param millis The number of milliseconds since the last full second.
     */
    public native void setMilliseconds(int millis) /*-{
        this.@de.marketmaker.itools.gwtutil.client.util.date.JsDate::jsDate.setMilliseconds(millis);
    }-*/;

    /**
     * @param minutes The number of minutes since the last full hour.
     */
    public native void setMinutes(int minutes) /*-{
        this.@de.marketmaker.itools.gwtutil.client.util.date.JsDate::jsDate.setMinutes(minutes);
    }-*/;

    /**
     * @param month The month (0-11). Caution: January is 0, December is 11. Use constants JANUARY, ...
     */
    public native void setMonth(int month) /*-{
        this.@de.marketmaker.itools.gwtutil.client.util.date.JsDate::jsDate.setMonth(month);
    }-*/;

    /**
     * @param seconds The number of seconds since last full minute.
     */
    public native void setSeconds(int seconds) /*-{
        this.@de.marketmaker.itools.gwtutil.client.util.date.JsDate::jsDate.setSeconds(seconds);
    }-*/;

    private native void _setTime(double millis) /*-{
        this.@de.marketmaker.itools.gwtutil.client.util.date.JsDate::jsDate.setTime(millis);
    }-*/;

    /**
     * Set date and time.
     * @param millis The number of milliseconds since 01.01.1970 00:00:00.
     */
    public void setTime(long millis) {
        _setTime(millis);
    }

    /**
     * UTC (=GMT) version of setDate(int)
     * @param date day of month (1-31)
     */
    public native void setUTCDate(int date) /*-{
        this.@de.marketmaker.itools.gwtutil.client.util.date.JsDate::jsDate.setUTCDate(date);
    }-*/;

    /**
     * UTC (=GMT) version of setFullYear(int)
     * @param fullYear The year as a 4-digit number.
     */
    public native void setUTCFullYear(int fullYear) /*-{
        this.@de.marketmaker.itools.gwtutil.client.util.date.JsDate::jsDate.setUTCFullYear(fullYear);
    }-*/;

    /**
     * UTC (=GMT) version of setHours(int)
     * @param hours The hours of the day (0-23).
     */
    public native void setUTCHours(int hours) /*-{
        this.@de.marketmaker.itools.gwtutil.client.util.date.JsDate::jsDate.setUTCHours(hours);
    }-*/;

    /**
     * UTC (=GMT) version of setMilliseconds(int)
     * @param millis The number of milliseconds since the last full second.
     */
    public native void setUTCMilliseconds(int millis) /*-{
        this.@de.marketmaker.itools.gwtutil.client.util.date.JsDate::jsDate.setUTCMilliseconds(millis);
    }-*/;

    /**
     * UTC (=GMT) version of setMinutes(int)
     * @param minutes The number of minutes since the last full hour.
     */
    public native void setUTCMinutes(int minutes) /*-{
        this.@de.marketmaker.itools.gwtutil.client.util.date.JsDate::jsDate.setUTCMinutes(minutes);
    }-*/;

    /**
     * UTC (=GMT) version of setMonth(int)
     * @param month The month (0-11). Caution: January is 0, December is 11. Use constants JANUARY, ...
     */
    public native void setUTCMonth(int month) /*-{
        this.@de.marketmaker.itools.gwtutil.client.util.date.JsDate::jsDate.setUTCMinutes(month);
    }-*/;

    /**
     * UTC (=GMT) version of setSeconds(int)
     * @param seconds The number of seconds since last full minute.
     */
    public native void setUTCSeconds(int seconds) /*-{
        this.@de.marketmaker.itools.gwtutil.client.util.date.JsDate::jsDate.setUTCSeconds(seconds);
    }-*/;

    /**
     * @param year The year.
     * @deprecated use setFullYear() instead
     */
    public native void setYear(int year) /*-{
        this.@de.marketmaker.itools.gwtutil.client.util.date.JsDate::jsDate.setYear(year);
    }-*/;

    public native String toGMTString() /*-{
        this.@de.marketmaker.itools.gwtutil.client.util.date.JsDate::jsDate.toGMTString();
    }-*/;

    public native String toLocaleString() /*-{
        this.@de.marketmaker.itools.gwtutil.client.util.date.JsDate::jsDate.toLocaleString();
    }-*/;

    private static native double _UTC(int year, int month, int day) /*-{
        return Date.UTC(year, month, day);
    }-*/;
    public static long UTC(int year, int month, int day) {
        return (long) _UTC(year, month, day);
    }

    private static native double _UTC(int year, int month, int day, int hours, int minutes, int seconds) /*-{
        return Date.UTC(year, month, day, hours, minutes, seconds);
    }-*/;
    public static long UTC(int year, int month, int day, int hours, int minutes, int seconds) {
        return (long) _UTC(year, month, day, hours, minutes, seconds);
    }
}
