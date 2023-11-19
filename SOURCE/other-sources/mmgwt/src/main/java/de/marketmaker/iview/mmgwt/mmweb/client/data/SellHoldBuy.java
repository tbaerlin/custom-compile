/*
 * SellHoldBuy.java
 *
 * Created on 20.08.2008 17:29:39
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */

package de.marketmaker.iview.mmgwt.mmweb.client.data;

import de.marketmaker.iview.dmxml.RSCAggregatedFinderElement;
import de.marketmaker.iview.dmxml.RecommendationCount;

/**
 * @author Ulrich Maurer
*/
public class SellHoldBuy {
    private final int numStrongSell;
    private final int numSell;
    private final int numHold;
    private final int numBuy;
    private final int numStrongBuy;

    private final int numAll;

    private int numMaxAll = -1;

    public SellHoldBuy(RSCAggregatedFinderElement e) {
        this.numStrongSell = getCount(e, "STRONG_SELL"); // $NON-NLS-0$
        this.numSell = getCount(e, "SELL"); // $NON-NLS-0$
        this.numHold = getCount(e, "HOLD"); // $NON-NLS-0$
        this.numBuy = getCount(e, "BUY"); // $NON-NLS-0$
        this.numStrongBuy = getCount(e, "STRONG_BUY"); // $NON-NLS-0$

        this.numAll = this.numStrongSell + this.numSell + this.numHold + this.numBuy + this.numStrongBuy;
    }

    private static int getCount(RSCAggregatedFinderElement e, String key) {
        if (e.getGroup() == null) return 0;
        for (RecommendationCount count : e.getGroup()) {
            if (key.equals(count.getRecommendation())) {
                return Integer.parseInt(count.getCount());
            }
        }
        return 0;
    }

    public int getAllBuy() {
        return this.numBuy + this.numStrongBuy;
    }

    public int getAllSell() {
        return this.numSell + this.numStrongSell;
    }

    public int getHold() {
        return this.numHold;
    }

    public double getCoefficient() {
        int n = this.numAll;
        if (n == 0) {
            return 0;
        }
        return (this.numStrongSell * -2 + this.numSell * -1 + this.numBuy + this.numStrongBuy * 2) / (double) n;
    }

    public int getAll() {
        return this.numAll;
    }

    public int getMaxAll() {
        return this.numMaxAll;
    }


    public void setMaxAll(int numMaxAll) {
        this.numMaxAll = numMaxAll;
    }

    public float getAllBuyPercent() {
        return 100f * getAllBuy() / this.numAll;
    }

    public float getHoldPercent() {
        return 100f * this.numHold / this.numAll;
    }

    public float getAllSellPercent() {
        return 100f * getAllSell() / this.numAll;
    }

    public float getMaxAllPercent() {
        return this.numMaxAll == -1 ? 100f : 100f * this.numAll / this.numMaxAll;
    }
}
