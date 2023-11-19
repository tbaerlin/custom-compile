/*
 * QwiMenu.java
 *
 * Created on 11.08.2008 17:09:18
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */

package de.marketmaker.iview.mmgwt.mmweb.client;

import com.extjs.gxt.ui.client.event.MenuEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import com.google.gwt.dom.client.Element;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.PopupPanel;

import de.marketmaker.itools.gwtutil.client.widgets.IconImage;
import de.marketmaker.iview.dmxml.Alert;
import de.marketmaker.iview.dmxml.InstrumentData;
import de.marketmaker.iview.dmxml.PortfolioElement;
import de.marketmaker.iview.dmxml.QuoteData;
import de.marketmaker.iview.dmxml.WatchlistElement;
import de.marketmaker.iview.mmgwt.mmweb.client.cercomparison.CerComparisonController;
import de.marketmaker.iview.mmgwt.mmweb.client.data.ContentFlagsEnum;
import de.marketmaker.iview.mmgwt.mmweb.client.data.InstrumentTypeEnum;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PlaceChangeEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.terminalpages.VwdPageSearch;
import de.marketmaker.iview.mmgwt.mmweb.client.util.AlertUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.PlaceUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.watchlist.PortfolioController;
import de.marketmaker.iview.mmgwt.mmweb.client.watchlist.WatchlistController;
import de.marketmaker.iview.mmgwt.mmweb.client.workspace.FavouriteInstrumentItemsStore;
import de.marketmaker.iview.mmgwt.mmweb.client.workspace.FavouriteItemsStores;
import de.marketmaker.iview.mmgwt.mmweb.client.workspace.InstrumentWorkspace;

import java.util.List;

/**
 * @author Ulrich Maurer
 */
public class QwiMenu {
    public static final QwiMenu INSTANCE = new QwiMenu();

    private final Menu menu;

    private final MenuItem itemWatchlist;

    private QuoteWithInstrument qwi;

    private List<WatchlistElement> watchlists;

    private List<PortfolioElement> portfolios;

    private Menu menuWatchlists;

    private Menu menuPortfolios;

    private final MenuItem itemPortfolio;

    private final MenuItem itemCerCompare;

    private final MenuItem itemBookmark;

    private QwiMenu() {
        this.menu = new Menu();

        this.menu.add(new MenuItem(I18n.I.showPortrait(), new SelectionListener<MenuEvent>() {
            public void componentSelected(MenuEvent menuEvent) {
                qwi.goToPortrait();
            }
        }));

        if ("true".equals(SessionData.INSTANCE.getUserProperty("iqdata"))) { // $NON-NLS$
            this.menu.add(new MenuItem("Instrumentdata/Quotedata", new SelectionListener<MenuEvent>() { // $NON-NLS$
                @Override
                public void componentSelected(MenuEvent ce) {
                    showInstrumentAndQuoteData(qwi);
                }
            }));
        }

        this.menu.add(new MenuItem(I18n.I.showChartcenter(), new SelectionListener<MenuEvent>() {
            public void componentSelected(MenuEvent menuEvent) {
                qwi.goToChartcenter();
            }
        }));

/*
TODO: page search disabled
        final MenuItem itemVwdPages = new MenuItem(I18n.I.vwdPagesUsingThisQwi(),
                new SelectionListener<MenuEvent>() {
                    @Override
                    public void componentSelected(MenuEvent event) {
                        showVwdPages(qwi.getQuoteData().getVwdcode());
                    }
                });
        IconImage.setIconStyle(itemVwdPages, "mm-icon-vwdpage"); // $NON-NLS$
        this.menu.add(itemVwdPages);
*/

        final MenuItem itemLimit = new MenuItem(I18n.I.addLimit(), new SelectionListener<MenuEvent>() {
            public void componentSelected(MenuEvent menuEvent) {
                AlertController.INSTANCE.edit(createAlert(qwi));
            }
        });
        IconImage.setIconStyle(itemLimit, "mm-limits-icon"); // $NON-NLS-0$
        if (SessionData.isWithLimits()) {
            this.menu.add(itemLimit);
        }

        this.itemBookmark = new MenuItem(I18n.I.addBookmarks(), new SelectionListener<MenuEvent>() {
            public void componentSelected(MenuEvent menuEvent) {
                if (SessionData.isAsDesign()) {
                    FavouriteItemsStores.ifPresent(FavouriteInstrumentItemsStore.class,
                            c -> c.addItem(qwi));
                }
                else {
                    InstrumentWorkspace.INSTANCE.add(qwi);
                }
            }
        });
        IconImage.setIconStyle(itemBookmark, "mm-bookmark"); // $NON-NLS-0$
        this.menu.add(itemBookmark);

        this.itemWatchlist = new MenuItem();
        this.itemWatchlist.setText(I18n.I.addToWatchlist());
        IconImage.setIconStyle(this.itemWatchlist, "mm-watchlist"); // $NON-NLS-0$
        this.itemWatchlist.setEnabled(false);
        this.menuWatchlists = new Menu();
        this.itemWatchlist.setSubMenu(this.menuWatchlists);
        this.menu.add(this.itemWatchlist);
        this.itemWatchlist.setHideOnClick(false);

        this.itemPortfolio = new MenuItem();
        this.itemPortfolio.setText(I18n.I.addToPortfolio());
        IconImage.setIconStyle(this.itemPortfolio, "mm-portfolio"); // $NON-NLS-0$
        this.itemPortfolio.setEnabled(false);
        this.menuPortfolios = new Menu();
        this.itemPortfolio.setSubMenu(this.menuPortfolios);
        this.menu.add(itemPortfolio);
        this.itemPortfolio.setHideOnClick(false);

        this.itemCerCompare = new MenuItem(I18n.I.addCertificatesComparison(), new SelectionListener<MenuEvent>() {
            public void componentSelected(MenuEvent menuEvent) {
                CerComparisonController.INSTANCE.setQuote(qwi);
                PlaceUtil.goTo(CerComparisonController.TOKEN);
            }
        });
        IconImage.setIconStyle(this.itemCerCompare, "mm-icon-comparison"); // $NON-NLS-0$
        this.menu.add(this.itemCerCompare);
    }

