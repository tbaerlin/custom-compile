/*
 * NullBasicHistoricRatios.java
 *
 * Created on 01.10.2006 13:42:11
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domainimpl.data;

import java.io.Serializable;
import java.math.BigDecimal;

import org.joda.time.Interval;

import de.marketmaker.istar.domain.data.BasicHistoricRatios;
import de.marketmaker.istar.domain.data.PriceRecord;
import de.marketmaker.istar.domain.data.Price;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class NullBasicHistoricRatios implements Serializable, BasicHistoricRatios {
    protected static final long serialVersionUID = 1L;
    public final static BasicHistoricRatios INSTANCE = new NullBasicHistoricRatios();

    private NullBasicHistoricRatios() {
    }

    public BasicHistoricRatios copy(PriceRecord pr, PriceRecord prBenchmark) {
        return this;
    }

    public BasicHistoricRatios copy(BigDecimal value) {
        return this;
    }

    public Interval getReference() {
        return null;
    }

    public BigDecimal getAlpha() {
        return null;
    }

    public BigDecimal getBeta() {
        return null;
    }

    public BigDecimal getCorrelation() {
        return null;
    }

    public BigDecimal getTrackingError() {
        return null;
    }

    public BigDecimal getPerformance() {
        return null;
    }

    public BigDecimal getCurrentPrice() {
        return null;
    }

    public BigDecimal getPerformanceBenchmark() {
        return null;  
    }

    public BigDecimal getPerformanceToBenchmark() {
        return null;
    }

    public BigDecimal getAveragePrice() {
        return null;
    }

    public BigDecimal getAverageVolume() {
        return null;
    }

    public BigDecimal getVolatility() {
        return null;
    }

    public BigDecimal getSharpeRatio() {
        return null;
    }

    public BigDecimal getMaximumLossPercent() {
        return null;
    }

    public BigDecimal getChangeNet() {
        return null;
    }

    public BigDecimal getChangePercent() {
        return null;
    }

    public Price getLow() {
        return null;
    }

    public Price getHigh() {
        return null;  
    }
}
