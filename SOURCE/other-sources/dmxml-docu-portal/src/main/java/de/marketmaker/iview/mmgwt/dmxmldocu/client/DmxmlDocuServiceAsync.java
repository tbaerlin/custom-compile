package de.marketmaker.iview.mmgwt.dmxmldocu.client;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface DmxmlDocuServiceAsync {

    /**
     * Get the repository of all available blocks. Authentication parameters may be null
     * if user is authenticated by a session.
     * auth
     * @return the repository of static documentation
     */
    void getRepository(AsyncCallback<BlocksDocumentation> async);

    /**
     * Send the dm[xml] request contained in {@code request} to dm[xml] and augment the response
     * with documentation.
     * returns response with documentation
     * @param request request to send
     */
    void sendDmxmlRequest(WrappedDmxmlRequest request,
            AsyncCallback<WrappedDmxmlResponse> async);

    void login(LoginData loginData, AsyncCallback<Void> async);

    /**
     * Terminates any existing session
     */
    void logout(AsyncCallback<Void> async);
}
