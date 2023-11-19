package de.marketmaker.istar.feed.vwd;

import java.math.BigDecimal;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import de.marketmaker.istar.domain.data.Price;
import de.marketmaker.istar.domain.data.PriceQuality;
import de.marketmaker.istar.domain.data.PriceRecord;
import de.marketmaker.istar.domainimpl.data.NullPriceRecord;

/**
 * price record that limits information to < 6 month see: T-44758
 */
public class PriceRecordItraxx implements PriceRecord {

    protected PriceRecord delegate;

    public PriceRecordItraxx(PriceRecord delegate) {
        this.delegate = delegate;
    }


    @Override
    public Price getHighYear() {
        return NullPriceRecord.INSTANCE.getHighYear();
    }

    @Override
    public Price getLowYear() {
        return NullPriceRecord.INSTANCE.getLowYear();
    }

    @Override
    public Price getHigh52W() {
        return NullPriceRecord.INSTANCE.getHigh52W();
    }

    @Override
    public Price getLow52W() {
        return NullPriceRecord.INSTANCE.getLow52W();
    }


    @Override
    public boolean isCurrentDayDefined() {
        return delegate.isCurrentDayDefined();
    }

    @Override
    public Price getValuationPrice() {
        return delegate.getValuationPrice();
    }

    @Override
    public DateTime getDate() {
        return delegate.getDate();
    }

    @Override
    public Price getPrice() {
        return delegate.getPrice();
    }

    @Override
    public Price getAsk() {
        return delegate.getAsk();
    }

    @Override
    public Price getBid() {
        return delegate.getBid();
    }

    @Override
    public Price getLastAsk() {
        return delegate.getLastAsk();
    }

    @Override
    public Price getLastBid() {
        return delegate.getLastBid();
    }

    @Override
    public BigDecimal getSpreadNet() {
        return delegate.getSpreadNet();
    }

    @Override
    public BigDecimal getSpreadPercent() {
        return delegate.getSpreadPercent();
    }

    @Override
    public BigDecimal getSpreadHomogenized() {
        return delegate.getSpreadHomogenized();
    }

    @Override
    public BigDecimal getChangeNet() {
        return delegate.getChangeNet();
    }

    @Override
    public BigDecimal getChangePercent() {
        return delegate.getChangePercent();
    }

    @Override
    public Price getHighDay() {
        return delegate.getHighDay();
    }

    @Override
    public Price getLowDay() {
        return delegate.getLowDay();
    }

    @Override
    public Price getPreviousHighDay() {
        return delegate.getPreviousHighDay();
    }

    @Override
    public Price getPreviousLowDay() {
        return delegate.getPreviousLowDay();
    }

    @Override
    public Long getVolumeDay() {
        return delegate.getVolumeDay();
    }

    @Override
    public Long getPreviousVolumeDay() {
        return delegate.getPreviousVolumeDay();
    }

    @Override
    public BigDecimal getTurnoverDay() {
        return delegate.getTurnoverDay();
    }

    @Override
    public BigDecimal getPreviousTurnoverDay() {
        return delegate.getPreviousTurnoverDay();
    }

    @Override
    public Long getNumberOfTrades() {
        return delegate.getNumberOfTrades();
    }

    @Override
    public Price getKassa() {
        return delegate.getKassa();
    }

    @Override
    public Price getOpen() {
        return delegate.getOpen();
    }

    @Override
    public Price getPreviousOpen() {
        return delegate.getPreviousOpen();
    }

    @Override
    public Price getPreviousClose() {
        return delegate.getValuationPrice();
    }

    @Override
    public BigDecimal getYield() {
        return delegate.getYield();
    }

    @Override
    public Price getYieldPrice() {
        return delegate.getYieldPrice();
    }

    @Override
    public BigDecimal getPreviousYield() {
        return delegate.getYield();
    }

    @Override
    public Price getPreviousYieldPrice() {
        return delegate.getYieldPrice();
    }

    @Override
    public Price getPreviousOfficialBid() {
        return delegate.getPreviousOfficialBid();
    }

    @Override
    public Price getPreviousOfficialAsk() {
        return delegate.getPreviousOfficialAsk();
    }

    @Override
    public Price getUnofficialBid() {
        return delegate.getUnofficialBid();
    }

    @Override
    public Price getUnofficialAsk() {
        return delegate.getUnofficialAsk();
    }

    @Override
    public Price getPreviousUnofficialBid() {
        return delegate.getPreviousUnofficialBid();
    }

    @Override
    public Price getPreviousUnofficialAsk() {
        return delegate.getPreviousUnofficialAsk();
    }

