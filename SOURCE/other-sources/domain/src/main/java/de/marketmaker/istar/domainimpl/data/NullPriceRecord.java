/*
 * NullPriceRecord.java
 *
 * Created on 07.07.2006 14:18:00
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domainimpl.data;

import java.io.Serializable;
import java.math.BigDecimal;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import de.marketmaker.istar.domain.data.Price;
import de.marketmaker.istar.domain.data.PriceQuality;
import de.marketmaker.istar.domain.data.PriceRecord;
import de.marketmaker.istar.domain.data.PriceRecordFund;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class NullPriceRecord implements PriceRecord, PriceRecordFund, Serializable {
    protected static final long serialVersionUID = -2436856545420467633L;

    public static final PriceRecord INSTANCE = new NullPriceRecord();

    private NullPriceRecord() {
    }

    public String toString() {
        return "NullPriceRecord[]";
    }

    protected Object readResolve() {
        return INSTANCE;
    }

    public Price getKassa() {
        return NullPrice.INSTANCE;
    }

    public Price getOpen() {
        return NullPrice.INSTANCE;
    }

    public Price getPreviousClose() {
        return NullPrice.INSTANCE;
    }

    public Long getPreviousVolumeDay() {
        return null;
    }

    public BigDecimal getYield() {
        return null;
    }

    public Price getYieldPrice() {
        return NullPrice.INSTANCE;
    }

    public BigDecimal getPreviousYield() {
        return null;
    }

    public Price getPreviousYieldPrice() {
        return NullPrice.INSTANCE;
    }

    public BigDecimal getBidYield() {
        return null;
    }

    public BigDecimal getAskYield() {
        return null;
    }

    public Price getAsk() {
        return NullPrice.INSTANCE;
    }

    public Price getBid() {
        return NullPrice.INSTANCE;
    }

    public Price getLastAsk() {
        return NullPrice.INSTANCE;
    }

    public Price getLastBid() {
        return NullPrice.INSTANCE;
    }

    public Price getPrice() {
        return NullPrice.INSTANCE;
    }

    public PriceQuality getPriceQuality() {
        return PriceQuality.NONE;
    }

    public boolean isPushAllowed() {
        return false;
    }

    public boolean isCurrentDayDefined() {
        return false;
    }

    public Price getValuationPrice() {
        return NullPrice.INSTANCE;
    }

    public DateTime getDate() {
        return null;
    }

    public BigDecimal getSpreadNet() {
        return null;
    }

    public BigDecimal getSpreadPercent() {
        return null;
    }

    public BigDecimal getSpreadHomogenized() {
        return null;
    }

    public BigDecimal getChangeNet() {
        return null;
    }

    public BigDecimal getChangePercent() {
        return null;
    }

    public Price getHighDay() {
        return NullPrice.INSTANCE;
    }

    public Price getLowDay() {
        return NullPrice.INSTANCE;
    }

    public Price getHighYear() {
        return NullPrice.INSTANCE;
    }

    public Price getLowYear() {
        return NullPrice.INSTANCE;
    }

    public Price getHigh52W() {
        return NullPrice.INSTANCE;
    }

    public Price getLow52W() {
        return NullPrice.INSTANCE;
    }

    public Long getVolumeDay() {
        return null;
    }

    public BigDecimal getTurnoverDay() {
        return null;
    }

    public Long getNumberOfTrades() {
        return null;
    }

    public BigDecimal getBrokenPeriodInterest() {
        return null;
    }

    public BigDecimal getDuration() {
        return null;
    }

    public BigDecimal getConvexity() {
        return null;
    }

    public BigDecimal getInterestRateElasticity() {
        return null;
    }

    public BigDecimal getBasePointValue() {
        return null;
    }

    public BigDecimal getCloseBefore(LocalDate date) {
        return null;
    }

    public Price getSettlement() {
        return NullPrice.INSTANCE;
    }

    public Price getPreviousSettlement() {
        return NullPrice.INSTANCE;
    }

    public Price getOpenInterest() {
        return NullPrice.INSTANCE;
    }

    public Price getIssuePrice() {
        return NullPrice.INSTANCE;
    }

    public Price getPreviousIssuePrice() {
        return NullPrice.INSTANCE;
    }

    public Price getRedemptionPrice() {
        return NullPrice.INSTANCE;
    }

    public Price getPreviousRedemptionPrice() {
        return NullPrice.INSTANCE;
    }

    public Price getOfficialClose() {
        return null;
    }

    public Price getOfficialBid() {
        return null;
    }

    public Price getOfficialAsk() {
        return null;
    }

    public Price getPreviousOfficialBid() {
        return null;
    }

    public Price getPreviousOfficialAsk() {
        return null;
    }

    public Price getUnofficialBid() {
        return null;
    }

    public Price getUnofficialAsk() {
        return null;
    }

    public Price getPreviousUnofficialBid() {
        return null;
    }

    public Price getPreviousUnofficialAsk() {
        return null;
    }

    public BigDecimal getInterpolatedClosing() {
        return null;
    }

    public BigDecimal getProvisionalEvaluation() {
        return null;
    }

    public Price getPreviousHighDay() {
        return NullPrice.INSTANCE;
    }

    public Price getPreviousLowDay() {
        return NullPrice.INSTANCE;
    }

    public BigDecimal getPreviousTurnoverDay() {
        return null;
    }

    public Price getPreviousOpen() {
        return NullPrice.INSTANCE;
    }

    public BigDecimal getVwap() {
        return null;
    }

    public BigDecimal getDividendCash() {
        return null;
    }

    public DateTime getDividendDate() {
        return null;
    }

    public BigDecimal getTwas() {
        return null;
    }

    public BigDecimal getMarketCapitalization() {
        return null;
    }

    public BigDecimal getDividendYield() {
        return null;
    }

    public BigDecimal getYieldISMA() {
        return null;
    }

    public Price getPreviousBid() {
        return NullPrice.INSTANCE;
    }

    public Price getPreviousAsk() {
        return NullPrice.INSTANCE;
    }

    public BigDecimal getModifiedDuration() {
        return null;
    }

    public Price getClose() {
        return NullPrice.INSTANCE;
    }

    public Price getLastClose() {
        return NullPrice.INSTANCE;
    }

    public String getLmeSubsystemPrice() {
        return null;
    }

    public String getLmeSubsystemBid() {
        return null;
    }

    public String getLmeSubsystemAsk() {
        return null;
    }

    public Price getNetAssetValue() {
        return NullPrice.INSTANCE;
    }

    public Price getPreviousNetAssetValue() {
        return NullPrice.INSTANCE;
    }

    public BigDecimal getAccruedInterest() {
        return null;
    }

    public BigDecimal getUnderlyingReferencePrice() {
        return null;
    }

    public BigDecimal getInterimProfit() {
        return null;
    }

    public Price getDistributionFund() {
        return null;
    }

    public BigDecimal getImpliedVolatility() {
        return null;
    }

    @Override
    public int getNominalDelayInSeconds() {
        return 0;
    }

    @Override
    public BigDecimal getBidAskMidPrice() {
        return null;
    }

    @Override
    public BigDecimal getPreviousBidAskMidPrice() {
        return null;
    }

    @Override
    public BigDecimal getAskHighDay() {
        return null;
    }

    @Override
    public BigDecimal getAskLowDay() {
        return null;
    }

    @Override
    public BigDecimal getBidHighDay() {
        return null;
    }

    @Override
    public BigDecimal getBidLowDay() {
        return null;
    }
}
