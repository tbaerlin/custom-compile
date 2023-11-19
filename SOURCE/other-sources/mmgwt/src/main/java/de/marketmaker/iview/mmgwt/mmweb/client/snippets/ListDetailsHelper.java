/*
 * ListDetailsHelper.java
 *
 * Created on 10.06.2008 14:44:10
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */

package de.marketmaker.iview.mmgwt.mmweb.client.snippets;

import de.marketmaker.iview.dmxml.InstrumentData;
import de.marketmaker.iview.dmxml.QuoteData;
import de.marketmaker.iview.mmgwt.mmweb.client.FeatureFlags;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.QuoteWithInstrument;
import de.marketmaker.iview.mmgwt.mmweb.client.QwiPosition;
import de.marketmaker.iview.mmgwt.mmweb.client.data.CurrentTrendBar;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.data.TrendBarData;
import de.marketmaker.iview.mmgwt.mmweb.client.history.HistoryContext;
import de.marketmaker.iview.mmgwt.mmweb.client.prices.Price;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableColumnModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableDataModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.SimpleVisibilityCheck;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableCellRenderers;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableColumn;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableColumnModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.VisibilityCheck;

/**
 * @author Ulrich Maurer
 */
public class ListDetailsHelper {
    public enum LinkType {
        NAME, MARKET, VWDCODE, MARKET_NO_LINK
    }

    private final LinkType linkType;
    private final boolean displayVolume;
    private final boolean displayMarket;
    private boolean displaySymbol = false;
    private boolean displayBidAskVolume = false;
    private boolean displayDzBankLink = false;
    private boolean displayLMEFields = false;

    public ListDetailsHelper(LinkType linkType, boolean displayVolume, boolean displayMarket) {
        this.linkType = linkType;
        this.displayVolume = displayVolume;
        this.displayMarket = displayMarket && linkType != LinkType.MARKET;
    }

    public ListDetailsHelper withWkn(boolean displayWkn) {
        this.displaySymbol = displayWkn;
        return this;
    }

    public ListDetailsHelper withDzBankLink(boolean displayDzBankLink) {
        this.displayDzBankLink = displayDzBankLink;
        return this;
    }

    public ListDetailsHelper withBidAskVolume(boolean displayBidAskVolume) {
        this.displayBidAskVolume = displayBidAskVolume;
        return this;
    }

    public ListDetailsHelper withLMEFields(boolean displayLMEFields) {
        this.displayLMEFields = displayLMEFields;
        return this;
    }

