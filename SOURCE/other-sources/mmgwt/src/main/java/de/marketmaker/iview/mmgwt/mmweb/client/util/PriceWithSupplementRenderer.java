/*
 * PriceWithSupplementRenderer.java
 *
 * Created on 05.09.2008 16:49:01
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */

package de.marketmaker.iview.mmgwt.mmweb.client.util;

import de.marketmaker.iview.mmgwt.mmweb.client.data.PriceWithSupplement;

/**
 * @author Ulrich Maurer
 */
public class PriceWithSupplementRenderer implements Renderer<PriceWithSupplement> {
    private final Renderer<String> rendererPrice;
    private final Renderer<String> rendererSupplement;

    public PriceWithSupplementRenderer() {
        this(Renderer.PRICE, new SupplementRenderer()); // strange bug: this.rendererSupplement is not assigned, if Renderer.SUPPLEMENT is used
    }

    public PriceWithSupplementRenderer(Renderer<String> rendererPrice, Renderer<String> rendererSupplement) {
        this.rendererPrice = rendererPrice;
        this.rendererSupplement = rendererSupplement;
    }

    public String render(PriceWithSupplement priceWithSupplement) {
        if (priceWithSupplement == null) {
            return ""; // $NON-NLS-0$
        }
        return this.rendererPrice.render(priceWithSupplement.getPrice()) + this.rendererSupplement.render(priceWithSupplement.getSupplement());
    }
}
