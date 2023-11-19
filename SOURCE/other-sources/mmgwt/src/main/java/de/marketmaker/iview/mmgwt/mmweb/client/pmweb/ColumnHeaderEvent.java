/*
 * ColumnHeaderEvent.java
 *
 * Created on 18.03.2015 09:30
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.pmweb;

import com.google.gwt.dom.client.Element;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.user.client.Event;

/**
 * @author mdick
 */
public class ColumnHeaderEvent extends GwtEvent<ColumnHeaderHandler> {
    public enum What {MOUSE_OVER, MOUSE_OUT, POPUP_HOOK_CLICKED, SORT_CLICKED}

    private static GwtEvent.Type<ColumnHeaderHandler> TYPE;

    private final What what;
    private final Element element;
    private final int columnIndex;
    private final Event causeEvent;

    private ColumnHeaderEvent(Element thElement, int columnIndex, What what, Event causeEvent) {
        this.element = thElement;
        this.columnIndex = columnIndex;
        this.what = what;
        this.causeEvent = causeEvent;
    }

    public Element getElement() {
        return this.element;
    }

    public int getColumnIndex() {
        return this.columnIndex;
    }

    public What getWhat() {
        return what;
    }

    public Event getCauseEvent() {
        return this.causeEvent;
    }

    public static GwtEvent.Type<ColumnHeaderHandler> getType() {
        if (TYPE == null) {
            TYPE = new GwtEvent.Type<>();
        }

        return TYPE;
    }

    public final GwtEvent.Type<ColumnHeaderHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(ColumnHeaderHandler handler) {
        handler.onColumnHeader(this);
    }

    public static <T> void fire(HasColumnHeaderHandlers source, Element element, int columnIndex, What what, Event causeEvent) {
        if (TYPE != null) {
            ColumnHeaderEvent event = new ColumnHeaderEvent(element, columnIndex, what, causeEvent);
            source.fireEvent(event);
        }
    }
}
