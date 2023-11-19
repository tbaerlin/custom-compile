/*
 * DefaultTopToolbar.java
 *
 * Created on 30.11.12
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client;

import java.util.function.Supplier;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;

import de.marketmaker.itools.gwtutil.client.util.CompareUtil;
import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.itools.gwtutil.client.widgets.Button;
import de.marketmaker.itools.gwtutil.client.widgets.IconImage;
import de.marketmaker.itools.gwtutil.client.widgets.ImageButton;
import de.marketmaker.itools.gwtutil.client.widgets.Tooltip;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.TextUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.events.ConfigChangedEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.events.EventBusRegistry;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PendingRequestsEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PushActivationEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DebugUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.Dialog;
import de.marketmaker.iview.mmgwt.mmweb.client.util.GuiUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.PdfOptionSpec;
import de.marketmaker.iview.mmgwt.mmweb.client.util.PlaceUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.widgets.PdfWidget;
import de.marketmaker.iview.mmgwt.mmweb.client.widgets.SearchBox;
import de.marketmaker.iview.mmgwt.mmweb.client.workspace.FavouriteItemsStores;
import de.marketmaker.iview.mmgwt.mmweb.client.workspace.FavouritesPopup;

/**
 * @author Ulrich Maurer
 */
public class DefaultTopToolbar implements TopToolbar {

    public static final double TOOLBAR_HEIGHT = 48;

    interface ToolbarUiBinder extends UiBinder<Panel, DefaultTopToolbar> {
    }

    private static ToolbarUiBinder uiBinder = GWT.create(ToolbarUiBinder.class);

    @UiField
    Panel toolbarPanel;

    @UiField
    Image leftLogo;

    @UiField
    SearchBox searchBox;

    @UiField(provided = true)
    final Image separator1 = IconImage.get("as-toptoolbar-sep").createImage();  // $NON-NLS$;

    @UiField(provided = true)
    final Image separator2 = IconImage.get("as-toptoolbar-sep").createImage();  // $NON-NLS$;

    @UiField(provided = true)
    ImageButton refreshButton = GuiUtil.createImageButton("as-reload-24", null, "as-reload-24 active", I18n.I.doUpdate()); // $NON-NLS$;

    @UiField(provided = true)
    Button pushButton = Button.icon("ice-push").tooltip(I18n.I.push()).build(); // $NON-NLS$

    private boolean pushActive = false;

    @UiField(provided = true)
    PdfWidget pdfButton = new PdfWidget(GuiUtil.createImageButton("as-export-pdf-24", "as-export-pdf-24 disabled", // $NON-NLS$;
            "as-export-pdf-24-with-options", I18n.I.exportAsPdf()));  // $NON-NLS$;

    @UiField(provided = true)
    ImageButton printButton = GuiUtil.createImageButton("as-print-24", "as-print-24 disabled", null, I18n.I.print()); // $NON-NLS$;

    @UiField(provided = true)
    Button limitsButton = Button.icon("ice-alert-24").tooltip(I18n.I.limit()).build();  // $NON-NLS$

    @UiField(provided = true)
    Button defineHomeButton = Button.icon("ice-define-home-24") // $NON-NLS$
            .tooltip(I18n.I.currentPageAsStartpage())
            .clickHandler(clickEvent -> Dialog.confirm(I18n.I.configCurrentPageAsStartpage(),
                    () -> AbstractMainController.INSTANCE.setCurrentAsStart()))
            .build();

    @UiField(provided = true)
    Button favouritesButton = Button.icon("ice-bookmark-24").tooltip(I18n.I.favorites()).build();  // $NON-NLS$

    @UiField(provided = true)
    Image rightLogo;

    @UiField(provided = true)
    ImageButton logoutButton = new ImageButton(IconImage.get("as-logout").createImage(), null, null);  // $NON-NLS$;

    @UiField(provided = true)
    Button privacyModeButton = Button.icon("as-privacymode-start").tooltip(I18n.I.privacyMode()).build();  // $NON-NLS$

    @UiField
    Label username;

    private FavouritesPopup favouritesPopup = new FavouritesPopup();

    private Supplier<Image> rightLogoSupplier;

    public DefaultTopToolbar(Supplier<Image> rightLogoSupplier) {
        this.rightLogoSupplier = rightLogoSupplier;
        initView();
        initHandlers();
    }

