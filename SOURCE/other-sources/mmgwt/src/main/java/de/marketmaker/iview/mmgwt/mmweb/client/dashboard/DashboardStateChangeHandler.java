package de.marketmaker.iview.mmgwt.mmweb.client.dashboard;

import com.google.gwt.event.shared.EventHandler;

/**
 * Author: umaurer
 * Created: 03.06.15
 */
public interface DashboardStateChangeHandler extends EventHandler {
    void onStateChanged(DashboardStateChangeEvent event);
}
