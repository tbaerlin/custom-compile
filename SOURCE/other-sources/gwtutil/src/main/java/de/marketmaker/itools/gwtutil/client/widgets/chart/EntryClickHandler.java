package de.marketmaker.itools.gwtutil.client.widgets.chart;

import com.google.gwt.event.shared.EventHandler;

/**
 * User: umaurer
 * Date: 10.06.13
 * Time: 12:57
 */
public interface EntryClickHandler<T, I> extends EventHandler {
    void onEntryClicked(EntryClickEvent<T, I> event);
}
