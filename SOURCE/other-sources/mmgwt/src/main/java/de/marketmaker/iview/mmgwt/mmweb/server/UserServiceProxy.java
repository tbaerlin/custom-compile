/*
 * MmwebServiceImpl.java
 *
 * Created on 27.02.2008 09:53:57
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.server;

import de.marketmaker.iview.mmgwt.mmweb.client.ChangePasswordResponse;
import de.marketmaker.iview.mmgwt.mmweb.client.UserRequest;
import de.marketmaker.iview.mmgwt.mmweb.client.UserResponse;
import de.marketmaker.iview.mmgwt.mmweb.client.UserService;
import de.marketmaker.iview.mmgwt.mmweb.client.data.AppConfig;
import de.marketmaker.iview.mmgwt.mmweb.client.data.MessageOfTheDay;

import org.springframework.context.ApplicationContext;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import java.util.Map;

/**
 * To be used in hosted mode as a connector to a service that provides an UserService.
 * Connects to a local activemq instance on the default port to get access
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class UserServiceProxy extends AbstractServiceProxy implements UserService {

    private UserService delegate;


    public void init(ServletConfig servletConfig) throws ServletException {
        super.init(servletConfig);
        final ApplicationContext ac = getContext();
        this.delegate = (UserService) ac.getBean("gwtuserService", UserService.class);
    }

    public UserResponse login(UserRequest userRequest) {
        return delegate.login(userRequest);
    }

    public ChangePasswordResponse changePassword(String uid, String pwOld, String pwNew, String module) {
        return delegate.changePassword(uid, pwOld, pwNew, module);
    }

    public void storeUserConfig(String userId, AppConfig config) {
        this.delegate.storeUserConfig(userId, config);
    }

    public void logout() {
        this.delegate.logout();
    }

    public String requestPasswordReset(String login, String module, String locale) {
        return this.delegate.requestPasswordReset(login, module, locale);
    }

    @Override
    public UserResponse getPublicProfile(UserRequest userRequest) {
        throw new UnsupportedOperationException();
    }

    public MessageOfTheDay getMessageOfTheDay() {
        return this.delegate.getMessageOfTheDay();
    }

    public String getMessageOfTheDayByDate() {
        return this.delegate.getMessageOfTheDayByDate();
    }

    public void setMessageOfTheDay(MessageOfTheDay motd) {
        this.delegate.setMessageOfTheDay(motd);
    }

    public Map<String, String> getEnvInfo() {
        return this.delegate.getEnvInfo();
    }
}
