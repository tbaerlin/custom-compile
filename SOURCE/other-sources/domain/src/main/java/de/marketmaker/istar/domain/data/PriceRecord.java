/*
 * PriceRecord.java
 *
 * Created on 07.07.2006 14:10:22
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domain.data;

import java.math.BigDecimal;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public interface PriceRecord extends HasPriceQuality {

    boolean isCurrentDayDefined();

    Price getValuationPrice();

    DateTime getDate();

    Price getPrice();

    Price getAsk();

    Price getBid();

    /**
     * @return same value as {@link #getAsk()} if the ask's value is != 0; otherwise, a Price
     * with the latest available value != 0, provided that value is available.
     */
    Price getLastAsk();

    /**
     * @return same value as {@link #getBid()} if the bid's value is != 0; otherwise, a Price
     * with the latest available value != 0, provided that value is available.
     */
    Price getLastBid();

    BigDecimal getSpreadNet();

    BigDecimal getSpreadPercent();

    BigDecimal getSpreadHomogenized();

    BigDecimal getChangeNet();

    BigDecimal getChangePercent();

    Price getHighDay();

    Price getLowDay();

    Price getPreviousHighDay();

    Price getPreviousLowDay();

    Price getHighYear();

    Price getLowYear();

    Price getHigh52W();

    Price getLow52W();

    Long getVolumeDay();

    Long getPreviousVolumeDay();

    BigDecimal getTurnoverDay();

    BigDecimal getPreviousTurnoverDay();

    Long getNumberOfTrades();

    Price getKassa();

    Price getOpen();

    Price getPreviousOpen();

    Price getPreviousClose();

    BigDecimal getYield();

    Price getYieldPrice();

    BigDecimal getPreviousYield();

    Price getPreviousYieldPrice();

    Price getPreviousOfficialBid();

    Price getPreviousOfficialAsk();

    Price getUnofficialBid();

    Price getUnofficialAsk();

    Price getPreviousUnofficialBid();

    Price getPreviousUnofficialAsk();

    BigDecimal getInterpolatedClosing();

    BigDecimal getProvisionalEvaluation();

    BigDecimal getBidYield();

    BigDecimal getAskYield();

    BigDecimal getBrokenPeriodInterest();

    BigDecimal getDuration();

    BigDecimal getConvexity();

    BigDecimal getInterestRateElasticity();

    BigDecimal getBasePointValue();

    BigDecimal getCloseBefore(LocalDate date);

    Price getSettlement();

    Price getPreviousSettlement();

    /**
     * open interest is the number of contracts that have not been settled; to be able to associate
     * a date with it, we return the open interest as the volume of a
     * <code>Price</code> whose value is always 0; the price's date is the date of the open interest
     * value
     */
    Price getOpenInterest();

    Price getOfficialClose();

    Price getOfficialBid();

    Price getOfficialAsk();

    BigDecimal getVwap();

    BigDecimal getDividendCash();

    DateTime getDividendDate();

    BigDecimal getTwas();

    BigDecimal getMarketCapitalization();

    BigDecimal getDividendYield();

    BigDecimal getYieldISMA();

    Price getPreviousBid();

    Price getPreviousAsk();

    BigDecimal getModifiedDuration();

    Price getClose();

    Price getLastClose();

    String getLmeSubsystemPrice();

    String getLmeSubsystemBid();

    String getLmeSubsystemAsk();

    BigDecimal getAccruedInterest();

    BigDecimal getUnderlyingReferencePrice();

    BigDecimal getInterimProfit();

    Price getDistributionFund();

    BigDecimal getImpliedVolatility();

    /**
     * @return nominal delay of the data in this record, 0 for realtime and eod; in contrast to actual
     * delay, the nominal delay does not include feed latency or processing delays.
     */
    int getNominalDelayInSeconds();

    BigDecimal getBidAskMidPrice();

    BigDecimal getPreviousBidAskMidPrice();

    BigDecimal getAskHighDay();

    BigDecimal getAskLowDay();

    BigDecimal getBidHighDay();

    BigDecimal getBidLowDay();
}
