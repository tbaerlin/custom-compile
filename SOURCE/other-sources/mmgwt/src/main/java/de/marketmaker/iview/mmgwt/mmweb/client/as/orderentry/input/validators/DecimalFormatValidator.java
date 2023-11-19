/*
 * DecimalFormatValidator.java
 *
 * Created on 16.01.13 12:09
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.input.validators;

import com.google.gwt.i18n.client.NumberFormat;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;

/**
 * @author Markus Dick
 */
public class DecimalFormatValidator implements Validator<String> {
    private Validator<Double> next;

    @Override
    public void validate(String value) throws ValidatorException {
        if(StringUtil.hasText(value)) {
            try {
                double d = NumberFormat.getDecimalFormat().parse(value.trim());
                if(this.next != null) {
//                    DebugUtil.logToFirebugConsole("<DecimalFormatValidator.validate> next != null, double="+d);
                    next.validate(d);
                }
            }
            catch(NumberFormatException e) {
                throw new ValidatorException(I18n.I.orderEntryValidateNumber());
            }
        }
    }

    public DecimalFormatValidator with(Validator<Double> next) {
        this.next = next;
        return this;
    }
}
