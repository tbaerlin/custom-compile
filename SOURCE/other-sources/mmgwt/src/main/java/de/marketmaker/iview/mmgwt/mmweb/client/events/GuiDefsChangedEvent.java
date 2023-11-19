package de.marketmaker.iview.mmgwt.mmweb.client.events;

import com.google.gwt.event.shared.GwtEvent;


public class GuiDefsChangedEvent extends GwtEvent<GuiDefsChangedHandler> {
    private static Type<GuiDefsChangedHandler> TYPE;

    public static Type<GuiDefsChangedHandler> getType() {
        if (TYPE == null) {
            TYPE = new Type<GuiDefsChangedHandler>();
        }
        return TYPE;
    }

    public Type<GuiDefsChangedHandler> getAssociatedType() {
        return TYPE;
    }

    protected void dispatch(GuiDefsChangedHandler handler) {
        handler.onGuidefsChange(this);
    }
}
