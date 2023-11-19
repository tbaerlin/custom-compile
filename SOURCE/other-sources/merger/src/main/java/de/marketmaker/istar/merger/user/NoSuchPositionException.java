/*
 * NoSuchPositionException.java
 *
 * Created on 03.08.2006 15:59:55
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.user;

import de.marketmaker.istar.merger.MergerException;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class NoSuchPositionException extends MergerException {
    public NoSuchPositionException(String message, long position) {
        super(message, position);
    }

    public String getCode() {
        return "user.positionid.invalid";
    }
}
