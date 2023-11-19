/*
 * VwdPageController.java
 *
 * Created on 17.03.2008 17:07:24
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.portrait;

import de.marketmaker.iview.mmgwt.mmweb.client.Selector;
import de.marketmaker.iview.mmgwt.mmweb.client.util.PdfOptionSpec;
import de.marketmaker.iview.mmgwt.mmweb.client.view.ContentContainer;

import java.util.Map;

import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.view.NavItemSpec;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class PortraitOPTController extends AbstractPortraitController {
    private static final String DEF_OVERVIEW = "opt_overview"; // $NON-NLS-0$
    private static final String DEF_PM = "opt_pm"; // $NON-NLS$

    public PortraitOPTController(ContentContainer contentContainer) {
        super(contentContainer, "portrait_opt"); // $NON-NLS$
    }

    protected void initNavItems() {
        final NavItemSpec typeNavItemsRoot = addNavItemSpec(getNavItemSpecRoot(), "OPT", I18n.I.options()); // $NON-NLS$
        addPmReports(getNavItemSpecRoot());

        addNavItemSpec(typeNavItemsRoot, "U", I18n.I.overview(), newOverviewController(DEF_OVERVIEW));  // $NON-NLS-0$
        addPmInstrumentData(typeNavItemsRoot, DEF_PM);
        addNavItemSpec(typeNavItemsRoot, "A", I18n.I.arbitrage(), newOverviewController(DEF_ARBITRAGE), false);  // $NON-NLS-0$
        addNavItemSpec(typeNavItemsRoot, "T", I18n.I.tickAndSaleAbbr(), newTimesAndSalesController());  // $NON-NLS-0$
        addNavItemSpec(typeNavItemsRoot, "K", I18n.I.ratios(), newOverviewController(DEF_RATIOS));  // $NON-NLS-0$
        addNavItemSpec(typeNavItemsRoot, "N", I18n.I.news(), newOverviewController(DEF_NEWS_UNDERLYING));  // $NON-NLS-0$
        addChartcenter(typeNavItemsRoot, true);
        addChartAnalyser(typeNavItemsRoot);
        if (Selector.DZ_WERTENTICKLUNGSRECHNER.isAllowed()) {
            addNavItemSpec(typeNavItemsRoot, "PFC", I18n.I.performanceCalculatorShort(), newOverviewController(DEF_PCALC));  // $NON-NLS-0$
        }
        addNavItemSpec(typeNavItemsRoot, "RR", I18n.I.regulatoryReporting(), newOverviewController(DEF_REGULATORY));  // $NON-NLS$CUR
    }

    @Override
    protected PdfOptionSpec getDefaultPdfOptionSpec(Map<String, String> map) {
        return null;
    }
}
