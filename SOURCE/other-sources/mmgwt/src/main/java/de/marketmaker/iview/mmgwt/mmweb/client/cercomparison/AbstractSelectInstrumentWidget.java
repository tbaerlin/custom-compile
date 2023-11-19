/*
 * AbstractSelectInstrumentWidget.java
 *
 * Created on 04.11.2014 12:48
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.cercomparison;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.iview.dmxml.CERDetailedStaticData;
import de.marketmaker.iview.dmxml.ResponseType;
import de.marketmaker.iview.mmgwt.mmweb.client.AbstractMainController;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.QuoteWithInstrument;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.ConfigurableSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.config.SnippetConfigurationView;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;

import java.util.HashMap;

/**
 * @author Markus Dick
 */
class AbstractSelectInstrumentWidget extends Composite implements ConfigurableSnippet {
    private final CerComparisonController controller;

    private final DmxmlContext context;
    private final DmxmlContext.Block<CERDetailedStaticData> blockCerStatic;

    private String qid;

    public AbstractSelectInstrumentWidget(final CerComparisonController controller) {
        this.controller = controller;
        this.context = new DmxmlContext();
        this.blockCerStatic = this.context.addBlock("CER_DetailedStaticData"); // $NON-NLS$
    }

    private void showTypeError() {
        AbstractMainController.INSTANCE.showError(I18n.I.wrongType());
    }

    public HashMap<String, String> getCopyOfParameters() {
        return new HashMap<>();
    }

    public void setParameters(HashMap<String, String> params) {
        final QuoteWithInstrument qwi = QuoteWithInstrument.getLastSelected();
        if (qwi != null) {
            setQuote(qwi);
        }
    }

    protected void searchForQuote() {
        final SnippetConfigurationView configView = new SnippetConfigurationView(this);
        configView.addSelectedCertSymbol(this.controller.getTypeKey());
        configView.show();
    }

    public void setQuote(final QuoteWithInstrument qwi) {
        this.qid = qwi.getQuoteData().getQid();
        this.blockCerStatic.setParameter("symbol", this.qid); // $NON-NLS$
        this.context.setCancellable(false);
        this.context.issueRequest(new AsyncCallback<ResponseType>() {
            public void onFailure(Throwable caught) {
            }

            public void onSuccess(ResponseType result) {
                if (blockCerStatic.isResponseOk()) {
                    if (isValidTypeKey()) {
                        controller.add(qwi, blockCerStatic.getResult());
                    }
                    else {
                        showTypeError();
                        Firebug.debug(I18n.I.wrongType());
                        Firebug.debug("controller expects typekey " + controller.getTypeKey() + // $NON-NLS$
                                ", but " + blockCerStatic.getResult().getInstrumentdata().getIsin() + " is typekey " + // $NON-NLS$
                                blockCerStatic.getResult().getTypeKey());
                    }
                }
            }
        });
    }

    boolean isValidTypeKey() {
        return !(controller.getTypeKey() != null && !controller.getTypeKey().equals(blockCerStatic.getResult().getTypeKey()));
    }

    public String getQid() {
        return this.qid;
    }

    protected void setQid(String qid) {
        this.qid = qid;
    }

    public CerComparisonController getController() {
        return controller;
    }

    public DmxmlContext getContext() {
        return context;
    }

    public DmxmlContext.Block<CERDetailedStaticData> getBlockCerStatic() {
        return blockCerStatic;
    }
}
