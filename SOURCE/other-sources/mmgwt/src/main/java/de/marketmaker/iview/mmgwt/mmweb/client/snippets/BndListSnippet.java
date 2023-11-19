/*
 * BndListSnippet.java
 *
 * Created on 4/20/15
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.snippets;

import com.google.gwt.dom.client.Element;
import de.marketmaker.iview.dmxml.BNDFinder;
import de.marketmaker.iview.dmxml.BNDFinderElement;
import de.marketmaker.iview.dmxml.BNDFinderMetadata;
import de.marketmaker.iview.dmxml.FinderMetaList;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.QuoteWithInstrument;
import de.marketmaker.iview.mmgwt.mmweb.client.data.Link;
import de.marketmaker.iview.mmgwt.mmweb.client.history.ItemListContext;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableColumnModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableDataModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableCellRenderers;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableColumn;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableDataModel;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;
import de.marketmaker.iview.mmgwt.mmweb.client.util.LinkContext;
import de.marketmaker.iview.mmgwt.mmweb.client.util.LinkListener;
import de.marketmaker.iview.tools.i18n.NonNLS;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Stefan Willenbrock
 */
@NonNLS
public class BndListSnippet extends GenericListSnippet<BndListSnippet> {

    final private DmxmlContext.Block<BNDFinderMetadata> metaBlock;

    public static class Class extends SnippetClass {
        public Class() {
            super("BndList");
        }

        public Snippet newSnippet(DmxmlContext context, SnippetConfiguration config) {
            return new BndListSnippet(context, config);
        }
    }

    public BndListSnippet(DmxmlContext context, SnippetConfiguration configuration) {
        super(context, configuration, "BND_Finder", "performance1y");
        this.metaBlock = context.addBlock("BND_FinderMetadata");

        final List<TableColumn> columns = new ArrayList<>();
        columns.add(new TableColumn(I18n.I.name(), -1f, TableCellRenderers.QUOTELINK_42, "name"));
        columns.add(new TableColumn("+/-%", -1f, TableCellRenderers.CHANGE_PERCENT, "changePercent"));
        columns.add(new TableColumn(I18n.I.nWeeks(1), -1f, TableCellRenderers.CHANGE_PERCENT, "performance1w"));
        columns.add(new TableColumn(I18n.I.nMonths(1), -1f, TableCellRenderers.CHANGE_PERCENT, "performance1m"));
        columns.add(new TableColumn(I18n.I.nYears(1), -1f, TableCellRenderers.CHANGE_PERCENT, "performance1y"));
        columns.add(new TableColumn(I18n.I.nYears(3), -1f, TableCellRenderers.CHANGE_PERCENT, "performance3y"));
        if (configuration.getArray("detailIds") != null) {
            columns.add(new TableColumn("", 10f, TableCellRenderers.DEFAULT));
        }
        final SnippetTableView<BndListSnippet> snippetTableView = new SnippetTableView<>(this, new DefaultTableColumnModel(columns.toArray(new TableColumn[columns.size()])));
        setView(new GenericListSnippetView<>(this, snippetTableView));

        setTypeField("bondType");
        setTypeLabel(I18n.I.bondType());
    }

    @Override
    protected boolean isResponseOk() {
        return this.metaBlock.isResponseOk();
    }

    @Override
    protected boolean isMetaDataEnabled() {
        return this.metaBlock.isEnabled();
    }

    @Override
    protected void enableMetaData(boolean enabled) {
        this.metaBlock.setEnabled(enabled);
    }

    protected void updateTableData() {
        final BNDFinder bndFinder = (BNDFinder) this.block.getResult();
        final List<BNDFinderElement> listElements = bndFinder.getElement();

        final List<Object[]> list = new ArrayList<>();
        for (BNDFinderElement e : listElements) {
            final List<Object> data = new ArrayList<>();
            data.add(new QuoteWithInstrument(e.getInstrumentdata(), e.getQuotedata())
                    .withHistoryContext(ItemListContext.createForBndPortrait(e, listElements, getView().getTitle())));
            data.add(e.getChangePercent());
            data.add(e.getPerformance1W());
            data.add(e.getPerformance1M());
            data.add(e.getPerformance1Y());
            data.add(e.getPerformance3Y());
            if (getConfiguration().getArray("detailIds") != null) {
                final String symbol = e.getQuotedata().getQid();
                final String styleName = this.selection.isSelected(symbol) ? "mm-bestTool-link selected" : "mm-bestTool-link";
                final String linkContent = "<div class=\"" + styleName + "-content\"></div>";
                final Link link = new Link(new LinkListener<Link>() {
                    public void onClick(LinkContext<Link> linkLinkContext, Element e) {
                        selection.setSymbol(symbol);
                    }
                }, linkContent, null);
                data.add(link);
            }
            list.add(data.toArray());
        }

        final TableDataModel tdm = DefaultTableDataModel.create(list).withSort(bndFinder.getSort());
        if (tdm == DefaultTableDataModel.NULL) {
            setNull();
        }
        else {
            getView().update(tdm, Integer.parseInt(bndFinder.getOffset()), Integer.parseInt(bndFinder.getCount()), Integer.parseInt(bndFinder.getTotal()));
        }
    }

    @Override
    protected boolean hasSymbol(String qid) {
        if (qid == null) {
            return false;
        }
        final BNDFinder bndFinder = (BNDFinder) this.block.getResult();
        final List<BNDFinderElement> listElements = bndFinder.getElement();
        for (BNDFinderElement e : listElements) {
            if (qid.equals(e.getQuotedata().getQid())) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected FinderMetaList getTypeMetaData() {
        return this.metaBlock.getResult().getBondType();
    }
}
