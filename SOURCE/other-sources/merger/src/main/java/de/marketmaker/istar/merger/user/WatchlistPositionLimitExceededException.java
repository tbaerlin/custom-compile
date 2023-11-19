/*
 * WatchlistPositionLimitExceededException.java
 *
 * Created on 07.08.2006 11:28:20
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.user;

import de.marketmaker.istar.merger.MergerException;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class WatchlistPositionLimitExceededException extends MergerException {
    public WatchlistPositionLimitExceededException(String message, long maxNumAllowed) {
        super(message, maxNumAllowed);
    }

    public String getCode() {
        return "user.watchlist.positions.limit.exceeded";
    }
}
