/*
 * ContactFormController.java
 *
 * Created on 26.05.2008 10:38:01
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */

package de.marketmaker.iview.mmgwt.mmweb.client;

import com.extjs.gxt.ui.client.event.FormEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.util.PdfOptionSpec;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.view.ContentContainer;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PlaceChangeEvent;
import de.marketmaker.iview.mmgwt.mmweb.server.ContactController;

import java.util.Map;

/**
 * @author Ulrich Maurer
 * @author Michael Lösch
 * @author Michael Wohlfart
 */
public class ContactFormController implements PageController {
    private final ContentContainer contentContainer;
    private ContactFormView view;

    public ContactFormController(ContentContainer contentContainer) {
        this.contentContainer = contentContainer;
    }

    public void onPlaceChange(PlaceChangeEvent event) {
        if (this.view == null) {
            this.view = new ContactFormView(this);
        }
        this.contentContainer.setContent(this.view);
        this.view.showMailDialog();
        if (AbstractMainController.INSTANCE != null) {
            AbstractMainController.INSTANCE.getView().setContentHeader(I18n.I.gisContactForm());
        }
    }

    public void activate() {
    }

    public void deactivate() {
    }

    public boolean supportsHistory() {
        return true;
    }

    public void refresh() {
        // empty
    }

    public void afterSubmit(FormEvent result) {
        final String content = result.getResultHtml();
        if (content.contains(ContactController.SUBMIT_OK)) {
            this.view.showOKDialog();
        } else if (!StringUtil.hasText(content)) {
            this.view.showMailDialog();
        } else {
            this.view.showErrorDialog();
        }
    }

    public void destroy() {
    }

    public String getPrintHtml() {
        return this.view.getElement().getInnerHTML();
    }

    @Override
    public boolean isPrintable() {
        return true;
    }

    public PdfOptionSpec getPdfOptionSpec() {
        return null;
    }

    public void addPdfPageParameters(Map<String, String> mapParameters) {
    }

    public String[] getAdditionalStyleSheetsForPrintHtml() {
        return null;
    }

}
