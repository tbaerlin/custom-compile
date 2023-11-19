/*
 * InstrumentWorkspace.java
 *
 * Created on 05.04.2008 14:55:07
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.workspace;

import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MenuEvent;
import com.extjs.gxt.ui.client.widget.menu.CheckMenuItem;

import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.iview.dmxml.SelectedWatchlist;
import de.marketmaker.iview.dmxml.WLWatchlist;
import de.marketmaker.iview.dmxml.WatchlistElement;
import de.marketmaker.iview.dmxml.WatchlistPositionElement;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.itools.gwtutil.client.widgets.IconImage;
import de.marketmaker.iview.mmgwt.mmweb.client.QuoteWithInstrument;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PushRegisterEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.prices.Price;
import de.marketmaker.iview.mmgwt.mmweb.client.push.PushRenderItem;
import de.marketmaker.iview.mmgwt.mmweb.client.util.PlaceUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.PortfolioUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class WatchlistWorkspace extends MultiListWorkspace<WLWatchlist, WatchlistPositionElement> {

    public static final WatchlistWorkspace INSTANCE = new WatchlistWorkspace();

    public WatchlistWorkspace() {
        super(I18n.I.watchlist());
        this.block = this.context.addBlock("WL_Watchlist"); // $NON-NLS-0$
        this.block.setParameter("userid", SessionData.INSTANCE.getUser().getVwdId()); // $NON-NLS-0$
        this.block.setParameter("disablePaging", "true"); // $NON-NLS-0$ $NON-NLS-1$
        this.block.setParameter("sortBy", "name"); // $NON-NLS-0$ $NON-NLS-1$

        addListsChangeListener(true);

        IconImage.setIconStyle(this.button, "mm-watchlist"); // $NON-NLS-0$
        IconImage.setIconStyle(this.buttonOpenList, "mm-goto-watchlist"); // $NON-NLS-0$

        this.buttonOpenList.setTitle(I18n.I.toWatchlist()); 

        reorganizeMenu();
    }

    @Override
    public String getStateKey() {
        return StateSupport.WATCHLIST;
    }

    public ArrayList<PushRenderItem> onPushRegister(PushRegisterEvent event) {
        if (this.block.isResponseOk()) {
            final int numAdded = event.addVwdcodes(this.block.getResult());
            Firebug.log("numAdded = " + numAdded); // $NON-NLS-0$
            if (numAdded == this.block.getResult().getPositions().getPosition().size()) {
                Firebug.log("toReload!"); // $NON-NLS-0$
                event.addComponentToReload(this.block, this.pushReloadCallback);
            }
            if (numAdded > 0) {
                return getRenderItems();
            }
        }
        return null;
    }

    protected void openCurrentList() {
        PlaceUtil.goTo("B_W/" + block.getParameter("watchlistid")); // $NON-NLS-0$ $NON-NLS-1$
    }

    protected String getCurrentListId() {
        final SelectedWatchlist watchlist = this.block.getResult().getWatchlist();
        return watchlist == null ? null : watchlist.getWatchlistid();
    }

    protected String getCurrentName() {
        final SelectedWatchlist watchlist = this.block.getResult().getWatchlist();
        return watchlist == null ? "n/a" : watchlist.getName(); // $NON-NLS$
    }

    protected List<WatchlistPositionElement> getCurrentList() {
        return this.block.getResult().getPositions().getPosition();
    }

    protected Price getPrice(WatchlistPositionElement data) {
        return Price.create(data);
    }

    @Override
    protected QuoteWithInstrument getQuoteWithInstrument(WatchlistPositionElement e) {
        return new QuoteWithInstrument(e.getInstrumentdata(), e.getQuotedata());
    }

    protected void reorganizeMenu() {
        final List<WatchlistElement> elements = SessionData.INSTANCE.getWatchlists();
        if (this.currentListId != null
                && !PortfolioUtil.isWatchlistIdOk(elements, this.currentListId)) {
            this.currentListId = null;
        }

        if (!elements.isEmpty()) {
            if (this.currentListId == null) {
                final WatchlistElement e = elements.get(0);
                this.currentListId = e.getWatchlistid();
                this.block.setParameter("watchlistid", this.currentListId); // $NON-NLS-0$
            }
        }

        this.menu.removeAll();
        for (final WatchlistElement e : elements) {
            this.menu.add(createMenuItem(e));
        }
    }

    private CheckMenuItem createMenuItem(final WatchlistElement e) {
        final CheckMenuItem result = new CheckMenuItem(e.getName());
        result.setChecked(e.getWatchlistid().equals(this.currentListId));
        result.setGroup("wlws"); // $NON-NLS-0$
        result.addListener(Events.CheckChange, new Listener<MenuEvent>() {
            public void handleEvent(MenuEvent event) {
                if (result.isChecked()) {
                    block.setParameter("watchlistid", e.getWatchlistid()); // $NON-NLS-0$
                    refresh();
                }
            }
        });
        return result;
    }
}
