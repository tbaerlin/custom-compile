package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.async;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import de.marketmaker.iview.mmgwt.mmweb.client.MainController;
import de.marketmaker.iview.mmgwt.mmweb.client.util.UrlBuilder;
import de.marketmaker.iview.pmxml.EvalLayoutRequest;
import de.marketmaker.iview.pmxml.GetStateResponse;

public interface AsyncServiceAsync {
    /**
     * @param vwdId
     * @return session id
     */
    void createSession(String vwdId, AsyncCallback<String> async);

    void closeSession(String sessionId, boolean cancelHandles, AsyncCallback<Void> async);

    void getStateResponse(String sessionId, String handle, boolean registerForPush, AsyncCallback<GetStateResponse> async);

    void createUuid(AsyncCallback<String> async);

    void evalLayout(String sessionId, EvalLayoutRequest request, AsyncCallback<AsyncHandleResult> async);

    void unregisterHandle(String handle, AsyncCallback<Void> async);

    void getAsyncData(AsyncCallback<AsyncData> async);

    public static class App {
        private static AsyncServiceAsync app = null;

        public static AsyncServiceAsync getInstance() {

            final String serverPrefix = UrlBuilder.getServerPrefix();
            final String entryPoint;

            if (serverPrefix != null) {
                // diese Variante wird in idoc verwendet
                String asyncRpcUrl = serverPrefix + "/pmxml-1/vwd/asyncadm.rpc"; // $NON-NLS$
                entryPoint = asyncRpcUrl.replace("$moduleName", UrlBuilder.MODULE_NAME); // $NON-NLS$
            } else {
                final String contextPath = MainController.INSTANCE == null ? "/dmxml-1" : MainController.INSTANCE.contextPath; // $NON-NLS$
                entryPoint = contextPath + "/" + UrlBuilder.MODULE_NAME + "/asyncadm.rpc"; // $NON-NLS$
            }
            return getInstance(entryPoint);
        }

        private static AsyncServiceAsync getInstance(String entryPoint) {
            if (app == null) {
                app = (AsyncServiceAsync) GWT.create(
                        AsyncService.class);
                ((ServiceDefTarget) app).setServiceEntryPoint(entryPoint);
            }
            return app;
        }

    }

}
