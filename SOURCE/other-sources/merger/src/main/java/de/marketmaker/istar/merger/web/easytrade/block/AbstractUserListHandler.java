/*
 * AbstractUserListHandler.java
 *
 * Created on 29.01.2007 14:35:15
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.block;

import java.util.List;

import de.marketmaker.istar.merger.user.NoSuchPortfolioException;
import de.marketmaker.istar.merger.user.Portfolio;
import de.marketmaker.istar.merger.user.User;
import de.marketmaker.istar.merger.user.UserContext;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public abstract class AbstractUserListHandler extends UserHandler {

    protected AbstractUserListHandler(Class command) {
        super(command);
    }

    protected Portfolio getPortfolio(UserListListCommand cmd) {
        final UserContext uc = getUserContext(cmd);
        final User user = uc.getUser();
        return getPortfolio(cmd, user);
    }

    protected Portfolio getPortfolio(UserListListCommand cmd, User user) {
        if (cmd.getListid() != null) {
            final Portfolio result = user.getPortfolioOrWatchlist(cmd.getListid());
            if (result == null) {
                throw new NoSuchPortfolioException(cmd.getListid());
            }
            return result;
        }
        final List<Portfolio> tmp
                = cmd.isDefaultWatchlist() ? user.getWatchlists() : user.getPortfolios();
        return tmp.isEmpty() ? null : tmp.get(0);
    }
}
