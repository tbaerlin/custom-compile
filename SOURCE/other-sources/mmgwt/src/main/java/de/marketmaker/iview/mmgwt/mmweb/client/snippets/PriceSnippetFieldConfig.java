package de.marketmaker.iview.mmgwt.mmweb.client.snippets;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.marketmaker.iview.dmxml.ContractPriceData;
import de.marketmaker.iview.dmxml.FundPriceData;
import de.marketmaker.iview.dmxml.LMEPriceData;
import de.marketmaker.iview.dmxml.PriceData;
import de.marketmaker.iview.dmxml.PriceDataExtended;
import de.marketmaker.iview.dmxml.QuoteData;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.data.PriceWithSupplement;
import de.marketmaker.iview.mmgwt.mmweb.client.prices.Price;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableColumnModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.RowData;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableCellRenderer;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableCellRenderers;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableColumn;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableColumnAndData;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableColumnAndDataConfig;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableColumnModel;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DateRenderer;
import de.marketmaker.iview.mmgwt.mmweb.client.util.Renderer;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;

import static de.marketmaker.iview.mmgwt.mmweb.client.util.Formatter.LF;
import static de.marketmaker.iview.mmgwt.mmweb.client.util.Formatter.formatTime;
import static de.marketmaker.iview.mmgwt.mmweb.client.util.Renderer.*;


class PriceSnippetFieldConfig implements TableColumnAndDataConfig<PriceDataContainer> {

    public static final String FLIP_ID = "flip-prev"; // $NON-NLS-0$

    abstract class PriceSnippetData implements TableColumnAndData<PriceDataContainer> {

        final TableCellRenderer boldLastPricePushRenderer
                = new TableCellRenderers.PushCompareRenderer<PriceWithSupplement>(Renderer.PRICE_WITH_SUPPLEMENT, "mm-right b") { // $NON-NLS-0$
            @Override
            public PriceWithSupplement getValue(Price price) {
                return price.getLastPrice();
            }

            @Override
            public int compareWithPrevious(Price p) {
                return p.getPrevious().compareLastPrice(p);
            }

            @Override
            protected boolean isWithChange(Price p) {
                return super.isWithChange(p) && p.getPrevious().getLastPrice().getPrice() != null;
            }

            @Override
            protected String getStyle(StringBuffer sb, Price p, boolean push) {
                return super.getStyle(sb, p, push);
            }
        };

        final TableCellRenderer boldVolumePushRenderer = new TableCellRenderers.PushRenderer<Long>(Renderer.VOLUME_LONG, "mm-right b") { // $NON-NLS-0$
            public Long getValue(Price price) {
                return price.getVolumePrice();
            }
        };

        final TableCellRenderer boldBidPushRenderer
                = new TableCellRenderers.PushCompareRenderer<String>(Renderer.PRICE, "mm-right b") { // $NON-NLS-0$
            @Override
            public String getValue(Price price) {
                return price.getBid();
            }

            @Override
            public int compareWithPrevious(Price p) {
                return p.getPrevious().compareBid(p);
            }
        };

        final TableCellRenderer boldAskPushRenderer
                = new TableCellRenderers.PushCompareRenderer<String>(Renderer.PRICE, "mm-pricesnippetfield-right") { // $NON-NLS-0$
            @Override
            public String getValue(Price price) {
                return price.getAsk();
            }

            @Override
            public int compareWithPrevious(Price p) {
                return p.getPrevious().compareAsk(p);
            }
        };

        final TableCellRenderer timePushRenderer
                = new TableCellRenderers.PushRenderer<String>(DateRenderer.fullTime("--:--:--"), "mm-right") { // $NON-NLS-0$ $NON-NLS-1$
            public String getValue(Price price) {
                return price.getDate();
            }
        };


        protected RowData boerse;

        protected RowData aktuell;

        protected RowData kursVol;

        protected RowData diff;

        protected RowData openClose;

        protected RowData highLow;

        protected RowData turnVol;

        protected RowData trades;

        protected RowData quote;

