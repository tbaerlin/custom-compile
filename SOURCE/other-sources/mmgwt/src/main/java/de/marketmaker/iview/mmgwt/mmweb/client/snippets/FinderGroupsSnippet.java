/*
 * FinderGroupsSnippet.java
 *
 * Created on 28.03.2008 16:11:58
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.snippets;

import com.google.gwt.dom.client.Element;
import de.marketmaker.iview.dmxml.FinderGroupCell;
import de.marketmaker.iview.dmxml.FinderGroupItem;
import de.marketmaker.iview.dmxml.FinderGroupRow;
import de.marketmaker.iview.dmxml.FinderGroupTable;
import de.marketmaker.iview.dmxml.MinMaxAvgElement;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.QuoteWithInstrument;
import de.marketmaker.iview.mmgwt.mmweb.client.data.InstrumentTypeEnum;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableColumnModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableDataModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableCellRenderers;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableColumn;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableDataModel;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;
import de.marketmaker.iview.mmgwt.mmweb.client.util.LinkContext;
import de.marketmaker.iview.mmgwt.mmweb.client.util.LinkListener;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class FinderGroupsSnippet extends
        AbstractSnippet<FinderGroupsSnippet, SnippetTableView<FinderGroupsSnippet>>
        implements LinkListener {

    public static class Class extends SnippetClass {
        public Class() {
            super("FinderGroups"); // $NON-NLS-0$
        }

        public Snippet newSnippet(DmxmlContext context, SnippetConfiguration config) {
            return new FinderGroupsSnippet(context, config);
        }
    }

    private DmxmlContext.Block<FinderGroupTable> block;

    private List<SymbolSnippet> symbolSnippets = new ArrayList<>();

    private FinderGroupsSnippet(DmxmlContext context, SnippetConfiguration config) {
        super(context, config);

        this.block = createBlock("MSC_FinderGroups"); // $NON-NLS-0$
        this.block.setParameter("type", "FND"); // $NON-NLS-0$ $NON-NLS-1$
        this.block.setParameter("primaryField", "fundtype"); // $NON-NLS-0$ $NON-NLS-1$
        this.block.setParameter("evaluateField", "bviperformance1y"); // $NON-NLS-0$ $NON-NLS-1$
        this.block.setParameter("disablePaging", "true"); // $NON-NLS-0$ $NON-NLS-1$
        this.block.setParameter("sortBy", "fieldname"); // $NON-NLS-0$ $NON-NLS-1$
        this.block.setParameter("ascending", "true"); // $NON-NLS-0$ $NON-NLS-1$
        this.block.setParameter("query", config.getString("query", "")); // $NON-NLS-0$ $NON-NLS-1$ $NON-NLS-2$

        this.setView(new SnippetTableView<>(this,
                new DefaultTableColumnModel(new TableColumn[]{
                        new TableColumn(I18n.I.type(), 0.3f, TableCellRenderers.STRING), 
                        new TableColumn(I18n.I.countAbbr(), 0.1f, TableCellRenderers.STRING_RIGHT), 
                        new TableColumn(I18n.I.minimumAbbr(), 0.2f, TableCellRenderers.CHANGE_PERCENT), 
                        new TableColumn(I18n.I.averageHtmlSign(), 0.2f, TableCellRenderers.CHANGE_PERCENT), 
                        new TableColumn(I18n.I.maximumAbbr(), 0.2f, TableCellRenderers.CHANGE_PERCENT) 
                }))
        );
    }

    public void addSymbolSnippet(SymbolSnippet s) {
        this.symbolSnippets.add(s);
    }

    public void destroy() {
        destroyBlock(this.block);
    }

    public void updateView() {
        if (!block.isResponseOk()) {
            getView().update(DefaultTableDataModel.NULL);
            return;
        }

        final FinderGroupTable result = this.block.getResult();
        final List<Object[]> list = new ArrayList<>();
        for (FinderGroupRow row : result.getRow()) {
            for (FinderGroupCell column : row.getColumn()) {
                for (FinderGroupItem item : column.getItem()) {
                    final MinMaxAvgElement e = (MinMaxAvgElement) item;
                    list.add(new Object[]{
                            row.getKey(),
                            e.getCount(),
                            e.getMinimum(),
                            e.getAverage(),
                            e.getMaximum()
                    });
                }
            }
        }


        final TableDataModel tdm = DefaultTableDataModel.create(list);
        getView().update(tdm);
    }


    public void onClick(LinkContext context, Element e) {
        for (SymbolSnippet s : symbolSnippets) {
            final QuoteWithInstrument qwi = (QuoteWithInstrument) context.data;
            s.setSymbol(InstrumentTypeEnum.valueOf(qwi.getInstrumentData().getType()), qwi.getQuoteData().getQid(), null);
        }
        ackParametersChanged();
    }
}