    private void initView() {
        this.rightLogo = createRightLogo();

        uiBinder.createAndBindUi(this);

        this.toolbarPanel.getElement().getStyle().setHeight(TOOLBAR_HEIGHT, Style.Unit.PX);

        if (SessionData.INSTANCE.isWithPush() && AbstractMainController.INSTANCE.withIcePushButton()) {
            this.pushButton.setVisible(true);
            this.pushButton.addClickHandler(clickEvent -> EventBusRegistry.get().fireEvent(new PushActivationEvent(!pushActive)));
        }

        if (isPdfButtonAllowed()) {
            this.pdfButton.asWidget().setVisible(true);
        }

        if (isPrintButtonAllowed()) {
            this.printButton.setVisible(true);
        }

        if (SessionData.isWithMarketData()) {
            this.favouritesButton.setVisible(true);
            this.favouritesButton.setEnabled(!FavouriteItemsStores.isAllStoresEmpty());
        }

        if (SessionData.isWithPmBackend()) {
            // app must be an Infront Advisory Solution
            this.leftLogo.addStyleName("ice-as-left-logo");  // $NON-NLS$

            // no 'define home' button in case of Infront Advisory Solution
            this.defineHomeButton.removeFromParent();
        }
        else {
            // app must be an vwd mm[web] in ICE/AS design
            this.leftLogo.addStyleName("ice-mmweb-left-logo");  // $NON-NLS$

            // set 'define home' button visible (invisible by default see UI binder XML)
            this.defineHomeButton.setVisible(true);
            // 'privacy mode' button only in case of Infront Advisory Solution
            this.privacyModeButton.removeFromParent();
            this.separator2.removeFromParent();
        }

        if (SessionData.isWithLimits()) {
            this.limitsButton.setVisible(true);
        }

        EventBusRegistry.get().addHandler(ConfigChangedEvent.getType(), event -> {
            if (FavouriteItemsStores.isFavouriteItemsConfigChangedEvent(event)) {
                this.favouritesButton.setEnabled(!FavouriteItemsStores.isAllStoresEmpty());
            }
        });

        final String loggedInSince = SessionData.INSTANCE.getLoggedInSinceWithLabel();
        final Tooltip.TooltipFactory qtipLogout = Tooltip.addQtip(this.logoutButton, TextUtil.toSafeHtmlLines(I18n.I.logout(), loggedInSince));
        final Tooltip.TooltipFactory qtipUsername = Tooltip.addQtip(this.username, loggedInSince);

        Scheduler.get().scheduleFixedPeriod(() -> {
            final String currentLoggedInSince = SessionData.INSTANCE.getLoggedInSinceWithLabel();
            if (!CompareUtil.equals(loggedInSince, currentLoggedInSince)) {
                qtipLogout.set(TextUtil.toSafeHtmlLines(I18n.I.logout(), currentLoggedInSince));
                qtipUsername.set(currentLoggedInSince);
            }
            return true;
        }, 3600000);
    }

    public DefaultTopToolbar forGrains() {
        this.searchBox.asWidget().removeFromParent();
        this.separator1.removeFromParent();
        this.pushButton.removeFromParent();
        this.pdfButton.removeFromParent();
        this.limitsButton.removeFromParent();
        this.defineHomeButton.removeFromParent();
        this.favouritesButton.removeFromParent();
        this.separator2.removeFromParent();
        return this;
    }

    public DefaultTopToolbar forMetals() {
        this.separator1.removeFromParent();
        this.searchBox.asWidget().removeFromParent();
        this.pushButton.removeFromParent();
        this.pdfButton.removeFromParent();
        this.limitsButton.removeFromParent();
        this.favouritesButton.removeFromParent();
        this.separator2.removeFromParent();
        return this;
    }

    private void initHandlers() {
        EventBusRegistry.get().addHandler(PendingRequestsEvent.getType(), this);
        EventBusRegistry.get().addHandler(ConfigChangedEvent.getType(), this);
        EventBusRegistry.get().addHandler(PushActivationEvent.getType(), this);
    }

    @Override
    public void ackSave() {
        Firebug.log("DefaultTopToolbar.ackSave()");
    }

    @Override
    public void onConfigChange(ConfigChangedEvent event) {
        Firebug.log("DefaultTopToolbar.onConfigChange(...)");
    }

