/*
* PortfolioController.java
*
* Created on 24.09.2008 13:57:39
*
* Copyright (c) vwd GmbH. All Rights Reserved.
*/
package de.marketmaker.iview.mmgwt.mmweb.client.watchlist;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.rpc.AsyncCallback;

import de.marketmaker.itools.gwtutil.client.widgets.IconImage;
import de.marketmaker.iview.dmxml.FundPriceData;
import de.marketmaker.iview.dmxml.InstrumentData;
import de.marketmaker.iview.dmxml.LMEPriceData;
import de.marketmaker.iview.dmxml.NWSLatestNews;
import de.marketmaker.iview.dmxml.PFEvaluation;
import de.marketmaker.iview.dmxml.PFOrder;
import de.marketmaker.iview.dmxml.PFOrderlist;
import de.marketmaker.iview.dmxml.PortfolioElement;
import de.marketmaker.iview.dmxml.PortfolioPositionElement;
import de.marketmaker.iview.dmxml.PortfolioWerte;
import de.marketmaker.iview.dmxml.QuoteData;
import de.marketmaker.iview.dmxml.ResponseType;
import de.marketmaker.iview.mmgwt.mmweb.client.AbstractMainController;
import de.marketmaker.iview.mmgwt.mmweb.client.AbstractPageController;
import de.marketmaker.iview.mmgwt.mmweb.client.FeatureFlags;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.PageController;
import de.marketmaker.iview.mmgwt.mmweb.client.QuoteWithInstrument;
import de.marketmaker.iview.mmgwt.mmweb.client.QwiPosition;
import de.marketmaker.iview.mmgwt.mmweb.client.ResponseTypeCallback;
import de.marketmaker.iview.mmgwt.mmweb.client.data.AppConfig;
import de.marketmaker.iview.mmgwt.mmweb.client.data.CurrentTrendBar;
import de.marketmaker.iview.mmgwt.mmweb.client.data.InstrumentTypeEnum;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.data.TrendBarData;
import de.marketmaker.iview.mmgwt.mmweb.client.data.User;
import de.marketmaker.iview.mmgwt.mmweb.client.desktop.ChartIcon;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PlaceChangeEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PricesUpdatedEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PushRegisterEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PushRegisterHandler;
import de.marketmaker.iview.mmgwt.mmweb.client.history.HistoryToken;
import de.marketmaker.iview.mmgwt.mmweb.client.history.ItemListContext;
import de.marketmaker.iview.mmgwt.mmweb.client.prices.Price;
import de.marketmaker.iview.mmgwt.mmweb.client.prices.PriceDataType;
import de.marketmaker.iview.mmgwt.mmweb.client.prices.PriceSupport;
import de.marketmaker.iview.mmgwt.mmweb.client.push.PushRenderItem;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableColumnModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableDataModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.SimpleVisibilityCheck;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableCellRenderers;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableColumn;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableColumnModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.VisibilityCheck;
import de.marketmaker.iview.mmgwt.mmweb.client.util.BlockPipe;
import de.marketmaker.iview.mmgwt.mmweb.client.util.BlockPipeResult;
import de.marketmaker.iview.mmgwt.mmweb.client.util.Dialog;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;
import de.marketmaker.iview.mmgwt.mmweb.client.util.LinkListener;
import de.marketmaker.iview.mmgwt.mmweb.client.util.PdfOptionSpec;
import de.marketmaker.iview.mmgwt.mmweb.client.util.Renderer;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.UrlBuilder;
import de.marketmaker.iview.mmgwt.mmweb.client.util.WatchlistPortfolioUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.watchlist.PositionNoteController.PositionNoteRenderer.CommentViewMode;

/**
 * @author Michael Lösch
 * @author Oliver Flege
 */
