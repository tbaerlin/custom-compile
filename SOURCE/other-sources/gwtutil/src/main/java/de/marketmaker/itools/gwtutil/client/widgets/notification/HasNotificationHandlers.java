package de.marketmaker.itools.gwtutil.client.widgets.notification;

import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.HasHandlers;

/**
 * User: umaurer
 * Date: 20.06.13
 * Time: 14:22
 */
public interface HasNotificationHandlers extends HasHandlers {
    HandlerRegistration addNotificationHandler(NotificationHandler handler);
}
