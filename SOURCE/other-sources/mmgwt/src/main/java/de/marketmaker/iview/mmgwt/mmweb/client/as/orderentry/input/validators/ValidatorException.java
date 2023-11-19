/*
 * ValidatorException.java
 *
 * Created on 20.12.12 13:40
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.input.validators;

/**
 * @author Markus Dick
 */
public class ValidatorException extends RuntimeException {
    public ValidatorException(String message) {
        super(message);
    }
}
