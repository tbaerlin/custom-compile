/*
 * PatternValidator.java
 *
 * Created on 31.07.2006 17:17:09
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.common.validator;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.validation.Errors;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class DateFormatValidator extends AbstractValidator<DateFormat> {

    private String format;

    private DateTimeFormatter formatter;

    private static final String ERROR_CODE = "validator.dateFormat";

    protected void doInitialize(DateFormat parameters) {
        this.format = parameters.format();
        this.formatter = DateTimeFormat.forPattern(this.format);
    }

    public void validateValue(Object value, Errors errors) {
        if (value == null || !(value instanceof String)) {
            return;
        }
        try {
            this.formatter.parseDateTime((String) value);
        } catch (Exception e) {
            errors.rejectValue(
                    getPropertyName(),
                    ERROR_CODE, new Object[]{this.format, value},
                    getPropertyName() + ": " + value
                            + " is invalid using: " + this.format);
        }
    }

}
