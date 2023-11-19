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

import org.joda.time.Interval;

import de.marketmaker.istar.domain.data.HighLow;
import de.marketmaker.istar.domain.data.Price;
import de.marketmaker.istar.domain.data.PriceRecord;
import de.marketmaker.istar.domain.data.PriceRecordFund;
import de.marketmaker.istar.domain.data.RatioDataRecord;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class HighLowImpl implements HighLow, Serializable {
    protected static final long serialVersionUID = 1L;

    private final Price high;

    private final Interval interval;

    private final Price low;

    /**
     * Multiplies the given highLow with a factor (e.g., to perform currency conversion).
     */
    public static HighLow create(HighLow highLow, BigDecimal factor) {
        if (highLow == null || highLow == NullHighLow.INSTANCE) {
            return highLow;
        }
        return new HighLowImpl(highLow.getInterval(), PriceUtil.multiply(highLow.getHigh(), factor),
                PriceUtil.multiply(highLow.getLow(), factor));
    }

    public HighLowImpl(Interval interval, Price high, Price low) {
        this.interval = interval;
        this.high = high;
        this.low = low;
    }

    public HighLow copy(PriceRecord pr) {
        return new HighLowImpl(this.interval, getHighestPrice(pr), getLowestPrice(pr));
    }

    public HighLow copy(RatioDataRecord rdr) {
        return new HighLowImpl(this.interval, getHighestPrice(rdr), getLowestPrice(rdr));
    }

    public Price getHigh() {
        return high;
    }

    public Interval getInterval() {
        return this.interval;
    }

    public Price getLow() {
        return low;
    }

    public String toString() {
        return "HighLowImpl[interval=" + getInterval()
                + ", high=" + getHigh()
                + ", low=" + getLow()
                + "]";
    }

    private Price getHighestPrice(RatioDataRecord rdr) {
        if (this.high.getValue() == null) {
            return NullPrice.INSTANCE;
        }
        return getHighestPrice(rdr.getHighDay());
    }

    private Price getHighestPrice(PriceRecord pr) {
        if (this.high.getValue() == null) {
            return NullPrice.INSTANCE;
        }
        return getHighestPrice(getPriceRecordPrice(pr, true));
    }

    private Price getHighestPrice(Price highDay) {
        return isHighHigherThan(highDay) ? this.high : highDay;
    }

    private Price getLowestPrice(RatioDataRecord rdr) {
        if (this.low.getValue() == null) {
            return NullPrice.INSTANCE;
        }
        return getLowestPrice(rdr.getLowDay());
    }

    private Price getLowestPrice(PriceRecord pr) {
        if (this.low.getValue() == null) {
            return NullPrice.INSTANCE;
        }
        return getLowestPrice(getPriceRecordPrice(pr, false));
    }

    private Price getLowestPrice(Price lowDay) {
        return isLowLowerThan(lowDay) ? this.low : lowDay;
    }

    private Price getPriceRecordPrice(PriceRecord pr, boolean high) {
        if (pr instanceof PriceRecordFund) {
            return ((PriceRecordFund) pr).getRedemptionPrice();
        }
        return high ? pr.getHighDay() : pr.getLowDay();
    }

    private boolean isHighHigherThan(Price highDay) {
        return highDay.getValue() != null && this.high.getValue().compareTo(highDay.getValue()) > 0;
    }

    private boolean isLowLowerThan(Price lowDay) {
        return lowDay.getValue() != null && this.low.getValue().compareTo(lowDay.getValue()) < 0;
    }
}
