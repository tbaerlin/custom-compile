/*
 * BNDRatiosSnippet.java
 *
 * Created on 28.03.2008 16:11:58
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.snippets.bond;

import de.marketmaker.iview.dmxml.BNDRatioData;
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
import de.marketmaker.iview.mmgwt.mmweb.client.util.Renderer;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class BNDRatiosSnippet extends AbstractSnippet<BNDRatiosSnippet, SnippetTableView<BNDRatiosSnippet>> implements SymbolSnippet {
    public static class Class extends SnippetClass {
        public Class() {
            super("BNDRatios", I18n.I.bondRatios()); // $NON-NLS-0$
        }

        public Snippet newSnippet(DmxmlContext context, SnippetConfiguration config) {
            return new BNDRatiosSnippet(context, config);
        }
    }

    private final DmxmlContext.Block<BNDRatioData> block;

    private BNDRatiosSnippet(DmxmlContext context, SnippetConfiguration config) {
        super(context, config);

        this.setView(new SnippetTableView<>(this,
                new DefaultTableColumnModel(new TableColumn[]{
                        new TableColumn(I18n.I.type(), 0.5f, new TableCellRenderers.StringRenderer("--", "mm-snippetTable-label")),  // $NON-NLS-0$ $NON-NLS-1$
                        new TableColumn(I18n.I.value(), 0.5f, TableCellRenderers.DEFAULT_RIGHT) 
                })));

        this.block = createBlock("BND_RatioData"); // $NON-NLS-0$
        setSymbol(InstrumentTypeEnum.BND, config.getString("symbol", null), null); // $NON-NLS-0$
    }

    public void setSymbol(InstrumentTypeEnum type, String symbol, String name, String... compareSymbols) {
        this.block.setEnabled(symbol != null);
        this.block.setParameter("symbol", symbol); // $NON-NLS-0$
    }

    public void destroy() {
        destroyBlock(this.block);
    }

    public void updateView() {
        final TableDataModel tdm;
        if (this.block.isResponseOk()) {
            final BNDRatioData data = this.block.getResult();
            final List<Object[]> list = new ArrayList<>();
            list.add(new Object[]{I18n.I.yieldPerYearAbbr(), Renderer.PERCENT.render(data.getYieldRelativePerYear())}); 
            list.add(new Object[]{I18n.I.accruedInterest(), Renderer.PERCENT_NO_SHIFT.render(data.getBrokenPeriodInterest())});
            list.add(new Object[]{I18n.I.duration(), Renderer.PRICE.render(data.getDuration())}); 
            list.add(new Object[]{I18n.I.modifiedDuration(), Renderer.PRICE.render(data.getModifiedDuration())}); 
            list.add(new Object[]{I18n.I.convexity(), Renderer.PRICE.render(data.getConvexity())}); 
            list.add(new Object[]{I18n.I.interestRateElasticity(), Renderer.PRICE.render(data.getInterestRateElasticity())}); 
            list.add(new Object[]{I18n.I.basePointValue(), Renderer.PRICE.render(data.getBasePointValue())}); 

            tdm = DefaultTableDataModel.create(list);
        }
        else {
            tdm = DefaultTableDataModel.NULL;
        }
        getView().update(tdm);
    }
}
