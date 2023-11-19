package de.marketmaker.iview.mmgwt.mmweb.server.async;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;

/**
 * Simple test for WebSocket connections.
 * Use client/wstest.html as client.
 * Add
 * <br/>
 * <code>&lt;Proxy balancer://wscluster>
 *      BalancerMember ws://localhost:8080 route=localhost
 *      ProxySet stickysession=JSESSIONID
 *      &lt;/Proxy></code>
 * <br/>
 * and
 * <br/>
 * <code>RewriteRule "^/pmxml-1/wsTest" balancer://wscluster%{REQUEST_URI} [P]</code>
 * <br/>
 * to httpd.conf (before other pmxml-1 lines).
 *
 * Author: umaurer
 * Created: 09.02.15
 */
@ServerEndpoint(value = "/wsTest")
public class WsTestEndpoint {
    private final Log logger = LogFactory.getLog(getClass());

    @OnOpen
    public void onOpen(Session session) {
        this.logger.info("session connected: " + session.getId());
        session.getUserProperties().put("start", System.currentTimeMillis());

        final long expirationMillis = System.currentTimeMillis() - 30000;
        for (Session s : session.getOpenSessions()) {
            close(s, expirationMillis);
        }
    }

    private boolean close(Session s, long expirationMillis) {
        if (((Long)s.getUserProperties().get("start")) < expirationMillis) {
            this.logger.info("session " + s.getId() + " expired -> close");
            try {
                s.close();
            } catch (IOException e) {
                this.logger.warn("closing expired session (" + s.getId() + ") failed: " + e.getMessage());
            }
            return true;
        }
        return false;
    }

    @OnError
    public void onError(Session session, Throwable t) {
        this.logger.warn("session error: " + session.getId(), t);
    }

    @OnClose
    public void onClose(Session session) {
        this.logger.info("session closed: " + session.getId());
    }

    @OnMessage
    public void onMessage(Session session, String text) {
        final long expirationMillis = System.currentTimeMillis() - 30000;
        for (Session s : session.getOpenSessions()) {
            if (close(s, expirationMillis)) {
                continue;
            }
            try {
                if (s.isOpen()) {
                    s.getBasicRemote().sendText("forward: " + text);
                }
            } catch (IOException e) {
                this.logger.warn("sending text to session (" + s.getId() + ") failed: " + e.getMessage());
            }
        }
    }
}
