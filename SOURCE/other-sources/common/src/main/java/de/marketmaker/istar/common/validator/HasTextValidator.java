/*
 * HasTextValidator.java
 *
 * Created on 28.11.2006 13:37:49
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.common.validator;

import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class HasTextValidator extends AbstractValidator<HasText> {

    public void validateValue(Object value, Errors errors) {
        if (isUndefined(value)) {
            errors.rejectValue(getPropertyName(),
                    "validator.hasText", getPropertyName() + ": is undefined");
        }
    }

    private boolean isUndefined(Object value) {
        return value == null
                || (value instanceof String && !StringUtils.hasText((String) value));
    }

}
