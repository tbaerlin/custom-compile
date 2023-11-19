/*
 * MinValidator.java
 *
 * Created on 31.07.2006 17:17:09
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.common.validator;

import org.springframework.validation.Errors;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class MinValidator extends AbstractValidator<Min> {

    private int min;

    protected void doInitialize(Min parameters) {
        this.min = parameters.value();
    }

    public void validateValue(Object value, Errors errors) {
        if ( value == null ) {
            return;
        }
        if (!isMin(value)) {
            errors.rejectValue(
                    getPropertyName(),
                    "validator.min", new Object[] { min, value },
                    getPropertyName() + ": " + value + " is not >= " + min);
        }
    }

    private boolean isMin(Object value) {
        if ( value instanceof String ) {
            try {
                double dv = Double.parseDouble( (String) value );
                return dv >= min;
            }
            catch (NumberFormatException nfe) {
                return false;
            }
        }
        else if ( ( value instanceof Double ) || ( value instanceof Float ) ) {
            double dv = ( (Number) value ).doubleValue();
            return dv >= min;
        }
        else if ( value instanceof Number ) {
            long lv = ( (Number) value ).longValue();
            return lv >= min;
        }
        else {
            return false;
        }

    }

}
