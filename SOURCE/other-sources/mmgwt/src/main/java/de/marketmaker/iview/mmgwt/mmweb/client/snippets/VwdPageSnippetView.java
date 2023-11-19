/*
 * PageSnippetView.java
 *
 * Created on 31.03.2008 11:23:52
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.snippets;

import com.google.gwt.dom.client.AnchorElement;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.EventListener;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextBox;

import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.terminalpages.TerminalPages;
import de.marketmaker.iview.mmgwt.mmweb.client.terminalpages.TerminalPagesView;
import de.marketmaker.iview.mmgwt.mmweb.client.terminalpages.VwdPageController;
import de.marketmaker.iview.mmgwt.mmweb.client.view.FloatingToolbar;
import de.marketmaker.iview.mmgwt.mmweb.client.view.HTMLWithLinks;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class VwdPageSnippetView extends SnippetView<VwdPageSnippet> {
    private final SimplePanel panel;

    private boolean withToolbar;

    private final EventListener eventListener = event -> {
        if (DOM.eventGetType(event) != Event.ONCLICK) {
            return;
        }

        final Element target = DOM.eventGetTarget(event);
        final AnchorElement anchor = AnchorElement.as(target);
        if (!"".equals(anchor.getTarget())) {
            return;
        }

        final int pos = anchor.getHref().indexOf(VwdPageController.LINK_TEXT);
        if (pos == -1) {
            return;
        }

        snippet.loadPage(anchor.getHref().substring(pos + VwdPageController.LINK_TEXT.length()));
        event.preventDefault();
    };


    public VwdPageSnippetView(VwdPageSnippet snippet, SnippetConfiguration config) {
        super(snippet);
        setTitle(config.getString("title")); // $NON-NLS$
        this.withToolbar = config.getBoolean("withToolbar", true); // $NON-NLS$
        this.panel = new SimplePanel();
    }

    protected void onContainerAvailable() {
        super.onContainerAvailable();
        this.container.setContentWidget(this.panel);
        if (!this.withToolbar) {
            return;
        }

        final FloatingToolbar toolbar = new FloatingToolbar();
        toolbar.addLabel(I18n.I.page());

        final TextBox pageTextBox = new TextBox();
        pageTextBox.setWidth("60px");  // $NON-NLS$
        toolbar.addEmpty("5px");  // $NON-NLS$
        toolbar.add(pageTextBox);

        this.container.setTopWidget(toolbar);

        pageTextBox.addKeyPressHandler(event -> {
            if (KeyCodes.KEY_ENTER == event.getNativeEvent().getKeyCode()) {
                final String q = pageTextBox.getValue();
                if (q == null || q.trim().length() == 0) {
                    return;
                }
                this.snippet.loadPage(q.trim());
            }
        });
    }

    public void showError() {
        this.panel.setWidget(new HTML(I18n.I.pageCannotBeDisplayed()));
    }

    public void showEmpty() {
        this.panel.setWidget(new HTML(I18n.I.pageWithoutContent()));
    }

    public void showPage(String text) {
        reloadTitle();

        final EventListener el = this.snippet.useLocalLinks()
                ? this.eventListener
                : TerminalPagesView.PAGE_LINK_LISTENER;

        final HTML html =
                new HTMLWithLinks(TerminalPagesView.getContent(text, TerminalPages.Mode.PRE), el);
        this.panel.setWidget(html);
    }
}
