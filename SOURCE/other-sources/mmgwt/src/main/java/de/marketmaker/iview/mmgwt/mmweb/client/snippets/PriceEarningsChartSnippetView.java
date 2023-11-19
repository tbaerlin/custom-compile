/*
 * PriceEarningsChartSnippetView.java
 *
 * Created on 19.02.2009 13:34:00
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.snippets;

import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import de.marketmaker.itools.gwtutil.client.widgets.LeftRightToolbar;
import de.marketmaker.itools.gwtutil.client.widgets.menu.Menu;
import de.marketmaker.itools.gwtutil.client.widgets.menu.MenuItem;
import de.marketmaker.itools.gwtutil.client.widgets.menu.SelectButton;
import de.marketmaker.iview.dmxml.IMGResult;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PlaceChangeEvent;

/**
 * @author Ulrich Maurer
 */
public class PriceEarningsChartSnippetView extends BasicChartSnippetView<PriceEarningsChartSnippet, PriceEarningsChartSnippetView>
        implements MarketSelectionButton.Callback {

    private LeftRightToolbar toolbar;
    private String selectedPeriod = "P1Y"; // $NON-NLS-0$

    public PriceEarningsChartSnippetView(PriceEarningsChartSnippet snippet) {
        super(snippet);

        this.toolbar = new LeftRightToolbar();
        this.toolbar.addStyleName("mm-viewWidget"); // $NON-NLS-0$

        final Menu m = new Menu();
        m.add(createItem(I18n.I.nMonths(1), "P1M"));  // $NON-NLS-0$
        m.add(createItem(I18n.I.nMonths(3), "P3M"));  // $NON-NLS-0$
        m.add(createItem(I18n.I.nMonths(6), "P6M"));  // $NON-NLS-0$
        final MenuItem defaultItem = createItem(I18n.I.nYears(1), this.selectedPeriod);
        m.add(defaultItem);
        m.add(createItem(I18n.I.nYears(2), "P2Y"));  // $NON-NLS-0$
        m.add(createItem(I18n.I.nYears(3), "P3Y"));  // $NON-NLS-0$
        m.add(createItem(I18n.I.nYears(5), "P5Y"));  // $NON-NLS-0$
        m.add(createItem(I18n.I.nYears(10), "P10Y"));  // $NON-NLS-0$
        m.add(createItem(I18n.I.total(), null));

        final SelectButton buttonPeriod = new SelectButton();
        buttonPeriod.withMenu(m);
        buttonPeriod.setSelectedItem(defaultItem);
        buttonPeriod.setClickOpensMenu(true);
        buttonPeriod.addSelectionHandler(new SelectionHandler<MenuItem>() {
            @Override
            public void onSelection(SelectionEvent<MenuItem> event) {
                select(event.getSelectedItem());
            }
        });

        this.toolbar.addLeft(buttonPeriod);
    }

    private void select(MenuItem menuItem) {
        this.selectedPeriod = (String) menuItem.getData("period"); // $NON-NLS$
        this.snippet.setPeriod(this.selectedPeriod);
    }

    protected void update(IMGResult ipr) {
        setImageUrl(ipr, "PE"); // $NON-NLS-0$
    }

    protected void onContainerAvailable() {
        this.container.setTopWidget(this.toolbar);
        super.onContainerAvailable();
    }

    @Override
    protected void goToChartcenter(PlaceChangeEvent event) {
        super.goToChartcenter(event.withProperty("period", this.selectedPeriod)); // $NON-NLS-0$
    }

    private MenuItem createItem(String name, String period) {
        final MenuItem menuItem = new MenuItem(name, null);
        menuItem.setData("period", period); // $NON-NLS$
        return menuItem;
    }

    public void updateQuote(String qid) {
        this.snippet.updateQuote(qid);
    }
}
