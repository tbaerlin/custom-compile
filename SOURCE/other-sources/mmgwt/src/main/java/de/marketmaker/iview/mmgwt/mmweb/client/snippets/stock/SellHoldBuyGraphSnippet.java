/*
 * PdfSnippet.java
 *
 * Created on 17.06.2008 12:35:45
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */

package de.marketmaker.iview.mmgwt.mmweb.client.snippets.stock;

import de.marketmaker.iview.dmxml.RSCAggregatedFinder;
import de.marketmaker.iview.dmxml.RSCAggregatedFinderElement;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SellHoldBuy;
import de.marketmaker.iview.mmgwt.mmweb.client.data.InstrumentTypeEnum;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.AbstractSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.Snippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SnippetClass;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SnippetConfiguration;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SymbolSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;

import java.util.List;

/**
 * @author Ulrich Maurer
 */
public class SellHoldBuyGraphSnippet extends AbstractSnippet<SellHoldBuyGraphSnippet, SellHoldBuyGraphSnippetView> implements SymbolSnippet {
    public static class Class extends SnippetClass {
        public Class() {
            super("SellHoldBuyGraph"); // $NON-NLS-0$
        }

        public Snippet newSnippet(DmxmlContext context, SnippetConfiguration config) {
            return new SellHoldBuyGraphSnippet(context, config);
        }
    }

    private DmxmlContext.Block<RSCAggregatedFinder> block;


    private SellHoldBuyGraphSnippet(DmxmlContext context, SnippetConfiguration config) {
        super(context, config);

        setView(new SellHoldBuyGraphSnippetView(this));

        this.block = context.addBlock("RSC_Finder"); // $NON-NLS-0$
        this.block.setParameter("resultType", "aggregated"); // $NON-NLS-0$ $NON-NLS-1$
    }

    public void setSymbol(InstrumentTypeEnum type, String symbol, String name, String... compareSymbols) {
        this.block.setParameter("query", "symbol==" + symbol); // $NON-NLS-0$ $NON-NLS-1$
    }

    public void destroy() {
    }

    public void updateView() {
        if (!this.block.isResponseOk()) {
            getView().update(null);
            return;
        }

        final List<RSCAggregatedFinderElement> list = this.block.getResult().getElement();
        if (list.isEmpty()) {
            getView().update(null);
            return;
        }
        getView().update(new SellHoldBuy(list.get(0)));
    }
}
