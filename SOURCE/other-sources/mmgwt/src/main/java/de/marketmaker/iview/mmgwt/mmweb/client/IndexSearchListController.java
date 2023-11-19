/*
 * IndexSearchListController.java
 *
 * Created on 16.07.2008 11:31:49
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */

package de.marketmaker.iview.mmgwt.mmweb.client;

import com.extjs.gxt.ui.client.util.Format;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.rpc.AsyncCallback;
import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.iview.dmxml.InstrumentData;
import de.marketmaker.iview.dmxml.MSCListDetailElement;
import de.marketmaker.iview.dmxml.MSCListDetails;
import de.marketmaker.iview.dmxml.QuoteData;
import de.marketmaker.iview.dmxml.ResponseType;
import de.marketmaker.iview.dmxml.STKFinder;
import de.marketmaker.iview.dmxml.STKFinderElement;
import de.marketmaker.iview.mmgwt.mmweb.client.data.CurrentTrendBar;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.data.TrendBarData;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PlaceChangeEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PricesUpdatedEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PushRegisterEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PushRegisterHandler;
import de.marketmaker.iview.mmgwt.mmweb.client.history.HistoryContext;
import de.marketmaker.iview.mmgwt.mmweb.client.history.HistoryToken;
import de.marketmaker.iview.mmgwt.mmweb.client.history.ItemListContext;
import de.marketmaker.iview.mmgwt.mmweb.client.paging.PageLoader;
import de.marketmaker.iview.mmgwt.mmweb.client.paging.PagingFeature;
import de.marketmaker.iview.mmgwt.mmweb.client.prices.Price;
import de.marketmaker.iview.mmgwt.mmweb.client.prices.PriceSupport;
import de.marketmaker.iview.mmgwt.mmweb.client.push.PushRenderItem;
import de.marketmaker.iview.mmgwt.mmweb.client.runtime.Permutation;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableColumnModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableDataModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.SimpleVisibilityCheck;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableCellRenderers;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableColumn;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableColumnModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.VisibilityCheck;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;
import de.marketmaker.iview.mmgwt.mmweb.client.util.LinkListener;
import de.marketmaker.iview.mmgwt.mmweb.client.util.PdfOptionSpec;
import de.marketmaker.iview.mmgwt.mmweb.client.util.SortLinkSupport;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.view.ContentContainer;
import de.marketmaker.iview.tools.i18n.NonNLS;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static de.marketmaker.iview.mmgwt.mmweb.client.util.ErrorMessageUtil.getMessage;

