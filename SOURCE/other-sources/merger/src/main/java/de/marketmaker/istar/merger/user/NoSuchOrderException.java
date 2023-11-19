/*
 * NoSuchOrderException.java
 *
 * Created on 03.08.2006 16:28:27
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.user;

import de.marketmaker.istar.merger.MergerException;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class NoSuchOrderException extends MergerException {
    public NoSuchOrderException(String message, long orderid) {
        super(message, orderid);
    }

    public String getCode() {
        return "user.orderid.invalid";
    }
}
