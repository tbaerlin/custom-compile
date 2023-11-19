/*
 * IllegalFieldException.java
 *
 * Created on 26.01.2006 18:05:25
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class IllegalFieldException extends RuntimeException {

    public IllegalFieldException(int fieldId) {
        super("Illegal field: " + fieldId);
    }
}
