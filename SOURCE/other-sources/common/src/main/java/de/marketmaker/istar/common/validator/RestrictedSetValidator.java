/*
 * NotNullValidator.java
 *
 * Created on 31.07.2006 17:17:09
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.common.validator;

import java.util.HashSet;
import java.util.Set;

import org.springframework.validation.Errors;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class RestrictedSetValidator extends AbstractValidator<RestrictedSet> {

    private final Set<String> allowedValues = new HashSet<>();

    protected void doInitialize(RestrictedSet parameters) {
        final String[] items = parameters.value().split(",");
        for (final String item : items) {
            this.allowedValues.add(item.trim());
        }
    }

    public void validateValue(Object value, Errors errors) {
        if (value == null) {
            return;
        }
        if ( value.getClass().isArray() ) {
            final Object[] values = (Object[]) value;
            for (Object o : values) {
                checkValue(o, errors);
            }            
        }
        else {
            checkValue(value, errors);
        }
    }

    private void checkValue(Object value, Errors errors) {
        final String toCheck = value instanceof Enum
                ? ((Enum) value).name()
                : value.toString();

        if (!this.allowedValues.contains(toCheck)) {
            errors.rejectValue(
                    getPropertyName(),
                    "validator.restrictedSet", new Object[]{this.allowedValues, value},
                    getPropertyName() + ": " + value + " is not in " + this.allowedValues);
        }
    }
}