    @Override
    public void updateLimitsIcon(String pending) {
        this.limitsButton.setActive(pending != null);
    }

    @Override
    public void onPushActivated(PushActivationEvent event) {
        this.pushActive = event.isActive();
        this.pushButton.setActive(this.pushActive);
    }

    @Override
    public Widget asWidget() {
        return this.toolbarPanel;
    }

    private boolean isPdfButtonAllowed() {
        return !"true".equals(SessionData.INSTANCE.getGuiDefValue("disablePdf"));  // $NON-NLS$
    }

    private boolean isPrintButtonAllowed() {
        return !"true".equals(SessionData.INSTANCE.getGuiDefValue("disablePrint"));  // $NON-NLS$
    }

    private Image createRightLogo() {
        return this.rightLogoSupplier.get();
    }

    @Override
    public void onPendingRequestsUpdate(PendingRequestsEvent event) {
        this.refreshButton.setActive(event.getNumPending() != 0);
    }

    @Override
    public void updatePdfButtonState() {
        final PdfOptionSpec spec = AbstractMainController.INSTANCE.getPdfOptionSpec();
        setPdfButtonState(spec == null ? PdfWidget.PdfButtonState.DISABLED : (spec.isWithOptions()
                ? PdfWidget.PdfButtonState.WITH_OPTIONS : PdfWidget.PdfButtonState.ENABLED));
    }

    @Override
    public void setPdfButtonState(PdfWidget.PdfButtonState pdfButtonState) {
        if (isPdfButtonAllowed()) {
            pdfButton.setPdfButtonState(pdfButtonState);
        }
    }

    @Override
    public void setPrintButtonEnabled(boolean enabled) {
        if (isPrintButtonAllowed()) {
            this.printButton.setEnabled(enabled);
        }
    }

    @Override
    public void updatePrintButtonEnabledState() {
        this.setPrintButtonEnabled(AbstractMainController.INSTANCE.isPrintable());
    }

    @SuppressWarnings("UnusedParameters")
    @UiHandler("leftLogo")
    public void onLeftLogoClicked(ClickEvent event) {
        AbstractMainController.INSTANCE.goHome();
    }

    @SuppressWarnings("UnusedParameters")
    @UiHandler("refreshButton")
    public void handleRefreshButton(ClickEvent event) {
        AbstractMainController.INSTANCE.refresh();
    }

    @SuppressWarnings("UnusedParameters")
    @UiHandler("printButton")
    public void handlePrintButton(ClickEvent event) {
        AbstractMainController.INSTANCE.print();
    }

    @SuppressWarnings("UnusedParameters")
    @UiHandler("favouritesButton")
    public void handleFavouritesButton(ClickEvent event) {
        this.favouritesPopup.showNearby(this.favouritesButton, (Widget) null);
    }

    @SuppressWarnings("UnusedParameters")
    @UiHandler("logoutButton")
    public void handleLogoutButton(ClickEvent event) {
        DebugUtil.logToServer("DefaultTopToolbar <createLogoutButton> onClick -> logout");
        AbstractMainController.INSTANCE.logout();
    }

    @SuppressWarnings("UnusedParameters")
    @UiHandler("privacyModeButton")
    public void onPrivacyModeClicked(ClickEvent event) {
        final boolean active = this.privacyModeButton.isActive();
        if (!active) {
            AbstractMainController.INSTANCE.onEnterPrivacyMode();
        }
        else {
            AbstractMainController.INSTANCE.onLeavePrivacyMode();
        }
    }

    @SuppressWarnings("UnusedParameters")
    @UiHandler("limitsButton")
    public void handleLimitsButton(ClickEvent event) {
        PlaceUtil.goTo("B_L"); // $NON-NLS$
    }

    public void setPrivacyModeEnabled(boolean enabled) {
        this.privacyModeButton.setEnabled(enabled);
    }

    public void setPrivacyMode(boolean privacyMode) {
        this.privacyModeButton.setIcon(privacyMode ? "as-privacymode-stop" : "as-privacymode-start"); // $NON-NLS$
        this.privacyModeButton.setActive(privacyMode);
        this.searchBox.setPrivacyMode(privacyMode);
        this.separator1.setVisible(this.searchBox.isVisible());
    }
}