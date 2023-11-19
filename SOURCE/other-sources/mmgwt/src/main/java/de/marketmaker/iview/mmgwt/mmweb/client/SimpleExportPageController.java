/*
 * SimpleExportPageController.java
 *
 * Created on 20.11.2012 15:31
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client;

import java.util.Map;

import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;

import de.marketmaker.iview.dmxml.MSCQuotelistWebQueryUrl;
import de.marketmaker.iview.dmxml.ResponseType;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PlaceChangeEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;
import de.marketmaker.iview.mmgwt.mmweb.client.util.PdfOptionSpec;
import de.marketmaker.iview.mmgwt.mmweb.client.view.ContentContainer;

/**
 * @author Markus Dick
 */
public class SimpleExportPageController extends AbstractPageController {
    private final ContentContainer contentContainer;

    private final FlowPanel mainPanel;

    private final Anchor webQueryAnchor;

    private final Panel webQueryPanel;

    private final DmxmlContext.Block<MSCQuotelistWebQueryUrl> webQueryUrlBlock;

    public SimpleExportPageController(ContentContainer contentContainer) {
        this.contentContainer = contentContainer;

        this.mainPanel = new FlowPanel();
        final Label toolHeaderLabel = new Label("Export"); // $NON-NLS$
        toolHeaderLabel.setStyleName("external-tool-header");
        this.mainPanel.add(toolHeaderLabel);

        this.webQueryPanel = new FlowPanel();
        this.webQueryPanel.setStyleName("external-tool-text");
        this.webQueryPanel.add(new HTML(I18n.I.exportAsWebQueryDescription() + "<br/><br/><br/>")); // $NON-NLS$

        this.webQueryAnchor = new Anchor(I18n.I.exportAsWebQueryUrl(), "", "_blank"); // $NON-NLS$
        this.webQueryPanel.add(this.webQueryAnchor);
        this.webQueryPanel.setVisible(false);
        this.mainPanel.add(webQueryPanel);

        this.webQueryUrlBlock = context.addBlock("MSC_QuotelistWebQueryUrl"); // $NON-NLS$
        final String listid = SessionData.INSTANCE.getGuiDef("list_export_web_query.listid").stringValue();// $NON-NLS$
        this.webQueryUrlBlock.setParameter("listid", listid); // $NON-NLS$
    }

    public void destroy() {
        context.removeBlock(webQueryUrlBlock);
        super.destroy();
    }

    public boolean supportsHistory() {
        return false;
    }

    public String getPrintHtml() {
        return null;
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

    @Override
    public void onFailure(Throwable throwable) {
        if (!this.webQueryUrlBlock.isResponseOk()) {
            this.webQueryPanel.setVisible(false);
        }

        super.onFailure(throwable);
    }

    @Override
    public void onSuccess(ResponseType responseType) {
        if (this.webQueryUrlBlock.isResponseOk()) {
            this.webQueryAnchor.setHref(this.webQueryUrlBlock.getResult().getWebQueryUrl());
            this.webQueryPanel.setVisible(true);
        }

        super.onSuccess(responseType);
    }

    public void onPlaceChange(PlaceChangeEvent event) {
        this.webQueryUrlBlock.issueRequest(this);
        this.contentContainer.setContent(this.mainPanel);
    }
}
