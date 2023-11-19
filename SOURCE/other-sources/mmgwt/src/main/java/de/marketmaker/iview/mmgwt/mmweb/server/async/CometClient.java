package de.marketmaker.iview.mmgwt.mmweb.server.async;

import de.marketmaker.itools.gwtcomet.comet.server.CometSession;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.async.AsyncData;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Author: umaurer
 * Created: 29.08.14
 */
public class CometClient implements Client {
    private final Log logger = LogFactory.getLog(getClass());

    private CometSession session;

    public CometClient(CometSession session) {
        this.session = session;
    }

    @Override
    public void write(String sessionId, AsyncData data) {
        try {
            this.session.enqueue(data);
        }
        catch (Exception e) {
            this.logger.error("cannot write AsyncData to session " + sessionId, e);
            //TODO: Hier macht cancel des Jobs vermutlich keinen Sinn. Es koennte nur die comet-Verbindung temporaer gestoert sein
        }
    }

    @Override
    public void sendPing(String sessionId) {
        try {
            this.session.enqueue(AsyncData.PING);
        }
        catch (Exception e) {
            this.logger.error("cannot write connection test message to session " + sessionId, e);
        }
    }
}