        protected RowData bidAsk;

        protected RowData bidAskVol;

        protected RowData bidAskHighDay;

        protected RowData bidAskLowDay;

        protected RowData spread;

        protected RowData openInterest;

        protected RowData vwap;

        protected RowData previous;

        protected RowData previousClose;

        protected RowData prevOpenClose;

        protected RowData prevHighLow;

        protected RowData prevTurnVol;

        protected RowData year;

        protected RowData yearHigh;

        protected RowData yearLow;

        protected FlipLink link;

        protected RowData fndRepurchasingPrice;

        protected RowData fndIssuePrice;

        protected RowData fndNav;

        protected RowData fndDate;

        protected RowData currency;

        protected RowData fndDiff;

        protected RowData fndDiffPercent;

        protected RowData fndYearHigh;

        protected RowData fndYearLow;

        protected RowData officialBidAsk;

        protected RowData unofficialBidAsk;

        protected RowData interpolatedClosing;

        protected RowData provisionalEvaluation;

        private final PriceSnippetView psv;


        PriceSnippetData(PriceSnippetView psv) {
            this.psv = psv;
        }

        @Override
        public TableColumnModel getTableColumnModel() {
            return getColModel();
        }

        @Override
        public List<RowData> getRowData(PriceDataContainer data) {
            final List<RowData> list = getCompleteDataModel(data);
            onCompleteList(list);
            if (list.contains(this.previous)) {
                this.link.setRow(list.indexOf(this.previous));
            }
            return list;
        }


        protected void onCompleteList(List<RowData> list) {
            // last chance to remove stuff from the data model
        }

        // provide/override renderers for single data fields in the dataset...
        public abstract TableColumnModel getColModel();

