package de.marketmaker.itools.gwtutil.client.widgets.chart;

import com.google.gwt.event.shared.GwtEvent;

/**
 * User: umaurer
 * Date: 10.06.13
 * Time: 12:59
 */
public class EntryClickEvent<T, I> extends GwtEvent<EntryClickHandler<T, I>> {
    private static Type<EntryClickHandler<?, ?>> TYPE;

    public static <T, I> void fire(HasEntryClickHandlers<T, I> source, T clickedEntry, I entryIndex) {
        if (TYPE != null) {
            final EntryClickEvent<T, I> event = new EntryClickEvent<>(clickedEntry, entryIndex);
            source.fireEvent(event);
        }
    }

    public static Type<EntryClickHandler<?, ?>> getType() {
        if (TYPE == null) {
            TYPE = new Type<>();
        }
        return TYPE;
    }

    private final T clickedEntry;
    private final I index;

    public EntryClickEvent(T clickedEntry, I index) {
        this.clickedEntry = clickedEntry;
        this.index = index;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Type<EntryClickHandler<T, I>> getAssociatedType() {
        return (Type) TYPE;
    }

    public T getClickedEntry() {
        return this.clickedEntry;
    }

    public I getIndex() {
        return index;
    }

    @Override
    protected void dispatch(EntryClickHandler<T, I> handler) {
        handler.onEntryClicked(this);
    }
}
