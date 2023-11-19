/*
 * AsHelpController.java
 *
 * Created on 09.07.13 13:38
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.as;

import com.google.gwt.user.client.Window;
import de.marketmaker.iview.mmgwt.mmweb.client.PageController;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PlaceChangeEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.util.PdfOptionSpec;

import java.util.Map;

/**
 * @author Markus Dick
 */
public class AsHelpController implements PageController {
    @Override
    public void onPlaceChange(PlaceChangeEvent event) {
        Window.open("as/nethelp/index.html", "vwdAdvisorySolutionNetHelp", ""); //$NON-NLS$
    }

    @Override
    public void destroy() {
    }

    @Override
    public void refresh() {
    }

    @Override
    public boolean supportsHistory() {
        return false;
    }

    @Override
    public String getPrintHtml() {
        return null;
    }

    @Override
    public boolean isPrintable() {
        return false;
    }

    @Override
    public PdfOptionSpec getPdfOptionSpec() {
        return null;
    }

    @Override
    public String[] getAdditionalStyleSheetsForPrintHtml() {
        return new String[0];
    }

    @Override
    public void addPdfPageParameters(Map<String, String> mapParameters) {
    }

    @Override
    public void activate() {
    }

    @Override
    public void deactivate() {
    }
}