        // returns the  dataset, subclasses remove single pieces from this list
        // or add different data in the onCompleteList() method, (it's a crying shame)
        protected List<RowData> getCompleteDataModel(PriceDataContainer data) {
            List<RowData> list = new ArrayList<>();
            QuoteData quoteData = data.getQuoteData();
            PriceData priceData = data.getPriceData();
            FundPriceData fundpriceData = data.getFundpriceData();
            LMEPriceData lmePriceData = data.getLMEPriceData();

            PriceDataExtended pricedataExtended = data.getPricedataExtended();

            Price price = data.getPrice();

            if (priceData != null) {
                this.boerse = new RowData(StringUtil.htmlBold(I18n.I.marketName()), "", quoteData.getMarketName());
                list.add(this.boerse);
                this.aktuell = new RowData(StringUtil.htmlBold(I18n.I.currentPriceShort()),
                        price, LF.formatDateShort(priceData.getDate()));
                list.add(this.aktuell);
                this.kursVol = new RowData(getKursVolLabel(quoteData), price, price);
                list.add(kursVol);
                this.diff = new RowData(I18n.I.changeNetAbbr(), price, price);
                list.add(diff);
                final String close;
                if (pricedataExtended != null && pricedataExtended.getOfficialClose() != null) {
                    close = PRICE.render(pricedataExtended.getOfficialClose()) + I18n.I.officialSuffix();
                }
                else {
                    close = PRICE.render(priceData.getClose());
                }
                this.openClose = new RowData(I18n.I.openCloseAbbr(), priceData.getOpen(), close);
                list.add(openClose);
                this.highLow = new RowData(I18n.I.highLow(), price, price);
                list.add(highLow);
                this.turnVol = new RowData(I18n.I.turnoverVolumeAbbr(), price, price);
                list.add(turnVol);
                this.trades = new RowData(I18n.I.nrOfTradesAbbr(), "", price);
                list.add(trades);
                this.quote = new RowData(StringUtil.htmlBold(I18n.I.bidAskQuotation()), formatTime(priceData.getBidAskDate()), "");
                list.add(quote);
                this.bidAsk = new RowData(I18n.I.bidAsk(), price, price);
                list.add(bidAsk);
                this.bidAskVol = new RowData(I18n.I.bidAskVolumeAbbr(), price, price);
                list.add(bidAskVol);
                if (pricedataExtended != null) {
                    this.bidAskHighDay = new RowData(I18n.I.bidAskHighDay(), PRICE.render(pricedataExtended.getBidHighDay()), PRICE.render(pricedataExtended.getAskHighDay()));
                    list.add(bidAskHighDay);
                    this.bidAskLowDay = new RowData(I18n.I.bidAskLowDay(), PRICE.render(pricedataExtended.getBidLowDay()), PRICE.render(pricedataExtended.getAskLowDay()));
                    list.add(bidAskLowDay);
                }
                this.spread = new RowData(I18n.I.spread(), PRICE.render(priceData.getSpreadNet()),
                        Renderer.PERCENT.render(priceData.getSpreadPercent()));
                list.add(spread);

                if (priceData instanceof ContractPriceData) {
                    final ContractPriceData cpd = (ContractPriceData) priceData;
                    this.openInterest = new RowData(I18n.I.openInterestSettlement(),
                            cpd.getOpenInterest(), PRICE.render(cpd.getSettlement()));
                    list.add(openInterest);
                }
                else if (pricedataExtended != null
                        && (pricedataExtended.getTwas() != null || pricedataExtended.getVwap() != null)) {
                    this.vwap = new RowData(I18n.I.vwaptwas(), Renderer.PRICE_MAX2.render(pricedataExtended.getVwap()),
                            Renderer.PRICE_MAX2.render(pricedataExtended.getTwas()));
                    list.add(vwap);
                }

                if (this.psv != null) {
                    this.link = new FlipLink(this.psv.getTableWidget(), FLIP_ID);
                    this.link.setRow(list.size());
                }
                this.previous = new RowData(StringUtil.htmlBold(I18n.I.previousClose()),
                        this.psv != null ? link : "", LF.formatDateShort(priceData.getPreviousCloseDate()));
                list.add(previous);
                this.previousClose = new RowData(I18n.I.close(),
                        PRICE.render(priceData.getPreviousClose()) + Renderer.SUPPLEMENT.render(priceData.getPreviousCloseSupplement()),
                        VOLUME.renderLong(priceData.getPreviousVolumeDay()));
                list.add(previousClose);
                if (pricedataExtended != null) {
                    this.prevOpenClose = new RowData(I18n.I.openCloseAbbr(), PRICE.render(pricedataExtended.getPreviousOpen()),
                            PRICE.render(pricedataExtended.getPreviousClose())).withFlipId(FLIP_ID);
                    list.add(prevOpenClose);
                    this.prevHighLow = new RowData(I18n.I.highLow(), PRICE.render(pricedataExtended.getPreviousHighDay()),
                            PRICE.render(pricedataExtended.getPreviousLowDay())).withFlipId(FLIP_ID);
                    list.add(prevHighLow);
                    this.prevTurnVol = new RowData(I18n.I.turnoverVolumeAbbr(),
                            TURNOVER.render(pricedataExtended.getPreviousTurnoverDay()),
                            VOLUME.renderLong(pricedataExtended.getPreviousVolumeDay())).withFlipId(FLIP_ID);
                    list.add(prevTurnVol);
                }

                this.year = new RowData(StringUtil.htmlBold(I18n.I.fiftyTwoWeeks()), "", "");
                list.add(year);
                this.yearHigh = new RowData(I18n.I.high(), PRICE.render(priceData.getHigh1Y()),
                        LF.formatDateDdMmYy(priceData.getHigh1YDate()));
                list.add(yearHigh);
                this.yearLow = new RowData(I18n.I.low(), PRICE.render(priceData.getLow1Y()),
                        LF.formatDateDdMmYy(priceData.getLow1YDate()));
                list.add(yearLow);
            }

            final String iso = (quoteData != null) ? quoteData.getCurrencyIso() : null;
            this.currency = new RowData(I18n.I.currency(), "", iso);

            if (fundpriceData != null) {
                this.fndRepurchasingPrice = new RowData(I18n.I.redemption(), "", PRICE.render(fundpriceData.getRepurchasingprice()));
                this.fndIssuePrice = new RowData(I18n.I.issuePrice2(), "", PRICE.render(fundpriceData.getIssueprice()));
                this.fndNav = new RowData(I18n.I.netAssetValue(), "", PRICE.render(fundpriceData.getNetAssetValue()));
                this.fndDate = new RowData(I18n.I.date(), "", LF.formatDate(fundpriceData.getDate()));
                this.fndDiff = new RowData(I18n.I.changeNetAbbr(), "", CHANGE_PRICE.render(fundpriceData.getChangeNet()));
                this.fndDiffPercent = new RowData(I18n.I.changePercentAbbr(), "",
                        CHANGE_PERCENT.render(fundpriceData.getChangePercent()));
                this.fndYearHigh = new RowData(I18n.I.yearHigh(), "", PRICE.render(fundpriceData.getHighYear()));
                this.fndYearLow = new RowData(I18n.I.yearLow(), "", PRICE.render(fundpriceData.getLowYear()));
            }

            if (lmePriceData != null) {
                this.officialBidAsk = new RowData(I18n.I.bidAsk() + I18n.I.officialSuffix(),
                        price, price);
                this.unofficialBidAsk = new RowData(I18n.I.bidAsk() + I18n.I.unOfficialSuffix(),
                        price, price);
                this.interpolatedClosing = new RowData(I18n.I.interpoClosing(), price, "");
                this.provisionalEvaluation = new RowData(I18n.I.provEvaluation(), price, "");
            }

            return list;
        }


