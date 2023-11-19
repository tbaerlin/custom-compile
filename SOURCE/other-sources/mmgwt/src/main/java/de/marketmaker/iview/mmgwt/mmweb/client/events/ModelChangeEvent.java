/*
 * ModelChangeEvent.java
 *
 * Created on 13.06.13 10:09
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.events;

import com.google.gwt.event.shared.GwtEvent;

/**
 * @author Markus Dick
 */
public class ModelChangeEvent<T> extends GwtEvent<ModelChangeHandler<T>> {
    private static Type<ModelChangeHandler<?>> TYPE;
    private final T model;

    public static Type<ModelChangeHandler<?>> getType() {
        if (TYPE == null) {
            TYPE = new Type<ModelChangeHandler<?>>();
        }
        return TYPE;
    }

    public ModelChangeEvent(T model) {
        this.model = model;
    }

    public T getModel() {
        return model;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public Type<ModelChangeHandler<T>> getAssociatedType() {
        return (Type) TYPE;
    }

    @Override
    protected void dispatch(ModelChangeHandler<T> handler) {
        handler.onModelChange(this);
    }

    /**
     * Fires a value change event on all registered handlers in the handler
     * manager. If no such handlers exist, this method will do nothing.
     *
     * @param source the source of the handlers
     * @param model the model
     */
    public static <T> void fire(HasModelChangeHandlers<T> source, T model) {
        if (TYPE != null) {
            ModelChangeEvent<T> event = new ModelChangeEvent<T>(model);
            source.fireEvent(event);
        }
    }

    /**
     * Fires a value change event on all registered handlers in the handler
     * manager. If no such handlers exist, this method will do nothing.
     *
     * @param source the source of the handlers, which is also the model
     */
    @SuppressWarnings({"unchecked"})
    public static <T> void fire(HasModelChangeHandlers<T> source) {
        if (TYPE != null) {
            ModelChangeEvent<T> event = new ModelChangeEvent<T>((T)source);
            source.fireEvent(event);
        }
    }
}
