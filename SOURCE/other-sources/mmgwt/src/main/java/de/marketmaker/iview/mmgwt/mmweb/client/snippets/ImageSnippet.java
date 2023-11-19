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
public class ImageSnippet extends AbstractSnippet<ImageSnippet, ImageSnippetView> {
    public static class Class extends SnippetClass {
        public Class() {
            super("Image"); // $NON-NLS-0$
        }

        public Snippet newSnippet(DmxmlContext context, SnippetConfiguration config) {
            return new ImageSnippet(context, config);
        }
    }


    private ImageSnippet(DmxmlContext context, SnippetConfiguration config) {
        super(context, config);
        this.setView(new ImageSnippetView(this));
    }

    public void destroy() {
        // empty
    }

    public void setTitle(String title) {
        getView().setTitle(title);
    }

    public void updateView() {
        getView().update();
    }
}
