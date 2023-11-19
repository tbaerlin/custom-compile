package de.marketmaker.iview.mmgwt.mmweb.client.events;


import com.google.gwt.event.shared.GwtEvent;

/**
 * Author: umaurer
 * Created: 19.10.15
 */
public class PushActivationEvent extends GwtEvent<PushActivationHandler> {
    private static Type<PushActivationHandler> TYPE;

    public static Type<PushActivationHandler> getType() {
        if (TYPE == null) {
            TYPE = new Type<PushActivationHandler>();
        }
        return TYPE;
    }

    private final boolean active;

    public PushActivationEvent(boolean active) {
        this.active = active;
    }

    public Type<PushActivationHandler> getAssociatedType() {
        return TYPE;
    }

    protected void dispatch(PushActivationHandler handler) {
        handler.onPushActivated(this);
    }

    public boolean isActive() {
        return active;
    }
}
