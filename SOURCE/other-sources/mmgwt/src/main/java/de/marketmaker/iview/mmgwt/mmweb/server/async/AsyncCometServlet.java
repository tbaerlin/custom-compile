package de.marketmaker.iview.mmgwt.mmweb.server.async;

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

/**
 * User: umaurer
 * Date: 17.10.13
 * Time: 11:24
 */
public class AsyncCometServlet extends CometServlet {
    private final Log logger = LogFactory.getLog(getClass());
    private AsyncServiceImpl asyncService;

    @Override
    public void init() throws ServletException {
        super.init();
        final WebApplicationContext context
                = WebApplicationContextUtils.getRequiredWebApplicationContext(getServletContext());
        final String name = context.getBeanNamesForType(AsyncServiceImpl.class)[0];
        this.asyncService = (AsyncServiceImpl) context.getBean(name);
    }

    @Override
    protected void doComet(CometServletResponse cometResponse) throws ServletException, IOException {
        CometSession cometSession = cometResponse.getSession(false);
        if (cometSession == null) {
            try {
                this.logger.trace("<doComet> cometSession is null -> creating session");
                cometSession = cometResponse.getSession();
            }
            catch (Exception e) {
                this.logger.error("<doComet> Could not create cometSession", e);
                cometResponse.sendError(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
        }

        final String sid = cometResponse.getRequest().getParameter("sid");
        if (sid == null) {
            this.logger.error("<doComet> sid == null!");
            cometResponse.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        this.logger.debug("<doComet> connecting " + sid + " / AsyncServlet: " + toString());

        try {
            final Client client = new CometClient(cometSession);
            this.asyncService.connect(sid, client);
            this.logger.debug("<doComet> connected " + sid);
        }
        catch (IllegalArgumentException e) {
            this.logger.error("<doComet> invalid session id '" + sid + "'");
        }
    }


    @Override
    public void cometTerminated(CometServletResponse cometResponse, boolean serverInitiated) {
        this.logger.debug("<cometTerminated> cometResponse=" + cometResponse + ", serverInitiated: " + serverInitiated);
    }
}
