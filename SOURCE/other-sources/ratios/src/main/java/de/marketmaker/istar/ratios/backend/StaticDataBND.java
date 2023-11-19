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
public class StaticDataBND extends AbstractStaticData {
    public final static StaticDataBND NULL = new StaticDataBND(Long.MIN_VALUE);

    private final long redemptionPrice;

    public StaticDataBND(long redemptionPrice) {
        this.redemptionPrice = redemptionPrice;
    }

    public long getRedemptionPrice() {
        return redemptionPrice;
    }

    public String toString() {
        return "StaticDataBND[redemptionPrice=" + redemptionPrice
                + "]";
    }
}