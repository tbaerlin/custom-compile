package de.marketmaker.itools.gwtutil.client.widgets.chart;

import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.HasHandlers;

/**
 * User: umaurer
 * Date: 10.06.13
 * Time: 13:03
 */
public interface HasEntryClickHandlers<T, I> extends HasHandlers {
    HandlerRegistration addEntryClickHandler(EntryClickHandler<T, I> handler);
}
