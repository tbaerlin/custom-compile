/*
 * StaticDataSTKSnippet.java
 *
 * Created on 28.03.2008 16:11:58
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.snippets.stock;

import de.marketmaker.iview.dmxml.INDListByQuote;
import de.marketmaker.iview.dmxml.IdentifierDataWithPrio;
import de.marketmaker.iview.dmxml.STKDates;
import de.marketmaker.iview.dmxml.STKDatesElement;
import de.marketmaker.iview.dmxml.STKKeyEstimates;
import de.marketmaker.iview.dmxml.STKStaticData;
import de.marketmaker.iview.mmgwt.mmweb.client.data.InstrumentTypeEnum;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.AbstractSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.PriceSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.Snippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SnippetClass;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SnippetConfiguration;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SymbolSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;

import java.util.List;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class StaticDataSTKSnippet extends AbstractSnippet<StaticDataSTKSnippet, StaticDataSTKSnippetView> implements SymbolSnippet {
    private PriceSnippet priceSnippet;


    public static class Class extends SnippetClass {
        public Class() {
            super("stock.StaticData"); // $NON-NLS-0$
        }

        public Snippet newSnippet(DmxmlContext context, SnippetConfiguration config) {
            return new StaticDataSTKSnippet(context, config);
        }

        @Override
        protected void addDefaultParameters(SnippetConfiguration config) {
            config.put("width", DEFAULT_SNIPPET_WIDTH); // $NON-NLS$
        }
    }

    private DmxmlContext.Block<STKStaticData> blockStatic;
    private DmxmlContext.Block<STKKeyEstimates> blockKeyEstimates;
    private DmxmlContext.Block<STKDates> blockDates;
    private DmxmlContext.Block<INDListByQuote> blockIndizes;

    private StaticDataSTKSnippet(DmxmlContext context, SnippetConfiguration config) {
        super(context, config);

        this.setView(new StaticDataSTKSnippetView(this));

        this.blockStatic = createBlock("STK_StaticData"); // $NON-NLS-0$
        this.blockKeyEstimates = createBlock("STK_KeyEstimates"); // $NON-NLS-0$
        this.blockDates = createBlock("STK_Dates"); // $NON-NLS-0$
        this.blockIndizes = createBlock("IND_ListByQuote"); // $NON-NLS-0$
        this.blockIndizes.setParameter("forInstrument", "true"); // $NON-NLS-0$ $NON-NLS-1$
        setSymbol(InstrumentTypeEnum.STK, "314085.qid", null); // $NON-NLS-0$
    }

    public void setPriceSnippet(PriceSnippet priceSnippet) {
        this.priceSnippet = priceSnippet;
    }

    public void setSymbol(InstrumentTypeEnum type, String symbol, String name, String... compareSymbols) {
        this.blockStatic.setParameter("symbol", symbol); // $NON-NLS-0$
        this.blockKeyEstimates.setParameter("symbol", symbol); // $NON-NLS-0$
        this.blockDates.setParameter("symbol", symbol); // $NON-NLS-0$
        this.blockIndizes.setParameter("symbol", symbol); // $NON-NLS-0$
    }

    public void destroy() {
        destroyBlock(this.blockStatic);
        destroyBlock(this.blockKeyEstimates);
        destroyBlock(this.blockDates);
        destroyBlock(this.blockIndizes);
    }

    public void updateView() {
        final List<STKDatesElement> dates = this.blockDates.isResponseOk() ? this.blockDates.getResult().getElement() : null;
        final List<IdentifierDataWithPrio> list = this.blockIndizes.isResponseOk() ? this.blockIndizes.getResult().getIndex() : null;
        getView().update(this.blockStatic.getResult(), this.blockKeyEstimates.getResult(), this.priceSnippet.getResult(), dates, list);
    }
}
