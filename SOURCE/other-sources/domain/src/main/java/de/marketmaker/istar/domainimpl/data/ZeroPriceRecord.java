/*
 * ZeroPriceRecord.java
 *
 * Created on 12.12.2006 10:12:38
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
 * A PriceRecord that returns ZeroPrice or BigDecimal.ZERO for all prices.
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class ZeroPriceRecord implements PriceRecord, PriceRecordFund, Serializable {
    protected static final long serialVersionUID = -2436856545420467633L;

    public static final PriceRecord INSTANCE = new ZeroPriceRecord();

    private ZeroPriceRecord() {
    }

    public String toString() {
        return "ZeroPriceRecord[]";
    }

    public Price getKassa() {
        return ZeroPrice.INSTANCE;
    }

    public Price getOpen() {
        return ZeroPrice.INSTANCE;
    }

    public Price getPreviousClose() {
        return ZeroPrice.INSTANCE;
    }

    public Long getPreviousVolumeDay() {
        return 0L;
    }

    public BigDecimal getYield() {
        return BigDecimal.ZERO;
    }

    public Price getYieldPrice() {
        return ZeroPrice.INSTANCE;
    }

    public BigDecimal getPreviousYield() {
        return BigDecimal.ZERO;
    }

    public Price getPreviousYieldPrice() {
        return ZeroPrice.INSTANCE;
    }

    public BigDecimal getBidYield() {
        return BigDecimal.ZERO;
    }

    public BigDecimal getAskYield() {
        return BigDecimal.ZERO;
    }

    public Price getAsk() {
        return ZeroPrice.INSTANCE;
    }

    public Price getBid() {
        return ZeroPrice.INSTANCE;
    }

    public Price getLastAsk() {
        return ZeroPrice.INSTANCE;
    }

    public Price getLastBid() {
        return ZeroPrice.INSTANCE;
    }

    public Price getPrice() {
        return ZeroPrice.INSTANCE;
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
        return ZeroPrice.INSTANCE;
    }

    public DateTime getDate() {
        return null;
    }

    public BigDecimal getSpreadNet() {
        return BigDecimal.ZERO;
    }

    public BigDecimal getSpreadPercent() {
        return BigDecimal.ZERO;
    }

    public BigDecimal getSpreadHomogenized() {
        return BigDecimal.ZERO;
    }

    public BigDecimal getChangeNet() {
        return BigDecimal.ZERO;
    }

    public BigDecimal getChangePercent() {
        return BigDecimal.ZERO;
    }

    public Price getHighDay() {
        return ZeroPrice.INSTANCE;
    }

    public Price getLowDay() {
        return ZeroPrice.INSTANCE;
    }

    public Price getHighYear() {
        return ZeroPrice.INSTANCE;
    }

    public Price getLowYear() {
        return ZeroPrice.INSTANCE;
    }

    public Price getLow52W() {
        return ZeroPrice.INSTANCE;
    }

    public Price getHigh52W() {
        return ZeroPrice.INSTANCE;
    }

    public Long getVolumeDay() {
        return 0L;
    }

    public BigDecimal getTurnoverDay() {
        return BigDecimal.ZERO;
    }

    public Long getNumberOfTrades() {
        return 0L;
    }

    public BigDecimal getBrokenPeriodInterest() {
        return BigDecimal.ZERO;
    }

    public BigDecimal getDuration() {
        return BigDecimal.ZERO;
    }

    public BigDecimal getConvexity() {
        return BigDecimal.ZERO;
    }

    public BigDecimal getInterestRateElasticity() {
        return BigDecimal.ZERO;
    }

    public BigDecimal getBasePointValue() {
        return BigDecimal.ZERO;
    }

    public BigDecimal getCloseBefore(LocalDate date) {
        return BigDecimal.ZERO;
    }

    public Price getSettlement() {
        return ZeroPrice.INSTANCE;
    }

    public Price getPreviousSettlement() {
        return ZeroPrice.INSTANCE;
    }

    public Price getOpenInterest() {
        return ZeroPrice.INSTANCE;
    }

    public Price getOfficialClose() {
        return ZeroPrice.INSTANCE;
    }

    public Price getOfficialBid() {
        return ZeroPrice.INSTANCE;
    }

    public Price getOfficialAsk() {
        return ZeroPrice.INSTANCE;
    }

    public Price getPreviousOfficialBid() {
        return ZeroPrice.INSTANCE;
    }

    public Price getPreviousOfficialAsk() {
        return ZeroPrice.INSTANCE;
    }

    public Price getUnofficialBid() {
        return ZeroPrice.INSTANCE;
    }

    public Price getUnofficialAsk() {
        return ZeroPrice.INSTANCE;
    }

    public Price getPreviousUnofficialBid() {
        return ZeroPrice.INSTANCE;
    }

    public Price getPreviousUnofficialAsk() {
        return ZeroPrice.INSTANCE;
    }

    public BigDecimal getInterpolatedClosing() {
        return BigDecimal.ZERO;
    }

    public BigDecimal getProvisionalEvaluation() {
        return BigDecimal.ZERO;
    }

    public Price getPreviousHighDay() {
        return ZeroPrice.INSTANCE;
    }

    public Price getPreviousLowDay() {
        return ZeroPrice.INSTANCE;
    }

    public BigDecimal getPreviousTurnoverDay() {
        return BigDecimal.ZERO;
    }

    public Price getPreviousOpen() {
        return ZeroPrice.INSTANCE;
    }

    public BigDecimal getVwap() {
        return BigDecimal.ZERO;
    }

    public BigDecimal getDividendCash() {
        return BigDecimal.ZERO;
    }

    public DateTime getDividendDate() {
        return null;
    }

    public BigDecimal getTwas() {
        return BigDecimal.ZERO;
    }

    public BigDecimal getMarketCapitalization() {
        return BigDecimal.ZERO;
    }

    public BigDecimal getDividendYield() {
        return BigDecimal.ZERO;
    }

    public BigDecimal getYieldISMA() {
        return BigDecimal.ZERO;
    }

    public Price getPreviousAsk() {
        return ZeroPrice.INSTANCE;
    }

    public BigDecimal getModifiedDuration() {
        return BigDecimal.ZERO;
    }

    public Price getClose() {
        return ZeroPrice.INSTANCE;
    }

    public Price getLastClose() {
        return ZeroPrice.INSTANCE;
    }

    public Price getPreviousBid() {
        return ZeroPrice.INSTANCE;
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

    public Price getIssuePrice() {
        return ZeroPrice.INSTANCE;
    }

    public Price getPreviousIssuePrice() {
        return ZeroPrice.INSTANCE;
    }

    public Price getRedemptionPrice() {
        return ZeroPrice.INSTANCE;
    }

    public Price getPreviousRedemptionPrice() {
        return ZeroPrice.INSTANCE;
    }

    public Price getPreviousNetAssetValue() {
        return ZeroPrice.INSTANCE;
    }

    public Price getNetAssetValue() {
        return ZeroPrice.INSTANCE;
    }

    public BigDecimal getAccruedInterest() {
        return BigDecimal.ZERO;
    }

    public BigDecimal getUnderlyingReferencePrice() {
        return BigDecimal.ZERO;
    }

    public BigDecimal getInterimProfit() {
        return BigDecimal.ZERO;
    }

    public Price getDistributionFund() {
        return ZeroPrice.INSTANCE;
    }

    public BigDecimal getImpliedVolatility() {
        return BigDecimal.ZERO;
    }

    @Override
    public int getNominalDelayInSeconds() {
        return 0;
    }

    @Override
    public BigDecimal getBidAskMidPrice() {
        return BigDecimal.ZERO;
    }

    @Override
    public BigDecimal getPreviousBidAskMidPrice() {
        return BigDecimal.ZERO;
    }

    @Override
    public BigDecimal getAskHighDay() {
        return BigDecimal.ZERO;
    }

    @Override
    public BigDecimal getAskLowDay() {
        return BigDecimal.ZERO;
    }

    @Override
    public BigDecimal getBidHighDay() {
        return BigDecimal.ZERO;
    }

    @Override
    public BigDecimal getBidLowDay() {
        return BigDecimal.ZERO;
    }
}
