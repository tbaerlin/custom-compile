/*
 * RangeValidator.java
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
public class RangeValidator extends AbstractValidator<Range> {
    private long max;
    private long min;

    protected void doInitialize(Range parameters) {
        max = parameters.max();
        min = parameters.min();
    }

    public void validateValue(Object value, Errors errors) {
        if ( value == null ) {
            return;
        }
        if (!isInRange(value)) {
            errors.rejectValue(
                    getPropertyName(),
                    "validator.range", new Object[] { this.min, this.max, value },
                    getPropertyName() + ": " + value
                            + " is not >= " + this.min + " AND <= " + this.max);
        }
    }

    private boolean isInRange(Object value) {
        if ( value instanceof String ) {
            try {
                double dv = Double.parseDouble( (String) value );
                return dv >= min && dv <= max;
            }
            catch (NumberFormatException nfe) {
                return false;
            }
        }
        else if ( ( value instanceof Double ) || ( value instanceof Float ) ) {
            double dv = ( (Number) value ).doubleValue();
            return dv >= min && dv <= max;
        }
        else if ( value instanceof Number ) {
            long lv = ( (Number) value ).longValue();
            return lv >= min && lv <= max;
        }
        else if ( value instanceof String[] ) {
            for (String v : (String[]) value) {
                if (!isInRange(v)) {
                    return false;
                }
            }
            return true;
        }
        else {
            return false;
        }
    }
}
