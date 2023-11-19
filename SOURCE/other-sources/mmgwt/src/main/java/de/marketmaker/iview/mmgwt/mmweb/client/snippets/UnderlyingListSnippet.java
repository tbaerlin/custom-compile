/*
 * UnderlyingListSnippet.java
 *
 * Created on 02.12.2009 10:50:00
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.snippets;

import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;

/**
 * @author Ulrich maurer
 */
public class UnderlyingListSnippet extends AbstractSnippet<UnderlyingListSnippet, UnderlyingListSnippetView> {
    public static class Class extends SnippetClass {
        public Class() {
            super("UnderlyingList", I18n.I.underlyings()); // $NON-NLS-0$
        }

        public Snippet newSnippet(DmxmlContext context, SnippetConfiguration config) {
            return new UnderlyingListSnippet(context, config);
        }

        @Override
        protected void addDefaultParameters(SnippetConfiguration config) {
            config.put("width", DEFAULT_SNIPPET_WIDTH); // $NON-NLS$
        }
    }

    private UnderlyingListSnippet(DmxmlContext context, SnippetConfiguration config) {
        super(context, config);
        this.setView(new UnderlyingListSnippetView(this, config));
    }

    public void destroy() {
    }

    public void updateView() {
    }
}
