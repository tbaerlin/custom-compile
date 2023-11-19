package de.marketmaker.itools.gwtutil.client.event;

import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.HasHandlers;

/**
 * @author Ulrich Maurer
 *         Date: 01.10.12
 */
public interface HasItemAddedHandlers<T> extends HasHandlers {
    HandlerRegistration addItemAddedHandler(ItemAddedHandler<T> handler);
}
