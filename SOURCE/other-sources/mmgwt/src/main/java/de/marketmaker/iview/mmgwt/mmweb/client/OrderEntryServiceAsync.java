/*
 * OrderEntryServiceAsync.java
 *
 * Created on 16.01.14 14:13
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import de.marketmaker.iview.mmgwt.mmweb.client.util.UrlBuilder;
import de.marketmaker.iview.pmxml.LoginBrokerRequest;
import de.marketmaker.iview.pmxml.LoginBrokerResponse;

/**
 * @author Markus Dick
 */
public interface OrderEntryServiceAsync {
    void login(LoginBrokerRequest loginBrokerRequest, AsyncCallback<LoginBrokerResponse> async) throws OrderEntryServiceException;

    /**
     * Utility/Convenience class.
     * Use OrderEntryServiceAsync.App.getInstance () to access static instance of UserAsync
     */
    public static class App {
        private static OrderEntryServiceAsync app = null;

        public static OrderEntryServiceAsync getInstance() {
            if (app == null) {
                final String entryPoint = MainController.INSTANCE.contextPath + "/" + UrlBuilder.MODULE_NAME + "/orderEntry.rpc"; // $NON-NLS$;
                app = (OrderEntryServiceAsync) GWT.create(OrderEntryService.class);
                ((ServiceDefTarget) app).setServiceEntryPoint(entryPoint);
            }
            return app;
        }
    }
}