    @Override
    public BigDecimal getInterpolatedClosing() {
        return delegate.getInterpolatedClosing();
    }

    @Override
    public BigDecimal getProvisionalEvaluation() {
        return delegate.getProvisionalEvaluation();
    }

    @Override
    public BigDecimal getBidYield() {
        return delegate.getBidYield();
    }

    @Override
    public BigDecimal getAskYield() {
        return delegate.getAskYield();
    }

    @Override
    public BigDecimal getBrokenPeriodInterest() {
        return delegate.getBrokenPeriodInterest();
    }

    @Override
    public BigDecimal getDuration() {
        return delegate.getDuration();
    }

    @Override
    public BigDecimal getConvexity() {
        return delegate.getConvexity();
    }

    @Override
    public BigDecimal getInterestRateElasticity() {
        return delegate.getInterestRateElasticity();
    }

    @Override
    public BigDecimal getBasePointValue() {
        return delegate.getBasePointValue();
    }

    @Override
    public BigDecimal getCloseBefore(LocalDate date) {
        return delegate.getCloseBefore(date);
    }

    @Override
    public Price getSettlement() {
        return delegate.getSettlement();
    }

    @Override
    public Price getPreviousSettlement() {
        return delegate.getPreviousSettlement();
    }

    @Override
    public Price getOpenInterest() {
        return delegate.getOpenInterest();
    }

    @Override
    public Price getOfficialClose() {
        return delegate.getOfficialClose();
    }

    @Override
    public Price getOfficialBid() {
        return delegate.getOfficialBid();
    }

    @Override
    public Price getOfficialAsk() {
        return delegate.getOfficialAsk();
    }

    @Override
    public BigDecimal getVwap() {
        return delegate.getVwap();
    }

    @Override
    public BigDecimal getDividendCash() {
        return delegate.getDividendCash();
    }

    @Override
    public DateTime getDividendDate() {
        return delegate.getDividendDate();
    }

    @Override
    public BigDecimal getTwas() {
        return delegate.getTwas();
    }

    @Override
    public BigDecimal getMarketCapitalization() {
        return delegate.getMarketCapitalization();
    }

    @Override
    public BigDecimal getDividendYield() {
        return delegate.getDividendYield();
    }

    @Override
    public BigDecimal getYieldISMA() {
        return delegate.getYieldISMA();
    }

    @Override
    public Price getPreviousBid() {
        return delegate.getPreviousBid();
    }

    @Override
    public Price getPreviousAsk() {
        return delegate.getPreviousAsk();
    }

    @Override
    public BigDecimal getModifiedDuration() {
        return delegate.getModifiedDuration();
    }

    @Override
    public Price getClose() {
        return delegate.getClose();
    }

    @Override
    public Price getLastClose() {
        return delegate.getLastClose();
    }

    @Override
    public String getLmeSubsystemPrice() {
        return delegate.getLmeSubsystemPrice();
    }

    @Override
    public String getLmeSubsystemBid() {
        return delegate.getLmeSubsystemBid();
    }

    @Override
    public String getLmeSubsystemAsk() {
        return delegate.getLmeSubsystemAsk();
    }

    @Override
    public BigDecimal getAccruedInterest() {
        return delegate.getAccruedInterest();
    }

    @Override
    public BigDecimal getUnderlyingReferencePrice() {
        return delegate.getUnderlyingReferencePrice();
    }

    @Override
    public BigDecimal getInterimProfit() {
        return delegate.getInterimProfit();
    }

    @Override
    public Price getDistributionFund() {
        return delegate.getDistributionFund();
    }

    @Override
    public BigDecimal getImpliedVolatility() {
        return delegate.getImpliedVolatility();
    }

    @Override
    public int getNominalDelayInSeconds() {
        return delegate.getNominalDelayInSeconds();
    }

    @Override
    public BigDecimal getBidAskMidPrice() {
        return delegate.getBidAskMidPrice();
    }

    @Override
    public BigDecimal getPreviousBidAskMidPrice() {
        return delegate.getPreviousBidAskMidPrice();
    }

    @Override
    public BigDecimal getAskHighDay() {
        return delegate.getAskHighDay();
    }

    @Override
    public BigDecimal getAskLowDay() {
        return delegate.getAskLowDay();
    }

    @Override
    public BigDecimal getBidHighDay() {
        return delegate.getBidHighDay();
    }

    @Override
    public BigDecimal getBidLowDay() {
        return delegate.getBidLowDay();
    }

    @Override
    public PriceQuality getPriceQuality() {
        return delegate.getPriceQuality();
    }

    @Override
    public boolean isPushAllowed() {
        return delegate.isPushAllowed();
    }

}
