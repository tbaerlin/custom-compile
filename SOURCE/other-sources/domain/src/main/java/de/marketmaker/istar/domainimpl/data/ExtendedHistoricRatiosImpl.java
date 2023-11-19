/*
 * BasicHistoricRatiosImpl.java
 *
 * Created on 17.07.2006 17:39:07
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domainimpl.data;

import de.marketmaker.istar.domain.data.BasicHistoricRatios;
import de.marketmaker.istar.domain.data.ExtendedHistoricRatios;
import de.marketmaker.istar.domain.data.PriceRecord;
import org.joda.time.Interval;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class ExtendedHistoricRatiosImpl implements ExtendedHistoricRatios, Serializable {
    public static final MathContext MC = new MathContext(8, RoundingMode.HALF_EVEN);

    protected static final long serialVersionUID = 1L;

    private final Interval reference;
    private final Integer longestContinuousNegativeReturnPeriod;
    private final BigDecimal maximumLossPercent;
    private final BigDecimal sharpeRatio;

    public ExtendedHistoricRatiosImpl(Interval reference, Integer longestContinuousNegativeReturnPeriod,
                                      BigDecimal maximumLossPercent, BigDecimal sharpeRatio) {
        this.reference = reference;
        this.longestContinuousNegativeReturnPeriod = longestContinuousNegativeReturnPeriod;
        this.maximumLossPercent = maximumLossPercent;
        this.sharpeRatio=sharpeRatio;
    }


    public Interval getReference() {
        return this.reference;
    }

    public Integer getLongestContinuousNegativeReturnPeriod() {
        return this.longestContinuousNegativeReturnPeriod;
    }

    public BigDecimal getMaximumLossPercent() {
        return this.maximumLossPercent;
    }

    public BigDecimal getSharpeRatio() {
        return sharpeRatio;
    }

    public String toString() {
        return "BasicHistoricRatios[reference=" + getReference()
                + ", longestContinuousNegativeReturnPeriod=" + this.longestContinuousNegativeReturnPeriod
                + ", maximumLossPercent=" + this.maximumLossPercent
                + ", sharpeRatio=" + this.sharpeRatio
                + "]";
    }
}