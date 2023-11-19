/*
 * SpsPibKiidArchiveDownloadWidget.java
 *
 * Created on 16.09.2014 11:25
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine;

import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.HasBlurHandlers;
import com.google.gwt.event.dom.client.HasFocusHandlers;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import de.marketmaker.itools.gwtutil.client.util.DefaultFocusKeyHandler;
import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.itools.gwtutil.client.util.MultiWidgetFocusSupport;
import de.marketmaker.itools.gwtutil.client.util.WidgetUtil;
import de.marketmaker.itools.gwtutil.client.widgets.Button;
import de.marketmaker.itools.gwtutil.client.widgets.notification.Notifications;
import de.marketmaker.iview.dmxml.DOCURL;
import de.marketmaker.iview.dmxml.ResponseType;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.Selector;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.regdoc.IsPibKiidAvailableMethod;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.regdoc.RegDocType;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DebugUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.Dialog;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.UrlBuilder;
import de.marketmaker.iview.pmxml.ShellMMInfo;

/**
 * @author mdick
 */
public class SpsPibKiidArchiveDownloadWidget extends SpsObjectBoundWidget<FlowPanel, SpsLeafProperty, SpsLeafProperty>
        implements AsyncCallback<ResponseType>, HasFocusHandlers, HasBlurHandlers {

    private static final String INVALID_DOCUMENT_ID_ERROR_CODE = "InvalidDocumentId";  // $NON-NLS$
    private static final String PIB_KIID = "PIB/KIID";  // $NON-NLS$

    private enum State { NOT_REQUESTED, CREATE_REQUESTED, URL_REQUESTED, ERROR, CREATE_ERROR, NOT_AVAILABLE, NOT_ALLOWED }

    private static final String USER_INFO = "vwdAdvisorySolution" +  // $NON-NLS$
            "/login=" + SessionData.INSTANCE.getUser().getLogin() +  // $NON-NLS$
            "/vwdId=" + SessionData.INSTANCE.getUser().getVwdId();   // $NON-NLS$

    private final DmxmlContext dmxmlContext = new DmxmlContext();
    private final DmxmlContext.Block<DOCURL> block = dmxmlContext.addBlock("DOC_URL");  // $NON-NLS$

    private final IsPibKiidAvailableMethod isPibKiidAvailableMethod;

    private final FlowPanel layout = new FlowPanel();

    private final Button archiveButton = Button.text(I18n.I.docmanCreateAndArchive(PIB_KIID))
            .clickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    onCreateButtonClicked();
                }
            })
            .build();

    private final Label downloadLink = new Label();
    private final Label stateLabel = new Label();

    private String currentArchiveId;
    private String currentUrl;

    private final MultiWidgetFocusSupport multiWidgetFocusSupport = new MultiWidgetFocusSupport();

    public SpsPibKiidArchiveDownloadWidget() {
        this.dmxmlContext.setCancellable(false);
        this.block.setParameter("pibOrKiid", "true");  // $NON-NLS$
        this.block.setParameter("userInfo", USER_INFO);  // $NON-NLS$
        this.block.setParameter("absoluteUrl", "false");  // $NON-NLS$

        this.layout.add(this.stateLabel);
        this.layout.add(this.downloadLink);
        this.layout.add(this.archiveButton);

        this.downloadLink.addStyleName("mm-link");
        this.downloadLink.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                onDownloadLinkClicked();
            }
        });

        WidgetUtil.makeFocusable(this.downloadLink, new DefaultFocusKeyHandler() {
            @Override
            public boolean onFocusKeyClick() {
                onDownloadLinkClicked();
                return true;
            }
        });
        this.multiWidgetFocusSupport.add(this.downloadLink);
        WidgetUtil.makeFocusable(this.archiveButton, new DefaultFocusKeyHandler() {
            @Override
            public boolean onFocusKeyClick() {
                onCreateButtonClicked();
                return true;
            }
        });
        this.multiWidgetFocusSupport.add(this.archiveButton);

        if(!Selector.AS_DOCMAN.isAllowed()) {
            this.block.setEnabled(false);
            this.isPibKiidAvailableMethod = null;
            updateWidget(State.NOT_ALLOWED);
            return;
        }

        this.isPibKiidAvailableMethod = new IsPibKiidAvailableMethod(new AsyncCallback<RegDocType>() {
            @Override
            public void onFailure(Throwable caught) {
                showErrorNotification(toErrorMessage(caught));
                updateWidget(State.ERROR, caught);
            }

            @Override
            public void onSuccess(RegDocType result) {
                switch (result) {
                    case PIB:
                    case KIID:
                        updateWidget(State.NOT_REQUESTED, result);
                        return;
                    case NONE:
                    default:
                        updateWidget(State.NOT_AVAILABLE, result);
                }
            }
        });

        updateWidget(State.NOT_REQUESTED);
    }

    public SpsObjectBoundWidget withVisible(boolean visible) {
        this.layout.setVisible(visible);
        return this;
    }

    private void onDownloadLinkClicked() {
        if(this.currentUrl == null) {
            Firebug.error("<SpsPibKiidArchiveDownloadWidget.downloadLink.onClick> currentUrl is null!");
            return;
        }
        showDocument(this.currentUrl);
    }

    private void onCreateButtonClicked() {
        updateWidget(State.CREATE_REQUESTED);
        final String archiveId = getArchiveIdProperty();
        if(StringUtil.hasText(archiveId)) {
            Firebug.debug("<SpsPibKiidArchiveDownloadWidget.onCreateButtonClicked> Instrument already archived! Nothing to do");
            return;
        }

        final String symbol = getSymbolProperty();
        if(!StringUtil.hasText(symbol)) {
            Firebug.debug("<SpsPibKiidArchiveDownloadWidget.onCreateButtonClicked> No Instrument selected. Nothing to archive!");
            return;
        }

        this.block.removeParameter("id");  // $NON-NLS$
        this.block.setParameter("symbol", symbol);  // $NON-NLS$
        this.block.setToBeRequested();
        this.dmxmlContext.issueRequest(this);
    }

    private void updateWidget(String archiveId, String url) {
        this.currentArchiveId = archiveId;
        this.currentUrl = url;

        this.stateLabel.setVisible(false);
        this.downloadLink.setVisible(true);
        this.downloadLink.setText(this.currentArchiveId);
        this.archiveButton.setVisible(false);
    }

    private void updateWidget(State state) {
        updateWidget(state, null, null, null);
    }

    private void updateWidget(State state, Throwable caught) {
        final String errorMessage = toErrorMessage(caught);
        updateWidget(state, null, errorMessage, null);
        DebugUtil.logToServer("<" + getClass().getSimpleName() + "> " + errorMessage);
    }

    private String toErrorMessage(Throwable caught) {
        return caught != null ? caught.getMessage() : I18n.I.internalError();
    }

    private void updateWidget(State state, RegDocType pibKiid) {
        updateWidget(state, pibKiid, null, null);
    }

    private void updateWidget(State state, String message, String code) {
        updateWidget(state, null, message, code);
    }

    private void updateWidget(State state, RegDocType pibKiid, String message, String code) {
        this.currentArchiveId = null;
        this.currentUrl = null;

        this.downloadLink.setVisible(false);

        switch(state) {
            case NOT_REQUESTED:
                final boolean visible = StringUtil.hasText(getSymbolProperty());
                if(visible && pibKiid != null) {
                    switch (pibKiid) {
                        case PIB:
                        case KIID:
                            this.archiveButton.setText(I18n.I.docmanCreateAndArchive(pibKiid.name()));
                            break;
                        default:
                            this.archiveButton.setText(I18n.I.docmanCreateAndArchive(PIB_KIID));
                    }
                }
                this.archiveButton.setVisible(visible);
                this.stateLabel.setVisible(false);
                this.stateLabel.setText(state.name());
                break;

            case CREATE_REQUESTED:
            case URL_REQUESTED:
                this.archiveButton.setVisible(false);
                this.stateLabel.setText("...");  // $NON-NLS$
                this.stateLabel.setVisible(true);
                break;

            case NOT_ALLOWED:
                this.archiveButton.setVisible(false);
                this.stateLabel.setText(I18n.I.noAccessToFeature());
                this.stateLabel.setVisible(true);
                break;

            case NOT_AVAILABLE:
                this.archiveButton.setVisible(false);
                this.stateLabel.setText(I18n.I.notAvailable(PIB_KIID));
                this.stateLabel.setVisible(true);
                break;

            case CREATE_ERROR:
                this.archiveButton.setVisible(true);
                this.stateLabel.setVisible(false);
                break;

            case ERROR:
            default:
                this.archiveButton.setVisible(false);
                final String errorText = toErrorMessage(message, code);
                this.stateLabel.setText(errorText);
                this.stateLabel.setVisible(true);
        }
    }

    private String getSymbolProperty() {
        final SpsLeafProperty spsProperty = getObjectBindFeature().getSpsProperty();
        if(spsProperty == null) {
            return null;
        }

        final ShellMMInfo shellMMInfo = spsProperty.getShellMMInfo();
        if(shellMMInfo == null) {
            return null;
        }
        return shellMMInfo.getISIN();
    }

    /**
     * Holds the instrument if any
     */
    @Override
    public void onObjectPropertyChange() {
        if(!Selector.AS_DOCMAN.isAllowed()) {
            updateWidget(State.NOT_ALLOWED);
            return;
        }
        if(isCreateRequest()) {
            final String symbol = getSymbolProperty();
            if(!StringUtil.hasText(symbol)) {
                updateWidget(State.NOT_REQUESTED);
                return;
            }
            this.isPibKiidAvailableMethod.invoke(symbol);
        }
    }

    /**
     * Holds the archive id if any
     */
    @Override
    public void onPropertyChange() {
        if(!Selector.AS_DOCMAN.isAllowed()) {
            updateWidget(State.NOT_ALLOWED);
            return;
        }

        final String archiveId = getArchiveIdProperty();
        if(!StringUtil.equals(archiveId, this.currentArchiveId) && StringUtil.hasText(archiveId)) {
            this.block.setParameter("id", archiveId);  // $NON-NLS$
            this.block.removeParameter("symbol");  // $NON-NLS$
            this.currentArchiveId = null;
            this.block.setToBeRequested();
            this.dmxmlContext.issueRequest(this);
        }
    }

    private String getArchiveIdProperty() {
        final SpsLeafProperty spsProperty = getBindFeature().getSpsProperty();
        if(spsProperty == null) {
            return null;
        }
        return spsProperty.getStringValue();
    }

    private void setArchiveIdProperty(String id) {
        final SpsLeafProperty spsProperty = getBindFeature().getSpsProperty();
        if(spsProperty == null) {
            throw new IllegalStateException("<SpsPibKiidArchiveDownloadWidget> bound property must not be null!");  // $NON-NLS$
        }
        spsProperty.setValue(id);
    }

    private static void showDocument(String url) {
        if (StringUtil.hasText(url)) {
            Window.open(UrlBuilder.forAsDocman(url).toURL(), "_blank", "");  // $NON-NLS$
        }
    }

    @Override
    public void onFailure(Throwable caught) {
        showErrorIffCreate();
        updateWidget(State.ERROR, caught);
        Firebug.error("<SpsPibKiidArchiveDownloadWidget.onFailure> failed for symbol '" + this.block.getParameter("symbol") + "' or id '" + block.getParameter("id") + "'!");  // $NON-NLS$
    }

    @Override
    public void onSuccess(ResponseType result) {
        if(this.block.isResponseOk()) {
            final String id = block.getResult().getId();
            //order matters!
            updateWidget(id, block.getResult().getRequest());
            setArchiveIdProperty(id);
        }
        else {
            showErrorIffCreate();
            if(isCreateRequest()) {
                updateWidget(State.CREATE_ERROR);
                Firebug.error("<SpsPibKiidArchiveDownloadWidget.onSuccess> create response not ok for symbol '" + this.block.getParameter("symbol") + "'!");  // $NON-NLS$
            }
            else {
                final de.marketmaker.iview.dmxml.ErrorType errorType = this.block.getError();
                showErrorNotification(toErrorMessage(errorType));
                updateWidget(State.ERROR, errorType.getDescription(), errorType.getCode());
                Firebug.error("<SpsPibKiidArchiveDownloadWidget.onSuccess> response not ok for symbol '" + this.block.getParameter("symbol") + "' or id '" + block.getParameter("id") + "'!");  // $NON-NLS$
            }
        }
    }

    private void showErrorIffCreate() {
        if(isCreateRequest()) { //Must be an error during create!
            showErrorMessageBox(this.block.getError());
        }
    }

    private boolean isCreateRequest() {
        return !StringUtil.hasText(getArchiveIdProperty());
    }

    private void showErrorMessageBox(de.marketmaker.iview.dmxml.ErrorType error) {
        Dialog.error(toErrorMessage(error));
    }

    private void showErrorNotification(String message) {
        Notifications.add(getCaption(), SafeHtmlUtils.fromString(message));
    }

    private String toErrorMessage(String message, String code) {
        if(INVALID_DOCUMENT_ID_ERROR_CODE.equals(code)) {
            return I18n.I.invalidDocumentId(getArchiveIdProperty());
        }
        else if(StringUtil.hasText(message)) {
            return message;
        }
        else {
            return I18n.I.internalError();
        }
    }

    private String toErrorMessage(de.marketmaker.iview.dmxml.ErrorType error) {
        if(error == null) {
            return toErrorMessage(null, null);
        }
        return toErrorMessage(error.getDescription(), error.getCode());
    }

    @Override
    protected FlowPanel createWidget() {
        this.layout.setStyleName(getBaseStyle());
        return this.layout;
    }

    @Override
    public HandlerRegistration addFocusHandler(FocusHandler handler) {
        return this.multiWidgetFocusSupport.addFocusHandler(handler);
    }

    @Override
    public HandlerRegistration addBlurHandler(BlurHandler handler) {
        return this.multiWidgetFocusSupport.addBlurHandler(handler);
    }

    @Override
    public void fireEvent(GwtEvent<?> event) {
        this.multiWidgetFocusSupport.fireEvent(event);
    }
}
