/*
 * IsPibKiidAvailableMethod.java
 *
 * Created on 16.09.2014 17:57
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.regdoc;

import com.google.gwt.user.client.rpc.AsyncCallback;
import de.marketmaker.iview.dmxml.DOCInstrument;
import de.marketmaker.iview.dmxml.DOCInstrumentAvailability;
import de.marketmaker.iview.dmxml.ErrorType;
import de.marketmaker.iview.dmxml.FNDReports;
import de.marketmaker.iview.dmxml.ReportType;
import de.marketmaker.iview.dmxml.ResponseType;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.Selector;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.context.DmxmlContextFacade;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.context.SimpleDmxmlContextFacade;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;

import java.util.List;
import java.util.function.Supplier;

/**
 * @author mdick
 */
public class IsPibKiidAvailableMethod implements AsyncCallback<ResponseType> {

    private final DmxmlContextFacade dmxmlContextFacade;
    private final DmxmlContext dmxmlContext;
    private final DmxmlContext.Block<DOCInstrumentAvailability> pibBlock;
    private final DmxmlContext.Block<FNDReports> kiidBlock;
    private final AsyncCallback<RegDocType> callback;

    public IsPibKiidAvailableMethod(AsyncCallback<RegDocType> callback) {
        this(Selector.AS_DOCMAN.isAllowed() ? new SimpleDmxmlContextFacade() : null, callback);
    }

    @SuppressWarnings("unused")
    public IsPibKiidAvailableMethod(Supplier<DmxmlContextFacade> dmxmlContextFacadeSupplier, AsyncCallback<RegDocType> callback) {
        this(Selector.AS_DOCMAN.isAllowed() ? dmxmlContextFacadeSupplier.get() : null, callback);
    }

    private IsPibKiidAvailableMethod(DmxmlContextFacade dmxmlContextFacade, AsyncCallback<RegDocType> callback) {
        this.callback = callback;

        if(!Selector.AS_DOCMAN.isAllowed()) {
            this.dmxmlContextFacade = null;
            this.dmxmlContext = null;
            this.pibBlock = null;
            this.kiidBlock = null;
            return;
        }

        this.dmxmlContextFacade = dmxmlContextFacade;
        this.dmxmlContext = dmxmlContextFacade.createOrGetContext(2);
        this.pibBlock = this.dmxmlContext.addBlock("DOC_InstrumentAvailability"); // $NON-NLS$
        this.kiidBlock = this.dmxmlContext.addBlock("FND_Reports@DOC");  // $NON-NLS$

        setBlocksEnabled(false);

        this.dmxmlContextFacade.subscribe(this.dmxmlContext, this);

        this.dmxmlContext.setCancellable(false);
    }

    public void invoke(String isin) {
        if(!Selector.AS_DOCMAN.isAllowed()) {
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
        if(!this.pibBlock.isEnabled() && !this.kiidBlock.isEnabled()) {
            return;
        }
        doOnSuccess();
        setBlocksEnabled(false);
    }

    private void doOnSuccess() {
        if(this.pibBlock.isResponseOk()) {
            final List<DOCInstrument> items = this.pibBlock.getResult().getItem();
            for (DOCInstrument item : items) {
                if(item.isAvailable() || (Boolean.TRUE.equals(item.isIssuerPibSource()))) {
                    this.callback.onSuccess(RegDocType.PIB);
                    return;
                }
            }
            if(this.kiidBlock.isResponseOk()) {
                final List<ReportType> report = this.kiidBlock.getResult().getReport();

                if(report.isEmpty()) {
                    this.callback.onSuccess(RegDocType.NONE);
                    return;
                }

                for (ReportType item : report) {
                    if("KIID".equals(item.getType())) {  // $NON-NLS$
                        this.callback.onSuccess(RegDocType.KIID);
                        return;
                    }
                }
                this.callback.onSuccess(RegDocType.NONE);
            }
            else {
                final ErrorType errorType = this.kiidBlock.getError();
                if(errorType != null && "instrument.symbol.unknown".equals(errorType.getCode())) {  // $NON-NLS$
                    this.callback.onSuccess(RegDocType.NONE);
                    return;
                }
                this.callback.onFailure(new RuntimeException(getErrorDescription(this.kiidBlock.getError())));
                return;
            }
            this.callback.onSuccess(RegDocType.NONE);
        }
        else {
            this.callback.onFailure(new RuntimeException(getErrorDescription(this.pibBlock.getError())));
        }
    }

    private void setSymbolParameters(String isin) {
        this.pibBlock.setParameter("symbol", isin); // $NON-NLS$
        this.kiidBlock.setParameter("symbol", isin); // $NON-NLS$
    }

    private void setBlocksEnabled(boolean enabled) {
        this.pibBlock.setEnabled(enabled);
        this.kiidBlock.setEnabled(enabled);
    }

    private String getErrorDescription(ErrorType errorType) {
        if(errorType == null) {
            return I18n.I.internalError();
        }
        return errorType.getDescription();
    }

    public void release() {
        if(this.dmxmlContextFacade != null) {
            this.dmxmlContextFacade.unsubscribe(this.dmxmlContext, this);
            this.dmxmlContext.removeBlock(this.pibBlock);
            this.dmxmlContext.removeBlock(this.kiidBlock);
        }
    }
}