    public TableColumnModel createTableColumnModel() {
        final VisibilityCheck showWknCheck = SimpleVisibilityCheck.valueOf(this.displaySymbol && SessionData.INSTANCE.isShowWkn());
        final VisibilityCheck showIsinCheck = SimpleVisibilityCheck.valueOf(this.displaySymbol && SessionData.INSTANCE.isShowIsin());
        final VisibilityCheck vcVolume = SimpleVisibilityCheck.valueOf(this.displayVolume);
        final VisibilityCheck vcMarket = SimpleVisibilityCheck.valueOf(this.displayMarket);
        final VisibilityCheck vcBidAskVolume = SimpleVisibilityCheck.valueOf(this.displayBidAskVolume);
        final VisibilityCheck dzBankLink = SimpleVisibilityCheck.valueOf(this.displayDzBankLink);
        final VisibilityCheck showLMEFieldsCheck = SimpleVisibilityCheck.valueOf(this.displayLMEFields && FeatureFlags.Feature.LME_CHANGES_2014.isEnabled());

        final TableColumn[] columns = new TableColumn[]{
                new TableColumn("", 0.05f, TableCellRenderers.VR_ICON_LINK).withVisibilityCheck(dzBankLink),
                getLinkTypeColumn(),
                new TableColumn("WKN", 0.05f, TableCellRenderers.DEFAULT, "wkn").withVisibilityCheck(showWknCheck), // $NON-NLS$
                new TableColumn("ISIN", 0.05f, TableCellRenderers.DEFAULT, "isin").withVisibilityCheck(showIsinCheck),  // $NON-NLS$
                new TableColumn(I18n.I.price(), 0.05f, TableCellRenderers.LAST_PRICE_WITH_SUPPLEMENT_PUSH),
                new TableColumn(I18n.I.market(), 0.05f, TableCellRenderers.STRING_CENTER).withVisibilityCheck(vcMarket),
                new TableColumn("+/-", 0.05f, TableCellRenderers.CHANGE_PERCENT_PUSH, "changePercent"), // $NON-NLS-0$ $NON-NLS-1$
                new TableColumn(I18n.I.trend(), 0.05f, TableCellRenderers.TRENDBAR, "changePercent").withCellClass("mm-middle"),//,  // $NON-NLS-0$ $NON-NLS-1$
                new TableColumn(I18n.I.time(), 0.07f, TableCellRenderers.DATE_OR_TIME_PUSH, "date").withCellClass("mm-center"),  // $NON-NLS-0$ $NON-NLS-1$
                new TableColumn(I18n.I.bid(), 0.06f, TableCellRenderers.BID_PUSH),
                new TableColumn(I18n.I.ask(), 0.06f, TableCellRenderers.ASK_PUSH),
                new TableColumn(I18n.I.bidVolumeAbbr(), 0.06f, TableCellRenderers.BID_VOLUME_PUSH, "bidVolume").withVisibilityCheck(vcBidAskVolume), // $NON-NLS-0$
                new TableColumn(I18n.I.askVolumeAbbr(), 0.06f, TableCellRenderers.ASK_VOLUME_PUSH, "askVolume").withVisibilityCheck(vcBidAskVolume), // $NON-NLS-0$
                new TableColumn(I18n.I.previousClose(), 0.07f, TableCellRenderers.PRICE_WITH_SUPPLEMENT, "previousClose"),  // $NON-NLS-0$
                new TableColumn(I18n.I.low(), 0.06f, TableCellRenderers.LOW_PUSH),
                new TableColumn(I18n.I.high(), 0.06f, TableCellRenderers.HIGH_PUSH),
                new TableColumn(I18n.I.volume(), 0.11f, TableCellRenderers.VOLUME_LONG_PUSH, "volume").withCellClass("mm-right").withVisibilityCheck(vcVolume),  // $NON-NLS-0$ $NON-NLS-1$
                new TableColumn(I18n.I.turnoverDay(), 0.13f, TableCellRenderers.TURNOVER_PUSH, "turnoverDay").withVisibilityCheck(vcVolume),  // $NON-NLS-0$
                new TableColumn(I18n.I.numTrades(), 0.13f, TableCellRenderers.TRADE_LONG_PUSH, "numberOfTrades").withVisibilityCheck(vcVolume), // $NON-NLS-0$
                new TableColumn(I18n.I.bid()  + I18n.I.officialSuffix(), 0.06f, TableCellRenderers.PRICE, "officialBid").withVisibilityCheck(showLMEFieldsCheck), // $NON-NLS-0$
                new TableColumn(I18n.I.ask() + I18n.I.officialSuffix(), 0.06f, TableCellRenderers.PRICE, "officialAsk").withVisibilityCheck(showLMEFieldsCheck), // $NON-NLS-0$
                new TableColumn(I18n.I.bid() + I18n.I.unOfficialSuffix(), 0.06f, TableCellRenderers.PRICE, "unofficialBid").withVisibilityCheck(showLMEFieldsCheck), // $NON-NLS-0$
                new TableColumn(I18n.I.ask()  + I18n.I.unOfficialSuffix(), 0.06f, TableCellRenderers.PRICE, "unofficialAsk").withVisibilityCheck(showLMEFieldsCheck), // $NON-NLS-0$
                new TableColumn(I18n.I.interpoClosing(), 0.06f, TableCellRenderers.PRICE, "interpolatedClosing").withVisibilityCheck(showLMEFieldsCheck), // $NON-NLS-0$
                new TableColumn(I18n.I.provEvaluation(), 0.06f, TableCellRenderers.PRICE, "provisionalEvaluation").withVisibilityCheck(showLMEFieldsCheck) // $NON-NLS-0$
        };

        return new DefaultTableColumnModel(columns);
    }

