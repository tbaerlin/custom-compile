/*
 * TextWithKeyKeyNotNullOrEmptyValidator.java
 *
 * Created on 04.11.13 15:27
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.input.validators;

import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;
import de.marketmaker.iview.pmxml.TextWithKey;

/**
 * @author Markus Dick
 */
public class TextWithKeyKeyNotNullOrEmptyValidator implements Validator<TextWithKey> {
    @Override
    public void validate(TextWithKey value) throws ValidatorException {
        if(value == null || !StringUtil.hasText(value.getKey())) {
            throw new ValidatorException(I18n.I.orderEntryValidateNotEmpty());
        }
    }
}
