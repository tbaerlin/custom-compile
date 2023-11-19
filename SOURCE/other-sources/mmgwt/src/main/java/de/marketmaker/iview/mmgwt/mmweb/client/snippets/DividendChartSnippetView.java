/*
 * DividendChartSnippetView.java
 *
 * Created on 15.10.2014 13:30
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.snippets;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.LoadEvent;

import de.marketmaker.iview.dmxml.IMGResult;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;

/**
 * @author jkirchg
 */
public class DividendChartSnippetView extends BasicChartSnippetView<DividendChartSnippet, DividendChartSnippetView> {

    public DividendChartSnippetView(final DividendChartSnippet snippet) {
        super(snippet);
        // Suppress link style
        getImage().setStyleName(""); // $NON-NLS$
    }

    @Override
    public void onLoad(LoadEvent loadEvent) {
        super.onLoad(loadEvent);
        // Set correct tooltip text
        getImage().getElement().setAttribute("qtip", I18n.I.dividendPayments());  // $NON-NLS$
    }

    @Override
    protected void updateFooter(IMGResult ipr) {
        // Don't display a footer
    }

    @Override
    public void onClick(ClickEvent clickEvent) {
        // Suppress link to chartcenter
    }

}
