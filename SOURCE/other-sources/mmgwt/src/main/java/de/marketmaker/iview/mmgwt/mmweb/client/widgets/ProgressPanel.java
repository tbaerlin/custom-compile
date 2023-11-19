package de.marketmaker.iview.mmgwt.mmweb.client.widgets;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import de.marketmaker.itools.gwtutil.client.widgets.Button;
import de.marketmaker.itools.gwtutil.client.widgets.ProgressBar;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;

/**
 * User: umaurer
 * Date: 25.10.13
 * Time: 16:03
 */
public class ProgressPanel extends Composite {
    private final Label labelProgress;
    private final ProgressBar progressBar;
    private final Label labelPercent;

    private final Button btnCancel;
    private final Button btnToBackground;
    private final FlowPanel panel;

    public ProgressPanel(boolean showProgressPanelBackgroundButton, boolean showProgressPanelCancelButton) {
        this.panel = new FlowPanel();
        this.panel.setStyleName("centerPanel");

        final FlexTable table = new FlexTable();
        table.setStyleName("progressTable");
        final FlexTable.FlexCellFormatter formatter = table.getFlexCellFormatter();
        this.labelProgress = new Label(I18n.I.pmEvaluationStart());
        table.setWidget(0, 0, this.labelProgress);

        this.progressBar = new ProgressBar();
        table.setWidget(1, 0, this.progressBar);
        formatter.setColSpan(1, 0, 2);

        this.labelPercent = new Label();
        table.setWidget(2, 0, this.labelPercent);
        formatter.setColSpan(2, 0, 2);
        formatter.setHorizontalAlignment(2, 0, HasHorizontalAlignment.ALIGN_CENTER);

        this.panel.add(table);

        this.btnCancel = Button.text(I18n.I.cancel()).build();
        this.btnCancel.addStyleName("mm-bottomLeft");
        if (showProgressPanelCancelButton) {
            this.panel.add(this.btnCancel);
        }

        this.btnToBackground = Button.text(I18n.I.pmEvaluateLater()).build();
        this.btnToBackground.addStyleName("mm-bottomRight");
        if (showProgressPanelBackgroundButton) {
            this.panel.add(this.btnToBackground);
        }

        final SimplePanel fitPanel = new SimplePanel(this.panel);
        fitPanel.setStyleName("as-progressPanel");
        initWidget(fitPanel);
    }

    public HandlerRegistration addCancelClickHandler(ClickHandler handler) {
        return this.btnCancel.addClickHandler(handler);
    }

    public HandlerRegistration addToBackgroundClickHandler(ClickHandler handler) {
        return this.btnToBackground.addClickHandler(handler);
    }

    public void setProgress(String progressText) {
        this.labelProgress.setText(progressText);
        this.labelPercent.setText("");
    }

    public void setProgress(String progressText, int progress) {
        if (progress < 0) {
            progress = 0;
        }
        if (progress > 100) {
            progress = 100;
        }
        this.labelProgress.setText(progressText);
        this.progressBar.setProgress(progress);
        this.labelPercent.setText(progress + "%");
    }

    public void showSentToBackground() {
        this.panel.remove(this.btnCancel);
        this.panel.remove(this.btnToBackground);
        final HTML html = new HTML("Für spätere Ansicht registriert."); // $NON-NLS$
        html.setStyleName("mm-bottomRight");
        this.panel.add(html);
    }
}
