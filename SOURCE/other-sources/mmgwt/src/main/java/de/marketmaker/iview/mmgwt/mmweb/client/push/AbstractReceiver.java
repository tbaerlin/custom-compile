/*
 * AbstractReceiver.java
 *
 * Created on 16.07.15
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.push;

import com.google.gwt.user.client.rpc.AsyncCallback;

import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.iview.mmgwt.mmweb.client.AbstractMainController;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.data.User;
import de.marketmaker.iview.mmgwt.mmweb.client.events.EventBusRegistry;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PushActivationEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DebugUtil;

import java.util.ArrayList;

/**
 * @author mloesch
 */
public abstract class AbstractReceiver {
    private final ArrayList<PushChangeRequest> pending = new ArrayList<>();

    protected String sessionId;

    protected final PushSupport pushSupport;

    protected final SessionData sessionData;

    public static AbstractReceiver create(PushSupport pushSupport, SessionData sessionData) {
        final User user = sessionData.getUser();
        final String id = user != null ? user.getUid() : "<not set>"; // $NON-NLS$
        if (useWebsocket()) {
            DebugUtil.logToServer("AbstractReceiver <create> creating WebsocketClient for PricePush (User: " + id + " / Agent: " + userAgent() + ")");
            return new WebsocketReceiver(pushSupport, sessionData);
        }
        else {
            DebugUtil.logToServer("AbstractReceiver <create> creating CometClient for PricePush (User: " + id + " / Agent: " + userAgent() + ")");
            return new CometReceiver(pushSupport, sessionData);
        }
    }

    private static boolean useWebsocket() {
        return isWebsocketSupported();
    }

    protected AbstractReceiver(PushSupport pushSupport, SessionData sessionData) {
        this.pushSupport = pushSupport;
        this.sessionData = sessionData;
    }

    protected void createSession() {
        final User user = this.sessionData.getUser();
        PushServiceAsync.App.getPushInstance().createSession(user.getVwdId(), user.getAppId(), useWebsocket(),
                new AsyncCallback<PushSessionResponse>() {
                    public void onFailure(Throwable throwable) {
                        AbstractReceiver.this.onFailure(I18n.I.pushConnectionCurrentlyNotAvailable());
                        Firebug.debug(I18n.I.pushConnectionCurrentlyNotAvailable() + "\n" + throwable);
                    }

                    public void onSuccess(PushSessionResponse pushSessionResponse) {
                        if (pushSessionResponse.getState() == PushSessionResponse.State.OK) {
                            sessionId = pushSessionResponse.getSessionId();
                            Firebug.debug("createSession " + sessionId);
                            onSessionCreated();
                        }
                    }
                });
    }

    protected void onFailure(String message) {
        if (this.pushSupport.isActive()) {
            AbstractMainController.INSTANCE.showError(message);
            this.pending.clear();
            this.sessionId = null;
            EventBusRegistry.get().fireEvent(new PushActivationEvent(false));
        }
    }

    private void onSessionCreated() {
        startPush();
        for (PushChangeRequest request : this.pending) {
            changePush(request);
        }
        this.pending.clear();
    }

    protected void closeSession() {
        if (this.sessionId == null) {
            return;
        }
        final String sid = this.sessionId;
        this.sessionId = null;
        PushServiceAsync.App.getPushInstance().closeSession(sid, new AsyncCallback<Void>() {
            public void onFailure(Throwable throwable) {
                Firebug.debug("closeSession (" + sid + "): close push session failed");
            }

            public void onSuccess(Void aVoid) {
                Firebug.debug("close push session");
            }
        });
    }

    public void changePush(PushChangeRequest request) {
        if (this.sessionId == null) {
            this.pending.add(request);
            startPush();
            return;
        }
        request.setSessionId(this.sessionId);

        final String sid = this.sessionId;
        Firebug.debug("modifySession called (" + sid + ")");
        PushServiceAsync.App.getPushInstance().modifySession(request, new AsyncCallback<PushChangeResponse>() {
            public void onFailure(Throwable throwable) {
                Firebug.error("modifySession (" + sid + ") failed", throwable);
            }

            public void onSuccess(PushChangeResponse response) {
                Firebug.debug("modifySession (" + sid + "): " + response.getState());
            }
        });
    }

    public static native boolean isWebsocketSupported() /*-{
        return 'WebSocket' in window;
    }-*/;

    private static native String userAgent() /*-{
        return navigator.userAgent;
    }-*/;

    protected abstract void startPush();

    abstract void stopPush();
}