        private String getKursVolLabel(QuoteData quoteData) {
            return quoteData.getCurrencyIso() != null
                    ? I18n.I.priceInCurrencyVolume(quoteData.getCurrencyIso())
                    : I18n.I.priceVolume();
        }


    }

    private class FullContent extends PriceSnippetData {
        @Override
        public TableColumnModel getColModel() {
            final TableColumn col2 = new TableColumn(null, 0.4f, TableCellRenderers.DEFAULT_RIGHT);
            final TableColumn col3 = new TableColumn(null, 0.2f, TableCellRenderers.DEFAULT_RIGHT);
            final TableColumn[] result = new TableColumn[]{
                    new TableColumn(null, 0.4f, TableCellRenderers.LABEL),
                    col2,
                    col3
            };
            int i = -1;
            col2.setRowRenderer(++i, this.timePushRenderer);

            col2.setRowRenderer(++i, this.boldLastPricePushRenderer);
            col3.setRowRenderer(i, this.boldVolumePushRenderer);

            col2.setRowRenderer(++i, TableCellRenderers.CHANGE_NET_PUSH);
            col3.setRowRenderer(i, TableCellRenderers.CHANGE_PERCENT_PUSH);

            col2.setRowRenderer(++i, TableCellRenderers.PRICE); //Open

            col2.setRowRenderer(++i, TableCellRenderers.HIGH_PUSH);
            col3.setRowRenderer(i, TableCellRenderers.LOW_PUSH);

            col2.setRowRenderer(++i, TableCellRenderers.TURNOVER_PUSH);
            col3.setRowRenderer(i, TableCellRenderers.VOLUME_LONG_PUSH);

            col3.setRowRenderer(++i, TableCellRenderers.TRADE_LONG_PUSH);
            i++;

            col2.setRowRenderer(++i, this.boldBidPushRenderer);
            col3.setRowRenderer(i, this.boldAskPushRenderer);

            col2.setRowRenderer(++i, TableCellRenderers.BID_VOLUME_PUSH);
            col3.setRowRenderer(i, TableCellRenderers.ASK_VOLUME_PUSH);

            return new DefaultTableColumnModel(result, false);
        }

        @Override
        protected void onCompleteList(List<RowData> list) {
            super.onCompleteList(list);
            list.remove(this.boerse);
            list.remove(this.bidAskHighDay);
            list.remove(this.bidAskLowDay);
        }

        private FullContent(PriceSnippetView psv) {
            super(psv);
        }
    }

