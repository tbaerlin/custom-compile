package de.marketmaker.itools.gwtutil.client.widgets.notification;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.Widget;

/**
 * User: umaurer
 * Date: 20.06.13
 * Time: 14:02
 */
public class Notifications implements HasNotificationHandlers {
    public static final Notifications I = new Notifications();
    private final HandlerManager handlerManager = new HandlerManager(this);

    @Override
    public HandlerRegistration addNotificationHandler(NotificationHandler handler) {
        return this.handlerManager.addHandler(NotificationEvent.getType(), handler);
    }

    @Override
    public void fireEvent(GwtEvent<?> event) {
        this.handlerManager.fireEvent(event);
    }

    public static NotificationMessage add(String header, String text) {
        return I.add(new NotificationMessage(SafeHtmlUtils.fromString(header), text, Double.NaN));
    }

    public static NotificationMessage addHidden(String header, String text) {
        return I.add(new NotificationMessage(SafeHtmlUtils.fromString(header), text, Double.NaN).withState(NotificationMessage.State.HIDDEN));
    }

    public static NotificationMessage add(String header, Widget widget) {
        return I.add(new NotificationMessage(SafeHtmlUtils.fromString(header), widget, Double.NaN));
    }

    public static NotificationMessage addHidden(String header, Widget widget) {
        return I.add(new NotificationMessage(SafeHtmlUtils.fromString(header), widget, Double.NaN).withState(NotificationMessage.State.HIDDEN));
    }

    public static NotificationMessage add(SafeHtml header, SafeHtml safeHtml) {
        return I.add(new NotificationMessage(header, safeHtml, Double.NaN));
    }

    public static NotificationMessage add(String header, SafeHtml safeHtml) {
        return I.add(new NotificationMessage(SafeHtmlUtils.fromString(header), safeHtml, Double.NaN));
    }

    public static NotificationMessage addHidden(String header, SafeHtml safeHtml) {
        return I.add(new NotificationMessage(SafeHtmlUtils.fromString(header), safeHtml, Double.NaN).withState(NotificationMessage.State.HIDDEN));
    }

    public static NotificationMessage add(String header, String text, double progress) {
        return I.add(new NotificationMessage(SafeHtmlUtils.fromString(header), text, progress));
    }

    public static NotificationMessage addHidden(String header, String text, double progress) {
        return I.add(new NotificationMessage(SafeHtmlUtils.fromString(header), text, progress).withState(NotificationMessage.State.HIDDEN));
    }

    public static NotificationMessage add(String header, Widget widget, double progress) {
        return I.add(new NotificationMessage(SafeHtmlUtils.fromString(header), widget, progress));
    }

    public static NotificationMessage addHidden(String header, Widget widget, double progress, boolean cancellableProgress) {
        if(cancellableProgress) {
            return I.add(new NotificationMessage(SafeHtmlUtils.fromString(header), widget, progress).withState(NotificationMessage.State.HIDDEN).withCancellableProgress());
        }
        return add(header, widget, progress);
    }

    public static NotificationMessage addHidden(String header, Widget widget, double progress) {
        return I.add(new NotificationMessage(SafeHtmlUtils.fromString(header), widget, progress).withState(NotificationMessage.State.HIDDEN));
    }

    public static NotificationMessage add(String header, SafeHtml safeHtml, double progress) {
        return I.add(new NotificationMessage(SafeHtmlUtils.fromString(header), safeHtml, progress));
    }

    public static NotificationMessage addHidden(String header, SafeHtml safeHtml, double progress) {
        return I.add(new NotificationMessage(SafeHtmlUtils.fromString(header), safeHtml, progress).withState(NotificationMessage.State.HIDDEN));
    }

    private NotificationMessage add(final NotificationMessage message) {
        NotificationEvent.fire(this, message, message.getState());
        return message;
    }

    void fireState(NotificationMessage message, NotificationMessage.State state) {
        if (message.getState() == NotificationMessage.State.DELETED) {
            return;
        }
        NotificationEvent.fire(this, message, state);
    }

    void fireStateDelayed(final NotificationMessage message, final NotificationMessage.State state, int delaySeconds) {
        assert(delaySeconds > 0);
        if (message.getState() == NotificationMessage.State.DELETED) {
            return;
        }
        Scheduler.get().scheduleFixedDelay(new Scheduler.RepeatingCommand() {
            @Override
            public boolean execute() {
                fireState(message, state);
                return false;
            }
        }, delaySeconds * 1000);
    }
}