    private TableColumn getLinkTypeColumn() {
        switch (this.linkType) {
            case MARKET:
                return new TableColumn(I18n.I.marketName(), 0.2f,
                        TableCellRenderers.MARKETLINK_22, "marketName"); // $NON-NLS$
            case MARKET_NO_LINK:
                return new TableColumn(I18n.I.marketName(), 0.2f,
                        TableCellRenderers.MARKETNAME_22, "marketName"); // $NON-NLS$
            case VWDCODE:
                return new TableColumn("vwdCode", 0.2f, // $NON-NLS$
                        TableCellRenderers.VWDCODELINK_22, "vwdCode"); // $NON-NLS$
            default:
                return new TableColumn(I18n.I.name(), 0.2f,
                        TableCellRenderers.QUOTELINK_22, "name"); // $NON-NLS$
        }
    }

    private int getNumColumns() {
        return 25;
    }


    public DefaultTableDataModel createTableDataModel(int rowCount) {
        return new DefaultTableDataModel(rowCount, getNumColumns())
                .withRowClasses(DefaultTableDataModel.ROW_CLASSES);
    }

    public void addRow(DefaultTableDataModel tableDataModel, int row, InstrumentData instrumentData,
                       QuoteData quoteData, TrendBarData trendBarData, final Price price) {
        addRow(tableDataModel, row, instrumentData, quoteData, null, null, trendBarData, price, null);
    }

    public void addRow(DefaultTableDataModel tableDataModel, int row, InstrumentData instrumentData,
                       QuoteData quoteData, TrendBarData trendBarData, final Price price, HistoryContext historyContext) {
        addRow(tableDataModel, row, instrumentData, quoteData, null, null, trendBarData, price, historyContext);
    }

    public void addRow(DefaultTableDataModel tableDataModel, int row, InstrumentData instrumentData,
                       QuoteData quoteData, TrendBarData trendBarData, final Price price, boolean useCurrentContext) {
        addRow(tableDataModel, row, instrumentData, quoteData, null, null, trendBarData, price, useCurrentContext);
    }

    public void addRow(DefaultTableDataModel tableDataModel, int row, InstrumentData instrumentData,
                       QuoteData quoteData, String positionId, QwiPosition.Type positionType, TrendBarData trendBarData,
                       final Price price, boolean useCurrentContext) {
        addRow(tableDataModel, row, instrumentData, quoteData, positionId, positionType, trendBarData, price,
                useCurrentContext ? HistoryContext.Util.getWithoutBreadcrumb() : null);
    }


    public void addRow(DefaultTableDataModel tableDataModel, int row, InstrumentData instrumentData,
                       QuoteData quoteData, String positionId, QwiPosition.Type positionType, TrendBarData trendBarData,
                       final Price price, HistoryContext historyContext) {

        final CurrentTrendBar currentTrendBar = new CurrentTrendBar(price.getChangePercent(), trendBarData);

        final boolean showWkn = SessionData.INSTANCE.isShowWkn();

        final QuoteWithInstrument qwi = createQwi(instrumentData, quoteData, positionId, positionType)
                .withHistoryContext(historyContext);
        tableDataModel.setValuesAt(row, new Object[]{
                qwi,
                qwi,
                instrumentData.getWkn(),
                instrumentData.getIsin(),
                price,
                quoteData.getMarketVwd(),
                price,
                currentTrendBar,
                price,
                price,
                price,
                price,
                price,
                price.getPreviousPrice(),
                price,
                price,
                price,
                price,
                price,
                price.getOfficialBid(),
                price.getOfficialAsk(),
                price.getUnofficialBid(),
                price.getUnofficialAsk(),
                price.getInterpolatedClosing(),
                price.getProvisionalEvaluation()
        });
    }

    private QuoteWithInstrument createQwi(InstrumentData instrumentData, QuoteData quoteData,
                                          String positionId, QwiPosition.Type positionType) {
        if (positionType != null) {
            return new QwiPosition(instrumentData, quoteData, positionId, positionType);
        }
        return new QuoteWithInstrument(instrumentData, quoteData);
    }

}
