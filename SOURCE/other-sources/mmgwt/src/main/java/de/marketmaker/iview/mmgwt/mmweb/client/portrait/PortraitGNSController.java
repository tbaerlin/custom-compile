/*
 * PortraitGNSController.java
 *
 * Created on 17.03.2008 17:07:24
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.portrait;

import de.marketmaker.iview.mmgwt.mmweb.client.Selector;
import de.marketmaker.iview.mmgwt.mmweb.client.util.FlashCheck;
import de.marketmaker.iview.mmgwt.mmweb.client.util.PdfOptionSpec;
import de.marketmaker.iview.mmgwt.mmweb.client.view.ContentContainer;

import java.util.Map;

import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.view.NavItemSpec;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class PortraitGNSController extends AbstractPortraitController {
    private static final String DEF_OVERVIEW = "gns_overview"; // $NON-NLS-0$
    protected static final String DEF_ANALYSER = "gns_analyser"; // $NON-NLS-0$
    private static final String DEF_PM = "gns_pm"; // $NON-NLS$

    public PortraitGNSController(ContentContainer contentContainer) {
        super(contentContainer, "portrait_gns"); // $NON-NLS$
    }

    protected void initNavItems() {
        final NavItemSpec typeNavItemsRoot = addNavItemSpec(getNavItemSpecRoot(), "GNS", I18n.I.bonusShares()); // $NON-NLS$
        addPmReports(getNavItemSpecRoot());

        addNavItemSpec(typeNavItemsRoot, "U", I18n.I.overview(), newOverviewController(DEF_OVERVIEW));  // $NON-NLS-0$
        addPmInstrumentData(typeNavItemsRoot, DEF_PM);
        addNavItemSpec(typeNavItemsRoot, "A", I18n.I.arbitrage(), newOverviewController(DEF_ARBITRAGE), false);  // $NON-NLS-0$
        addNavItemSpec(typeNavItemsRoot, "T", I18n.I.tickAndSaleAbbr(), newTimesAndSalesController());  // $NON-NLS-0$
        addNavItemSpec(typeNavItemsRoot, "K", I18n.I.ratios(), newOverviewController(DEF_RATIOS));  // $NON-NLS-0$
        addChartcenter(typeNavItemsRoot, false);
        addChartAnalyser(typeNavItemsRoot);
        addNavItemSpec(typeNavItemsRoot, "G", FlashCheck.isFlashAvailable() && Selector.BOND_CALCULATOR.isAllowed(), I18n.I.analyst(), newOverviewController(DEF_ANALYSER));  // $NON-NLS-0$
        if (Selector.DZ_WERTENTICKLUNGSRECHNER.isAllowed()) {
            addNavItemSpec(typeNavItemsRoot, "PFC", I18n.I.performanceCalculatorShort(), newOverviewController(DEF_PCALC));  // $NON-NLS-0$
        }
    }

    @Override
    protected PdfOptionSpec getDefaultPdfOptionSpec(Map<String, String> map) {
        return new PdfOptionSpec("bondportrait.pdf", map, null); // $NON-NLS-0$
    }
}
