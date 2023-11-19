package de.marketmaker.itools.gwtutil.client.event;

import com.google.gwt.event.shared.GwtEvent;

/**
 * @author Ulrich Maurer
 *         Date: 01.10.12
 */
public class ItemRemovedEvent<T> extends GwtEvent<ItemRemovedHandler<T>> {
    private static Type<ItemRemovedHandler<?>> TYPE;

    public static <T> void fire(HasItemRemovedHandlers<T> source, T target) {
        if (TYPE != null) {
            ItemRemovedEvent<T> event = new ItemRemovedEvent<T>(target);
            source.fireEvent(event);
        }
    }

    public static Type<ItemRemovedHandler<?>> getType() {
        return TYPE != null ? TYPE : (TYPE = new Type<ItemRemovedHandler<?>>());
    }

    private final T target;


    protected ItemRemovedEvent(T target) {
        this.target = target;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Type<ItemRemovedHandler<T>> getAssociatedType() {
        return (Type) TYPE;
    }

    public T getTarget() {
        return target;
    }

    @Override
    protected void dispatch(ItemRemovedHandler<T> handler) {
        handler.onItemRemoved(this);
    }
}

