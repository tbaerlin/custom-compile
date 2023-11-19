package de.marketmaker.iview.mmgwt.mmweb.server.async;

import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.async.AsyncData;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Author: umaurer
 * Created: 29.08.14
 */
public interface Client {
    public static final Client EMPTY = new Client() {
        private final Log logger = LogFactory.getLog(getClass());
        @Override
        public void write(String sessionId, AsyncData data) {
            this.logger.error("no client object found for sessionId: " + sessionId);
        }

        @Override
        public void sendPing(String sessionId) {
            this.logger.error("no client object found for sessionId: " + sessionId);
        }
    };

    void write(String sessionId, AsyncData data);
    void sendPing(String sessionId);
}
