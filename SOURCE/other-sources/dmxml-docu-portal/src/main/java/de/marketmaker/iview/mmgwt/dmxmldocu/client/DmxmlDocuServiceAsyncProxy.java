/*
 * DmxmlDocuServiceAsyncProxy.java
 *
 * Created on 23.03.12 16:23
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.dmxmldocu.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;

import com.google.gwt.user.client.rpc.StatusCodeException;
import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.iview.mmgwt.dmxmldocu.client.login.LoginServiceAsync;
import de.marketmaker.iview.mmgwt.dmxmldocu.client.data.User;

/**
 * @author Sebastian Wild (s.wild@market-maker.de)
 */
public class DmxmlDocuServiceAsyncProxy implements DmxmlDocuServiceAsync, LoginServiceAsync {

    public static DmxmlDocuServiceAsyncProxy INSTANCE;

    public final static boolean HOSTED;

    private final DmxmlDocuServiceAsync delegate;

    static {
        final DmxmlDocuServiceAsync delegate = (DmxmlDocuServiceAsync) GWT.create(DmxmlDocuService.class);
        HOSTED = GWT.getHostPageBaseURL().startsWith("http://localhost:8888/") || !GWT.isScript(); // $NON-NLS-0$
        final String rpcUrl = Util.getServerSetting("docuRpcUrl"); // $NON-NLS$
        final String entryPoint;
        if (rpcUrl != null) {
            entryPoint = rpcUrl.replace("$moduleName", Util.getModuleName()); // $NON-NLS$
        }
        else if (HOSTED) {
            entryPoint = "DmxmlDocuService"; // $NON-NLS$
        }
        else {
            entryPoint = "/dmxml-1/" + Util.getModuleName() + "/docu.rpc"; // $NON-NLS$
        }
        ((ServiceDefTarget) delegate).setServiceEntryPoint(entryPoint);
        Firebug.log("Created DmxmlDocuService with entry point " + entryPoint);
        INSTANCE = new DmxmlDocuServiceAsyncProxy(delegate);
    }

    public DmxmlDocuServiceAsyncProxy(DmxmlDocuServiceAsync delegate) {
        this.delegate = delegate;
    }

    @Override
    public void login(LoginData loginData, AsyncCallback<Void> async) {
        delegate.login(loginData, async);
    }

    @Override
    public void getUserFromSession(AsyncCallback<User> async) {
        async.onFailure(null);
    }

    @Override
    public void login(String username, String password, boolean storeSession, final AsyncCallback<User> async) {
        final LoginData loginData = new LoginData(password).withCurrentZone();

        login(loginData, new AsyncCallback<Void>() {
                @Override
                public void onFailure(Throwable caught) {
                    if (caught instanceof StatusCodeException) {
                        if (((StatusCodeException) caught).getStatusCode() == 503) {
                            Util.onError(caught.getMessage(), caught);
                        }
                    }
                    async.onFailure(caught);
                }

                @Override
                public void onSuccess(Void result) {
                    //Create Dummy User to fulfil interface requirements.
                    //TODO: replace with a real user, when such one is available.
                    final User user = new User();
                    user.setUsername(Util.getZoneName());
                    user.setFirstName("Arthur"); //$NON-NLS$
                    user.setLastName("Dent"); //$NON-NLS$
                    user.setId(23);
                    async.onSuccess(user);
                }
            });
    }

    @Override
    public void logout(AsyncCallback<Void> async) {
        delegate.logout(async);
    }

    @Override
    public void getRepository(AsyncCallback<BlocksDocumentation> async) {
        delegate.getRepository(async);
    }

    @Override
    public void sendDmxmlRequest(WrappedDmxmlRequest request,
            AsyncCallback<WrappedDmxmlResponse> async) {
        delegate.sendDmxmlRequest(request, async);
    }
}
