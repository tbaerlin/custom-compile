/*
 * PeriodEditor.java
 *
 * Created on 19.03.2007 12:34:55
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.common.util;

import org.joda.time.LocalDate;
import org.joda.time.format.ISODateTimeFormat;
import org.springframework.util.StringUtils;

import java.beans.PropertyEditorSupport;

/**
 * Converts between Strings and LocalDates.
 * format - yyy-MM-dd
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class LocalDateEditor extends PropertyEditorSupport {
    public String getAsText() {
        final LocalDate date = (LocalDate)  getValue();
        if (date == null) {
            return null;
        }
        return ISODateTimeFormat.date().print(date);
    }

    public void setAsText(String text) throws IllegalArgumentException {
        if (!StringUtils.hasText(text)) {
            return;
        }
            super.setValue(ISODateTimeFormat.date().parseDateTime(text).toLocalDate());
    }

    public static void main(String[] args) {
        LocalDateEditor e = new LocalDateEditor();
        e.setAsText("2007-03-27");
        LocalDate dt = (LocalDate) e.getValue();
        System.out.println(dt);
    }
}