package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.events;

import com.google.gwt.event.shared.EventHandler;

/**
 * @author umaurer
 */
public interface PmAsyncHandler extends EventHandler {
    void onAsync(PmAsyncEvent event);
}
