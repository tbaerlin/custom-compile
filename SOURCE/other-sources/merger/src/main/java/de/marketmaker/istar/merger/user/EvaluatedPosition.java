/*
 * EvaluatedPosition.java
 *
 * Created on 9/15/14 12:40 PM
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.user;

import java.math.BigDecimal;
import java.util.List;

import org.joda.time.DateTime;

import de.marketmaker.istar.domain.data.PriceRecord;
import de.marketmaker.istar.domain.instrument.Quote;

/**
 * @author kmilyut
 */
public interface EvaluatedPosition {
    BigDecimal getRealizedCost();

    BigDecimal getRealizedGain();

    BigDecimal getOrderValue();

    BigDecimal getOrderValueInPortfolioCurrency();

    BigDecimal getCurrentValue();

    BigDecimal getCurrentValueInPortfolioCurrency();

    BigDecimal getPreviousCloseValue();

    BigDecimal getPreviousCloseValueInPortfolioCurrency();

    BigDecimal getAverageOrderPrice();

    BigDecimal getAverageOrderPriceInPortfolioCurrency();

    BigDecimal getChangeNet();

    BigDecimal getChangePercent();

    BigDecimal getChangeNetInPortfolioCurrency();

    BigDecimal getChangePercentInPortfolioCurrency();

    BigDecimal getDailyChangeNet();

    BigDecimal getDailyChangePercent();

    BigDecimal getDailyChangeNetInPortfolioCurrency();

    BigDecimal getDailyChangePercentInPortfolioCurrency();

    BigDecimal getTotalVolume();

    PriceRecord getCurrentPrice();

    BigDecimal getExchangerate();

    Quote getQuote();

    DateTime getLastOrderDate();

    PortfolioPosition getPosition();
}
