/*
 * WatchlistController.java
 *
 * Created on 26.05.2008 10:38:01
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */

package de.marketmaker.iview.mmgwt.mmweb.client.watchlist;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.extjs.gxt.ui.client.util.Util;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.rpc.AsyncCallback;

import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.itools.gwtutil.client.util.date.MmJsDate;
import de.marketmaker.itools.gwtutil.client.widgets.IconImage;
import de.marketmaker.iview.dmxml.FundPriceData;
import de.marketmaker.iview.dmxml.InstrumentData;
import de.marketmaker.iview.dmxml.LMEPriceData;
import de.marketmaker.iview.dmxml.MSCBasicRatiosElement;
import de.marketmaker.iview.dmxml.QuoteData;
import de.marketmaker.iview.dmxml.ResponseType;
import de.marketmaker.iview.dmxml.WLWatchlist;
import de.marketmaker.iview.dmxml.WLWatchlistWebQueryUrl;
import de.marketmaker.iview.dmxml.WatchlistElement;
import de.marketmaker.iview.dmxml.WatchlistPositionElement;
import de.marketmaker.iview.dmxml.WatchlistWebQueryUrl;
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
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.ConfigurableSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.config.SnippetConfigurationView;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableColumnModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableDataModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.SimpleVisibilityCheck;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableCellRenderers;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableColumn;
import de.marketmaker.iview.mmgwt.mmweb.client.table.VisibilityCheck;
import de.marketmaker.iview.mmgwt.mmweb.client.util.Dialog;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;
import de.marketmaker.iview.mmgwt.mmweb.client.util.LinkListener;
import de.marketmaker.iview.mmgwt.mmweb.client.util.PdfOptionSpec;
import de.marketmaker.iview.mmgwt.mmweb.client.util.Renderer;
import de.marketmaker.iview.mmgwt.mmweb.client.util.SortLinkSupport;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.UrlBuilder;
import de.marketmaker.iview.mmgwt.mmweb.client.watchlist.PositionNoteController.PositionNoteRenderer.CommentViewMode;

import static de.marketmaker.iview.mmgwt.mmweb.client.QwiPosition.Type.WATCHLIST;

/**
 * @author Michael Lösch
 */