    private class WithoutVolumeContent extends PriceSnippetData {
        @Override
        public TableColumnModel getColModel() {
            final TableColumn col2 = new TableColumn(null, 0.4f, TableCellRenderers.DEFAULT_RIGHT);
            final TableColumn col3 = new TableColumn(null, 0.2f, TableCellRenderers.DEFAULT_RIGHT);
            final TableColumn[] result = new TableColumn[]{
                    new TableColumn(null, 0.4f, TableCellRenderers.LABEL),
                    col2,
                    col3
            };
            int i = -1;
            col2.setRowRenderer(++i, this.timePushRenderer);

            col2.setRowRenderer(++i, this.boldLastPricePushRenderer);
            col3.setRowRenderer(i, this.boldVolumePushRenderer);

            col2.setRowRenderer(++i, TableCellRenderers.CHANGE_NET_PUSH);
            col3.setRowRenderer(i, TableCellRenderers.CHANGE_PERCENT_PUSH);

            col2.setRowRenderer(++i, TableCellRenderers.PRICE); //Open

            col2.setRowRenderer(++i, TableCellRenderers.HIGH_PUSH);
            col3.setRowRenderer(i, TableCellRenderers.LOW_PUSH);

            col3.setRowRenderer(++i, TableCellRenderers.TRADE_LONG_PUSH);
            i++;

            col2.setRowRenderer(++i, this.boldBidPushRenderer);
            col3.setRowRenderer(i, this.boldAskPushRenderer);

            return new DefaultTableColumnModel(result, false);

        }

        @Override
        protected void onCompleteList(List<RowData> list) {
            super.onCompleteList(list);
            list.remove(this.boerse);
            list.remove(this.bidAskVol);
            list.remove(this.bidAskHighDay);
            list.remove(this.bidAskLowDay);
            list.remove(this.turnVol);
        }

        private WithoutVolumeContent(PriceSnippetView psv) {
            super(psv);
        }
    }

    private class ReducedContentWithVolume extends PriceSnippetData {
        @Override
        public TableColumnModel getColModel() {
            final TableColumn col1 = new TableColumn(null, 0.4f, TableCellRenderers.LABEL);
            final TableColumn col2 = new TableColumn(null, 0.4f, TableCellRenderers.DEFAULT_RIGHT);
            final TableColumn col3 = new TableColumn(null, 0.2f, TableCellRenderers.DEFAULT_RIGHT);
            int i = 0;
            i++; //row 1
            col2.setRowRenderer(i, this.timePushRenderer);

            i++; //row 2
            col2.setRowRenderer(i, this.boldLastPricePushRenderer);
            col3.setRowRenderer(i, this.boldVolumePushRenderer);

            i++; //row 3
            col2.setRowRenderer(i, TableCellRenderers.CHANGE_NET_PUSH);
            col3.setRowRenderer(i, TableCellRenderers.CHANGE_PERCENT_PUSH);

            i++; // row 4 default renderers used

            i++; //row 5
            col2.setRowRenderer(i, this.boldBidPushRenderer);
            col3.setRowRenderer(i, this.boldAskPushRenderer);

            i++; //row 6
            setVolumeRowRenderers(col2, col3, i);

            return new DefaultTableColumnModel(new TableColumn[]{col1, col2, col3}, false);
        }

        protected void setVolumeRowRenderers(TableColumn col2, TableColumn col3, int i) {
            col2.setRowRenderer(i, TableCellRenderers.BID_VOLUME_PUSH);
            col3.setRowRenderer(i, TableCellRenderers.ASK_VOLUME_PUSH);
        }

        @Override
        protected void onCompleteList(List<RowData> list) {
            super.onCompleteList(list);
            list.remove(this.openClose);
            list.remove(this.highLow);
            list.remove(this.turnVol);
            list.remove(this.trades);
            list.remove(this.bidAskHighDay);
            list.remove(this.bidAskLowDay);
            list.remove(this.spread);
            list.remove(this.prevHighLow);
            list.remove(this.previous);
            list.remove(this.previousClose);
            list.remove(this.prevOpenClose);
            list.remove(this.prevTurnVol);
            list.remove(this.year);
            list.remove(this.yearHigh);
            list.remove(this.yearLow);
            list.remove(this.openInterest);
        }

