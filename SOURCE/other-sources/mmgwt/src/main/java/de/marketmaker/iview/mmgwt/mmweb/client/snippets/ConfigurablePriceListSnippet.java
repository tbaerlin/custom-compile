/*
 * ConfigurablePriceListSnippet.java
 *
 * Created on 31.03.2010 13:40:59
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.snippets;

import de.marketmaker.iview.dmxml.InstrumentData;
import de.marketmaker.iview.dmxml.MSCPriceDataExtended;
import de.marketmaker.iview.dmxml.MSCPriceDataExtendedElement;
import de.marketmaker.iview.dmxml.QuoteData;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.QuoteWithInstrument;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PlaceChangeEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PricesUpdatedEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PushRegisterEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PushRegisterHandler;
import de.marketmaker.iview.mmgwt.mmweb.client.prices.Price;
import de.marketmaker.iview.mmgwt.mmweb.client.prices.PriceSupport;
import de.marketmaker.iview.mmgwt.mmweb.client.push.PushRenderItem;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableColumnModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableDataModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableCellRenderers;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableColumn;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;
import de.marketmaker.iview.tools.i18n.NonNLS;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Michael Lösch
 * @author Ulrich Maurer
 */
@NonNLS
public class ConfigurablePriceListSnippet extends AbstractSnippet<ConfigurablePriceListSnippet, SnippetTableView<ConfigurablePriceListSnippet>> implements PushRegisterHandler {

    public static class Class extends SnippetClass {
        public Class() {
            super("ConfigurablePriceList", I18n.I.pricelist()); // $NON-NLS-0$
        }

        public Snippet newSnippet(DmxmlContext context, SnippetConfiguration config) {
            return new ConfigurablePriceListSnippet(context, config);
        }
    }

    private DefaultTableDataModel dtm;

    private final int columnCount;

    private final PriceSupport priceSupport = new PriceSupport(this);

    private DmxmlContext.Block<MSCPriceDataExtended> block;

    private final String origTitle;

    private final boolean calcCurrency;


    public ConfigurablePriceListSnippet(DmxmlContext context, SnippetConfiguration config) {
        super(context, config);
        this.block = createBlock("MSC_PriceDataExtended"); // $NON-NLS-0$

        final DefaultTableColumnModel columnModel = createColumnModel(config);
        this.columnCount = columnModel.getColumnCount();

        final SnippetTableView<ConfigurablePriceListSnippet> view = new SnippetTableView<>(this
                , columnModel, "mm-snippetTable mm-snippet-priceList"); // $NON-NLS-0$
        this.setView(view);

        this.origTitle = config.getString("title"); // $NON-NLS-0$
        this.calcCurrency = config.getBoolean("calcCurrency", false); // $NON-NLS-0$

        this.block.setParameters("symbol", getSymbols()); // $NON-NLS-0$
        this.block.setParameter("sortBy", config.getString("sortBy", "name")); // $NON-NLS$
        this.block.setParameter("symbolStrategy", config.getString("symbolStrategy", null)); // $NON-NLS$
        this.block.setParameter("marketStrategy", config.getString("marketStrategy", null)); // $NON-NLS$
        this.block.setParameter("suffixPattern", config.getString("suffixPattern", null)); // $NON-NLS$
        this.block.setParameter("maxCountPerSymbol", config.getString("maxCountPerSymbol", null)); // $NON-NLS$
    }

    @Override
    public void onPlaceChange(PlaceChangeEvent event) {
        final String currency = event.getHistoryToken().get("cur"); // $NON-NLS-0$

        if (currency != null && currency.isEmpty()) {
            this.block.removeParameter("currency"); // $NON-NLS-0$
            getView().setTitle(this.origTitle);
        }
        else if (this.calcCurrency) {
            this.block.setParameter("currency", currency); // $NON-NLS-0$
            getView().setTitle(this.origTitle + (currency != null ? " - " + currency : "")); // $NON-NLS-0$ $NON-NLS-1$
        }
    }

