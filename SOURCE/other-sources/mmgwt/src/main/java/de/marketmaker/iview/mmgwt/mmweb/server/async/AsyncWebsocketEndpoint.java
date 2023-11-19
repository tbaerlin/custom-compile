package de.marketmaker.iview.mmgwt.mmweb.server.async;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;

import javax.websocket.CloseReason;
import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

/**
 * Author: umaurer<br/>
 * Created: 29.08.14<br/>
 * <br/>
 * To enable WebSocket proxy in Apache httpd the following conditions must be met:
 * <ul>
 *     <li>Tomcat Version must be greater than 7.0.47</li>
 *     <li>Apache Version must be greater than 2.4.5. To migrate from Apache 2.2.x to 2.4.x remove the lines of the form
 *         <pre>
 *              Order Allow,Deny
 *              Allow from All
 *         </pre>
 *         and replace them by
 *         <pre>
 *             Require all granted
 *         </pre>
 *         for other Access control statements see <a href="http://httpd.apache.org/docs/2.4/upgrading.html">http://httpd.apache.org/docs/2.4/upgrading.html</a>.
 *     </li>
 *     <li>Apache must be compiled with option --enable-proxy-wstunnel</li>
 *     <li>httpd config must contain something like this (LoadModule in httpd.conf, the rest in httpd-include.conf):<br/>
 *          <pre>
 *              LoadModule proxy_wstunnel_module modules/mod_proxy_wstunnel.so
 *              ...
 *              &lt;Proxy balancer://wscluster&gt;
 *                  BalancerMember ws://localhost:8080 route=localhost
 *                  ProxySet stickysession=JSESSIONID
 *              &lt;/Proxy&gt;
 *              ...
 *              RewriteRule "^/pmxml-1/pmweb/asyncWs/(.*)" balancer://wscluster%{REQUEST_URI} [P]
 *          </pre>
 *     </li>
 * </ul>
 */
@ServerEndpoint(value = "/pmweb/asyncWs/{sid}"
        , encoders = {AsyncDataCoder.class}
        , decoders = {AsyncDataCoder.class})
public class AsyncWebsocketEndpoint {
    private final Log logger = LogFactory.getLog(getClass());
    private AsyncServiceImpl asyncService;

    private AsyncServiceImpl getAsyncService(EndpointConfig config) {
        if (this.asyncService != null) {
            return this.asyncService;
        }
        final WebApplicationContext context = ContextLoader.getCurrentWebApplicationContext();
        final String name = context.getBeanNamesForType(AsyncServiceImpl.class)[0];
        this.asyncService = (AsyncServiceImpl) context.getBean(name);
        return this.asyncService;
    }

    @OnOpen
    public void onOpen(Session session, EndpointConfig config, @PathParam("sid") String sessionId) {
        this.logger.debug("onOpen sessionId: " + sessionId);
        final AsyncServiceImpl asyncService = getAsyncService(config);
        asyncService.connect(sessionId, new WebsocketClient(session));
    }

    @OnMessage
    public void onMessage(String message) {
        this.logger.debug("onMessage - " + message);
    }

    @OnClose
    public void onClose(Session session, CloseReason closeReason, @PathParam("sid") String sessionId) {
        this.logger.debug("onClose - " + sessionId);
        if (this.asyncService != null) {
            this.asyncService.closeSession(sessionId, true);
        }
    }

    @OnError
    public void onError(Session session, Throwable thr) {
        this.logger.debug("onError", thr);
    }
}