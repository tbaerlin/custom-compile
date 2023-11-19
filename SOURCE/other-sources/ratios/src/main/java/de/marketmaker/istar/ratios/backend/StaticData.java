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
public interface StaticData {
    String getCurrencystrike();

    long getCap();

    long getSubscriptionratio();

    String getProducttype();

    int getExpires();

    long getFactsetBookvalue1Y();

    long getFactsetBookvalue2Y();

    long getFactsetCashflow1Y();

    long getFactsetCashflow2Y();

    long getFactsetSales1Y();

    long getFactsetSales2Y();

    long getFactsetEarning1Y();

    long getFactsetEarning2Y();

    long getRedemptionPrice();

    long getWmDividend();

    long getWmDividendLastYear();

    String getWmDividendCurrency();

    long getWmIssueVolume();

    long getWmNumberOfIssuedEquities();

    Long getVwdBenchmarkQid();

    Long getVwdbenlBenchmarkQid();

    String getFactsetCurrency();

    long getTRBookvalue1Y();

    long getTRBookvalue2Y();

    long getTRCashflow1Y();

    long getTRCashflow2Y();

    long getTRSales1Y();

    long getTRSales2Y();

    long getTREarning1Y();

    long getTREarning2Y();

    String getTRCurrency();
}