    private String[] getSymbols() {
        final SnippetConfiguration config = getConfiguration();
        final String listId = config.getString("listid", null); // $NON-NLS-0$
        if (listId == null) {
            return config.getArray("symbols"); // $NON-NLS-0$
        }
        final List<QuoteWithInstrument> listQid = SessionData.INSTANCE.getList(listId);
        final List<String> result = new ArrayList<>(listQid.size());
        for (QuoteWithInstrument qid : listQid) {
            result.add(qid.getQuoteData().getQid());
        }
        return result.toArray(new String[result.size()]);
    }

    private DefaultTableColumnModel createColumnModel(SnippetConfiguration config) {
        final DefaultTableColumnModel m = new DefaultTableColumnModel(new TableColumn[]{
                new TableColumn(I18n.I.name(), -1f, TableCellRenderers.QUOTELINK_32).withId("name")  // $NON-NLS-0$
                , new TableColumn(I18n.I.name(), -1f, TableCellRenderers.QUOTELINK_32).withId("ncur")  // $NON-NLS-0$
                , new TableColumn(I18n.I.time(), -1f, TableCellRenderers.DATE_OR_TIME_COMPACT_PUSH).withId("time")  // $NON-NLS-0$
                , new TableColumn(I18n.I.time(), -1f, TableCellRenderers.BIDASK_DATE_COMPACT_PUSH).withId("bat")  // $NON-NLS-0$
                , new TableColumn("", -1f, TableCellRenderers.STRING_RIGHT).withId("ls") // $NON-NLS-0$ $NON-NLS-1$
                , new TableColumn("", -1f, TableCellRenderers.STRING_RIGHT).withId("hs") // $NON-NLS$
                , new TableColumn("", -1f, TableCellRenderers.STRING_LEFT).withId("psl") // $NON-NLS$
                , new TableColumn("", -1f, TableCellRenderers.STRING_LEFT).withId("lsl") // $NON-NLS$
                , new TableColumn("", -1f, TableCellRenderers.STRING_LEFT).withId("hsl") // $NON-NLS$
                , new TableColumn(I18n.I.bid(), -1f, TableCellRenderers.BID_PUSH).withId("bid")  // $NON-NLS-0$
                , new TableColumn(I18n.I.ask(), -1f, TableCellRenderers.ASK_PUSH).withId("ask")  // $NON-NLS-0$
                , new TableColumn(I18n.I.priceValue(), -1f, TableCellRenderers.LAST_PRICE_PUSH).withId("prc")  // $NON-NLS-0$
                , new TableColumn(I18n.I.price(), -1f, TableCellRenderers.LAST_PRICE_PUSH).withId("prcl")  // $NON-NLS-0$
                , new TableColumn(I18n.I.priceValue(), -1f, TableCellRenderers.LAST_PRICE_WITH_SUPPLEMENT_PUSH).withId("pws")  // $NON-NLS-0$
                , new TableColumn(I18n.I.price(), -1f, TableCellRenderers.LAST_PRICE_WITH_SUPPLEMENT_PUSH).withId("pwsl")  // $NON-NLS-0$
                , new TableColumn(I18n.I.previousClose(), -1f, TableCellRenderers.PRICE_WITH_SUPPLEMENT).withId("pre")  // $NON-NLS-0$
                , new TableColumn(I18n.I.settlement(), -1f, TableCellRenderers.PRICE).withId("sett")  // $NON-NLS-0$
                , new TableColumn(I18n.I.high(), -1f, TableCellRenderers.HIGH_PUSH).withId("hd")  // $NON-NLS-0$
                , new TableColumn(I18n.I.low(), -1f, TableCellRenderers.LOW_PUSH).withId("ld")  // $NON-NLS-0$
                , new TableColumn(I18n.I.yearHighAbbr(), -1f, TableCellRenderers.PRICE).withId("hy")  // $NON-NLS-0$
                , new TableColumn(I18n.I.yearLowAbbr(), -1f, TableCellRenderers.PRICE).withId("ly")  // $NON-NLS-0$
                , new TableColumn("+/-%", -1f, TableCellRenderers.CHANGE_PERCENT_PUSH).withId("chg%") // $NON-NLS-0$ $NON-NLS-1$
                , new TableColumn("+/-", -1f, TableCellRenderers.CHANGE_NET_PUSH).withId("chg") // $NON-NLS-0$ $NON-NLS-1$
                , new TableColumn(I18n.I.high52WeeksAbbr(), -1f, TableCellRenderers.PRICE).withId("h52")  // $NON-NLS-0$
                , new TableColumn(I18n.I.low52WeeksAbbr(), -1f, TableCellRenderers.PRICE).withId("l52")  // $NON-NLS-0$
                , new TableColumn(I18n.I.turnoverDay(), -1f, TableCellRenderers.TURNOVER_PUSH).withId("turnoverDay")  // $NON-NLS-0$
                , new TableColumn(I18n.I.preTurnoverDay(), -1f, TableCellRenderers.TURNOVER_PUSH).withId("preTurnoverDay")  // $NON-NLS-0$
                , new TableColumn(I18n.I.bid(), -1f, TableCellRenderers.OFFICIAL_BID_PUSH).withId("officialBid") // $NON-NLS-0$
                , new TableColumn(I18n.I.ask(), -1f, TableCellRenderers.OFFICIAL_ASK_PUSH).withId("officialAsk") // $NON-NLS-0$
                , new TableColumn(I18n.I.previousClose(), -1f, TableCellRenderers.PRICE).withId("pprc")  // $NON-NLS$
                , new TableColumn(I18n.I.previousClose(), -1f, TableCellRenderers.PRICE_WITH_SUPPLEMENT).withId("pprcs")  // $NON-NLS$
                , new TableColumn(I18n.I.price(), -1f, TableCellRenderers.PRICE_WITH_SUPPLEMENT).withId("lme_pprcs")  // $NON-NLS$  (LME previous day group)
                , new TableColumn(I18n.I.price(), -1f, TableCellRenderers.PRICE).withId("lme_pprc") // $NON-NLS$  (LME previous day group)
                , new TableColumn("", -1f, TableCellRenderers.STRING_LEFT).withId("lme_pprc_supp") // $NON-NLS$  (LME previous day group)
                , new TableColumn(I18n.I.bidPreviousOfficialPriceAbbr(), -1f, TableCellRenderers.PRICE).withId("lme_pob") // $NON-NLS$ (LME previous day group)
                , new TableColumn(I18n.I.askPreviousOfficialPriceAbbr(), -1f, TableCellRenderers.PRICE).withId("lme_poa") // $NON-NLS$ (LME previous day group)
                , new TableColumn(I18n.I.bidPreviousUnofficialPriceAbbr(), -1f, TableCellRenderers.PRICE).withId("lme_pub") // $NON-NLS$ (LME previous day group)
                , new TableColumn(I18n.I.askPreviousUnofficialPriceAbbr(), -1f, TableCellRenderers.PRICE).withId("lme_pua") // $NON-NLS$ (LME previous day group)
        }).withColumnOrder(config.getArray("orderids", new String[]{"name", "time", "ls"}));

        if(config.getBoolean("lmeCurrentPreviousGroups", false)) {
            m.groupColumns(0, m.findIndexOfColumnById("pprcs"), I18n.I.dataOfCurrentDay());
            m.groupColumns(m.findIndexOfColumnById("lme_pprcs"), m.getColumnCount(), I18n.I.dataOfPreviousDay());
        }

        return m; // $NON-NLS$
    }


