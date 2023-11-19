/*
 * IlSole24OrePortraitSnippet.java
 *
 * Created on 28.08.2012 10:40:23
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.snippets.stock.ilsole24ore;

import de.marketmaker.iview.dmxml.STKIlSole24OreCompanyData;
import de.marketmaker.iview.dmxml.STKVwdItCompanyRawdata;
import de.marketmaker.iview.mmgwt.mmweb.client.data.InstrumentTypeEnum;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.AbstractSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.Snippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SnippetClass;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SnippetConfiguration;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SymbolSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;

/**
 * @author Markus Dick
 */
public class IlSole24OrePortraitSnippet
        extends AbstractSnippet<IlSole24OrePortraitSnippet, IlSole24OrePortraitSnippetView>
        implements SymbolSnippet {

    public static class Class extends SnippetClass {
        public Class() {
            super("IlSole24OrePortrait"); // $NON-NLS$
        }

        public Snippet newSnippet(DmxmlContext context, SnippetConfiguration config) {
            return new IlSole24OrePortraitSnippet(context, config);
        }
    }

    private DmxmlContext.Block<STKIlSole24OreCompanyData> block;
    //TODO: remove rawBlock!!! This is a workaround, because of block StkIlSole24OreCompanyData is buggy
    //TODO: and doesn't render pdf urls
    private DmxmlContext.Block<STKVwdItCompanyRawdata> rawblock;

    private IlSole24OrePortraitSnippet(DmxmlContext context, SnippetConfiguration config) {
        super(context, config);
        this.block = createBlock("STK_IlSole24OreCompanyData"); // $NON-NLS$
        this.rawblock = createBlock("STK_VwdItCompanyRawdata"); // $NON-NLS$
        this.block.setParameter("contentKey", config.getString("contentKey")); // $NON-NLS$

        setView(new IlSole24OrePortraitSnippetView(this));
    }

    public void setSymbol(InstrumentTypeEnum type, String symbol, String name, String... compareSymbols) {
        this.block.setParameter("symbol", symbol); // $NON-NLS$
        this.rawblock.setParameter("symbol", symbol); // $NON-NLS$
    }

    public void destroy() {
        destroyBlock(this.block);
        destroyBlock(this.rawblock);
    }

    public void updateView() {
        if (this.block.isResponseOk() && this.rawblock.isResponseOk()) {
            getView().update(this.block.getResult(), this.rawblock.getResult());
        }
        else {
            getView().setNoData();
        }
    }
}
