/*
 * DoubleNotZeroValidator.java
 *
 * Created on 16.01.13 12:52
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.input.validators;

import de.marketmaker.iview.mmgwt.mmweb.client.I18n;

/**
 * @author Markus Dick
 */
public class DoubleNotZeroValidator implements Validator<Double> {
    @Override
    public void validate(Double value) throws ValidatorException {
        if(value != 0.0d) {
            return;
        }
        throw new ValidatorException(I18n.I.orderEntryValidateDoubleNotZero());
    }
}
