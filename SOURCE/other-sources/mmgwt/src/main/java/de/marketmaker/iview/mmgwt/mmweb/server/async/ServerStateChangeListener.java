package de.marketmaker.iview.mmgwt.mmweb.server.async;

import de.marketmaker.iview.pmxml.SEAsyncStateChange;

/**
 * User: umaurer
 * Date: 18.10.13
 * Time: 11:44
 */
public interface ServerStateChangeListener {
    void onStateChange(SEAsyncStateChange event);
}
