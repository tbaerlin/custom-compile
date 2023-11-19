package de.marketmaker.itools.gwtutil.client.i18n;

import com.google.gwt.i18n.client.LocaleInfo;
import de.marketmaker.itools.gwtutil.client.util.date.GwtDateParser;
import de.marketmaker.itools.gwtutil.client.util.date.JsDate;
import de.marketmaker.itools.gwtutil.client.util.date.MmJsDate;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author Ulrich Maurer
 *         Date: 09.06.11
 */
public abstract class LocalizedFormatter {
    protected static final int CURRENT_YEAR = new MmJsDate().getFullYear();
    protected static final String CURRENT_YEAR_S = String.valueOf(CURRENT_YEAR);

    private static final char EN_DECIMAL_SEPARATOR = '.';
    private static final char EN_GROUPING_SEPARATOR = ',';
    private static final char LOCALE_DECIMAL_SEPARATOR = getSeparator(LocaleInfo.getCurrentLocale().getNumberConstants().decimalSeparator(), '.');
    private static final char LOCALE_GROUPING_SEPARATOR = getSeparator(LocaleInfo.getCurrentLocale().getNumberConstants().groupingSeparator(), ',');

    private final char dateSeparator;
    private final char timeSeparator;
    private final char timeMillisSeparator;

    private static LocalizedFormatter instance;

    public static LocalizedFormatter getInstance() {
        if (instance == null) {
            if (LocaleInfo.getCurrentLocale().getLocaleName().startsWith("de")) {
                instance = new LocalizedFormatter_de();
            }
            else if (LocaleInfo.getCurrentLocale().getLocaleName().startsWith("it")) {
                instance = new LocalizedFormatter_it();
            }
            else {
                instance = new LocalizedFormatter_en();
            }
        }
        return instance;
    }

    protected LocalizedFormatter(char dateSeparator, char timeSeparator, char timeMillisSeparator) {
        this.dateSeparator = dateSeparator;
        this.timeSeparator = timeSeparator;
        this.timeMillisSeparator = timeMillisSeparator;
    }

    public char getDateSeparator() {
        return this.dateSeparator;
    }

    public char getTimeSeparator() {
        return this.timeSeparator;
    }

    public char getTimeMillisSeparator() {
        return this.timeMillisSeparator;
    }

    protected String[] getYmdHms(String date) {
        if (date == null) {
            return null;
        }
        return GwtDateParser.getYmdHms(date);
    }

    public String formatDate(String date) {
        return formatDateDdMmYyyy(date, "-");
    }

    public String formatDateYyyy(String date, String defaultText) {
        final String[] ymd = getYmdHms(date);
        if (ymd == null) {
            return "";
        }
        return ymd[0];
    }

    public String formatDateDdMm(String date, String defaultText) {
        final String[] ymd = getYmdHms(date);
        if (ymd == null) {
            return defaultText;
        }
        return formatDateDdMm(ymd);
    }

    public String formatDateDdMmYy(String date, String defaultText) {
        final String[] ymd = getYmdHms(date);
        if (ymd == null) {
            return defaultText;
        }
        return formatDateDdMmYy(ymd);
    }

    public String formatDateDdMmYyyy(String date, String defaultText) {
        final String[] ymd = getYmdHms(date);
        if (ymd == null) {
            return defaultText;
        }
        return formatDateDdMmYyyy(ymd);
    }

    public String formatTimestamp(String timestamp, String defaultText) {
        final String[] ymd = getYmdHms(timestamp);
        if (ymd == null || ymd.length < 3) {
            return defaultText;
        }

        StringBuilder sb = new StringBuilder();
        sb.append(ymd[2]);
        sb.append(dateSeparator);
        sb.append(ymd[1]);
        sb.append(dateSeparator);
        sb.append(ymd[0]);

        if ((ymd.length >= 6) && !isMidnight(ymd[3], ymd[4], ymd[5])) {
            sb.append(' ');
            sb.append(ymd[3]);
            sb.append(timeSeparator);
            sb.append(ymd[4]);
            sb.append(timeSeparator);
            sb.append(ymd[5]);
        }
        return sb.toString();
    }

    public boolean isMidnight(String h, String m, String s) {
        return ("0".equals(h) || "00".equals(h))
                && ("0".equals(m) || "00".equals(m))
                && ("0".equals(s) || "00".equals(s));
    }


