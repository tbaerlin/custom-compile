/*
 * BasicHistoricRatiosImpl.java
 *
 * Created on 17.07.2006 17:39:07
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domainimpl.data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

import org.joda.time.Interval;
import org.joda.time.LocalDate;

import de.marketmaker.istar.domain.data.BasicHistoricRatios;
import de.marketmaker.istar.domain.data.PriceRecord;
import de.marketmaker.istar.domain.data.Price;
import de.marketmaker.istar.domain.data.PriceRecordFund;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class BasicHistoricRatiosImpl implements BasicHistoricRatios, Serializable {
    public static final MathContext MC = new MathContext(8, RoundingMode.HALF_EVEN);

    protected static final long serialVersionUID = 1L;

    private final Interval reference;

    private final BigDecimal volatility;

    private final BigDecimal firstPrice;

    private final BigDecimal currentPrice;

    private final BigDecimal firstPriceBenchmark;

    private final BigDecimal currentPriceBenchmark;

    private final BigDecimal alpha;

    private final BigDecimal beta;

    private final BigDecimal correlation;

    private final BigDecimal trackingError;

    private final BigDecimal priceSum;

    private final BigDecimal priceLength;

    private final BigDecimal volumeLength;

    private final BigDecimal volumeSum;

    private final BigDecimal sharpeRatio;

    private final BigDecimal maximumLossPercent;

    private final Price high;

    private final Price low;

    public BasicHistoricRatiosImpl(Interval reference, BigDecimal priceLength, BigDecimal priceSum,
            BigDecimal volumeLength, BigDecimal volumeSum,
            BigDecimal sharpeRatio, BigDecimal volatility,
            BigDecimal maximumLossPercent, BigDecimal firstPrice, BigDecimal currentPrice,
            BigDecimal firstPriceBenchmark, BigDecimal currentPriceBenchmark,
            BigDecimal alpha, BigDecimal beta, BigDecimal correlation, BigDecimal trackingError, Price high,
            Price low) {
        this.reference = reference;
        this.priceLength = priceLength;
        this.priceSum = priceSum;
        this.volumeLength = volumeLength;
        this.volumeSum = volumeSum;
        this.sharpeRatio = sharpeRatio;
        this.volatility = volatility;
        this.maximumLossPercent = maximumLossPercent;
        this.firstPrice = firstPrice;
        this.currentPrice = currentPrice;
        this.firstPriceBenchmark = firstPriceBenchmark;
        this.currentPriceBenchmark = currentPriceBenchmark;
        this.alpha = alpha;
        this.beta = beta;
        this.correlation = correlation;
        this.trackingError = trackingError;
        this.high = high;
        this.low = low;
    }

    public BasicHistoricRatios copy(PriceRecord pr, PriceRecord prBenchmark) {
        if (pr == NullPriceRecord.INSTANCE) {
            return this;
        }
        final BigDecimal currentPriceBenchmark = prBenchmark != NullPriceRecord.INSTANCE
                ? prBenchmark.getPrice().getValue()
                : this.currentPriceBenchmark;

        final BigDecimal value = getPrice(pr);
        if (value == null) {
            return this;
        }

        final boolean noPrices = !pr.isCurrentDayDefined() || this.priceLength == null || this.priceLength.intValue() == 0;
        final boolean noVolumes = !pr.isCurrentDayDefined() || this.volumeLength == null || this.volumeLength.intValue() == 0;

        return new BasicHistoricRatiosImpl(this.reference,
                noPrices ? this.priceLength : add(this.priceLength, BigDecimal.ONE),
                noPrices ? this.priceSum : add(this.priceSum, value),
                pr.getVolumeDay() != null && !noVolumes ? add(this.volumeLength, BigDecimal.ONE) : this.volumeLength,
                pr.getVolumeDay() != null && !noVolumes ? add(this.volumeSum, new BigDecimal(pr.getVolumeDay())) : this.volumeSum,
                this.sharpeRatio, this.volatility,
                this.maximumLossPercent, this.firstPrice, value,
                this.firstPriceBenchmark, currentPriceBenchmark,
                this.alpha, this.beta, this.correlation, this.trackingError,
                getHigh(pr instanceof PriceRecordFund ? ((PriceRecordFund) pr).getRedemptionPrice() : pr.getHighDay()),
                getLow(pr instanceof PriceRecordFund ? ((PriceRecordFund) pr).getRedemptionPrice() : pr.getLowDay()));
    }

    private BigDecimal getPrice(PriceRecord pr) {
        if (pr instanceof PriceRecordFund) {
            // PREFER redemptionPrice over NAV (which be returned using getPrice) as
            // pm historic timeseries contains redemptionPrice and not NAV
            final Price redemptionPrice = ((PriceRecordFund) pr).getRedemptionPrice();
            final BigDecimal value = redemptionPrice.getValue();
            if (value != null) {
                return value;
            }
        }
        return pr.getPrice().getValue();
    }

    private Price getLow(Price lowDay) {
        final Price l;
        if (this.low.getValue() == null) {
            l = NullPrice.INSTANCE;
        }
        else {
            l = lowDay != null
                    && lowDay.getValue() != null
                    && lowDay.getValue().compareTo(BigDecimal.ZERO) > 0   // prevent rolled data from being used
                    && lowDay.getValue().compareTo(this.low.getValue()) < 0
                    ? lowDay : this.low;
        }
        return l;
    }

    private Price getHigh(Price highDay) {
        final Price h;
        if (this.high.getValue() == null) {
            h = NullPrice.INSTANCE;
        }
        else {
            h = highDay != null
                    && highDay.getValue() != null
                    && highDay.getValue().compareTo(this.high.getValue()) > 0
                    ? highDay : this.high;
        }
        return h;
    }

    public BasicHistoricRatios copy(BigDecimal value) {
        return new BasicHistoricRatiosImpl(this.reference,
                value != null ? add(this.priceLength, BigDecimal.ONE) : this.priceLength,
                value != null ? add(this.priceSum, value) : this.priceSum,
                this.volumeLength,
                this.volumeSum,
                this.sharpeRatio, this.volatility,
                this.maximumLossPercent, this.firstPrice, value,
                this.firstPriceBenchmark, this.currentPriceBenchmark,
                this.alpha, this.beta, this.correlation, this.trackingError,
                getHigh(new PriceImpl(value, null, null, new LocalDate().toDateTimeAtStartOfDay(), null)),
                getLow(new PriceImpl(value, null, null, new LocalDate().toDateTimeAtStartOfDay(), null)));
    }

    private static BigDecimal add(BigDecimal bd1, BigDecimal bd2) {
        if (bd1 == null) {
            return bd2;
        }
        if (bd2 == null) {
            return bd1;
        }
        return bd1.add(bd2);
    }

    public Interval getReference() {
        return this.reference;
    }

    public BigDecimal getAlpha() {
        return this.alpha;
    }

    public BigDecimal getBeta() {
        return this.beta;
    }

    public BigDecimal getCorrelation() {
        return this.correlation;
    }

    public BigDecimal getTrackingError() {
        return this.trackingError;
    }

    public BigDecimal getPerformance() {
        return getPerformance(this.currentPrice, this.firstPrice);
    }

    public BigDecimal getPerformanceBenchmark() {
        return getPerformance(this.currentPriceBenchmark, this.firstPriceBenchmark);
    }

    public BigDecimal getChangePercent() {
        return getPerformance();
    }

    public BigDecimal getChangeNet() {
        if (this.currentPrice == null || this.firstPrice == null) {
            return null;
        }
        return this.currentPrice.subtract(this.firstPrice);
    }

    public BigDecimal getCurrentPrice() {
        return this.currentPrice;
    }

    private BigDecimal getPerformance(BigDecimal currentPrice, BigDecimal firstPrice) {
        if (currentPrice == null || firstPrice == null) {
            return null;
        }
        return currentPrice.divide(firstPrice, MC).subtract(BigDecimal.ONE);
    }

    public BigDecimal getPerformanceToBenchmark() {
        final BigDecimal performance = getPerformance();
        final BigDecimal performanceBenchmark = getPerformance(this.currentPriceBenchmark, this.firstPriceBenchmark);
        if (performance == null || performanceBenchmark == null) {
            return null;
        }
        return performance.subtract(performanceBenchmark);
    }

    public BigDecimal getAveragePrice() {
        if (isDefinedAndNotZero(this.priceLength)) {
            return this.priceSum.divide(this.priceLength, MC);
        }
        return null;
    }

    public BigDecimal getAverageVolume() {
        if (isDefinedAndNotZero(this.volumeLength)) {
            return this.volumeSum.divide(this.volumeLength, MC);
        }
        return null;
    }

    private static boolean isDefinedAndNotZero(BigDecimal bd) {
        return (bd != null) && BigDecimal.ZERO.compareTo(bd) != 0;
    }

    public BigDecimal getVolatility() {
        return this.volatility;
    }

    public BigDecimal getSharpeRatio() {
        return this.sharpeRatio;
    }

    public BigDecimal getMaximumLossPercent() {
        return this.maximumLossPercent;
    }

    public Price getLow() {
        return low;
    }

    public Price getHigh() {
        return high;
    }

    public String toString() {
        return "BasicHistoricRatios[reference=" + getReference()
                + ", priceLength=" + this.priceLength
                + ", priceSum=" + this.priceSum
                + ", averagePrice=" + getAveragePrice()
                + ", volumeLength=" + this.volumeLength
                + ", volumeSum=" + this.volumeSum
                + ", averageVolume=" + getAverageVolume()
                + ", sharpeRatio=" + getSharpeRatio()
                + ", volatility=" + getVolatility()
                + ", firstPrice=" + this.firstPrice
                + ", currentPrice=" + this.currentPrice
                + ", performance=" + getPerformance()
                + ", firstPriceBenchmark=" + this.firstPriceBenchmark
                + ", currentPriceBenchmark=" + this.currentPriceBenchmark
                + ", performanceToBenchmark=" + getPerformanceToBenchmark()
                + ", alpha=" + getAlpha()
                + ", beta=" + getBeta()
                + ", correlation=" + getCorrelation()
                + ", trackingError=" + getTrackingError()
                + ", maximumLossPercent=" + getMaximumLossPercent()
                + ", high=" + getHigh()
                + ", low=" + getLow()
                + "]";
    }
}
