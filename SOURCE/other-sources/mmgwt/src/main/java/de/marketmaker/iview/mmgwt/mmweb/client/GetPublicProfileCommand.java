/*
 * GetPublicProfileCommand.java
 *
 * Created on 16.11.2012 09:06:42
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client;

import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.util.JsUtil;

/**
 * @author Markus Dick
 */
public class GetPublicProfileCommand extends LoginCommand {
    final String login;

    public GetPublicProfileCommand(String login) {
        super(login, null);
        this.login = login;
    }

    public void execute() {
        Firebug.log("<PublicGetProfileCommand> execute");
        final UserRequest userRequest = createUserRequest(this.login, null, GuiDefsLoader.getModuleName(), JsUtil.getScreenInfo(), I18n.I.locale());
        Firebug.log("<PublicGetProfileCommand> sending getPublicRequest: " + userRequest.getLogin());
        UserServiceAsync.App.getInstance().getPublicProfile(userRequest, this);
    }

    public void onSuccess(UserResponse response) {
        Firebug.log("<PublicGetProfileCommand> onSuccess: " + response.getUser().toString());

        switch (response.getState()) {
            case OK:
                SessionData.INSTANCE.setUser(response.getUser());
                AbstractMainController.INSTANCE.runInitSequence();
                break;
            default:
                showError(I18n.I.internalError());
                break;
        }
    }

    @Override
    public void onFailure(Throwable throwable) {
        Firebug.error("<PublicGetProfileCommand> onFailure", throwable);
        super.onFailure(throwable);
    }
}
