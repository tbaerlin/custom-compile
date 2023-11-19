/*
 * PriceRecordImpl.java
 *
 * Created on 08.08.2006 15:06:31
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domainimpl.data;

import java.math.BigDecimal;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import de.marketmaker.istar.domain.data.Price;
import de.marketmaker.istar.domain.data.PriceQuality;
import de.marketmaker.istar.domain.data.PriceRecord;

/**
 * PriceRecord with setters for all properties, mainly useful for test cases.
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class PriceRecordImpl implements PriceRecord {
    private Price ask;

    private BigDecimal basePointValue;

    private Price bid;

    private BigDecimal brokenPeriodInterest;

    private BigDecimal changeNet;

    private BigDecimal changePercent;

    private BigDecimal convexity;

    private BigDecimal duration;

    private Price highDay;

    private Price highYear;

    private BigDecimal interestRateElasticity;

    private Price kassa;

    private Price lowDay;

    private Price lowYear;

    private Price high52W;

    private Price low52W;

    private Long numberOfTrades;

    private Price open;

    private Price previousClose;

    private Long previousVolumeDay;

    private Price valuationPrice;

    private Price valuationPriceEoD;

    private DateTime date;

    private Price price;

    private BigDecimal spreadHomogenized;

    private BigDecimal spreadNet;

    private BigDecimal spreadPercent;

    private Long volumeDay;

    private Price yield;

    private Price previousYield;

    private BigDecimal bidYield;

    private BigDecimal askYield;

    private BigDecimal closeBefore;

    private PriceQuality priceQuality;

    private boolean pushAllowed;

    private Price openInterest;

    private Price settlement;

    private BigDecimal turnoverDay;

    private BigDecimal interimProfit;

    private Price distributionFund;

    private Price officialClose;

    private Price officialBid;

    private Price officialAsk;

    private Price previousSettlement;

    private Price previousHighDay;

    private Price previousLowDay;

    private BigDecimal previousTurnoverDay;

    private Price previousOpen;

    private Price previousBid;

    private Price previousAsk;

    private BigDecimal vwap;

    private BigDecimal twas;

    private BigDecimal marketCapitalization;

    private BigDecimal dividendYield;

    private BigDecimal yieldISMA;

    private BigDecimal modifiedDuration;

    private Price close;

    private Price lastClose;

    private String lmeSubsystemPrice;

    private String lmeSubsystemBid;

    private String lmeSubsystemAsk;

    private boolean currentDayDefined;

    private BigDecimal accruedInterest;

    private BigDecimal underlyingReferencePrice;

    private BigDecimal impliedVolatility;

    private int nominalDelayInSeconds;

    private BigDecimal dividendCash;

    private DateTime dividendDate;

    private Price previousOfficialBid;

    private Price previousOfficialAsk;

    private Price unofficialBid;

    private Price unofficialAsk;

    private BigDecimal interpolatedClosing;

    private Price previousUnofficialAsk;

    private Price previousUnofficialBid;

    private BigDecimal provisionalEvaluation;

    private BigDecimal bidAskMidPrice;

    private BigDecimal previousBidAskMidPrice;

    private BigDecimal askHighDay;

    private BigDecimal askLowDay;

    private BigDecimal bidHighDay;

    private BigDecimal bidLowDay;

    public Price getAsk() {
        return this.ask;
    }

    @Override
    public Price getLastAsk() {
        return getAsk();
    }

    public BigDecimal getBasePointValue() {
        return this.basePointValue;
    }

    public BigDecimal getCloseBefore(LocalDate date) {
        return this.closeBefore;
    }

    public void setOpenInterest(Price openInterest) {
        this.openInterest = openInterest;
    }

    public void setSettlement(Price settlement) {
        this.settlement = settlement;
    }

    public Price getSettlement() {
        return this.settlement;
    }

    public void setPreviousSettlement(Price previousSettlement) {
        this.previousSettlement = previousSettlement;
    }

    public Price getPreviousSettlement() {
        return this.previousSettlement;
    }

    public Price getOpenInterest() {
        return this.openInterest;
    }

    public void setCloseBefore(BigDecimal closeBefore) {
        this.closeBefore = closeBefore;
    }

    public Price getBid() {
        return this.bid;
    }

    @Override
    public Price getLastBid() {
        return getBid();
    }

    public BigDecimal getBrokenPeriodInterest() {
        return this.brokenPeriodInterest;
    }

    public BigDecimal getChangeNet() {
        return this.changeNet;
    }

    public BigDecimal getChangePercent() {
        return this.changePercent;
    }

    public BigDecimal getConvexity() {
        return this.convexity;
    }

    public BigDecimal getDuration() {
        return this.duration;
    }

    public Price getHighDay() {
        return this.highDay;
    }

    public Price getHighYear() {
        return this.highYear;
    }

    public BigDecimal getInterestRateElasticity() {
        return this.interestRateElasticity;
    }

    public Price getKassa() {
        return this.kassa;
    }

    public Price getLowDay() {
        return this.lowDay;
    }

    public Price getLowYear() {
        return this.lowYear;
    }

    public Price getHigh52W() {
        return this.high52W;
    }

    public Price getLow52W() {
        return this.low52W;
    }

    public Long getNumberOfTrades() {
        return this.numberOfTrades;
    }

    public Price getOpen() {
        return this.open;
    }

    public void setPreviousClose(Price previousClose) {
        this.previousClose = previousClose;
    }

    public Price getPreviousClose() {
        return this.previousClose;
    }

    public Long getPreviousVolumeDay() {
        return this.previousVolumeDay;
    }

    public void setPreviousVolumeDay(Long previousVolumeDay) {
        this.previousVolumeDay = previousVolumeDay;
    }

    public void setValuationPrice(Price valuationPrice) {
        this.valuationPrice = valuationPrice;
    }

    public PriceQuality getPriceQuality() {
        return this.priceQuality;
    }

    public boolean isPushAllowed() {
        return this.pushAllowed;
    }

    public void setPushAllowed(boolean pushAllowed) {
        this.pushAllowed = pushAllowed;
    }

    public boolean isCurrentDayDefined() {
        return this.currentDayDefined;
    }

    public void setCurrentDayDefined(boolean currentDayDefined) {
        this.currentDayDefined = currentDayDefined;
    }

    public Price getValuationPrice() {
        return this.valuationPrice;
    }

    public DateTime getDate() {
        return this.date;
    }

    public Price getPrice() {
        return this.price;
    }

    public BigDecimal getSpreadHomogenized() {
        return this.spreadHomogenized;
    }

    public BigDecimal getSpreadNet() {
        return this.spreadNet;
    }

    public BigDecimal getSpreadPercent() {
        return this.spreadPercent;
    }

    public Long getVolumeDay() {
        return this.volumeDay;
    }

    public void setTurnoverDay(BigDecimal turnoverDay) {
        this.turnoverDay = turnoverDay;
    }

    public BigDecimal getTurnoverDay() {
        return this.turnoverDay;
    }

    public BigDecimal getYield() {
        return this.yield.getValue();
    }

    public Price getYieldPrice() {
        return this.yield;
    }

    public BigDecimal getPreviousYield() {
        return this.previousYield.getValue();
    }

    public Price getPreviousYieldPrice() {
        return this.previousYield;
    }

    public BigDecimal getBidYield() {
        return this.bidYield;
    }

    public BigDecimal getAskYield() {
        return this.askYield;
    }

    public PriceRecordImpl setAsk(Price ask) {
        this.ask = ask;
        return this;
    }

    public PriceRecordImpl setBasePointValue(BigDecimal basePointValue) {
        this.basePointValue = basePointValue;
        return this;
    }

    public PriceRecordImpl setBid(Price bid) {
        this.bid = bid;
        return this;
    }

    public PriceRecordImpl setBrokenPeriodInterest(BigDecimal brokenPeriodInterest) {
        this.brokenPeriodInterest = brokenPeriodInterest;
        return this;
    }

    public PriceRecordImpl setChangeNet(BigDecimal changeNet) {
        this.changeNet = changeNet;
        return this;
    }

    public PriceRecordImpl setChangePercent(BigDecimal changePercent) {
        this.changePercent = changePercent;
        return this;
    }

    public PriceRecordImpl setConvexity(BigDecimal convexity) {
        this.convexity = convexity;
        return this;
    }

    public PriceRecordImpl setDuration(BigDecimal duration) {
        this.duration = duration;
        return this;
    }

    public PriceRecordImpl setHighDay(Price highDay) {
        this.highDay = highDay;
        return this;
    }

    public PriceRecordImpl setHighYear(Price highYear) {
        this.highYear = highYear;
        return this;
    }

    public PriceRecordImpl setInterestRateElasticity(BigDecimal interestRateElasticity) {
        this.interestRateElasticity = interestRateElasticity;
        return this;
    }

    public PriceRecordImpl setKassa(Price kassa) {
        this.kassa = kassa;
        return this;
    }

    public PriceRecordImpl setLowDay(Price lowDay) {
        this.lowDay = lowDay;
        return this;
    }

    public PriceRecordImpl setLowYear(Price lowYear) {
        this.lowYear = lowYear;
        return this;
    }

    public void setHigh52W(Price high52W) {
        this.high52W = high52W;
    }

    public void setLow52W(Price low52W) {
        this.low52W = low52W;
    }

    public PriceRecordImpl setNumberOfTrades(Long numberOfTrades) {
        this.numberOfTrades = numberOfTrades;
        return this;
    }

    public PriceRecordImpl setOpen(Price open) {
        this.open = open;
        return this;
    }

    public PriceRecordImpl setDate(DateTime date) {
        this.date = date;
        return this;
    }

    public PriceRecordImpl setPrice(Price price) {
        this.price = price;
        return this;
    }

    public PriceRecordImpl setSpreadHomogenized(BigDecimal spreadHomogenized) {
        this.spreadHomogenized = spreadHomogenized;
        return this;
    }

    public PriceRecordImpl setSpreadNet(BigDecimal spreadNet) {
        this.spreadNet = spreadNet;
        return this;
    }

    public PriceRecordImpl setSpreadPercent(BigDecimal spreadPercent) {
        this.spreadPercent = spreadPercent;
        return this;
    }

    public PriceRecordImpl setVolumeDay(Long volumeDay) {
        this.volumeDay = volumeDay;
        return this;
    }

    public PriceRecordImpl setYield(Price yield) {
        this.yield = yield;
        return this;
    }

    public PriceRecordImpl setPreviousYield(Price previousYield) {
        this.previousYield = previousYield;
        return this;
    }

    public PriceRecordImpl setBidYield(BigDecimal bidYield) {
        this.bidYield = bidYield;
        return this;
    }

    public PriceRecordImpl setAskYield(BigDecimal askYield) {
        this.askYield = askYield;
        return this;
    }

    public PriceRecordImpl setPriceQuality(PriceQuality priceQuality) {
        this.priceQuality = priceQuality;
        return this;
    }

    public Price getOfficialClose() {
        return this.officialClose;
    }

    public PriceRecordImpl setOfficialClose(Price officialClose) {
        this.officialClose = officialClose;
        return this;
    }

    public Price getOfficialBid() {
        return this.officialBid;
    }

    public PriceRecordImpl setOfficialBid(Price officialBid) {
        this.officialBid = officialBid;
        return this;
    }

    public Price getOfficialAsk() {
        return this.officialAsk;
    }

    public PriceRecordImpl setOfficialAsk(Price officialAsk) {
        this.officialAsk = officialAsk;
        return this;
    }

    public Price getPreviousOfficialBid() {
        return this.previousOfficialBid;
    }

    public Price getPreviousOfficialAsk() {
        return this.previousOfficialAsk;
    }

    public Price getUnofficialBid() {
        return this.unofficialBid;
    }

    public Price getUnofficialAsk() {
        return this.unofficialAsk;
    }

    public Price getPreviousUnofficialBid() {
        return this.previousUnofficialBid;
    }

    public Price getPreviousUnofficialAsk() {
        return this.previousUnofficialAsk;
    }

    public BigDecimal getInterpolatedClosing() {
        return this.interpolatedClosing;
    }

    public BigDecimal getProvisionalEvaluation() {
        return this.provisionalEvaluation;
    }

    public PriceRecordImpl setPreviousOfficialBid(Price previousOfficialBid) {
        this.previousOfficialBid = previousOfficialBid;
        return this;
    }

    public PriceRecordImpl setPreviousOfficialAsk(Price previousOfficialAsk) {
        this.previousOfficialAsk = previousOfficialAsk;
        return this;
    }

    public PriceRecordImpl setUnofficialBid(Price unofficialBid) {
        this.unofficialBid = unofficialBid;
        return this;
    }

    public PriceRecordImpl setUnofficialAsk(Price unofficialAsk) {
        this.unofficialAsk = unofficialAsk;
        return this;
    }

    public PriceRecordImpl setInterpolatedClosing(BigDecimal interpolatedClosing) {
        this.interpolatedClosing = interpolatedClosing;
        return this;
    }

    public PriceRecordImpl setPreviousUnofficialAsk(Price previousUnofficialAsk) {
        this.previousUnofficialAsk = previousUnofficialAsk;
        return this;
    }

    public PriceRecordImpl setPreviousUnofficialBid(Price previousUnofficialBid) {
        this.previousUnofficialBid = previousUnofficialBid;
        return this;
    }

    public PriceRecordImpl setProvisionalEvaluation(BigDecimal provisionalEvaluation) {
        this.provisionalEvaluation = provisionalEvaluation;
        return this;
    }

    public Price getPreviousHighDay() {
        return previousHighDay;
    }

    public PriceRecordImpl setPreviousHighDay(Price previousHighDay) {
        this.previousHighDay = previousHighDay;
        return this;
    }

    public Price getPreviousLowDay() {
        return previousLowDay;
    }

    public PriceRecordImpl setPreviousLowDay(Price previousLowDay) {
        this.previousLowDay = previousLowDay;
        return this;
    }

    public BigDecimal getPreviousTurnoverDay() {
        return previousTurnoverDay;
    }

    public PriceRecordImpl setPreviousTurnoverDay(BigDecimal previousTurnoverDay) {
        this.previousTurnoverDay = previousTurnoverDay;
        return this;
    }

    public Price getPreviousOpen() {
        return previousOpen;
    }

    public PriceRecordImpl setPreviousOpen(Price previousOpen) {
        this.previousOpen = previousOpen;
        return this;
    }

    public BigDecimal getVwap() {
        return vwap;
    }

    public PriceRecordImpl setVwap(BigDecimal vwap) {
        this.vwap = vwap;
        return this;
    }

    public BigDecimal getTwas() {
        return twas;
    }

    public PriceRecordImpl setTwas(BigDecimal twas) {
        this.twas = twas;
        return this;
    }

    public BigDecimal getMarketCapitalization() {
        return marketCapitalization;
    }

    public PriceRecordImpl setMarketCapitalization(BigDecimal marketCapitalization) {
        this.marketCapitalization = marketCapitalization;
        return this;
    }

    public BigDecimal getDividendYield() {
        return dividendYield;
    }

    public BigDecimal getDividendCash() {
        return dividendCash;
    }

    public PriceRecordImpl setDividendCash(BigDecimal dividendCash) {
        this.dividendCash = dividendCash;
        return this;
    }

    public DateTime getDividendDate() {
        return dividendDate;
    }

    public PriceRecordImpl setDividendDate(DateTime dividendDate) {
        this.dividendDate = dividendDate;
        return this;
    }

    public void setYieldISMA(BigDecimal yieldISMA) {
        this.yieldISMA = yieldISMA;
    }

    public BigDecimal getYieldISMA() {
        return yieldISMA;
    }

    public Price getPreviousBid() {
        return this.previousBid;
    }

    public PriceRecordImpl setPreviousBid(Price previousBid) {
        this.previousBid = previousBid;
        return this;
    }

    public PriceRecordImpl setPreviousAsk(Price previousAsk) {
        this.previousAsk = previousAsk;
        return this;
    }

    public Price getPreviousAsk() {
        return this.previousAsk;
    }

    public PriceRecordImpl setModifiedDuration(BigDecimal modifiedDuration) {
        this.modifiedDuration = modifiedDuration;
        return this;
    }

    public BigDecimal getModifiedDuration() {
        return this.modifiedDuration;
    }

    public Price getClose() {
        return this.close;
    }

    public PriceRecordImpl setClose(Price close) {
        this.close = close;
        return this;
    }

    public Price getLastClose() {
        return this.lastClose;
    }

    public PriceRecordImpl setLastClose(Price lastClose) {
        this.lastClose = lastClose;
        return this;
    }

    public PriceRecordImpl setDividendYield(BigDecimal dividendYield) {
        this.dividendYield = dividendYield;
        return this;
    }

    public PriceRecordImpl setLmeSubsystemPrice(String lmeSubsystemPrice) {
        this.lmeSubsystemPrice = lmeSubsystemPrice;
        return this;
    }

    public String getLmeSubsystemPrice() {
        return this.lmeSubsystemPrice;
    }

    public String getLmeSubsystemBid() {
        return this.lmeSubsystemBid;
    }

    public String getLmeSubsystemAsk() {
        return this.lmeSubsystemAsk;
    }

    public PriceRecordImpl setValuationPriceEoD(Price valuationPriceEoD) {
        this.valuationPriceEoD = valuationPriceEoD;
        return this;
    }

    public PriceRecordImpl setLmeSubsystemBid(String lmeSubsystemBid) {
        this.lmeSubsystemBid = lmeSubsystemBid;
        return this;
    }

    public PriceRecordImpl setLmeSubsystemAsk(String lmeSubsystemAsk) {
        this.lmeSubsystemAsk = lmeSubsystemAsk;
        return this;
    }


    public PriceRecordImpl setAccruedInterest(BigDecimal accruedInterest) {
        this.accruedInterest = accruedInterest;
        return this;
    }

    public BigDecimal getAccruedInterest() {
        return this.accruedInterest;
    }

    public PriceRecordImpl setUnderlyingReferencePrice(BigDecimal underlyingReferencePrice) {
        this.underlyingReferencePrice = underlyingReferencePrice;
        return this;
    }

    public BigDecimal getUnderlyingReferencePrice() {
        return this.underlyingReferencePrice;
    }

    public BigDecimal getInterimProfit() {
        return interimProfit;
    }

    public void setInterimProfit(BigDecimal interimProfit) {
        this.interimProfit = interimProfit;
    }

    public Price getDistributionFund() {
        return distributionFund;
    }

    public void setDistributionFund(Price distributionFund) {
        this.distributionFund = distributionFund;
    }

    public BigDecimal getImpliedVolatility() {
        return impliedVolatility;
    }

    public void setNominalDelayInSeconds(int nominalDelayInSeconds) {
        this.nominalDelayInSeconds = nominalDelayInSeconds;
    }

    public int getNominalDelayInSeconds() {
        return this.nominalDelayInSeconds;
    }

    public PriceRecordImpl setImpliedVolatility(BigDecimal impliedVolatility) {
        this.impliedVolatility = impliedVolatility;
        return this;
    }

    @Override
    public BigDecimal getBidAskMidPrice() {
        return bidAskMidPrice;
    }

    public void setBidAskMidPrice(BigDecimal bidAskMidPrice) {
        this.bidAskMidPrice = bidAskMidPrice;
    }

    @Override
    public BigDecimal getPreviousBidAskMidPrice() {
        return previousBidAskMidPrice;
    }

    public void setPreviousBidAskMidPrice(BigDecimal previousBidAskMidPrice) {
        this.previousBidAskMidPrice = previousBidAskMidPrice;
    }

    @Override
    public BigDecimal getAskHighDay() {
        return askHighDay;
    }

    public void setAskHighDay(BigDecimal askHighDay) {
        this.askHighDay = askHighDay;
    }

    @Override
    public BigDecimal getAskLowDay() {
        return askLowDay;
    }

    public void setAskLowDay(BigDecimal askLowDay) {
        this.askLowDay = askLowDay;
    }

    @Override
    public BigDecimal getBidHighDay() {
        return bidHighDay;
    }

    public void setBidHighDay(BigDecimal bidHighDay) {
        this.bidHighDay = bidHighDay;
    }

    @Override
    public BigDecimal getBidLowDay() {
        return bidLowDay;
    }

    public void setBidLowDay(BigDecimal bidLowDay) {
        this.bidLowDay = bidLowDay;
    }


}
