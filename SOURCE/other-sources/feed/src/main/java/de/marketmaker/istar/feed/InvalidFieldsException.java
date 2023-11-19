/*
 * InvalidFieldsException.java
 *
 * Created on 13.01.14 08:54
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed;

/**
 * @author oflege
 */
public class InvalidFieldsException extends Exception {

    public InvalidFieldsException(String message, Throwable cause) {
        super(message, cause);
    }
}
