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
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class NewsSearchParamsSnippet
        extends AbstractSnippet<NewsSearchParamsSnippet, NewsSearchParamsSnippetView> {
    public static class Class extends SnippetClass {
        public Class() {
            super("NewsSearchParams"); // $NON-NLS-0$
        }

        public Snippet newSnippet(DmxmlContext context, SnippetConfiguration config) {
            return new NewsSearchParamsSnippet(context, config);
        }
    }


    private NewsSearchParamsSnippet(DmxmlContext context, SnippetConfiguration config) {
        super(context, config);
        this.setView(new NewsSearchParamsSnippetView(this));
    }

    public void destroy() {
    }


    public void setSearchstring(String searchstring) {
        getView().setSearchstring(searchstring);
    }


    public void updateView() {
    }
}
