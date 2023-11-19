/*
 * OrderBookDelegatePageController.java
 *
 * Created on 05.09.13 12:29
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.publicnavitemspec;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.RunAsyncCallback;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HTML;
import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.iview.dmxml.ResponseType;
import de.marketmaker.iview.mmgwt.mmweb.client.AbstractMainController;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.PageController;
import de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.OrderModule;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PlaceChangeEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.depotobjects.AbstractDepotObjectPortraitController;
import de.marketmaker.iview.mmgwt.mmweb.client.util.PdfOptionSpec;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.view.ContentContainer;

import java.util.Map;

/**
 * This controller can be safely used outside of order entry without disturbing code splitting
 * @author Markus Dick
 */
public class OrderBookDelegatePageController implements PageController {
    private IsPageControllerIsWidget delegate;
    private String currentObjectId = null;

    @Override
    public void onPlaceChange(final PlaceChangeEvent event) {
        final String depotId = event.getHistoryToken().get(AbstractDepotObjectPortraitController.OBJECTID_KEY);
        if(this.delegate != null && StringUtil.equals(this.currentObjectId, depotId)) {
            Firebug.debug("<OrderBookDelegatePageController.onPlaceChange> depotId has not changed, only show required!");
            showView();
            this.delegate.asPageController().onPlaceChange(event);
            return;
        }
        this.currentObjectId = depotId;

        Firebug.debug("<OrderBookDelegatePageController.onPlaceChange> depotId has changed, requesting new OrderBookController! depotId=" + depotId);
        GWT.runAsync(new RunAsyncCallback() {
            @Override
            public void onFailure(Throwable reason) {
                Firebug.warn("<OrderBookDelegatePageController.onPlaceChange> GWT.runAsync failed!", reason);
                onOrderBookRequestFailed(reason);
            }

            @Override
            public void onSuccess() {
                OrderModule.createInstance(new AsyncCallback<ResponseType>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        Firebug.warn("<OrderBookDelegatePageController.onPlaceChange> Initializing order module failed!", caught);
                        onOrderBookRequestFailed(caught);
                    }

                    @Override
                    public void onSuccess(ResponseType result) {
                        OrderModule.requestOrderBookByDepotId(depotId, new AsyncCallback<IsPageControllerIsWidget>() {
                            @Override
                            public void onSuccess(IsPageControllerIsWidget result) {
                                Firebug.debug("<OrderBookDelegatePageController.onPlaceChange> requestOrderBookByDepotId successful!");
                                onOrderBookRequestSuccessful(result, event);
                            }

                            @Override
                            public void onFailure(Throwable caught) {
                                Firebug.warn("<OrderBookDelegatePageController.onPlaceChange> requestOrderBookByDepotId failed!", caught);
                                onOrderBookRequestFailed(caught);
                            }
                        });
                    }
                });
            }
        });
    }

    private void onOrderBookRequestSuccessful(IsPageControllerIsWidget delegate, PlaceChangeEvent event) {
        if(this.delegate != delegate) {
            delegate.asPageController().destroy();
        }
        this.delegate = delegate;
        showView();
        this.delegate.asPageController().onPlaceChange(event);
    }

    private void showView() {
        if(this.delegate != null) {
            getContentContainer().setContent(this.delegate.asWidget());
        }
    }

    private void onOrderBookRequestFailed(Throwable caught) {
        if(caught != null) {
            AbstractMainController.INSTANCE.showError(I18n.I.orderEntryError(caught.getMessage()));
        }
        if(this.delegate != null) {
            this.delegate.asPageController().destroy();
            this.delegate = null;
        }
        getContentContainer().setContent(new HTML(I18n.I.internalError()));
    }

    public ContentContainer getContentContainer() {
        return AbstractMainController.INSTANCE.getView();
    }

    @Override
    public void destroy() {
        if(this.delegate != null) {
            this.delegate.asPageController().destroy();
        }
    }

    @Override
    public void refresh() {
        Firebug.debug("<OrderBookDelegatePageController.refresh>");

        if(this.delegate != null) {
            this.delegate.asPageController().refresh();
        }
    }

    @Override
    public boolean supportsHistory() {
        return this.delegate != null && this.delegate.asPageController().supportsHistory();
    }

    @Override
    public String getPrintHtml() {
        if(this.delegate != null) {
            return this.delegate.asPageController().getPrintHtml();
        }
        return null;
    }

    @Override
    public boolean isPrintable() {
        return this.delegate != null && this.delegate.asPageController().isPrintable();
    }

    @Override
    public PdfOptionSpec getPdfOptionSpec() {
        if(this.delegate != null) {
            return this.delegate.asPageController().getPdfOptionSpec();
        }
        return null;
    }

    @Override
    public String[] getAdditionalStyleSheetsForPrintHtml() {
        if(this.delegate != null) {
            return this.delegate.asPageController().getAdditionalStyleSheetsForPrintHtml();
        }
        return new String[0];
    }

    @Override
    public void addPdfPageParameters(Map<String, String> mapParameters) {
        if(this.delegate != null) {
            this.delegate.asPageController().addPdfPageParameters(mapParameters);
        }
    }

    @Override
    public void activate() {
        if(this.delegate != null) {
            this.delegate.asPageController().activate();
        }
    }

    @Override
    public void deactivate() {
        if(this.delegate != null) {
            this.delegate.asPageController().deactivate();
        }
    }
}
