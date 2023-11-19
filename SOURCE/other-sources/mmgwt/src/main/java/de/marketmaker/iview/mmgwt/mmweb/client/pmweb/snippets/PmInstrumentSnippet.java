/*
 * PmInstrumentSnippet.java
 *
 * Created on 07.05.13 13:51
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.snippets;

import com.google.gwt.user.client.rpc.AsyncCallback;
import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.iview.dmxml.MSCQuoteMetadata;
import de.marketmaker.iview.dmxml.ResponseType;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.data.Instrument;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.data.InstrumentMetadata;
import de.marketmaker.iview.mmgwt.mmweb.client.portrait.MetadataAware;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.AbstractSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.Snippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SnippetClass;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SnippetConfiguration;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;
import de.marketmaker.iview.pmxml.MMTalkResponse;

/**
 * @author Michael Lösch
 */
public class PmInstrumentSnippet extends AbstractSnippet<PmInstrumentSnippet, PmSecuritySnippetView<PmInstrumentSnippet>>
        implements MetadataAware {

    public static class Class extends SnippetClass {
        public Class() {
            super("PmInstrumentSnippet", I18n.I.pmInstrumentData()); // $NON-NLS$
        }

        public Snippet newSnippet(DmxmlContext context, SnippetConfiguration config) {
            return new PmInstrumentSnippet(context, config);
        }
    }

    private Instrument.Talker mmtalker;
    private final DmxmlContext.Block<MMTalkResponse> block;
    private final DmxmlContext myContext;

    public PmInstrumentSnippet(DmxmlContext context, SnippetConfiguration configuration) {
        super(context, configuration);

        this.myContext = new DmxmlContext();
        this.myContext.setCancellable(false);
        this.block = this.myContext.addBlock("PM_MMTalk"); // $NON-NLS$
        setView(new PmSecuritySnippetView<>(this));
    }

    @Override
    public boolean isMetadataNeeded() {
        return true;
    }

    @Override
    public void onMetadataAvailable(MSCQuoteMetadata metadata) {
        final DmxmlContext metaContext = new DmxmlContext();
        metaContext.setCancellable(false);
        final DmxmlContext.Block<MMTalkResponse> metaBlock = metaContext.addBlock("PM_MMTalk");// $NON-NLS$
        final InstrumentMetadata.Talker metaTalker = new InstrumentMetadata.Talker();
        final String iid = metadata.getInstrumentdata().getIid();
        metaTalker.setSecurityId(iid.substring(0, iid.indexOf(".iid"))); // $NON-NLS$
        metaBlock.setParameter(metaTalker.createRequest());
        metaBlock.issueRequest(new AsyncCallback<ResponseType>() {
            @Override
            public void onSuccess(ResponseType result) {
                if(!metaBlock.isResponseOk()) {
                    updateOnFailure();
                    Firebug.debug("<PmInstrumentSnippet.onMetadataAvailable> response not ok: " + metaBlock.getError().getDescription());
                    return;
                }

                final InstrumentMetadata instrumentMetadata = metaTalker.createResultObject(metaBlock.getResult());
                if(instrumentMetadata == null) {
                    updateOnFailure();
                    Firebug.debug("<PmInstrumentSnippet.onMetadataAvailable> response ok but instrument metadata is null");
                    return;
                }
                requestInstrumentData(instrumentMetadata);
            }

            @Override
            public void onFailure(Throwable caught) {
                updateOnFailure();
                Firebug.warn("<PmInstrumentSnippet.onMetadataAvailable> requesting instrument's pm metadata failed", caught);
            }
        });
    }

    private void requestInstrumentData(InstrumentMetadata metadata) {
        this.mmtalker = new Instrument.Talker(metadata.getType());
        this.mmtalker.setInstrumentId(metadata.getSecurityId());
        this.block.setParameter(this.mmtalker.createRequest());
        this.block.setEnabled(true);
        this.myContext.issueRequest(new AsyncCallback<ResponseType>() {
            @Override
            public void onSuccess(ResponseType result) {
                update();
            }

            @Override
            public void onFailure(Throwable caught) {
                updateOnFailure();
                Firebug.warn("<PmInstrumentSnippet.requestInstrumentData> requesting instrument's pm data failed", caught);
            }
        });
    }

    @Override
    public void destroy() {
        destroyBlock(this.block);
    }

    @Override
    public void updateView() {
        //do nothing
    }

    private void updateOnFailure() {
        getView().showNoData();
    }

    private void update() {
        if (this.block.isResponseOk()) {
            final Instrument instrument = this.mmtalker.createResultObject(this.block.getResult());
            if(instrument == null) {
                Firebug.warn("<PmInstrumentSnippet.update> response ok but instrument is null");
                updateOnFailure();
                return;
            }
            getView().update(instrument);
            return;
        }
        Firebug.debug("<PmInstrumentSnippet.update> response not ok: " + this.block.getError().getCode());
        updateOnFailure();
    }
}