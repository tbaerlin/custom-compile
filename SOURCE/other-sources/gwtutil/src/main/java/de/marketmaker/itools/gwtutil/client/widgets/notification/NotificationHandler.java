package de.marketmaker.itools.gwtutil.client.widgets.notification;

import com.google.gwt.event.shared.EventHandler;

/**
 * User: umaurer
 * Date: 20.06.13
 * Time: 14:20
 */
public interface NotificationHandler extends EventHandler {
    void onNotification(NotificationEvent event);
}
