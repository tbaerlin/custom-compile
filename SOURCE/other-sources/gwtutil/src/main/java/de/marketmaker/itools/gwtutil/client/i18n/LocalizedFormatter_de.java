package de.marketmaker.itools.gwtutil.client.i18n;

import java.util.Date;

import com.google.gwt.i18n.client.DateTimeFormat;
import de.marketmaker.itools.gwtutil.client.util.date.JsDate;

/**
 * @author Ulrich Maurer
 *         Date: 09.06.11
 */
public class LocalizedFormatter_de extends LocalizedFormatter {
    private final DateTimeFormat FORMAT_DATE = DateTimeFormat.getFormat("dd.MM.yyyy"); // $NON-NLS$

    public LocalizedFormatter_de() {
        super('.', ':', '.');
    }

    @Override
    public String formatDate(Date date) {
        return FORMAT_DATE.format(date);
    }

    protected String formatDateDdMm(String[] ymd) {
        return ymd[2] + "." + ymd[1] + ".";
    }


    public String formatDateDdMm(String date) {
        return formatDateDdMm(date, "--.--.");
    }

    protected String formatDateDdMmYy(String[] ymd) {
        return ymd[2] + "." + ymd[1] + "." + ymd[0].substring(2);
    }


    public String formatDateDdMmYy(String date) {
        return formatDateDdMmYy(date, "--.--.--");
    }

    protected String formatDateDdMmYyyy(String[] ymd) {
        return ymd[2] + "." + ymd[1] + "." + ymd[0];
    }


    public String formatDateDdMmYyyy(String date) {
        return formatDateDdMmYyyy(date, "--.--.----");
    }
    
    public String formatDateShort(String date) {
        return formatDateShort(date, "--.--.");
    }

    public void appendDdmm(StringBuilder sb, JsDate date) {
        final int dayOfMonth = date.getDate();
        final int month = date.getMonth() + 1;
        if (dayOfMonth < 10) {
            sb.append('0');
        }
        sb.append(dayOfMonth).append('.');
        if (month < 10) {
            sb.append('0');
        }
        sb.append(month).append('.');
    }

    public void appendDdmmyy(StringBuilder sb, JsDate date) {
        final String year = String.valueOf(date.getFullYear());
        appendDdmm(sb, date);
        sb.append(year.substring(year.length() - 2, year.length()));
    }

    public void appendDdmmyyyy(StringBuilder sb, JsDate date) {
        final int year = date.getFullYear();
        appendDdmm(sb, date);
        sb.append(year);
    }

    @Override
    public String getPlaceholderIsoDay() {
        return "jjjj-mm-tt";
    }

    @Override
    public String getPlaceholderDmy() {
        return "tt.mm.jjjj";
    }
}
