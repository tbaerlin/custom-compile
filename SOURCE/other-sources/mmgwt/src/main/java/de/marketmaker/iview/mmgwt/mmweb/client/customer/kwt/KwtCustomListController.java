/*
 * KwtCustomListController.java
 *
 * Created on 17.04.2009 10:20:45
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.customer.kwt;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.Label;

import de.marketmaker.iview.dmxml.MSCListDetailElement;
import de.marketmaker.iview.dmxml.MSCListDetails;
import de.marketmaker.iview.mmgwt.mmweb.client.AbstractPageController;
import de.marketmaker.iview.mmgwt.mmweb.client.QuoteWithInstrument;
import de.marketmaker.iview.mmgwt.mmweb.client.TableView;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PlaceChangeEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.data.TrendBarData;
import de.marketmaker.iview.mmgwt.mmweb.client.prices.Price;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.ListDetailsHelper;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableDataModel;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;
import de.marketmaker.iview.mmgwt.mmweb.client.util.SortLinkSupport;
import de.marketmaker.iview.mmgwt.mmweb.client.view.ContentContainer;

import de.marketmaker.iview.mmgwt.mmweb.client.I18n;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class KwtCustomListController extends AbstractPageController {
    public static final String LIST_NAME_PREFIX = "list_kwt_kurslisten"; // $NON-NLS-0$

    private DmxmlContext.Block<MSCListDetails> block;
    
    private TableView view;

    private final ListDetailsHelper helper;

    public KwtCustomListController(ContentContainer contentContainer) {
        super(contentContainer);

        this.block = this.context.addBlock("MSC_PriceDataMulti"); // $NON-NLS-0$
        this.block.setParameter("symbolStrategy", "auto"); // $NON-NLS-0$ $NON-NLS-1$

        this.helper = new ListDetailsHelper(ListDetailsHelper.LinkType.NAME, true, true).withWkn(true);
        this.view = new TableView(this, helper.createTableColumnModel());

        this.view.setSortLinkListener(new SortLinkSupport(this.block, new Command() {
            public void execute() {
                refresh();
            }
        }));
    }

    public void onPlaceChange(PlaceChangeEvent event) {
        final String listName = event.getHistoryToken().get(1, null);
        if (listName == null) {
            onNoData();
            return;
        }
        showList(listName);
    }

    private void showList(String s) {
        final List<QuoteWithInstrument> qwis = SessionData.INSTANCE.getList(LIST_NAME_PREFIX + s);
        if (qwis.isEmpty()) {
            onNoData();
            return;
        }
        final List<String> qids = new ArrayList<String>(qwis.size());
        for (QuoteWithInstrument qwi : qwis) {
            qids.add(qwi.getId());
        }
        this.block.setParameters("symbol", qids.toArray(new String[qids.size()])); // $NON-NLS-0$
        refresh();
    }

    private void onNoData() {
        getContentContainer().setContent(new Label(I18n.I.noDataAvailable())); 
    }

    @Override
    protected void onResult() {
        if (!this.block.isResponseOk()) {
            onNoData();
            return;
        }
        final MSCListDetails result = this.block.getResult();
        final TrendBarData trendBarData = TrendBarData.create(result);
        final int size = result.getElement().size();
        final DefaultTableDataModel model = helper.createTableDataModel(size).withSort(result.getSort());
        for (int i = 0; i < size; i++) {
            final MSCListDetailElement e = result.getElement().get(i);
            this.helper.addRow(model, i, e.getInstrumentdata(), e.getQuotedata(), trendBarData, Price.create(e));
        }
        this.view.show(model);
    }
}
