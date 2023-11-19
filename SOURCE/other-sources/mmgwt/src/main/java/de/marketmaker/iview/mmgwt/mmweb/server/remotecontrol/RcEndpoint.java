package de.marketmaker.iview.mmgwt.mmweb.server.remotecontrol;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;

import javax.websocket.OnClose;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;

/**
 * Author: umaurer
 * Created: 19.02.15
 */
@ServerEndpoint(value = "/rcEndpoint/{sessionId}")
public class RcEndpoint {
    private final Log logger = LogFactory.getLog(getClass());
    private RcController rcController;

    private RcController getRcController() {
        if (this.rcController == null) {
            final WebApplicationContext context = ContextLoader.getCurrentWebApplicationContext();
            final String name = context.getBeanNamesForType(RcController.class)[0];
            this.rcController = (RcController) context.getBean(name);
        }
        return this.rcController;
    }

    @OnOpen
    public void onOpen(Session session, @PathParam("sessionId") String sessionId) {
        this.logger.info("register session: " + sessionId);
        getRcController().registerSession(sessionId, session);
    }

    @OnClose
    public void onClose(@PathParam("sessionId") String sessionId) {
        this.logger.info("unregister session: " + sessionId);
        getRcController().unregisterSession(sessionId);
    }
}
