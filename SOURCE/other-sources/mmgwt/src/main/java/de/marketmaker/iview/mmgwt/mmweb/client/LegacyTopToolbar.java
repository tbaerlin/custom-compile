/*
 * LegacyTopToolbar.java
 *
 * Created on 06.05.2008 15:33:37
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;

import de.marketmaker.itools.gwtutil.client.widgets.ImageButton;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.events.ConfigChangedEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.events.EventBusRegistry;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PendingRequestsEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PushActivationEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DebugUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.Dialog;
import de.marketmaker.iview.mmgwt.mmweb.client.util.GuiUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.LogWindow;
import de.marketmaker.iview.mmgwt.mmweb.client.util.PdfOptionSpec;
import de.marketmaker.iview.mmgwt.mmweb.client.util.PlaceUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.widgets.PdfWidget;
import de.marketmaker.iview.mmgwt.mmweb.client.widgets.SecretActivationLabel;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
@SuppressWarnings("UnusedParameters")
public class LegacyTopToolbar implements TopToolbar {

    interface ToolbarUiBinder extends UiBinder<Panel, LegacyTopToolbar> {
    }

    private static ToolbarUiBinder uiBinder = GWT.create(ToolbarUiBinder.class);

    @UiField
    protected Panel toolbarPanel;

    @UiField
    protected Panel gotoStartPanel;

    @UiField(provided = true)
    protected final ImageButton gotoStartButton = GuiUtil.createImageButton("mm-home-icon", // $NON-NLS-0$
            null, null, I18n.I.toStartpage());

    @UiField
    protected Panel refreshPanel;

    @UiField(provided = true)
    protected final ImageButton refreshButton = GuiUtil.createImageButton("mm-reload-icon", null, "mm-reload-icon reload-active", I18n.I.doUpdate());  // $NON-NLS$

    @UiField
    protected Panel setStartPanel;

    @UiField(provided = true)
    protected final ImageButton setStartButton = GuiUtil.createImageButton("page-to-home-icon", null, null, I18n.I.currentPageAsStartpage());  // $NON-NLS-0$

    @UiField
    protected Panel searchBoxPanel;

    @UiField
    protected Panel limitsPanel;

    @UiField(provided = true)
    protected final ImageButton limitsButton = GuiUtil.createImageButton("mm-limits-icon", null, "mm-limits-icon limit-active", I18n.I.limit());  // $NON-NLS-0$ $NON-NLS-1$

    @UiField
    protected Panel pushPanel;

    @UiField(provided = true)
    protected ImageButton pushButton = GuiUtil.createImageButton("push-inactive", null, "push-active", I18n.I.push());  // $NON-NLS-0$ $NON-NLS-1$
    private boolean pushActive = false;

    @UiField
    protected Panel printPanel;

    @UiField(provided = true)
    protected final ImageButton printButton = GuiUtil.createImageButton("mm-print-icon",  //$NON-NLS-0$
            "mm-print-icon disabled", null, I18n.I.print());  //$NON-NLS$

    @UiField
    protected Panel pdfPanel;

    @UiField(provided = true)
    protected PdfWidget pdfButton = new PdfWidget(GuiUtil.createImageButton("x-tbar-pdf", "x-tbar-pdf disabled", "x-tbar-pdf-with-options", I18n.I.exportAsPdf()));  // $NON-NLS$

    @UiField
    protected Panel savePanel;

    @UiField(provided = true)
    protected final ImageButton saveButton = GuiUtil.createImageButton("mm-save-icon", null, "mm-save-icon pending", I18n.I.saveSettings());  // $NON-NLS$;

    @UiField
    protected Panel logoutPanel;

    @UiField(provided = true)
    protected final ImageButton logoutButton = GuiUtil.createImageButton("mm-logout-icon", // $NON-NLS-0$
            null, null, I18n.I.logout());

    @UiField
    protected Panel secretActivationPanel;

    @UiField
    protected SecretActivationLabel secretActivationLabel;

    private final ToolbarConfiguration configuration;

    protected LegacyTopToolbar(ToolbarConfiguration config) {
        this.configuration = config;
        initView();
        initHandlers();
    }

    private void initView() {
        uiBinder.createAndBindUi(this);

        if (configuration.withHomeButtons) {
            gotoStartPanel.setVisible(true);
            setStartPanel.setVisible(true);
        }

        if (configuration.withRefreshButton) {
            refreshPanel.setVisible(true);
        }

        if (configuration.withSearchButton) {
            searchBoxPanel.setVisible(true);
        }

        if (configuration.withLimitsButton) {
            limitsPanel.setVisible(true);
        }

        if (configuration.withPushButton) {
            this.pushPanel.setVisible(true);
            this.pushButton.addClickHandler(event -> EventBusRegistry.get().fireEvent(new PushActivationEvent(!this.pushActive)));
        }

        if (configuration.withPrintButton) {
            printPanel.setVisible(true);
        }

        if (configuration.withPdfButton) {
            pdfPanel.setVisible(true);
        }

        if (configuration.withSaveButton) {
            savePanel.setVisible(true);
        }

        if (configuration.withLogoutButton) {
            logoutPanel.setVisible(true);
        }

        if (configuration.withSecretActivationLabel) {
            secretActivationPanel.setVisible(true);
            configureSecretActivationLabel();
        }
    }

    private void initHandlers() {
        if (configuration.withSaveButton) {
            EventBusRegistry.get().addHandler(ConfigChangedEvent.getType(), this);
        }

        if (configuration.withRefreshButton) {
            EventBusRegistry.get().addHandler(PendingRequestsEvent.getType(), this);
        }

        if (configuration.withPushButton) {
            EventBusRegistry.get().addHandler(PushActivationEvent.getType(), this);
        }
    }

    @Override
    public Widget asWidget() {
        return this.toolbarPanel;
    }

    @Override
    public void onPendingRequestsUpdate(PendingRequestsEvent event) {
        if (configuration.withRefreshButton) {
            this.refreshButton.setActive(event.getNumPending() != 0);
        }
    }

    @Override
    public void ackSave() {
        if (configuration.withSaveButton) {
            this.saveButton.setActive(false);
        }
    }

    @Override
    public void onConfigChange(ConfigChangedEvent event) {
        if (configuration.withSaveButton) {
            this.saveButton.setActive(true);
        }
    }

    @Override
    public void setPrintButtonEnabled(boolean enabled) {
        if (configuration.withPrintButton) {
            this.printButton.setEnabled(enabled);
        }
    }

    @Override
    public void updatePrintButtonEnabledState() {
        setPrintButtonEnabled(AbstractMainController.INSTANCE.isPrintable());
    }

    @Override
    public void updatePdfButtonState() {
        if (configuration.withPdfButton) {
            final PdfOptionSpec spec = AbstractMainController.INSTANCE.getPdfOptionSpec();
            setPdfButtonState(spec == null ? PdfWidget.PdfButtonState.DISABLED :
                    (spec.isWithOptions() ? PdfWidget.PdfButtonState.WITH_OPTIONS : PdfWidget.PdfButtonState.ENABLED));
        }
    }

    @Override
    public void updateLimitsIcon(String pending) {
        if (configuration.withLimitsButton) {
            this.limitsButton.setActive(pending != null);
        }
    }

    @Override
    public void onPushActivated(PushActivationEvent event) {
        this.pushActive = event.isActive();
        this.pushButton.setActive(this.pushActive);
    }

    @Override
    public void setPdfButtonState(PdfWidget.PdfButtonState pdfButtonState) {
        if (this.pdfButton != null) {
            pdfButton.setPdfButtonState(pdfButtonState);
        }
    }

    @UiHandler("gotoStartButton")
    public void handleGotoStartButton(ClickEvent event) {
        AbstractMainController.INSTANCE.goHome();
    }

    @UiHandler("refreshButton")
    public void handleRefreshButton(ClickEvent event) {
        AbstractMainController.INSTANCE.refresh();
    }

    @UiHandler("setStartButton")
    public void handleSetStartButton(ClickEvent event) {
        Dialog.confirm(I18n.I.configCurrentPageAsStartpage(), () -> {
            AbstractMainController.INSTANCE.setCurrentAsStart();
        });
    }

    @UiHandler("limitsButton")
    public void handleLimitsButton(ClickEvent event) {
        PlaceUtil.goTo("B_L"); // $NON-NLS-0$
    }

    @UiHandler("printButton")
    public void handlePrintButton(ClickEvent event) {
        AbstractMainController.INSTANCE.print();
    }

    @UiHandler("saveButton")
    public void handleSaveButton(ClickEvent event) {
        AbstractMainController.INSTANCE.save();
    }

    @UiHandler("logoutButton")
    public void handleLogoutButton(ClickEvent event) {
        DebugUtil.logToServer("TopToolbarBasic <createLogoutButton> onClick -> logout"); // $NON-NLS$
        AbstractMainController.INSTANCE.logout();
    }

    public void configureSecretActivationLabel() {
        final String username = SessionData.INSTANCE.getUserName();
        secretActivationLabel.setText(username);
        secretActivationLabel.setDoubleclickCommand(LogWindow::flipMode);
        secretActivationLabel.setSecretCommand(LogWindow::show);
    }
}
