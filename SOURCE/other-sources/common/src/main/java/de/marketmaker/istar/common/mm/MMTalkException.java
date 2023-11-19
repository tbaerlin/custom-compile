/*
 * MMTalkException.java
 *
 * Created on 17.03.2005 08:09:17
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.common.mm;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class MMTalkException extends Exception {
    public MMTalkException(String message) {
        super(message);
    }

    public MMTalkException(String message, Throwable cause) {
        super(message, cause);
    }
}
