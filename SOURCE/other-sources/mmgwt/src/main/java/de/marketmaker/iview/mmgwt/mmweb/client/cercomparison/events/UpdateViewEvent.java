package de.marketmaker.iview.mmgwt.mmweb.client.cercomparison.events;

import com.google.gwt.event.shared.GwtEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.events.EventBusRegistry;

/**
 * @author umaurer
 */
public class UpdateViewEvent extends GwtEvent<UpdateViewHandler> {
    private static Type<UpdateViewHandler> TYPE;

    public static Type<UpdateViewHandler> getType() {
        if (TYPE == null) {
            TYPE = new Type<UpdateViewHandler>();
        }
        return TYPE;
    }

    public static void fire() {
        EventBusRegistry.get().fireEvent(new UpdateViewEvent());
    }

    public UpdateViewEvent() {
    }

    public Type<UpdateViewHandler> getAssociatedType() {
        return TYPE;
    }

    protected void dispatch(UpdateViewHandler handler) {
        handler.onUpdateView(this);
    }
}
