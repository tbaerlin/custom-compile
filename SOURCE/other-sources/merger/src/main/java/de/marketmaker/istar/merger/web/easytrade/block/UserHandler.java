/*
 * UserHandler.java
 *
 * Created on 31.07.2006 12:07:53
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.block;

import de.marketmaker.istar.merger.provider.CachingUserProvider;
import de.marketmaker.istar.merger.provider.UserProvider;
import de.marketmaker.istar.merger.user.AddPortfolioCommand;
import de.marketmaker.istar.merger.user.NoSuchUserException;
import de.marketmaker.istar.merger.user.Portfolio;
import de.marketmaker.istar.merger.user.User;
import de.marketmaker.istar.merger.user.UserContext;

import javax.servlet.http.HttpServletRequest;

/**
 * Base class for all classes that deal with users.
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public abstract class UserHandler extends EasytradeCommandController {

    private CachingUserProvider cachingUserProvider;

    protected UserHandler(Class command) {
        super(command);
    }

    /**
     * If the command is a UserCommand and does not have the userid set, we try to retrieve the
     * userid from the session. One use case is to allow to retrieve watchlists by just having to
     * know the sessionid; this is what flex chart does.
     *
     * @param httpServletRequest used for binding
     * @param o                  command object
     * @throws Exception if superclass throws it
     */
    @Override
    protected void onBind(HttpServletRequest httpServletRequest, Object o) throws Exception {
        super.onBind(httpServletRequest, o);
        if (o instanceof UserCommand) {
            new UserHandlerMethod(httpServletRequest, (UserCommand) o).invoke();
        }
    }

    protected UserProvider getUserProvider() {
        return cachingUserProvider.getUserProvider();
    }

    protected UserContext getUserContext(UserCommand cmd) {
        if (cmd == null) {
            throw new NoSuchUserException("failed to get user", -1);
        }
        return this.cachingUserProvider.getUserContext(cmd);
    }

    protected Long getLocalUserId(UserCommand cmd) {
        return this.cachingUserProvider.getLocalUserId(cmd);
    }

    public void setCachingUserProvider(CachingUserProvider cachingUserProvider) {
        this.cachingUserProvider = cachingUserProvider;
    }

    protected Long createWatchlist(Long userId, final String name) {
        final AddPortfolioCommand awc = new AddPortfolioCommand();
        awc.setUserid(userId);
        awc.setName(name);
        awc.setWatchlist(true);

        return getUserProvider().addPortfolio(awc);
    }

    /**
     * returns a watchlist id; if a watchlistName is given, this methods ensures a watchlist
     * with this name and returns the corresponding id; otherwise the default id is returned
     */
    protected Long getWatchlistId(User user, String watchlistName, Long defaultWatchlistId) {
        if (watchlistName == null) {
            return defaultWatchlistId;
        }

        for (final Portfolio wl : user.getWatchlists()) {
            if (watchlistName.equals(wl.getName())) {
                return wl.getId();
            }
        }

        return createWatchlist(user.getId(), watchlistName);
    }
}
