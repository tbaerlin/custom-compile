/*
 * SnippetMenuConfig.java
 *
 * Created on 29.02.12 10:37
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.myspace;

import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.iview.mmgwt.mmweb.client.FeatureFlags;
import de.marketmaker.iview.mmgwt.mmweb.client.Selector;
import de.marketmaker.iview.mmgwt.mmweb.client.customer.Customer;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.AlertsSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.ArbitrageSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.CalendarSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.CapitalMarketFavoritesSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.CompactQuoteSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.DzPageSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.FavouritesSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.MarketOverviewSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.NewsHeadlinesSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.OrderbookSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.PortfolioSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.PortraitChartSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.PriceListSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SnippetClass;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.TopFlopSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.VwdPageSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.WatchlistSnippet;

import java.util.ArrayList;

/**
 * @author oflege
 */
public class SnippetMenuConfig {

    public static class Item {
        private final SnippetClass clazz;

        private final Selector selector;

        private final FeatureFlags.Feature feature;

        private Item(SnippetClass clazz) {
            this(clazz, null, null);
            if (clazz.getTitle() == null) {
                Firebug.warn("SnippetMenuConfig.Item - title must be set for snippet class: " + this.clazz.getSnippetClassName()); // $NON-NLS$
            }
        }

        private Item(SnippetClass clazz, Selector selector) {
            this(clazz, selector, null);
        }

        private Item(SnippetClass clazz, Selector selector, FeatureFlags.Feature feature) {
            this.clazz = clazz;
            this.selector = selector;
            this.feature = feature;
        }

        public String getTitle() {
            return this.clazz.getTitle();
        }

        public SnippetClass getClazz() {
            return clazz;
        }
    }

    public static Item COMPACT_QUOTE = new Item(new CompactQuoteSnippet.Class());

    public static Item ORDERBOOK = new Item(new OrderbookSnippet.Class(), null);

    public static Item ARBITRAGE = new Item(new ArbitrageSnippet.Class());

    public static Item PRICE_LIST = new Item(new PriceListSnippet.Class());

    public static Item CAPITALMARKET_FAV = new Item(new CapitalMarketFavoritesSnippet.Class(), Selector.DZ_KAPITALMARKT);

    public static Item TOP_FLOP = new Item(new TopFlopSnippet.Class());

    public static Item CHART = new Item(new PortraitChartSnippet.Class());

    public static Item MARKET_OVERVIEW = new Item(new MarketOverviewSnippet.Class());

    public static Item NEWS = new Item(new NewsHeadlinesSnippet.Class());

    public static Item CALENDAR = new Item(new CalendarSnippet.Class());

    public static Item VWD_PAGES = new Item(new VwdPageSnippet.Class(), Selector.PAGES_VWD);

    public static Item DZBANK_PAGES = new Item(new DzPageSnippet.Class(), Selector.PAGES_DZBANK);

    public static Item WATCHLIST = new Item(new WatchlistSnippet.Class());

    public static Item PORTFOLIO = new Item(new PortfolioSnippet.Class());

    public static Item ALERTS = new Item(new AlertsSnippet.Class());

    public static Item FAVOURITES = new Item(new FavouritesSnippet.Class());

    /**
     * The visibility of these menu elements can be overwritten by using
     * the guidefs entry <code>myspace-snippets-menu</code> in a zone specific guidefs.
     * The default visibility is set with the <code>default-myspace-snippets-menu</code>
     * entry in the default guidefs.
     */
    public static SnippetMenuConfig INSTANCE = new SnippetMenuConfig(
            COMPACT_QUOTE,
            ORDERBOOK,
            ARBITRAGE,
            PRICE_LIST,
            CAPITALMARKET_FAV,
            TOP_FLOP,
            CHART,
            MARKET_OVERVIEW,
            NEWS,
            CALENDAR,
            VWD_PAGES,
            DZBANK_PAGES,
            WATCHLIST,
            PORTFOLIO,
            SessionData.isAsDesign() && SessionData.isWithLimits() ? ALERTS : null,
            SessionData.isAsDesign() && SessionData.isWithMarketData() ? FAVOURITES : null
    );

    private final ArrayList<Item> items = new ArrayList<>();

    public SnippetMenuConfig(Item... items) {
        for (Item item : items) {
            if (item != null && Customer.INSTANCE.isJsonMyspaceSnippetsMenuElementTrue(item.getClazz().getSnippetClassName())
                    && (item.selector == null || item.selector.isAllowed())
                    && (item.feature == null || item.feature.isEnabled())) {
                this.items.add(item);
            }
        }
    }

    public void add(SnippetClass clazz) {
        this.items.add(new Item(clazz));
    }

    public ArrayList<Item> getItems() {
        return items;
    }
}
