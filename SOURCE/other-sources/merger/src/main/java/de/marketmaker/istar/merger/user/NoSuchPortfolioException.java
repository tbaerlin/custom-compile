/*
 * NoSuchPortfolioException.java
 *
 * Created on 03.08.2006 15:44:45
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.user;

import de.marketmaker.istar.merger.MergerException;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class NoSuchPortfolioException extends MergerException {
    public static final String USER_PORTFOLIOID_INVALID = "user.portfolioid.invalid";

    public NoSuchPortfolioException(long portfolioid) {
        super("no portfolio for id " + portfolioid);
    }

    public String getCode() {
        return USER_PORTFOLIOID_INVALID;
    }
}
