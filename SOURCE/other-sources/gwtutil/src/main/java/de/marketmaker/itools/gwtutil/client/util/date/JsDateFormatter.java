package de.marketmaker.itools.gwtutil.client.util.date;


import de.marketmaker.itools.gwtutil.client.i18n.GwtUtilI18n;
import de.marketmaker.itools.gwtutil.client.i18n.LocalizedFormatter;

/**
 * @author umaurer
 */
public class JsDateFormatter {
    public static final String PATTERN_ISO_DAY = "([0-9]{1,4})-([0-9]{1,2})-([0-9]{1,2})";
    public static final String PATTERN_ISO_DATE_TIME = "([0-9]{1,4})-([0-9]{1,2})-([0-9]{1,2})T([0-9]{1,2}):([0-9]{1,2}):([0-9]{1,2})[+-][0-9]{2}:[0-9]{2}";

    public static final String[] MONTH_NAMES = GwtUtilI18n.I.monthNames().split(",");
    
    public static final String[] MONTH_NAMES_SHORT = GwtUtilI18n.I.monthNamesShort().split(",");
    public static final String[] MONTH_NAMES_SHORT_EN = new String[]{"Jan", "Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"};

    public static final String[] DAY_NAMES_1 = GwtUtilI18n.I.dayNames1().split(",");

    public enum Format {
        DMY(LocalizedFormatter.getInstance().getPlaceholderDmy()),
        ISO_DAY(LocalizedFormatter.getInstance().getPlaceholderIsoDay());

        private final String placeholder;

        Format(String placeholder) {
            this.placeholder = placeholder;
        }

        public String getPlaceholder() {
            return placeholder;
        }
    }

    private static void appendIsoDay(StringBuilder sb, JsDate date) {
        final int dayOfMonth = date.getDate();
        final int month = date.getMonth() + 1;
        final int year = date.getFullYear();
        sb.append(year).append('-');
        if (month < 10) {
            sb.append('0');
        }
        sb.append(month).append('-');
        if (dayOfMonth < 10) {
            sb.append('0');
        }
        sb.append(dayOfMonth);
    }

    private static void appendHhmmssSSS(StringBuilder sb, JsDate date) {
        final int millis = date.getMilliseconds();
        appendHhmmss(sb, date);
        sb.append('.');
        if (millis < 10) {
            sb.append("00");
        }
        else if (millis < 100) {
            sb.append('0');
        }
        sb.append(millis);
    }

    private static void appendHhmmss(StringBuilder sb, JsDate date) {
        final int seconds = date.getSeconds();
        appendHhmm(sb, date);
        sb.append(':');
        if (seconds < 10) {
            sb.append('0');
        }
        sb.append(seconds);
    }

    private static void appendHhmm(StringBuilder sb, JsDate date) {
        final int hours = date.getHours();
        final int minutes = date.getMinutes();
        if (hours < 10) {
            sb.append('0');
        }
        sb.append(hours).append(':');
        if (minutes < 10) {
            sb.append('0');
        }
        sb.append(minutes);
    }

    private static void appendTimezoneOffset(StringBuilder sb, JsDate date, boolean withColon) {
        int timezoneOffset = date.getTimezoneOffset();
        if (timezoneOffset < 0) {
            timezoneOffset = -timezoneOffset;
            sb.append('+');
        }
        else {
            sb.append('-');
        }
        final int timezoneHours = timezoneOffset / 60;
        final int timezoneMinutes = timezoneOffset % 60;
        if (timezoneHours < 10) {
            sb.append('0');
        }
        sb.append(timezoneHours);
        if (withColon) {
            sb.append(':');
        }
        if (timezoneMinutes < 10) {
            sb.append('0');
        }
        sb.append(timezoneMinutes);
    }

    public static String format(JsDate date, boolean allowNull, Format format) {
        if (date == null && allowNull) {
            return null;
        }
        switch (format) {
            case DMY:
                return formatDdmmyyyy(date);
            case ISO_DAY:
                return formatIsoDay(date);
            default:
                throw new IllegalArgumentException("unhandled date format: " + format);
        }
    }

