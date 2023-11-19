/*
 * TimeStringValidator.java
 *
 * Created on 12.12.13 11:10
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.input.validators;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.regexp.shared.RegExp;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.input.TimeFormat;
import de.marketmaker.iview.mmgwt.mmweb.client.util.Formatter;

/**
 * @author Markus Dick
 */
public class TimeStringValidator implements Validator<String> {
    private final String formatPattern;
    private final RegExp pattern;

    public TimeStringValidator(TimeFormat timeFormat) {
        final String regexPattern;
        final DateTimeFormat dateTimeFormat;
        switch(timeFormat) {
            case HHMMSS:
                dateTimeFormat = Formatter.FORMAT_TIME;
                regexPattern = "^([0-1][0-9]|[2][0-3]):([0-5][0-9]):([0-5][0-9])$"; //$NON-NLS$
                break;
            case HHMM:
            default:
                dateTimeFormat = Formatter.FORMAT_TIME_HHMM;
                regexPattern = "^([0-1][0-9]|[2][0-3]):([0-5][0-9])$"; //$NON-NLS$
        }
        this.formatPattern = dateTimeFormat.getPattern().toUpperCase();
        this.pattern = RegExp.compile(regexPattern);
    }

    @Override
    public void validate(String value) throws ValidatorException {
        if(this.pattern.exec(value) == null) {
            throw new ValidatorException(I18n.I.orderEntryValidateTimeString(this.formatPattern));
        }
    }
}
