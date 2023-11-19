package de.marketmaker.iview.mmgwt.mmweb.server.remotecontrol;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.util.WebUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.websocket.Session;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Author: umaurer
 * Created: 19.02.15
 */
@Controller
public class RcController {
    private final Log logger = LogFactory.getLog(this.getClass());
    private final ConcurrentHashMap<String, Session> mapSessions = new ConcurrentHashMap<>();

    public void registerSession(String sessionId, Session session) {
        this.mapSessions.put(sessionId, session);
    }

    public void unregisterSession(String sessionId) {
        this.mapSessions.remove(sessionId);
    }

    @RequestMapping("/*/rcController.frm")
    protected void sendToken(HttpServletRequest request, HttpServletResponse response) throws IOException, URISyntaxException {
        if ("createSession".equals(request.getParameter("action"))) {
            final String sessionId = UUID.randomUUID().toString();
            this.logger.info("rcController created sessionId: " + sessionId);
            final Cookie cookie = new Cookie("sessionId", sessionId);
            response.addCookie(cookie);
            response.setContentType("text/plain");
            response.getWriter().print(sessionId);
            return;
        }

        final String token = request.getParameter("token");
        if (token == null) {
            this.logger.info("rcController no token");
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "no token");
            return;
        }

        final String sessionId = WebUtils.getCookie(request, "sessionId").getValue();
        if (sessionId == null) {
            this.logger.info("rcController no sessionId");
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "no sessionId");
            return;
        }
        final Session session = this.mapSessions.get(sessionId);
        if (session == null) {
            this.logger.info("rcController no session");
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "sessionId " + sessionId + " not available");
            return;
        }
        this.logger.info("rcController send token to " + sessionId + ": " + token);
        session.getBasicRemote().sendText(token);
        response.setContentType("text/plain");
        response.getWriter().print("Ok");
    }

}
