/*
 * RGBColorValidator.java
 *
 * Created on 27.03.2009 11:21:33
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.common.validator;

import org.springframework.validation.Errors;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class RGBColorValidator extends AbstractValidator<RGBColor> {
    private static final String ERROR_CODE = "invalid.color";

    public void validateValue(Object value, Errors errors) {
        if ( value == null ) {
            return;
        }
        if (value instanceof String) {
            validate(new String[] {(String) value}, errors);
        }
        if (value instanceof String[]) {
            validate((String[]) value, errors);
        }
    }

    public void validate(String[] values, Errors errors) {
        for (int i = 0; i < values.length; i++) {
            final String value = values[i];
            try {
                final int color = Integer.parseInt(value, 16);
                if (color < 0 || color > 0xFFFFFF) {
                    throw new Exception();
                }
            } catch (Exception e) {
                errors.rejectValue(getPropertyName(), ERROR_CODE, "'" + value + "'");
                return;
            }
        }
    }
}
