/*
 * PortraitSTKController.java
 *
 * Created on 17.03.2008 17:07:24
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.portrait;

import java.util.Map;

import de.marketmaker.iview.dmxml.MSCQuoteMetadata;
import de.marketmaker.iview.mmgwt.mmweb.client.FeatureFlags;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.Selector;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.runtime.PermStr;
import de.marketmaker.iview.mmgwt.mmweb.client.util.PdfOptionSpec;
import de.marketmaker.iview.mmgwt.mmweb.client.view.ContentContainer;
import de.marketmaker.iview.mmgwt.mmweb.client.view.NavItemSpec;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class PortraitSTKController extends AbstractPortraitController implements MetadataAware {
    static final String DEF_OVERVIEW = "stk_overview"; // $NON-NLS$

    private static final String DEF_ANALYSIS = "stk_analysis"; // $NON-NLS$

    private static final String DEF_SCREENER = "stk_screener"; // $NON-NLS$

    private static final String DEF_DEPENDENT = "stk_dep"; // $NON-NLS$

    public static final String DEF_CONVENSYS = "stk_convensys"; // $NON-NLS$

    public static final String DEF_ILSOLE24ORE = "stk_ilsole24ore"; // $NON-NLS$

    private static final String DEF_ESTIMATES = "stk_estim"; // $NON-NLS$

    private static final String DEF_ESTIMATES_FACTSET = "stk_estim_factset"; // $NON-NLS$

    private static final String DEF_ESTIMATES_FACTSET_2015 = "stk_estim_factset_2015"; // $NON-NLS$

    private static final String DEF_RESEARCH = "stk_research"; // $NON-NLS$

    private static final String DEF_PM = "stk_pm"; // $NON-NLS$

    private MSCQuoteMetadata metadata = null;
    private NavItemSpec navItemOrderbook;
    private NavItemSpec navItemScreener;
    private NavItemSpec navItemConvensys;
    private NavItemSpec navItemPortraitIlSole;
    private NavItemSpec navItemEstimates = null;

    public PortraitSTKController(ContentContainer contentContainer) {
        super(contentContainer, "portrait_stk"); // $NON-NLS$
    }

    protected void initNavItems() {
        final NavItemSpec typeNavItemsRoot = addNavItemSpec(getNavItemSpecRoot(), "STK", I18n.I.stock()); // $NON-NLS$
        final NavItemSpec navItemSpecDep = addNavItemSpec(getNavItemSpecRoot(), "DEP", PermStr.LINKED_OBJECTS.value()); // $NON-NLS$
        addPmReports(getNavItemSpecRoot());

        addNavItemSpec(typeNavItemsRoot, "U", I18n.I.overview(), newOverviewController(DEF_OVERVIEW));  // $NON-NLS$

        addPmInstrumentData(typeNavItemsRoot, DEF_PM);

        addNavItemSpec(typeNavItemsRoot, "A", I18n.I.arbitrage(), newOverviewController(DEF_ARBITRAGE), false);  // $NON-NLS$

        addNavItemSpec(typeNavItemsRoot, "T", I18n.I.tickAndSaleAbbr(), newTimesAndSalesController());  // $NON-NLS$
        this.navItemOrderbook = addNavItemSpec(typeNavItemsRoot, "O", I18n.I.orderBook(), newOverviewController(DEF_ORDERBOOK)); // $NON-NLS$
        addNavItemSpec(typeNavItemsRoot, "K", I18n.I.ratios(), newOverviewController(DEF_RATIOS));  // $NON-NLS$
        if (!"true".equals(SessionData.INSTANCE.getGuiDefValue("portrait-without-news"))) { // $NON-NLS$
            addNavItemSpec(typeNavItemsRoot, "N", I18n.I.news(), newOverviewController(DEF_NEWS));  // $NON-NLS$
        }

        addNavItemSpec(typeNavItemsRoot, "Y", Selector.SMARTHOUSE_ANALYSES.isAllowed() || Selector.DPA_AFX_ANALYSES.isAllowed(), I18n.I.analyses(), newOverviewController(DEF_ANALYSIS));  // $NON-NLS$
        this.navItemScreener = addNavItemSpec(typeNavItemsRoot, "S", Selector.SCREENER, I18n.I.evaluation1(), newOverviewController(DEF_SCREENER)); // $NON-NLS$
        this.navItemConvensys = addNavItemSpec(typeNavItemsRoot, "P", I18n.I.company(), newOverviewController(DEF_CONVENSYS)); // $NON-NLS$
        if(Selector.IL_SOLE_COMPANY_DATA_XML.isAllowed()) {
            this.navItemPortraitIlSole = addNavItemSpec(typeNavItemsRoot, "PI", I18n.I.company() + " " + I18n.I.portraitIlSole24OreTabSuffix(), newOverviewController(DEF_ILSOLE24ORE)); // $NON-NLS$
        }
        if (Selector.isDzProfitEstimate()) {
                this.navItemEstimates = addNavItemSpec(typeNavItemsRoot, "R", I18n.I.estimations(), newOverviewController(DEF_ESTIMATES)); // $NON-NLS$
            }
        else if (Selector.PROFIT_ESTIMATES_AND_PER.isAllowed()) {
           String def =
                   FeatureFlags.Feature.VWD_RELEASE_2015.isEnabled()
                           ? DEF_ESTIMATES_FACTSET_2015
                           : DEF_ESTIMATES_FACTSET;
           this.navItemEstimates = addNavItemSpec(typeNavItemsRoot, "R", I18n.I.estimations(), newOverviewController(def)); // $NON-NLS$
        }
        addChartcenter(typeNavItemsRoot, false);
        addChartAnalyser(typeNavItemsRoot);
        addNavItemSpec(navItemSpecDep, "D", SessionData.isAsDesign() ? I18n.I.derivatives() : I18n.I.associates(), newOverviewController(DEF_DEPENDENT));  // $NON-NLS$
        addNavItemSpecValidDef(typeNavItemsRoot, DEF_RESEARCH, "F", "Research", newOverviewController(DEF_RESEARCH)); // $NON-NLS$
        if (Selector.DZ_WERTENTICKLUNGSRECHNER.isAllowed()) {
            addNavItemSpec(typeNavItemsRoot, "PFC", I18n.I.performanceCalculatorShort(), newOverviewController(DEF_PCALC));  // $NON-NLS-0$
        }
        addNavItemSpec(typeNavItemsRoot, "RR", I18n.I.regulatoryReporting(), newOverviewController(DEF_REGULATORY));  // $NON-NLS$
    }

    public boolean isMetadataNeeded() {
        return true;
    }

    public void onMetadataAvailable(MSCQuoteMetadata metadata) {
        this.metadata = metadata;
        getNavItemSelectionModel().setVisibility(this.navItemOrderbook, metadata.isOrderbookAvailable());
        getNavItemSelectionModel().setVisibility(this.navItemScreener, metadata.isScreenerAvailable());
        getNavItemSelectionModel().setVisibility(this.navItemConvensys, metadata.isConvensysIAvailable());
        getNavItemSelectionModel().setVisibility(this.navItemPortraitIlSole, metadata.isIlSole24OreAmfAvailable());
        getNavItemSelectionModel().setVisibility(this.navItemEstimates, metadata.isEstimatesAvailable());
    }

    @Override
    protected PdfOptionSpec getDefaultPdfOptionSpec(Map<String, String> map) {
        final PdfOptionSpec spec = new PdfOptionSpec("stockportrait.pdf", map, "stk_pdf_options"); // $NON-NLS$
        if (this.metadata != null) {
            spec.setDisabled("convensys", !this.metadata.isConvensysIAvailable()); // $NON-NLS$
            spec.setDisabled("convensysShares", !this.metadata.isConvensysIAvailable()); // $NON-NLS$
            spec.setDisabled("ilSole24OreAmf", !this.metadata.isIlSole24OreAmfAvailable()); // $NON-NLS$
            spec.setDisabled("screener", !this.metadata.isScreenerAvailable()); // $NON-NLS$
            spec.setDisabled("estimates", !this.metadata.isEstimatesAvailable()); // $NON-NLS$
        }
        return spec;
    }
}