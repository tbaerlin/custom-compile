/*
 * SimpleResetPageController.java
 *
 * Created on 30.01.2015 14:26
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.as;

import de.marketmaker.iview.mmgwt.mmweb.client.PageController;
import de.marketmaker.iview.mmgwt.mmweb.client.util.PdfOptionSpec;
import de.marketmaker.iview.mmgwt.mmweb.client.view.ContentContainer;

import java.util.Map;

/**
 * @author mdick
 */
public abstract class SimpleResetPageController implements PageController {
    private final ContentContainer contentContainer;

    protected abstract void reset();

    public SimpleResetPageController(ContentContainer contentContainer) {
        this.contentContainer = contentContainer;
    }

    protected ContentContainer getContentContainer() {
        return this.contentContainer;
    }

    public boolean isPrintable() {
        return false;
    }

    public String getPrintHtml() {
        return "";
    }

    public boolean supportsHistory() {
        return true;
    }

    public void activate() {
        reset();
    }

    public void deactivate() {
        reset();
    }

    public void destroy() {
        reset();
    }

    public void refresh() {
        reset();
    }

    public PdfOptionSpec getPdfOptionSpec() {
        return null;
    }

    public String[] getAdditionalStyleSheetsForPrintHtml() {
        return new String[0];
    }

    public void addPdfPageParameters(Map<String, String> mapParameters) {

    }
}
