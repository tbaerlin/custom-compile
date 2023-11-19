/*
 * SizeValidator.java
 *
 * Created on 31.07.2006 17:17:09
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.common.validator;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Map;

import org.springframework.validation.Errors;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class SizeValidator extends AbstractValidator<Size> {
    private int max;
    private int min;

    private static final String ERROR_CODE = "validator.size";

    protected void doInitialize(Size parameters) {
        this.max = parameters.max();
        this.min = parameters.min();
    }

    public void validateValue(Object value, Errors errors) {
        if ( value == null ) {
            return;
        }
        final Integer size = getSize(value);
        if (size == null || size < min || size > max) {
            errors.rejectValue(
                    getPropertyName(),
                    ERROR_CODE, new Object[] { this.min, this.max, size != null ? size : "n/a"},
                    getPropertyName() + ": " + value
                            + " not within [" + this.min + ".." + this.max + "]");
        }
    }

    private Integer getSize(Object value) {
        if ( value.getClass().isArray() ) {
            return Array.getLength( value );
        }
        else if ( value instanceof Collection ) {
            return ( (Collection) value ).size();
        }
        else if ( value instanceof Map ) {
            return ( (Map) value ).size();
        }
        else if ( value instanceof String ) {
            return ( (String) value ).length();
        }
        else {
            return null;
        }
    }

}
