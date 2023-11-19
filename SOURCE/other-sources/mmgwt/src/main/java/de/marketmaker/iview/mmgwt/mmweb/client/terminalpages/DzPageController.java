/*
 * VwdPageController.java
 *
 * Created on 17.03.2008 17:07:24
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.terminalpages;

import de.marketmaker.iview.dmxml.MSCGisPages;
import de.marketmaker.iview.mmgwt.mmweb.client.FeatureFlags;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.Selector;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.view.ContentContainer;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class DzPageController extends TerminalPages<MSCGisPages> {

    public static final String KEY = "P_D"; // $NON-NLS$

    public static final String LINK_TEXT = "#" + KEY + "/"; // $NON-NLS

    public static final Selector SELECTOR = Selector.PAGES_DZBANK;

    public DzPageController(ContentContainer contentContainer) {
        super(contentContainer, "MSC_GisPages", DzPageController.getDefaultPageId(), Mode.IFRAME); // $NON-NLS$
        this.block.setParameter("type", "dz"); // $NON-NLS$
    }

    protected void prepareBlock(String page) {
        this.block.setParameter("pagenumber", page); // $NON-NLS$
    }

    public static String getDefaultPageId() {
        final String pageId = SessionData.INSTANCE.getGuiDefValue("defaultPageId"); // $NON-NLS$
        return pageId == null ? "A100" : pageId; // $NON-NLS$
    }

    protected PageType getType() {
        return PageType.DZBANK;
    }

    protected boolean withPaging() {
        return false;
    }

    protected boolean withCurrency() {
        return false;
    }

    @Override
    protected EmbeddedPageHistory getPageHistory() {
        return null;
    }

    @Override
    protected void createView() {
        super.createView();
        if (FeatureFlags.Feature.DZ_RELEASE_2015.isEnabled()) {
            view.setLabelText(I18n.I.page());
        }
    }
    @Override
    protected boolean withReferencesCurrentSearch() {
        return false;
    }

    @Override
    protected String getKey(String query) {
        return KEY;
    }

    protected String getPageText() {
        final MSCGisPages display = this.block.getResult();
        return display.getPage();
    }

    public String getPrintHtml() {
        return getPageText();
    }

    public String[] getAdditionalStyleSheetsForPrintHtml() {
        return new String[]{getAdditionalStylesheet()};
    }

    @Override
    String getAdditionalStylesheet() {
        return "./style/dzorgstyles.css"; // $NON-NLS$
    }
}
