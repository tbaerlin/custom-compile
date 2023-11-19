/*
 * MergerException.java
 *
 * Created on 27.07.2006 15:36:12
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public abstract class MergerException extends RuntimeException {
    private final Object[] arguments;

    protected MergerException(String message, Throwable cause) {
        super(message, cause);
        this.arguments = new Object[0];
    }

    protected MergerException(String message, Throwable cause, Object... arguments) {
        super(message, cause);
        this.arguments = arguments;
    }

    protected MergerException(String message) {
        super(message);
        this.arguments = new Object[0];
    }

    protected MergerException(String message, Object... arguments) {
        super(message);
        this.arguments = arguments;
    }

    public abstract String getCode();
}
