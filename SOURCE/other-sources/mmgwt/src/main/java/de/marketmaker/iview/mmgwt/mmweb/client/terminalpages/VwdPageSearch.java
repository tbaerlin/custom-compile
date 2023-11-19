/*
 * VwdPageSearch.java
 *
 * Created on 06.08.2010 14:54:19
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.terminalpages;

import com.extjs.gxt.ui.client.util.Format;
import com.google.gwt.user.client.ui.HTML;
import de.marketmaker.iview.dmxml.MSCPageSearch;
import de.marketmaker.iview.dmxml.PageSummary;
import de.marketmaker.iview.mmgwt.mmweb.client.AbstractMainController;
import de.marketmaker.iview.mmgwt.mmweb.client.AbstractPageController;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.QuickSearchController;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PlaceChangeEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.history.HistoryToken;
import de.marketmaker.iview.mmgwt.mmweb.client.paging.PageLoader;
import de.marketmaker.iview.mmgwt.mmweb.client.paging.PagingFeature;
import de.marketmaker.iview.mmgwt.mmweb.client.table.AbstractRowMapper;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableDataModel;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;
import de.marketmaker.iview.mmgwt.mmweb.client.util.PlaceUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.view.ContentContainer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author swild
 */
public class VwdPageSearch extends AbstractPageController
        implements QuickSearchController, PageLoader, SearchToolbar.SearchToolbarPresenter {

    /**
     * requests for searching static text in pages use this key
     */
    public static final String TEXT_SEARCH_KEY = "P_VS"; // $NON-NLS-0$

    /**
     * This key is used for search requests for pages containing a given symbol
     * (instrument or quote id)
     */
    public static final String SYMBOL_SEARCH_KEY = "P_VS_S"; // $NON-NLS-0$

    /**
     * This key is used for search requests for pages containing a reference to
     * given page number
     */
    public static final String POINTER_SEARCH_KEY = "P_VS_P"; // $NON-NLS-0$

    private static final Map<String, String> blockParameter = new HashMap<>(5);

    private static final Map<String, String> viewHeading = new HashMap<>(5);

    static {
        blockParameter.put(TEXT_SEARCH_KEY, "text"); // $NON-NLS-0$
        blockParameter.put(SYMBOL_SEARCH_KEY, "usedSymbol"); // $NON-NLS-0$
        blockParameter.put(POINTER_SEARCH_KEY, "referencedPage"); // $NON-NLS-0$
        viewHeading.put(TEXT_SEARCH_KEY, " " + I18n.I.forQuery() + " \""); // $NON-NLS-0$ $NON-NLS-1$
        viewHeading.put(SYMBOL_SEARCH_KEY, " " + I18n.I.forQuery() + // $NON-NLS-0$
                " " + I18n.I.symbol() + " \""); // $NON-NLS$
        viewHeading.put(POINTER_SEARCH_KEY, " " + I18n.I.forQuery() + // $NON-NLS-0$
                " " + I18n.I.pagenumberReference() + " \""); // $NON-NLS$
    }

    public static final String BLOCK_NAME = "MSC_PageSearch"; // $NON-NLS-0$

    private final Map<String, String> lastQuery = new HashMap<>(5);

    public static final int DEFAULT_PAGE_SIZE = 30;

    protected DefaultTableDataModel defaultTableDataModel;

    private final PagingFeature pagingFeature;

    protected final DmxmlContext.Block<MSCPageSearch> block;

    protected VwdPageSearchView view;

    public VwdPageSearch(ContentContainer contentContainer) {
        this(contentContainer, DEFAULT_PAGE_SIZE);
    }

    protected VwdPageSearch(ContentContainer contentContainer, int pageSize) {
        super(contentContainer);
        this.block = this.context.addBlock(BLOCK_NAME);
        this.pagingFeature = new PagingFeature(this, this.block, pageSize);
        lastQuery.put(TEXT_SEARCH_KEY, ""); // $NON-NLS$
        lastQuery.put(SYMBOL_SEARCH_KEY, ""); // $NON-NLS$
        lastQuery.put(POINTER_SEARCH_KEY, ""); // $NON-NLS$
    }

    @Override
    public void quickSearch(String ctrlKey, String query) {
        if (StringUtil.hasText(query)) {
            PlaceUtil.goTo(StringUtil.joinTokens(ctrlKey, query));
        }
    }

    @Override
    public void onPlaceChange(PlaceChangeEvent event) {
        final HistoryToken historyToken = event.getHistoryToken();
        String query = historyToken.getByNameOrIndex("s", 1); // $NON-NLS$
        if (query == null) {
            query = getCurrentQuery(historyToken.get(0, TEXT_SEARCH_KEY));
            if (query == null) {
                getContentContainer().setContent(new HTML(I18n.I.noPageSelected()));
                return;
            }
        }
        else {
            reset(historyToken.getControllerId(), query);
        }
        AbstractMainController.INSTANCE.getView().setContentHeader(
                AbstractMainController.INSTANCE.getContentHeader(historyToken.getControllerId())
                + viewHeading.get(historyToken.get(0)) + Format.htmlEncode(query) + "\""); // $NON-NLS-0$
    }

    protected String getCurrentQuery(String key) {
        return this.lastQuery.get(key);
    }

    @Override
    protected void onResult() {
        this.pagingFeature.onResult();
        if (updateModel()) {
            updateView();
        }
    }

    /**
     * updates the model of found pages
     * @return true iff the search results shall be shown afterwards, i.e. false is returned if
     *         either nothing was changed or the result page was left because there was only one
     *         result which is shown immediately
     */
    protected final boolean updateModel() {
        if (this.block.getResult() == null) {
            this.defaultTableDataModel = null;
            return true;
        }

        final List<PageSummary> foundPages = getResult().getPagesummary();

        if (isSingleHit()) {
            // jump to page directly
            final PageSummary pageSummary = foundPages.get(0);
            PlaceUtil.goTo(StringUtil.joinTokens(VwdPageController.KEY, pageSummary.getPagenumber()));
            return false;
        }

        this.defaultTableDataModel = DefaultTableDataModel.create(foundPages,
                new AbstractRowMapper<PageSummary>() {
                    @Override
                    public Object[] mapRow(PageSummary page) {
                        return new Object[]{
                                page,
                                page
                        };
                    }
                });
        this.defaultTableDataModel.setMessage(I18n.I.noSuchPagesFound());
        return true;
    }

    public MSCPageSearch getResult() {
        return this.block.getResult();
    }

    protected void updateView() {
        if (this.view == null) {
            this.view = new VwdPageSearchView(this);
        }
        if (this.defaultTableDataModel != null) {
            this.view.show(this.defaultTableDataModel, getCurrentQuery(TEXT_SEARCH_KEY));
            getContentContainer().setContent(this.view);
        }
    }

    @Override
    public void reload() {
        refresh();
    }

    @Override
    public void doSearch(String text) {
        quickSearch(TEXT_SEARCH_KEY, text);
    }

    protected void reset(String key, String query) {
        this.block.removeAllParameters();
        this.block.setParameter(blockParameter.get(key), query);
        this.lastQuery.put(key, query);
        this.pagingFeature.resetPaging();
        refresh();
    }

    public PagingFeature getPagingFeature() {
        return pagingFeature;
    }

    private boolean isSingleHit() {
        try {
            return Integer.parseInt(getResult().getTotal()) == 1;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void prevPage() {
        // previous search result?
    }

    @Override
    public void nextPage() {
        // next what?
    }

    @Override
    public void bookmarkCurrentPage() {
        // bookmark a search result page?
    }

}