    private Alert createAlert(QuoteWithInstrument qwi) {
        final Alert result = new Alert();
        result.setQuotedata(qwi.getQuoteData());
        result.setInstrumentdata(qwi.getInstrumentData());
//        result.setReferenceValue(this.currentPrice);
        result.setFieldId(AlertUtil.getDefaultFieldId(qwi.getQuoteData()));
        return result;
    }

    public void show(QuoteWithInstrument qwi, Element anchor) {
        this.qwi = qwi;
        resetBookmarkItem();
        resetWatchlistMenu();
        resetPortfolioMenu();
        setVisibility();
        this.menu.showAt(anchor.getAbsoluteLeft(), anchor.getAbsoluteTop() + anchor.getOffsetHeight());
    }

    private void resetBookmarkItem() {
        if(SessionData.isAsDesign()) {
            FavouriteItemsStores.ifPresent(FavouriteInstrumentItemsStore.class,
                    c -> this.itemBookmark.setEnabled(c.canAddItem(this.qwi)));
        }
    }

    private void setVisibility() {
        this.itemCerCompare.setVisible(InstrumentTypeEnum.valueOf(this.qwi.getInstrumentData().getType()) == InstrumentTypeEnum.CER);
    }

    private void resetWatchlistMenu() {
        final List<WatchlistElement> watchlists = SessionData.INSTANCE.getWatchlists();
        if (watchlists == null) {
            this.itemWatchlist.setEnabled(false);
            return;
        }
        if (watchlists == this.watchlists) {
            return;
        }
        this.menuWatchlists.removeAll();
        if (watchlists.isEmpty()) {
            this.itemWatchlist.setEnabled(false);
            return;
        }
        for (final WatchlistElement watchlist : watchlists) {
            this.menuWatchlists.add(new MenuItem(watchlist.getName(), new SelectionListener<MenuEvent>() {
                public void componentSelected(MenuEvent menuEvent) {
                    addToWatchlist(watchlist);
                }
            }));
        }
        this.itemWatchlist.setEnabled(true);
        this.watchlists = watchlists;
    }


    private void addToWatchlist(WatchlistElement watchlist) {
        WatchlistController.INSTANCE.createPosition(this.qwi, watchlist.getWatchlistid());
    }

    private void resetPortfolioMenu() {
        final List<PortfolioElement> portfolios = SessionData.INSTANCE.getPortfolios();
        if (portfolios == null) {
            this.itemPortfolio.setEnabled(false);
            return;
        }
        if (portfolios == this.portfolios) {
            return;
        }
        this.menuPortfolios.removeAll();
        if (portfolios.isEmpty()) {
            this.itemPortfolio.setEnabled(false);
            return;
        }
        for (final PortfolioElement portfolio : portfolios) {
            this.menuPortfolios.add(new MenuItem(portfolio.getName(), new SelectionListener<MenuEvent>() {
                public void componentSelected(MenuEvent menuEvent) {
                    addToPortfolio(portfolio);
                }
            }));
        }
        this.itemPortfolio.setEnabled(true);
        this.portfolios = portfolios;
    }

