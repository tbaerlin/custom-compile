/*
 * CerListSnippet.java
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.snippets;

import com.google.gwt.dom.client.Element;
import de.marketmaker.iview.dmxml.CERFinder;
import de.marketmaker.iview.dmxml.CERFinderElement;
import de.marketmaker.iview.dmxml.CERFinderMetadata;
import de.marketmaker.iview.dmxml.FinderMetaList;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.QuoteWithInstrument;
import de.marketmaker.iview.mmgwt.mmweb.client.data.Link;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
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
 * @author Michael Lösch
 */
@NonNLS
public class CerListSnippet extends GenericListSnippet<CerListSnippet> {

    final private DmxmlContext.Block<CERFinderMetadata> metaBlock;

    public static class Class extends SnippetClass {
        public Class() {
            super("CerList");
        }

        public Snippet newSnippet(DmxmlContext context, SnippetConfiguration config) {
            return new CerListSnippet(context, config);
        }
    }

    protected CerListSnippet(DmxmlContext context, SnippetConfiguration config) {
        super(context, config, "CER_Finder", "performance1y");
        final String providerPreference = SessionData.INSTANCE.getGuiDefValue("providerPreference");
        this.block.setParameter("providerPreference", providerPreference);

        this.metaBlock = context.addBlock("CER_FinderMetadata"); 
        this.metaBlock.setParameter("providerPreference", providerPreference);

        final List<TableColumn> columns = new ArrayList<>();
        columns.add(new TableColumn(I18n.I.name(), -1f, TableCellRenderers.QUOTELINK_42, "name"));
        columns.add(new TableColumn("+/-%", -1f, TableCellRenderers.CHANGE_PERCENT, "changePercent"));
        columns.add(new TableColumn(I18n.I.nWeeks(1), -1f, TableCellRenderers.CHANGE_PERCENT, "performance1w"));
        columns.add(new TableColumn(I18n.I.nMonths(1), -1f, TableCellRenderers.CHANGE_PERCENT, "performance1m"));
        columns.add(new TableColumn(I18n.I.nYears(1), -1f, TableCellRenderers.CHANGE_PERCENT, "performance1y"));
        columns.add(new TableColumn(I18n.I.nYears(3), -1f, TableCellRenderers.CHANGE_PERCENT, "performance3y"));
        if (config.getArray("detailIds") != null) {
            columns.add(new TableColumn("", 10f, TableCellRenderers.DEFAULT));
        }
        SnippetTableView<CerListSnippet> snippetTableView = new SnippetTableView<>(this, new DefaultTableColumnModel(columns.toArray(new TableColumn[columns.size()])));

        final GenericListSnippetView<CerListSnippet> view = new GenericListSnippetView<>(this, snippetTableView);

        this.setView(view);

        this.setIssuerField("issuername");
        this.setIssuerLabel(I18n.I.issuer());
        this.setTypeField("typeKey");
        this.setTypeLabel(I18n.I.certificateType());
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

    @Override
    protected void updateTableData() {
        final CERFinder cerFinder = (CERFinder) this.block.getResult();
        final List<CERFinderElement> listElements = cerFinder.getElement();
        final List<Object[]> list = new ArrayList<>(listElements.size());

        for (CERFinderElement e : listElements) {
            final List<Object> data = new ArrayList<>();
            data.add(new QuoteWithInstrument(e.getInstrumentdata(), e.getQuotedata())
                    .withHistoryContext(ItemListContext.createForCerPortrait(e, listElements, getView().getTitle())));
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

        final TableDataModel tdm = DefaultTableDataModel.create(list).withSort(cerFinder.getSort());
        if (tdm == DefaultTableDataModel.NULL) {
            setNull();
        }
        else {
            getView().update(tdm, Integer.parseInt(cerFinder.getOffset()), Integer.parseInt(cerFinder.getCount()), Integer.parseInt(cerFinder.getTotal()));
        }
    }

    @Override
    protected boolean hasSymbol(String qid) {
        if (qid == null) {
            return false;
        }
        final CERFinder cerFinder = (CERFinder) this.block.getResult();
        final List<CERFinderElement> listElements = cerFinder.getElement();
        for (CERFinderElement e : listElements) {
            if (qid.equals(e.getQuotedata().getQid())) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected FinderMetaList getIssuerMetaData() {
        return this.metaBlock.getResult().getIssuername();
    }

    @Override
    protected FinderMetaList getTypeMetaData() {
        return this.metaBlock.getResult().getTypeKey();
    }
}
