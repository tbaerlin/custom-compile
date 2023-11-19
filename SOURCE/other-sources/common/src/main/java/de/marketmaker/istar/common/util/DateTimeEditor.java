/*
 * PeriodEditor.java
 *
 * Created on 19.03.2007 12:34:55
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.common.util;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.springframework.util.StringUtils;

import java.beans.PropertyEditorSupport;

/**
 * Converts between Strings and {@link org.joda.time.DateTime} objects.
 * Supported formats:
 * <dl>
 * <dt>yyyy-MM-dd'T'HH:mm:ssZZ
 * <dd>date with time, no millis, and a time zone offest, e.g. <tt>2007-01-01T08:02:01+01:00</tt>
 * <dt>yyy-MM-dd'T'HH:mm:ss
 * <dd>date with time, no millis, w/o time zone offset (uses local offset), e.g. <tt>2007-01-01T08:02:01</tt>
 * <dt>yyyy-MM-dd
 * <dd>just a date, time will be midnight in local time zone, e.g., <tt>2007-01-01</tt>
 * </dl>
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class DateTimeEditor extends PropertyEditorSupport {
    private final DateTimeFormatter DTF = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss");

    public String getAsText() {
        final DateTime dateTime = (DateTime) getValue();
        if (dateTime == null) {
            return null;
        }
        return ISODateTimeFormat.dateTimeNoMillis().print(dateTime);
    }

    public void setAsText(String text) throws IllegalArgumentException {
        if (!StringUtils.hasText(text)) {
            return;
        }
        if (text.length() == 10) {
            setValue(ISODateTimeFormat.date().parseDateTime(text));
        }
        else if (isWithZoneOffset(text)) {
            setValue(ISODateTimeFormat.dateTimeNoMillis().parseDateTime(text));
        } else {
            setValue(DTF.parseDateTime(text));
        }
    }

    private boolean isWithZoneOffset(String text) {
        return text.length() > 19 && (text.lastIndexOf('+') == 19 || text.lastIndexOf('-') == 19);
    }

    public static void main(String[] args) {
        DateTimeEditor e = new DateTimeEditor();
        e.setAsText("2007-01-01");
        DateTime dt = (DateTime) e.getValue();
        System.out.println(dt);
    }
}