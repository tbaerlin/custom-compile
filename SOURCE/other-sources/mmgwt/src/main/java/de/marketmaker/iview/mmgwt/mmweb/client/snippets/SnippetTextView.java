/*
 * PriceListSnippetView.java
 *
 * Created on 31.03.2008 11:23:52
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.snippets;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.client.ui.HTML;
import de.marketmaker.itools.gwtutil.client.util.StringUtility;

/**
 * A simple view that show a single SnippetTableWidget
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class SnippetTextView<S extends Snippet<S>> extends SnippetView<S> {
    private final HTML html = new HTML();

    public SnippetTextView(S snippet) {
        super(snippet);
        setTitle(snippet.getConfiguration().getString("title")); // $NON-NLS-0$
    }

    @Override
    protected void onContainerAvailable() {
        this.container.setHeaderVisible(StringUtility.hasText(this.container.getHeading()));
        this.container.setContentWidget(this.html);
    }

    public void setTitle(String title) {
        this.snippet.getConfiguration().put("title", title); // $NON-NLS-0$
        reloadTitle();
    }

    public void setStyleName(String style) {
        this.html.setStyleName(style);
    }

    public void setText(String text) {
        this.html.setText(text);
    }

    public void setHtml(String html) {
        this.html.setHTML(html);
    }

    public void setHtml(SafeHtml safeHtml) {
        this.html.setHTML(safeHtml);
    }

    public HTML getWidget() {
        return this.html;
    }
}
