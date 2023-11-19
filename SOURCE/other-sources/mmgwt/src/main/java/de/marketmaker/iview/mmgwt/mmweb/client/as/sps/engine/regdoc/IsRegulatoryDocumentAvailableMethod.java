/*
 * IsRegulatoryDocumentAvailableMethod.java
 *
 * Created on 02.02.2018 13:09
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.regdoc;

import java.util.function.Supplier;

import com.google.gwt.user.client.rpc.AsyncCallback;

import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.iview.dmxml.ErrorType;
import de.marketmaker.iview.dmxml.ResponseType;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.Selector;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.context.DmxmlContextFacade;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;
import de.marketmaker.iview.pmxml.RegHubDocumentAvailability;
import de.marketmaker.iview.pmxml.RegHubDocumentAvailabilityRequest;
import de.marketmaker.iview.pmxml.RegHubDocumentType;

/**
 * @author mdick
 */
public class IsRegulatoryDocumentAvailableMethod implements AsyncCallback<ResponseType> {
    private final DmxmlContextFacade dmxmlContextFacade;

    private final DmxmlContext dmxmlContext;

    private final DmxmlContext.Block<RegHubDocumentAvailability> block;

    private final AsyncCallback<RegDocType> callback;

    public IsRegulatoryDocumentAvailableMethod(Supplier<DmxmlContextFacade> dmxmlContextFacadeSupplier, AsyncCallback<RegDocType> callback) {
        this(Selector.AS_DOCMAN.isAllowed() ? dmxmlContextFacadeSupplier.get() : null, callback);
    }

    private IsRegulatoryDocumentAvailableMethod(DmxmlContextFacade dmxmlContextFacade, AsyncCallback<RegDocType> callback) {
        this.callback = callback;

        if (!Selector.AS_DOCMAN.isAllowed()) {
            this.dmxmlContextFacade = null;
            this.dmxmlContext = null;
            this.block = null;
            return;
        }

        this.dmxmlContextFacade = dmxmlContextFacade;
        this.dmxmlContext = dmxmlContextFacade.createOrGetContext(1);
        this.block = this.dmxmlContext.addBlock("PM_RegDocAvailability"); // $NON-NLS$

        setBlocksEnabled(false);

        this.dmxmlContextFacade.subscribe(this.dmxmlContext, this);

        this.dmxmlContext.setCancellable(false);
    }

    public void invoke(String isin) {
        if (!Selector.AS_DOCMAN.isAllowed()) {
            this.callback.onFailure(new RuntimeException(I18n.I.noAccessToFeature()));
            return;
        }
        setSymbolParameters(isin);
        setBlocksEnabled(true);
        this.dmxmlContextFacade.reload();
    }

    @Override
    public void onFailure(Throwable caught) {
        this.callback.onFailure(caught);
        setBlocksEnabled(false);
    }

    @Override
    public void onSuccess(ResponseType result) {
        if (!this.block.isEnabled()) {
            return;
        }
        doOnSuccess();
        setBlocksEnabled(false);
    }

    private void doOnSuccess() {
        if (this.block.isResponseOk()) {
            final RegHubDocumentAvailability result = this.block.getResult();
            if (result.isIsRegulatory()) {
                final RegHubDocumentType documentType = result.getDocumentType();
                if (documentType == null) {
                    Firebug.error("<IsRegulatoryDocumentAvailableMethod> document type is null");
                    this.callback.onSuccess(RegDocType.NONE);
                    return;
                }
                Firebug.debug("<IsRegulatoryDocumentAvailableMethod> " + result.getInstrumentID() + " is regulatory: " + documentType);
                switch (documentType) {
                    case RDT_KID:
                        this.callback.onSuccess(RegDocType.KID);
                        return;
                    case RDT_KIID:
                        this.callback.onSuccess(RegDocType.KIID);
                        return;
                    case RDT_PIB:
                        this.callback.onSuccess(RegDocType.PIB);
                        return;
                    default:
                        this.callback.onSuccess(RegDocType.NONE);
                }
            }
            else {
                Firebug.debug("<IsRegulatoryDocumentAvailableMethod> " + result.getInstrumentID() + " is not regulatory");
                this.callback.onSuccess(RegDocType.NONE);
            }
        }
        else {
            this.callback.onFailure(new RuntimeException(getErrorDescription(this.block.getError())));
        }
    }

    private void setSymbolParameters(String isin) {
        final RegHubDocumentAvailabilityRequest request = new RegHubDocumentAvailabilityRequest();
        request.setInstrumentID(isin);
        this.block.setParameter(request);
    }

    private void setBlocksEnabled(boolean enabled) {
        this.block.setEnabled(enabled);
    }

    private String getErrorDescription(ErrorType errorType) {
        if (errorType == null) {
            return I18n.I.internalError();
        }
        return errorType.getDescription();
    }

    public void release() {
        if (this.dmxmlContextFacade != null) {
            this.dmxmlContextFacade.unsubscribe(this.dmxmlContext, this);
            this.dmxmlContext.removeBlock(this.block);
        }
    }
}
