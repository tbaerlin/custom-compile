/*
 * StockRevenueSummary.java
 *
 * Created on 12.07.2006 15:08:06
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domain.data;

import java.math.BigDecimal;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.LocalDate;

import de.marketmaker.istar.domainimpl.data.StockRevenueSummaryImpl;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public interface StockRevenueSummary {
    Interval getReference();

    String getCurrency();

    Integer getFiscalYear();

    StockRevenueSummaryImpl.Field getEarningPerShare();

    StockRevenueSummaryImpl.Field getEarningPerShareAfterGoodwill();

    StockRevenueSummaryImpl.Field getEarningPerShareBeforeGoodwill();

    StockRevenueSummaryImpl.Field getLongTermGrowth();

    StockRevenueSummaryImpl.Field getBookValue();

    StockRevenueSummaryImpl.Field getBookValueAdjusted();

    StockRevenueSummaryImpl.Field getPriceEarningRatio();

    StockRevenueSummaryImpl.Field getCashflow();

    StockRevenueSummaryImpl.Field getDividend();

    StockRevenueSummaryImpl.Field getDividendYield();

    StockRevenueSummaryImpl.Field getNetProfit();

    StockRevenueSummaryImpl.Field getPreTaxProfit();

    StockRevenueSummaryImpl.Field getEBIT();

    StockRevenueSummaryImpl.Field getEBITDA();

    StockRevenueSummaryImpl.Field getGoodwill();

    StockRevenueSummaryImpl.Field getNetDebt();

    StockRevenueSummaryImpl.Field getPriceTarget();

    StockRevenueSummaryImpl.Field getSales();

    StockRevenueSummaryImpl.Field getPostEventConsensus();

    List<String> getBrokerNames();

    BigDecimal getPrice();

    BigDecimal getRecommendation();

    Integer getNumBuy();

    Integer getNumOverweight();

    Integer getNumHold();

    Integer getNumUnderweight();

    Integer getNumSell();

    Integer getNumTotal();

    StockRevenueSummaryImpl.Field getNumberOfShares();

    LocalDate getReferenceDate();

    DateTime getDmlDate();

    StockRevenueSummaryImpl.Field getFreeCashFlow();

    StockRevenueSummaryImpl.Field getReturnOnEquity();

    StockRevenueSummaryImpl.Field getReturnOnInvestedCapital();
}
