/*
 * EmailValidator.java
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
public class EmailValidator extends AbstractValidator<Email> {

    private java.util.regex.Pattern pattern;

    private static final String ERROR_CODE = "validator.email";

    public void validateValue(Object value, Errors errors) {
        if ( value == null ) return;
        if ( !( value instanceof String ) ) {
            errors.rejectValue(
                    getPropertyName(),
                    ERROR_CODE, getPropertyName() + " not a String");
            return;
        }
        String string = (String) value;
        Matcher m = pattern.matcher( string );
        if (!m.matches()) {
            errors.rejectValue(
                    getPropertyName(),
                    ERROR_CODE, getPropertyName() + " invalid: " + value);
        }
    }

    public void doInitialize(Email parameters) {
        this.pattern = java.util.regex.Pattern.compile(
                "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*$",
                java.util.regex.Pattern.CASE_INSENSITIVE
        );
    }
}
