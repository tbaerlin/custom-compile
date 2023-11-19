/*
 * StaticDataSTKSnippet.java
 *
 * Created on 28.03.2008 16:11:58
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.snippets.warrant;

import de.marketmaker.iview.dmxml.WNTDerivedData;
import de.marketmaker.iview.mmgwt.mmweb.client.data.InstrumentTypeEnum;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.*;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableColumnModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableDataModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableCellRenderers;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableColumn;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;
import de.marketmaker.iview.mmgwt.mmweb.client.util.Renderer;

import java.util.ArrayList;
import java.util.List;

import de.marketmaker.iview.mmgwt.mmweb.client.I18n;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class MiniWntRatiosSnippet
        extends AbstractSnippet<MiniWntRatiosSnippet, SnippetTableView<MiniWntRatiosSnippet>>
        implements SymbolSnippet {

    public static class Class extends SnippetClass {
        public Class() {
            super("warrant.MiniWntRatios"); // $NON-NLS-0$
        }

        public Snippet newSnippet(DmxmlContext context, SnippetConfiguration config) {
            return new MiniWntRatiosSnippet(context, config);
        }
    }

    private DmxmlContext.Block<WNTDerivedData> block;

    private MiniWntRatiosSnippet(DmxmlContext context, SnippetConfiguration config) {
        super(context, config);

        this.setView(SnippetTableView.create(this, new DefaultTableColumnModel(new TableColumn[]{
                new TableColumn(I18n.I.type(), 0.4f, new TableCellRenderers.StringRenderer("--", "mm-snippetTable-label")),  // $NON-NLS-0$ $NON-NLS-1$
                new TableColumn(I18n.I.value(), 0.6f, TableCellRenderers.DEFAULT_RIGHT) 
        })));

        this.block = createBlock("WNT_DerivedData"); // $NON-NLS-0$
    }

    public void setSymbol(InstrumentTypeEnum type, String symbol, String name, String... compareSymbols) {
        this.block.setParameter("symbol", symbol); // $NON-NLS-0$
    }

    public void destroy() {
        destroyBlock(this.block);
    }

    public void updateView() {
        if (!this.block.isResponseOk()) {
            getView().update(DefaultTableDataModel.NULL);
            return;
        }

        final WNTDerivedData data = this.block.getResult();

        final List<Object[]> list = new ArrayList<Object[]>();
        list.add(new Object[]{I18n.I.leverage(), Renderer.PRICE2.render(data.getLeverage())}); 
        list.add(new Object[]{I18n.I.contangoPerYearAbbr(), Renderer.PERCENT.render(data.getContangoPerYear())}); 
        list.add(new Object[]{I18n.I.breakeven(), Renderer.PRICE2.render(data.getBreakeven())}); 
        list.add(new Object[]{I18n.I.delta(), Renderer.PRICE2.render(data.getDelta())}); 
        list.add(new Object[]{I18n.I.extrinsicValue(), Renderer.PRICE2.render(data.getExtrinsicValue())}); 
        list.add(new Object[]{I18n.I.omega(), Renderer.PRICE2.render(data.getOmega())}); 

        getView().update(DefaultTableDataModel.create(list));
    }
}
