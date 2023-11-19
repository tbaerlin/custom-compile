/*
 * Formatter.java
 *
 * Created on 31.03.2008 13:35:20
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.util;

import java.util.Date;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.NumberFormat;
import de.marketmaker.itools.gwtutil.client.i18n.LocalizedFormatter;
import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.itools.gwtutil.client.util.date.MmJsDate;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class Formatter {
    public static final NumberFormat FORMAT_PERCENT = NumberFormat.getFormat("0.00%"); // $NON-NLS$

    public static final NumberFormat FORMAT_PERCENT_SIGNED = NumberFormat.getFormat("+0.00%;-0.00"); // $NON-NLS$

    public static final NumberFormat FORMAT_NUMBER = NumberFormat.getFormat("0.00#"); // $NON-NLS$
    public static final NumberFormat FORMAT_NUMBER_2 = NumberFormat.getFormat("0.00"); // $NON-NLS$
    public static final NumberFormat FORMAT_NUMBER23 = NumberFormat.getFormat("0.00#"); // $NON-NLS$
    public static final NumberFormat FORMAT_NUMBER07 = NumberFormat.getFormat("0.#######"); // $NON-NLS$
    public static final NumberFormat FORMAT_NUMBER_GROUPS = NumberFormat.getFormat("#,###,###,###"); // $NON-NLS$

    public static final NumberFormat FORMAT_NUMBER_SIGNED = NumberFormat.getFormat("+0.00#;-0.00#"); // $NON-NLS$

    public static final DateTimeFormat FORMAT_YEAR = DateTimeFormat.getFormat("yyyy"); // $NON-NLS$
    public static final DateTimeFormat FORMAT_MONTH_YEAR = DateTimeFormat.getFormat("MMM yyyy"); // $NON-NLS$
    public static final DateTimeFormat FORMAT_ISO_DAY = DateTimeFormat.getFormat("yyyy-MM-dd"); // $NON-NLS$
    public static final DateTimeFormat FORMAT_ISO_DATE = DateTimeFormat.getFormat("yyyy-MM-dd'T'HH:mm:ssZ"); // $NON-NLS$
    public static final DateTimeFormat FORMAT_ISO_DATE_00 = DateTimeFormat.getFormat("yyyy-MM-dd'T00:00:00'Z"); // $NON-NLS$
    public static final DateTimeFormat FORMAT_TIME = DateTimeFormat.getFormat("HH:mm:ss"); // $NON-NLS$
    public static final DateTimeFormat FORMAT_TIME_HHMM = DateTimeFormat.getFormat("HH:mm"); // $NON-NLS$
    public static final DateTimeFormat PM_DATE_TIME_FORMAT_MMTALK = DateTimeFormat.getFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZZZ"); // $NON-NLS$
    public static final DateTimeFormat DMXML_DATE_TIME_FORMAT = DateTimeFormat.getFormat("yyyy-MM-dd'T'HH:mm:ssZZZ"); // $NON-NLS$

    private static final DateTimeFormat FORMAT_DE_DAY = DateTimeFormat.getFormat("dd.MM.yyyy"); // $NON-NLS$
    public static final LocalizedFormatter LF = LocalizedFormatter.getInstance();

    public static String formatDateYyyy(String date, String defaultText) {
        if (date == null) {
            return defaultText;
        }
        return date.substring(0, 4);
    }

    public static String formatTime(String date) {
        return formatTime(date, "--:--:--");
    }

    public static String formatTime(String date, String defaultText) {
        if (date == null) {
            return defaultText;
        }
        return date.substring(11, 19);
    }


    public static String formatTimeHhmm(String date) {
        return formatTimeHhmm(date, "--:--");
    }

    public static String formatTimeHhmm(String date, String defaultText) {
        if (date == null) {
            return defaultText;
        }
        return date.substring(11, 16);
    }

    public static String formatNumber(String text, String defaultText) {
        return formatNumber(text, defaultText, false);
    }

    public static String formatNumber(String text, String defaultText, boolean signed) {
        if (text == null) {
            return defaultText;
        }
        final NumberFormat nf = signed ? FORMAT_NUMBER_SIGNED : FORMAT_NUMBER;
        try {
            return nf.format(Double.parseDouble(text));
        } catch (NumberFormatException e) {
            return defaultText;
        }
    }

    public static String formatPercent(String text, String defaultText) {
        return formatPercent(text, defaultText, false);
    }

    public static String formatPercent(String text, String defaultText, boolean signed) {
        if (text == null) {
            return defaultText;
        }
        final NumberFormat nf = signed ? FORMAT_PERCENT_SIGNED : FORMAT_PERCENT;
        try {
            final String s = nf.format(Double.parseDouble(text));
            return s.endsWith("%") ? s : s + "%"; // bug in gwt, '%' missing for negative values
        } catch (NumberFormatException e) {
            return defaultText;
        }
    }

    public static String formatDateAsISODay(Date d) {
        return format(FORMAT_ISO_DAY, d);
    }

    public static Date parseDay(String date) {
        if (date.matches("[0-9]{4}-[0-9]{2}-[0-9]{2}")) { // $NON-NLS$
            try {
                return parse(FORMAT_ISO_DAY, date);
            }
            catch (Exception e) {
                Firebug.error("cannot parse ISO day: " + date, e);
            }
        }
        if (date.matches("[0-9]{1,2}\\.[0-9]{1,2}\\.[0-9]{4}")) { // $NON-NLS$
            try {
                return parse(FORMAT_DE_DAY, date);
            }
            catch (Exception e) {
                Firebug.error("cannot parse de day: " + date, e);
            }
        }
        return null;
    }

    public static String formatDateAsDEDate(Date d) {
        return format(FORMAT_DE_DAY, d);
    }

    public static String formatDateAsISODate(Date d) {
        return format(FORMAT_ISO_DATE, d);
    }

    public static String formatDateAsISODate00(Date d) {
        return format(FORMAT_ISO_DATE_00, d);
    }

    public static String formatTime(Date d) {
        return format(FORMAT_TIME, d);
    }

    public static String formatTimeHhmm(Date d) {
        return format(FORMAT_TIME_HHMM, d);
    }

    public static Date parseISODate(String s) {
        return parse(FORMAT_ISO_DATE, s);
    }

    public static String formatYear(Date d) {
        return format(FORMAT_YEAR, d);
    }

    private static String format(DateTimeFormat dtf, Date d) {
        return (d != null) ? dtf.format(d) : "--";
    }

    private static Date parse(DateTimeFormat dtf, String s) {
        return (s != null) ? dtf.parse(s) : null;
    }

    public static String formatDateTime(Date date) {
        return formatDateTime(date, Formatter.FORMAT_TIME);
    }

    public static String formatDateTimeHHMM(Date date) {
        return formatDateTime(date, Formatter.FORMAT_TIME_HHMM);
    }

    private static String formatDateTime(Date date, DateTimeFormat formatTime) {
        String dateString = Formatter.LF.formatDate(date);
        if(!new MmJsDate(date).isMidnight()) {
            dateString += " " + formatTime.format(date);
        }

        return dateString;
    }
}
