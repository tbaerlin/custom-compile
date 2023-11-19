/*
 * PriceListSnippetView.java
 *
 * Created on 31.03.2008 11:23:52
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.snippets;


import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

import de.marketmaker.iview.mmgwt.mmweb.client.I18n;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class NewsSearchParamsSnippetView extends SnippetView<NewsSearchParamsSnippet> {
    private TextBox searchstring = new TextBox();

    public NewsSearchParamsSnippetView(NewsSearchParamsSnippet snippet) {
        super(snippet);
        setTitle(I18n.I.searchOptions()); 
    }

    @Override
    protected void onContainerAvailable() {
        super.onContainerAvailable();
        final Grid g = new Grid(3, 2);
        g.setCellPadding(2);
        g.setCellSpacing(2);
        g.setCellSpacing(2);

        g.setText(0, 0, I18n.I.provider()); 
        g.setWidget(0, 1, createProviderWidget());

        g.setText(1, 0, I18n.I.language()); 
        g.setWidget(1, 1, createLangWidget());

        g.setText(2, 0, I18n.I.searchText());
        g.setWidget(2, 1, this.searchstring);
        this.container.setContentWidget(g);
    }

    private Widget createProviderWidget() {
        Grid g = new Grid(1, 6);
        g.setCellPadding(2);
        g.setCellSpacing(2);
        g.setWidget(0, 0, new CheckBox(I18n.I.all())); 
        g.setWidget(0, 1, new CheckBox("Dow Jones")); // $NON-NLS-0$
        g.setWidget(0, 2, new CheckBox("dpa-AFX")); // $NON-NLS-0$
        g.setWidget(0, 3, new CheckBox("AWP")); // $NON-NLS-0$
        g.setWidget(0, 4, new CheckBox("Reuters")); // $NON-NLS-0$
        g.setWidget(0, 5, new CheckBox("SMH")); // $NON-NLS-0$
        return g;
    }

    private Widget createLangWidget() {
        Grid g = new Grid(1, 5);
        g.setCellPadding(2);
        g.setCellSpacing(2);
        g.setWidget(0, 0, new CheckBox(I18n.I.all())); 
        g.setWidget(0, 1, new CheckBox(I18n.I.german())); 
        g.setWidget(0, 2, new CheckBox(I18n.I.english())); 
        g.setWidget(0, 3, new CheckBox(I18n.I.french())); 
        g.setWidget(0, 4, new CheckBox(I18n.I.italian())); 
        return g;
    }

    public void setSearchstring(String searchstring) {
        this.searchstring.setText(searchstring);
    }
}
