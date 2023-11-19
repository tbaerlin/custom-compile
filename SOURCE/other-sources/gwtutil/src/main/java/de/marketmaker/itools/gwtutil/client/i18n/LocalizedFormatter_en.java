package de.marketmaker.itools.gwtutil.client.i18n;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.google.gwt.i18n.client.DateTimeFormat;
import de.marketmaker.itools.gwtutil.client.util.date.JsDate;
import de.marketmaker.itools.gwtutil.client.util.date.JsDateFormatter;

/**
 * @author Ulrich Maurer
 *         Date: 09.06.11
 */
public class LocalizedFormatter_en extends LocalizedFormatter {
    private final DateTimeFormat FORMAT_DATE = DateTimeFormat.getFormat("MMM/d/yyyy"); // $NON-NLS$
    private final Map<String, String> MONTHS = new HashMap<>(12);

    public LocalizedFormatter_en() {
        super('/', ':', '.');
        for (int i = 0, length = JsDateFormatter.MONTH_NAMES_SHORT.length; i < length; i++) {
            MONTHS.put(String.valueOf(i + 1), JsDateFormatter.MONTH_NAMES_SHORT[i]);
        }
    }

    private String removeLeadingZero(String s) {
        assert(s.length() == 2);
        return s.charAt(0) == '0' ? s.substring(1) : s;
    }

    @Override
    protected String[] getYmdHms(String date) {
        if (date == null) {
            return null;
        }
        final String[] ymd = super.getYmdHms(date);
        ymd[1] = removeLeadingZero(ymd[1]);
        ymd[2] = removeLeadingZero(ymd[2]);
        return ymd;
    }

    private String getMonthNumber(String monthNameShort) {
        for (int i = 0, length = JsDateFormatter.MONTH_NAMES_SHORT_EN.length; i < length; i++) {
            final String month = JsDateFormatter.MONTH_NAMES_SHORT_EN[i];
            if (month.equals(monthNameShort)) {
                return String.valueOf(i + 1);
            }
        }
        throw new IllegalArgumentException("Unknown short month name: " + monthNameShort);
    }

    private String getMonthAbbr(final String[] ymd) {
        return MONTHS.get(ymd[1]);
    }

    private String getYearAbbr(final String[] ymd) {
        return removeLeadingZero(ymd[0].substring(2));
    }


    public String formatDate(Date date) {
        return FORMAT_DATE.format(date);
    }

    protected String formatDateDdMm(String[] ymd) {
        return getMonthAbbr(ymd) + "/" + ymd[2];
    }


    public String formatDateDdMm(String date) {
        return formatDateDdMm(date, "-/-");
    }

    protected String formatDateDdMmYy(String[] ymd) {
        return ymd[1] + "/" + ymd[2] + "/" + getYearAbbr(ymd);
    }


    public String formatDateDdMmYy(String date) {
        return formatDateDdMmYy(date, "-/-/-");
    }

    protected String formatDateDdMmYyyy(String[] ymd) {
        return getMonthAbbr(ymd) + "/" + ymd[2] + "/" + ymd[0];
    }


    @Override
    public String formatDateDdMmYyyy(String date) {
        return formatDateDdMmYyyy(date, "-/-/-");
    }

    public String formatDateShort(String date) {
        return formatDateShort(date, "-/-");
    }

    public void appendDdmm(StringBuilder sb, JsDate date) {
        final int dayOfMonth = date.getDate();
        final int month = date.getMonth() + 1;
        sb.append(month).append('/').append(dayOfMonth);
    }

    public void appendDdmmyy(StringBuilder sb, JsDate date) {
        final int year = date.getFullYear();
        appendDdmm(sb, date);
        sb.append('/').append(year % 100);
    }

    public void appendDdmmyyyy(StringBuilder sb, JsDate date) {
        final int year = date.getFullYear();
        appendDdmm(sb, date);
        sb.append('/').append(year);
    }
}
