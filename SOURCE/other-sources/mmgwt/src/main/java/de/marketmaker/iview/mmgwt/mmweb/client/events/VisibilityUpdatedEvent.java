/*
 * VisibilityUpdatedEvent.java
 *
 * Created on 17.04.13 15:18
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.events;

import com.google.gwt.event.shared.GwtEvent;

/**
 * Indicates that the visibility of some kind of an object has been updated.
 * May be fired on the central event bus or on classes implementing HasVisibilityUpdatedHandlers.
 *
 * @see de.marketmaker.iview.mmgwt.mmweb.client.snippets.FlexSnippetsView#updateVisibility(de.marketmaker.iview.mmgwt.mmweb.client.snippets.SnippetView, boolean)
 * @see de.marketmaker.iview.mmgwt.mmweb.client.snippets.FlexSnippetsView.MuxPanel#updateVisibility(de.marketmaker.iview.mmgwt.mmweb.client.snippets.SnippetView, boolean)
 * @see de.marketmaker.iview.mmgwt.mmweb.client.view.ObjectTree#onVisibilityUpdated(VisibilityUpdatedEvent)
 * @see de.marketmaker.iview.mmgwt.mmweb.client.view.ObjectTreeModel#fireVisibilityUpdated(de.marketmaker.iview.mmgwt.mmweb.client.view.NavItemSpec)
 *
 * @author Markus Dick
 */
public class VisibilityUpdatedEvent<T> extends GwtEvent<VisibilityUpdatedHandler<T>> {
    private static GwtEvent.Type<VisibilityUpdatedHandler<?>> TYPE;

    public static Type<VisibilityUpdatedHandler<?>> getType() {
        if (TYPE == null) {
            TYPE = new GwtEvent.Type<VisibilityUpdatedHandler<?>>();
        }
        return TYPE;
    }

    final T target;
    final boolean visible;

    public VisibilityUpdatedEvent(T target, boolean visible) {
        this.target = target;
        this.visible = visible;
    }

    public T getTarget() {
        return this.target;
    }

    public boolean isVisible() {
        return this.visible;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public Type<VisibilityUpdatedHandler<T>> getAssociatedType() {
        return (Type) TYPE;
    }

    @Override
    protected void dispatch(VisibilityUpdatedHandler<T> visibilityUpdatedHandler) {
        visibilityUpdatedHandler.onVisibilityUpdated(this);
    }

    /**
     * Fires a value change event on all registered handlers in the handler
     * manager. If no such handlers exist, this method will do nothing.
     *
     * @param source the source of the handlers
     * @param target the target whose visibility has been updated
     */
    public static <T> void fire(HasVisibilityUpdatedHandlers<T> source, T target, boolean visible) {
        if (TYPE != null) {
            VisibilityUpdatedEvent<T> event = new VisibilityUpdatedEvent<T>(target, visible);
            source.fireEvent(event);
        }
    }
}
