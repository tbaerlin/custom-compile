/*
 * WatchlistLimitExceededException.java
 *
 * Created on 07.08.2006 11:29:06
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.user;

import de.marketmaker.istar.merger.MergerException;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class WatchlistLimitExceededException extends MergerException {
    public WatchlistLimitExceededException(String message, long maxNumAllowed) {
        super(message, maxNumAllowed);
    }

    public String getCode() {
        return "user.watchlist.limit.exceeded";
    }
}
