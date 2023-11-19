/*
 * CometReceiver.java
 *
 * Created on 04.02.2010 13:03:21
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.push;

import com.google.gwt.core.client.GWT;

import de.marketmaker.itools.gwtcomet.comet.client.CometClient;
import de.marketmaker.itools.gwtcomet.comet.client.CometListener;
import de.marketmaker.itools.gwtcomet.comet.client.CometSerializer;
import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.iview.mmgwt.mmweb.client.AbstractMainController;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.events.EventBusRegistry;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PushActivationEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.prices.PriceStore;

import java.io.Serializable;
import java.util.List;

/**
 * @author Michael Lösch
 */
class CometReceiver extends AbstractReceiver implements CometListener {

    private final CometSerializer serializer;

    private CometClient client;

    CometReceiver(PushSupport pushSupport, SessionData sessionData) {
        super(pushSupport, sessionData);
        this.serializer = GWT.create(PushPriceDataSerializer.class);
    }

    protected void startPush() {
        if (this.sessionId == null) {
            createSession();
            return;
        }

        if (this.client == null) {
            this.client = new CometClient("/push?sid=" + this.sessionId + "&length=786432", // $NON-NLS-0$ $NON-NLS-1$
                    this.serializer, this);
            Firebug.log("this.client.start();"); // $NON-NLS-0$
            this.client.start();
        }
    }

    void stopPush() {
        if (this.client != null) {
            this.client.stop();
            Firebug.log("client.stop();"); // $NON-NLS-0$
            closeSession();
        }
        this.client = null;
    }

    public void onRefresh() {
        Firebug.log("Push: Refresh"); // $NON-NLS-0$
    }

    public void onConnected(int heartbeat) {
        Firebug.log("Push: Connected"); // $NON-NLS-0$
    }

    public void onDisconnected() {
        Firebug.log("Push: Disconnected"); // $NON-NLS-0$
    }

    public void onHeartbeat() {
        Firebug.log("Push: Heartbeat"); // $NON-NLS-0$
    }

    public void onError(Throwable exception, boolean connected) {
//        DebugUtil.logToServer("Push [" + this.sessionId + "] " + new Date() + ": onError: " , exception); // $NON-NLS-0$
        closeSession();
        onFailure(I18n.I.pushConnectionBroken());
    }

    public void onMessage(List<? extends Serializable> messages) {
        for (Serializable message : messages) {
            final PushData data = (PushData) message;
            if (data.isStopPush()) {
                AbstractMainController.INSTANCE.showError(I18n.I.pushSessionStoppedByServer());
                EventBusRegistry.get().fireEvent(new PushActivationEvent(false));
                break;
            }
            if (data.isEmpty()) {
                // hack: see PushServlet#onComet for when this happens
                Firebug.log("onMessage empty - recreate session"); // $NON-NLS-0$
                stopPush();
                changePush(this.pushSupport.createChangeRequestToRecreateSession());
                break;
            }
            PriceStore.INSTANCE.onPush(data);
        }
    }
}