@NonNLS
public class IndexSearchListController extends AbstractPageController implements PageLoader,
        PushRegisterHandler {
    public static final int DEFAULT_COUNT = 50;

    public static final String LIST_ID = "DZ";

    private final static int NUM_COLUMNS = 25;

    private DmxmlContext.Block<MSCListDetails> blockIndizes;

    private DmxmlContext.Block<STKFinder> blockConstituents;

    private PagingFeature pagingFeature;

    private IndexSearchListView view;

    private boolean reduceCurrencyName;

    private DefaultTableDataModel tdmIndizes;

    private DefaultTableDataModel tdmConstituents;

    private final PriceSupport priceSupport = new PriceSupport(this);

    protected IndexSearchListController(ContentContainer contentContainer) {
        super(contentContainer);
        init();
    }

    private void init() {
        this.blockIndizes = createDetailsBlock();
        this.blockIndizes.setParameter("disablePaging", "true");
        this.blockConstituents = createFinderBlock();

        this.pagingFeature = new PagingFeature(this, this.blockConstituents, DEFAULT_COUNT);
    }

    private DmxmlContext.Block<MSCListDetails> createDetailsBlock() {
        final DmxmlContext.Block<MSCListDetails> result = this.context.addBlock("MSC_List_Details");
        result.setParameter("sortBy", "name");
        result.setParameter("ascending", "true");
        return result;
    }

    private DmxmlContext.Block<STKFinder> createFinderBlock() {
        final DmxmlContext.Block<STKFinder> result = this.context.addBlock("STK_Finder");
        final String providerPreference = SessionData.INSTANCE.getGuiDefValue("providerPreference");
        result.setParameter("providerPreference", providerPreference);
        result.setParameter("sortBy", "name");
        result.setParameter("ascending", "true");
        return result;
    }

    LinkListener<String> getSortLinkListener() {
        return new SortLinkSupport(this.blockConstituents, new Command() {
            public void execute() {
                reload();
            }
        });
    }

    public PagingFeature getPagingFeature() {
        return this.pagingFeature;
    }

    public void onPlaceChange(PlaceChangeEvent event) {
        final HistoryToken historyToken = event.getHistoryToken();
        final String marketOverviewVariant = SessionData.INSTANCE.getGuiDefValue("market-overview-variant");
        final String marketOverviewSuffix = marketOverviewVariant == null ? "" : ("|" + marketOverviewVariant);
        String param1 = historyToken.get(1, null);
        param1 = param1 == null ? "" : param1;
        final String param2 = historyToken.get(2, null);
        this.reduceCurrencyName = param1.toLowerCase().startsWith("devisen") || "reduceCurrencyName".equals(param2);
        this.blockIndizes.setParameter("listid", "listoverview|" + LIST_ID + "|elements|" + param1 + marketOverviewSuffix);
        this.pagingFeature.resetPaging();
        final String listName = historyToken.getAllParamCount() >= 3 ? param2 : param1;
        AbstractMainController.INSTANCE.getView().setContentHeader(AbstractMainController.INSTANCE.getContentHeader(historyToken.getControllerId())
                + " \"" + Format.htmlEncode(listName.replace(".", " - ")) + "\"");
        reload();
    }

    public void reload() {
        refresh();
    }

    protected void onResult() {
        this.priceSupport.invalidateRenderItems();

        if (this.view == null) {
            // doing this in init() would cause problems with high load in some browsers (chrome),
            // so we go for lazy init
            this.view = new IndexSearchListView(this);
        }
        this.pagingFeature.onResult();

        final TrendBarData tbd = createTrendBarData();
        this.tdmIndizes = createModel(this.blockIndizes, tbd, false);

        // this.tdmConstituents = createModel(this.blockConstituents, tbd);

        if (!this.blockConstituents.isEnabled()) {
            this.blockConstituents.setEnabled(true);

            this.blockConstituents.issueRequest(new AsyncCallback<ResponseType>() {
                @Override
                public void onFailure(Throwable throwable) {
                    Firebug.error(IndexSearchListController.class.getSimpleName() + ": STK_Finder constituents callback failed!", throwable);
                }

                @Override
                public void onSuccess(ResponseType responseType) {
                    if (blockConstituents.isResponseOk()) {
                        tdmConstituents = createModel(blockConstituents, tbd);
                        //onPush();
                    }
                }
            });
            return;
        }
        this.view.show(this.tdmIndizes, this.tdmConstituents);
        this.priceSupport.activate();
    }

    private DefaultTableDataModel createModel(final DmxmlContext.Block<MSCListDetails> block,
                                              TrendBarData tbd, boolean isConstituents) {
        if (!block.isResponseOk()) {
            return DefaultTableDataModel.create(getMessage(block.getError()));
        }
        final List<MSCListDetailElement> elements = block.getResult().getElement();

        final DefaultTableDataModel result = new DefaultTableDataModel(elements.size(), NUM_COLUMNS)
                .withRowClasses(DefaultTableDataModel.ROW_CLASSES);

        int row = 0;
        for (final MSCListDetailElement e : elements) {
            if (this.reduceCurrencyName) {
                StringUtil.reduceCurrencyNameLength(e.getInstrumentdata());
            }
            final Price price = Price.create(e);
            addRow(result, row, e.getInstrumentdata(), e.getQuotedata(), null, null, tbd, price, ItemListContext.createForPortrait(e, elements, I18n.I.overview()));
            row++;
            if (isConstituents) {
                this.blockConstituents.setParameter("query", e.getQuotedata().getVwdcode());
            }
        }
        return result;
    }

    private DefaultTableDataModel createModel(final DmxmlContext.Block<STKFinder> block,
                                              TrendBarData tbd) {
        if (!block.isResponseOk()) {
            return DefaultTableDataModel.create(getMessage(block.getError()));
        }
        final List<STKFinderElement> elements = block.getResult().getElement();

        final DefaultTableDataModel result = new DefaultTableDataModel(elements.size(), NUM_COLUMNS)
                .withRowClasses(DefaultTableDataModel.ROW_CLASSES);

        int row = 0;
        for (final STKFinderElement e : elements) {
            if (this.reduceCurrencyName) {
                StringUtil.reduceCurrencyNameLength(e.getInstrumentdata());
            }
            addRow(result, row, e.getInstrumentdata(), e.getQuotedata(), null, null, tbd, null, null);
            row++;
        }
        return result;
    }

    TableColumnModel createColumnModel() {
        final boolean displayVolume = true;
        final boolean displayMarket = true;
        final boolean withWkn = true;
        final boolean withBidAskVolume = true;
        final boolean withDzBankLink = Permutation.GIS.isActive();
        final boolean displaySymbol = false;
        final boolean displayBidAskVolume = false;
        final boolean displayDzBankLink = false;
        final boolean displayLMEFields = false;

        final VisibilityCheck showWknCheck = SimpleVisibilityCheck.valueOf(displaySymbol && SessionData.INSTANCE.isShowWkn());
        final VisibilityCheck showIsinCheck = SimpleVisibilityCheck.valueOf(displaySymbol && SessionData.INSTANCE.isShowIsin());
        final VisibilityCheck vcVolume = SimpleVisibilityCheck.valueOf(displayVolume);
        final VisibilityCheck vcMarket = SimpleVisibilityCheck.valueOf(displayMarket);
        final VisibilityCheck vcBidAskVolume = SimpleVisibilityCheck.valueOf(displayBidAskVolume);
        final VisibilityCheck dzBankLink = SimpleVisibilityCheck.valueOf(displayDzBankLink);
        final VisibilityCheck showLMEFieldsCheck = SimpleVisibilityCheck.valueOf(displayLMEFields && FeatureFlags.Feature.LME_CHANGES_2014.isEnabled());

        // final ListDetailsHelper.LinkType linkType = ListDetailsHelper.LinkType.NAME;

        final TableColumn[] columns = new TableColumn[]{
                new TableColumn("", 0.05f, TableCellRenderers.VR_ICON_LINK).withVisibilityCheck(dzBankLink),
                new TableColumn(I18n.I.name(), 0.2f, TableCellRenderers.QUOTELINK_22, "name"),
                new TableColumn("WKN", 0.05f, TableCellRenderers.DEFAULT, "wkn").withVisibilityCheck(showWknCheck),
                new TableColumn("ISIN", 0.05f, TableCellRenderers.DEFAULT, "isin").withVisibilityCheck(showIsinCheck), 
                new TableColumn(I18n.I.price(), 0.05f, TableCellRenderers.LAST_PRICE_WITH_SUPPLEMENT_PUSH),
                new TableColumn(I18n.I.market(), 0.05f, TableCellRenderers.STRING_CENTER).withVisibilityCheck(vcMarket),
                new TableColumn("+/-", 0.05f, TableCellRenderers.CHANGE_PERCENT_PUSH, "changePercent"),
                new TableColumn(I18n.I.trend(), 0.05f, TableCellRenderers.TRENDBAR, "changePercent").withCellClass("mm-middle"),//, 
                new TableColumn(I18n.I.time(), 0.07f, TableCellRenderers.DATE_OR_TIME_PUSH, "date").withCellClass("mm-center"), 
                new TableColumn(I18n.I.bid(), 0.06f, TableCellRenderers.BID_PUSH),
                new TableColumn(I18n.I.ask(), 0.06f, TableCellRenderers.ASK_PUSH),
                new TableColumn(I18n.I.bidVolumeAbbr(), 0.06f, TableCellRenderers.BID_VOLUME_PUSH, "bidVolume").withVisibilityCheck(vcBidAskVolume),
                new TableColumn(I18n.I.askVolumeAbbr(), 0.06f, TableCellRenderers.ASK_VOLUME_PUSH, "askVolume").withVisibilityCheck(vcBidAskVolume),
                new TableColumn(I18n.I.previousClose(), 0.07f, TableCellRenderers.PRICE_WITH_SUPPLEMENT, "previousClose"), 
                new TableColumn(I18n.I.low(), 0.06f, TableCellRenderers.LOW_PUSH),
                new TableColumn(I18n.I.high(), 0.06f, TableCellRenderers.HIGH_PUSH),
                new TableColumn(I18n.I.volume(), 0.11f, TableCellRenderers.VOLUME_LONG_PUSH, "volume").withCellClass("mm-right").withVisibilityCheck(vcVolume), 
                new TableColumn(I18n.I.turnoverDay(), 0.13f, TableCellRenderers.TURNOVER_PUSH, "turnoverDay").withVisibilityCheck(vcVolume), 
                new TableColumn(I18n.I.numTrades(), 0.13f, TableCellRenderers.TRADE_LONG_PUSH, "numberOfTrades").withVisibilityCheck(vcVolume),
                new TableColumn(I18n.I.bid()  + I18n.I.officialSuffix(), 0.06f, TableCellRenderers.PRICE, "officialBid").withVisibilityCheck(showLMEFieldsCheck),
                new TableColumn(I18n.I.ask() + I18n.I.officialSuffix(), 0.06f, TableCellRenderers.PRICE, "officialAsk").withVisibilityCheck(showLMEFieldsCheck),
                new TableColumn(I18n.I.bid() + I18n.I.unOfficialSuffix(), 0.06f, TableCellRenderers.PRICE, "unofficialBid").withVisibilityCheck(showLMEFieldsCheck),
                new TableColumn(I18n.I.ask()  + I18n.I.unOfficialSuffix(), 0.06f, TableCellRenderers.PRICE, "unofficialAsk").withVisibilityCheck(showLMEFieldsCheck),
                new TableColumn(I18n.I.interpoClosing(), 0.06f, TableCellRenderers.PRICE, "interpolatedClosing").withVisibilityCheck(showLMEFieldsCheck),
                new TableColumn(I18n.I.provEvaluation(), 0.06f, TableCellRenderers.PRICE, "provisionalEvaluation").withVisibilityCheck(showLMEFieldsCheck)
        };

        assert columns.length == NUM_COLUMNS;

        return new DefaultTableColumnModel(columns);
    }

    private void addRow(DefaultTableDataModel tableDataModel, int row, InstrumentData instrumentData,
                       QuoteData quoteData, String positionId, QwiPosition.Type positionType, TrendBarData trendBarData,
                       final Price price, HistoryContext historyContext) {

        final CurrentTrendBar currentTrendBar = new CurrentTrendBar(price.getChangePercent(), trendBarData);

        final QuoteWithInstrument qwi;
        if (positionType != null) {
            qwi = new QwiPosition(instrumentData, quoteData, positionId, positionType);
        } else {
            qwi = new QuoteWithInstrument(instrumentData, quoteData);
        }
        if (historyContext != null) {
            qwi.withHistoryContext(historyContext);
        }

        // historyContext may be null

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


    private TrendBarData createTrendBarData() {
        final List<MSCListDetailElement> elements = new ArrayList<MSCListDetailElement>();
        if (this.blockIndizes.isResponseOk()) {
            elements.addAll(this.blockIndizes.getResult().getElement());
        }
        /*
        if (this.blockConstituents.isResponseOk()) {
            elements.addAll(this.blockConstituents.getResult().getElement());
        }
        */
        return TrendBarData.create(elements);
    }


    /**
     * Provide special print version of this page.
     *
     * @return the special print version.
     */
    @Override
    public String getPrintHtml() {
        return
                "<div class=\"mm-printHeader\">" + AbstractMainController.INSTANCE.getView().getContentHeader().asString() + "</div>" +
                        this.view.getPrintHtml() +
                        "<div class=\"mm-printFooter\">" + this.pagingFeature.getPageString() + "</div>";
    }

    public String getListId() {
        if (this.blockConstituents.isResponseOk()) {
            return this.blockConstituents.getParameter("listid");
        }
        else {
            return null;
        }
    }

    @Override
    public PdfOptionSpec getPdfOptionSpec() {
        final Map<String, String> map = new HashMap<String, String>();
        map.put("listid", getListId());
        map.put("sortBy", this.blockConstituents.getParameter("sortBy"));
        map.put("ascending", this.blockConstituents.getParameter("ascending"));
        return new PdfOptionSpec("quotelist.pdf", map, "pdf_options_format");
    }

    public ArrayList<PushRenderItem> onPushRegister(PushRegisterEvent event) {
        final ArrayList<PushRenderItem> list1 = addToEvent(event, this.blockIndizes);
        /*
        final ArrayList<PushRenderItem> list2 = addToEvent(event, this.blockConstituents);
        if (list1 == null) {
            return list2;
        }
        if (list2 != null) {
            list1.addAll(list2);
        }
        */
        return list1;
    }

    private ArrayList<PushRenderItem> addToEvent(PushRegisterEvent event, DmxmlContext.Block<MSCListDetails> block) {
        if (block.isResponseOk()) {
            final int numAdded = event.addVwdcodes(block.getResult());
            if (numAdded == block.getResult().getElement().size()) {
                event.addComponentToReload(block, this);
            }
            if (numAdded > 0) {
                return this.view.getRenderItems(block == this.blockIndizes ?
                        this.tdmIndizes : this.tdmConstituents, block == this.blockIndizes);
            }
        }
        return null;
    }

    public void onPricesUpdated(PricesUpdatedEvent event) {
        if (!event.isPushedUpdate()) {
            this.view.show(this.tdmIndizes, this.tdmConstituents);
        }
    }

    @Override
    public void deactivate() {
        super.deactivate();
        this.priceSupport.deactivate();
    }
}
