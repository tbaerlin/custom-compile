package de.marketmaker.itools.gwtutil.client.widgets.notification;

import com.google.gwt.event.shared.GwtEvent;

/**
 * User: umaurer
 * Date: 20.06.13
 * Time: 14:20
 */
public class NotificationEvent extends GwtEvent<NotificationHandler> {
    private static Type<NotificationHandler> TYPE;

    public static <T> void fire(HasNotificationHandlers source, NotificationMessage message, NotificationMessage.State requestedState) {
        if (TYPE != null) {
            NotificationEvent event = new NotificationEvent(message, requestedState);
            source.fireEvent(event);
        }
    }

    public static Type<NotificationHandler> getType() {
        if (TYPE == null) {
            TYPE = new Type<NotificationHandler>();
        }
        return TYPE;
    }

    private final NotificationMessage message;
    private final NotificationMessage.State requestedState;

    public NotificationEvent(NotificationMessage message, NotificationMessage.State requestedState) {
        this.message = message;
        this.requestedState = requestedState;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Type<NotificationHandler> getAssociatedType() {
        return (Type) TYPE;
    }

    public NotificationMessage getMessage() {
        return this.message;
    }

    public NotificationMessage.State getRequestedState() {
        return requestedState;
    }

    @Override
    protected void dispatch(NotificationHandler handler) {
        handler.onNotification(this);
    }
}