    public ArrayList<PushRenderItem> onPushRegister(PushRegisterEvent event) {
        if (this.block.isResponseOk()) {
            int numAdded = event.addVwdcodes(this.block.getResult());
            if (this.block.getResult().getElement().size() == numAdded) {
                event.addComponentToReload(this.block, this);
            }
            if (numAdded > 0) {
                return getView().getRenderItems(this.dtm);
            }
        }
        return null;
    }

    public void onPricesUpdated(PricesUpdatedEvent event) {
        if (this.block.isResponseOk() && !this.priceSupport.isLatestPriceGeneration() && !event.isPushedUpdate()) {
            updateView();
        }
    }

    public void destroy() {
        this.priceSupport.deactivate();
        destroyBlock(this.block);
    }

    public void updateView() {
        this.priceSupport.invalidateRenderItems();

        if (!block.isResponseOk()) {
            getView().update(DefaultTableDataModel.NULL);
            return;
        }

        final MSCPriceDataExtended ld = this.block.getResult();
        final int rows = Integer.parseInt(ld.getCount());

        this.dtm = new DefaultTableDataModel(rows, this.columnCount).withRowClasses(DefaultTableDataModel.ROW_CLASSES);
        int row = 0;
        for (final MSCPriceDataExtendedElement e : ld.getElement()) {
            int col = -1;
            final InstrumentData id = e.getInstrumentdata();
            final QuoteData qd = e.getQuotedata();
            this.dtm.setValueAt(row, ++col, new QuoteWithInstrument(id, qd)); //Name
            this.dtm.setValueAt(row, ++col, new QuoteWithInstrument(id, qd, StringUtil.getReducedCurrencyName(id))); //Name (short currency)
            final Price price = Price.create(e);
            this.dtm.setValueAt(row, ++col, price); //TimeOrDate
            this.dtm.setValueAt(row, ++col, price); //BidAsk TimeOrDate
            this.dtm.setValueAt(row, ++col, e.getPricedataExtended().getLmeSubsystemBid());
            this.dtm.setValueAt(row, ++col, e.getPricedataExtended().getLmeSubsystemAsk());
            this.dtm.setValueAt(row, ++col, price.getLastPrice().getSupplement());
            this.dtm.setValueAt(row, ++col, e.getPricedataExtended().getLmeSubsystemBid());
            this.dtm.setValueAt(row, ++col, e.getPricedataExtended().getLmeSubsystemAsk());
            this.dtm.setValueAt(row, ++col, price); //Bid
            this.dtm.setValueAt(row, ++col, price); //Ask
            this.dtm.setValueAt(row, ++col, price); //Price
            this.dtm.setValueAt(row, ++col, price); //Price (Kurs-Label)
            this.dtm.setValueAt(row, ++col, price); //Price with Supplement
            this.dtm.setValueAt(row, ++col, price); //Price with Supplement (Kurs-Label)
            this.dtm.setValueAt(row, ++col, price.getPreviousPrice());
            this.dtm.setValueAt(row, ++col, e.getPricedataExtended().getSettlement());
            this.dtm.setValueAt(row, ++col, price); // HighDay
            this.dtm.setValueAt(row, ++col, price); // LowDay
            this.dtm.setValueAt(row, ++col, price.getHighYear());
            this.dtm.setValueAt(row, ++col, price.getLowYear());
            this.dtm.setValueAt(row, ++col, price); // Change Percent
            this.dtm.setValueAt(row, ++col, price); // Change Net
            this.dtm.setValueAt(row, ++col, price.getHigh52W());
            this.dtm.setValueAt(row, ++col, price.getLow52W());
            this.dtm.setValueAt(row, ++col, price); // total volume (turnoverDay)
            this.dtm.setValueAt(row, ++col, price.getPrevious()); // previous total volume (turnoverDay)
            this.dtm.setValueAt(row, ++col, price); // Official Bid
            this.dtm.setValueAt(row, ++col, price); // Official Ask
            this.dtm.setValueAt(row, ++col, price.getPreviousPrice().getPrice()); // Previous Price without supplement
            this.dtm.setValueAt(row, ++col, price.getPreviousPrice()); // Previous Price with supplement
            this.dtm.setValueAt(row, ++col, price.getPreviousPrice()); // Previous Price with supplement (LME previous day group)
            this.dtm.setValueAt(row, ++col, price.getPreviousPrice().getPrice()); // Previous Price without supplement (LME previous day group)
            this.dtm.setValueAt(row, ++col, price.getPreviousPrice().getSupplement()); // Previous supplement (LME previous day group)
            this.dtm.setValueAt(row, ++col, price.getPreviousOfficialBid()); // Previous Official Bid (LME previous day group)
            this.dtm.setValueAt(row, ++col, price.getPreviousOfficialAsk()); // Previous Official Ask (LME previous day group)
            this.dtm.setValueAt(row, ++col, price.getPreviousUnofficialBid()); // Previous Unofficial Bid (LME previous day group)
            this.dtm.setValueAt(row, ++col, price.getPreviousUnofficialAsk()); // Previous Unofficial Ask (LME previous day group)
            row++;
        }
        getView().update(dtm);
        this.priceSupport.activate();
    }


    @Override
    public void deactivate() {
        super.deactivate();
        this.priceSupport.deactivate();
    }
}
