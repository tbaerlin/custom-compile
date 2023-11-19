/*
 * BestToolSnippet.java
 *
 * Created on 28.03.2008 16:11:58
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.snippets.stock.screener;

import de.marketmaker.iview.dmxml.STKScreenerData;
import de.marketmaker.iview.dmxml.ScreenerBaseFields;
import de.marketmaker.iview.dmxml.ScreenerFieldString;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
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
public class ScreenerStaticDataSnippet extends AbstractSnippet<ScreenerStaticDataSnippet, SnippetTableView<ScreenerStaticDataSnippet>> implements SymbolSnippet {
    public static class Class extends SnippetClass {
        public Class() {
            super("ScreenerStaticData", I18n.I.screenerStaticData()); // $NON-NLS$
        }

        public Snippet newSnippet(DmxmlContext context, SnippetConfiguration config) {
            return new ScreenerStaticDataSnippet(context, config);
        }
    }

    private DmxmlContext.Block<STKScreenerData> block;

    private ScreenerStaticDataSnippet(DmxmlContext context, SnippetConfiguration config) {
        super(context, config);
        this.block = createBlock("STK_ScreenerData"); // $NON-NLS$
        this.block.setParameter("language", config.getString("language", I18n.I.localeForScreener())); // $NON-NLS$

        this.setView(new SnippetTableView<ScreenerStaticDataSnippet>(this,
                new DefaultTableColumnModel(new TableColumn[]{
                        new TableColumn(I18n.I.type(), 0.3f, new TableCellRenderers.StringRenderer("--", "mm-snippetTable-label")),  // $NON-NLS$
                        new TableColumn(I18n.I.value(), 0.7f, TableCellRenderers.DEFAULT_RIGHT) 
                }, false)));
    }

    public void setSymbol(InstrumentTypeEnum type, String symbol, String name, String... compareSymbols) {
        this.block.setParameter("symbol", symbol); // $NON-NLS$
    }


    public void destroy() {
        destroyBlock(this.block);
    }

    public void updateView() {
        if (!this.block.isResponseOk() || this.block.getResult() == null || this.block.getResult().getBasefields() == null) {
            getView().update(DefaultTableDataModel.NULL);
            return;
        }
        final ScreenerBaseFields bf = this.block.getResult().getBasefields();
        final List<Object[]> list = new ArrayList<Object[]>();
        list.add(getLineData(bf.getName()));
        list.add(getLineData(bf.getTickersymbol()));
        list.add(getLineData(bf.getIsin()));
        list.add(getLineData(bf.getCurrency()));
        list.add(getLineData(bf.getCountry()));
        list.add(getLineData(bf.getIndex()));
        list.add(getLineData(bf.getGroup()));
        list.add(getLineData(bf.getSector()));
//        list.add(getLineData(bf.getPrice()));

        final TableDataModel tdm = DefaultTableDataModel.create(list);
        getView().update(tdm);
    }

    private Object[] getLineData(ScreenerFieldString sf) {
        return new Object[]{ sf.getHeadline(), sf.getValue() };
    }
}
