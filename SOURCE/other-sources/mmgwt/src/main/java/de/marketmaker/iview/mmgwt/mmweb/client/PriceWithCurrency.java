/*
 * PriceWithCurrency.java
 *
 * Created on 04.06.2008 17:45:56
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class PriceWithCurrency {
    private final String price;
    private final String currency;

    public PriceWithCurrency(String price, String currency) {
        this.price = price;
        this.currency = currency;
    }

    public String getPrice() {
        return price;
    }

    public String getCurrency() {
        return currency;
    }
}
