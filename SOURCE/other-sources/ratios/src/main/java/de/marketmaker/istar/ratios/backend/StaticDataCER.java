/*
 * StaticData.java
 *
 * Created on 16.09.2005 14:10:19
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.ratios.backend;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class StaticDataCER extends AbstractStaticData {
    public final static StaticDataCER NULL = new StaticDataCER(0, 0, null, 0, null);

    private final long cap;
    private final long subscriptionratio;
    private final String producttype;
    private final int expires;
    private final String currencystrike;

    StaticDataCER(long cap, long subscriptionratio, String producttype, int expires, String strikecurrency) {
        this.cap = cap;
        this.subscriptionratio = subscriptionratio;
        this.producttype = producttype;
        this.expires = expires;
        this.currencystrike = strikecurrency;
    }

    public String getCurrencystrike() {
        return currencystrike;
    }

    public long getCap() {
        return cap;
    }

    public long getSubscriptionratio() {
        return subscriptionratio;
    }

    public String getProducttype() {
        return producttype;
    }

    public int getExpires() {
        return expires;
    }

    public String toString() {
        return "StaticDataCER[cap=" + cap
                + ", currencystrike=" + currencystrike
                + ", subscriptionratio=" + subscriptionratio
                + ", producttype=" + producttype
                + ", expires=" + expires
                + "]";
    }
}