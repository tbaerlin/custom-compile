/*
 * OlbFundSnippet.java
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.customer.olb;

import com.google.gwt.dom.client.Element;
import de.marketmaker.iview.dmxml.FNDFinder;
import de.marketmaker.iview.dmxml.FNDFinderElement;
import de.marketmaker.iview.dmxml.FNDFinderMetadata;
import de.marketmaker.iview.dmxml.FinderMetaList;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.QuoteWithInstrument;
import de.marketmaker.iview.mmgwt.mmweb.client.data.Link;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.history.ItemListContext;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.GenericListSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.GenericListSnippetView;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.Snippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SnippetClass;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SnippetConfiguration;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SnippetTableView;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableColumnModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableDataModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.SimpleVisibilityCheck;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableCellRenderers;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableColumn;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableDataModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.VisibilityCheck;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;
import de.marketmaker.iview.mmgwt.mmweb.client.util.LinkContext;
import de.marketmaker.iview.mmgwt.mmweb.client.util.LinkListener;
import de.marketmaker.iview.tools.i18n.NonNLS;

import java.util.ArrayList;
import java.util.List;

/**
 * @author umaurer
 */
@NonNLS
public class OlbFundSnippet extends GenericListSnippet<OlbFundSnippet> {

    final private DmxmlContext.Block<FNDFinderMetadata> metaBlock;

    private final boolean hasEtfTypes;

    public static class Class extends SnippetClass {
        public Class() {
            super("OlbFund");
        }

        public Snippet newSnippet(DmxmlContext context, SnippetConfiguration config) {
            return new OlbFundSnippet(context, config);
        }
    }

    protected OlbFundSnippet(DmxmlContext context, SnippetConfiguration config) {
        super(context, config, "FND_Finder", "bviperformance1y");

        this.metaBlock = context.addBlock("FND_FinderMetadata");
        this.hasEtfTypes = config.getBoolean("etfTypes", false);

        final VisibilityCheck showWknCheck = SimpleVisibilityCheck.valueOf(SessionData.INSTANCE.isShowWkn());
        final VisibilityCheck showIsinCheck = SimpleVisibilityCheck.valueOf(SessionData.INSTANCE.isShowIsin());

        final List<TableColumn> columns = new ArrayList<>();
        columns.add(new TableColumn("ISIN", -1f, TableCellRenderers.STRING_10).withVisibilityCheck(showIsinCheck));
        columns.add(new TableColumn("WKN", -1f, TableCellRenderers.STRING_10).withVisibilityCheck(showWknCheck));
        columns.add(new TableColumn(I18n.I.name(), -1f, TableCellRenderers.QUOTELINK_32, "name"));
        columns.add(new TableColumn(I18n.I.redemption(), -1f, TableCellRenderers.PRICE, "priceValue"));
        columns.add(new TableColumn(I18n.I.issuePrice2(), -1f, TableCellRenderers.PRICE, "issueprice"));
        columns.add(new TableColumn(I18n.I.currency(), -1f, TableCellRenderers.DEFAULT, "currency"));
        columns.add(new TableColumn(I18n.I.currentYearAbbr(), -1f, TableCellRenderers.CHANGE_PERCENT, "bviperformanceCurrentYear"));
        columns.add(new TableColumn(I18n.I.nMonths(1), -1f, TableCellRenderers.CHANGE_PERCENT, "bviperformance1m"));
        columns.add(new TableColumn(I18n.I.nYears(1), -1f, TableCellRenderers.CHANGE_PERCENT, "bviperformance1y"));
        columns.add(new TableColumn(I18n.I.nYears(3), -1f, TableCellRenderers.CHANGE_PERCENT, "bviperformance3y"));
        columns.add(new TableColumn(I18n.I.nYears(5), -1f, TableCellRenderers.CHANGE_PERCENT, "bviperformance5y"));
        if (config.getArray("detailIds") != null) {
            columns.add(new TableColumn("", 10f, TableCellRenderers.DEFAULT));
        }
        final SnippetTableView<OlbFundSnippet> snippetTableView = new SnippetTableView<>(this, new DefaultTableColumnModel(columns.toArray(new TableColumn[columns.size()])));

        setView(new GenericListSnippetView<>(this, snippetTableView));

        this.setIssuerField("issuername");
        this.setIssuerLabel(I18n.I.assetManagementCompany());
        this.setTypeField("fundtype");
        this.setTypeLabel(I18n.I.fundsKind());
        this.setInvestFocusField("investmentFocus");
        this.setInvestFocusLabel(I18n.I.investmentFocus());
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
        final FNDFinder fndFinder = (FNDFinder) this.block.getResult();
        final List<FNDFinderElement> listElements = fndFinder.getElement();
        final List<Object[]> list = new ArrayList<>();

        for (final FNDFinderElement e : listElements) {
            final List<Object> data = new ArrayList<>();
            data.add(e.getInstrumentdata().getIsin());
            data.add(e.getInstrumentdata().getWkn());
            data.add(new QuoteWithInstrument(e.getInstrumentdata(), e.getQuotedata())
                    .withHistoryContext(ItemListContext.createForFndPortrait(e, listElements, getView().getTitle())));
            data.add(e.getPrice());
            data.add(e.getIssuePrice());
            data.add(e.getQuotedata().getCurrencyIso());
            data.add(e.getBviperformanceCurrentYear());
            data.add(e.getBviperformance1M());
            data.add(e.getBviperformance1Y());
            data.add(e.getBviperformance3Y());
            data.add(e.getBviperformance5Y());
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

        final TableDataModel tdm = DefaultTableDataModel.create(list).withSort(fndFinder.getSort());
        if (tdm == DefaultTableDataModel.NULL) {
            setNull();
        }
        else {
            this.getView().update(tdm, Integer.parseInt(fndFinder.getOffset()), Integer.parseInt(fndFinder.getCount()), Integer.parseInt(fndFinder.getTotal()));
        }
    }

    @Override
    protected boolean hasSymbol(String qid) {
        if (qid == null) {
            return false;
        }
        final FNDFinder fndFinder = (FNDFinder) this.block.getResult();
        final List<FNDFinderElement> listElements = fndFinder.getElement();
        for (FNDFinderElement e : listElements) {
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
        if (hasEtfTypes) {
            return this.metaBlock.getResult().getFundsubtype();
        }
        return this.metaBlock.getResult().getFundtype();
    }

    @Override
    protected FinderMetaList getInvestFocusMetaData() {
        return this.metaBlock.getResult().getInvestmentFocus();
    }
}
