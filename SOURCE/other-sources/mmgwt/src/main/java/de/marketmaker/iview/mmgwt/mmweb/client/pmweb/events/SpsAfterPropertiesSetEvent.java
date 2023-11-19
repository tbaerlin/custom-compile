package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.events;

import com.google.gwt.event.shared.GwtEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.events.EventBusRegistry;

/**
 * @author umaurer
 */
public class SpsAfterPropertiesSetEvent extends GwtEvent<SpsAfterPropertiesSetHandler> {
    private static Type<SpsAfterPropertiesSetHandler> TYPE;

    public static Type<SpsAfterPropertiesSetHandler> getType() {
        if (TYPE == null) {
            TYPE = new Type<>();
        }
        return TYPE;
    }

    public static void fireAndRemoveHandlers() {
        EventBusRegistry.get().fireEvent(new SpsAfterPropertiesSetEvent());
    }

    public SpsAfterPropertiesSetEvent() {
    }

    @Override
    public Type<SpsAfterPropertiesSetHandler> getAssociatedType() {
        return getType();
    }

    @Override
    protected void dispatch(SpsAfterPropertiesSetHandler handler) {
        try {
            handler.afterPropertiesSet();
        }
        finally {
            EventBusRegistry.get().removeHandler(getType(), handler);
        }
    }
}
