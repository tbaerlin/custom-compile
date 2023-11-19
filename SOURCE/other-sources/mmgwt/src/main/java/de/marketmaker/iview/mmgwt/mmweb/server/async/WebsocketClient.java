package de.marketmaker.iview.mmgwt.mmweb.server.async;

import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.async.AsyncData;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.websocket.Session;

/**
 * Author: umaurer
 * Created: 29.09.14
 */
public class WebsocketClient implements Client {
    private final Log logger = LogFactory.getLog(getClass());
    private final Session session;

    public WebsocketClient(Session session) {
        this.session = session;
    }

    @Override
    public void write(String sessionId, AsyncData data) {
        try {
            this.session.getBasicRemote().sendObject(data);
        } catch (Exception e) {
            logger.warn("cannot write to WebSocket client: " + sessionId, e);
        }
    }

    @Override
    public void sendPing(String sessionId) {
        try {
            this.session.getBasicRemote().sendObject(AsyncData.PING);
        } catch (Exception e) {
            logger.warn("cannot write connection test message to WebSocket client: " + sessionId, e);
        }
    }
}