        private ReducedContentWithVolume(PriceSnippetView psv) {
            super(psv);
        }
    }

    private class ReducedContent extends ReducedContentWithVolume {
        private ReducedContent(PriceSnippetView psv) {
            super(psv);
        }

        @Override
        protected void setVolumeRowRenderers(TableColumn col2, TableColumn col3, int i) {
            // do nothing, because bisAskVol will be removed in #onCompleteList
        }

        @Override
        protected void onCompleteList(List<RowData> list) {
            super.onCompleteList(list);
            list.remove(this.bidAskVol);
        }
    }


    private class FundContent extends PriceSnippetData {
        @Override
        public TableColumnModel getColModel() {
            final TableColumn[] result = new TableColumn[]{
                    new TableColumn(null, 0.4f, TableCellRenderers.LABEL),
                    new TableColumn(null, 0.4f, TableCellRenderers.DEFAULT_RIGHT),
                    new TableColumn(null, 0.2f, TableCellRenderers.DEFAULT_RIGHT),
            };
            return new DefaultTableColumnModel(result, false);
        }


        @Override
        protected void onCompleteList(List<RowData> list) {
            super.onCompleteList(list);
            list.clear();
            list.add(this.fndRepurchasingPrice);
            list.add(this.fndIssuePrice);
            list.add(this.fndNav);
            list.add(this.fndDate);
            list.add(this.currency);
            list.add(this.fndDiff);
            list.add(this.fndDiffPercent);
            list.add(this.fndYearHigh);
            list.add(this.fndYearLow);
        }

        private FundContent(PriceSnippetView psv) {
            super(psv);
        }
    }

    // London Metal Exchange
    private class LmeContent extends PriceSnippetData {

        @Override
        protected void onCompleteList(List<RowData> rowset) {
            super.onCompleteList(rowset);
            rowset.clear();
            rowset.add(this.boerse);  // row 0
            rowset.add(this.aktuell); // row 1
            rowset.add(this.kursVol); // row 2

            rowset.add(this.diff); // row 3

            rowset.add(this.bidAsk); // row 4
            rowset.add(this.bidAskVol); // row 5

            rowset.add(this.officialBidAsk);   // row 6
            rowset.add(this.unofficialBidAsk); // row 7

            rowset.add(this.interpolatedClosing); // row 8
            rowset.add(this.provisionalEvaluation); // row 9
        }

        @Override
        public TableColumnModel getColModel() {
            final TableColumn col1 = new TableColumn(null, 0.4f, TableCellRenderers.LABEL);
            final TableColumn col2 = new TableColumn(null, 0.4f, TableCellRenderers.DEFAULT_RIGHT);
            final TableColumn col3 = new TableColumn(null, 0.2f, TableCellRenderers.DEFAULT_RIGHT);

            // row 0 defaults to the market and default renderer
            int row = 1;
            col2.setRowRenderer(row, this.timePushRenderer);

            row++; // row 2
            col2.setRowRenderer(row, this.boldLastPricePushRenderer);
            col3.setRowRenderer(row, this.boldVolumePushRenderer);

            row++; // row 3
            col2.setRowRenderer(row, TableCellRenderers.CHANGE_NET_PUSH);
            col3.setRowRenderer(row, TableCellRenderers.CHANGE_PERCENT_PUSH);

            row++; // row 4
            col2.setRowRenderer(row, this.boldBidPushRenderer);
            col3.setRowRenderer(row, this.boldAskPushRenderer);

            row++; // row 5
            col2.setRowRenderer(row, TableCellRenderers.BID_VOLUME_PUSH);
            col3.setRowRenderer(row, TableCellRenderers.ASK_VOLUME_PUSH);

            row++; // row 6
            col2.setRowRenderer(row, TableCellRenderers.OFFICIAL_BID_PUSH);
            col3.setRowRenderer(row, TableCellRenderers.OFFICIAL_ASK_PUSH);

            row++; // row 7
            col2.setRowRenderer(row, TableCellRenderers.UNOFFICIAL_BID_PUSH);
            col3.setRowRenderer(row, TableCellRenderers.UNOFFICIAL_ASK_PUSH);

            row++; // row 8
            col2.setRowRenderer(row, TableCellRenderers.INTERPOLATED_CLOSING_PUSH);

            row++; // row 9
            col2.setRowRenderer(row, TableCellRenderers.PROVISIONAL_EVALUATION_PUSH);

            return new DefaultTableColumnModel(new TableColumn[]{col1, col2, col3}, false);
        }

