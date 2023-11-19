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
public class StaticDataSTK extends AbstractStaticData {
    public final static StaticDataSTK NULL = new StaticDataSTK(Long.MIN_VALUE, Long.MIN_VALUE,
            Long.MIN_VALUE, Long.MIN_VALUE, Long.MIN_VALUE, Long.MIN_VALUE, Long.MIN_VALUE,
            Long.MIN_VALUE, null, Long.MIN_VALUE, Long.MIN_VALUE, Long.MIN_VALUE, Long.MIN_VALUE,
            Long.MIN_VALUE, Long.MIN_VALUE, Long.MIN_VALUE, Long.MIN_VALUE, null, Long.MIN_VALUE,
            Long.MIN_VALUE, null, Long.MIN_VALUE, Long.MIN_VALUE);

    private final long factsetBookvalue1Y;

    private final long factsetBookvalue2Y;

    private final long factsetCashflow1Y;

    private final long factsetCashflow2Y;

    private final long factsetSales1Y;

    private final long factsetSales2Y;

    private final long factsetEarning1Y;

    private final long factsetEarning2Y;

    private final String factsetCurrency;

    private final long trBookvalue1Y;

    private final long trBookvalue2Y;

    private final long trCashflow1Y;

    private final long trCashflow2Y;

    private final long trSales1Y;

    private final long trSales2Y;

    private final long trEarning1Y;

    private final long trEarning2Y;

    private final String trCurrency;

    private final long wmDividend;

    private final long wmDividendLastYear;

    private final String wmDividendCurrency;

    private final long wmIssueVolume;

    private final long wmNumberOfIssuedEquities;

    public StaticDataSTK(long factsetBookvalue1Y, long factsetBookvalue2Y, long factsetCashflow1Y,
            long factsetCashflow2Y, long factsetSales1Y, long factsetSales2Y, long factsetEarning1Y,
            long factsetEarning2Y, String factsetCurrency,
            long trBookvalue1Y, long trBookvalue2Y, long trCashflow1Y, long trCashflow2Y,
            long trSales1Y, long trSales2Y, long trEarning1Y, long trEarning2Y, String trCurrency,
            long wmDividend, long wmDividendLastYear, String wmDividendCurrency,
            long wmIssueVolume, long wmNumberOfIssuedEquities) {
        this.factsetBookvalue1Y = factsetBookvalue1Y;
        this.factsetBookvalue2Y = factsetBookvalue2Y;
        this.factsetCashflow1Y = factsetCashflow1Y;
        this.factsetCashflow2Y = factsetCashflow2Y;
        this.factsetSales1Y = factsetSales1Y;
        this.factsetSales2Y = factsetSales2Y;
        this.factsetEarning1Y = factsetEarning1Y;
        this.factsetEarning2Y = factsetEarning2Y;
        this.factsetCurrency = factsetCurrency;
        this.trBookvalue1Y = trBookvalue1Y;
        this.trBookvalue2Y = trBookvalue2Y;
        this.trCashflow1Y = trCashflow1Y;
        this.trCashflow2Y = trCashflow2Y;
        this.trSales1Y = trSales1Y;
        this.trSales2Y = trSales2Y;
        this.trEarning1Y = trEarning1Y;
        this.trEarning2Y = trEarning2Y;
        this.trCurrency = trCurrency;
        this.wmDividend = wmDividend;
        this.wmDividendLastYear = wmDividendLastYear;
        this.wmDividendCurrency = wmDividendCurrency;
        this.wmIssueVolume = wmIssueVolume;
        this.wmNumberOfIssuedEquities = wmNumberOfIssuedEquities;
    }

    public long getFactsetBookvalue1Y() {
        return factsetBookvalue1Y;
    }

    public long getFactsetBookvalue2Y() {
        return factsetBookvalue2Y;
    }

    public long getFactsetCashflow1Y() {
        return factsetCashflow1Y;
    }

    public long getFactsetCashflow2Y() {
        return factsetCashflow2Y;
    }

    public long getFactsetSales1Y() {
        return factsetSales1Y;
    }

    public long getFactsetSales2Y() {
        return factsetSales2Y;
    }

    public long getFactsetEarning1Y() {
        return factsetEarning1Y;
    }

    public long getFactsetEarning2Y() {
        return factsetEarning2Y;
    }

    public String getFactsetCurrency() {
        return factsetCurrency;
    }

    public long getTRBookvalue1Y() {
        return trBookvalue1Y;
    }

    public long getTRBookvalue2Y() {
        return trBookvalue2Y;
    }

    public long getTRCashflow1Y() {
        return trCashflow1Y;
    }

    public long getTRCashflow2Y() {
        return trCashflow2Y;
    }

    public long getTRSales1Y() {
        return trSales1Y;
    }

    public long getTRSales2Y() {
        return trSales2Y;
    }

    public long getTREarning1Y() {
        return trEarning1Y;
    }

    public long getTREarning2Y() {
        return trEarning2Y;
    }

    public String getTRCurrency() {
        return trCurrency;
    }

    public long getWmDividend() {
        return wmDividend;
    }

    public long getWmDividendLastYear() {
        return wmDividendLastYear;
    }

    public long getWmIssueVolume() {
        return wmIssueVolume;
    }

    public long getWmNumberOfIssuedEquities() {
        return wmNumberOfIssuedEquities;
    }

    public String getWmDividendCurrency() {
        return wmDividendCurrency;
    }
}