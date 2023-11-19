package de.marketmaker.itools.gwtutil.client.widgets.notification;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;
import de.marketmaker.itools.gwtutil.client.util.StringUtility;

/**
 * User: umaurer
 * Date: 20.06.13
 * Time: 14:24
 */
public class NotificationMessage {
    public enum State {
        DEFAULT, HIDDEN, VISIBLE, FORCE_HIDDEN, DELETED
    }

    private static int idCounter = 0;
    private final Integer id;
    private final SafeHtml header;
    private final Widget widget;
    private final HTML html;

    private double progress;
    private boolean cancellableProgress = false;

    private State state = State.DEFAULT;

    /**
     * Use {@link Notifications#add(SafeHtml, String) Notifications.I.add(...)} to create an instance of NotificationMessage
     * @param header
     * @param text
     */
    NotificationMessage(SafeHtml header, String text, double progress) {
        this.id = nextId();
        this.header = header;
        this.html = new HTML(StringUtility.toHtmlLines(text));
        this.widget = this.html;
        this.progress = progress;
    }

    /**
     * Use {@link Notifications#add(SafeHtml, SafeHtml) Notifications.I.add(...)} to create an instance of NotificationMessage
     * @param header
     * @param text
     */
    NotificationMessage(SafeHtml header, SafeHtml text, double progress) {
        this.id = nextId();
        this.header = header;
        this.html = new HTML(text);
        this.widget = this.html;
        this.progress = progress;
    }

    /**
     * Use {@link Notifications#add(SafeHtml, Widget) Notifications.I.add(...)} to create an instance of NotificationMessage
     * @param header
     * @param widget
     */
    NotificationMessage(SafeHtml header, Widget widget, double progress) {
        this.id = nextId();
        this.header = header;
        this.html = null;
        this.widget = widget;
        this.progress = progress;
    }

    private static int nextId() {
        return idCounter++;
    }

    public SafeHtml getHeader() {
        return header;
    }

    public void setText(String text) {
        if (this.html == null) {
            throw new IllegalStateException("setText(String) is not allowed for NotificationMessage(String, Widget)");
        }
        this.html.setHTML(StringUtility.toHtmlLines(text));
    }

    public void setHTML(SafeHtml text) {
        if (this.html == null) {
            throw new IllegalStateException("setHTML(SafeHtml) is not allowed for NotificationMessage(String, Widget)");
        }
        this.html.setHTML(text);
    }

    public Widget getWidget() {
        return widget;
    }

    public Integer getId() {
        return id;
    }

    public State getState() {
        return state;
    }

    void setState(State state) {
        this.state = state;
    }

    NotificationMessage withState(State state) {
        setState(state);
        return this;
    }

    public void requestState(State state) {
        Notifications.I.fireState(this, state);
    }

    public void requestStateDelayed(State state, int delaySeconds) {
        Notifications.I.fireStateDelayed(this, state, delaySeconds);
    }

    public double getProgress() {
        return progress;
    }

    public void setProgress(double progress) {
        this.progress = progress;
        Notifications.I.fireState(this, State.DEFAULT);
    }

    public NotificationMessage withProgress(double progress) {
        setProgress(progress);
        return this;
    }

    public NotificationMessage withCancellableProgress() {
        this.cancellableProgress = true;
        return this;
    }

    public boolean isCancellableProgress() {
        return cancellableProgress;
    }
}
