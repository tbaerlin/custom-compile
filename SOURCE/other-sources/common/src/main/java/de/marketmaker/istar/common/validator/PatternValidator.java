/*
 * PatternValidator.java
 *
 * Created on 31.07.2006 17:17:09
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.common.validator;

import java.util.regex.Matcher;

import org.springframework.validation.Errors;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class PatternValidator extends AbstractValidator<Pattern> {

    private java.util.regex.Pattern pattern;

    private static final String ERROR_CODE = "validator.pattern";

    protected void doInitialize(Pattern parameters) {
        pattern = java.util.regex.Pattern.compile(
                parameters.regex(),
                parameters.flags()
        );
    }

    public void validateValue(Object value, Errors errors) {
        if ( value == null ) {
            return;
        }
        if (value.getClass().isArray()) {
            Object[] values = (Object[]) value;
            for (Object o : values) {
                validateValue(o, errors);
            }
            return;
        }
        if ( !( value instanceof String ) ) {
            errors.rejectValue(
                    getPropertyName(),
                    ERROR_CODE, new Object[] { pattern.pattern(), "n/a" },
                    getPropertyName() + ": " + value + " is not a String");
            return;
        }
        String string = (String) value;
        Matcher m = pattern.matcher( string );
        if (!m.matches()) {
            errors.rejectValue(
                    getPropertyName(),
                    ERROR_CODE, new Object[] { pattern.pattern(), value },
                    getPropertyName() + ": " + value
                            + " does not match " + pattern.pattern());
        }
    }

}
