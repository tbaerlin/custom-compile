/*
 * Validator.java
 *
 * Created on 16.01.13 11:29
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.input.validators;

import de.marketmaker.itools.gwtutil.client.util.date.MmJsDate;
import de.marketmaker.iview.pmxml.TextWithKey;

/**
 * @author Markus Dick
 */
public interface Validator<T> {
    public abstract void validate(final T value) throws ValidatorException;

    public final static Validator<MmJsDate> DATE_NOT_BEFORE_TODAY = new DateNotBeforeTodayValidator();
    public final static Validator<String> NOT_EMPTY = new NotEmptyValidator();
    public final static Validator<String> DECIMAL_FORMAT = new DecimalFormatValidator();
    public final static Validator<Double> DOUBLE_NOT_ZERO = new DoubleNotZeroValidator();
    public final static Validator NOT_NULL = new NotNullValidator(); //used for e.g. mapped list boxes
    public final static Validator<TextWithKey> TEXT_WITH_KEY_KEY_NOT_NULL_OR_EMPTY_VALIDATOR = new TextWithKeyKeyNotNullOrEmptyValidator();
}


