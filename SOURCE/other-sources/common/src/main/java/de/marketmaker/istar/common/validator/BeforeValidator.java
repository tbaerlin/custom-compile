/*
 * BeforeValidator.java
 *
 * Created on 31.07.2006 17:17:09
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.common.validator;

import java.util.Calendar;
import java.util.Date;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.springframework.beans.BeanWrapper;
import org.springframework.validation.Errors;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class BeforeValidator extends AbstractValidator<Before> {
    private String otherPropertyName;

    @Override
    protected void doInitialize(Before parameters) {
        this.otherPropertyName = parameters.value();
    }

    @Override
    public void validate(BeanWrapper bw, Errors errors) {
        final Object value = getValue(bw);
        if (value == null) {
            return;
        }
        final Object other = getValue(bw, this.otherPropertyName);
        if (other == null) {
            return;
        }
        if (value.getClass() != other.getClass()) {
            errors.rejectValue(getPropertyName(),
                    "validator.before", getPropertyName() + " and  " + this.otherPropertyName
                            + " are incompatible");
            return;
        }

        if (!isBefore(value, other)) {
            errors.rejectValue(getPropertyName(), "validator.before", value + " not before " + other);
        }
    }

    private boolean isBefore(Object value, Object other) {
        if (value instanceof DateTime) {
            return ((DateTime)value).isBefore((DateTime)other);
        }
        if (value instanceof LocalDate) {
            return ((LocalDate)value).isBefore((LocalDate)other);
        }
        if (value instanceof Date) {
            return ((Date)value).before((Date)other);
        }
        if (value instanceof Calendar) {
            return ((Calendar)value).before(other);
        }
        return false;
    }
}