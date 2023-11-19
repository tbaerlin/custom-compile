/*
 * UserProvider.java
 *
 * Created on 27.07.2006 11:34:51
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider;

import de.marketmaker.istar.merger.user.AddLimitCommand;
import de.marketmaker.istar.merger.user.AddOrderCommand;
import de.marketmaker.istar.merger.user.AddPortfolioCommand;
import de.marketmaker.istar.merger.user.AddPositionCommand;
import de.marketmaker.istar.merger.user.AlternativeIid;
import de.marketmaker.istar.merger.user.RemoveLimitCommand;
import de.marketmaker.istar.merger.user.RemoveOrderCommand;
import de.marketmaker.istar.merger.user.RemovePortfolioCommand;
import de.marketmaker.istar.merger.user.RemovePositionCommand;
import de.marketmaker.istar.merger.user.UpdateLimitCommand;
import de.marketmaker.istar.merger.user.UpdateLoginCommand;
import de.marketmaker.istar.merger.user.UpdateOrderCommand;
import de.marketmaker.istar.merger.user.UpdatePortfolioCommand;
import de.marketmaker.istar.merger.user.UpdatePositionCommand;
import de.marketmaker.istar.merger.user.User;
import de.marketmaker.istar.merger.user.UserContext;
import de.marketmaker.istar.merger.web.easytrade.block.EasytradeInstrumentProvider;

import java.util.List;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public interface UserProvider {
    /**
     * Retrieve user context based on user's id. This is the preferred access method.
     *
     * @param id
     * @return UserContext for the given user.
     */
    UserContext getUserContext(long id);

    /**
     * Get user context based on login and companyid; based on the company property
     * 'is.user.autocreate', the user is created. This method will most likely be slower
     * than {@link #getUserContext(long)}, so this method should only be used if the user's id
     * is not available.
     *
     * @param login
     * @param companyid
     * @return UserContext for the given user.
     */
    UserContext getUserContext(String login, long companyid);

    /**
     * Retrieve user context based on login and companyid. This method will most likely be slower
     * than {@link #getUserContext(long)}, so this method should only be used if the user's id
     * is not available.
     *
     * @param login
     * @param companyid
     * @return UserContext for the given user.
     */
    UserContext retrieveUserContext(String login, long companyid);

    void updateLogin(UpdateLoginCommand command);

    void updatePosition(UpdatePositionCommand command);

    void insertPosition(AddPositionCommand command);

    /**
     * Adds a new watchlist for the user specified in the command and returns the
     * watchlist's id
     *
     * @param awc
     * @return watchlist id
     */
    Long addPortfolio(AddPortfolioCommand awc);

    /**
     * Updates information for a certain portfolio
     *
     * @param upc
     */
    void updatePortfolio(UpdatePortfolioCommand upc);

    /**
     * Removes the watchlist specified in the command
     *
     * @param rwc
     */
    void removePortfolio(RemovePortfolioCommand rwc);

    void removePosition(RemovePositionCommand rpc);

    void removeOrder(RemoveOrderCommand roc);

    void updateOrder(UpdateOrderCommand uoc);

    long addOrder(AddOrderCommand aoc);

    List<AddOrderCommand> distributeOrderWithinInstrument(AddOrderCommand command,
            EasytradeInstrumentProvider instrumentProvider);

    void removeUser(long id);

    List<AlternativeIid> getAlternativeIids(long iid, User user);

    /**
     * Retrieve all user ids of a specific company. Used for exports.
     *
     * @param companyId the company to select the users for
     * @return list of user ids
     */
    List<Long> getUserIds(long companyId);
}
