/*
 * VwdPageController.java
 *
 * Created on 17.03.2008 17:07:24
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.terminalpages;

import java.util.HashMap;
import java.util.Map;

import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.iview.dmxml.MSCPageDisplay;
import de.marketmaker.iview.mmgwt.mmweb.client.FeatureFlags;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.Selector;
import de.marketmaker.iview.mmgwt.mmweb.client.customer.Customer;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PlaceChangeEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.util.PageUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.PdfOptionSpec;
import de.marketmaker.iview.mmgwt.mmweb.client.util.PlaceUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.view.ContentContainer;

import static de.marketmaker.iview.mmgwt.mmweb.client.FeatureFlags.Feature.VWD_RELEASE_2014;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class VwdPageController extends TerminalPages<MSCPageDisplay> {

    public static final String KEY = "P_V"; // $NON-NLS$

    public static final String LINK_TEXT = "#" + KEY + "/"; // $NON-NLS$

    public static final String DEF = "vwd_pages"; // $NON-NLS$

    public static final Selector SELECTOR = Selector.PAGES_VWD;

    private final String[] renderingProperties = createRenderingProperties();

    private final EmbeddedPageHistory pageHistory;

    public VwdPageController(ContentContainer contentContainer) {
        super(contentContainer, "MSC_PageDisplay", getDefaultPageId(), Mode.PRE); // $NON-NLS$
        setMinPageKey(1);
        this.pageHistory = hasHistory() ? new EmbeddedPageHistory(KEY, getDefaultPageId()) : null;
    }

    private boolean hasHistory() {
        return FeatureFlags.Feature.VWD_RELEASE_2015.isEnabled() && Customer.INSTANCE.isVwd();
    }

    private static String readConfigValue(String key, String fallback) {
        return SessionData.INSTANCE.getGuiDef(DEF).getString(key, fallback);
    }

    public static String[] createRenderingProperties() {
        return new String[] {
                "linkText=" + LINK_TEXT // $NON-NLS$
                , "usePriceQualityStyle=true" // $NON-NLS$
                , "typeDataClass=mm-page-data" // $NON-NLS$
                , "typeInverseClass=mm-page-inverse" // $NON-NLS$
                , "linkClass=mm-page-link" // $NON-NLS$
                , "fieldLinkClass=mm-page-hover" // $NON-NLS$
                , "dataObjectLinkPrefix=#M_S/" // $NON-NLS$
                , "dataObjectLinkFields=" + getLinkFields() // $NON-NLS$
        };
    }

    private static String getLinkFields() {
        return readConfigValue("linkFields", VWD_RELEASE_2014.isEnabled() ? "*" : "49 58 96 80"); // $NON-NLS$
    }

    @Override
    protected void createView() {
        super.createView();
        // fix the view's search label
        if (Selector.SEARCH_VWD_PAGES.isAllowed()) {
            view.setLabelText(I18n.I.pageOrSearchterm());
        } else {
            view.setLabelText(I18n.I.page());
        }
    }

    public static String getDefaultPageId() {
        return readConfigValue("defaultPageId", "1");  // $NON-NLS$
    }

    protected void prepareBlock(String page) {
        this.block.setParameter("pageId", page); // $NON-NLS-0$
        this.block.setParameters("renderingProperties", this.renderingProperties); // $NON-NLS$
    }

    @Override
    protected PageType getType() {
        return PageType.VWD;
    }

    @Override
    protected String getKey(String query) {
        return (query.matches("\\d+") || !Selector.SEARCH_VWD_PAGES.isAllowed()) ? KEY : VwdPageSearch.TEXT_SEARCH_KEY; // $NON-NLS$
    }

    protected String getPageText() {
        return PageUtil.toPageText(this.block.getResult());
    }

    protected boolean withPaging() {
        return true;
    }

    protected boolean withCurrency() {
        return false;
    }

    @Override
    protected boolean withReferencesCurrentSearch() {
        return false; // TODO: page search disabled
    }

    @Override
    protected EmbeddedPageHistory getPageHistory() {
        return this.pageHistory;
    }

    @Override
    protected void searchPagesReferencingCurrent() {
        PlaceUtil.goTo(StringUtil.joinTokens(VwdPageSearch.POINTER_SEARCH_KEY, this.lastPage));
    }

    @Override
    void nextPage() {
        if (this.block.isResponseOk() && this.block.getResult().getNextPage() != null) {
            final String nextPage = this.block.getResult().getNextPage();
            goToPage(nextPage);
        }
        else { // if no page is displayed currently, increment page number
            int current = Integer.parseInt(this.lastPage);
            ++current;
            if (!isMaxPage(current)) {
                goToPage(Integer.toString(current));
            }
        }
    }

    @Override
    void prevPage() {
        if (this.block.isResponseOk() && this.block.getResult().getPreviousPage() != null) {
            final String prevPage = this.block.getResult().getPreviousPage();
            goToPage(prevPage);
        }
        else { // if no page is displayed currently, increment page number
            int current = Integer.parseInt(this.lastPage);
            --current;
            if (!isMinPage(current)) {
                goToPage(Integer.toString(current));
            }
        }
    }

    void nextHistoryPage() {
        if (this.pageHistory.hasNext()) {
            final String pageKey = this.pageHistory.next();
            PlaceUtil.goTo(StringUtil.joinTokens(this.getKey(pageKey), pageKey, EmbeddedPageHistory.PAGE_HISTORY_TOKEN));
        }
    }

    void prevHistoryPage() {
        if (this.pageHistory.hasPrevious()) {
            final String pageKey = this.pageHistory.previous();
            PlaceUtil.goTo(StringUtil.joinTokens(this.getKey(pageKey), pageKey, EmbeddedPageHistory.PAGE_HISTORY_TOKEN));
        }
    }

    @Override
    protected void onResult() {
        if (this.block.isResponseOk() && this.block.getResult().getNextPage() == null) {
            // currently loading page is the last page
            Firebug.log("max page number: " + this.block.getParameterAsInt("pageId")); // $NON-NLS$
            setMaxPageKey(this.block.getParameterAsInt("pageId")); // $NON-NLS$
        }
        super.onResult();
    }

    @Override
    public PdfOptionSpec getPdfOptionSpec() {
        final Map<String, String> map = new HashMap<String, String>();
        addPdfPageParameters(map);
        map.put("pageId", this.block.getParameter("pageId")); // $NON-NLS$
        return new PdfOptionSpec("page.pdf", map, "pdf_options_format"); // $NON-NLS$
    }

    @Override
    public void onPlaceChange(PlaceChangeEvent event) {
        if (hasHistory()) {
            this.pageHistory.updatePageHistory(event.getHistoryToken());
        }
        super.onPlaceChange(event);
    }
}