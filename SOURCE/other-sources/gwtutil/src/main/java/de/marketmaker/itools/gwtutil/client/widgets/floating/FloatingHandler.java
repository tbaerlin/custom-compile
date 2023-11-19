package de.marketmaker.itools.gwtutil.client.widgets.floating;

import com.google.gwt.event.shared.EventHandler;

/**
 * User: umaurer
 * Date: 01.10.13
 * Time: 14:58
 */
public interface FloatingHandler extends EventHandler {
    void onFloating(FloatingEvent event);
}
