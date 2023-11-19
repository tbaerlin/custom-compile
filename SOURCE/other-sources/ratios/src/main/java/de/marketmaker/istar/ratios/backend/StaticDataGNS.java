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
public class StaticDataGNS extends AbstractStaticData {
    public final static StaticDataGNS NULL = new StaticDataGNS(Long.MIN_VALUE, Long.MIN_VALUE, Long.MIN_VALUE, Long.MIN_VALUE, null);

    private final long wmIssueVolume;

    private final long wmNumberOfIssuedEquities;

    private final long wmDividend;

    private final long wmDividendLastYear;

    private final String wmDividendCurrency;

    public StaticDataGNS(long wmIssueVolume, long wmNumberOfIssuedEquities,
            long wmDividend, long wmDividendLastYear, String wmDividendCurrency) {
        this.wmIssueVolume = wmIssueVolume;
        this.wmNumberOfIssuedEquities = wmNumberOfIssuedEquities;
        this.wmDividend = wmDividend;
        this.wmDividendLastYear = wmDividendLastYear;
        this.wmDividendCurrency = wmDividendCurrency;
    }

    public long getWmIssueVolume() {
        return wmIssueVolume;
    }

    public long getWmNumberOfIssuedEquities() {
        return wmNumberOfIssuedEquities;
    }

    public long getWmDividend() {
        return wmDividend;
    }

    public long getWmDividendLastYear() {
        return wmDividendLastYear;
    }

    public String getWmDividendCurrency() {
        return wmDividendCurrency;
    }
}