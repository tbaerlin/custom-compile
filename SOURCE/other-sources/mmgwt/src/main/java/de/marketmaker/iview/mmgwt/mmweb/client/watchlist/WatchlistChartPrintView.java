/*
* WatchlistView.java
*
* Created on 11.08.2008 10:41:32
*
* Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
*/
package de.marketmaker.iview.mmgwt.mmweb.client.watchlist;

import java.util.List;

import com.extjs.gxt.ui.client.widget.ContentPanel;

import de.marketmaker.iview.mmgwt.mmweb.client.QuoteWithInstrument;
import de.marketmaker.iview.mmgwt.mmweb.client.desktop.ChartIcon;
import de.marketmaker.iview.mmgwt.mmweb.client.desktop.Desktop;
import de.marketmaker.iview.mmgwt.mmweb.client.util.WatchlistPortfolioUtil;


/**
 * @author Michael Lösch
 */

class WatchlistChartPrintView extends ContentPanel {

    private Desktop<QuoteWithInstrument> desktop;

    public WatchlistChartPrintView() {
        addStyleName("mm-contentData"); // $NON-NLS-0$
        setBorders(false);
        setScrollMode(com.extjs.gxt.ui.client.Style.Scroll.AUTO);
    }

    public void update(List<ChartIcon> list) {
        if (this.desktop != null) {
            remove(this.desktop);
        }
        this.desktop = new Desktop<QuoteWithInstrument>(Desktop.Mode.TABLE);
        add(this.desktop);
        for (ChartIcon icon : list) {
            this.desktop.add(icon);
        }
    }

    public String getChartGalleryPrintHtml(String watchlistName) {
        StringBuilder sb = new StringBuilder();
        sb.append(WatchlistPortfolioUtil.getPrintHeadWithDate(watchlistName))
                .append(this.desktop.getElement().getInnerHTML());
        return sb.toString();
    }

}
