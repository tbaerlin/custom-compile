/*
 * WebsocketEndpoint.java
 *
 * Created on 16.07.15
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.server.push;

import de.marketmaker.iview.mmgwt.mmweb.client.push.PushData;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;

import javax.websocket.CloseReason;
import javax.websocket.EncodeException;
import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

import java.io.IOException;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * @author mloesch
 */
@SuppressWarnings("unused")
@ServerEndpoint(value = "/push/ws/{sid}"
        , encoders = {PushDataCoder.class}
        , decoders = {PushDataCoder.class})
public class WebsocketEndpoint {
    private final Log logger = LogFactory.getLog(getClass());

    private WebsocketPushConnect websocketPushConnect;

    private final Map<Session, String> sessionIds
            = Collections.synchronizedMap(new IdentityHashMap<>());

    private final Timer evictionThread = new Timer(getClass().getSimpleName() + "-eviction", true);

    private WebsocketPushConnect getWebsocketPushConnect() {
        if (this.websocketPushConnect != null) {
            return this.websocketPushConnect;
        }
        final WebApplicationContext context = ContextLoader.getCurrentWebApplicationContext();
        this.websocketPushConnect = context.getBean(WebsocketPushConnect.class);
        return this.websocketPushConnect;
    }

    @OnOpen
    public void onOpen(Session session, EndpointConfig config, @PathParam("sid") String sid) throws IOException, EncodeException {
        this.sessionIds.put(session, sid);
        try {
            getWebsocketPushConnect().connect(sid, session);
            this.logger.info("<onOpen> connected " + sid);
        } catch (IllegalArgumentException e) {
            this.logger.warn("<onOpen> invalid session id '" + sid + "': " + format(sid, session));
            this.evictionThread.schedule(new TimerTask() {
                @Override
                public void run() {
                    terminateInvalid(sid, session);
                }
            }, 10000);
            // hack: send empty data to tell client about invalid session
            // see PushReceiver#onMessage
            session.getBasicRemote().sendObject(new PushData());
        }
    }

    private void terminateInvalid(String sid, Session session) {
        if (sid.equals(this.sessionIds.remove(session))) {
            this.logger.warn("<terminateInvalid> for " + format(sid, session));
            try {
                session.close();
            } catch (IOException e) {
                this.logger.warn("<terminateInvalid> failed", e);
            }
        }
        else if (this.logger.isDebugEnabled()) {
            this.logger.debug("<terminateInvalid> already terminated " + format(sid, session));
        }
    }

    @OnMessage
    public void onMessage(String message) {
        this.logger.debug("onMessage - " + message);
    }

    @OnClose
    public void onClose(Session session, CloseReason closeReason,
            @PathParam("sid") String sessionId) {
        final String sid = this.sessionIds.remove(session);
        if (sid == null) {
            return;
        }
        if (!sid.equals(sessionId)) {
            throw new IllegalStateException("<onClose> sid found in map doesn't match with sid in method's parameters: " + sid + " : " + sessionId);
        }
        getWebsocketPushConnect().disconnect(sid);
        this.logger.info("<onClose> disconnected " + format(sid, session) + ", " + closeReason);
    }

    @OnError
    public void onError(Session session, Throwable thr) {
        this.logger.debug("onError", thr);
    }

    private String format(String sid, Session session) {
        if (session == null) {
            return sid;
        }
        return sid + "/" + session.getClass().getSimpleName() + "@" + Integer.toHexString(session.hashCode());
    }
}