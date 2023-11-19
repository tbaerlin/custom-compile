/*
 * PastValidator.java
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
public class PastValidator extends AbstractValidator<Past> {

    public void validateValue(Object value, Errors errors) {
        if ( value == null ) {
            return;
        }

        if (!isBeforeNow(value)) {
            errors.rejectValue(
                    getPropertyName(),
                    "validator.past", getPropertyName() + " not a past date: " + value);
        }
    }

    private boolean isBeforeNow(Object value) {
        if ( value instanceof String ) {
            try {
                Date date = DateFormat.getTimeInstance().parse( (String) value );
                return date.before( new Date() );
            }
            catch (ParseException nfe) {
                return false;
            }
        }
        else if ( value instanceof Date ) {
            Date date = (Date) value;
            return date.before( new Date() );
        }
        else if ( value instanceof Calendar ) {
            Calendar cal = (Calendar) value;
            return cal.before( Calendar.getInstance() );
        }
        else {
            return false;
        }
    }
}
