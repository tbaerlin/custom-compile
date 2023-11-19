package de.marketmaker.itools.gwtutil.client.event;

import com.google.gwt.dom.client.BrowserEvents;
import com.google.gwt.event.dom.client.DomEvent;
import com.google.gwt.user.client.ui.ValueBox;

/**
 * Author: umaurer
 * Created: 18.03.14
 */
public class PasteEvent extends DomEvent<PasteHandler> {
    private static Type<PasteHandler> TYPE;

    public static <T> void fire(ValueBox valueBox) {
        if (TYPE != null) {
            PasteEvent event = new PasteEvent();
            valueBox.fireEvent(event);
        }
    }

    public static Type<PasteHandler> getType() {
        return TYPE != null ? TYPE : (TYPE = new Type<>("paste", new PasteEvent()));
    }

    protected PasteEvent() {
    }

    @Override
    public Type<PasteHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(PasteHandler handler) {
        handler.onPaste(this);
    }
}
