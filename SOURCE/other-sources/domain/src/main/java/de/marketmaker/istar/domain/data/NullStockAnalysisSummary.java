/*
 * NullStockAnalysisSummary.java
 *
 * Created on 29.03.12 12:04
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domain.data;

import java.math.BigDecimal;

import org.joda.time.DateTime;

/**
 * @author oflege
 */
public class NullStockAnalysisSummary implements StockAnalysisSummary {
    public static final StockAnalysisSummary INSTANCE = new NullStockAnalysisSummary();

    private NullStockAnalysisSummary() {
    }

    @Override
    public int getNumberOfAnalyses() {
        return 0;
    }

    @Override
    public BigDecimal getNormalizedRecommendation(boolean strongEqualsNormal) {
        return null;
    }

    @Override
    public DateTime getDate() {
        return null;
    }

    @Override
    public int getNumberOfBuys() {
        return 0;
    }

    @Override
    public int getNumberOfStrongBuys() {
        return 0;
    }

    @Override
    public int getNumberOfHolds() {
        return 0;
    }

    @Override
    public int getNumberOfSells() {
        return 0;
    }

    @Override
    public int getNumberOfStrongSells() {
        return 0;
    }
}
