/**
 * PortraitZNSController.java
 *
 * Created on Oct 6, 2008 4:41:09 PM
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.portrait;

import de.marketmaker.iview.mmgwt.mmweb.client.util.PdfOptionSpec;
import de.marketmaker.iview.mmgwt.mmweb.client.view.ContentContainer;

import java.util.Map;

import de.marketmaker.iview.mmgwt.mmweb.client.I18n;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class PortraitZNSController extends AbstractPortraitController {
    private static final String DEF_PDF = "zns_pdf"; // $NON-NLS-0$

    public PortraitZNSController(ContentContainer contentContainer) {
        super(contentContainer, "portrait_zns"); // $NON-NLS$
    }

    protected void initNavItems() {
        addChartcenter(false);
        addNavItemSpec("T", I18n.I.tickAndSaleAbbr(), newTimesAndSalesController());  // $NON-NLS-0$
//        addViewSpec("F", "PDF", newOverviewController(DEF_PDF));
    }

    @Override
    protected PdfOptionSpec getDefaultPdfOptionSpec(Map<String, String> map) {
        return new PdfOptionSpec("simplesecurityportrait.pdf", map, null); // $NON-NLS-0$
    }
}
