/*
 * GisPortal.java
 *
 * Created on 25.11.11
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.customer.dzwgz;

import java.util.Map;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;

import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.PageController;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PlaceChangeEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.util.PdfOptionSpec;
import de.marketmaker.iview.mmgwt.mmweb.client.view.ContentContainer;

/**
 * @author Ulrich Maurer
 */
public class GisPortal implements PageController {
    private final ContentContainer contentContainer;
    private final Command openGisPortalMethod;
    private final FlowPanel panel;

    public GisPortal(ContentContainer contentContainer, Command openGisPortalMethod) {
        this.contentContainer = contentContainer;

        this.openGisPortalMethod = openGisPortalMethod;

        this.panel = new FlowPanel();
        final Label lblToolHeader = new Label("GIS Portal"); // $NON-NLS$
        lblToolHeader.setStyleName("external-tool-header");
        this.panel.add(lblToolHeader);
        final FlowPanel pnlText = new FlowPanel();
        pnlText.setStyleName("external-tool-text");
        pnlText.add(new HTML(I18n.I.htmlOpenInWindow(1, I18n.I.gisPortalLinkDescription()) + "<br/><br/><br/>")); // $NON-NLS$
        final Anchor anchor = new Anchor("GIS Portal", "not used"); // $NON-NLS$
        anchor.getElement().removeAttribute("href");  // $NON-NLS$ //fix for IE 10, which reloads the whole app if the default GWT anchor href for scripting is used. Therefore removing the default href.
        anchor.addClickHandler(event -> this.openGisPortalMethod.execute());
        pnlText.add(anchor);
        this.panel.add(pnlText);
    }

    public void onPlaceChange(PlaceChangeEvent event) {
        this.contentContainer.setContent(this.panel);
        this.openGisPortalMethod.execute();
    }

    public void destroy() {
    }

    public void refresh() {
    }

    public boolean supportsHistory() {
        return false;
    }

    public String getPrintHtml() {
        return null;
    }

    @Override
    public boolean isPrintable() {
        return false;
    }

    public PdfOptionSpec getPdfOptionSpec() {
        return null;
    }

    public String[] getAdditionalStyleSheetsForPrintHtml() {
        return new String[0];
    }

    public void addPdfPageParameters(Map<String, String> mapParameters) {
    }

    public void activate() {
    }

    public void deactivate() {
    }
}
