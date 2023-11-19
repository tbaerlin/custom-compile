/*
 * FndRatiosSnippet.java
 *
 * Created on 02.02.2009 00:17:59
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.snippets.fund;

import java.util.ArrayList;
import java.util.List;

import de.marketmaker.iview.dmxml.FNDRatioData;
import de.marketmaker.iview.dmxml.FNDStaticData;
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

import de.marketmaker.iview.mmgwt.mmweb.client.I18n;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class FndRatiosSnippet extends
        AbstractSnippet<FndRatiosSnippet, SnippetTableView<FndRatiosSnippet>> implements
        SymbolSnippet {
    public static class Class extends SnippetClass {
        public Class() {
            super("FndRatios"); // $NON-NLS-0$
        }

        public Snippet newSnippet(DmxmlContext context, SnippetConfiguration config) {
            return new FndRatiosSnippet(context, config);
        }
    }

    private final DefaultTableColumnModel columnModel;

    private final DmxmlContext.Block<FNDRatioData> block;

    private final DmxmlContext.Block<FNDStaticData> blockStatic;

    private FndRatiosSnippet(DmxmlContext context, SnippetConfiguration config) {
        super(context, config);

        columnModel = new DefaultTableColumnModel(new TableColumn[]{
                new TableColumn(I18n.I.ratio(), 0.15f, new TableCellRenderers.StringRenderer("--", "mm-snippetTable-label")),  // $NON-NLS-0$ $NON-NLS-1$
                new TableColumn(I18n.I.value(), 0.15f, TableCellRenderers.DEFAULT_RIGHT), 
                new TableColumn("", 0.05f).withCellClass("mm-snippetTable-blank"), // $NON-NLS-0$ $NON-NLS-1$
                new TableColumn(I18n.I.ratio(), 0.15f, new TableCellRenderers.StringRenderer("--", "mm-snippetTable-label")),  // $NON-NLS-0$ $NON-NLS-1$
                new TableColumn(I18n.I.value(), 0.15f, TableCellRenderers.DEFAULT_RIGHT), 
        });
        this.setView(new SnippetTableView<FndRatiosSnippet>(this, columnModel));

        this.block = createBlock("FND_RatioData"); // $NON-NLS-0$
        this.blockStatic = createBlock("FND_StaticData"); // $NON-NLS-0$
    }

    public void setSymbol(InstrumentTypeEnum type, String symbol, String name, String... compareSymbols) {
        this.block.setParameter("symbol", symbol); // $NON-NLS-0$
        this.blockStatic.setParameter("symbol", symbol); // $NON-NLS-0$
    }

    public void destroy() {
        destroyBlock(this.block);
        destroyBlock(this.blockStatic);
    }

    public void updateView() {
        final TableDataModel tdm;
        if (this.block.isResponseOk()) {
            final FNDRatioData data = this.block.getResult();
            final List<Object[]> list = new ArrayList<Object[]>();
            // left column
            list.add(new Object[]{I18n.I.jensensAlphaYear(1), Renderer.PERCENT.render(data.getAlpha1Y())});
            list.add(new Object[]{I18n.I.jensensAlphaYear(3), Renderer.PERCENT.render(data.getAlpha3Y())});
            list.add(new Object[]{I18n.I.jensensAlphaYear(5), Renderer.PERCENT.render(data.getAlpha5Y())});
            list.add(new Object[]{I18n.I.sharpeRatioWithPeriod(1), Renderer.PRICE.render(data.getSharperatio1Y())});
            list.add(new Object[]{I18n.I.sharpeRatioWithPeriod(3), Renderer.PRICE.render(data.getSharperatio3Y())});
            list.add(new Object[]{I18n.I.sharpeRatioWithPeriod(5), Renderer.PRICE.render(data.getSharperatio5Y())});
            list.add(new Object[]{I18n.I.treynor(1), Renderer.PRICE.render(data.getTreynor1Y())}); 
            list.add(new Object[]{I18n.I.treynor(3), Renderer.PRICE.render(data.getTreynor3Y())}); 
            list.add(new Object[]{I18n.I.treynor(5), Renderer.PRICE.render(data.getTreynor5Y())}); 
            list.add(new Object[]{I18n.I.informationRatio(1), Renderer.PRICE.render(data.getInformationRatio1Y())}); 
            list.add(new Object[]{I18n.I.informationRatio(3), Renderer.PRICE.render(data.getInformationRatio3Y())}); 
            list.add(new Object[]{I18n.I.informationRatio(5), Renderer.PRICE.render(data.getInformationRatio5Y())}); 
            list.add(new Object[]{I18n.I.sterlingRatio(1), Renderer.PRICE.render(data.getSterlingRatio1Y())}); 
            list.add(new Object[]{I18n.I.sterlingRatio(3), Renderer.PRICE.render(data.getSterlingRatio3Y())}); 
            list.add(new Object[]{I18n.I.sterlingRatio(5), Renderer.PRICE.render(data.getSterlingRatio5Y())}); 
            list.add(new Object[]{I18n.I.trackingError(1), Renderer.PERCENT_NO_SHIFT.render(data.getTrackingError1Y())});
            list.add(new Object[]{I18n.I.trackingError(3), Renderer.PERCENT_NO_SHIFT.render(data.getTrackingError3Y())});
            list.add(new Object[]{I18n.I.trackingError(5), Renderer.PERCENT_NO_SHIFT.render(data.getTrackingError5Y())});
            list.add(new Object[]{I18n.I.maximumLoss(), Renderer.PERCENT.render(data.getMaximumloss3Y())}); 
            final String ml = data.getMaximumlossmonths3Y();
            final String mlText;
            if (ml == null) {
                mlText = "--"; // $NON-NLS-0$
            }
            else {
                mlText = I18n.I.nMonths(Integer.parseInt(ml)); 
            }
            list.add(new Object[]{I18n.I.longestLossPeriod(), mlText}); 
            list.add(new Object[]{I18n.I.stockProfit(), Renderer.PRICE.render(data.getStockprofit())}); 
            list.add(new Object[]{I18n.I.estateProfit(), Renderer.PRICE.render(data.getEstateprofit())}); 
            list.add(new Object[]{I18n.I.interimProfit(), Renderer.PRICE.render(data.getInterimprofit())}); 

            if (this.blockStatic.isResponseOk()) {
                final FNDStaticData staticData = this.blockStatic.getResult();
                list.add(new Object[]{I18n.I.duration(), Renderer.PRICE2.render(staticData.getDuration())}); 
                list.add(new Object[]{I18n.I.modifiedDuration(), Renderer.PRICE2.render(staticData.getModifiedDuration())});                 
            }

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
