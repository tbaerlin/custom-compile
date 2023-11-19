package de.marketmaker.itools.gwtutil.client.widgets.floating;

import com.google.gwt.event.shared.GwtEvent;

/**
 * User: umaurer
 * Date: 01.10.13
 * Time: 14:58
 */
public class FloatingEvent extends GwtEvent<FloatingHandler> {
    private static Type<FloatingHandler> TYPE;

    public static void fire(HasFloatingHandlers source, int delta) {
        if (TYPE != null) {
            FloatingEvent event = new FloatingEvent(delta);
            source.fireEvent(event);
        }
    }

    public static Type<FloatingHandler> getType() {
        return TYPE != null ? TYPE : (TYPE = new Type<FloatingHandler>());
    }

    int delta;

    protected FloatingEvent(int delta) {
        this.delta = delta;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Type<FloatingHandler> getAssociatedType() {
        return (Type) TYPE;
    }

    public int getDelta() {
        return delta;
    }

    @Override
    protected void dispatch(FloatingHandler handler) {
        handler.onFloating(this);
    }
}
