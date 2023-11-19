/*
 * PortraitMERController.java
 *
 * Created on 17.03.2008 17:07:24
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.portrait;

import de.marketmaker.iview.mmgwt.mmweb.client.FeatureFlags;
import de.marketmaker.iview.mmgwt.mmweb.client.util.PdfOptionSpec;
import de.marketmaker.iview.mmgwt.mmweb.client.view.ContentContainer;

import java.util.Map;

import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.view.NavItemSpec;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class PortraitMERController extends AbstractPortraitController {
    private static final String DEF_OVERVIEW = "mer_overview"; // $NON-NLS-0$
    private static final String DEF_PM = "mer_pm"; // $NON-NLS-0$

    public PortraitMERController(ContentContainer contentContainer) {
        super(contentContainer, "portrait_mer"); // $NON-NLS$
    }

    protected void initNavItems() {
        final NavItemSpec typeNavItemsRoot = addNavItemSpec(getNavItemSpecRoot(), "MER", I18n.I.commodities()); // $NON-NLS$
        addPmReports(getNavItemSpecRoot());

        addNavItemSpec(typeNavItemsRoot, "U", I18n.I.overview(), newOverviewController(DEF_OVERVIEW));  // $NON-NLS-0$
        addPmInstrumentData(typeNavItemsRoot, DEF_PM);
        if (FeatureFlags.Feature.LME_CHANGES_2014.isEnabled()) {
            addNavItemSpec(typeNavItemsRoot, "T", I18n.I.tickAndSaleAbbr(), newLmeTimesAndSalesController());  // $NON-NLS-0$
        } else {
            addNavItemSpec(typeNavItemsRoot, "T", I18n.I.tickAndSaleAbbr(), newTimesAndSalesController());  // $NON-NLS-0$
        }
        addChartcenter(typeNavItemsRoot, false);
        addChartAnalyser(typeNavItemsRoot);
        addNavItemSpec(typeNavItemsRoot, "RR", I18n.I.regulatoryReporting(), newOverviewController(DEF_REGULATORY));  // $NON-NLS$
    }

    @Override
    protected PdfOptionSpec getDefaultPdfOptionSpec(Map<String, String> map) {
        return new PdfOptionSpec("simplesecurityportrait.pdf", map, null); // $NON-NLS-0$
    }
}
