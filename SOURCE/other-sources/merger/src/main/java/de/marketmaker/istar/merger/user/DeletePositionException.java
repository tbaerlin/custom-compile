/*
 * DeletePositionException.java
 *
 * Created on 09.08.2006 10:39:47
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.user;

import de.marketmaker.istar.merger.MergerException;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class DeletePositionException extends MergerException {
    public DeletePositionException(String message, long position) {
        super(message, position);
    }

    public String getCode() {
        return "user.position.delete.denied";
    }
}