        private LmeContent(PriceSnippetView psv) {
            super(psv);
        }
    }

    private class ContributorContent extends PriceSnippetData {

        @Override
        public TableColumnModel getColModel() {
            final TableColumn col2 = new TableColumn(null, 0.4f, TableCellRenderers.DEFAULT_RIGHT);
            final TableColumn col3 = new TableColumn(null, 0.2f, TableCellRenderers.DEFAULT_RIGHT);
            final TableColumn[] result = new TableColumn[]{
                    new TableColumn(null, 0.4f, TableCellRenderers.LABEL),
                    col2,
                    col3
            };
            int i = 0;

            col2.setRowRenderer(++i, this.boldBidPushRenderer);
            col3.setRowRenderer(i, this.boldAskPushRenderer);

            col2.setRowRenderer(++i, TableCellRenderers.BID_VOLUME_PUSH);
            col3.setRowRenderer(i, TableCellRenderers.ASK_VOLUME_PUSH);

            return new DefaultTableColumnModel(result, false);
        }

        @Override
        protected void onCompleteList(List<RowData> list) {
            super.onCompleteList(list);
            list.remove(this.boerse);
            list.remove(this.aktuell);
            list.remove(this.kursVol);
            list.remove(this.diff);
            list.remove(this.openClose);
            list.remove(this.highLow);
            list.remove(this.turnVol);
            list.remove(this.trades);
        }

        private ContributorContent(PriceSnippetView psv) {
            super(psv);
        }
    }


    public enum Mode {
        FULL_CONTENT("1"), // $NON-NLS-0$
        WITHOUT_VOLUME("3"), // $NON-NLS-0$
        REDUCED_CONTENT_WITH_VOLUME("2"), // $NON-NLS-0$
        FUND_OTC("4"), // $NON-NLS-0$
        REDUCED_CONTENT("5"), // $NON-NLS-0$
        LME_CONTENT("6"), // $NON-NLS-0$
        CONTRIBUTOR_CONTENT("7"), // $NON-NLS-0$
        ;

        private final String id;

        Mode(String id) {
            this.id = id;
        }

        public String getId() {
            return id;
        }
    }

    private final Map<String, TableColumnAndData<PriceDataContainer>> configs;

    public PriceSnippetFieldConfig(PriceSnippetView psv) {
        this.configs = new HashMap<>();
        this.configs.put(Mode.FULL_CONTENT.getId(), new FullContent(psv));
        this.configs.put(Mode.REDUCED_CONTENT_WITH_VOLUME.getId(), new ReducedContentWithVolume(psv));
        this.configs.put(Mode.WITHOUT_VOLUME.getId(), new WithoutVolumeContent(psv));
        this.configs.put(Mode.FUND_OTC.getId(), new FundContent(psv));
        this.configs.put(Mode.REDUCED_CONTENT.getId(), new ReducedContent(psv));
        this.configs.put(Mode.LME_CONTENT.getId(), new LmeContent(psv));
        this.configs.put(Mode.CONTRIBUTOR_CONTENT.getId(), new ContributorContent(psv));
    }

    public PriceSnippetFieldConfig() {
        this(null);
    }


    @Override
    public TableColumnAndData<PriceDataContainer> getTableColumnAndData(String id) {
        return this.configs.get(id);
    }
}
