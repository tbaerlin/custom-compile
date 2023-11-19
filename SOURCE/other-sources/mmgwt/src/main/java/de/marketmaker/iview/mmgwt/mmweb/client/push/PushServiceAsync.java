package de.marketmaker.iview.mmgwt.mmweb.client.push;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;

public interface PushServiceAsync {
    void createSession(String vwdId, String appId, boolean websocket, AsyncCallback<PushSessionResponse> async);

    void modifySession(PushChangeRequest request, AsyncCallback<PushChangeResponse> async);

    void closeSession(String id, AsyncCallback<Void> async);

    void getPushData(AsyncCallback<PushData> async);

    public static class App {
        private static PushServiceAsync app = null;

        public static PushServiceAsync getPushInstance() {
            return getInstance("/pushadm.rpc"); // $NON-NLS$
        }

        private static PushServiceAsync getInstance(String entryPoint) {
            if (app == null) {
                app = GWT.create(PushService.class);
                ((ServiceDefTarget) app).setServiceEntryPoint(entryPoint);
            }
            return app;
        }
    }
}