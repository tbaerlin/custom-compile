/*
 * SpsProgressControlWidget.java
 *
 * Created on 24.08.2015 14:55
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine;

import com.google.gwt.dom.client.Style;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.SimplePanel;
import de.marketmaker.itools.gwtutil.client.widgets.IconImage;
import de.marketmaker.itools.gwtutil.client.widgets.ProgressBar;
import de.marketmaker.itools.gwtutil.client.widgets.Tooltip;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.widget.NoValidationPopup;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.async.AsyncData;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.async.AsyncStateResult;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.async.PmAsyncManager;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.events.PmAsyncEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.events.PmAsyncEventFilter;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.events.PmAsyncHandler;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;

/**
 * @author Markus Dick
 */
public class SpsProgressControlWidget extends SpsBoundWidget<Panel, SpsLeafProperty> implements NoValidationPopup {
    private final Panel rootWidget = new SimplePanel();

    private final HTML messageLabel = new HTML();
    private final ProgressBar progressBar = new ProgressBar();
    private final Label percentLabel = new Label();

    private HandlerRegistration asyncRegistration;
    private String currentAsyncHandle;

    public SpsProgressControlWidget() {
        final FlexTable flexTable = new FlexTable();
        this.rootWidget.add(flexTable);

        flexTable.setWidget(0, 0, this.messageLabel);
        flexTable.setWidget(1, 0, this.progressBar);
        flexTable.setWidget(2, 0, this.percentLabel);
        flexTable.getFlexCellFormatter().setHorizontalAlignment(2, 0, HasHorizontalAlignment.ALIGN_CENTER);

        this.progressBar.setVisible(false);
        this.messageLabel.getElement().getStyle().setVerticalAlign(Style.VerticalAlign.TOP);
    }

    @Override
    public void onPropertyChange() {
        final String asyncHandle = getBindFeature().getSpsProperty().getStringValue();

        if(StringUtil.hasText(this.currentAsyncHandle) && !StringUtil.equals(this.currentAsyncHandle, asyncHandle)) {
            PmAsyncManager.getInstance().unregisterHandle(this.currentAsyncHandle, true, "SpsProgressControlWidget.onPropertyChange");  // $NON-NLS$
        }

        this.currentAsyncHandle = asyncHandle;

        PmAsyncManager.getInstance().registerHandle(asyncHandle, new AsyncCallback<AsyncStateResult>() {
            @Override
            public void onFailure(Throwable caught) {
                messageLabel.setText(I18n.I.serverError() + (caught != null ? ": " + caught.getMessage() : ""));  // $NON-NLS$
            }

            @Override
            public void onSuccess(AsyncStateResult result) {
                if (!result.isFinished()) {
                    startProgress(asyncHandle);
                } else {
                    updateState(AsyncData.State.FINISHED, 100, null);
                }
            }
        });
    }

    private void startProgress(final String asyncHandle) {
        if(this.asyncRegistration != null) {
            this.asyncRegistration.removeHandler();
        }
        this.asyncRegistration = PmAsyncEventFilter.addFilteredHandler(asyncHandle, new PmAsyncHandler() {
            @Override
            public void onAsync(PmAsyncEvent event) {
                AsyncData asyncData = event.getAsyncData();
                updateState(asyncData.getState(), asyncData.getProgress(), asyncData.getMessage());
            }
        });
    }

    private void updateState(AsyncData.State asyncDataState, int progress, String message) {
        switch (asyncDataState) {
            case STARTED:
                this.messageLabel.setText(I18n.I.spsProgressControlProcessingStarted());
                setProgressVisible(true);
                setProgress(progress);
                break;
            case PROGRESS:
                this.messageLabel.setText(I18n.I.spsProgressControlProcessingProgress());
                setProgressVisible(true);
                setProgress(progress);
                break;
            case PAUSED:
                this.messageLabel.setText(I18n.I.spsProgressControlProcessingPaused());
                setProgressVisible(true);
                setProgress(progress);
                break;
            case FINISHED:
                setProgressVisible(false);
                setProgress(100);
                this.messageLabel.setHTML(IconImage.get("pm-box-checked").getSafeHtml()); // $NON-NLS$
                Tooltip.addQtip(this.messageLabel, I18n.I.spsProgressControlProcessingFinishedTooltip());
                break;
            case ERROR:
                setProgressVisible(false);
                setProgress(0);
                String icon = IconImage.get("pmSeverity-esvError").getHTML(); // $NON-NLS$
                if(StringUtil.hasText(message)) {
                    icon += " " + SafeHtmlUtils.htmlEscape(message);
                }
                this.messageLabel.setHTML(SafeHtmlUtils.fromSafeConstant(icon));
                Tooltip.addQtip(this.messageLabel, I18n.I.serverError());
                break;
            case PING:
            default:
                setProgressVisible(false);
                setProgress(0);
                this.messageLabel.setText(asyncDataState.name());
        }
    }

    private void setProgress(int progress) {
        this.progressBar.setProgress(progress);
        this.percentLabel.setText(progress + "%"); // $NON-NLS$
    }

    private void setProgressVisible(boolean visible) {
        this.progressBar.setVisible(visible);
        this.percentLabel.setVisible(visible);
    }

    @Override
    protected Panel createWidget() {
        this.rootWidget.setStyleName(getBaseStyle());
        return this.rootWidget;
    }

    @Override
    public void release() {
        if(this.asyncRegistration != null) {
            this.asyncRegistration.removeHandler();
        }
        if(StringUtil.hasText(this.currentAsyncHandle)) {
            PmAsyncManager.getInstance().unregisterHandle(this.currentAsyncHandle, false, "SpsProgressControlWidget.release");  // $NON-NLS$
        }
        super.release();
    }
}
