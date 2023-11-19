/*
 * PortraitWNTController.java
 *
 * Created on 17.03.2008 17:07:24
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.portrait;

import de.marketmaker.iview.mmgwt.mmweb.client.Selector;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.util.FlashCheck;
import de.marketmaker.iview.mmgwt.mmweb.client.util.PdfOptionSpec;
import de.marketmaker.iview.mmgwt.mmweb.client.view.ContentContainer;
import de.marketmaker.iview.dmxml.MSCQuoteMetadata;

import java.util.Map;

import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.view.NavItemSpec;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class PortraitWNTController extends AbstractPortraitController implements MetadataAware {
    private static final String DEF_OVERVIEW = "wnt_overview"; // $NON-NLS-0$

    protected static final String DEF_WNT_RATIOS = "wnt_ratios"; // $NON-NLS-0$

    private static final String DEF_PDF = "wnt_pdf"; // $NON-NLS-0$

    protected static final String DEF_ANALYSER = "wnt_analyser"; // $NON-NLS-0$

    protected static final String DEF_WNT_EDG = "wnt_edg"; // $NON-NLS-0$

    private static final String DEF_PM = "wnt_pm"; // $NON-NLS$

    private NavItemSpec navItemEdg;
    private NavItemSpec navItemOrderbook;

    public PortraitWNTController(ContentContainer contentContainer) {
        super(contentContainer, "portrait_wnt"); // $NON-NLS$
    }

    protected void initNavItems() {
        final NavItemSpec typeNavItemsRoot = addNavItemSpec(getNavItemSpecRoot(), "WNT", I18n.I.warrants()); // $NON-NLS$
        addPmReports(getNavItemSpecRoot());

        addNavItemSpec(typeNavItemsRoot, "U", I18n.I.overview(), newOverviewController(DEF_OVERVIEW));  // $NON-NLS-0$
        addPmInstrumentData(typeNavItemsRoot, DEF_PM);
        addNavItemSpec(typeNavItemsRoot, "A", I18n.I.arbitrage(), newOverviewController(DEF_ARBITRAGE), false);  // $NON-NLS-0$
        addNavItemSpec(typeNavItemsRoot, "T", I18n.I.tickAndSaleAbbr(), newTimesAndSalesController());  // $NON-NLS-0$
        this.navItemOrderbook = addNavItemSpec(typeNavItemsRoot, "O", I18n.I.orderBook(), newOverviewController(DEF_ORDERBOOK)); // $NON-NLS$
        addNavItemSpec(typeNavItemsRoot, "K", I18n.I.ratios(), newOverviewController(DEF_WNT_RATIOS));  // $NON-NLS-0$
        this.navItemEdg = addNavItemSpec(typeNavItemsRoot, "EDG", Selector.EDG_RATING, "EDG", newOverviewController(DEF_WNT_EDG)); // $NON-NLS$
        if (!"true".equals(SessionData.INSTANCE.getGuiDefValue("portrait-without-news"))) { // $NON-NLS-0$ $NON-NLS-1$
            addNavItemSpec(typeNavItemsRoot, "N", I18n.I.news(), newOverviewController(DEF_NEWS_UNDERLYING));  // $NON-NLS-0$
        }
        addChartcenter(typeNavItemsRoot, true);
        addChartAnalyser(typeNavItemsRoot);
        addNavItemSpec(typeNavItemsRoot, "G", FlashCheck.isFlashAvailable() && Selector.WARRANT_CALCULATOR.isAllowed(), I18n.I.warrantCalculator(), newOverviewController(DEF_ANALYSER));  // $NON-NLS-0$
        if (Selector.DZ_WERTENTICKLUNGSRECHNER.isAllowed()) {
            addNavItemSpec(typeNavItemsRoot, "PFC", I18n.I.performanceCalculatorShort(), newOverviewController(DEF_PCALC));  // $NON-NLS-0$
        }
        addNavItemSpec(typeNavItemsRoot, "RR", I18n.I.regulatoryReporting(), newOverviewController(DEF_REGULATORY));  // $NON-NLS$
    }

    public boolean isMetadataNeeded() {
        return true;
    }

    public void onMetadataAvailable(MSCQuoteMetadata metadata) {
        getNavItemSelectionModel().setVisibility(this.navItemEdg, metadata.isEdgAvailable());
        getNavItemSelectionModel().setVisibility(this.navItemOrderbook, metadata.isOrderbookAvailable());
    }

    @Override
    protected PdfOptionSpec getDefaultPdfOptionSpec(Map<String, String> map) {
        return new PdfOptionSpec("warrantportrait.pdf", map, null); // $NON-NLS-0$
    }
}
