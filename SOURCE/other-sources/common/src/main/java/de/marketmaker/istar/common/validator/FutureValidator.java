/*
 * FutureValidator.java
 *
 * Created on 31.07.2006 17:17:09
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.common.validator;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

import org.springframework.validation.Errors;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class FutureValidator extends AbstractValidator<Future> {

    public void validateValue(Object value, Errors errors) {
        if (value == null) {
            return;
        }
        if (!isAfterNow(value)) {
            errors.rejectValue(
                    getPropertyName(),
                    "validator.future", getPropertyName() + " not a future date: " + value);
        }
    }

    private boolean isAfterNow(Object value) {
        if (value instanceof String) {
            try {
                Date date = DateFormat.getTimeInstance().parse((String) value);
                return date.after(new Date());
            }
            catch (ParseException nfe) {
                return false;
            }
        }
        else if (value instanceof Date) {
            Date date = (Date) value;
            return date.after(new Date());
        }
        else if (value instanceof Calendar) {
            Calendar cal = (Calendar) value;
            return cal.after(Calendar.getInstance());
        }
        else {
            return false;
        }
    }
}
