/*
 * TerminalPages.java
 *
 * Created on 08.04.2008 16:55:58
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.terminalpages;

import com.google.gwt.event.shared.HandlerRegistration;

import de.marketmaker.iview.dmxml.BlockType;
import de.marketmaker.iview.mmgwt.mmweb.client.AbstractPageController;
import de.marketmaker.iview.mmgwt.mmweb.client.FeatureFlags;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.events.ConfigChangedEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.events.ConfigChangedHandler;
import de.marketmaker.iview.mmgwt.mmweb.client.events.EventBusRegistry;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PlaceChangeEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.history.HistoryToken;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;
import de.marketmaker.iview.mmgwt.mmweb.client.util.PlaceUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.view.ContentContainer;
import de.marketmaker.iview.mmgwt.mmweb.client.workspace.FavouriteItemsStores;
import de.marketmaker.iview.mmgwt.mmweb.client.workspace.FavouritePageItemsStore;
import de.marketmaker.iview.mmgwt.mmweb.client.workspace.PagesWorkspace;

import de.marketmaker.iview.mmgwt.mmweb.client.I18n;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public abstract class TerminalPages<V extends BlockType> extends AbstractPageController implements
        ConfigChangedHandler {

    private final HandlerRegistration configChangedHandlerRegistration;

    public enum Mode {
        TEXT, HTML, PRE, IFRAME
    }

    protected String lastPage;

    protected String defaultPage;

    protected final DmxmlContext.Block<V> block;

    protected TerminalPagesView view;

    private Mode mode;

    private Integer minPageKey = null;

    private Integer maxPageKey = null;

    /**
     * @return the id of the controller, i.e. the part of the URL between
     * {@code #} and {@code /pagenumber}.
     */
    protected abstract String getKey(String query);

    protected TerminalPages(ContentContainer contentContainer, String blockName, String defaultPage,
            Mode mode) {
        super(contentContainer);
        this.block = this.context.addBlock(blockName);
        this.defaultPage = defaultPage;
        this.lastPage = defaultPage;
        this.mode = mode;

        if(SessionData.isAsDesign()) {
            this.configChangedHandlerRegistration = EventBusRegistry.get()
                    .addHandler(ConfigChangedEvent.getType(), this);
        }
        else {
            this.configChangedHandlerRegistration = null;
        }
    }

    @Override
    public void onConfigChange(ConfigChangedEvent event) {
        if(FavouriteItemsStores.isFavouriteItemsConfigChangedEvent(event)) {
            if(this.view != null) {
                this.view.updateBookmarkButton(this.lastPage);
            }
        }
    }

    @Override
    public void destroy() {
        if(FeatureFlags.Feature.ICE_DESIGN.isEnabled() && SessionData.isAsDesign()) {
            this.configChangedHandlerRegistration.removeHandler();
        }
        super.destroy();
    }

    @Override
    public void onPlaceChange(PlaceChangeEvent event) {
        final HistoryToken historyToken = event.getHistoryToken();
        displayPage(historyToken.getByNameOrIndexFromAll("s", 1, this.defaultPage)); // $NON-NLS$
    }

    /**
     * go to next page
     */
    void nextPage() {
        // subclasses may override
    }

    /**
     * go to previous page
     */
    void prevPage() {
        // subclasses may override
    }

    void nextHistoryPage() {
        // subclasses may override
    }

    void prevHistoryPage() {
        // subclasses may override
    }

    /**
     * Called when the corresponding button was clicked, subclasses can override this.
     */
    protected void searchPagesReferencingCurrent() {
    }

    /**
     * Issue a PlaceChange event for the page with page number {@code pageKey}
     */
    protected void goToPage(String pageKey) {
        PlaceUtil.goTo(StringUtil.joinTokens(this.getKey(pageKey), pageKey));
    }

    /**
     * This method loads the page with number {@code pageKey} into the view.
     */
    void displayPage(String pageKey) {
        prepareBlock(pageKey);
        refresh();

        this.lastPage = pageKey;
    }

    @Override
    protected void onResult() {
        if (this.view == null) {
            createView();
        }
        getContentContainer().setContent(this.view);

        if (!this.block.isResponseOk()) {
            this.view.update(I18n.I.pageCannotBeDisplayed(), Mode.TEXT, null);
            return;
        }

        final String text = getPageText();
        if (text == null || "".equals(text.trim())) { // $NON-NLS-0$
            this.view.update(I18n.I.pageWithoutContent(), Mode.TEXT, null);
            return;
        }

        this.view.update(text, this.mode, this.lastPage);
    }

    protected abstract String getPageText();

    protected abstract void prepareBlock(String page);

    protected abstract PageType getType();

    protected abstract boolean withPaging();

    protected abstract boolean withCurrency();

    protected abstract boolean withReferencesCurrentSearch();

    protected abstract EmbeddedPageHistory getPageHistory();

    public void bookmarkCurrentPage() {
        if (SessionData.isAsDesign()) {
            FavouriteItemsStores.ifPresent(FavouritePageItemsStore.class, store ->
                    store.addItem(new FavouritePageItemsStore.Page(this.lastPage, getType())));
        }
        else {
            PagesWorkspace.INSTANCE.addPage(this.lastPage, getType());
        }
    }

    String getAdditionalStylesheet() {
        return null;
    }

    protected void createView() {
        this.view = new TerminalPagesView(this, withPaging(), withCurrency(),
                withReferencesCurrentSearch(), getPageHistory());
    }

    protected void setMinPageKey(Integer minPageKey) {
        this.minPageKey = minPageKey;
    }

    protected void setMaxPageKey(Integer maxPageKey) {
        this.maxPageKey = maxPageKey;
    }

    boolean isMinPage(int pageNum) {
        return this.minPageKey != null && pageNum <= this.minPageKey;
    }

    boolean isMaxPage(int pageNum) {
        return this.maxPageKey != null && pageNum >= this.maxPageKey;
    }

    public void setCurrency(String currency) {
        this.block.setParameter("currency", currency); // $NON-NLS-0$
        refresh();
    }

    public String getLastPage() {
        return lastPage;
    }
}
