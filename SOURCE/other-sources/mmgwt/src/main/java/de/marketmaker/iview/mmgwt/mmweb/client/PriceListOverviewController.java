/*
 * PriceListController.java
 *
 * Created on 16.07.2008 11:31:49
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */

package de.marketmaker.iview.mmgwt.mmweb.client;

import de.marketmaker.iview.dmxml.ListOverviewType;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;
import de.marketmaker.iview.mmgwt.mmweb.client.view.ContentContainer;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PlaceChangeEvent;

/**
 * @author Ulrich Maurer
 */
public class PriceListOverviewController extends AbstractPageController {
    public static final int DEFAULT_COUNT = 30;

    public static final String LIST_ID = "DZ"; // $NON-NLS$

    private DmxmlContext.Block<ListOverviewType> block;

    private PriceListOverviewView view;

    protected PriceListOverviewController(ContentContainer contentContainer) {
        super(contentContainer);
        init();
    }

    protected PriceListOverviewController(ContentContainer contentContainer, DmxmlContext context) {
        super(contentContainer, context);
        init();
    }

    private void init() {
        this.block = this.context.addBlock("MSC_List_Overview"); // $NON-NLS$
        this.block.setParameter("id", LIST_ID); // $NON-NLS$
        this.block.setParameter("variant", SessionData.INSTANCE.getGuiDefValue("market-overview-variant")); // $NON-NLS$
        this.view = new PriceListOverviewView(this);
    }

    public void onPlaceChange(PlaceChangeEvent event) {
        refresh();
    }

    protected void onResult() {
        if (this.block.isResponseOk()) {
            this.view.show(this.block.getResult());
        }
    }
}
