/*
 * TerminalPagesView.java
 *
 * Created on 29.07.2008 15:22:49
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.terminalpages;

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.google.gwt.dom.client.AnchorElement;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.EventListener;
import com.google.gwt.user.client.ui.Frame;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextBox;

import de.marketmaker.itools.gwtutil.client.widgets.Button;
import de.marketmaker.itools.gwtutil.client.widgets.menu.Menu;
import de.marketmaker.itools.gwtutil.client.widgets.menu.MenuItem;
import de.marketmaker.itools.gwtutil.client.widgets.menu.SelectButton;
import de.marketmaker.iview.mmgwt.mmweb.client.AbstractMainController;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DOMUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.PlaceUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.view.FloatingToolbar;
import de.marketmaker.iview.mmgwt.mmweb.client.view.HTMLWithLinks;
import de.marketmaker.iview.mmgwt.mmweb.client.workspace.FavouriteItemsStores;
import de.marketmaker.iview.mmgwt.mmweb.client.workspace.FavouritePageItemsStore;

import static de.marketmaker.iview.mmgwt.mmweb.client.terminalpages.TerminalPages.Mode;
import static de.marketmaker.iview.mmgwt.mmweb.client.view.FloatingToolbar.ToolbarHeight.FOR_ICON_SIZE_S;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class TerminalPagesView extends ContentPanel {

    private final TerminalPages terminalPages;  // controller

    private final EmbeddedPageHistory pageHistory;

    private final Label searchLabel;

    private final TextBox searchBox;

    private Button bookmarkButton;

    protected Button prevButton;

    protected Button nextButton;

    protected Button prevHistoryButton;

    protected Button nextHistoryButton;

    protected Button pagesReferencingCurrent;

    private final SimplePanel contentWrapper = new SimplePanel();

    public static final EventListener PAGE_LINK_LISTENER = event -> {
        if (DOM.eventGetType(event) != Event.ONCLICK) {
            return;
        }

        final Element target = DOM.eventGetTarget(event);
        if (target == null || !target.hasTagName("a")) { // $NON-NLS$
            return;
        }
        final AnchorElement anchor = AnchorElement.as(target);
        if (!"".equals(anchor.getTarget())) { // $NON-NLS-0$
            return;
        }

        final int p = anchor.getHref().indexOf("#"); // $NON-NLS-0$
        if (p == -1) {
            return;
        }

        PlaceUtil.goTo(anchor.getHref().substring(p + 1));
        event.preventDefault();
    };

    public TerminalPagesView(final TerminalPages tp, final boolean paging, final boolean currency,
            final boolean referencingThisSearch, EmbeddedPageHistory pageHistory) {
        this.terminalPages = tp;
        this.pageHistory = pageHistory;

        setHeaderVisible(false);
        setStyleName("mm-terminalPages"); // $NON-NLS-0$

        final FloatingToolbar searchToolbar = new FloatingToolbar(FOR_ICON_SIZE_S);

        if (paging) {
            addPreviousNextButtons(this.terminalPages, searchToolbar);
        }

        this.searchLabel = searchToolbar.addLabel(I18n.I.page());
        searchToolbar.addEmpty("5px"); // $NON-NLS$

        this.searchBox = new TextBox();
        this.searchBox.setMaxLength(40);
        this.searchBox.addKeyDownHandler(event -> {
            if (KeyCodes.KEY_ENTER == event.getNativeKeyCode()) {
                onPageChange();
            }
        });
        searchToolbar.add(this.searchBox);

        if (this.pageHistory != null) {
            addHistoryButtons(this.terminalPages, searchToolbar);
        }

        if (!SessionData.INSTANCE.isAnonymous()) {
            searchToolbar.addSeparator();
            this.bookmarkButton = Button.icon("mm-bookmark") // $NON-NLS$
                    .tooltip(I18n.I.addBookmarks())
                    .clickHandler(event -> this.terminalPages.bookmarkCurrentPage())
                    .build();
            searchToolbar.add(this.bookmarkButton);
            this.bookmarkButton.setEnabled(false);
        }

        if (currency) {
            addCurrencyButton(this.terminalPages, searchToolbar);
        }

        setTopComponent(searchToolbar);
        add(this.contentWrapper);

        addListener(Events.Render, baseEvent -> update(I18n.I.noPageSelected(), Mode.TEXT, null));

        if (referencingThisSearch) {
            addReferencesCurrentButton(this.terminalPages, searchToolbar);
        }
    }

    private void addPreviousNextButtons(final TerminalPages tp, FloatingToolbar tb) {
        tb.add(this.prevButton = Button.icon("x-tbar-page-prev") // $NON-NLS$
                .clickHandler(event -> tp.prevPage())
                .build());
        tb.add(this.nextButton = Button.icon("x-tbar-page-next") // $NON-NLS$
                .clickHandler(event -> tp.nextPage())
                .build());
        tb.addSeparator();
    }

    private void addHistoryButtons(final TerminalPages tp, FloatingToolbar tb) {
        tb.add(this.prevHistoryButton = Button.icon("mm-list-move-left") // $NON-NLS$
                .clickHandler(event -> tp.prevHistoryPage())
                .build());
        tb.add(this.nextHistoryButton = Button.icon("mm-list-move-right") // $NON-NLS$
                .clickHandler(event -> tp.nextHistoryPage())
                .build());
        tb.addSeparator();
    }

    private void addReferencesCurrentButton(final TerminalPages tp, FloatingToolbar tb) {
        tb.addSeparator();
        tb.add(this.pagesReferencingCurrent = Button.icon("links-to-vwdpage") // $NON-NLS$
                .tooltip(I18n.I.pagesReferencingCurrentPage())
                .clickHandler(event -> tp.searchPagesReferencingCurrent())
                .build());
    }

    private void addCurrencyButton(final TerminalPages tp, FloatingToolbar tb) {
        final Menu m = new Menu();
        m.add(new MenuItem("- Original -").withData("value", null)); // $NON-NLS$
        m.add(new MenuItem("EUR").withData("value", "EUR")); // $NON-NLS$
        m.add(new MenuItem("GBP").withData("value", "GBP")); // $NON-NLS$
        final SelectButton curButton = new SelectButton().withMenu(m);
        curButton.addSelectionHandler(event -> {
            tp.setCurrency((String) event.getSelectedItem().getData("value")); // $NON-NLS$
        });
        curButton.setClickOpensMenu(true);

        tb.addSeparator();
        tb.addLabel(I18n.I.currency() + ": "); // $NON-NLS$
        tb.add(curButton);
    }

    private void onPageChange() {
        final String page = this.searchBox.getValue().trim();
        if (StringUtil.hasText(page)) {
            if (this.terminalPages.getLastPage().equals(page)) {
                AbstractMainController.INSTANCE.refresh();
            }
            else {
                this.terminalPages.goToPage(page);
            }
        }
    }

    private boolean isWithPaging() {
        return this.prevButton != null;
    }


    void setLabelText(String label) {
        searchLabel.setText(label);
    }

    void update(String content, Mode mode, String pageKey) {
        updateBookmarkButton(pageKey);

        if (mode == Mode.IFRAME) {
            updateFrame(content);
        }
        else {
            updateText(content, mode);
        }

        this.searchBox.setValue(pageKey != null ? pageKey : ""); // $NON-NLS-0$
        if (pageKey == null || !isWithPaging()) {
            return;
        }

        if (pageKey.matches("[0-9]+")) { // $NON-NLS-0$
            final int pageNum = Integer.parseInt(pageKey);
            this.prevButton.setEnabled(!this.terminalPages.isMinPage(pageNum));
            this.nextButton.setEnabled(!this.terminalPages.isMaxPage(pageNum));
        }
        else {
            this.nextButton.setEnabled(false);
            this.prevButton.setEnabled(false);
        }

        if (this.pageHistory != null) {
            this.prevHistoryButton.setEnabled(this.pageHistory.hasPrevious());
            this.nextHistoryButton.setEnabled(this.pageHistory.hasNext());
        }
    }

    public void updateBookmarkButton(String pageKey) {
        if (this.bookmarkButton != null) {
            if (SessionData.isAsDesign()) {
                FavouriteItemsStores.ifPresent(FavouritePageItemsStore.class,
                        c -> this.bookmarkButton.setEnabled(c.canAddItem(
                                new FavouritePageItemsStore.Page(pageKey,
                                        this.terminalPages.getType()))));
            }
            else {
                this.bookmarkButton.setEnabled(pageKey != null);
            }
        }
    }

    private void updateFrame(final String content) {
        setScrollMode(Style.Scroll.NONE);
        final Frame frame = new Frame();
        frame.setStyleName("mm-terminalPageFrame"); // $NON-NLS-0$
        this.contentWrapper.setWidget(frame);
        doLayout();

        DOMUtil.fillFrame(frame, content);
        final String additionalStylesheet = this.terminalPages.getAdditionalStylesheet();
        if (additionalStylesheet != null) {
            DOMUtil.loadStylesheet(frame, additionalStylesheet, true);
        }
        DOMUtil.launderLinks(DOMUtil.getDocument(frame));
    }

    private void updateText(String content, Mode mode) {
        setScrollMode(Style.Scroll.AUTO);
        addStyleName("mm-page"); // $NON-NLS-0$
        final HTML html = toHtml(content, mode);
        this.contentWrapper.setWidget(html);
        doLayout();
    }

    private HTML toHtml(String content, Mode mode) {
        if (mode == Mode.TEXT) {
            final HTML html = new HTML();
            html.setText(content);
            return html;
        }

        return new HTMLWithLinks(getContent(content, mode), PAGE_LINK_LISTENER);
    }

    public static String getContent(String content, Mode mode) {
        if (mode == Mode.PRE) {
            return "<pre class=\"page\">" + content + "</pre>"; // $NON-NLS$
        }
        return content;
    }
}
