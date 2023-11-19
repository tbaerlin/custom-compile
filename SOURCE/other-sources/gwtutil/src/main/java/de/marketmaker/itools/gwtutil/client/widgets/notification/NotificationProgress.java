package de.marketmaker.itools.gwtutil.client.widgets.notification;

import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;

/**
 * User: umaurer
 * Date: 25.06.13
 * Time: 11:20
 */
public class NotificationProgress extends Composite {
    private final SimplePanel panel = new SimplePanel();
    private final Label bar = new Label();

    public NotificationProgress() {
        this.panel.setStyleName("mm-noteBox-timer");
        this.bar.setStyleName("bar");
        this.panel.setWidget(this.bar);
        this.panel.setVisible(false);
        initWidget(this.panel);
    }

    /**
     * Set the progress as a value between 0 and 1. Double.NaN makes the widget invisible.
     * @param progress A value between 0 (no progress) and 1 (finished) or Double.NaN.
     */
    public void setProgress(double progress) {
        if (Double.isNaN(progress)) {
            this.panel.setVisible(false);
        }
        else {
            this.panel.setVisible(true);

            final double width =
                    progress <= 0d
                            ? 100d
                            : (progress >= 1d ? 0d : (100d - progress * 100d));
            this.bar.getElement().getStyle().setWidth(width, Style.Unit.PCT);
        }

    }
}
