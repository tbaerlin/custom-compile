/*
 * AbstractStaticData.java
 *
 * Created on 13.09.11 09:46
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.ratios.backend;

/**
 * @author oflege
 */
abstract class AbstractStaticData implements StaticData {

    private int age;

    protected int incrementAndGetAge() {
        this.age++;
        return this.age;
    }

    @Override
    public String getCurrencystrike() {
        return null;
    }

    @Override
    public long getCap() {
        return 0;
    }

    @Override
    public long getSubscriptionratio() {
        return 0;
    }

    @Override
    public String getProducttype() {
        return null;
    }

    @Override
    public int getExpires() {
        return 0;
    }

    @Override
    public long getFactsetBookvalue1Y() {
        return 0;
    }

    @Override
    public long getFactsetBookvalue2Y() {
        return 0;
    }

    @Override
    public long getFactsetCashflow1Y() {
        return 0;
    }

    @Override
    public long getFactsetCashflow2Y() {
        return 0;
    }

    @Override
    public long getFactsetSales1Y() {
        return 0;
    }

    @Override
    public long getFactsetSales2Y() {
        return 0;
    }

    @Override
    public long getFactsetEarning1Y() {
        return 0;
    }

    @Override
    public long getFactsetEarning2Y() {
        return 0;
    }

    @Override
    public long getRedemptionPrice() {
        return 0;
    }

    @Override
    public long getWmDividend() {
        return 0;
    }

    @Override
    public long getWmDividendLastYear() {
        return 0;
    }

    @Override
    public long getWmIssueVolume() {
        return 0;
    }

    @Override
    public long getWmNumberOfIssuedEquities() {
        return 0;
    }

    @Override
    public String getWmDividendCurrency() {
        return null;
    }

    @Override
    public Long getVwdbenlBenchmarkQid() {
        return null;
    }

    @Override
    public Long getVwdBenchmarkQid() { return null; }

    @Override
    public String getFactsetCurrency() {
        return null;
    }

    @Override
    public long getTRBookvalue1Y() {
        return 0;
    }

    @Override
    public long getTRBookvalue2Y() {
        return 0;
    }

    @Override
    public long getTRCashflow1Y() {
        return 0;
    }

    @Override
    public long getTRCashflow2Y() {
        return 0;
    }

    @Override
    public long getTRSales1Y() {
        return 0;
    }

    @Override
    public long getTRSales2Y() {
        return 0;
    }

    @Override
    public long getTREarning1Y() {
        return 0;
    }

    @Override
    public long getTREarning2Y() {
        return 0;
    }

    @Override
    public String getTRCurrency() {
        return null;
    }
}
