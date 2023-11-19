/*
 * UserServiceAsync.java
 *
 * Created on 29.04.2008 16:26:36
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;

import de.marketmaker.iview.mmgwt.mmweb.client.data.AppConfig;
import de.marketmaker.iview.mmgwt.mmweb.client.data.MessageOfTheDay;
import de.marketmaker.iview.mmgwt.mmweb.client.data.User;
import de.marketmaker.iview.mmgwt.mmweb.client.util.JsUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.PhoneGapUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.UrlBuilder;

import java.util.Map;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public interface UserServiceAsync {
    void login(UserRequest userRequest, AsyncCallback<UserResponse> async);

    void changePassword(String uid, String pwOld, String pwNew, String module,
                        AsyncCallback<ChangePasswordResponse> async);

    void storeUserConfig(String userId, AppConfig config, AsyncCallback<Void> async);

    void logout(AsyncCallback<Void> async);

    void requestPasswordReset(String login, String module, String locale, AsyncCallback<String> async);

    void getMessageOfTheDay(AsyncCallback<MessageOfTheDay> async);

    void setMessageOfTheDay(MessageOfTheDay motd, AsyncCallback<Void> async);

    void getMessageOfTheDayByDate(AsyncCallback<String> async);

    void getPublicProfile(UserRequest userRequest, AsyncCallback<UserResponse> async);

    void getEnvInfo(AsyncCallback<Map<String, String>> callback);

    /**
     * Utility/Convenience class.
     * Use UserService.App.getInstance () to access static instance of UserAsync
     */
    public static class App {
        private static UserServiceAsync app = null;

        public static UserServiceAsync getInstance() {
            if (app == null) {
                final String entryPoint;
                if (PhoneGapUtil.isPhoneGap()) {
                    final String serverPrefix = UrlBuilder.getServerPrefix();
                    if (serverPrefix != null) {
                        String userRpcUrl = serverPrefix + "/pmxml-1/vwd/user.rpc"; // $NON-NLS$
                        entryPoint = userRpcUrl.replace("$moduleName", UrlBuilder.MODULE_NAME); // $NON-NLS$
                    } else {
                        entryPoint = MainController.INSTANCE.contextPath + "/" + UrlBuilder.MODULE_NAME + "/user.rpc"; // $NON-NLS$
                    }
                } else {
                    final String userRpcUrl = JsUtil.getServerSetting("userRpcUrl"); // $NON-NLS$
                    if (userRpcUrl != null) {
                        // diese Variante wird in idoc verwendet
                        entryPoint = userRpcUrl.replace("$moduleName", UrlBuilder.MODULE_NAME); // $NON-NLS$
                    }
                    else {
                        entryPoint = MainController.INSTANCE.contextPath + "/" + UrlBuilder.MODULE_NAME + "/user.rpc"; // $NON-NLS$
                    }
                }
                app = (UserServiceAsync) GWT.create(UserService.class);
                ((ServiceDefTarget) app).setServiceEntryPoint(entryPoint);
            }
            return app;
        }
    }


}
