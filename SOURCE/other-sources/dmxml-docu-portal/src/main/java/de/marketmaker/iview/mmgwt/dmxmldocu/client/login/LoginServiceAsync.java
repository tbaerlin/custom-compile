package de.marketmaker.iview.mmgwt.dmxmldocu.client.login;

import com.google.gwt.user.client.rpc.AsyncCallback;
import de.marketmaker.iview.mmgwt.dmxmldocu.client.data.User;

public interface LoginServiceAsync {
    void getUserFromSession(AsyncCallback<User> async);

    void login(String username, String password, boolean storeSession, AsyncCallback<User> async);

    void logout(AsyncCallback<Void> async);
}
