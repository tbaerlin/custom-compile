/*
 * UserDao.java
 *
 * Created on 27.07.2006 15:28:56
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.user;

import java.util.List;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public interface UserDao {
    /**
     * Retrieves user
     *
     * @param userId
     * @return null if user does not exist.
     */
    User selectUser(long userId);

    int removeUser(long userId);

    /**
     * Retrieves user
     *
     * @param login
     * @param companyid
     * @return null if user does not exist
     */
    User selectUser(String login, long companyid);

    /**
     * Returns the user's login
     *
     * @param userId
     * @return
     */
    String selectLogin(long userId);

    /**
     * Updates the position
     *
     * @param positionid the id for the old and new portfolio positions
     * @param pp new values for the position
     */
    int updatePosition(Long positionid, PortfolioPosition pp);

    /**
     * Inserts a user into the database
     *
     * @param user
     * @return the new user's id.
     * @throws LoginExistsException if a user with the same login exists for the user's company.
     */
    long insertUser(User user);

    /**
     * Creates a new portfolio for the user
     *
     * @param user
     * @param p
     * @return the portfolio's id
     */
    long insertPortfolio(User user, Portfolio p);

    /**
     * Inserts a new portfolio position
     *
     * @return
     */
    long insertPosition(AddPositionCommand apc, PortfolioPosition pp);


    /**
     * Deletes a position by id and returns the number of rows affected.
     *
     * @return
     */
    int deletePosition(RemovePositionCommand rpc);

    void updatePortfolio(UpdatePortfolioCommand upc);

    /**
     * Deletes a portfolio and returns the number of rows affected.
     *
     * @param rwc
     */
    int deletePortfolio(RemovePortfolioCommand rwc);

    /**
     * Deletes a specific order
     *
     * @param command
     * @param l
     * @return
     */
    int deleteOrder(RemoveOrderCommand command, long l);

    /**
     * Updates a specified order
     *
     * @param command
     */
    void updateOrder(UpdateOrderCommand command);

    long insertOrder(User user, PortfolioPosition position, Order o);

    /**
     * Updates the cash in a portfolio
     */
    void updatePortfolioCash(Portfolio portfolio);

    void updateLogin(long userId, String newLogin);

    /**
     * selects a limited list of iids which are in portfolios that contain the given iid.
     * the list is ordered by count of appearance of the iids.
     */

    List<AlternativeIid> getAlternativeIids(int iid, User user);

    /**
     * Retrieve all user ids of a specific company. Used for exports.
     *
     * @param companyId the company to select the users for
     * @return list of user ids
     */
    List<Long> getUserIds(long companyId);
}
