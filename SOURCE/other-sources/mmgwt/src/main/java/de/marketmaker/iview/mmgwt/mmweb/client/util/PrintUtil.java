package de.marketmaker.iview.mmgwt.mmweb.client.util;

import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.SimplePanel;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.DzNewsRelatedOffersSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.NewsEntrySnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.NewsrelatedQuotesSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.PortraitChartSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.PriceTeaserSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.Snippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SymbolSnippet;

import java.util.List;

/**
 * PrintUtil.java
 * Created on Feb 4, 2009 12:06:35 PM
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author Michael Lösch
 */
public class PrintUtil {

    public static String getNewsPrintHtml(NewsEntrySnippet entry, NewsrelatedQuotesSnippet related,
                                          PortraitChartSnippet chart,
                                          DzNewsRelatedOffersSnippet dzRelatedOffers,
                                          SymbolSnippet dzSnippet) {
        final Panel p = new SimplePanel();
        final Grid grid;

        if (dzRelatedOffers != null && dzSnippet != null) {
            grid = new Grid(5, 1);
        }
        else {
            grid = new Grid(3, 1);
        }

        grid.setHTML(0, 0, entry.getView().getPrintHtml());
        if (related != null && chart != null && related.isVisible()) {
            grid.setHTML(1, 0, related.getView().getElement().getInnerHTML());
            grid.setHTML(2, 0, chart.getView().getElement().getInnerHTML());
        }
        if (dzRelatedOffers != null && dzSnippet != null && dzRelatedOffers.isVisible()) {
            grid.setHTML(3, 0, dzRelatedOffers.getView().getElement().getInnerHTML());

            if(dzSnippet instanceof Snippet) {
                grid.setHTML(4, 0, ((Snippet) dzSnippet).getView().getElement().getInnerHTML());
            }
        }

        p.add(grid);
        return p.getElement().getInnerHTML();

    }
}
