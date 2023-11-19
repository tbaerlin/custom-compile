/*
 * NoSuchLimitException.java
 *
 * Created on 03.08.2006 16:28:06
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.user;

import de.marketmaker.istar.merger.MergerException;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class NoSuchLimitException extends MergerException {
    public NoSuchLimitException(String message, long limitid) {
        super(message, limitid);
    }

    public String getCode() {
        return "user.limitid.invalid";
    }
}
