/*
 * MaxValidator.java
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
public class MaxValidator extends AbstractValidator<Max> {

    private int max;

    protected void doInitialize(Max parameters) {
        max = parameters.value();
    }

    public void validateValue(Object value, Errors errors) {
        if (value == null) {
            return;
        }
        if (!isMax(value)) {
            errors.rejectValue(
                    getPropertyName(),
                    "validator.max", new Object[] { max, value },
                    getPropertyName() + ": " + value + " is not <= " + max);
        }
    }

    private boolean isMax(Object value) {
        if (value instanceof String) {
            try {
                double dv = Double.parseDouble((String) value);
                return dv <= max;
            }
            catch (NumberFormatException nfe) {
                return false;
            }
        }
        else if ((value instanceof Double) || (value instanceof Float)) {
            double dv = ((Number) value).doubleValue();
            return dv <= max;
        }
        else if (value instanceof Number) {
            long lv = ((Number) value).longValue();
            return lv <= max;
        }
        else {
            return false;
        }
    }

}
