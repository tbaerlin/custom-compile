/*
 * DateNotBeforeTodayValidator.java
 *
 * Created on 16.01.13 12:04
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.input.validators;

import de.marketmaker.itools.gwtutil.client.util.date.MmJsDate;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;

/**
 * @author Markus Dick
 */
public class DateNotBeforeTodayValidator implements Validator<MmJsDate> {
    @Override
    public void validate(MmJsDate then) throws ValidatorException {
        if(then == null) return;

        final MmJsDate now = new MmJsDate().atMidnight();

        if(then.isBefore(now)) {
            throw new ValidatorException(I18n.I.orderEntryValidateDateNotBeforeToday());
        }
    }
}
