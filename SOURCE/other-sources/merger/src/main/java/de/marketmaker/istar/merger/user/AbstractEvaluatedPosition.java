/*
 * AbstractEvaluatedPosition.java
 *
 * Created on 10/28/14 4:12 PM
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.user;

import java.math.BigDecimal;

import static de.marketmaker.istar.merger.Constants.MC;
import static de.marketmaker.istar.merger.Constants.ONE_PERCENT;

/**
 * @author kmilyut
 */
abstract class AbstractEvaluatedPosition implements EvaluatedPosition {


    @Override
    public BigDecimal getChangePercentInPortfolioCurrency() {
        final BigDecimal currentValue = getCurrentValueInPortfolioCurrency();
        final BigDecimal orderValue = getOrderValueInPortfolioCurrency();
        if (BigDecimal.ZERO.compareTo(orderValue) == 0) {
            return BigDecimal.ZERO;
        }
        return currentValue.subtract(orderValue, MC).divide(orderValue, MC);
    }

    @Override
    public BigDecimal getDailyChangeNet() {
        final BigDecimal currentValue = getCurrentValue();
        final BigDecimal previousCloseValue = getPreviousCloseValue();
        return currentValue.subtract(previousCloseValue, MC);
    }

    @Override
    public BigDecimal getDailyChangePercent() {
        final BigDecimal currentValue = getCurrentValue();
        final BigDecimal previousCloseValue = getPreviousCloseValue();
        if (BigDecimal.ZERO.compareTo(previousCloseValue) == 0) {
            return BigDecimal.ZERO;
        }
        return currentValue.subtract(previousCloseValue, MC).divide(previousCloseValue, MC);
    }

    @Override
    public BigDecimal getDailyChangeNetInPortfolioCurrency() {
        final BigDecimal currentValue = getCurrentValueInPortfolioCurrency();
        final BigDecimal previousCloseValue = getPreviousCloseValueInPortfolioCurrency();
        return currentValue.subtract(previousCloseValue, MC);
    }

    @Override
    public BigDecimal getDailyChangePercentInPortfolioCurrency() {
        final BigDecimal currentValue = getCurrentValueInPortfolioCurrency();
        final BigDecimal previousCloseValue = getPreviousCloseValueInPortfolioCurrency();
        if (BigDecimal.ZERO.compareTo(previousCloseValue) == 0) {
            return BigDecimal.ZERO;
        }
        return currentValue.subtract(previousCloseValue, MC).divide(previousCloseValue, MC);
    }

    @Override
    public BigDecimal getChangeNet() {
        final BigDecimal currentValue = getCurrentValue();
        final BigDecimal orderValue = getOrderValue();
        return currentValue.subtract(orderValue, MC);
    }

    @Override
    public BigDecimal getChangePercent() {
        final BigDecimal currentValue = getCurrentValue();
        final BigDecimal orderValue = getOrderValue();
        if (BigDecimal.ZERO.compareTo(orderValue) == 0) {
            return BigDecimal.ZERO;
        }
        return currentValue.subtract(orderValue, MC).divide(orderValue, MC);
    }

    @Override
    public BigDecimal getChangeNetInPortfolioCurrency() {
        final BigDecimal currentValue = getCurrentValueInPortfolioCurrency();
        final BigDecimal orderValue = getOrderValueInPortfolioCurrency();
        return currentValue.subtract(orderValue, MC);
    }

    protected BigDecimal getQuotedPerFactor() {
        return getPosition().isQuotedPerPercent() ? ONE_PERCENT : BigDecimal.ONE;
    }
}
