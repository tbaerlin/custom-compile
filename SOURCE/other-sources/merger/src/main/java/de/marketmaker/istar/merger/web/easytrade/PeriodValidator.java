/*
 * PeriodValidator.java
 *
 * Created on 16.11.2006 10:24:40
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.joda.time.format.ISOPeriodFormat;
import org.springframework.validation.Errors;

import de.marketmaker.istar.common.validator.AbstractValidator;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class PeriodValidator extends AbstractValidator<Period> {
    public static final Pattern PERIOD_W_ENDDATE = Pattern.compile("^(\\d{4}-\\d{2}-\\d{2})(P.+)");

    private static final String CODE = "validator.period";

    public void validateValue(Object value, Errors errors) {
        if (value == null) {
            return;
        }
        if (value instanceof String) {
            final String s = (String) value;
            if (!isValid(s)) {
                reject(errors, s);
            }
            return;
        }
        if (value instanceof String[]) {
            final String[] formats = (String[]) value;
            for (int i = 0; i < formats.length; i++) {
                final String format = formats[i];
                if (format != null && !isValid(format)) {
                    reject(errors, i + " in " + Arrays.toString(formats));
                    return;
                }
            }
            return;
        }
        reject(errors, value.toString());
    }

    private void reject(Errors errors, String s) {
        errors.rejectValue(getPropertyName(), CODE, getPropertyName() + " invalid: " + s);
    }

    private boolean isValid(String s) {
        final Matcher m = PERIOD_W_ENDDATE.matcher(s);
        final String period;
        if (m.matches() && m.groupCount() == 2) {
            period = m.group(2);
        }
        else {
            period = ensureFormat(s);
        }
        try {
            ISOPeriodFormat.standard().parsePeriod(period);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private String ensureFormat(String value) {
        final String s = value.toUpperCase();
        return s.startsWith("P") ? s : "P" + s;
    }
}
