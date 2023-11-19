/*
 * UserListListCommand.java
 *
 * Created on 29.01.2007 14:29:34
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.block;

import org.springframework.util.StringUtils;

import de.marketmaker.istar.merger.web.easytrade.ListCommandForUser;

/**
 * A combined ListCommand/UserCommand that refers to a user's watchlist or portfolio
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class UserListListCommand extends ListCommandForUser {
    /** id of watchlist or portfolio, may be null*/
    private Long listid;

    /** if listid is null: use the user's default watchlist (true) or portfolio (false) */
    private boolean defaultWatchlist = false;

    public Long getListid() {
        return listid;
    }

    public void setListid(Long listid) {
        this.listid = listid;
    }

    public void setDefaultWatchlist(boolean defaultWatchlist) {
        this.defaultWatchlist = defaultWatchlist;
    }

    public void setDefaultWatchlist(String s) {
        if (StringUtils.hasText(s)) {
            this.defaultWatchlist = Boolean.parseBoolean(s);
        }
    }

    public boolean isDefaultWatchlist() {
        return defaultWatchlist;
    }
}
