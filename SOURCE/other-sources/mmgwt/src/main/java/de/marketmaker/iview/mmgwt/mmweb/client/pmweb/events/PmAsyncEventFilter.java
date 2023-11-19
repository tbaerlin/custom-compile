package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.events;

import com.google.gwt.event.shared.HandlerRegistration;
import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.iview.mmgwt.mmweb.client.events.EventBusRegistry;

/**
 * User: umaurer
 * Date: 14.10.13
 * Time: 14:51
 */
public class PmAsyncEventFilter {
    public static HandlerRegistration addFilteredHandler(final String handle, final PmAsyncHandler handler) {
        return EventBusRegistry.get().addHandler(PmAsyncEvent.getType(), new PmAsyncHandler() {
            @Override
            public void onAsync(PmAsyncEvent event) {
                if (event.getAsyncData().getHandle().equals(handle)) {
                    handler.onAsync(event);
                }
                else {
                    Firebug.debug("PmAsyncEventFilter: async event ignored for handle " + event.getAsyncData() + "! Listening for " + handle);
                }
            }
        });
    }
}