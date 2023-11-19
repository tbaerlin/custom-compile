/*
 * PageController.java
 *
 * Created on 17.03.2008 17:05:00
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client;

import de.marketmaker.iview.mmgwt.mmweb.client.events.PlaceChangeHandler;
import de.marketmaker.iview.mmgwt.mmweb.client.util.PdfOptionSpec;

import java.util.Map;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public interface PageController extends Activatable, PlaceChangeHandler {
    void destroy();

    void refresh();

    /**
     * Should return true iff invoking this controller should be done using the browser's
     * history
     * @return true iff history should be used
     */
    boolean supportsHistory();

    String getPrintHtml();

    boolean isPrintable();

    PdfOptionSpec getPdfOptionSpec();

    String[] getAdditionalStyleSheetsForPrintHtml();

    void addPdfPageParameters(Map<String, String> mapParameters);
}
