/*
 * UserDataLoader.java
 *
 * Created on 03.02.2009 14:29:42
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.rpc.AsyncCallback;

import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.util.JsUtil;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class LoginCommand implements Command, AsyncCallback<UserResponse> {

    private final String username;

    private final String password;

    public LoginCommand(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public void execute() {
        AbstractMainController.INSTANCE.updateProgress(I18n.I.login());
        final UserRequest userRequest = createUserRequest(this.username, this.password, GuiDefsLoader.getModuleName(), JsUtil.getScreenInfo(), I18n.I.locale());
        UserServiceAsync.App.getInstance().login(userRequest, this);
    }

    public static UserRequest createUserRequest(final String username, final String password, final String moduleName, final String screenInfo, final String locale) {
        final String mmModuleName = JsUtil.getMetaValue("mmModuleName"); // $NON-NLS$
        final boolean pmZone = "true".equals(JsUtil.getMetaValue("pmZone")); // $NON-NLS$
        return new UserRequest(pmZone, username, password, mmModuleName == null ? moduleName : mmModuleName, screenInfo, locale);
    }

    public void onFailure(Throwable throwable) {
        Firebug.error("login failed", throwable); // $NON-NLS-0$
        showError(I18n.I.internalError()); 
    }

    public void onSuccess(UserResponse response) {
        switch (response.getState()) {
            case OK:
                SessionData.INSTANCE.setUser(response.getUser());
                AbstractMainController.INSTANCE.runInitSequence();
                break;
            case WRONG_PASSWORD:
                showError(I18n.I.invalidPassword()); 
                break;
            case WRONG_INITIAL_PASSWORD:
                showError(I18n.I.invalidInitialPassword()); 
                break;
            case UNKNOWN_USER:
                showError(I18n.I.unknownUser()); 
                break;
            case INACTIVE_USER:
                showError(I18n.I.userAccountNotActive()); 
                break;
            case INVALID_PRODUCT:
                showError(I18n.I.productNotAllowed()); 
                break;
            case INVALID_VWDID:
                showError(I18n.I.vwdIdNotAllowed());
                break;
            default:
                showError(I18n.I.internalError()); 
                break;
        }
    }

    protected void showError(String s) {
        AbstractMainController.INSTANCE.onInitFailed(s);
    }
}
