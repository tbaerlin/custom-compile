/**
 * PortraitMKController.java
 *
 * Created on Oct 6, 2008 4:41:09 PM
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.portrait;

import de.marketmaker.iview.mmgwt.mmweb.client.util.PdfOptionSpec;
import de.marketmaker.iview.mmgwt.mmweb.client.view.ContentContainer;

import java.util.Map;

/**
 * @author mloesch
 */
public class PortraitMKController extends AbstractPortraitController {
    public PortraitMKController(ContentContainer contentContainer) {
        super(contentContainer, "portrait_mk"); // $NON-NLS$
    }

    protected void initNavItems() {
        addChartcenter(false);
    }

    @Override
    protected PdfOptionSpec getDefaultPdfOptionSpec(Map<String, String> map) {
        return new PdfOptionSpec("simplesecurityportrait.pdf", map, null); // $NON-NLS-0$
    }
}
