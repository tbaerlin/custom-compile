package de.marketmaker.itools.gwtutil.client.event;

import com.google.gwt.event.shared.GwtEvent;

/**
 * @author Ulrich Maurer
 *         Date: 01.10.12
 */
public class ItemAddedEvent<T> extends GwtEvent<ItemAddedHandler<T>> {
    private static Type<ItemAddedHandler<?>> TYPE;

    public static <T> void fire(HasItemAddedHandlers<T> source, T target) {
        if (TYPE != null) {
            ItemAddedEvent<T> event = new ItemAddedEvent<T>(target);
            source.fireEvent(event);
        }
    }

    public static Type<ItemAddedHandler<?>> getType() {
        return TYPE != null ? TYPE : (TYPE = new Type<ItemAddedHandler<?>>());
    }

    private final T target;


    protected ItemAddedEvent(T target) {
        this.target = target;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Type<ItemAddedHandler<T>> getAssociatedType() {
        return (Type) TYPE;
    }

    public T getTarget() {
        return target;
    }

    @Override
    protected void dispatch(ItemAddedHandler<T> handler) {
        handler.onItemAdded(this);
    }
}

