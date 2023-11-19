/*
 * SpsPibKiidAvailabilityWidget.java
 *
 * Created on 02.07.2014 11:28
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine;

import java.util.function.Supplier;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.Selector;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.regdoc.IsRegulatoryDocumentAvailableMethod;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.regdoc.RegDocType;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.context.DmxmlContextFacade;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.widget.NoValidationPopup;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DebugUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;
import de.marketmaker.iview.pmxml.OrderActionType;

/**
 * @author Markus Dick
 */
public class SpsPibKiidAvailabilityWidget extends SpsObjectBoundWidget<FlowPanel, SpsLeafProperty, SpsLeafProperty>
        implements AsyncCallback<RegDocType>, NoValidationPopup {
    protected final FlowPanel panel = new FlowPanel();

    protected final HTML statusLabel = new HTML();
    protected final HTML kidLabel = new HTML();
    protected final HTML pibLabel = new HTML();
    protected final HTML kiidLabel = new HTML();

    protected enum State { NOT_VISIBLE, NOT_ALLOWED, NOT_REQUESTED, LOADING, KID_AVAILABLE, PIB_AVAILABLE, KIID_AVAILABLE, NOT_AVAILABLE, ERROR }

    private final IsRegulatoryDocumentAvailableMethod method;

    public SpsPibKiidAvailabilityWidget(Supplier<DmxmlContextFacade> dmxmlContextFacadeSupplier) {
        this.method = new IsRegulatoryDocumentAvailableMethod(dmxmlContextFacadeSupplier, this);

        this.panel.add(this.statusLabel);
        this.panel.add(this.kidLabel);
        this.panel.add(this.pibLabel);
        this.panel.add(this.kiidLabel);

        this.statusLabel.setStyleName("sps-pib-kiid-status");

        this.kidLabel.setStyleName("sps-pib-kiid-pib");
        this.kidLabel.setText(I18n.I.available("KID"));  // $NON-NLS$

        this.pibLabel.setStyleName("sps-pib-kiid-pib");
        this.pibLabel.setText(I18n.I.producible("PIB"));  // $NON-NLS$

        this.kiidLabel.setStyleName("sps-pib-kiid-kiid");
        this.kiidLabel.setText(I18n.I.available("KIID"));  // $NON-NLS$

        updateWidgets(Selector.AS_DOCMAN.isAllowed() ? State.NOT_REQUESTED : State.NOT_ALLOWED);
    }

    @Override
    public boolean isReadonly() {
        return true;
    }

    @Override
    public void onPropertyChange() {
        doOnPropertyChange();
    }

    private void doOnPropertyChange() {
        if(!isInternalVisible()) {
            updateWidgets(State.NOT_VISIBLE);
            return;
        }

        final SpsLeafProperty spsProperty = getBindFeature().getSpsProperty();

        if(spsProperty != null && spsProperty.getDataItem() != null) {
            if(spsProperty.isShellMMInfo()) {
                updateWidgets(State.LOADING);
                this.method.invoke(spsProperty.getShellMMInfo().getISIN());
            }
            else {
                Firebug.warn("<SpsPibKiidAvailabilityWidget.onPropertyChange> value is not of type ShellMMInfo");
                DebugUtil.showDeveloperNotification("Bound property for pibKiidAvailability must be of type ShellMMInfo!");  // $NON-NLS$
                updateWidgets(State.ERROR);
            }
        }
        else {
            updateWidgets(State.NOT_REQUESTED);
        }
    }

    private boolean isInternalVisible() {
        if(!Selector.AS_DOCMAN.isAllowed()) {
            return false;
        }

        final SpsLeafProperty spsObjectProperty = getObjectBindFeature().getSpsProperty();
        if(spsObjectProperty != null) {
            final OrderActionType orderActionType = SpsUtil.toOrderActionType(spsObjectProperty.getStringValue());
            return orderActionType == OrderActionType.AT_BUY || orderActionType == OrderActionType.AT_SUBSCRIBE;
        }
        return true;
    }

    @Override
    public void onObjectPropertyChange() {
        doOnPropertyChange();
    }

    @Override
    public void onFailure(Throwable caught) {
//        final String errorMessage = toErrorMessage(caught);
//        showErrorNotification(errorMessage);
        updateWidgets(caught);
//        DebugUtil.logToServer("<" + getClass().getSimpleName() + "> " + errorMessage);
    }

    @Override
    public void onSuccess(RegDocType result) {
        final State state;
        switch (result) {
            case KID:
                state = State.KID_AVAILABLE;
                break;
            case PIB:
                state = State.PIB_AVAILABLE;
                break;
            case KIID:
                state = State.KIID_AVAILABLE;
                break;
            case NONE:
            default:
                state = State.NOT_AVAILABLE;
        }
        updateWidgets(state);
    }

    protected void updateWidgets(Throwable caught) {
        updateWidgets(State.ERROR, caught);
    }

    protected void updateWidgets(State state) {
        updateWidgets(state, null);
    }

    protected void updateWidgets(State state, Throwable caught) {
//        Firebug.debug("<" + getClass().getSimpleName() + ".updateWidgets> state=" + state);

        this.statusLabel.removeStyleName("sps-pib-kiid-loading");
        this.statusLabel.removeStyleName("sps-pib-kiid-not-available");
        this.statusLabel.removeStyleName("sps-pib-kiid-error");

        //do not use isInternalVisible here!
        final boolean visible = state != State.NOT_VISIBLE;
        this.panel.setVisible(visible);
        if(getCaptionWidget() != null) {  //lazily created!
            getCaptionWidget().setVisible(visible);
        }

        switch (state) {
            case NOT_VISIBLE:
                this.statusLabel.setText(null);
                hideDocTypeLabel();
                return;

            case NOT_ALLOWED:
                showStatus(I18n.I.noAccessToFeature(), "sps-pib-kiid-no-access", state);  // $NON-NLS$
                return;

            case NOT_REQUESTED:
                this.showStatus(null, null, state);
                hideDocTypeLabel();
                return;

            case LOADING:
                showStatus("...", "sps-pib-kiid-loading", state);  // $NON-NLS$
                return;

            case KID_AVAILABLE:
                this.statusLabel.setVisible(false);
                this.kidLabel.setVisible(true);
                return;

            case PIB_AVAILABLE:
                this.statusLabel.setVisible(false);
                this.pibLabel.setVisible(true);
                return;

            case KIID_AVAILABLE:
                this.statusLabel.setVisible(false);
                this.kiidLabel.setVisible(true);
                return;

            case NOT_AVAILABLE:
                showStatus(I18n.I.notAvailable("KID/PIB/KIID"), "sps-pib-kiid-not-available", state);  // $NON-NLS$
                return;

            case ERROR:
            default:
                showStatus(toErrorMessage(caught), "sps-pib-kiid-error", state);  // $NON-NLS$
        }
    }

    protected void showStatus(String text, String style, State state) {
        this.statusLabel.setVisible(true);
        this.statusLabel.setText(text);
        addOrRemoveStyleName(style);
        hideDocTypeLabel();
    }

    private void addOrRemoveStyleName(String style) {
        if(style == null) {
            final String styleName = this.statusLabel.getStyleName();
            if(StringUtil.hasText(styleName)) {
                this.statusLabel.removeStyleName(styleName);
            }
        }
        else {
            this.statusLabel.addStyleName(style);
        }
    }

    protected void hideDocTypeLabel() {
        this.kidLabel.setVisible(false);
        this.kiidLabel.setVisible(false);
        this.pibLabel.setVisible(false);
    }

    protected String toErrorMessage(Throwable caught) {
        if(caught != null && StringUtil.hasText(caught.getMessage())) {
            return  I18n.I.internalError() + ": " + caught.getMessage();  // $NON-NLS$
        }
        return I18n.I.internalError();
    }

    @Override
    protected void onWidgetConfigured() {
        forceCaptionWidget();
        super.onWidgetConfigured();
    }

    @Override
    protected FlowPanel createWidget() {
        this.panel.setStyleName(getBaseStyle());
        return this.panel;
    }

    @Override
    public boolean isVisible() {
        return super.isVisible() && isInternalVisible();
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible && isInternalVisible());
    }

    @Override
    public void release() {
        super.release();
        this.method.release();
    }
}
