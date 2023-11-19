/*
 * DoubleMaxValueValidator.java
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
public class DoubleMaxValueValidator implements Validator<Double> {
    private double maxValue = 0.0d;

    @Override
    public void validate(Double value) throws ValidatorException {
        if(value <= maxValue) {
            return;
        }
        throw new ValidatorException(I18n.I.orderEntryValidateDoubleMaxValue(Double.toString(this.maxValue)));
    }

    public void setMaxValue(double maxValue) {
        this.maxValue = maxValue;
    }
}
