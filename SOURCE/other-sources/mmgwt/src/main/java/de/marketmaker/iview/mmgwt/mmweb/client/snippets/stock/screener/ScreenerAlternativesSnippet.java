/*
 * BestToolSnippet.java
 *
 * Created on 28.03.2008 16:11:58
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.snippets.stock.screener;

import de.marketmaker.iview.dmxml.InstrumentQuoteElement;
import de.marketmaker.iview.dmxml.STKScreenerAlternatives;
import de.marketmaker.iview.dmxml.STKScreenerData;
import de.marketmaker.iview.dmxml.ScreenerBaseFields;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.QuoteWithInstrument;
import de.marketmaker.iview.mmgwt.mmweb.client.data.InstrumentTypeEnum;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.AbstractSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.Snippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SnippetClass;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SnippetConfiguration;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SnippetTableView;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SymbolSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableColumnModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableDataModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableCellRenderers;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableColumn;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableDataModel;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class ScreenerAlternativesSnippet extends
        AbstractSnippet<ScreenerAlternativesSnippet, SnippetTableView<ScreenerAlternativesSnippet>> implements
        SymbolSnippet {
    private DefaultTableColumnModel columnModel;

    public static class Class extends SnippetClass {
        public Class() {
            super("ScreenerAlternatives", I18n.I.alternativeScreeners()); // $NON-NLS$
        }

        public Snippet newSnippet(DmxmlContext context, SnippetConfiguration config) {
            return new ScreenerAlternativesSnippet(context, config);
        }
    }

    private DmxmlContext.Block<STKScreenerData> block;

    private DmxmlContext.Block<STKScreenerAlternatives> blockAlternatives;

    private ScreenerAlternativesSnippet(DmxmlContext context, SnippetConfiguration config) {
        super(context, config);
        this.block = createBlock("STK_ScreenerData"); // $NON-NLS$
        this.block.setParameter("language", config.getString("language", I18n.I.localeForScreener())); // $NON-NLS$
        this.blockAlternatives = createBlock("STK_ScreenerAlternatives"); // $NON-NLS$

        this.columnModel = new DefaultTableColumnModel(new TableColumn[]{
                new TableColumn(I18n.I.value(), 1f, TableCellRenderers.DEFAULT) 
        }, false);
        this.setView(new SnippetTableView<ScreenerAlternativesSnippet>(this, this.columnModel));
    }

    public void setSymbol(InstrumentTypeEnum type, String symbol, String name, String... compareSymbols) {
        this.block.setParameter("symbol", symbol); // $NON-NLS$
        this.blockAlternatives.setParameter("symbol", symbol); // $NON-NLS$
    }


    public void destroy() {
        destroyBlock(this.block);
        destroyBlock(this.blockAlternatives);
    }

    public void updateView() {
        if (!this.block.isResponseOk() || this.block.getResult() == null || this.block.getResult().getBasefields() == null) {
            getView().update(DefaultTableDataModel.NULL);
            return;
        }
        final ScreenerBaseFields base = this.block.getResult().getBasefields();
        final STKScreenerAlternatives alt = this.blockAlternatives.getResult();
        final List<Object[]> list = new ArrayList<Object[]>();
        int row = -1;
        final TableColumn tableColumn = columnModel.getTableColumn(0);

        list.add(new Object[]{I18n.I.alternativeWithinCountry(base.getCountry().getValue())}); 
        tableColumn.setRowRenderer(++row, TableCellRenderers.LABEL);
        for (InstrumentQuoteElement e : alt.getCountry().getElement()) {
            tableColumn.setRowRenderer(++row, null);
            list.add(new Object[]{new QuoteWithInstrument(e.getInstrumentdata(), e.getQuotedata())});
        }

        list.add(new Object[]{I18n.I.alternativeWithinGroup(base.getGroup().getValue())}); 
        tableColumn.setRowRenderer(++row, TableCellRenderers.LABEL);
        for (InstrumentQuoteElement e : alt.getGroup().getElement()) {
            tableColumn.setRowRenderer(++row, null);
            list.add(new Object[]{new QuoteWithInstrument(e.getInstrumentdata(), e.getQuotedata())});
        }

        final TableDataModel tdm = DefaultTableDataModel.create(list);
        getView().update(tdm);
    }
}
