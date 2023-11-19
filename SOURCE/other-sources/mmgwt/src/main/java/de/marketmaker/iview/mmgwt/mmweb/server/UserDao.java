/*
 * UserDao.java
 *
 * Created on 30.06.2008 14:49:05
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.server;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;

import de.marketmaker.iview.mmgwt.mmweb.client.data.MessageOfTheDay;
import org.joda.time.DateTime;

import de.marketmaker.iview.mmgwt.mmweb.client.data.AppConfig;
import de.marketmaker.iview.mmgwt.mmweb.client.data.User;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public interface UserDao {
    /**
     * Returns a user with the given id
     * @param id
     * @return user or null if not available
     */
    User getUser(String id);

    /**
     * Returns all existing users
     * @return existing users or empty list if no one available
     */
    List<User> getUsers();

    /**
     * Persists configuration associated with the given user
     * @param u to be persisted
     */
    void storeUserConfig(String userId, AppConfig config);

    /**
     * Stores a user that is currently not stored
     * @param u to be stored
     */
    void insertUser(User u);

    /**
     * Changes a user's password; to be used by the user itself to change the password, knowledge
     * of the old password is required to be able to prevent changing a password by just getting
     * access to a user's terminal
     * @param uid identifies user
     * @param oldPassword the new password
     * @param newPassword the new password
     * @return true iff password changed
     */
    boolean changePassword(String uid, String oldPassword, String newPassword);

    /**
     * Changes a user's password; to be used by an administrator to reset a user's password. No
     * knowledge of the old password is required.
     * @param uid identifies user
     * @param password the new password
     * @return true iff password changed
     */
    boolean resetPassword(String uid, String password);

    /**
     * Returns the currently stored session id
     * @param uid user id
     * @return the value previously stored as session id
     */
    String getSessionId(String uid);

    /**
     * Sets a new session id
     * @param uid user id
     * @param id session id
     */
    void setSessionId(String uid, String id);

    /**
     * Returns a map of all available client configs, keyed by the respective module name
     * @return all client configs
     */
    Map<String, ClientConfig> getClientConfigs();

    /**
     * Returns the history of AppConfigs for the given user
     * @param uid user id
     * @return AppConfigs keyed by date (excluding the current config)
     */
    NavigableMap<DateTime, AppConfig> getAppConfigHistory(String uid);

    AppConfig getAppConfig(int id);

    /**
     * @return The message of the day (login screen message) for the given zone or null if no message of the day is available.
     */
    MessageOfTheDay getMessageOfTheDay(String zone);

    /**
     * @return The message of the day (login screen message) for the given zone or null if no message of the day is available for the current date.
     */
    String getMessageOfTheDayByDate(String zone);

    /**
     * Inserts or updates the message of the day (login screen message) for the given zone.
     * @param zone The zone.
     * @param motd The message of the day.
     */
    void setMessageOfTheDay(String zone, MessageOfTheDay motd);

}
