/*
 * EditWatchlistPosition.java
 *
 * Created on 28.03.2008 16:11:58
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.watchlist;

import de.marketmaker.iview.dmxml.MSCPriceDatas;
import de.marketmaker.iview.dmxml.MSCPriceDatasElement;
import de.marketmaker.iview.dmxml.QuoteData;
import de.marketmaker.iview.dmxml.WatchlistPositionElement;
import de.marketmaker.iview.mmgwt.mmweb.client.AbstractMainController;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.QuoteWithInstrument;
import de.marketmaker.iview.mmgwt.mmweb.client.QwiPosition;
import de.marketmaker.iview.mmgwt.mmweb.client.ResponseTypeCallback;
import de.marketmaker.iview.mmgwt.mmweb.client.data.CurrentTrendBar;
import de.marketmaker.iview.mmgwt.mmweb.client.data.TrendBarData;
import de.marketmaker.iview.mmgwt.mmweb.client.prices.Price;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableDataModel;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;

import java.util.List;

/**
 * EditWatchlistPosition.java
 * Created on 06.07.2009 10:38:01
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author Michael Lösch
 */

class EditWatchlistPosition {

    private final DmxmlContext.Block<MSCPriceDatas> block;
    private final WatchlistController controller;
    private final EditWatchlistPositionView view;
    private QwiPosition currentQwiPosition;

    EditWatchlistPosition(WatchlistController controller) {
        this.view = new EditWatchlistPositionView(this);
        this.controller = controller;
        this.block = new DmxmlContext().addBlock("MSC_PriceDatas"); // $NON-NLS-0$
        this.block.setParameter("disablePaging", "true"); // $NON-NLS-0$ $NON-NLS-1$
    }

    void show(QwiPosition qwip) {
        this.currentQwiPosition = qwip;
        this.block.setParameter("symbol", qwip.getQuoteData().getQid()); // $NON-NLS-0$
        this.block.issueRequest(new ResponseTypeCallback() {
            @Override
            protected void onResult() {
                updateView();
                EditWatchlistPosition.this.view.show();
            }
        });
    }

    public void updateView() {
        if (!this.block.isResponseOk()) {
            this.view.update(DefaultTableDataModel.NULL);
            return;
        }
        final MSCPriceDatas data = this.block.getResult();
        final DefaultTableDataModel tdm = new DefaultTableDataModel(data.getElement().size(), 6);
        final TrendBarData tbd = TrendBarData.create(this.block.getResult());
        final String currentMarketName = currentQwiPosition.getQuoteData().getMarketName();
        for (int i = 0; i < data.getElement().size(); i++) {
            final MSCPriceDatasElement e = data.getElement().get(i);
            final Price price = Price.create(e);
            final String marketName = e.getQuotedata().getMarketName();
            final CurrentTrendBar currentTrendBar = new CurrentTrendBar(price.getChangePercent(), tbd);
            tdm.setValuesAt(i, new Object[] {
                    marketName.equals(currentMarketName),  // preselect
                    marketName,
                    price.getLastPrice(),
                    price.getChangeNet(),
                    currentTrendBar,
                    price.getVolume(),
            });
        }
        this.view.update(tdm);
    }

    // callback from the view
    public void updateWatchlistPosition(int selected) {
        final List<MSCPriceDatasElement> elements = this.block.getResult().getElement();
        final QuoteData quoteData = elements.get(selected).getQuotedata();
        final QuoteWithInstrument qwi = new QuoteWithInstrument(this.currentQwiPosition.getInstrumentData(), quoteData);
        // don't override an existing item
        List<WatchlistPositionElement> origElems = controller.getPositionElements();
        for (WatchlistPositionElement elem : origElems) {
            if (elem.getQuotedata().getQid().equals(quoteData.getQid())) {
                AbstractMainController.INSTANCE.showMessage(I18n.I.quoteExistsInWatchlist());
                return; // collision
            }
        }
        this.controller.updatePosition(this.currentQwiPosition.getPositionId(), qwi);
    }
}