    public static String format(JsDate date, Format format) {
        switch (format) {
            case DMY:
                return formatDdmmyyyy(date);
            case ISO_DAY:
                return formatIsoDay(date);
            default:
                throw new IllegalArgumentException("unhandled date format: " + format);
        }
    }

    public static String formatDdmmyyyy(JsDate date) {
        final StringBuilder sb = new StringBuilder();
        LocalizedFormatter.getInstance().appendDdmmyyyy(sb, date);
        return sb.toString();
    }

    public static String formatDdmmyyyy(JsDate date, boolean allowNull) {
        return date == null && allowNull ? null : formatDdmmyyyy(date);
    }

    public static String formatDdmm(JsDate date) {
        final StringBuilder sb = new StringBuilder();
        LocalizedFormatter.getInstance().appendDdmm(sb, date);
        return sb.toString();
    }
    
    public static String formatDateShort(JsDate date) {
        final StringBuilder sb = new StringBuilder();
        LocalizedFormatter.getInstance().appendDateShort(sb, date);
        return sb.toString();
    }

    public static String formatIsoDay(JsDate date) {
        final StringBuilder sb = new StringBuilder();
        appendIsoDay(sb, date);
        return sb.toString();
    }

    public static String formatIsoDateTime(JsDate date) {
        final StringBuilder sb = new StringBuilder();
        appendIsoDay(sb, date);
        sb.append('T');
        appendHhmmss(sb, date);
        appendTimezoneOffset(sb, date, false);
        return sb.toString();
    }

    public static String formatIso8601(JsDate date) {
        final StringBuilder sb = new StringBuilder();
        appendIsoDay(sb, date);
        sb.append('T');
        appendHhmmssSSS(sb, date);
        appendTimezoneOffset(sb, date, true);
        return sb.toString();
    }

    public static String formatIsoDateTimeMidnight(JsDate date) {
        final StringBuilder sb = new StringBuilder();
        appendIsoDay(sb, date);
        sb.append("T00:00:00");
        appendTimezoneOffset(sb, date, false);
        return sb.toString();
    }

    public static String formatHhmmss(JsDate date) {
        final StringBuilder sb = new StringBuilder();
        appendHhmmss(sb, date);
        return sb.toString();
    }

    public static String formatHhmm(JsDate date) {
        final StringBuilder sb = new StringBuilder();
        appendHhmm(sb, date);
        return sb.toString();
    }

    public static String formatDdmmyyyyHhmmss(JsDate date) {
        final StringBuilder sb = new StringBuilder();
        LocalizedFormatter.getInstance().appendDdmmyyyy(sb, date);
        sb.append(' ');
        appendHhmmss(sb, date);
        return sb.toString();
    }

    public static String formatDdmmyyyyHhmm(JsDate date) {
        final StringBuilder sb = new StringBuilder();
        LocalizedFormatter.getInstance().appendDdmmyyyy(sb, date);
        sb.append(' ');
        appendHhmm(sb, date);
        return sb.toString();
    }

    public static MmJsDate parseDdmmyyyy(String s, boolean allowNull) {
        return parseDdmmyyyy(s, allowNull, GwtDateParser.MSG);
    }

    public static MmJsDate parseDdmmyyyy(String s, boolean allowNull, GwtDateParser.Msg msg) {
        if (allowNull && (s == null || s.trim().isEmpty())) {
            return null;
        }
        return GwtDateParser.getMmJsDate(s.trim(), msg);
    }

    public static MmJsDate parseDdmmyyyy(String s) {
        return parseDdmmyyyy(s, GwtDateParser.MSG);
    }

    public static MmJsDate parseDdmmyyyy(String s, GwtDateParser.Msg msg) {
        return parseDdmmyyyy(s, false, msg);
    }

    public static int getMonthNumberEn(String monthNameShort) {
        for (int i = 0, length = JsDateFormatter.MONTH_NAMES_SHORT_EN.length; i < length; i++) {
            final String month = JsDateFormatter.MONTH_NAMES_SHORT_EN[i];
            if (month.equals(monthNameShort)) {
                return i + 1;
            }
        }
        throw new IllegalArgumentException("Unknown short month name: " + monthNameShort);
    }

}