    private void addToPortfolio(PortfolioElement portfolio) {
        PortfolioController.INSTANCE.createBuyOrder(portfolio.getPortfolioid(), this.qwi.getQuoteData().getQid());
    }

    /**
     * Starts a search for pages containing the given symbol.
     */
    @SuppressWarnings("unused")
    private void showVwdPages(String symbol) {
        PlaceUtil.fire(new PlaceChangeEvent(StringUtil.joinTokens(
                VwdPageSearch.SYMBOL_SEARCH_KEY, symbol)));
    }

    public static void showInstrumentAndQuoteData(QuoteWithInstrument qwi) {
        final FlexTable table = new FlexTable();
        final FlexTable.FlexCellFormatter formatter = table.getFlexCellFormatter();

        final InstrumentData id = qwi.getInstrumentData();
        int row = 0;
        table.setText(row, 0, "QuoteWithInstrument:"); // $NON-NLS$
        formatter.setColSpan(row, 0, 2);
        formatter.addStyleName(row, 0, "b");
        row++;
        table.setText(row, 0, "name"); // $NON-NLS$
        table.setText(row, 1, qwi.getName());
        row++;
        table.setText(row, 0, "realName"); // $NON-NLS$
        table.setText(row, 1, qwi.getRealName());
        row++;
        table.setText(row, 0, "InstrumentData:"); // $NON-NLS$
        formatter.setColSpan(row, 0, 2);
        formatter.addStyleName(row, 0, "b");
        row++;
        table.setText(row, 0, "iid"); // $NON-NLS$
        table.setText(row, 1, id.getIid());
        row++;
        if (SessionData.INSTANCE.isShowIsin()) {
            table.setText(row, 0, "isin"); // $NON-NLS$
            table.setText(row, 1, id.getIsin());
            row++;
        }
        if (SessionData.INSTANCE.isShowWkn()) {
            table.setText(row, 0, "wkn"); // $NON-NLS$
            table.setText(row, 1, id.getWkn());
            row++;
        }
        table.setText(row, 0, "name"); // $NON-NLS$
        table.setText(row, 1, id.getName());
        row++;
        table.setText(row, 0, "type"); // $NON-NLS$
        table.setText(row, 1, id.getType());
        row++;
        table.setText(row, 0, "expiration"); // $NON-NLS$
        table.setText(row, 1, id.getExpiration());

        final QuoteData qd = qwi.getQuoteData();
        if (qd == null) {
            row++;
            table.setText(row, 0, "QuoteData is null"); // $NON-NLS$
            formatter.setColSpan(row, 0, 2);
            formatter.addStyleName(row, 0, "b");
        }
        else {
            row++;
            table.setText(row, 0, "QuoteData:"); // $NON-NLS$
            formatter.setColSpan(row, 0, 2);
            formatter.addStyleName(row, 0, "b");
            row++;
            table.setText(row, 0, "qid"); // $NON-NLS$
            table.setText(row, 1, qd.getQid());
            row++;
            table.setText(row, 0, "vwdcode"); // $NON-NLS$
            table.setText(row, 1, qd.getVwdcode());
            row++;
            table.setText(row, 0, "marketVwd"); // $NON-NLS$
            table.setText(row, 1, qd.getMarketVwd());
            row++;
            table.setText(row, 0, "marketName"); // $NON-NLS$
            table.setText(row, 1, qd.getMarketName());
            row++;
            table.setText(row, 0, "currencyIso"); // $NON-NLS$
            table.setText(row, 1, qd.getCurrencyIso());
            row++;
            table.setText(row, 0, "quotedPer"); // $NON-NLS$
            table.setText(row, 1, qd.getQuotedPer());
            row++;
            table.setText(row, 0, "contentFlags"); // $NON-NLS$
            table.setText(row, 1, qd.getContentFlags());
            final SafeHtmlBuilder sb = new SafeHtmlBuilder();
            String br = null;
            for (ContentFlagsEnum cf : ContentFlagsEnum.values()) {
                if (cf.isAvailableFor(qd)) {
                    if (br == null) {
                        br = "<br/>"; // $NON-NLS$
                    }
                    else {
                        sb.appendHtmlConstant(br);
                    }
                    sb.appendEscaped(cf.toString());
                }
            }
            row++;
            table.setHTML(row, 1, sb.toSafeHtml());
        }

        final PopupPanel popupPanel = new PopupPanel(true, true);
        popupPanel.addStyleName("mm-iqdata-window");
        popupPanel.setWidget(table);
        popupPanel.setPopupPositionAndShow((offsetWidth, offsetHeight) -> {
            int left = (Window.getClientWidth() - offsetWidth) / 3;
            int top = (Window.getClientHeight() - offsetHeight) / 3;
            popupPanel.setPopupPosition(left, top);
        });
    }
}
