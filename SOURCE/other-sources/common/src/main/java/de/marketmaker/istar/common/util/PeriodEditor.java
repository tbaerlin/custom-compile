/*
 * PeriodEditor.java
 *
 * Created on 19.03.2007 12:34:55
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.common.util;

import java.beans.PropertyEditorSupport;

import org.joda.time.Period;
import org.joda.time.format.ISOPeriodFormat;
import org.springframework.util.StringUtils;

/**
 * Converts between Strings and Periods using the standard ISO-8601
 * period format - PyYmMwWdDTHmMsS.
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class PeriodEditor extends PropertyEditorSupport {
    public static Period fromText(String s) {
        final PeriodEditor editor = new PeriodEditor();
        editor.setAsText(s);
        return (Period) editor.getValue();
    }

    public String getAsText() {
        final Period period = (Period) getValue();
        if (period== null) {
            return null;
        }
        return ISOPeriodFormat.standard().print(period);
    }

    public void setAsText(String text) throws IllegalArgumentException {
        if (!StringUtils.hasText(text) || "MAX".equals(text) || "PMX".equals(text)) {
            return;
        }
        final String s = text.toUpperCase();
        // for historical reasons, some clients may just submit 3M instead of P3M: ensure P
        setValue(ISOPeriodFormat.standard().parsePeriod(s.startsWith("P") ? s : "P" + s));
    }
}
