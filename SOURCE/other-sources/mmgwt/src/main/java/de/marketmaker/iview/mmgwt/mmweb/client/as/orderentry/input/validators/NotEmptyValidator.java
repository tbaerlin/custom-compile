/*
 * NotEmptyValidator.java
 *
 * Created on 16.01.13 12:07
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.input.validators;

import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;

/**
 * @author Markus Dick
 */
public class NotEmptyValidator implements Validator<String>{
    @Override
    public void validate(String value) throws ValidatorException {
        if(!StringUtil.hasText(value)) {
            throw new ValidatorException(I18n.I.orderEntryValidateNotEmpty());
        }
    }
}
