package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.async;

import java.io.Serializable;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Command;

import de.marketmaker.itools.gwtcomet.comet.client.CometClient;
import de.marketmaker.itools.gwtcomet.comet.client.CometListener;
import de.marketmaker.itools.gwtcomet.comet.client.CometSerializer;
import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.iview.mmgwt.mmweb.client.AbstractMainController;
import de.marketmaker.iview.mmgwt.mmweb.client.MainController;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.events.PmAsyncEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DebugUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.UrlBuilder;

/**
 * Created on 15.05.14
 * Copyright (c) market maker Software AG. All Rights Reserved.
 * @author mloesch
 */
public class AsyncCometHandler implements AsyncHandler, CometListener {

    private final AmokDetector detector;

    private CometClient client;

    private final ErrorCallback errorCallback;

    private final TestMessageCallback testMessageCallback;

    private String sessionId;

    private Command connectedCallback;

    public AsyncCometHandler(TestMessageCallback testMessageCallback, ErrorCallback errorCallback) {
        this.detector = new AmokDetector(this);
        this.errorCallback = errorCallback;
        this.testMessageCallback = testMessageCallback;
    }

    private CometClient createClient(String sessionId, String serverPrefix,
                                     CometSerializer serializer) {
        if (serverPrefix != null) {
            String cometUrl = serverPrefix + "/pmxml-1/pmweb/asyncComet"; // $NON-NLS$
            return new CometClient(cometUrl + "?sid=" + sessionId + "&length=786432", serializer, this); // $NON-NLS$
        }
        return new CometClient(MainController.INSTANCE.contextPath + "/pmweb/asyncComet?sid=" + sessionId + "&length=786432", serializer, this); // $NON-NLS$
    }

    @Override
    public void start(String sessionId, Command onConnectedCallback) {
        if (this.client == null) {
            this.sessionId = sessionId;
            final String serverPrefix = UrlBuilder.getServerPrefix();
            final CometSerializer serializer = GWT.create(AsyncDataSerializer.class);
            this.client = createClient(sessionId, serverPrefix, serializer);
            Firebug.debug("CometHandler <startComet> starting client for " + sessionId);
            this.connectedCallback = onConnectedCallback;
            this.client.start();
        }
    }

    @Override
    public void stop() {
        if (this.client == null) {
            return;
        }
        Firebug.debug("CometHandler <stopComet> stopping client for " + this.sessionId); // $NON-NLS-0$
        this.client.stop();
        this.client = null;
        this.sessionId = null;
    }

    void amokDetected() {
        DebugUtil.logToServer("CometHandler <amokDetected> stopping Comet for session " + this.sessionId);
        stop();
        DebugUtil.logToServer("CometHandler <amokDetected> fire shutdown event");
        AbstractMainController.INSTANCE.fireShutdownEvent(false, true);
    }

    @Override
    public void onConnected(int heartbeat) {
        this.detector.reportConnected();
        Firebug.debug("CometHandler <onConnected> heartbeat: " + heartbeat + " / sessionId: " + this.sessionId);
        if (this.connectedCallback != null) {
            Firebug.debug("CometHandler <onConnected> calling connectedCallback for sessionId " + this.sessionId);
            this.connectedCallback.execute();
            this.connectedCallback = null;
        }
    }

    @Override
    public void onDisconnected() {
        Firebug.debug("CometHandler <onDisconnected>");
    }

    @Override
    public void onError(Throwable throwable, boolean b) {
        DebugUtil.logToServer("CometHandler <onError> sessionId: " + this.sessionId, throwable); // $NON-NLS-0$
        Firebug.error("CometHandler <onError>: ", throwable);
        stop();
        this.errorCallback.onError(this.sessionId);
    }

    @Override
    public void onHeartbeat() {
        Firebug.debug("CometHandler <onHeartbeat> session " + this.sessionId);
    }

    @Override
    public void onRefresh() {
        Firebug.debug("CometHandler <onRefresh> session " + this.sessionId);
    }

    @Override
    public void onMessage(List<? extends Serializable> serializables) {
        for (Serializable serializable : serializables) {
            final AsyncData asyncData = (AsyncData) serializable;
            if (this.testMessageCallback != null && asyncData.getState() == AsyncData.State.PING) {
                this.testMessageCallback.onTestMessageReceived();
            }
            else {
                PmAsyncEvent.fire(asyncData);
            }
        }
    }

    @Override
    public boolean isActive() {
        return this.client != null && this.client.isRunning();
    }
}