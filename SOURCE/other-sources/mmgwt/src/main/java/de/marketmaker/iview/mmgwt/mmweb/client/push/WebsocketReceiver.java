/*
 * WebsocketReceiver.java
 *
 * Created on 16.07.15
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.push;

import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.iview.mmgwt.mmweb.client.AbstractMainController;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.events.EventBusRegistry;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PushActivationEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.prices.PriceStore;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DebugUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.UrlBuilder;
import de.marketmaker.iview.tools.i18n.NonNLS;

import elemental.client.Browser;
import elemental.events.MessageEvent;
import elemental.html.WebSocket;

/**
 * @author mloesch
 */
@NonNLS
public class WebsocketReceiver extends AbstractReceiver {

    private WebSocket webSocket;

    public WebsocketReceiver(PushSupport pushSupport, SessionData sessionData) {
        super(pushSupport, sessionData);
    }

    @Override
    protected void startPush() {
        if (this.sessionId == null) {
            createSession();
            return;
        }
        if (this.webSocket == null) {
            this.webSocket = createWebsocket(this.sessionId);
        }
    }

    @Override
    void stopPush() {
        if (this.webSocket == null) {
            Firebug.warn("Push: websocket already stopped");
            return;
        }
        Firebug.log("Push stopping websocket");
        this.webSocket.close();
        this.webSocket = null;
    }

    private WebSocket createWebsocket(final String sessionId) {
        final String url = UrlBuilder.getWebSocketUrl("/push/ws/" + sessionId); // $NON-NLS$
        Firebug.info("create WebSocket for push: " + url);
        final WebSocket webSocket = Browser.getWindow().newWebSocket(url);
        webSocket.setOnerror(event -> {
            DebugUtil.logToServer("Push: onError: " + event.getClass().getName()); // $NON-NLS-0$
            WebsocketReceiver.this.closeSession();
            WebsocketReceiver.this.onFailure(I18n.I.pushConnectionBroken());
        });

        webSocket.setOnopen(event -> Firebug.log("Push: Connected"));

        webSocket.setOnmessage(event -> {
            final MessageEvent me = (MessageEvent) event;
            final String data = (String) me.getData();
            final PushData pushData = PushDataCoderGwt.deserialize(data);
            WebsocketReceiver.this.onMessage(pushData);
        });

        webSocket.setOnclose(event -> {
            Firebug.debug("Push: Closed");
            WebsocketReceiver.this.closeSession();
        });

        return webSocket;
    }

    private void onMessage(PushData data) {
        if (data.isStopPush()) {
            AbstractMainController.INSTANCE.showError(I18n.I.pushSessionStoppedByServer());
            EventBusRegistry.get().fireEvent(new PushActivationEvent(false));
            return;
        }
        if (data.isEmpty()) {
            // hack: see PushServlet#onComet for when this happens
            Firebug.log("onMessage empty - recreate session"); // $NON-NLS-0$
            stopPush();
            changePush(this.pushSupport.createChangeRequestToRecreateSession());
            return;
        }
        PriceStore.INSTANCE.onPush(data);
    }
}