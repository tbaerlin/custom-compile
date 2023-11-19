/*
 * AssertTrueValidator.java
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
public class AssertTrueValidator extends AbstractValidator<AssertTrue> {

    public void validateValue(Object value, Errors errors) {
        if (!(Boolean) value) {
            errors.rejectValue(
                    getPropertyName(), "validator.assertTrue",
                    getPropertyName() + " is not true");
        }
    }

}