    public String formatDateShort(String date, String defaultText) {
        final String[] ymd = getYmdHms(date);
        if (ymd == null) {
            return defaultText;
        }
        return CURRENT_YEAR_S.equals(ymd[0])
                ? formatDateDdMm(ymd)
                : formatDateDdMmYy(ymd);
    }
    
    public void appendDateShort(StringBuilder sb, JsDate date) {
        if (date.getFullYear() == CURRENT_YEAR) {
            appendDdmm(sb, date);
        }
        else {
            appendDdmmyy(sb, date);
        }
    }
    
    public abstract String formatDateShort(String date);

    public abstract String formatDate(Date date);

    public abstract String formatDateDdMm(String date);

    abstract String formatDateDdMm(String[] ymd);

    public abstract String formatDateDdMmYy(String date);

    abstract String formatDateDdMmYy(String[] ymd);

    public abstract String formatDateDdMmYyyy(String date);

    abstract String formatDateDdMmYyyy(String[] ymd);

    public abstract void appendDdmm(StringBuilder sb, JsDate date);

    public abstract void appendDdmmyy(StringBuilder sb, JsDate date);

    public abstract void appendDdmmyyyy(StringBuilder sb, JsDate date);

    private static char getSeparator(String sep, char defaultValue) {
        return sep.length() != 1 ? defaultValue : sep.charAt(0);
    }

    public String formatDecimal(BigDecimal bd, boolean removeTrailingZeros) {
        return bd == null
                ? null
                : formatDecimal(bd.toPlainString(), removeTrailingZeros);
    }

    public String formatDecimal(String rawValue) {
        return formatDecimal(rawValue, true);
    }

    public String formatDecimal(String rawValue, boolean removeTrailingZeros) {
        final String value = removeTrailingZeros ? removeDecimalTrailingZeros(rawValue) : rawValue;

        final StringBuilder sbInteger = new StringBuilder();
        final StringBuilder sbDecimal = new StringBuilder();
        StringBuilder sb = sbInteger;
        final char[] chars = value.toCharArray();
        boolean negativeValue = false;
        for (char c : chars) {
            if (c == '-') {
                negativeValue = true;
            }
            else if (c == EN_DECIMAL_SEPARATOR) {
                sb = sbDecimal;
            }
            else if (c != EN_GROUPING_SEPARATOR) {
                sb.append(c);
            }
        }
        int pos = 0;
        int g = sbInteger.length() % 3;
        final int negativeLength = negativeValue ? 1 : 0;
        final int integerLength = sbInteger.length() + (sbInteger.length() - 1) / 3;
        final int decimalLength = sbDecimal.length() == 0 ? 0 : (sbDecimal.length() + 1);
        final char[] result = new char[negativeLength + integerLength + decimalLength];
        if (negativeValue) {
            result[pos++] = '-';
        }
        for (int i = 0, len = sbInteger.length(); i < len; i++) {
            if (g == 0) {
                if (i > 0) {
                    result[pos++] = LOCALE_GROUPING_SEPARATOR;
                }
                g = 3;
            }
            g--;
            result[pos++] = sbInteger.charAt(i);
        }
        if (decimalLength > 0) {
            result[pos++] = LOCALE_DECIMAL_SEPARATOR;
            for (int i = 0, len = sbDecimal.length(); i < len; i++) {
                result[pos++] = sbDecimal.charAt(i);
            }
        }
        return new String(result);
    }

    private static String removeDecimalTrailingZeros(String value) {
        if(value.lastIndexOf(EN_DECIMAL_SEPARATOR) > -1 && '0' == value.charAt(value.length() - 1)) {
            final StringBuilder sb = new StringBuilder(value);
            while('0' == sb.charAt(sb.length() - 1)) {
                sb.deleteCharAt(sb.length() - 1);
            }
            return sb.toString();
        }
        return value;
    }

    public String parseDecimal(String value) {
        final StringBuilder sb = new StringBuilder(value.length());
        for (char c : value.toCharArray()) {
            if (c == LOCALE_DECIMAL_SEPARATOR) {
                sb.append(EN_DECIMAL_SEPARATOR);
            }
            else if (c != LOCALE_GROUPING_SEPARATOR) {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    public String getPlaceholderDmy() {
        return "mm/dd/yyyy";
    }

    public String getPlaceholderIsoDay() {
        return "yyyy-mm-dd";
    }

    public String getPlaceholderHm() {
        return "hh:mm";
    }

    public String getPlaceholderHms() {
        return "hh:mm:ss";
    }
}
