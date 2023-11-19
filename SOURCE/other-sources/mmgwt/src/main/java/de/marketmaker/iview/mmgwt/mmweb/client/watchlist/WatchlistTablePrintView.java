/*
* WatchlistView.java
*
* Created on 11.08.2008 10:41:32
*
* Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
*/
package de.marketmaker.iview.mmgwt.mmweb.client.watchlist;

import com.extjs.gxt.ui.client.widget.ContentPanel;

import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SnippetTableWidget;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableColumnModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableDataModel;
import de.marketmaker.iview.mmgwt.mmweb.client.util.WatchlistPortfolioUtil;


/**
 * @author Michael Lösch
 */

class WatchlistTablePrintView extends ContentPanel {

    final public SnippetTableWidget stw;

    public WatchlistTablePrintView(WatchlistController controller) {
        addStyleName("mm-contentData"); // $NON-NLS-0$
        setBorders(false);
        setScrollMode(com.extjs.gxt.ui.client.Style.Scroll.AUTO);
        /////////////////////////////////////////////////////////////////////////////
        //Table /////////////////////////////////////////////////////////////////////
        final TableColumnModel columnModel = controller.getColumnModel();
        this.stw = SnippetTableWidget.create(columnModel, "mm-listSnippetTable"); // $NON-NLS-0$
        this.stw.setSortLinkListener(controller.getSortLinkListener());
        /////////////////////////////////////////////////////////////////////////////
        add(this.stw);
    }

    public void update(TableDataModel dtm) {
        this.stw.updateData(dtm);
    }

    public String getPrintHtml(String watchlistName) {
        StringBuilder sb = new StringBuilder();
        sb.append(WatchlistPortfolioUtil.getPrintHeadWithDate(watchlistName))
                .append(this.stw.getElement().getInnerHTML());
        return sb.toString();
    }
}
