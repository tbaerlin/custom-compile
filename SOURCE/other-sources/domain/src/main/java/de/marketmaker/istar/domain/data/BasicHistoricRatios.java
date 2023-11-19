/*
 * BasicHistoricRatios.java
 *
 * Created on 12.07.2006 22:54:15
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domain.data;

import org.joda.time.Interval;

import java.math.BigDecimal;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public interface BasicHistoricRatios {
    BasicHistoricRatios copy(PriceRecord pr, PriceRecord prBenchmark);
    BasicHistoricRatios copy(BigDecimal value);

    Interval getReference();
    BigDecimal getAlpha();
    BigDecimal getBeta();
    BigDecimal getCorrelation();
    BigDecimal getTrackingError();
    BigDecimal getPerformance();
    BigDecimal getCurrentPrice();
    BigDecimal getPerformanceBenchmark();
    BigDecimal getPerformanceToBenchmark();
    BigDecimal getAveragePrice();
    BigDecimal getAverageVolume();
    BigDecimal getVolatility();
    BigDecimal getSharpeRatio();
    BigDecimal getMaximumLossPercent();

    BigDecimal getChangeNet();
    BigDecimal getChangePercent();

    Price getLow();
    Price getHigh();

}
