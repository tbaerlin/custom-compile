/*
 * TickImpl.java
 *
 * Created on 08.06.2007 14:19:19
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domain.data;

import net.jcip.annotations.Immutable;
import org.joda.time.ReadableInterval;

import java.math.BigDecimal;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
@Immutable
public class AggregatedTickImpl {

    private final ReadableInterval interval;
    private final BigDecimal open;
    private final BigDecimal high;
    private final BigDecimal low;
    private final BigDecimal close;
    private final Long volume;
    private final int numberOfAggregatedTicks;
    private final TickType type;

    public AggregatedTickImpl(ReadableInterval interval, BigDecimal open, BigDecimal high, BigDecimal low,
                              BigDecimal close, Long volume, int numberOfAggregatedTicks, TickType type) {
        this.interval = interval;
        this.open = open;
        this.high = high;
        this.low = low;
        this.close = close;
        this.volume=volume;
        this.numberOfAggregatedTicks = numberOfAggregatedTicks;
        this.type = type;
    }

    public ReadableInterval getInterval() {
        return interval;
    }

    public BigDecimal getOpen() {
        return open;
    }

    public BigDecimal getHigh() {
        return high;
    }

    public BigDecimal getLow() {
        return low;
    }

    public BigDecimal getClose() {
        return close;
    }

    public Long getVolume() {
        return volume;
    }

    public int getNumberOfAggregatedTicks() {
        return numberOfAggregatedTicks;
    }

    public TickType getType() {
        return type;
    }

    public AggregatedTickImpl multiply(BigDecimal factor) {
        if (BigDecimal.ONE.equals(factor)) {
            return this;
        }
        return new AggregatedTickImpl(this.interval,
                multiply(this.open, factor),
                multiply(this.high, factor),
                multiply(this.low, factor),
                multiply(this.close, factor),
                this.volume, this.numberOfAggregatedTicks, this.type);
    }

    private BigDecimal multiply(BigDecimal bd, BigDecimal factor) {
        return (bd == null) ? null : bd.multiply(factor);
    }
}