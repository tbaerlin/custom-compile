/*
 * PortraitPEController.java
 *
 * Created on 17.03.2008 17:07:24
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.portrait;

import de.marketmaker.iview.mmgwt.mmweb.client.Selector;
import de.marketmaker.iview.mmgwt.mmweb.client.util.PdfOptionSpec;
import de.marketmaker.iview.mmgwt.mmweb.client.util.PlaceUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.view.ContentContainer;

import java.util.Map;

import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.view.NavItemSpec;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class PortraitPEController extends AbstractPortraitController {
    private static final String DEF_OVERVIEW = "ind_overview"; // $NON-NLS-0$
    private static final String DEF_OVERVIEW_WEB = "ind_overview_web"; // $NON-NLS-0$
    private static final String DEF_CHARTCENTER_PE = "wp_chartcenter_pe"; // $NON-NLS-0$
    private NavItemSpec navItemOverview;

    public PortraitPEController(ContentContainer contentContainer) {
        super(contentContainer, "portrait_pe"); // $NON-NLS$
    }

    protected void initNavItems() {
        String overviewKey =
                Selector.isDzProfitEstimate()
                        ? DEF_OVERVIEW_WEB
                        : DEF_OVERVIEW;
        this.navItemOverview = addNavItemSpec("U", I18n.I.toIndex(), newOverviewController(overviewKey)); // $NON-NLS$
        addChartcenter(DEF_CHARTCENTER_PE);
    }

    @Override
    protected void goTo(NavItemSpec navItemSpec) {
        if (navItemSpec == this.navItemOverview) {
            PlaceUtil.goTo(StringUtil.joinTokens("P_IND", getSymbol(), navItemSpec.getId())); // $NON-NLS$
        }
        else {
            super.goTo(navItemSpec);
        }
    }

    @Override
    protected PdfOptionSpec getDefaultPdfOptionSpec(Map<String, String> map) {
        return null;
    }
}
