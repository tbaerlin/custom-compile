package de.marketmaker.itools.gwtutil.client.widgets.floating;

import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.HasHandlers;

/**
 * User: umaurer
 * Date: 01.10.13
 * Time: 14:59
 */
public interface HasFloatingHandlers extends HasHandlers {
    HandlerRegistration addFloatingHandler(FloatingHandler handler);
}
