/*
 * UnknownSymbolException.java
 *
 * Created on 23.08.2006 17:41:11
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider;

import de.marketmaker.istar.merger.MergerException;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class InternalFailure extends MergerException {
    public InternalFailure(String message, Throwable cause) {
        super(message, cause);
    }

    public InternalFailure(String message) {
        super(message);
    }

    public String getCode() {
        return "internal.failure";
    }
}