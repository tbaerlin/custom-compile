/*
 * PriceRenderer.java
 *
 * Created on 05.06.2008 17:04:40
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.util;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class PriceWithSuffixRenderer implements Renderer<String> {
    private final PriceStringRenderer priceRenderer;

    private final String suffix;

    public PriceWithSuffixRenderer(final PriceStringRenderer priceRenderer, final String suffix) {
        this.priceRenderer = priceRenderer;
        this.suffix = suffix;
    }

    public String render(String s) {
        if (s == null) {
            return this.priceRenderer.render(s);
        }
        return this.priceRenderer.render(s) + this.suffix;
    }
}
