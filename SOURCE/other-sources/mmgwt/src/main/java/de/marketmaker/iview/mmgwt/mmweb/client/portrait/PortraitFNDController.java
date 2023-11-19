/*
 * PortraitFNDController.java
 *
 * Created on 17.03.2008 17:07:24
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.portrait;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.marketmaker.iview.dmxml.MSCQuoteMetadata;
import de.marketmaker.iview.dmxml.WMStaticData;
import de.marketmaker.iview.dmxml.WMStringField;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.Selector;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;
import de.marketmaker.iview.mmgwt.mmweb.client.util.PdfOptionSpec;
import de.marketmaker.iview.mmgwt.mmweb.client.view.ContentContainer;
import de.marketmaker.iview.mmgwt.mmweb.client.view.NavItemSpec;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class PortraitFNDController extends AbstractPortraitController implements MetadataAware {
    public final static String GD198C = "GD198C"; // $NON-NLS-0$

    public final static String GD198F = "GD198F"; // $NON-NLS-0$

    public final static String GD873F = "GD873F"; // $NON-NLS-0$

    private static final String DEF_OVERVIEW = "fnd_overview"; // $NON-NLS-0$

    private static final String DEF_REPORTS_HEADER = "PriceTeaser(id=pt;row=0;col=0;colSpan=1)"; // $NON-NLS-0$
    private static final String DEF_REPORTS_HEADER_AS = "PriceTeaser(id=pt;row=0;col=0;colSpan=1;isObjectInfo=true)"; // $NON-NLS$

    private static final String DEF_REPORTS_STANDARD = ":Reports(id=fr;col=0;colSpan=1;blockName=FND_Reports)"; // $NON-NLS-0$

    private static final String DEF_REPORTS_STANDARD_FUNDINFO = ":Reports(id=fr;col=0;colSpan=1;blockName=FND_Reports;country=DE;language=de;escapeUrl=false)"; // $NON-NLS-0$

    private static final String DEF_REPORTS_KWT = ":FundprospectKWT(title=KWT Fondsprospekte;id=fpkwt;col=0;colSpan=1)"; // $NON-NLS-0$

    private static final String DEF_REPORTS_PIFS = ":Reports(id=fr;col=0;colSpan=1;blockName=GIS_Reports;title=Union PIFs)"; // $NON-NLS-0$

    public static final String DEF_STRUCT = "fnd_struct"; // $NON-NLS-0$

    protected static final String DEF_FND_RATIOS = "fnd_ratios"; // $NON-NLS-0$

    private static final String DEF_RESEARCH = "fnd_research"; // $NON-NLS-0$

    private static final String DEF_PM = "fnd_pm"; // $NON-NLS$

    private static final String DEF_DOCMAN = "fnd_docman"; // $NON-NLS$

    private NavItemSpec navItemOrderbook;

    public PortraitFNDController(ContentContainer contentContainer) {
        super(contentContainer, "portrait_fnd"); // $NON-NLS$
    }

    protected void initNavItems() {
        final NavItemSpec typeNavItemsRoot = addNavItemSpec(getNavItemSpecRoot(), "FND", I18n.I.funds()); // $NON-NLS$
        addPmReports(getNavItemSpecRoot());

        addNavItemSpec(typeNavItemsRoot, "U", I18n.I.overview(), newOverviewController(DEF_OVERVIEW));  // $NON-NLS-0$
        addPmInstrumentData(typeNavItemsRoot, DEF_PM);

        addNavItemSpec(typeNavItemsRoot, "A", I18n.I.arbitrage(), newOverviewController(DEF_ARBITRAGE), false);  // $NON-NLS-0$
        addNavItemSpec(typeNavItemsRoot, "T", I18n.I.tickAndSaleAbbr(), newTimesAndSalesController());  // $NON-NLS-0$
        this.navItemOrderbook = addNavItemSpec(typeNavItemsRoot, "O", I18n.I.orderBook(), newOverviewController(DEF_ORDERBOOK)); // $NON-NLS$
        addNavItemSpec(typeNavItemsRoot, "K", I18n.I.ratios(), newOverviewController(DEF_FND_RATIOS));  // $NON-NLS-0$
        if (Selector.isFunddataAllowed()) {
            addNavItemSpec(typeNavItemsRoot, "S", I18n.I.structure(), newOverviewController(DEF_STRUCT));  // $NON-NLS-0$
        }
        addReportsViewSpec(typeNavItemsRoot);

        if (SessionData.INSTANCE.hasGuiDef(DEF_DOCMAN)) {
            addNavItemSpec(typeNavItemsRoot, "D", I18n.I.docman(), newOverviewController(DEF_DOCMAN));   // $NON-NLS-0$
        }
        addChartcenter(typeNavItemsRoot, false);
        addChartAnalyser(typeNavItemsRoot);
        addNavItemSpecValidDef(typeNavItemsRoot, DEF_RESEARCH, "F", "Research", newOverviewController(DEF_RESEARCH)); // $NON-NLS$
        addNavItemSpec(typeNavItemsRoot, "PFC", Selector.DZ_WERTENTICKLUNGSRECHNER.isAllowed(), I18n.I.performanceCalculatorShort(), newOverviewController(DEF_PCALC));  // $NON-NLS-0$
        addNavItemSpec(typeNavItemsRoot, "RR", I18n.I.regulatoryReporting(), newOverviewController(DEF_REGULATORY));  // $NON-NLS$
    }

    private void addReportsViewSpec(NavItemSpec root) {
        final boolean standardAllowed = Selector.isStandardFndAllowed();
        final boolean pifsAllowed = Selector.UNION_PIFS.isAllowed();
        final boolean kwtAllowed = Selector.DZB_KWT_FUNCTION.isAllowed();

        if (standardAllowed || pifsAllowed || kwtAllowed) {
            final String standardSnippet = Selector.FUNDINFO_REPORTS.isAllowed() || Selector.FUNDINFO_FACTSHEET.isAllowed() ? DEF_REPORTS_STANDARD_FUNDINFO : DEF_REPORTS_STANDARD;

            final String def = (SessionData.isAsDesign() ? DEF_REPORTS_HEADER_AS : DEF_REPORTS_HEADER);

            addNavItemSpec(root, "R", I18n.I.fundBrochures(), newOverviewController(def  // $NON-NLS-0$
                    + (standardAllowed ? standardSnippet : "") // $NON-NLS-0$
                    + (kwtAllowed ? DEF_REPORTS_KWT : "") // $NON-NLS-0$
                    + (pifsAllowed ? DEF_REPORTS_PIFS : "") // $NON-NLS-0$
            ));
        }
    }

    @Override
    public boolean isMetadataNeeded() {
        return true;
    }

    public void onMetadataAvailable(MSCQuoteMetadata metadata) {
        getNavItemSelectionModel().setVisibility(this.navItemOrderbook, metadata.isOrderbookAvailable());
    }

    @Override
    protected PdfOptionSpec getDefaultPdfOptionSpec(Map<String, String> map) {
        return new PdfOptionSpec("fundportrait.pdf", map, null); // $NON-NLS-0$
    }

    public static Map<String, String> buildFndModel(DmxmlContext.Block<WMStaticData> blockWmStaticData) {
        final Map<String, String> result = new HashMap<>();

        if (blockWmStaticData != null && blockWmStaticData.isResponseOk()) {
            WMStaticData wmStaticData = blockWmStaticData.getResult();
            List<Serializable> parameters = wmStaticData.getStringOrDecimalOrDate();
            for (Serializable parameter : parameters) {
                if (parameter instanceof WMStringField) {
                    WMStringField stringParameter = (WMStringField) parameter;
                    if (GD198C.equals(stringParameter.getId())) {
                        result.put(GD198C, stringParameter.getKey());
                    }
                    else if (GD198F.equals(stringParameter.getId())) {
                        result.put(GD198F, stringParameter.getKey());
                    }
                    else if (GD873F.equals(stringParameter.getId())) {
                        result.put(GD873F, stringParameter.getValue());
                    }
                }
            }
        }
        return result;
    }

    public static boolean isSingleHedgeFond(Map<String, String> fndModel) {
        return "5006".equals(fndModel.get(GD198C)) && !"F304".equals(GD198F); // $NON-NLS$
    }
}
