/*
 * PageControllerAdapter.java
 *
 * Created on 24.07.2009 11:27:19
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client;

import de.marketmaker.iview.mmgwt.mmweb.client.events.PlaceChangeEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.util.PdfOptionSpec;

import java.util.Map;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class PageControllerAdapter implements PageController {
    @Override
    public void onPlaceChange(PlaceChangeEvent event) {
    }

    @Override
    public boolean isPrintable() {
        return true;
    }

    @Override
    public void activate() {
    }

    @Override
    public void deactivate() {
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
    public PdfOptionSpec getPdfOptionSpec() {
        return null;
    }

    @Override
    public void addPdfPageParameters(Map<String, String> mapParameters) {
    }

    @Override
    public String[] getAdditionalStyleSheetsForPrintHtml() {
        return null;
    }
}
