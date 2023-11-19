/*
 * PortraitINDController.java
 *
 * Created on 17.03.2008 17:07:24
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.portrait;

import de.marketmaker.iview.dmxml.MSCQuoteMetadata;
import de.marketmaker.iview.mmgwt.mmweb.client.Selector;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.runtime.PermStr;
import de.marketmaker.iview.mmgwt.mmweb.client.util.PdfOptionSpec;
import de.marketmaker.iview.mmgwt.mmweb.client.view.ContentContainer;

import java.util.Map;

import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.view.NavItemSpec;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class PortraitINDController extends AbstractPortraitController implements MetadataAware {
    private static final String DEF_OVERVIEW = "ind_overview"; // $NON-NLS-0$
    private static final String DEF_OVERVIEW_WEB = "ind_overview_web"; // $NON-NLS-0$
    private static final String DEF_LIST = "ind_list"; // $NON-NLS-0$
    private static final String DEF_DEPENDENT = "ind_dependent"; // $NON-NLS-0$
    private static final String DEF_PM = "ind_pm"; // $NON-NLS$

    private NavItemSpec listItem;


    public PortraitINDController(ContentContainer contentContainer) {
        super(contentContainer, "portrait_ind"); // $NON-NLS$
    }

    protected void initNavItems() {
        final boolean isAsDesign = SessionData.isAsDesign();

        final NavItemSpec typeNavItemsRoot = addNavItemSpec(getNavItemSpecRoot(), "IND", I18n.I.indices()); // $NON-NLS$
        final NavItemSpec navItemSpecDep = isAsDesign ? addNavItemSpec(getNavItemSpecRoot(), "DEP", PermStr.LINKED_OBJECTS.value()) : typeNavItemsRoot; // $NON-NLS$
        addPmReports(getNavItemSpecRoot());
        String overviewKey =
                Selector.isDzProfitEstimate()
                        ? DEF_OVERVIEW_WEB
                        : DEF_OVERVIEW;

        addNavItemSpec(typeNavItemsRoot, "U", I18n.I.overview(), newOverviewController(overviewKey));  // $NON-NLS-0$
        addPmInstrumentData(typeNavItemsRoot, DEF_PM);
        this.listItem = addNavItemSpec(typeNavItemsRoot, "L", I18n.I.list(), newOverviewController(DEF_LIST));  // $NON-NLS-0$
        addNavItemSpec(typeNavItemsRoot, "A", I18n.I.arbitrage(), newOverviewController(DEF_ARBITRAGE_NO_VOLUME), false);  // $NON-NLS-0$
        addNavItemSpec(typeNavItemsRoot, "T", I18n.I.tickAndSaleAbbr(), newTimesAndSalesController());  // $NON-NLS-0$
        addNavItemSpec(typeNavItemsRoot, "K", I18n.I.ratios(), newOverviewController(DEF_RATIOS_NO_VOLUME));  // $NON-NLS-0$
        addNavItemSpec(typeNavItemsRoot, "N", I18n.I.news(), newOverviewController(DEF_NEWS));  // $NON-NLS-0$
        addChartcenter(typeNavItemsRoot, false);
        addChartAnalyser(typeNavItemsRoot);

        addNavItemSpec(navItemSpecDep, "D", isAsDesign ? I18n.I.derivatives() : I18n.I.associates(), newOverviewController(DEF_DEPENDENT));  // $NON-NLS$
        addNavItemSpec(typeNavItemsRoot, "RR", I18n.I.regulatoryReporting(), newOverviewController(DEF_REGULATORY));  // $NON-NLS$
    }

    @Override
    protected PdfOptionSpec getDefaultPdfOptionSpec(Map<String, String> map) {
        return new PdfOptionSpec("indexportrait.pdf", map, null); // $NON-NLS-0$
    }

    @Override
    public boolean isMetadataNeeded() {
        return true;
    }

    @Override
    public void onMetadataAvailable(MSCQuoteMetadata metadata) {
        getNavItemSelectionModel().setVisibility(this.listItem, metadata.isIndexWithConstituents());
    }
}
