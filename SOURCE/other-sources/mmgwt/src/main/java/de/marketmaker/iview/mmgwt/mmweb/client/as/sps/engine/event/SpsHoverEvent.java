package de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.event;

import com.google.gwt.event.shared.GwtEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsWidget;

/**
 * Author: umaurer
 * Created: 09.12.14
 */
public class SpsHoverEvent extends GwtEvent<SpsHoverHandler> {
    private static Type<SpsHoverHandler> TYPE;
    private final SpsWidget widgetSource;
    private final String bindKeys;
    private final boolean selected;

    public SpsHoverEvent(SpsWidget widgetSource, String bindKeys, boolean selected) {
        this.widgetSource = widgetSource;
        this.bindKeys = bindKeys;
        this.selected = selected;
    }

    public static Type<SpsHoverHandler> getType() {
        if (TYPE == null) {
            TYPE = new Type<>();
        }
        return TYPE;
    }

    @Override
    public Type<SpsHoverHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(SpsHoverHandler handler) {
        handler.onHover(this);
    }

    public SpsWidget getWidgetSource() {
        return widgetSource;
    }

    public String getBindKeys() {
        return bindKeys;
    }

    public boolean isSelected() {
        return selected;
    }
}
