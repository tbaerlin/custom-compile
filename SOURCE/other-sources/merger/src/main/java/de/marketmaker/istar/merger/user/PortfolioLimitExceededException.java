/*
 * PortfolioLimitExceededException.java
 *
 * Created on 07.08.2006 11:29:17
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.user;

import de.marketmaker.istar.merger.MergerException;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class PortfolioLimitExceededException extends MergerException {
    public PortfolioLimitExceededException(String message, long maxNumAllowed) {
        super(message, maxNumAllowed);
    }

    public String getCode() {
        return "user.portfolio.limit.exceeded";
    }
}
