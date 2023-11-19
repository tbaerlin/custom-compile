package de.marketmaker.itools.gwtutil.client.event;

import com.google.gwt.event.shared.EventHandler;

/**
 * @author Ulrich Maurer
 *         Date: 01.10.12
 */
public interface ItemRemovedHandler<T> extends EventHandler {
    void onItemRemoved(ItemRemovedEvent<T> event);
}
