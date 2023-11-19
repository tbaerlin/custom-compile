package de.marketmaker.iview.mmgwt.mmweb.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import de.marketmaker.iview.mmgwt.mmweb.client.util.UrlBuilder;
import de.marketmaker.iview.pmxml.CheckAndSetBackendCredentialsRequest;
import de.marketmaker.iview.pmxml.CheckAndSetBackendCredentialsResponse;

public interface OrderEntryActivityServiceAsync {

    void login(CheckAndSetBackendCredentialsRequest request, AsyncCallback<CheckAndSetBackendCredentialsResponse> async) throws OrderEntryServiceException;

    /**
     * Utility/Convenience class. Use OrderEntryActivityServiceAsync.App.getInstance () to access static instance of OrderEntryActivityServiceAsync
     */
    class App {

        private static OrderEntryActivityServiceAsync app = null;

        public static OrderEntryActivityServiceAsync getInstance() {
            if (app == null) {
                final String entryPoint = MainController.INSTANCE.contextPath + "/" + UrlBuilder.MODULE_NAME + "/orderEntryActivity.rpc"; // $NON-NLS$;
                app = GWT.create(OrderEntryActivityService.class);
                ((ServiceDefTarget) app).setServiceEntryPoint(entryPoint);
            }
            return app;
        }
    }
}
