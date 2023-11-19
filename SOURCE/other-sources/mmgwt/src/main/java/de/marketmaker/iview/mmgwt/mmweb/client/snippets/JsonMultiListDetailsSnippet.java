package de.marketmaker.iview.mmgwt.mmweb.client.snippets;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.marketmaker.iview.dmxml.MSCPriceDataExtended;
import de.marketmaker.iview.dmxml.MSCPriceDataExtendedElement;
import de.marketmaker.iview.dmxml.PriceDataExtended;
import de.marketmaker.iview.mmgwt.mmweb.client.QuoteWithInstrument;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.history.ItemListContext;
import de.marketmaker.iview.mmgwt.mmweb.client.prices.Price;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableColumnModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableDataModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.SimpleVisibilityCheck;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableCellRenderers;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableColumn;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableDataModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.VisibilityCheck;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;

import de.marketmaker.iview.mmgwt.mmweb.client.I18n;

/**
 * @author umaurer
 */
public class JsonMultiListDetailsSnippet extends AbstractSnippet<JsonMultiListDetailsSnippet, SnippetTableView<JsonMultiListDetailsSnippet>> {
    public static class Class extends SnippetClass {
        public Class() {
            super("JsonMultiListDetails"); // $NON-NLS-0$
        }

        public Snippet newSnippet(DmxmlContext context, SnippetConfiguration config) {
            return new JsonMultiListDetailsSnippet(context, config);
        }
    }

    private DmxmlContext.Block<MSCPriceDataExtended> block;
    private Map<String, QuoteWithInstrument> mapQwis = new HashMap<String, QuoteWithInstrument>();

    protected JsonMultiListDetailsSnippet(DmxmlContext context, SnippetConfiguration config) {
        super(context, config);

        this.block = createBlock("MSC_PriceDataExtended"); // $NON-NLS-0$
        this.block.setParameter("symbolStrategy", "auto"); // $NON-NLS-0$ $NON-NLS-1$
        this.block.setParameters("symbol", getListSymbols(config)); // $NON-NLS-0$

        final String[] columns = config.getArray("columns"); // $NON-NLS-0$
        final Set<String> setColumns = new HashSet<String>(Arrays.asList(columns));

        final VisibilityCheck showWknCheck = SimpleVisibilityCheck.valueOf(SessionData.INSTANCE.isShowWkn() && setColumns.contains("wkn")); //$NON-NLS$
        final VisibilityCheck showIsinCheck = SimpleVisibilityCheck.valueOf(SessionData.INSTANCE.isShowIsin() && setColumns.contains("isin")); //$NON-NLS$

        this.setView(new SnippetTableView<JsonMultiListDetailsSnippet>(this, new DefaultTableColumnModel(new TableColumn[]{
                new TableColumn("WKN", -1f, TableCellRenderers.DEFAULT).withVisibilityCheck(showWknCheck), //$NON-NLS$
                new TableColumn("ISIN", -1f, TableCellRenderers.DEFAULT).withVisibilityCheck(showIsinCheck), //$NON-NLS$
                new TableColumn(I18n.I.name(), -1f, TableCellRenderers.QUOTELINK_32).withVisibilityCheck(SimpleVisibilityCheck.valueOf(setColumns.contains("name"))),  // $NON-NLS-0$
                new TableColumn(I18n.I.price(), -1f, TableCellRenderers.PRICE).withVisibilityCheck(SimpleVisibilityCheck.valueOf(setColumns.contains("priceValue"))),  // $NON-NLS-0$
                new TableColumn("+/-%", -1f, TableCellRenderers.CHANGE_PERCENT).withVisibilityCheck(SimpleVisibilityCheck.valueOf(setColumns.contains("changePercent"))), // $NON-NLS-0$ $NON-NLS-1$
                new TableColumn(I18n.I.time(), -1f, TableCellRenderers.COMPACT_DATE_OR_TIME).withVisibilityCheck(SimpleVisibilityCheck.valueOf(setColumns.contains("date"))),  // $NON-NLS-0$
                new TableColumn(I18n.I.settlement(), -1f, TableCellRenderers.PRICE).withVisibilityCheck(SimpleVisibilityCheck.valueOf(setColumns.contains("settlement"))),  // $NON-NLS-0$
                new TableColumn(I18n.I.time(), -1f, TableCellRenderers.COMPACT_DATE_OR_TIME).withVisibilityCheck(SimpleVisibilityCheck.valueOf(setColumns.contains("settlementDate")))  // $NON-NLS-0$
        })));
    }

    private String[] getListSymbols(SnippetConfiguration config) {
        final List<String> qids = new ArrayList<String>();
        final String[] listnames = config.getArray("listnames"); // $NON-NLS-0$
        for (final String listname : listnames) {
            final List<QuoteWithInstrument> qwis = SessionData.INSTANCE.getList(listname);
            for (QuoteWithInstrument qwi : qwis) {
                qids.add(qwi.getQuoteData().getQid());
                mapQwis.put(qwi.getQuoteData().getQid(), qwi);
            }
        }
        return qids.toArray(new String[qids.size()]);
    }

    public void destroy() {
        destroyBlock(this.block);
    }

    public void updateView() {
        if (!this.block.isResponseOk()) {
            getView().update(DefaultTableDataModel.NULL);
            return;
        }

        final MSCPriceDataExtended result = this.block.getResult();
        final List<Object[]> list = new ArrayList<Object[]>();
        for (MSCPriceDataExtendedElement e : result.getElement()) {
            QuoteWithInstrument qwi = this.mapQwis.get(e.getQuotedata().getQid());
            if (qwi == null) {
                qwi = new QuoteWithInstrument(e.getInstrumentdata(), e.getQuotedata());
            }
            qwi.withHistoryContext(ItemListContext.createForPortrait(e, result.getElement(), getView().getTitle()));
            final Price price = Price.create(e);
            final PriceDataExtended extended = Price.nonNullPriceDataExtended(e.getPricedataExtended());
            list.add(new Object[]{
                    qwi.getInstrumentData().getWkn(),
                    qwi.getInstrumentData().getIsin(),
                    qwi,
                    price.getLastPrice().getPrice(),
                    price.getChangePercent(),
                    price.getDate(),
                    extended.getSettlement(),
                    extended.getSettlementDate()
            });
        }

        final TableDataModel tdm = DefaultTableDataModel.create(list);
        getView().update(tdm);
    }
}
