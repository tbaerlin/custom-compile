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
import de.marketmaker.iview.mmgwt.mmweb.client.data.ExtendBarData;
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
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;

/**
 * @author Ulrich Maurer
 */
public class STKEstimatesHistorySnippet extends
        AbstractSnippet<STKEstimatesHistorySnippet, SnippetTableView<STKEstimatesHistorySnippet>> implements
        SymbolSnippet {
    public static class Class extends SnippetClass {
        public Class() {
            super("STKEstimatesHistory", I18n.I.estimationHistory()); // $NON-NLS-0$
        }

        public Snippet newSnippet(DmxmlContext context, SnippetConfiguration config) {
            return new STKEstimatesHistorySnippet(context, config);
        }
    }

    private DmxmlContext.Block<STKEstimates> blockEstimates;

    private DmxmlContext.Block<STKHistoricEstimates> blockHistoricEstimates;


    protected STKEstimatesHistorySnippet(DmxmlContext context, SnippetConfiguration config) {
        super(context, config);
        this.blockEstimates = context.addBlock("STK_Estimates"); // $NON-NLS-0$
        this.blockEstimates.setParameters("year", new String[]{"fy0", "fy1", "fy2"}); // $NON-NLS-0$ $NON-NLS-1$ $NON-NLS-2$ $NON-NLS-3$
        this.blockHistoricEstimates = context.addBlock("STK_HistoricEstimates"); // $NON-NLS-0$

        this.setView(new SnippetTableView<STKEstimatesHistorySnippet>(this,
                new DefaultTableColumnModel(new TableColumn[]{
                        new TableColumn(I18n.I.estimationTime(), 0.3f, TableCellRenderers.LABEL), 
                        new TableColumn(I18n.I.evaluation1(), 0.3f, TableCellRenderers.PRICE_MAX2),
                        new TableColumn("", 0.4f, TableCellRenderers.EXTEND_BAR_LEFT) // $NON-NLS-0$
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

        final DefaultTableDataModel tdm = new DefaultTableDataModel(8, 3);
        int row = -1;
        tdm.setValuesAt(++row, new Object[]{I18n.I.monthsAgo(4), historic.getRecommendation4M(), getExtend(historic.getRecommendation4M())}); 
        tdm.setValuesAt(++row, new Object[]{I18n.I.monthsAgo(3), historic.getRecommendation3M(), getExtend(historic.getRecommendation3M())}); 
        tdm.setValuesAt(++row, new Object[]{I18n.I.monthsAgo(2), historic.getRecommendation2M(), getExtend(historic.getRecommendation2M())}); 
        tdm.setValuesAt(++row, new Object[]{I18n.I.monthsAgo(1), historic.getRecommendation1M(), getExtend(historic.getRecommendation1M())}); 
        tdm.setValuesAt(++row, new Object[]{I18n.I.weeksAgo(3), historic.getRecommendation3W(), getExtend(historic.getRecommendation3W())}); 
        tdm.setValuesAt(++row, new Object[]{I18n.I.weeksAgo(2), historic.getRecommendation2W(), getExtend(historic.getRecommendation2W())}); 
        tdm.setValuesAt(++row, new Object[]{I18n.I.weeksAgo(1), historic.getRecommendation1W(), getExtend(historic.getRecommendation1W())}); 
        tdm.setValuesAt(++row, new Object[]{I18n.I.current(), estimates.getRecommendation(), getExtend(estimates.getRecommendation())}); 
        getView().update(tdm);
    }

    private ExtendBarData getExtend(String sRec) {
        if (sRec == null) {
            return null;
        }
        final float rec = Float.parseFloat(sRec);
        final ExtendBarData extend = new ExtendBarData(3f - rec);
        extend.setMaxValue(2f);
        return extend;
    }
}
