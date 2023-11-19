/*
 * PortraitChartSnippetView.java
 *
 * Created on 15.05.2008 14:07:40
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.snippets;

import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.ui.SimplePanel;
import de.marketmaker.itools.gwtutil.client.widgets.Button;
import de.marketmaker.itools.gwtutil.client.widgets.menu.Menu;
import de.marketmaker.itools.gwtutil.client.widgets.menu.MenuItem;
import de.marketmaker.itools.gwtutil.client.widgets.menu.SelectButton;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PlaceChangeEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.view.FloatingToolbar;
import de.marketmaker.iview.mmgwt.mmweb.client.view.IndexedViewSelectionModel;
import de.marketmaker.iview.mmgwt.mmweb.client.view.ViewSelectionView;
import de.marketmaker.iview.mmgwt.mmweb.client.view.ViewSelectionViewButtons;
import de.marketmaker.iview.mmgwt.mmweb.client.view.ViewSelectionViewCombo;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
class FlipChartSnippetView extends BasicChartSnippetView<FlipChartSnippet, FlipChartSnippetView> {
    private final SimplePanel toolbarContainer = new SimplePanel();

    private String selectedPeriod = null;

    public FlipChartSnippetView(FlipChartSnippet snippet) {
        super(snippet);
        setupToolbar();
    }

    private void setupToolbar() {
        final FloatingToolbar topToolbar = createViewSelectionView().getToolbar();
        this.toolbarContainer.setWidget(topToolbar);
        if (this.snippet.getConfiguration().getBoolean("historySwitch", false)) { // $NON-NLS-0$
            topToolbar.addEmpty();
            topToolbar.add(createHistorySwitch());
        }
    }

    private ViewSelectionView createViewSelectionView() {
        final String buttonType = getConfiguration().getString("buttonType", "button"); // $NON-NLS$
        final IndexedViewSelectionModel model = this.snippet.getMultiViewSupport().getViewSelectionModel();
        return ("combo".equals(buttonType)) // $NON-NLS-0$
                ? new ViewSelectionViewCombo(model)
                : new ViewSelectionViewButtons(model);
    }

    protected void onContainerAvailable() {
        this.container.setTopWidget(this.toolbarContainer);
        super.onContainerAvailable();
    }

    @Override
    protected void goToChartcenter(PlaceChangeEvent event) {
        if (this.selectedPeriod != null) {
            super.goToChartcenter(event.withProperty("period", this.selectedPeriod)); // $NON-NLS-0$
        }
        else {
            super.goToChartcenter(event);
        }
    }

    private Button createHistorySwitch() {
        this.selectedPeriod = this.snippet.getConfiguration().getString("period"); // $NON-NLS-0$
        final Menu menu = new Menu();
        final SelectButton button = new SelectButton().withMenu(menu);
        button.setClickOpensMenu(true);
        addItem(button, menu, I18n.I.nDays(1), "P1D");  // $NON-NLS-0$
        addItem(button, menu, I18n.I.nDays(5), "P5D");  // $NON-NLS-0$
        addItem(button, menu, I18n.I.nMonths(1), "P1M");  // $NON-NLS-0$
        addItem(button, menu, I18n.I.nMonths(3), "P3M");  // $NON-NLS-0$
        addItem(button, menu, I18n.I.nMonths(6), "P6M");  // $NON-NLS-0$
        addItem(button, menu, I18n.I.nYears(1), "P1Y");  // $NON-NLS-0$
        addItem(button, menu, I18n.I.nYears(2), "P2Y");  // $NON-NLS-0$
        addItem(button, menu, I18n.I.nYears(3), "P3Y");  // $NON-NLS-0$
        addItem(button, menu, I18n.I.nYears(5), "P5Y");  // $NON-NLS-0$
        addItem(button, menu, I18n.I.nYears(10), "P10Y");  // $NON-NLS-0$
        addItem(button, menu, I18n.I.total(), null);
        button.addSelectionHandler(new SelectionHandler<MenuItem>() {
            @Override
            public void onSelection(SelectionEvent<MenuItem> event) {
                setSelectedPeriod((String) event.getSelectedItem().getData("period")); // $NON-NLS$
            }
        });
        return button;
    }

    private void addItem(SelectButton button, Menu menu, final String name, final String period) {
        final MenuItem menuItem = new MenuItem(name, null);
        menuItem.setData("period", period); // $NON-NLS-0$
        menu.add(menuItem);
        if (isConfiguredPeriod(period)) {
            button.setSelectedItem(menuItem);
        }
    }

    private void setSelectedPeriod(String period) {
        this.selectedPeriod = period;
        this.snippet.setPeriod(period);
    }

    public void setTitle(String title) {
        if (this.container != null) {
            this.container.setHeading(title);
        }
        else {
            super.setTitle(title);
        }
    }

    public void onGuidefsChange() {
        setupToolbar();
    }
}
