/*
 * NotZeroValidator.java
 *
 * Created on 4/2/14 4:44 PM
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.common.validator;

import org.springframework.validation.Errors;

/**
 * @author Stefan Willenbrock
 */
public class NotZeroValidator extends AbstractValidator<NotZero> {

    @Override
    public void validateValue(Object value, Errors errors) {
        if (isZero(value)) {
            errors.rejectValue(
                    getPropertyName(),
                    "validator.notZero", new Object[] { value },
                    getPropertyName() + ": " + value + " is zero");
        }
    }

    private boolean isZero(Object value) {
        if ( value instanceof String ) {
            try {
                double dv = Double.parseDouble( (String) value );
                return dv == 0;
            } catch (NumberFormatException nfe) {}
        } else if ( ( value instanceof Double ) || ( value instanceof Float ) ) {
            double dv = ( (Number) value ).doubleValue();
            return dv == 0;
        } else if (value instanceof Number) {
            long lv = ( (Number) value ).longValue();
            return lv == 0;
        }
        return false;
    }
}
