package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.async;

import com.google.gwt.user.client.Command;
import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.events.PmAsyncEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.util.UrlBuilder;
import elemental.client.Browser;
import elemental.events.Event;
import elemental.events.EventListener;
import elemental.events.MessageEvent;
import elemental.html.WebSocket;

/**
 * Author: umaurer
 * Created: 29.09.14
 */
public class AsyncWebsocketHandler implements AsyncHandler {
    private final ErrorCallback errorCallback;
    private final TestMessageCallback testMessageCallback;
    private WebSocket webSocket;

    public AsyncWebsocketHandler(TestMessageCallback testMessageCallback, ErrorCallback errorCallback) {
        this.errorCallback = errorCallback;
        this.testMessageCallback = testMessageCallback;
    }

    @Override
    public boolean isActive() {
        return this.webSocket != null && this.webSocket.getReadyState() == WebSocket.OPEN;
    }

    @Override
    public void start(String sessionId, final Command onConnectedCallback) {
        if (this.webSocket == null) {
            this.webSocket = createWebsocket(sessionId, onConnectedCallback);
        }
    }

    @Override
    public void stop() {
        if (this.webSocket == null) {
            Firebug.warn("AsyncWebsocketHandler <stop> -> websocket already stopped");
            return;
        }
        Firebug.debug("AsyncWebsocketHandler <stop> stopping websocket"); // $NON-NLS-0$
        this.webSocket.close();
        this.webSocket = null;
    }

    private WebSocket createWebsocket(final String sessionId, final Command onConnectedCallback) {
        final String url = UrlBuilder.getAsyncPmUrl(sessionId);
        Firebug.info("create WebSocket for async: " + url);
        final WebSocket webSocket = Browser.getWindow().newWebSocket(url);
        webSocket.setOnerror(new EventListener() {
            @Override
            public void handleEvent(Event event) {
                Firebug.warn("WebSocket.onError " + event.getClass().getName());
                errorCallback.onError(sessionId);
            }
        });
        webSocket.setOnopen(new EventListener() {
            boolean firstOpen = true;

            @Override
            public void handleEvent(Event event) {
                Firebug.debug("WebSocket.onOpen");
                if (this.firstOpen) {
                    onConnectedCallback.execute();
                    this.firstOpen = false;
                }
                else {
                    Firebug.warn("AsyncWebsocketHandler.WebSocket.onOpen");
                }
            }
        });
        webSocket.setOnmessage(new EventListener() {
            @Override
            public void handleEvent(Event event) {
                final MessageEvent me = (MessageEvent) event;
                final String data = (String) me.getData();
                final AsyncData asyncData = AsyncDataCoderGwt.deserialize(data);
                if (testMessageCallback != null && asyncData.getState() == AsyncData.State.PING) {
                    testMessageCallback.onTestMessageReceived();
                }
                else {
                    PmAsyncEvent.fire(asyncData);
                }
            }
        });
        webSocket.setOnclose(new EventListener() {
            @Override
            public void handleEvent(Event event) {
                Firebug.debug("WebSocket.onClose");
            }
        });
        return webSocket;
    }
}