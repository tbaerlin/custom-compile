/*
 * WntRatiosSnippet.java
 *
 * Created on 28.03.2008 16:11:58
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.snippets.warrant;

import de.marketmaker.iview.dmxml.WNTDerivedData;
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
import de.marketmaker.iview.mmgwt.mmweb.client.util.ListUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.Renderer;

import java.util.ArrayList;
import java.util.List;

import de.marketmaker.iview.mmgwt.mmweb.client.I18n;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class WntRatiosSnippet extends AbstractSnippet<WntRatiosSnippet, SnippetTableView<WntRatiosSnippet>> implements SymbolSnippet {
    public static class Class extends SnippetClass {
        public Class() {
            super("WntRatios"); // $NON-NLS-0$
        }

        public Snippet newSnippet(DmxmlContext context, SnippetConfiguration config) {
            return new WntRatiosSnippet(context, config);
        }
    }

    private final DefaultTableColumnModel columnModel;
    private final DmxmlContext.Block<WNTDerivedData> block;

    private WntRatiosSnippet(DmxmlContext context, SnippetConfiguration config) {
        super(context, config);

        columnModel = new DefaultTableColumnModel(new TableColumn[]{
            new TableColumn(I18n.I.ratio(), 0.15f, new TableCellRenderers.StringRenderer("--", "mm-snippetTable-label")),  // $NON-NLS-0$ $NON-NLS-1$
            new TableColumn(I18n.I.value(), 0.15f, TableCellRenderers.DEFAULT_RIGHT), 
            new TableColumn("", 0.05f).withCellClass("mm-snippetTable-blank"), // $NON-NLS-0$ $NON-NLS-1$
            new TableColumn(I18n.I.ratio(), 0.15f, new TableCellRenderers.StringRenderer("--", "mm-snippetTable-label")),  // $NON-NLS-0$ $NON-NLS-1$
            new TableColumn(I18n.I.value(), 0.15f, TableCellRenderers.DEFAULT_RIGHT), 
            new TableColumn("", 0.05f).withCellClass("mm-snippetTable-blank"), // $NON-NLS-0$ $NON-NLS-1$
            new TableColumn(I18n.I.ratio(), 0.15f, new TableCellRenderers.StringRenderer("--", "mm-snippetTable-label")),  // $NON-NLS-0$ $NON-NLS-1$
            new TableColumn(I18n.I.value(), 0.15f, TableCellRenderers.DEFAULT_RIGHT) 
        });
        this.setView(new SnippetTableView<WntRatiosSnippet>(this, columnModel));

        this.block = createBlock("WNT_DerivedData"); // $NON-NLS-0$
    }

    public void setSymbol(InstrumentTypeEnum type, String symbol, String name, String... compareSymbols) {
        this.block.setParameter("symbol", symbol); // $NON-NLS-0$
    }

    public void destroy() {
        destroyBlock(this.block);
    }

    public void updateView() {
        final TableDataModel tdm;
        if (this.block.isResponseOk()) {
            final WNTDerivedData data = this.block.getResult();
            final List<Object[]> list = new ArrayList<Object[]>();
            // left column
            list.add(new Object[]{I18n.I.fairPrice(), Renderer.PRICE2.render(data.getFairprice())}); 
            list.add(new Object[]{I18n.I.breakeven(), Renderer.PRICE2.render(data.getBreakeven())}); 
            list.add(new Object[]{I18n.I.leverage(), Renderer.PRICE2.render(data.getLeverage())}); 
            list.add(new Object[]{I18n.I.contango(), Renderer.PERCENT.render(data.getContango())}); 
            list.add(new Object[]{I18n.I.contangoPerYearAbbr(), Renderer.PERCENT.render(data.getContangoPerYear())}); 
            list.add(new Object[]{I18n.I.intrinsicValue(), Renderer.PRICE2.render(data.getIntrinsicValue())}); 
            list.add(new Object[]{I18n.I.extrinsicValue(), Renderer.PRICE2.render(data.getExtrinsicValue())}); 
            list.add(new Object[]{I18n.I.parity(), Renderer.PRICE2.render(data.getParity())}); 

            // middle column
            list.add(new Object[]{I18n.I.impliedVolatility(), Renderer.PERCENT.render(data.getImpliedVolatility())}); 
            list.add(new Object[]{I18n.I.moneyness(), Renderer.PRICE2.render(data.getMoneyness())}); 
            list.add(new Object[]{I18n.I.moneynessRelativeAbbr(), Renderer.PRICE2.render(data.getMoneynessRelative())}); 
            list.add(new Object[]{I18n.I.delta(), Renderer.PRICE2.render(data.getDelta())}); 
            list.add(new Object[]{I18n.I.gamma(), Renderer.PRICE2.render(data.getGamma())}); 
            list.add(new Object[]{I18n.I.omega(), Renderer.PRICE2.render(data.getOmega())}); 
            list.add(new Object[]{I18n.I.rho(), Renderer.PRICE2.render(data.getRho())}); 

            // right column
            list.add(new Object[]{I18n.I.theta(), Renderer.PRICE2.render(data.getTheta())}); 
            list.add(new Object[]{I18n.I.thetaRelativeAbbr(), Renderer.PERCENT.render(data.getThetaRelative())}); 
            list.add(new Object[]{I18n.I.thetaWeek(), Renderer.PRICE2.render(data.getTheta1W())}); 
            list.add(new Object[]{I18n.I.thetaWeekRelativeAbbr(), Renderer.PERCENT.render(data.getTheta1WRelative())}); 
            list.add(new Object[]{I18n.I.thetaMonth(), Renderer.PRICE2.render(data.getTheta1M())}); 
            list.add(new Object[]{I18n.I.thetaMonthRelativeAbbr(), Renderer.PERCENT.render(data.getTheta1MRelative())}); 
            list.add(new Object[]{I18n.I.vega(), Renderer.PRICE2.render(data.getVega())}); 

            tdm = create(list);
        }
        else {
            tdm = DefaultTableDataModel.NULL;
        }
        getView().update(tdm);
    }

    private TableDataModel create(List<Object[]> list) {
        final int columnCount = this.columnModel.getColumnCount();
        final int listCount = (columnCount + 1) / 3;
        final List<List<Object[]>> lists = ListUtil.splitByNumber(list, listCount);
        int rowCount = 0;
        for (List<Object[]> listColumn : lists) {
            final int size = listColumn.size();
            if (rowCount < size) {
                rowCount = size;
            }
        }
        final DefaultTableDataModel tdm = new DefaultTableDataModel(rowCount, columnCount);
        int column = 0;
        for (List<Object[]> listColumn : lists) {
            int row = 0;
            for (Object[] objects : listColumn) {
                tdm.setValueAt(row, column, objects[0]);
                tdm.setValueAt(row, column + 1, objects[1]);
                row++;
            }
            column += 3;
        }
        return tdm;
    }
}
