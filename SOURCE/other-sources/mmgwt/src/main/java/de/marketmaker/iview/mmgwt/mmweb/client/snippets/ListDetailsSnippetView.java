/*
 * PriceListSnippetView.java
 *
 * Created on 31.03.2008 11:23:52
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.snippets;

import com.extjs.gxt.ui.client.widget.toolbar.LabelToolItem;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import de.marketmaker.itools.gwtutil.client.util.date.JsDateFormatter;
import de.marketmaker.itools.gwtutil.client.util.date.MmJsDate;
import de.marketmaker.itools.gwtutil.client.widgets.menu.Menu;
import de.marketmaker.itools.gwtutil.client.widgets.menu.MenuItem;
import de.marketmaker.itools.gwtutil.client.widgets.menu.SelectButton;
import de.marketmaker.iview.dmxml.MSCListDetails;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.paging.PagingWidgets;
import de.marketmaker.iview.mmgwt.mmweb.client.push.PushRenderItem;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DatePickerHeaderRenderer;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableDataModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableColumn;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableColumnModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableDataModel;
import de.marketmaker.iview.mmgwt.mmweb.client.view.FloatingToolbar;
import de.marketmaker.iview.mmgwt.mmweb.client.view.PagingPanel;

import java.util.ArrayList;

import static de.marketmaker.iview.mmgwt.mmweb.client.snippets.ListDetailsSnippet.DEFAULT_COUNT;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class ListDetailsSnippetView extends SnippetView<ListDetailsSnippet> implements
        PagingWidgets.ToolbarAddOn {
    private final TableColumnModel columnModel;

    private final TableColumn columnTrend;

    private PagingPanel pp;

    private SnippetTableWidget tw;

    private Menu menuMarkets;

    private SelectButton buttonMarkets;

    public ListDetailsSnippetView(final ListDetailsSnippet snippet) {
        super(snippet);
        setTitle(I18n.I.pricelist());

        this.columnModel = snippet.getListDetailsHelper()
                .withWkn(true)
                .withBidAskVolume(true)
                .createTableColumnModel();

        if (getConfiguration().getBoolean("showPeriodButton", false)) { // $NON-NLS$
            this.columnTrend = findColumnTrend(this.columnModel);
            if (this.columnTrend != null) {
                this.columnTrend.setSortKey(null);
                this.columnTrend.setHeaderRenderer(createDatePicker());
            }
        }
        else {
            this.columnTrend = null;
        }
    }

    private DatePickerHeaderRenderer createDatePicker() {
        final DatePickerHeaderRenderer result = new DatePickerHeaderRenderer();
        result.setLinkQuickTip(I18n.I.selectComparisonPeriod());
        result.setValue(new MmJsDate().addDays(-1));
        result.setMaxDate(new MmJsDate().addDays(-1));
        result.addValueChangeHandler(new ValueChangeHandler<MmJsDate>() {
            public void onValueChange(ValueChangeEvent<MmJsDate> e) {
                updateDate(e.getValue());
            }
        });
        return result;
    }

    public void onContainerAvailable() {
        super.onContainerAvailable();

        final SnippetConfiguration config = getConfiguration();

        final PagingWidgets.Config widgetsConfig = new PagingWidgets.Config()
                .withAddOn(this);

        this.pp = new PagingPanel(new PagingPanel.Config(this.container)
                .withPageSize(config.getInt("count", DEFAULT_COUNT)) // $NON-NLS-0$
                .withWidgetsConfig(widgetsConfig)
        );
        this.pp.setHandler(snippet);
    }

    private TableColumn findColumnTrend(TableColumnModel columnModel) {
        final int colCount = columnModel.getColumnCount();
        for (int col = 0; col < colCount; col++) {
            final TableColumn tableColumn = columnModel.getTableColumn(col);
            if (I18n.I.trend().equals(tableColumn.getTitle())) {
                return tableColumn;
            }
        }
        return null;
    }


    void update(int offset, int count, int total, TableDataModel dtm) {
        reloadTitle();
        if (this.tw == null) {
            this.tw = SnippetTableWidget.create(this.columnModel);
            this.tw.setSortLinkListener(this.snippet.getSortLinkListener());
            this.container.setContentWidget(this.tw);
        }
        this.tw.updateData(dtm);
        this.pp.update(offset, count, total);
        this.container.layout();
    }

    private void updateDate(MmJsDate date) {
        final long days = date.getDiffDays(new MmJsDate().getMidnight());
        final String period;
        if (days > 1) {
            period = "P" + days + "D"; // $NON-NLS-0$ $NON-NLS-1$
            this.columnTrend.setTitle(JsDateFormatter.formatDdmmyyyy(date));
        }
        else if (days == 1) {
            period = "P1D"; // $NON-NLS-0$
            this.columnTrend.setTitle(I18n.I.previousDay());
        }
        else {
            period = "P1000Y"; // $NON-NLS-0$
            this.columnTrend.setTitle(I18n.I.alltime());
        }
        this.snippet.setPeriod(period);
        this.snippet.ackParametersChanged();
    }

    ArrayList<PushRenderItem> getRenderItems(DefaultTableDataModel dtm) {
        return this.tw.getRenderItems(dtm);
    }

    public void addTo(FloatingToolbar toolbar) {
        final LabelToolItem markets = new LabelToolItem(I18n.I.markets() + ":"); // $NON-NLS$
        markets.addStyleName("mm-toolbar-text"); // $NON-NLS-0$
        toolbar.add(markets);
        this.menuMarkets = new Menu();
        this.buttonMarkets = new SelectButton();
        this.buttonMarkets.setClickOpensMenu(true);
        this.buttonMarkets.withMenu(this.menuMarkets);
        this.buttonMarkets.addSelectionHandler(new SelectionHandler<MenuItem>() {
            @Override
            public void onSelection(SelectionEvent<MenuItem> event) {
                final MenuItem selectedItem = event.getSelectedItem();
                if (selectedItem != null) {
                    snippet.setMarketStrategy((String) selectedItem.getData("ms")); // $NON-NLS$
                }
            }
        });
        this.buttonMarkets.addStyleName("mm-frameless"); // $NON-NLS-0$
        toolbar.add(this.buttonMarkets);
    }

    public void updateMarkets(MSCListDetails ld, int total, String ms) {
        this.menuMarkets.removeAll();
        this.menuMarkets.add(new MenuItem("Default").withData("ms", null));  // $NON-NLS$
        for (MSCListDetails.OptionalMarket market : ld.getOptionalMarket()) {
            if (market.getCount() * 10 > total * 9) {
                this.menuMarkets.add(new MenuItem(market.getName()).withData("ms", market.getMarketStrategy())); // $NON-NLS$
            }
        }
        this.buttonMarkets.setSelectedData("ms", ms); // $NON-NLS$
    }
}
