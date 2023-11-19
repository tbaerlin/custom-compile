/*
 * DividendDataImpl.java
 *
 * Created on 21.10.2014 15:23
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domainimpl.data;

import java.io.Serializable;
import java.math.BigDecimal;

import de.marketmaker.istar.domain.data.DividendData;

/**
 * @author jkirchg
 */
public class DividendDataImpl implements Serializable, DividendData {

    private final int year;

    private final BigDecimal dividendPayment;

    private final BigDecimal dividendPayoutRatio;

    private final BigDecimal dividendYield;

    private final BigDecimal dividendCoverage;

    private final BigDecimal dividendPerShare;

    private final BigDecimal dividendPerShareGrowth5y;

    public DividendDataImpl(int year, BigDecimal dividendPayment,
            BigDecimal dividendPayoutRatio, BigDecimal dividendYield,
            BigDecimal dividendCoverage, BigDecimal dividendPerShare,
            BigDecimal dividendPerShareGrowth5y) {
        this.year = year;
        this.dividendPayment = dividendPayment;
        this.dividendPayoutRatio = dividendPayoutRatio;
        this.dividendYield = dividendYield;
        this.dividendCoverage = dividendCoverage;
        this.dividendPerShare = dividendPerShare;
        this.dividendPerShareGrowth5y = dividendPerShareGrowth5y;
    }

    @Override
    public int getYear() {
        return this.year;
    }

    @Override
    public BigDecimal getDividendPayment() {
        return this.dividendPayment;
    }

    @Override
    public BigDecimal getDividendPayoutRatio() {
        return this.dividendPayoutRatio;
    }

    @Override
    public BigDecimal getDividendYield() {
        return this.dividendYield;
    }

    @Override
    public BigDecimal getDividendCoverage() {
        return this.dividendCoverage;
    }

    @Override
    public BigDecimal getDividendPerShare() {
        return this.dividendPerShare;
    }

    @Override
    public BigDecimal getDividendPerShareGrowth5y() {
        return this.dividendPerShareGrowth5y;
    }

    @Override
    public String toString() {
        return "DividendDataImpl{" +
                "year=" + year +
                ", dividendPayment=" + dividendPayment +
                ", dividendPayoutRatio=" + dividendPayoutRatio +
                ", dividendYield=" + dividendYield +
                ", dividendCoverage=" + dividendCoverage +
                ", dividendPerShare=" + dividendPerShare +
                ", dividendPerShareGrowth5y=" + dividendPerShareGrowth5y +
                "}";
    }
}
