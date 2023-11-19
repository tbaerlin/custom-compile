package de.marketmaker.itools.gwtutil.client.widgets;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;

/**
 * User: umaurer
 * Date: 25.10.13
 * Time: 16:15
 */
public class ProgressBar extends Composite {
    private final Label bar;

    public ProgressBar() {
        this.bar = new Label();
        this.bar.setStyleName("bar");
        final SimplePanel panel = new SimplePanel(bar);
        panel.setStyleName("mm-progressBar");
        initWidget(panel);
    }

    public void setProgress(int progress) {
        if (progress < 0) {
            progress = 0;
        }
        if (progress > 100) {
            progress = 100;
        }
        this.bar.setWidth(progress + "%");
    }
}