public class PortfolioController extends AbstractPageController implements PageController,
        PushRegisterHandler {

    /**
     * This is the instance used to manipulate a user's portfolios; other instances can be
     * created to show portfolios created by others in read-only mode.
     */
    public static final PortfolioController INSTANCE =
            new PortfolioController(SessionData.INSTANCE.getUser().getVwdId(), false);

    private final PortfolioView view;

    private final DmxmlContext.Block<PFEvaluation> pfBlock;

    private final DmxmlContext.Block<PFOrderlist> pfOrdersBlock;

    private final DmxmlContext.Block<NWSLatestNews> latesNewsBlock;

    private final BlockPipe pipe;

    private String currentPortfolioId;

    private int currentPortfolioCount;


    private String controllerToken = null;

    private static final int TAB_COLS_PORTFOLIOS = 45;

    private static final int TAB_COLS_ORDERS = 8;

    private final boolean readOnly;

    private String chartPeriod = AbstractWatchlistView.CHART_PERIOD_DEFAULT;

    private PortfolioView.ViewMode viewMode = PortfolioView.VIEW_TABLE;

    private WatchlistChartPrintView printView = null;

    private List<ChartIcon> currentListCharts;

    private final TableCellRenderers.IconLinkRenderer<QuoteWithInstrument> delPositionRenderer;

    private final TableCellRenderers.IconLinkRenderer editPositionRenderer;

    private final TableCellRenderers.IconLinkRenderer<QuoteWithInstrument> sellPositionRenderer;

    private final PortfolioVisualizationContainer pv;

    private final PriceSupport priceSupport = new PriceSupport(this);

    private final AsyncCallback<ResponseType> selectCallback = new ResponseTypeCallback() {
        protected void onResult() {
            onPortfolioSelected();
        }
    };

    private DefaultTableDataModel ordersTableModel;

    private DefaultTableColumnModel tableColumnModel;

    public static String AVG_PRICE;

    private DefaultTableDataModel portfolioTdm;

    private CommentViewMode commentViewMode = CommentViewMode.SHORT;

    public static PortfolioController create(String uid) {
        return new PortfolioController(uid, true);
    }

    private PortfolioController(String uid, boolean readOnly) {
        this.context.setCancellable(false);
        this.pfBlock = this.context.addBlock("PF_Evaluation"); // $NON-NLS-0$
        this.pfBlock.setParameter("userid", uid); // $NON-NLS-0$
        this.pfBlock.setParameter("disablePaging", "true"); // $NON-NLS-0$ $NON-NLS-1$
        this.pfBlock.setParameter("extendedPriceData", "true"); // $NON-NLS-0$ $NON-NLS-1$
        this.pfOrdersBlock = this.context.addBlock("PF_Orderlist"); // $NON-NLS-0$
        this.pfOrdersBlock.setParameter("userid", uid); // $NON-NLS-0$
        this.latesNewsBlock = this.context.addBlock("NWS_LatestNews"); // $NON-NLS-0$

        this.pipe = new BlockPipe(this.pfBlock, this.context, "iid", true, new BlockPipeResult<PFEvaluation>() { // $NON-NLS-0$

            public String[] getResult(DmxmlContext.Block<PFEvaluation> pfEvaluationBlock) {
                createTableColumnModel(pfEvaluationBlock.getResult().getPortfolio().getPortfolioid(),
                        pfEvaluationBlock.getResult().getPortfolio().getPortfolioCurrency());
                final int size = pfEvaluationBlock.getResult().getElements().getPosition().size();
                final String[] result = new String[size];
                for (int i = 0; i < size; i++) {
                    final String iid = pfEvaluationBlock.getResult().getElements().getPosition().get(i).getInstrumentdata().getIid();
                    result[i] = iid;
                }
                return result;
            }
        }).setNext(this.latesNewsBlock);

        this.readOnly = readOnly;

        this.delPositionRenderer = new TableCellRenderers.IconLinkRenderer<>((LinkListener<QuoteWithInstrument>) (quoteWithInstrumentLinkContext, e) -> {
            assert quoteWithInstrumentLinkContext.getData() instanceof QwiPosition : "data must be of type QwiPosition: " + quoteWithInstrumentLinkContext.getData().getClass().getSimpleName();
            QwiPosition qwi = (QwiPosition) quoteWithInstrumentLinkContext.getData();
            deleteOrder(qwi.getPositionId());
        }, IconImage.get("mm-small-remove"), I18n.I.deleteOrder());  // $NON-NLS-0$

        this.editPositionRenderer = new TableCellRenderers.IconLinkRenderer<>((LinkListener<QuoteWithInstrument>) (quoteWithInstrumentLinkContext, e) -> {
            assert quoteWithInstrumentLinkContext.getData() instanceof QwiPosition : "data must be of type QwiPosition: " + quoteWithInstrumentLinkContext.getData().getClass().getSimpleName();
            final QwiPosition qwi = (QwiPosition) quoteWithInstrumentLinkContext.getData();
            updateOrder(qwi.getQuoteData().getQid(), qwi.getPositionId());
        }, IconImage.get("mm-small-edit"), I18n.I.editOrder());  // $NON-NLS-0$


        this.sellPositionRenderer = new TableCellRenderers.IconLinkRenderer<QuoteWithInstrument>((LinkListener<QuoteWithInstrument>) (quoteWithInstrumentLinkContext, e) -> {
            assert quoteWithInstrumentLinkContext.getData() instanceof QwiPosition : "data must be of type QwiPosition: " + quoteWithInstrumentLinkContext.getData().getClass().getSimpleName();
            final QwiPosition qwi = (QwiPosition) quoteWithInstrumentLinkContext.getData();
            createSellOrder(qwi);
        }, IconImage.get("mm-small-remove"), I18n.I.executeSellOrder()) {  // $NON-NLS$
            @Override
            public void render(Object data, StringBuffer sb, Context context) {
                if(data == null) {
                    return;
                }
                super.render(data, sb, context);
            }
        };

        // the userid is necessary for advisory solution regardless whether the portfolio is readonly or not,
        // because we do not have a session in dm.vwd.com.
        this.pv = PortfolioVisualizationContainer.create(readOnly || SessionData.isAsDesign() ? uid : null);
        this.view = new PortfolioView(this, this.pv.getView(), readOnly);

        final List<PortfolioElement> pfs = this.readOnly
                ? SessionData.INSTANCE.getProfiPortfolios()
                : SessionData.INSTANCE.getPortfolios();

        final int portfolioCount = (pfs != null) ? pfs.size() : 0;
        setCurrentPortfolioCount(portfolioCount);
        if (pfs == null) { //doesn't make sense to continue. this happens when an AS-User has NO vwdid.
            return;
        }
        this.currentPortfolioId = (portfolioCount > 0) ? pfs.get(0).getPortfolioid() : null;
        if (portfolioCount > 0) {
            this.view.buildPortfolioMenu(pfs);
        }

    }

    @SuppressWarnings("UnusedAssignment")
    private void createTableColumnModel(String portfolioId, String portfolioCurrency) {
        AVG_PRICE = I18n.I.buyPriceIn(portfolioCurrency);

        final VisibilityCheck vwdRelease2014OrDZRelease2016 = SimpleVisibilityCheck.valueOf(FeatureFlags.Feature.VWD_RELEASE_2014.isEnabled() || FeatureFlags.Feature.DZ_RELEASE_2016.isEnabled());
        final VisibilityCheck lmeChanges2014 = SimpleVisibilityCheck.valueOf(FeatureFlags.Feature.LME_CHANGES_2014.isEnabled());

        final TableColumn[] columns = new TableColumn[TAB_COLS_PORTFOLIOS];
        int i = 0;
        columns[i++] = new TableColumn("", 14f, this.sellPositionRenderer).setFixed().withId("A"); // $NON-NLS-0$ $NON-NLS-1$
        columns[i++] = new TableColumn(I18n.I.type(), -1f, TableCellRenderers.STRING).withId("B");  // $NON-NLS-0$
        columns[i++] = new TableColumn(I18n.I.name(), -1f, TableCellRenderers.DEFAULT).withId("C");  // $NON-NLS-0$
        columns[i++] = new TableColumn("ISIN", -1f, TableCellRenderers.STRING_CENTER).withId("D"); // $NON-NLS-0$ $NON-NLS-1$
        columns[i++] = new TableColumn(I18n.I.marketName(), -1f, TableCellRenderers.STRING_CENTER).withId("E");  // $NON-NLS-0$
        columns[i++] = new TableColumn(I18n.I.countAndValue(), -1f, TableCellRenderers.STRING_RIGHT).withId("F");  // $NON-NLS-0$
        columns[i++] = new TableColumn(I18n.I.costValueIn(portfolioCurrency), -1f, TableCellRenderers.PRICE23).withId("G");  // $NON-NLS-0$
        columns[i++] = new TableColumn(AVG_PRICE, -1f, TableCellRenderers.PRICE23).withId("H"); // $NON-NLS-0$
        columns[i++] = new TableColumn(I18n.I.currentPrice(), -1f, TableCellRenderers.LAST_PRICE_WITH_SUPPLEMENT_PUSH).withId("I");  // $NON-NLS-0$
        columns[i++] = new TableColumn(I18n.I.profitAndLossPercentAbbr(), -1f, TableCellRenderers.CHANGE_PERCENT).withId("J");  // $NON-NLS-0$
        columns[i++] = new TableColumn(I18n.I.profitAndLossAbbr(), -1f, TableCellRenderers.CHANGE_NET).withId("K");  // $NON-NLS-0$
        columns[i++] = new TableColumn(I18n.I.profitAndLossTrend(), -1f, TableCellRenderers.TRENDBAR).withId("AE");  // $NON-NLS-0$
        columns[i++] = new TableColumn("+/- %", -1f, TableCellRenderers.CHANGE_PERCENT_PUSH).withId("S"); // $NON-NLS-0$ $NON-NLS-1$
        columns[i++] = new TableColumn("+/-", -1f, TableCellRenderers.CHANGE_NET_PUSH).withId("T"); // $NON-NLS-0$ $NON-NLS-1$
        columns[i++] = new TableColumn(I18n.I.trendWithSign(), -1f, TableCellRenderers.TRENDBAR).withId("AF");  // $NON-NLS-0$
        columns[i++] = new TableColumn(I18n.I.currentValueIn(portfolioCurrency), -1f, TableCellRenderers.PRICE23).withId("L");  // $NON-NLS-0$
        columns[i++] = new TableColumn("WKN", -1f, TableCellRenderers.STRING_CENTER).withId("M"); // $NON-NLS-0$ $NON-NLS-1$
        columns[i++] = new TableColumn(I18n.I.openPrice(), -1f, TableCellRenderers.PRICE23).withId("N");  // $NON-NLS-0$
        columns[i++] = new TableColumn(I18n.I.previousClose(), -1f, TableCellRenderers.PREVIOUS_PRICE_PUSH).withId("O");  // $NON-NLS-0$
        columns[i++] = new TableColumn(I18n.I.volume(), -1f, TableCellRenderers.VOLUME_LONG_PUSH).withId("Q");  // $NON-NLS-0$
        columns[i++] = new TableColumn(I18n.I.dateTime(), -1f, TableCellRenderers.COMPACT_DATETIME_PUSH).withId("R");  // $NON-NLS-0$
        columns[i++] = new TableColumn(I18n.I.yearLow(), -1f, TableCellRenderers.PRICE23).withId("U");  // $NON-NLS-0$
        columns[i++] = new TableColumn(I18n.I.yearHigh(), -1f, TableCellRenderers.PRICE23).withId("V");  // $NON-NLS-0$
        columns[i++] = new TableColumn(I18n.I.volumeByLastPrice(), -1f, TableCellRenderers.VOLUME_PRICE_PUSH).withId("W");  // $NON-NLS-0$
        columns[i++] = new TableColumn(I18n.I.askPrice(), -1f, TableCellRenderers.ASK_PUSH).withId("X");  // $NON-NLS-0$
        columns[i++] = new TableColumn(I18n.I.askVolume(), -1f, TableCellRenderers.ASK_VOLUME_PUSH).withId("Y");  // $NON-NLS-0$
        columns[i++] = new TableColumn(I18n.I.bidPrice(), -1f, TableCellRenderers.BID_PUSH).withId("Z");  // $NON-NLS-0$
        columns[i++] = new TableColumn(I18n.I.bidVolume(), -1f, TableCellRenderers.BID_VOLUME_PUSH).withId("AA");  // $NON-NLS-0$
        columns[i++] = new TableColumn(I18n.I.issuePrice(), -1f, TableCellRenderers.PRICE).withId("AB");  // $NON-NLS-0$
        columns[i++] = new TableColumn(I18n.I.redemptionPrice(), -1f, TableCellRenderers.PRICE).withId("AC");  // $NON-NLS-0$
        columns[i++] = new TableColumn(I18n.I.currency(), -1f, TableCellRenderers.STRING, "currencyIso").withId("AH");  // $NON-NLS-0$ $NON-NLS-1$
        columns[i++] = new TableColumn(I18n.I.high(), -1f, TableCellRenderers.HIGH_PUSH).withId("AI");  // $NON-NLS-0$
        columns[i++] = new TableColumn(I18n.I.low(), -1f, TableCellRenderers.LOW_PUSH).withId("AJ");  // $NON-NLS-0$
        columns[i++] = new TableColumn(I18n.I.high52WeeksAbbr(), -1f, TableCellRenderers.PRICE).withId("AK");  // $NON-NLS-0$
        columns[i++] = new TableColumn(I18n.I.low52WeeksAbbr(), -1f, TableCellRenderers.PRICE).withId("AL");  // $NON-NLS-0$
        columns[i++] = new TableColumn(I18n.I.yieldPerYearAbbr(), -1f, TableCellRenderers.PERCENT).withId("AM");  // $NON-NLS$
        columns[i++] = new TableColumn(I18n.I.accruedInterest(), -1f, TableCellRenderers.PERCENT).withId("ACIN")  // $NON-NLS$
                .withVisibilityCheck(vwdRelease2014OrDZRelease2016);
        columns[i++] = new TableColumn(I18n.I.previousClose() + " (" + I18n.I.date() + ")", -1f, TableCellRenderers.PREVIOUS_CLOSE_DATE_PUSH).withId("PCD")  // $NON-NLS$
                .withVisibilityCheck(vwdRelease2014OrDZRelease2016);

        PositionNoteController.PositionNoteRenderer noteRenderer = null;
        String currentGrouping = getCurrentGroupBy();
        String currentPortfolioId = (getCurrentPortfolio() == null ? "" : getCurrentPortfolio().getPortfolioid());

        if (!currentPortfolioId.isEmpty()) {
            if (currentGrouping.equals("MARKET")) {  // $NON-NLS$
                noteRenderer = new PositionNoteController.QuotePositionNoteRenderer(this, currentPortfolioId, commentViewMode);
            }
            else if (currentGrouping.equals("INSTRUMENT")) {  // $NON-NLS$
                noteRenderer = new PositionNoteController.InstrumentPositionNoteRenderer(this, currentPortfolioId, commentViewMode);
            }
        }

        columns[i++] = new TableColumn(I18n.I.comment(), 300, noteRenderer).withId("US")  // $NON-NLS$
                .withVisibilityCheck(vwdRelease2014OrDZRelease2016);

        columns[i++] = new TableColumn(I18n.I.bid() + I18n.I.officialSuffix(), -1f, TableCellRenderers.STRING).withId("BD") // $NON-NLS$
                        .withVisibilityCheck(lmeChanges2014);
        columns[i++] = new TableColumn(I18n.I.ask() + I18n.I.officialSuffix(), -1f, TableCellRenderers.STRING).withId("BC") // $NON-NLS$
                .withVisibilityCheck(lmeChanges2014);
        columns[i++] = new TableColumn(I18n.I.bid() + I18n.I.unOfficialSuffix(), -1f, TableCellRenderers.STRING).withId("BF") // $NON-NLS$
                        .withVisibilityCheck(lmeChanges2014);
        columns[i++] = new TableColumn(I18n.I.ask() + I18n.I.unOfficialSuffix(), -1f, TableCellRenderers.STRING).withId("BE") // $NON-NLS$
                .withVisibilityCheck(lmeChanges2014);
        columns[i++] = new TableColumn(I18n.I.interpoClosing(), -1f, TableCellRenderers.STRING).withId("BA") // $NON-NLS$
                        .withVisibilityCheck(lmeChanges2014);
        columns[i++] = new TableColumn(I18n.I.provEvaluation(), -1f, TableCellRenderers.STRING).withId("BB") // $NON-NLS$
                .withVisibilityCheck(lmeChanges2014);

        final String[] order = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "AH", "J", "K", "L", "US"}; // $NON-NLS$
        this.tableColumnModel = new DefaultTableColumnModel(AppConfig.getPropKeyPfColumns(portfolioId), columns).withColumnOrder(order);
    }

    public void onPlaceChange(PlaceChangeEvent event) {
        final HistoryToken historyToken = event.getHistoryToken();
        this.controllerToken = historyToken.getControllerId();
        final String portfolioid = historyToken.get(1, SessionData.isAsDesign() ? null : this.currentPortfolioId);
        if (portfolioid == null || !portfolioid.equals(this.currentPortfolioId) || this.pfBlock.isToBeRequested()) {
            selectPortfolio(portfolioid);
        }
        getContentContainer().setContent(this.view);
    }

    /**
     * When the user clicks "refresh", the portfolio menu will be rebuilt; useful for Profi-Depot // $NON-NLS-0$
     */
    @Override
    public void onResult() {
        onPortfolioSelected();
    }

    public void selectPortfolio(final String portfolioId) {
        createTableColumnModel(portfolioId, "--");
        preparePfBlock(portfolioId);
        this.pfOrdersBlock.setParameter("portfolioid", portfolioId); // $NON-NLS-0$
        this.pfOrdersBlock.setToBeRequested();
        if (this.tableColumnModel.configContainsColumn("AD")) { // $NON-NLS-0$
            this.latesNewsBlock.setEnabled(true);
        }
        else {
            this.latesNewsBlock.setEnabled(false);
        }
        this.pipe.issueRequest(this.selectCallback);
    }

    private void preparePfBlock(String portfolioId) {
        this.pfBlock.setParameter("portfolioid", portfolioId); // $NON-NLS-0$
        this.pfBlock.setParameter("sortBy", "name"); // $NON-NLS-0$ $NON-NLS-1$
        this.pfBlock.setToBeRequested();
    }

    public void groupBy(String field) {
        this.pfBlock.setParameter("groupBy", field);    // $NON-NLS-0$
        selectPortfolio(currentPortfolioId);
    }

    public String getCurrentGroupBy() {
        if (this.pfBlock.isResponseOk()) {
            return this.pfBlock.getResult().getGroup().getGroupedBy();
        }

        return "";
    }


    private boolean isInvalidID() {
        return this.pfBlock.getError() != null
                && WatchlistController.INVALID_ID_ERROR_CODE.equals(this.pfBlock.getError().getCode());
    }

    private void onPortfolioSelected() {
        if (this.pfBlock.isResponseOk()) {
            onSelectPortfolio(this.pfBlock.getResult());
        }
        else {
            if (isInvalidID()) {
                Dialog.error(I18n.I.hint(), I18n.I.cannotFindPortfolio());
            }
            else {
                AbstractMainController.INSTANCE.showError(I18n.I.internalError());
            }
        }
    }

    private void onSelectPortfolio(PFEvaluation pfEvaluation) {
        if (pfEvaluation.getPortfolio() == null) {
            return;
        }
        this.currentPortfolioId = pfEvaluation.getPortfolio().getPortfolioid();
        if (this == PortfolioController.INSTANCE) {
            setCurrentPortfolioCount(pfEvaluation.getPortfolios().getElement().size());
            SessionData.INSTANCE.setPortfolios(pfEvaluation.getPortfolios().getElement());
        }

        this.view.buildPortfolioMenu(pfEvaluation.getPortfolios().getElement());
        this.view.setSelectedPortfolio(pfEvaluation.getPortfolio().getPortfolioid());
        this.view.buildGroupingMenu(pfEvaluation.getGroup().getType());
        this.view.setBtnGrouping(pfEvaluation.getGroup().getGroupedBy(), false);

        updateView();
    }

    void updateView() {
        this.priceSupport.invalidateRenderItems();

        if (this.viewMode == PortfolioView.VIEW_ORDERS) {
            updateOrdersViewTable();
        }
        else if (this.viewMode == PortfolioView.VIEW_TABLE) {
            updatePortfolioViewTable();
            this.priceSupport.activate();
        }
        else if (this.viewMode == PortfolioView.VIEW_CHARTS) {
            updatePortfolioViewGallery();
        }
        else {
            updateVisualizationView();
        }
    }

    private void setCurrentPortfolioCount(int currentPortfolioCount) {
        this.currentPortfolioCount = currentPortfolioCount;
        this.view.enableDelBtn(currentPortfolioCount > 1);
        this.view.enableAddBtn(currentPortfolioCount < SessionData.INSTANCE.getMaxNumPortfolios());
    }

    public void createPortfolio(String name, String liquidity, String currency) {
        PortfolioActions.INSTANCE.createPortfolio(name, liquidity, currency);
    }

    public void deletePortfolio() {
        if (this.currentPortfolioCount > 1) {
            final String idToDelete = this.currentPortfolioId;
            this.currentPortfolioId = getNextPortfolioId();
            this.pfBlock.setToBeRequested();
            PortfolioActions.INSTANCE.deletePortfolio(idToDelete);
            SessionData.INSTANCE.getUser().getAppConfig().addProperty(AppConfig.getPropKeyPfColumns(idToDelete), null);
        }
        else {
            Dialog.error(I18n.I.cannotRemovePortfolio(),
                    I18n.I.atLeastOnePortfolioMustExist());
        }
    }

    private String getNextPortfolioId() {
        final List<PortfolioElement> pfs = SessionData.INSTANCE.getPortfolios();
        for (PortfolioElement pf : pfs) {
            if (!pf.getPortfolioid().equals(this.currentPortfolioId)) {
                return pf.getPortfolioid();
            }
        }
        return null;
    }

    private PortfolioElement getElement(String pfid) {
        if (!this.pfBlock.isResponseOk()) {
            return null;
        }
        for (PortfolioElement e : this.pfBlock.getResult().getPortfolios().getElement()) {
            if (e.getPortfolioid().equals(pfid)) {
                return e;
            }
        }
        return null;
    }

    private PFOrder getOrder(final String orderId) {
        for (PFOrder order : this.pfOrdersBlock.getResult().getElement()) {
            if (order.getOrderid().equals(orderId)) {
                return order;
            }
        }
        return null;
    }

    public void updatePortfolio(String name, String liquidity) {
        PortfolioActions.INSTANCE.updatePortfolio(this.currentPortfolioId, name, liquidity);
    }

    public void createBuyOrder(final String portfolioId, final String symbol) {
        preparePfBlock(portfolioId);
        this.pfBlock.issueRequest(new ResponseTypeCallback() {
            protected void onResult() {
                final PortfolioElement element = getElement(portfolioId);
                if (element == null) {
                    AbstractMainController.INSTANCE.showError(I18n.I.internalError());
                    return;
                }
                EditOrderController.buy(symbol, element);
            }
        });
    }

    public void createSellOrder(final QwiPosition position) {
        EditOrderController.sell(position, getElement(this.currentPortfolioId), sellOtherMarkets());
    }

    private boolean sellOtherMarkets() {
        String groupBy = getCurrentGroupBy();
        return !(groupBy.isEmpty() || groupBy.equals("MARKET"));  // $NON-NLS-0$
    }

    public void updateOrder(final String symbol, final String orderId) {
        final PFOrder order = getOrder(orderId);
        if (order == null) {
            AbstractMainController.INSTANCE.showError(I18n.I.internalError());
            return;
        }

        EditOrderController.edit(symbol, order, getElement(this.currentPortfolioId));
    }

    public void deleteOrder(final String orderId) {
        Dialog.confirm(I18n.I.shouldDeleteOrder(), () -> {
            doDeleteOrder(orderId);
        });
    }

    private void doDeleteOrder(String orderId) {
        PortfolioActions.INSTANCE.deleteOrder(this.currentPortfolioId, orderId);
    }

    public TableColumnModel getPortfoliosTableColumnModel() {
        return this.tableColumnModel;
    }

    public TableColumnModel createOrdersTableColumnModel() {
        return new DefaultTableColumnModel(new TableColumn[]{
                new TableColumn("", 1.1f, this.editPositionRenderer), // $NON-NLS-0$
                new TableColumn("", 1.1f, this.delPositionRenderer), // $NON-NLS-0$
                new TableColumn(I18n.I.name(), 0.3f, TableCellRenderers.QUOTELINK_42),
                new TableColumn(I18n.I.orderType(), 0.1f, TableCellRenderers.STRING),
                new TableColumn(I18n.I.date(), 0.1f, TableCellRenderers.DATE),
                new TableColumn(I18n.I.marketName(), 0.2f, TableCellRenderers.STRING_CENTER),
                new TableColumn(I18n.I.priceValue(), 0.2f, TableCellRenderers.STRING_RIGHT),
                new TableColumn(I18n.I.perShareOrPerValue(), 0.1f, TableCellRenderers.STRING_RIGHT)
        });
    }


    public void setChartPeriod(String chartPeriod) {
        this.chartPeriod = chartPeriod;
        refresh();
    }

    @Override
    public void refresh() {
        pfBlock.setToBeRequested();
        super.refresh();
    }

    private void updateVisualizationView() {
        if (!isOk()) {
            return;
        }
        this.pv.update(getCurrentPortfolioId());
        this.view.setMode(this.viewMode);
        this.view.showVisualizationView();
    }

    private void updatePortfolioViewTable() {
        if (!isOk()) {
            return;
        }
        PFEvaluation.Elements pos = getElements();
        final int rows = pos.getPosition().size();
        this.portfolioTdm = new DefaultTableDataModel(rows + 1, TAB_COLS_PORTFOLIOS);
        PortfolioPositionElement ppe;
        int count;
        final TrendBarData tbdGuV = TrendBarData.createGuV(this.pfBlock.getResult());
        final TrendBarData tbdPrice = TrendBarData.createPrice(this.pfBlock.getResult());
        for (count = 0; count < rows; count++) {
            ppe = pos.getPosition().get(count);
            if (ppe != null) {
                addRow(this.portfolioTdm, count, ppe, this.readOnly ? null : QwiPosition.Type.PORTFOLIO,
                        tbdGuV, tbdPrice, pos.getPosition());
            }
        }
        final PortfolioWerte pf = getCurrentPortfolio();
        addFooterRow(this.portfolioTdm, count, pf, tbdGuV);
        this.view.setMode(this.viewMode);
        this.view.updatePortfolio(this.portfolioTdm, pf.getInitialInvestment(), pf.getCash(), pf.getRealizedGainNet(),
                pf.getPortfolioCurrency());
    }

    private PFEvaluation.Elements getElements() {
        return pfBlock.getResult().getElements();
    }

    private PortfolioPositionElement getPositionElementFor(PFOrder order) {
        for (PortfolioPositionElement element : getElements().getPosition()) {
            if (element.getPositionid().equals(order.getPositionid())) {
                return element;
            }
        }
        return null;
    }


    private void updateOrdersViewTable() {
        if (!isOk()) {
            return;
        }

        if (this.ordersTableModel != null && !this.pfOrdersBlock.blockChanged()) {
            doUpdateOrdersViewTable();
            return;
        }

        // orders do not contain instrument/quotedata elements; since we may have orders for positions
        // that are not any more present in the pfBlock result, we have to create an extra list
        // that is able to load the missing instrument/quotedata elements for each order
        final OrderProxyList orderProxyList = new OrderProxyList();

        for (PFOrder order : this.pfOrdersBlock.getResult().getElement()) {
            orderProxyList.add(getPositionElementFor(order), order);
        }
        orderProxyList.checkData(this);
    }

    void doUpdateOrdersViewTable(List<OrderProxyList.Element> elements) {
        this.ordersTableModel = getOrdersTableModel(elements);
        doUpdateOrdersViewTable();
    }

    private void doUpdateOrdersViewTable() {
        final PortfolioWerte pf = getCurrentPortfolio();
        this.view.updateOrders(this.ordersTableModel, pf.getInitialInvestment(),
                pf.getCash(), pf.getRealizedGainNet(),
                pf.getPortfolioCurrency());
        this.view.setMode(PortfolioView.VIEW_ORDERS);
    }

    private DefaultTableDataModel getOrdersTableModel(List<OrderProxyList.Element> elements) {
        if (elements == null) {
            return null;
        }

        final DefaultTableDataModel result = new DefaultTableDataModel(elements.size(), TAB_COLS_ORDERS);
        for (int j = 0; j < elements.size(); j++) {
            final OrderProxyList.Element element = elements.get(j);
            addOrderRow(result, j, element.getOrder(), element.getInstrumentdata(), element.getQuotedata(),
                    this.readOnly ? null : QwiPosition.Type.ORDER);
        }
        return result;
    }

    private boolean isOk() {
        return this.pfOrdersBlock.isResponseOk() && this.pfBlock.isResponseOk();
    }

    private void addOrderRow(DefaultTableDataModel tableDataModel, int row, PFOrder order,
            InstrumentData instrumentData, QuoteData quoteData, QwiPosition.Type positionType) {

        final QwiPosition qwiPosition = createQwiPosition(order, instrumentData, quoteData, positionType);
        final String marketName =
                qwiPosition.getQuoteData() != null ? qwiPosition.getQuoteData().getMarketName() : "???"; // $NON-NLS-0$
        final String currency =
                qwiPosition.getQuoteData() != null ? qwiPosition.getQuoteData().getCurrencyIso() : "???"; // $NON-NLS-0$

        tableDataModel.setValuesAt(row, new Object[]{
                qwiPosition,
                qwiPosition,
                qwiPosition,
                getOrderType(order),
                order.getDate(),
                marketName,
                Renderer.LARGE_PRICE_MAX2.render(order.getPrice()) + " " + currency, // $NON-NLS-0$
                Renderer.LARGE_PRICE_0MAX5.render(order.getAmount())
        });

    }

    private QwiPosition createQwiPosition(PFOrder order, InstrumentData instrumentData,
            QuoteData quoteData, QwiPosition.Type positionType) {
        if (instrumentData != null && quoteData != null) {
            return new QwiPosition(instrumentData, quoteData, order.getOrderid(), positionType);
        }
        return new QwiPosition(order.getOrderid(), positionType);
    }

    private String getOrderType(PFOrder order) {
        if (order.getOrdertype().equals("BUY")) { // $NON-NLS-0$
            return I18n.I.purchase();
        }
        if (order.getOrdertype().equals("SELL")) { // $NON-NLS-0$
            return I18n.I.sale();
        }
        return I18n.I.unknown();
    }


    private void addFooterRow(DefaultTableDataModel tableDataModel, int row, PortfolioWerte pf,
            TrendBarData tbd) {
        final CurrentTrendBar currentTrendBar = new CurrentTrendBar(pf.getChangePercent(), tbd);
        tableDataModel.setValuesAt(row, new Object[]{
                null,
                StringUtil.htmlBold(I18n.I.sum()),
                null,
                null,
                null,
                null,
                pf.getPurchaseValue(),
                null,
                null,
                pf.getChangePercent(),
                pf.getChangeNet(),
                currentTrendBar,
                null,
                null,
                null,
                pf.getCurrentValue(),
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
        });
    }

    private void addRow(DefaultTableDataModel tableDataModel, int row, PortfolioPositionElement ppe,
            QwiPosition.Type positionType, TrendBarData tbdGuV,
            TrendBarData tbdPrice, List<PortfolioPositionElement> elements) {
        final Price p = Price.create(ppe);
        final QuoteWithInstrument qwiPosition = new QwiPosition(ppe.getInstrumentdata(), ppe.getQuotedata(), ppe.getPositionid(), positionType)
                .withHistoryContext(ItemListContext.createForPortrait(ppe, elements, SessionData.isAsDesign() ? I18n.I.portfolioSample() : I18n.I.portfolio()));

        String issueprice = null;
        String repurchasingprice = null;
        String interpoClosing = null;
        String provEvaluation = null;
        String officialAsk = null;
        String officialBid = null;
        String unOfficialAsk = null;
        String unOfficialBid = null;
        switch (PriceDataType.fromDmxml(ppe.getPricedatatype())) {
            case FUND_OTC:
                final FundPriceData fp = ppe.getFundpricedata();
                issueprice = fp.getIssueprice();
                repurchasingprice = fp.getRepurchasingprice();
                break;
            case LME:
                final LMEPriceData lp = ppe.getLmepricedata();
                interpoClosing = lp.getInterpolatedClosing();
                provEvaluation = lp.getProvisionalEvaluation();
                officialAsk = lp.getOfficialAsk();
                officialBid = lp.getOfficialBid();
                unOfficialAsk = lp.getUnofficialAsk();
                unOfficialBid = lp.getUnofficialBid();
                break;
            default:
                // ignore
        }

        final CurrentTrendBar currentTrendBarGuV = new CurrentTrendBar(ppe.getPositionChangePercentInPortfolioCurrency(), tbdGuV);
        final CurrentTrendBar currentTrendBarPrice = new CurrentTrendBar(p.getChangePercent(), tbdPrice);
        tableDataModel.setValuesAt(row, new Object[]{
                qwiPosition,
                InstrumentTypeEnum.valueOf(ppe.getInstrumentdata().getType()).getDescription(),
                qwiPosition,
                ppe.getInstrumentdata().getIsin(),
                ppe.getQuotedata().getMarketName(),
                ppe.getPositionVolume(),
                ppe.getOrderValueInPortfolioCurrency(),
                ppe.getAverageOrderPriceInPortfolioCurrency(),
                p,
                ppe.getPositionChangePercentInPortfolioCurrency(),
                ppe.getPositionChangeNetInPortfolioCurrency(),
                currentTrendBarGuV,
                p,
                p,
                currentTrendBarPrice,
                ppe.getPositionValueInPortfolioCurrency(),
                ppe.getInstrumentdata().getWkn(),
                p.getOpen(),
                p,
                p,
                p,
                p.getLowYear(),
                p.getHighYear(),
                p,
                p,
                p,
                p,
                p,
                issueprice,
                repurchasingprice
//                ,news
                , ppe.getQuotedata().getCurrencyIso(),
                p,
                p,
                p.getHigh52W(),
                p.getLow52W(),
                ppe.getPricedataExtended() != null ? ppe.getPricedataExtended().getYield() : null,
                ppe.getPricedataExtended() != null ? ppe.getPricedataExtended().getAccruedInterest() : null,
                p,
                new PositionNoteController.PortfolioPositionWrapper(ppe),
                Renderer.LARGE_PRICE_MAX2.render(interpoClosing),
                Renderer.LARGE_PRICE_MAX2.render(provEvaluation),
                Renderer.LARGE_PRICE_MAX2.render(officialAsk),
                Renderer.LARGE_PRICE_MAX2.render(officialBid),
                Renderer.LARGE_PRICE_MAX2.render(unOfficialAsk),
                Renderer.LARGE_PRICE_MAX2.render(unOfficialBid),
        });
    }

    PortfolioWerte getCurrentPortfolio() {
        if (this.pfBlock.isResponseOk()) {
            return this.pfBlock.getResult().getPortfolio();
        }
        return null;
    }

    public void switchView(PortfolioView.ViewMode mode) {
        this.viewMode = mode;
        if (mode == PortfolioView.VIEW_TABLE && this.pfBlock.isToBeRequested()) {
            // contains live data, so we may want to get it fresh
            selectPortfolio(this.currentPortfolioId);
        }
        else {
            updateView();
        }
    }


    //TODO: can be out of sync with portfolio id from xml block
    public String getCurrentPortfolioId() {
        return currentPortfolioId;
    }

    String getControllerToken() {
        return this.controllerToken;
    }

    void setToBeRequested() {
        this.pfBlock.setToBeRequested();
        this.pfOrdersBlock.setToBeRequested();
    }

    private void updatePortfolioViewGallery() {
        final List<PortfolioPositionElement> listPositions = getElements().getPosition();
        final List<ChartIcon> listCharts = new ArrayList<>(listPositions.size());
        for (final PortfolioPositionElement element : listPositions) {
            listCharts.add(new ChartIcon(element.getInstrumentdata(), element.getQuotedata(), this.chartPeriod));
        }
        this.currentListCharts = listCharts;
        this.view.updateCharts(listCharts);
        this.view.setMode(this.viewMode);
    }


    @Override
    public String getPrintHtml() {
        final PortfolioWerte pf = getCurrentPortfolio();
        if (this.viewMode == PortfolioView.VIEW_CHARTS) {
            if (this.printView == null) {
                this.printView = new WatchlistChartPrintView();
            }
            printView.update(this.currentListCharts);
            return printView.getChartGalleryPrintHtml(pf.getName());
        }
        else if (this.viewMode == PortfolioView.VIEW_VISUAL) {
            return this.pv.getPrintHtml(pf.getName(), pf.getInitialInvestment(), pf.getCash(), pf.getRealizedGainNet(),
                    pf.getPortfolioCurrency());
        }
        return WatchlistPortfolioUtil.getPrintHeadWithDate(pf.getName()) + super.getPrintHtml();
    }


    String getCsvUrl() {
        return UrlBuilder.byName("portfolio.csv").addAll(createParameterMapTable()).toURL(); // $NON-NLS$
    }

    String getXlsUrl() {
        return UrlBuilder.byName("portfolio.xls").addAll(createParameterMapTable()).toURL(); // $NON-NLS$
    }

    @Override
    public PdfOptionSpec getPdfOptionSpec() {
        return new PdfOptionSpec(
                this.viewMode.getPdfLink(),
                this.viewMode == PortfolioView.VIEW_CHARTS ? createParameterMapChartlist() : createParameterMapTable(),
                "pdf_options_format" // $NON-NLS-0$
        );
    }

    private Map<String, String> createParameterMapTable() {
        final Map<String, String> map = new HashMap<>();
        final User user = SessionData.INSTANCE.getUser();
        map.put("userid", user.getVwdId()); // $NON-NLS-0$
        map.put("portfolioid", this.currentPortfolioId); // $NON-NLS-0$
        map.put("orderids", this.tableColumnModel.getOrderString()); // $NON-NLS-0$
        if (FeatureFlags.Feature.VWD_RELEASE_2014.isEnabled()) {
            final boolean showCash = user.getAppConfig().getBooleanProperty(AppConfig.SHOW_CASH_IN_PORTFOLIO, true);
            map.put("showCash", Boolean.toString(showCash)); // $NON-NLS-0$
        }
        return map;
    }

    private Map<String, String> createParameterMapChartlist() {
        final Map<String, String> map = new HashMap<>();
        map.put("userid", SessionData.INSTANCE.getUser().getVwdId()); // $NON-NLS-0$
        map.put("symbols", getSymbols()); // $NON-NLS-0$
        map.put("title", I18n.I.portfolio() + ": " + getCurrentPortfolio().getName());  // $NON-NLS-0$ $NON-NLS-1$
        map.put("period", this.chartPeriod); // $NON-NLS-0$
        return map;
    }

    private String getSymbols() {
        final ArrayList<String> qids = new ArrayList<>();
        for (PortfolioPositionElement e : this.pfBlock.getResult().getElements().getPosition()) {
            qids.add(e.getQuotedata().getQid());
        }
        return StringUtil.join('-', qids);
    }

    public void onPricesUpdated(PricesUpdatedEvent event) {
        if (this.pfBlock.isResponseOk() && !this.priceSupport.isLatestPriceGeneration() && !event.isPushedUpdate()) {
            updateView();
            this.priceSupport.updatePriceGeneration();
        }
    }

    public ArrayList<PushRenderItem> onPushRegister(PushRegisterEvent event) {
        if (this.pfBlock.isResponseOk()) {
            final int numAdded = event.addVwdcodes(this.pfBlock.getResult());
            if (numAdded == this.pfBlock.getResult().getElements().getPosition().size()) {
                event.addComponentToReload(this.pfBlock, this);
            }
            if (numAdded > 0) {
                if (this.viewMode == PortfolioView.VIEW_TABLE) {
                    return this.view.getRenderItems(this.portfolioTdm);
                }
            }
        }
        return null;
    }

    @Override
    public void deactivate() {
        super.deactivate();
        this.priceSupport.deactivate();
    }

    public CommentViewMode getCommentViewMode() {
        return commentViewMode;
    }

    public void toggleCommentViewMode() {
        this.commentViewMode = commentViewMode.toggle();
        createTableColumnModel(currentPortfolioId, "--");
        updateView();
    }

    public PortfolioView.ViewMode getViewMode() {
        return viewMode;
    }
}
