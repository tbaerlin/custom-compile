/*
 * PriceListSnippet.java
 *
 * Created on 28.03.2008 16:11:58
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.snippets;

import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;

/**
 * @author Ulrich Maurer
 */
public class SimpleHtmlSnippet extends AbstractSnippet<SimpleHtmlSnippet, SnippetTextView<SimpleHtmlSnippet>> {
    public static class Class extends SnippetClass {
        public Class() {
            super("SimpleHtml"); // $NON-NLS-0$
        }

        public Snippet newSnippet(DmxmlContext context, SnippetConfiguration config) {
            return new SimpleHtmlSnippet(context, config);
        }
    }

    SimpleHtmlSnippet(DmxmlContext context, SnippetConfiguration config) {
        super(context, config);
        this.setView(new SnippetTextView<SimpleHtmlSnippet>(this));
    }

    public void setHtml(String html) {
        getView().setHtml(html);
    }

    public void destroy() {
    }

    public void updateView() {
    }
}