public class WatchlistController extends AbstractPageController implements PageController,
        ConfigurableSnippet, PushRegisterHandler {

    static class CurrentWatchlistData {
        private final String watchlistId;

        private final String watchlistName;

        private final int count;

        CurrentWatchlistData(String watchlistId, String watchlistName, int count) {
            this.watchlistId = watchlistId;
            this.watchlistName = watchlistName;
            this.count = count;
        }

        String getWatchlistId() {
            return watchlistId;
        }

        String getWatchlistName() {
            return watchlistName;
        }

        int getCount() {
            return count;
        }
    }

    public static final WatchlistController INSTANCE = new WatchlistController();

    public static final String INVALID_ID_ERROR_CODE = "user.portfolioid.invalid"; // $NON-NLS-0$

    public static final int TAB_COLS = 50;

    private final WatchlistView view;

    private final DmxmlContext.Block<WLWatchlist> block;

    private final DmxmlContext.Block<WLWatchlistWebQueryUrl> webQueryUrlBlock;

    private CurrentWatchlistData currentWatchlistData = null;

    private String currentWebQueryUrl = null;

    private String chartPeriod = AbstractWatchlistView.CHART_PERIOD_DEFAULT;

    private final HashMap<String, String> positionSearchDialogParams = new HashMap<>();

    private List<ChartIcon> currentListCharts;

    private DefaultTableDataModel currentDtm;

    private final TableCellRenderers.IconLinkRenderer<QuoteWithInstrument> delPositionRenderer;

    private final TableCellRenderers.IconLinkRenderer editPositionRenderer;

    private final PriceSupport priceSupport = new PriceSupport(this);

    private CommentViewMode commentViewMode = CommentViewMode.SHORT;

    private final AsyncCallback<ResponseType> selectCallback = new ResponseTypeCallback() {
        protected void onResult() {
            onWatchlistSelected();
        }
    };

    private WatchlistController() {
        this.context.setCancellable(false);
        this.block = this.context.addBlock("WL_Watchlist"); // $NON-NLS-0$
        this.block.setParameter("userid", SessionData.INSTANCE.getUser().getVwdId()); // $NON-NLS-0$
        this.block.setParameter("sortBy", "name"); // $NON-NLS$
        this.block.setParameter("extendedPriceData", "true"); // $NON-NLS$

        this.webQueryUrlBlock = this.context.addBlock("WL_WatchlistWebQueryUrl"); // $NON-NLS$
        this.webQueryUrlBlock.setParameter("userid", SessionData.INSTANCE.getUser().getVwdId()); // $NON-NLS$

        final MmJsDate today = new MmJsDate().getMidnight();
        final MmJsDate januaryFirst = new MmJsDate(today.getFullYear(), MmJsDate.MONTH_JANUARY, 1, 0, 0, 0, 0);
        final long days = today.getDiffDays(januaryFirst);

        this.block.setParameters("period", new String[]{"P" + days + "D", "P1M", "P6M", "P1Y", "P3Y", "P5Y"}); // $NON-NLS$

        this.delPositionRenderer = new TableCellRenderers.IconLinkRenderer<>((LinkListener<QuoteWithInstrument>) (quoteWithInstrumentLinkContext, e) -> {
            assert (quoteWithInstrumentLinkContext.getData() instanceof QwiPosition);
            final QwiPosition qwi = (QwiPosition) quoteWithInstrumentLinkContext.getData();
            deletePosition(qwi.getPositionId());
        }, IconImage.get("mm-small-remove"), I18n.I.removePositionFromWatchlist());  // $NON-NLS-0$

        this.editPositionRenderer = new TableCellRenderers.IconLinkRenderer<>((LinkListener<QuoteWithInstrument>) (quoteWithInstrumentLinkContext, e) -> {
            assert (quoteWithInstrumentLinkContext.getData() instanceof QwiPosition);
            final QwiPosition qwi = (QwiPosition) quoteWithInstrumentLinkContext.getData();
            showEditPositionDialog(qwi);
        }, IconImage.get("mm-small-edit"), I18n.I.changeExchange());  // $NON-NLS$

        this.view = new WatchlistView(this,
                "true".equals(SessionData.INSTANCE.getGuiDefValue("watchlist-without-toolbar"))); // $NON-NLS$

        final List<WatchlistElement> wls = SessionData.INSTANCE.getWatchlists();
        final int watchlistCount = (wls != null) ? wls.size() : 0;
        updateWatchlistCount(watchlistCount);
        if (watchlistCount > 0) {
            this.currentWatchlistData = new CurrentWatchlistData(wls.get(0).getWatchlistid(),
                    wls.get(0).getName(), watchlistCount);
            this.view.buildWatchlistMenu(this.currentWatchlistData.getWatchlistId());

            selectWatchlist(wls.get(0).getWatchlistid());
        }

    }

    @SuppressWarnings("UnusedAssignment")
    DefaultTableColumnModel getColumnModel() {
        final String separator = "/"; // $NON-NLS-0$
        final String id;
        if (getCurrentWatchlistData() != null) {
            id = getCurrentWatchlistData().getWatchlistId();
        }
        else {
            id = "NULL"; // $NON-NLS-0$
        }

        final VisibilityCheck vwdRelease2014OrDZRelease2016 = SimpleVisibilityCheck.valueOf(FeatureFlags.Feature.VWD_RELEASE_2014.isEnabled() || FeatureFlags.Feature.DZ_RELEASE_2016.isEnabled());
        final VisibilityCheck lmeChanges2014 = SimpleVisibilityCheck.valueOf(FeatureFlags.Feature.LME_CHANGES_2014.isEnabled());

        final String wknOrIsinColumn = SessionData.INSTANCE.isShowIsin() ? "Y" : "I"; // $NON-NLS$
        TableColumn[] columns = new TableColumn[TAB_COLS];
        int i = 0;
        columns[i++] = new TableColumn("", 14f, this.editPositionRenderer).setFixed().withId("A"); // $NON-NLS$
        columns[i++] = new TableColumn("", 11f, this.delPositionRenderer).setFixed().withId("B"); // $NON-NLS$
        columns[i++] = new TableColumn(I18n.I.type(), -1f, TableCellRenderers.DEFAULT, "instrumentTypeDescription").withId("C");  // $NON-NLS$
        columns[i++] = new TableColumn(I18n.I.name(), -1f, TableCellRenderers.QUOTELINK_32, "name").withId("D");  // $NON-NLS$
        columns[i++] = new TableColumn(I18n.I.price(), -1f, TableCellRenderers.LAST_PRICE_WITH_SUPPLEMENT_PUSH, "priceValue").withId("E");  // $NON-NLS$
        columns[i++] = new TableColumn("+/- %", -1f, TableCellRenderers.CHANGE_PERCENT_PUSH, "changePercentPeriod").withId("F"); // $NON-NLS$
        columns[i++] = new TableColumn("+/-", -1f, TableCellRenderers.CHANGE_NET_PUSH).withId("G"); // $NON-NLS$
        columns[i++] = new TableColumn(I18n.I.trendWithSign(), -1f, TableCellRenderers.TRENDBAR).withId("AA");  // $NON-NLS$
        columns[i++] = new TableColumn(I18n.I.currency(), -1f, TableCellRenderers.STRING, "currencyIso").withId("H");  // $NON-NLS$
        columns[i++] = new TableColumn("WKN", -1f, TableCellRenderers.STRING, "wkn").withId("I"); // $NON-NLS$

        columns[i++] = new TableColumn("ISIN", -1f, TableCellRenderers.STRING, "isin").withId("Y"); // $NON-NLS$
        columns[i++] = new TableColumn(I18n.I.marketName(), -1f, TableCellRenderers.STRING_CENTER, "marketName").withId("J");  // $NON-NLS$
        columns[i++] = new TableColumn(I18n.I.date() + separator + I18n.I.time(), -1f, TableCellRenderers.COMPACT_DATETIME, "date")  // $NON-NLS$
                .withCellClass("mm-center").withId("K");  // $NON-NLS$
        columns[i++] = new TableColumn(I18n.I.volume(), -1f, TableCellRenderers.VOLUME_LONG_PUSH, "turnover").withId("N");  // $NON-NLS$
        columns[i++] = new TableColumn(I18n.I.previousClose(), -1f, TableCellRenderers.PREVIOUS_PRICE_PUSH).withId("O");  // $NON-NLS-0$
        columns[i++] = new TableColumn(I18n.I.openPrice(), -1f, TableCellRenderers.STRING_RIGHT).withId("Q");  // $NON-NLS-0$
        columns[i++] = new TableColumn(I18n.I.volumeByLastPrice(), -1f, TableCellRenderers.VOLUME_PRICE_PUSH).withId("R");  // $NON-NLS-0$
        columns[i++] = new TableColumn(I18n.I.askPrice(), -1f, TableCellRenderers.ASK_PUSH).withId("S");  // $NON-NLS-0$
        columns[i++] = new TableColumn(I18n.I.askVolume(), -1f, TableCellRenderers.ASK_VOLUME_PUSH).withId("T");  // $NON-NLS-0$
        columns[i++] = new TableColumn(I18n.I.bidPrice(), -1f, TableCellRenderers.BID_PUSH).withId("U");  // $NON-NLS-0$

        columns[i++] = new TableColumn(I18n.I.bidVolume(), -1f, TableCellRenderers.BID_VOLUME_PUSH).withId("V");  // $NON-NLS-0$
        columns[i++] = new TableColumn(I18n.I.issuePrice(), -1f, TableCellRenderers.STRING_RIGHT).withId("W");  // $NON-NLS-0$
        columns[i++] = new TableColumn(I18n.I.redemptionPrice(), -1f, TableCellRenderers.STRING_RIGHT).withId("X");  // $NON-NLS-0$
        columns[i++] = new TableColumn(I18n.I.high(), -1f, TableCellRenderers.HIGH_PUSH).withId("AG");  // $NON-NLS-0$
        columns[i++] = new TableColumn(I18n.I.low(), -1f, TableCellRenderers.LOW_PUSH).withId("AF");  // $NON-NLS-0$
        columns[i++] = new TableColumn(I18n.I.high52WeeksAbbr(), -1f, TableCellRenderers.PRICE).withId("AB");  // $NON-NLS-0$
        columns[i++] = new TableColumn(I18n.I.low52WeeksAbbr(), -1f, TableCellRenderers.PRICE).withId("AC");  // $NON-NLS-0$
        columns[i++] = new TableColumn(I18n.I.yearHigh(), -1f, TableCellRenderers.PRICE).withId("AD");  // $NON-NLS-0$
        columns[i++] = new TableColumn(I18n.I.yearLow(), -1f, TableCellRenderers.PRICE).withId("AE");  // $NON-NLS-0$
        columns[i++] = new TableColumn(I18n.I.maximumLossAbbr("6M"), -1f, TableCellRenderers.CHANGE_PERCENT).withId("AH");  // $NON-NLS$

        columns[i++] = new TableColumn(I18n.I.volatilityAbbr("1Y"), -1f, TableCellRenderers.CHANGE_PERCENT).withId("AI");  // $NON-NLS$
        columns[i++] = new TableColumn(I18n.I.volatilityAbbr("3Y"), -1f, TableCellRenderers.CHANGE_PERCENT).withId("AJ");  // $NON-NLS$
        columns[i++] = new TableColumn(I18n.I.performanceAbbr("YTD"), -1f, TableCellRenderers.CHANGE_PERCENT).withId("AK");  // $NON-NLS$
        columns[i++] = new TableColumn(I18n.I.performanceAbbr("1M"), -1f, TableCellRenderers.CHANGE_PERCENT).withId("AL");  // $NON-NLS$
        columns[i++] = new TableColumn(I18n.I.performanceAbbr("6M"), -1f, TableCellRenderers.CHANGE_PERCENT).withId("AM");  // $NON-NLS$
        columns[i++] = new TableColumn(I18n.I.performanceAbbr("1Y"), -1f, TableCellRenderers.CHANGE_PERCENT).withId("AN");  // $NON-NLS$
        columns[i++] = new TableColumn(I18n.I.performanceAbbr("3Y"), -1f, TableCellRenderers.CHANGE_PERCENT).withId("AO");  // $NON-NLS$
        columns[i++] = new TableColumn(I18n.I.performanceAbbr("5Y"), -1f, TableCellRenderers.CHANGE_PERCENT).withId("AP");  // $NON-NLS$
        columns[i++] = new TableColumn(I18n.I.yieldPerYearAbbr(), -1f, TableCellRenderers.PERCENT).withId("AQ");  // $NON-NLS$
        columns[i++] = new TableColumn(I18n.I.accruedInterest(), -1f, TableCellRenderers.PERCENT).withId("ACIN")  // $NON-NLS$
                .withVisibilityCheck(vwdRelease2014OrDZRelease2016);
        columns[i++] = new TableColumn(I18n.I.previousClose() + " (" + I18n.I.date() + ")", -1f, TableCellRenderers.PREVIOUS_CLOSE_DATE_PUSH).withId("PCD")  // $NON-NLS$
                .withVisibilityCheck(vwdRelease2014OrDZRelease2016);

        String watchlistId = (getCurrentWatchlistData() == null ? "" : getCurrentWatchlistData().getWatchlistId());
        columns[i++] = new TableColumn(I18n.I.comment(), 300, new PositionNoteController.QuotePositionNoteRenderer(this, watchlistId, commentViewMode)).withId("US") // $NON-NLS$
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

        columns[i++] = new TableColumn(I18n.I.dividend(), -1f, TableCellRenderers.STRING).withId("BG"); // $NON-NLS$
        columns[i++] = new TableColumn(I18n.I.dividendYield(), -1f, TableCellRenderers.STRING).withId("BH"); // $NON-NLS$

        DefaultTableColumnModel columnModel = new DefaultTableColumnModel(AppConfig.getPropKeyWlColumns(id), columns);
        columnModel.withColumnOrder(new String[]{"A", "B", "C", "D", "E", "F", "G", "H", wknOrIsinColumn, "J", "K", "U", "S", "AG", "AF", "AD", "AE", "N", "O", "AB", "AC", "US"}); // $NON-NLS$

        return columnModel;
    }

    LinkListener<String> getSortLinkListener() {
        return new SortLinkSupport(this.block, () -> selectWatchlist(currentWatchlistData.getWatchlistId()));
    }

    public void selectWatchlist(String id) {
        this.block.setParameter("watchlistid", id); // $NON-NLS-0$
        this.block.setParameter("disablePaging", "true"); // $NON-NLS-0$ $NON-NLS-1$
        this.block.setToBeRequested();

        this.webQueryUrlBlock.setParameter("watchlistid", id); //$NON-NLS$
        this.webQueryUrlBlock.setToBeRequested();

        this.context.issueRequest(this.selectCallback);
    }

    private void onWatchlistSelected() {
        if (this.webQueryUrlBlock.isResponseOk()) {
            List<WatchlistWebQueryUrl> urls = this.webQueryUrlBlock.getResult().getWatchlistWebQuery();
            if (urls != null && urls.size() > 0 && urls.get(0) != null) {
                this.currentWebQueryUrl = urls.get(0).getUrl();
            }
        }

        if (this.block.isResponseOk()) {
            final WLWatchlist watchlist = this.block.getResult();
            if (watchlist.getWatchlist() == null) {
                return;
            }
            onWatchlistSelected(watchlist);
        }
        else {
            if (isInvalidID()) {
                Dialog.error(I18n.I.hint(), I18n.I.cannotFindWatchlist());
            }
            else {
                AbstractMainController.INSTANCE.showError(I18n.I.internalError());
            }
        }
    }

    private boolean isInvalidID() {
        return this.block.getError() != null
                && INVALID_ID_ERROR_CODE.equals(this.block.getError().getCode());
    }

    private void onWatchlistSelected(WLWatchlist watchlist) {
        this.currentWatchlistData = new CurrentWatchlistData(
                watchlist.getWatchlist().getWatchlistid(),
                watchlist.getWatchlist().getName(),
                watchlist.getWatchlists().getElement().size());
        updateWatchlistCount(this.currentWatchlistData.getCount());

        final boolean changed = SessionData.INSTANCE.setWatchlists(watchlist.getWatchlists().getElement());
        if(!SessionData.isAsDesign()) {
            if (changed) {
                this.view.buildWatchlistMenu(this.currentWatchlistData.getWatchlistId());
            }
            else {
                this.view.setSelectedWatchlist(watchlist.getWatchlist().getWatchlistid());
            }
        }
        updateView();
    }

    public void createWatchlist(String name) {
        if (StringUtil.hasText(name)) {
            WatchlistActions.INSTANCE.createWatchlist(name.trim());
        }
    }

    public void deleteWatchlist() {
        if (currentWatchlistData.getCount() > 1) {
            final String idToDelete = currentWatchlistData.getWatchlistId();
            WatchlistActions.INSTANCE.deleteWatchlist(idToDelete);
            deselectWatchlist(idToDelete);
            SessionData.INSTANCE.getUser().getAppConfig().addProperty(AppConfig.getPropKeyWlColumns(idToDelete), null);
        }
        else {
            Dialog.error(I18n.I.cannotRemoveWatchlist(),
                    I18n.I.atLeastOneWatchMustExist());
        }
    }

    private void deselectWatchlist(String deletedId) {
        final List<WatchlistElement> WatchlistElements = SessionData.INSTANCE.getWatchlists();
        for (WatchlistElement watchlist : WatchlistElements) {
            if (!watchlist.getWatchlistid().equals(deletedId)) {
                currentWatchlistData = new CurrentWatchlistData(
                        watchlist.getWatchlistid(),
                        watchlist.getName(),
                        WatchlistElements.size()
                );
            }
        }
    }

    public void updateWatchlistName(String newName) {
        if (StringUtil.hasText(newName)) {
            WatchlistActions.INSTANCE.updateWatchlist(this.currentWatchlistData.getWatchlistId(), newName.trim());
        }
    }

    public void createPosition(QuoteWithInstrument qwi, String watchlistId) {
        WatchlistActions.INSTANCE.createPosition(watchlistId != null
                ? watchlistId : this.currentWatchlistData.getWatchlistId(), qwi);
    }

    @SuppressWarnings("unused")
    public void createPosition(QuoteWithInstrument qwi) {
        if (this.currentWatchlistData.getWatchlistId() != null) {
            createPosition(qwi, this.currentWatchlistData.getWatchlistId());
        }
    }

    @SuppressWarnings("unused")
    public void deletePosition(final String positionId, AsyncCallback<ResponseType> delegate) {
        doDeletePosition(positionId, delegate);
    }

    public void deletePosition(final String positionId) {
        Dialog.confirm(I18n.I.shouldDeletePosition(), () -> {
            doDeletePosition(positionId);
        });
    }

    private void doDeletePosition(String positionId) {
        WatchlistActions.INSTANCE.deletePosition(this.currentWatchlistData.getWatchlistId(), positionId);
    }

    private void doDeletePosition(String positionId, AsyncCallback<ResponseType> delegate) {
        WatchlistActions.INSTANCE.deletePosition(this.currentWatchlistData.getWatchlistId(), positionId, delegate);
    }

    public void showEditPositionDialog(QwiPosition qwi) {
        new EditWatchlistPosition(this).show(qwi);
    }

    public void updatePosition(String positionId, QuoteWithInstrument qwi) {
        WatchlistActions.INSTANCE.updatePosition(this.currentWatchlistData.getWatchlistId(), positionId, qwi);
    }

    protected void onResult() {
        updateView();
    }

    void setToBeRequested() {
        this.block.setToBeRequested();
    }

    public void updateView() {
        this.priceSupport.invalidateRenderItems();

        if (this.block.isResponseOk()) {
            if (this.view.getViewMode() == WatchlistView.VIEW_CHARTS) {
                this.currentListCharts = createChartList();
                this.view.update(this.currentListCharts);
            }
            else {
                this.currentDtm = createDataModel();
                this.view.update(this.currentDtm);
            }
        }
        this.priceSupport.activate();
    }

    private DefaultTableDataModel createDataModel() {
        final TrendBarData tbd = TrendBarData.create(this.block.getResult());
        WLWatchlist.Positions p = this.block.getResult().getPositions();
        final int rows = p.getPosition().size();
        final DefaultTableDataModel dtm = new DefaultTableDataModel(rows, TAB_COLS);
        for (int count = 0; count < rows; count++) {
            WatchlistPositionElement wpe = p.getPosition().get(count);
            if (wpe != null) {
                addRow(dtm, count, wpe, tbd, p.getPosition());
            }
        }
        return dtm.withSort(this.block.getResult().getSort());
    }

    List<WatchlistPositionElement> getPositionElements() {
        return this.block.getResult().getPositions().getPosition();
    }

    public void setChartPeriod(String chartPeriod) {
        this.chartPeriod = chartPeriod;
        refresh();
    }

    private void updateWatchlistCount(int count) {
        this.view.enableDelBtn(count > 1);
        this.view.enableAddBtn(count < SessionData.INSTANCE.getMaxNumWatchlists());
    }

    private ArrayList<ChartIcon> createChartList() {
        final ArrayList<ChartIcon> result = new ArrayList<>();
        for (final WatchlistPositionElement e : getPositionElements()) {
            result.add(new ChartIcon(e.getInstrumentdata(), e.getQuotedata(), this.chartPeriod));
        }
        return result;
    }

    public void onPlaceChange(PlaceChangeEvent event) {
        final HistoryToken historyToken = event.getHistoryToken();
        final String currentWatchlistId = this.currentWatchlistData == null
                ? null
                : this.currentWatchlistData.getWatchlistId();
        final String watchlistid = historyToken.get(1, SessionData.isAsDesign() ? null : currentWatchlistId);
        if (watchlistid == null || !watchlistid.equals(currentWatchlistId) || this.block.isToBeRequested()) {
            selectWatchlist(watchlistid);
        }
        getContentContainer().setContent(this.view);
    }

    public HashMap<String, String> getCopyOfParameters() {
        return this.positionSearchDialogParams;
    }

    public void setParameters(HashMap<String, String> params) {
        QuoteWithInstrument qwi = QuoteWithInstrument.getLastSelected();
        if (qwi != null) {
            createPosition(qwi, this.currentWatchlistData.getWatchlistId());
        }
    }

    public void showConfigView() {
        final SnippetConfigurationView configView = new SnippetConfigurationView(this);
        configView.addSelectSymbol(null);
        configView.show();
    }

    private void addRow(DefaultTableDataModel tdm, int row, WatchlistPositionElement wpe,
                        TrendBarData tbd, List<WatchlistPositionElement> positions) {
        final Price p = Price.create(wpe);
        final InstrumentData instrumentData = wpe.getInstrumentdata();
        final QuoteData quoteData = wpe.getQuotedata();

        String issueprice = null;
        String repurchasingprice = null;
        String interpoClosing = null;
        String provEvaluation = null;
        String officialAsk = null;
        String officialBid = null;
        String unOfficialAsk = null;
        String unOfficialBid = null;
        switch (PriceDataType.fromDmxml(wpe.getPricedatatype())) {
            case FUND_OTC:
                final FundPriceData fp = wpe.getFundpricedata();
                issueprice = fp.getIssueprice();
                repurchasingprice = fp.getRepurchasingprice();
                break;
            case LME:
                final LMEPriceData lp = wpe.getLmepricedata();
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

        String dividendCash = wpe.getDivCurrentYear();
        String dividendYield;
        if (!StringUtil.hasText(dividendCash)) {
            dividendCash = "--";
            dividendYield = "--";
        } else {
            dividendYield = calculateYield(wpe);
        }

        final MSCBasicRatiosElement ratiosYtd = wpe.getRatiodata().get(0);
        final MSCBasicRatiosElement ratios1m = wpe.getRatiodata().get(1);
        final MSCBasicRatiosElement ratios6m = wpe.getRatiodata().get(2);
        final MSCBasicRatiosElement ratios1y = wpe.getRatiodata().get(3);
        final MSCBasicRatiosElement ratios3y = wpe.getRatiodata().get(4);
        final MSCBasicRatiosElement ratios5y = wpe.getRatiodata().get(5);

        final CurrentTrendBar currentTrendBar = new CurrentTrendBar(p.getChangePercent(), tbd);
        final QuoteWithInstrument qwiPosition = new QwiPosition(instrumentData, quoteData, wpe.getPositionid(), WATCHLIST)
                .withHistoryContext(ItemListContext.createForPortrait(wpe, positions, I18n.I.watchlist()));
        tdm.setValuesAt(row, new Object[]{
                qwiPosition,
                qwiPosition,
                InstrumentTypeEnum.valueOf(instrumentData.getType()).getDescription(),
                qwiPosition,
                p,
                p,
                p,
                currentTrendBar,
                quoteData.getCurrencyIso(),
                instrumentData.getWkn(),

                instrumentData.getIsin(),
                quoteData.getMarketName(),
                p.getDate(),
                p,
                p,
                p.getOpen(),
                p,
                p,
                p,
                p,

                p,
                Renderer.LARGE_PRICE_MAX2.render(issueprice),
                Renderer.LARGE_PRICE_MAX2.render(repurchasingprice),
                p,
                p,
                p.getHigh52W(),
                p.getLow52W(),
                p.getHighYear(),
                p.getLowYear(),
                ratios6m.getMaximumLossPercent(),

                ratios1y.getVolatility(),
                ratios3y.getVolatility(),
                ratiosYtd.getPerformance(),
                ratios1m.getPerformance(),
                ratios6m.getPerformance(),
                ratios1y.getPerformance(),
                ratios3y.getPerformance(),
                ratios5y.getPerformance(),
                wpe.getPricedataExtended() != null
                        ? wpe.getPricedataExtended().getYield()
                        : null,
                wpe.getPricedataExtended() != null ? wpe.getPricedataExtended().getAccruedInterest() : null,
                p,
                new PositionNoteController.WatchlistPositionWrapper(wpe),
                Renderer.LARGE_PRICE_MAX2.render(interpoClosing),
                Renderer.LARGE_PRICE_MAX2.render(provEvaluation),
                Renderer.LARGE_PRICE_MAX2.render(officialAsk),
                Renderer.LARGE_PRICE_MAX2.render(officialBid),
                Renderer.LARGE_PRICE_MAX2.render(unOfficialAsk),
                Renderer.LARGE_PRICE_MAX2.render(unOfficialBid),

                dividendCash,
                dividendYield,
        });
    }

    // should be the same semantics as in StaticDataSTKSnippetView
    private String calculateYield(WatchlistPositionElement wpe) {

        final String dividendStr = !Util.isEmptyString(wpe.getDivLastYear())
                ? wpe.getDivLastYear()
                : wpe.getDivCurrentYear();
        if (!StringUtil.hasText(dividendStr) || wpe.getPricedata() == null) {
            return "--"; // $NON-NLS$
        }

        String priceStr = wpe.getPricedata().getPrice();
        if (!StringUtil.hasText(priceStr)) {
            return "--"; // $NON-NLS$
        }

        String dividendYield;
        try {
            final double dividend = Double.parseDouble(dividendStr);
            final double price = Double.parseDouble(priceStr);
            dividendYield = Renderer.PERCENT.render(Double.toString(dividend / price));
        } catch (Exception e) {
            Firebug.log("<getDividendYield> failed w/ " + e.getMessage() // $NON-NLS$
                    + " for " + dividendStr + " and " + priceStr); // $NON-NLS$
            dividendYield =  "--";
        }
        return dividendYield;
    }

    @Override
    public String getPrintHtml() {
        final String wlName = this.currentWatchlistData.watchlistName;
        if (this.view.getViewMode() == WatchlistView.VIEW_CHARTS) {
            final WatchlistChartPrintView printView = new WatchlistChartPrintView();
            printView.update(this.currentListCharts);
            return printView.getChartGalleryPrintHtml(wlName);
        }
        else if (this.view.getViewMode() == WatchlistView.VIEW_TABLE) {
            final WatchlistTablePrintView printView = new WatchlistTablePrintView(this);
            printView.update(this.currentDtm);
            return printView.getPrintHtml(wlName);
        }
        return this.view.getPrintHtml();
    }

    public CurrentWatchlistData getCurrentWatchlistData() {
        return currentWatchlistData;
    }

    public String getChartPeriod() {
        return chartPeriod;
    }

    String getCsvUrl() {
        return UrlBuilder.byName("watchlist.csv").addAll(createParameterMapTable()).toURL(); // $NON-NLS$
    }

    String getXlsUrl() {
        return UrlBuilder.byName("watchlist.xls").addAll(createParameterMapTable()).toURL(); // $NON-NLS$
    }

    String getWebQueryUrl() {
        if (currentWebQueryUrl == null) return null;

        final String orderIdParam = "&orderids=" + URL.encodeQueryString(getColumnModel().getOrderString()); //$NON-NLS$
        return currentWebQueryUrl + orderIdParam;
    }

    @Override
    public boolean isPrintable() {
        return super.isPrintable();
    }

    @Override
    public PdfOptionSpec getPdfOptionSpec() {
        if (getCurrentWatchlistData() == null) {
            return null;
        }
        final AbstractWatchlistView.ViewMode viewMode = this.view.getViewMode();
        return new PdfOptionSpec(viewMode.getPdfLink(),
                viewMode == WatchlistView.VIEW_CHARTS ? createParameterMapChartlist() : createParameterMapTable(),
                "pdf_options_format"); // $NON-NLS$
    }

    private Map<String, String> createParameterMapTable() {
        final Map<String, String> map = new HashMap<>();
        map.put("userid", SessionData.INSTANCE.getUser().getVwdId()); // $NON-NLS$
        map.put("watchlistid", getCurrentWatchlistData().getWatchlistId()); // $NON-NLS$
        map.put("orderids", getColumnModel().getOrderString()); // $NON-NLS$
        map.put("sortBy", this.block.getParameter("sortBy")); // $NON-NLS$
        final String ascending = this.block.getParameter("ascending"); // $NON-NLS$
        if (ascending != null) {
            map.put("ascending", ascending); // $NON-NLS-0$
        }
        return map;
    }

    private Map<String, String> createParameterMapChartlist() {
        final Map<String, String> map = new HashMap<>();
        map.put("userid", SessionData.INSTANCE.getUser().getVwdId()); // $NON-NLS-0$
        map.put("symbols", getSymbols()); // $NON-NLS-0$
        map.put("title", I18n.I.watchlist() + ": " + getCurrentWatchlistData().getWatchlistName());  // $NON-NLS-0$ $NON-NLS-1$
        map.put("period", getChartPeriod()); // $NON-NLS-0$
        return map;
    }

    private String getSymbols() {
        final ArrayList<String> qids = new ArrayList<>();
        for (WatchlistPositionElement e : getPositionElements()) {
            qids.add(e.getQuotedata().getQid());
        }
        return StringUtil.join('-', qids);
    }

    public void onPricesUpdated(PricesUpdatedEvent event) {
        if (this.block.isResponseOk() && !this.priceSupport.isLatestPriceGeneration() && !event.isPushedUpdate()) {
            updateView();
        }
    }

    public ArrayList<PushRenderItem> onPushRegister(PushRegisterEvent event) {
        if (this.block.isResponseOk()) {
            final int numAdded = event.addVwdcodes(this.block.getResult());
            if (numAdded == this.block.getResult().getPositions().getPosition().size()) {
                event.addComponentToReload(this.block, this);
            }
            if (numAdded > 0) {
                return this.view.getRenderItems(this.currentDtm);
            }
        }
        return null;
    }

    @Override
    public void deactivate() {
        super.deactivate();
        this.priceSupport.deactivate();
    }

    @Override
    public void refresh() {
        block.setToBeRequested();
        super.refresh();
    }

    public CommentViewMode getCommentViewMode() {
        return commentViewMode;
    }

    public void toggleCommentViewMode() {
        this.commentViewMode = commentViewMode.toggle();
        updateView();
    }
}