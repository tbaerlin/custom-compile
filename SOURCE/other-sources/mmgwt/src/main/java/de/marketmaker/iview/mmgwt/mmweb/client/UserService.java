/*
 * UserService.java
 *
 * Created on 29.04.2008 16:25:03
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client;

import java.util.Map;

import com.google.gwt.user.client.rpc.RemoteService;

import de.marketmaker.iview.mmgwt.mmweb.client.data.AppConfig;
import de.marketmaker.iview.mmgwt.mmweb.client.data.MessageOfTheDay;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public interface UserService extends RemoteService {
    UserResponse login(UserRequest userRequest);

    ChangePasswordResponse changePassword(String uid, String pwOld, String pwNew, String module);

    void storeUserConfig(String userId, AppConfig config);

    void logout();

    /**
     * To be invoked when the user forgot his password; the system will tries to find out the user's
     * email address and send him an email that contains a link that can be used to reset the
     * password.
     * @param login user's login
     * @param module user's module (only logins within a module are unique)
     * @param locale .
     * @return email address to which password change link has been sent
     */
    String requestPasswordReset(String login, String module, String locale);

    /**
     * Returns a public profile, e.g. for zone kwtpub where no login is used.
     * @param userRequest A user request, which requires usually no password.
     * @return A user response, possibly with an empty user except for the login.
     */
    UserResponse getPublicProfile(UserRequest userRequest);

    MessageOfTheDay getMessageOfTheDay();

    String getMessageOfTheDayByDate();

    void setMessageOfTheDay(MessageOfTheDay motd);

    Map<String, String> getEnvInfo();
}
