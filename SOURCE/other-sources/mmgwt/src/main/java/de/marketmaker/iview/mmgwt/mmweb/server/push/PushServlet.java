package de.marketmaker.iview.mmgwt.mmweb.server.push;

import de.marketmaker.itools.gwtcomet.comet.server.CometServlet;
import de.marketmaker.itools.gwtcomet.comet.server.CometServletResponse;
import de.marketmaker.itools.gwtcomet.comet.server.CometSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import de.marketmaker.iview.mmgwt.mmweb.client.push.PushData;

/**
 * Created on 04.02.2010 11:32:13
 * Copyright (c) market maker Software AG. All Rights Reserved.
 * @author Michael Lösch
 */
public class PushServlet extends CometServlet {
    private final Log logger = LogFactory.getLog(getClass());

    private CometPushConnect cometPushConnect;

    private final Map<CometServletResponse, String> sessionIds
            = Collections.synchronizedMap(new IdentityHashMap<>());

    private final Timer evictionThread = new Timer(getClass().getSimpleName() + "-eviction", true);

    @Override
    public void destroy() {
        super.destroy();
        this.evictionThread.cancel();
    }

    @Override
    public void init() throws ServletException {
        super.init();
        final WebApplicationContext context
                = WebApplicationContextUtils.getRequiredWebApplicationContext(getServletContext());
        final String name = context.getBeanNamesForType(CometPushConnect.class)[0];
        this.cometPushConnect = (CometPushConnect) context.getBean(name);
    }


    @Override
    protected void doComet(CometServletResponse cometResponse) throws ServletException, IOException {
        CometSession cometSession = cometResponse.getSession(false);
        if (cometSession == null) {
            cometSession = cometResponse.getSession();
        }
        final String sid = cometResponse.getRequest().getParameter("sid");
        if (sid == null) {
            cometResponse.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        this.sessionIds.put(cometResponse, sid);
        try {
            this.cometPushConnect.connect(sid, cometSession);
            this.logger.info("<doComet> connected " + sid);
        } catch (IllegalArgumentException e) {
            this.logger.warn("<doComet> invalid session id '" + sid + "': " + format(sid, cometResponse));
            this.evictionThread.schedule(new TimerTask() {
                @Override
                public void run() {
                    terminateInvalid(sid, cometResponse);
                }
            }, 10000);
            // hack: send empty data to tell client about invalid session
            // see PushReceiver#onMessage
            cometSession.enqueue(new PushData());
        }
    }

    @Override
    public void cometTerminated(CometServletResponse r, boolean serverInitiated) {
        final String sid = this.sessionIds.remove(r);
        if (sid == null) {
            return;
        }
        this.cometPushConnect.disconnect(sid);
        this.logger.info("<cometTerminated> terminated " + format(sid, r) + ", " + serverInitiated);
    }

    private void terminateInvalid(String sid, CometServletResponse r) {
        if (sid.equals(this.sessionIds.remove(r))) {
            this.logger.warn("<terminateInvalid> for " + format(sid, r));
            try {
                r.terminate();
            } catch (IOException e) {
                this.logger.warn("<terminateInvalid> failed", e);
            }
        }
        else if (this.logger.isDebugEnabled()) {
            this.logger.debug("<terminateInvalid> already terminated " + format(sid, r));
        }
    }

    private String format(String sid, CometServletResponse r) {
        if (r == null) {
            return sid;
        }
        return sid + "/" + r.getClass().getSimpleName() + "@" + Integer.toHexString(r.hashCode());
    }
}
