/*
 * PmSecuritySnippet.java
 *
 * Created on 07.06.13 16:41
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.snippets;

import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.data.Instrument;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.AbstractSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.Snippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SnippetClass;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SnippetConfiguration;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;
import de.marketmaker.iview.pmxml.MMTalkResponse;
import de.marketmaker.iview.pmxml.ShellMMType;

/**
 * @author Markus Dick
 */
public class PmSecuritySnippet
        extends AbstractSnippet<PmSecuritySnippet, PmSecuritySnippetView<PmSecuritySnippet>>
        implements SecurityIdSnippet {

    public static class Class extends SnippetClass {
        public Class() {
            super("PmSecuritySnippet", I18n.I.pmInstrumentData()); // $NON-NLS$
        }

        public Snippet newSnippet(DmxmlContext context, SnippetConfiguration config) {
            return new PmSecuritySnippet(context, config);
        }
    }

    private Instrument.Talker instrumentMmTalker;
    private final DmxmlContext.Block<MMTalkResponse> block;

    public PmSecuritySnippet(DmxmlContext context, SnippetConfiguration configuration) {
        super(context, configuration);
        this.block = this.context.addBlock("PM_MMTalk"); // $NON-NLS$
        setView(new PmSecuritySnippetView<PmSecuritySnippet>(this));
    }

    @Override
    public void destroy() {
        destroyBlock(this.block);
    }

    @Override
    public void updateView() {
        if (this.block.isResponseOk()) {
            final Instrument instrument = this.instrumentMmTalker.createResultObject(this.block.getResult());
            if(instrument == null) {
                Firebug.warn("<PmPriceTeaserSnippet.updateView> response ok but instrument is null");
                getView().showNoData();
                return;
            }
            getView().update(instrument);
            return;
        }

        Firebug.warn("<PmSecuritySnippet.updateView> response not ok: " + this.block.getError().getCode());
        getView().showNoData();
    }

    @Override
    public void setSecurityId(ShellMMType shellMMType, String securityId) {
        this.instrumentMmTalker = new Instrument.Talker(shellMMType);
        this.instrumentMmTalker.setInstrumentId(securityId);
        this.block.setParameter(this.instrumentMmTalker.createRequest());
    }
}
