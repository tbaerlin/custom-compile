/*
 * PerformanceYear.java
 *
 * Created on 3/31/14 1:16 PM
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.domain.data;

import org.joda.time.Interval;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author Stefan Willenbrock
 */
public class IntervalPerformance implements Comparable<IntervalPerformance>, Serializable {

    protected static final long serialVersionUID = 1L;

    private Interval interval;
    private BigDecimal performance;

    public IntervalPerformance(Interval interval, BigDecimal performance) {
        this.interval = interval;
        this.performance = performance;
    }

    public Interval getInterval() {
        return interval;
    }

    public BigDecimal getPerformance() {
        return performance;
    }

    @Override
    /**
     * Compares two intervals after the start date in ascending millisecond instance order.
     *
     * @see org.joda.time.base.AbstractInstant#compareTo(org.joda.time.ReadableInstant)
     */
    public int compareTo(IntervalPerformance o) {
        if (this.interval == null || this.interval.getStart() == null) {
            throw new IllegalStateException("interval error");
        }
        if (o.getInterval() == null || o.getInterval().getStart() == null) {
            throw new IllegalArgumentException("interval error in argument");
        }
        return this.interval.getStart().compareTo(o.getInterval().getStart());
    }
}
