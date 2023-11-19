package de.marketmaker.itools.gwtutil.client.event;

import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.HasHandlers;

/**
 * Author: umaurer
 * Created: 24.08.15
 */
public interface HasMenuItemClickedHandlers extends HasHandlers {
    HandlerRegistration addMenuItemClickedHandler(MenuItemClickedHandler handler);
}
