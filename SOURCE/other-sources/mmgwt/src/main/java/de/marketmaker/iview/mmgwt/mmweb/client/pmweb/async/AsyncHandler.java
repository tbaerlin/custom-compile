package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.async;

import com.google.gwt.user.client.Command;
import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.data.User;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DebugUtil;

/**
 * Author: umaurer
 * Created: 29.09.14
 */
public interface AsyncHandler {
    boolean isActive();

    void start(String sessionId, Command onConnectedCallback);

    void stop();

    interface ErrorCallback {
        void onError(String sessionId);
    }

    interface TestMessageCallback {
        void onTestMessageReceived();
    }

    public static class Factory {
        public static AsyncHandler create(TestMessageCallback testMessageCallback, ErrorCallback errorCallback) {
            final User user = SessionData.INSTANCE.getUser();
            final String id = user != null ? user.getUid() : "<not set>"; // $NON-NLS$
            final boolean forceComet = "true".equals(SessionData.INSTANCE.getUserProperty("forceComet")); // $NON-NLS$
            if (!forceComet && isWebsocketSupported()) {
                final String msg = "AsyncHandler.Factory <create> creating AsyncWebsocketHandler (User: " + id + " / Agent: " + userAgent() + ")"; // $NON-NLS$
                DebugUtil.logToServer(msg);
                Firebug.debug(msg);
                return new AsyncWebsocketHandler(testMessageCallback, errorCallback);
            } else {
                final String msg = "AsyncHandler.Factory <create> creating AsyncCometHandler (User: " + id + " / Agent: " + userAgent() + ")"; // $NON-NLS$
                DebugUtil.logToServer(msg);
                Firebug.debug(msg);
                return new AsyncCometHandler(testMessageCallback, errorCallback);
            }
        }

        public static native boolean isWebsocketSupported() /*-{
            return 'WebSocket' in window;
        }-*/;

        private static native String userAgent() /*-{
            return navigator.userAgent;
        }-*/;
    }
}
