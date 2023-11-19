/*
 * PriceWithCurrencyRenderer.java
 *
 * Created on 14.10.13 17:11
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.util;

import de.marketmaker.iview.mmgwt.mmweb.client.PriceWithCurrency;

/**
 * This renderer is not intended to be used for pushed prices.
 *
 * @author Markus Dick
 */
public class PriceWithCurrencyRenderer implements Renderer<PriceWithCurrency> {
    private final Renderer<String> priceRenderer;

    public PriceWithCurrencyRenderer(Renderer<String> priceRenderer) {
        this.priceRenderer = priceRenderer;
    }

    @Override
    public String render(PriceWithCurrency priceWithCurrency) {
        if(priceWithCurrency == null) {
            return Renderer.STRING_DOUBLE_DASH.render(null);
        }

        return StringUtil.join(' ',
                this.priceRenderer.render(priceWithCurrency.getPrice()),
                Renderer.STRING_DOUBLE_DASH.render(priceWithCurrency.getCurrency()));
    }
}
