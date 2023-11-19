/*
 * FILENAME
 *
 * Created on 16.08.13 08:48
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.input.validators;

import de.marketmaker.iview.mmgwt.mmweb.client.I18n;

/**
 * @author Markus Dick
 */
public class NotNullValidator<T> implements Validator<T> {
    @Override
    public void validate(T value) throws ValidatorException {
        if(value == null) throw new ValidatorException(I18n.I.orderEntryValidateNotEmpty());
    }
}
