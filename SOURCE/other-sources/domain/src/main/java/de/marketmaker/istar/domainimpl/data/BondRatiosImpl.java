/*
 * BondRatiosImpl.java
 *
 * Created on 28.07.2006 07:45:51
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domainimpl.data;

import de.marketmaker.istar.domain.data.BondRatios;

import java.math.BigDecimal;
import java.io.Serializable;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class BondRatiosImpl implements Serializable, BondRatios {
    protected static final long serialVersionUID = 1L;

    private final BigDecimal marketRate;

    private final BigDecimal yield;
    private final BigDecimal brokenPeriodInterest;
    private final BigDecimal duration;
    private final BigDecimal convexity;
    private final BigDecimal interestRateElasticity;
    private final BigDecimal basePointValue;

    public BondRatiosImpl(BigDecimal marketRate, BigDecimal yield, BigDecimal brokenPeriodInterest,
                          BigDecimal duration, BigDecimal convexity, BigDecimal yieldElasticity,
                          BigDecimal basePointValue) {
        this.marketRate = marketRate;
        this.yield = yield;
        this.brokenPeriodInterest=brokenPeriodInterest;
        this.duration = duration;
        this.convexity = convexity;
        this.interestRateElasticity = yieldElasticity;
        this.basePointValue = basePointValue;
    }

    public BigDecimal getYield() {
        return yield;
    }

    public BigDecimal getDuration() {
        return duration;
    }

    public BigDecimal getConvexity() {
        return convexity;
    }

    public BigDecimal getInterestRateElasticity() {
        return interestRateElasticity;
    }

    public BigDecimal getBasePointValue() {
        return basePointValue;
    }

    public BigDecimal getBrokenPeriodInterest() {
        return brokenPeriodInterest;
    }

    public BigDecimal getBuyingPrice() {
        return null;
    }

    public BigDecimal getBuyingYield() {
        return null;
    }

    public BigDecimal getSellingPrice() {
        return null;
    }

    public BigDecimal getSellingYield() {
        return null;
    }

    public String toString() {
        return "BondRatiosImpl[marketRate=" + marketRate
                + ", yield=" + yield
                + ", brokenPeriodInterest=" + brokenPeriodInterest
                + ", duration=" + duration
                + ", convexity=" + convexity
                + ", interestRateElasticity=" + interestRateElasticity
                + ", basePointValue=" + basePointValue
                + "]";
    }
}
