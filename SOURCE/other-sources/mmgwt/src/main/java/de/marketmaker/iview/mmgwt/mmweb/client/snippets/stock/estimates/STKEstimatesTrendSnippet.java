/*
 * STKEstimatesTrendSnippet.java
 *
 * Created on 17.09.2008 13:14:38
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */

package de.marketmaker.iview.mmgwt.mmweb.client.snippets.stock.estimates;

import de.marketmaker.iview.dmxml.STKEstimates;
import de.marketmaker.iview.dmxml.STKHistoricEstimates;
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
import de.marketmaker.iview.mmgwt.mmweb.client.table.RowData;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableCellRenderers;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableColumn;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;
import de.marketmaker.iview.mmgwt.mmweb.client.util.Renderer;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Ulrich Maurer
 */
public class STKEstimatesTrendSnippet extends
        AbstractSnippet<STKEstimatesTrendSnippet, SnippetTableView<STKEstimatesTrendSnippet>> implements
        SymbolSnippet {
    public static class Class extends SnippetClass {
        public Class() {
            super("STKEstimatesTrend", I18n.I.analystsTrend()); // $NON-NLS-0$
        }

        public Snippet newSnippet(DmxmlContext context, SnippetConfiguration config) {
            return new STKEstimatesTrendSnippet(context, config);
        }
    }

    private DmxmlContext.Block<STKEstimates> blockEstimates;

    private DmxmlContext.Block<STKHistoricEstimates> blockHistoricEstimates;


    protected STKEstimatesTrendSnippet(DmxmlContext context, SnippetConfiguration config) {
        super(context, config);
        this.blockEstimates = context.addBlock("STK_Estimates"); // $NON-NLS-0$
        this.blockEstimates.setParameters("year", new String[]{"fy0", "fy1", "fy2"}); // $NON-NLS-0$ $NON-NLS-1$ $NON-NLS-2$ $NON-NLS-3$
        this.blockHistoricEstimates = context.addBlock("STK_HistoricEstimates"); // $NON-NLS-0$

        this.setView(new SnippetTableView<STKEstimatesTrendSnippet>(this,
                new DefaultTableColumnModel(new TableColumn[]{
                        new TableColumn("", 0.4f, TableCellRenderers.LABEL), // $NON-NLS-0$
                        new TableColumn(I18n.I.monthsAgo(1), 0.2f, TableCellRenderers.DEFAULT_RIGHT), 
                        new TableColumn(I18n.I.weeksAgo(1), 0.2f, TableCellRenderers.DEFAULT_RIGHT), 
                        new TableColumn(I18n.I.current(), 0.2f, TableCellRenderers.DEFAULT_RIGHT) 
                })));

    }

    public void destroy() {
        destroyBlock(this.blockEstimates);
        destroyBlock(this.blockHistoricEstimates);
    }

    public void setSymbol(InstrumentTypeEnum type, String symbol, String name, String... compareSymbols) {
        this.blockEstimates.setParameter("symbol", symbol); // $NON-NLS-0$
        this.blockHistoricEstimates.setParameter("symbol", symbol); // $NON-NLS-0$
    }

    public void updateView() {
        if (!(this.blockEstimates.isResponseOk() && this.blockHistoricEstimates.isResponseOk())) {
            getView().update(DefaultTableDataModel.NULL);
            return;
        }
        final STKEstimates estimates = this.blockEstimates.getResult();
        final STKHistoricEstimates historic = this.blockHistoricEstimates.getResult();
        final List<RowData> list = new ArrayList<RowData>(7);
        list.add(new RowData(I18n.I.evaluation1(),
                Renderer.PRICE_MAX2.render(historic.getRecommendation1M()),
                Renderer.PRICE_MAX2.render(historic.getRecommendation1W()),
                Renderer.PRICE_MAX2.render(estimates.getRecommendation())));
        list.add(new RowData(I18n.I.analystsCount(), 
                historic.getNumTotal1M(),
                historic.getNumTotal1W(),
                estimates.getNumTotal()));
        list.add(new RowData(I18n.I.buy(), 
                historic.getNumBuy1M(),
                historic.getNumBuy1W(),
                estimates.getNumBuy()));
        list.add(new RowData(I18n.I.overweight(), 
                historic.getNumOverweight1M(),
                historic.getNumOverweight1W(),
                estimates.getNumOverweight()));
        list.add(new RowData(I18n.I.hold(), 
                historic.getNumHold1M(),
                historic.getNumHold1W(),
                estimates.getNumHold()));
        list.add(new RowData(I18n.I.underweight(), 
                historic.getNumUnderweight1M(),
                historic.getNumUnderweight1W(),
                estimates.getNumUnderweight()));
        list.add(new RowData(I18n.I.sell(), 
                historic.getNumSell1M(),
                historic.getNumSell1W(),
                estimates.getNumSell()));

        getView().update(DefaultTableDataModel.createWithRowData(list));
    }
}
