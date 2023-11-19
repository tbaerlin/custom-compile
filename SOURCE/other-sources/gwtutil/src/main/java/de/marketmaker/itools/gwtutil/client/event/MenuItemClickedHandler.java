package de.marketmaker.itools.gwtutil.client.event;

import com.google.gwt.event.shared.EventHandler;

/**
 * Author: umaurer
 * Created: 24.08.15
 */
public interface MenuItemClickedHandler extends EventHandler {
    void onMenuItemClicked(MenuItemClickedEvent event);
}
