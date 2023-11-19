/*
 * PortraitBNDController.java
 *
 * Created on 17.03.2008 17:07:24
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.portrait;

import de.marketmaker.iview.dmxml.MSCQuoteMetadata;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.Selector;
import de.marketmaker.iview.mmgwt.mmweb.client.util.FlashCheck;
import de.marketmaker.iview.mmgwt.mmweb.client.util.PdfOptionSpec;
import de.marketmaker.iview.mmgwt.mmweb.client.view.ContentContainer;
import de.marketmaker.iview.mmgwt.mmweb.client.view.NavItemSpec;

import java.util.Map;
import java.util.Optional;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class PortraitBNDController extends AbstractPortraitController implements MetadataAware {
    private static final String DEF_OVERVIEW = "bnd_overview"; // $NON-NLS-0$

    private static final String DEF_REPORTS = "bnd_reports"; // $NON-NLS-0$

    private static final String DEF_RESEARCH = "bnd_research"; // $NON-NLS-0$

    protected static final String DEF_ANALYSER = "bnd_analyser"; // $NON-NLS-0$

    private static final String DEF_PM = "bnd_pm"; // $NON-NLS$

    private NavItemSpec navItemOrderbook;

    private Optional<NavItemSpec> navItemReports = Optional.empty();

    public PortraitBNDController(ContentContainer contentContainer) {
        super(contentContainer, "portrait_bnd"); // $NON-NLS$
    }

    protected void initNavItems() {
        final NavItemSpec typeNavItemsRoot = addNavItemSpec(getNavItemSpecRoot(), "BND", I18n.I.bonds()); // $NON-NLS$
        addPmReports(getNavItemSpecRoot());

        addNavItemSpec(typeNavItemsRoot, "U", I18n.I.overview(), newOverviewController(DEF_OVERVIEW));  // $NON-NLS-0$

        addPmInstrumentData(typeNavItemsRoot, DEF_PM);

        addNavItemSpec(typeNavItemsRoot, "A", I18n.I.arbitrage(), newOverviewController(DEF_ARBITRAGE), false);  // $NON-NLS-0$
        addNavItemSpec(typeNavItemsRoot, "T", I18n.I.tickAndSaleAbbr(), newTimesAndSalesController());  // $NON-NLS-0$
        this.navItemOrderbook = addNavItemSpec(typeNavItemsRoot, "O", I18n.I.orderBook(), newOverviewController(DEF_ORDERBOOK)); // $NON-NLS$
        addNavItemSpec(typeNavItemsRoot, "K", I18n.I.ratios(), newOverviewController(DEF_RATIOS));  // $NON-NLS-0$
        this.navItemReports = addNavItemSpec(typeNavItemsRoot, "P", isReportsAllowed(), I18n.I.bondBrochures(), newOverviewController(DEF_REPORTS)); // $NON-NLS$
        addChartcenter(typeNavItemsRoot, false);
        addChartAnalyser(typeNavItemsRoot);
        addNavItemSpec(typeNavItemsRoot, "G", FlashCheck.isFlashAvailable() && Selector.BOND_CALCULATOR.isAllowed(), I18n.I.bondCalculator(), newOverviewController(DEF_ANALYSER));  // $NON-NLS-0$
        addNavItemSpecValidDef(typeNavItemsRoot, DEF_RESEARCH, "F", "Research", newOverviewController(DEF_RESEARCH)); // $NON-NLS$
        addNavItemSpec(typeNavItemsRoot, "PFC", Selector.DZ_WERTENTICKLUNGSRECHNER.isAllowed(), I18n.I.performanceCalculatorShort(), newOverviewController(DEF_PCALC));  // $NON-NLS-0$
        addNavItemSpec(typeNavItemsRoot, "RR", I18n.I.regulatoryReporting(), newOverviewController(DEF_REGULATORY));  // $NON-NLS$
    }

    private boolean isReportsAllowed() {
        return Selector.DZBANK_CERTIFICATE_REPORTS.isAllowed();
    }

    public boolean isMetadataNeeded() {
        return true;
    }

    public void onMetadataAvailable(MSCQuoteMetadata metadata) {
        getNavItemSelectionModel().setVisibility(this.navItemOrderbook, metadata.isOrderbookAvailable());
        this.navItemReports.ifPresent(navItemSpec -> getNavItemSelectionModel().setVisibility(navItemSpec, metadata.isGisBndReport() || metadata.isStockselectionCerReport()));
    }

    @Override
    protected PdfOptionSpec getDefaultPdfOptionSpec(Map<String, String> map) {
        return new PdfOptionSpec("bondportrait.pdf", map, null); // $NON-NLS-0$
    }
}
