package de.marketmaker.iview.mmgwt.mmweb.client.events;

import com.google.gwt.event.shared.EventHandler;

/**
 * Author: umaurer
 * Created: 19.10.15
 */
public interface PushActivationHandler extends EventHandler {
    void onPushActivated(PushActivationEvent event);
}
