/*
 * PortraitCERController.java
 *
 * Created on 17.03.2008 17:07:24
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.portrait;

import de.marketmaker.iview.dmxml.MSCQuoteMetadata;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.Selector;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.StaticDataCER;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.StaticDataCERSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.StaticDataCERTabConfig;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.StaticDataCERTypeTabConfig;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableColumnAndDataConfig;
import de.marketmaker.iview.mmgwt.mmweb.client.util.PdfOptionSpec;
import de.marketmaker.iview.mmgwt.mmweb.client.view.ContentContainer;
import de.marketmaker.iview.mmgwt.mmweb.client.view.NavItemSpec;

import java.util.Map;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class PortraitCERController extends AbstractPortraitController implements MetadataAware {

    private NavItemSpec navItemEdg;
    private NavItemSpec navItemBasket;
    private NavItemSpec navItemOrderbook;
    private NavItemSpec navItemReports = null;

    private class CEROverviewController extends PortraitOverviewController {
        private CEROverviewController(ContentContainer contentContainer, String def) {
            super(contentContainer, def);
        }

        protected void initDelegate() {
            super.initDelegate();
            setConfig("sd", StaticDataCERTabConfig.INSTANCE); // $NON-NLS-0$
            setConfig("sd2", StaticDataCERTypeTabConfig.INSTANCE); // $NON-NLS-0$
        }

        private void setConfig(final String key,
                final TableColumnAndDataConfig<StaticDataCER> value) {
            final StaticDataCERSnippet cer = getSnippet(key);
            if (cer != null) {
                cer.setConfig(value);
            }
        }
    }

    private static final String DEF_OVERVIEW = "cer_overview"; // $NON-NLS-0$

    private static final String DEF_REPORTS = "cer_reports"; // $NON-NLS-0$

    private static final String DEF_RESEARCH = "cer_research"; // $NON-NLS-0$

    private static final String DEF_CER_EDG = "cer_edg"; // $NON-NLS-0$

    private static final String DEF_CER_BASKET = "cer_basket"; // $NON-NLS-0$

    private static final String DEF_PM = "cer_pm"; // $NON-NLS$

    public PortraitCERController(ContentContainer contentContainer) {
        super(contentContainer, "portrait_cer"); // $NON-NLS$
    }

    protected void initNavItems() {
        final NavItemSpec typeNavItemsRoot = addNavItemSpec(getNavItemSpecRoot(), "CER", I18n.I.certificates()); // $NON-NLS$
        addPmReports(getNavItemSpecRoot());

        addNavItemSpec(typeNavItemsRoot, "U", I18n.I.overview(), new CEROverviewController(this.getInnerContainer(), DEF_OVERVIEW));  // $NON-NLS-0$
        addPmInstrumentData(typeNavItemsRoot, DEF_PM);
        this.navItemBasket = addNavItemSpec(typeNavItemsRoot, "B", I18n.I.certBasket(), newOverviewController(DEF_CER_BASKET)); // $NON-NLS$
        addNavItemSpec(typeNavItemsRoot, "A", I18n.I.arbitrage(), newOverviewController(DEF_ARBITRAGE_NO_VOLUME), false);  // $NON-NLS-0$
        addNavItemSpec(typeNavItemsRoot, "T", I18n.I.tickAndSaleAbbr(), newTimesAndSalesController());  // $NON-NLS-0$
        this.navItemOrderbook = addNavItemSpec(typeNavItemsRoot, "O", I18n.I.orderBook(), newOverviewController(DEF_ORDERBOOK)); // $NON-NLS$
        addNavItemSpec(typeNavItemsRoot, "K", I18n.I.ratios(), newOverviewController(DEF_RATIOS));  // $NON-NLS-0$
        this.navItemEdg = addNavItemSpec(typeNavItemsRoot, "EDG", Selector.EDG_RATING, "EDG", newOverviewController(DEF_CER_EDG)); // $NON-NLS$
        if (!"true".equals(SessionData.INSTANCE.getGuiDefValue("portrait-without-news"))) { // $NON-NLS-0$ $NON-NLS-1$
            addNavItemSpec(typeNavItemsRoot, "N", I18n.I.news(), newOverviewController(DEF_NEWS_UNDERLYING));  // $NON-NLS-0$
        }
        if (Selector.isReportsAllowed()) {
            this.navItemReports = addNavItemSpec(typeNavItemsRoot, "P", I18n.I.certificateBrochures(), newOverviewController(DEF_REPORTS)); // $NON-NLS$
        }
        addChartcenter(typeNavItemsRoot, true);
        addChartAnalyser(typeNavItemsRoot);
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
        getNavItemSelectionModel().setVisibility(this.navItemEdg, metadata.isEdgAvailable());
        getNavItemSelectionModel().setVisibility(this.navItemBasket, (metadata.getUnderlying() != null && metadata.getUnderlying().size() > 1));
        getNavItemSelectionModel().setVisibility(this.navItemOrderbook, metadata.isOrderbookAvailable());
        if (this.navItemReports != null) {
            getNavItemSelectionModel().setVisibility(this.navItemReports, metadata.isGisCerReport() || metadata.isStockselectionCerReport());
        }
    }

    @Override
    protected PdfOptionSpec getDefaultPdfOptionSpec(Map<String, String> map) {
        return new PdfOptionSpec("certificateportrait.pdf", map, "cer_pdf_options"); // $NON-NLS-0$ $NON-NLS-1$
    }
}
