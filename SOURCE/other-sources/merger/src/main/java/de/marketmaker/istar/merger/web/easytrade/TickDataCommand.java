/*
 * TickDataCommand.java
 *
 * Created on 28.06.2007 14:18:08
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade;

import java.util.Collections;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Interval;
import org.joda.time.LocalDate;
import org.joda.time.Period;
import org.springframework.beans.factory.InitializingBean;

import de.marketmaker.istar.common.dmxmldocu.MmInternal;
import de.marketmaker.istar.common.util.DateUtil;
import de.marketmaker.istar.common.validator.Before;
import de.marketmaker.istar.domain.data.TickImpl;
import de.marketmaker.istar.domain.data.TickType;
import de.marketmaker.istar.merger.web.easytrade.util.IntradayUtil;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class TickDataCommand extends DefaultSymbolCommand implements InitializingBean {

    public enum ElementDataType {
        TICK, CLOSE, OHLC, OHLCV, FUND, PERFORMANCE, VOLUME_AGGREGATION
    }

    public enum Format {
        XML, PROTOBUF
    }

    private Period aggregation;

    private Period period;

    private DateTime start;

    private DateTime end;

    private ElementDataType type = ElementDataType.CLOSE;

    private TickImpl.Type tickType;

    private boolean inferTickType = false;

    private boolean alignStartWithAggregationPeriod = true;

    private boolean alignEndWithAggregationPeriod = false;

    private Format format = Format.XML;

    public void afterPropertiesSet() throws Exception {
        if (this.tickType == null) {
            this.tickType = getDefaultTickType();
        }
        if (this.period != null) {
            if (isDaysOnly()) {
                if (this.end == null) {
                    this.end = endOfToday();
                }
            }
            else {
                if (this.end == null) {
                    this.end = new DateTime().withMillisOfSecond(0);
                }
            }
            this.start = this.end.minus(this.period);
        }
        else {
            if (this.start == null) {
                this.start = new DateTime().withTimeAtStartOfDay();
            }
            if (this.end == null) {
                this.end = endOfToday();
            }
        }
    }

    protected TickImpl.Type getDefaultTickType() {
        return TickImpl.Type.TRADE;
    }

    private DateTime endOfToday() {
        return new LocalDate().plusDays(1).toDateTimeAtStartOfDay();
    }

    /**
     * @return whether to align the start of the requested period with the aggregation interval
     * (e.g., if set to true, a start date of 2001-03-04 for an aggregation period P1M will be
     * converted into 2001-03-01, for P1Y into 2001-01-01 etc.).
     */
    public boolean isAlignStartWithAggregationPeriod() {
        return alignStartWithAggregationPeriod;
    }

    public void setAlignStartWithAggregationPeriod(boolean alignStartWithAggregationPeriod) {
        this.alignStartWithAggregationPeriod = alignStartWithAggregationPeriod;
    }

    /**
     * @return whether to align the end of the requested period with the aggregation interval
     * (e.g., if set to true, a end date of 2001-03-04 for an aggregation period P1M will be converted into
     * 2001-03-31, for P1Y into 2001-12-31 etc.).
     */
    public boolean isAlignEndWithAggregationPeriod() {
        return alignEndWithAggregationPeriod;
    }

    public void setAlignEndWithAggregationPeriod(boolean alignEndWithAggregationPeriod) {
        this.alignEndWithAggregationPeriod = alignEndWithAggregationPeriod;
    }

    /**
     * @return interval for tick aggregation
     * @sample PT1m
     */
    public Period getAggregation() {
        return aggregation;
    }

    public void setAggregation(Period aggregation) {
        this.aggregation = aggregation;
    }

    @MmInternal
    public boolean isWithAggregation() {
        return getAggregation() != null && getAggregation().toStandardDuration().getMillis() > 0;
    }

    /**
     * @return defines the interval for which ticks are requested; if <tt>end</tt> is undefined,
     * the period ends <em>now</em> (or today at 24:00 if <tt>period</tt> has no time component),
     * otherwise, it ends at <tt>end</tt>.
     */
    public Period getPeriod() {
        return period;
    }

    public void setPeriod(Period period) {
        this.period = period;
    }

    /**
     * @return start time for requested ticks, <b>ignored</b> if <tt>period</tt> is defined;
     * defaults to start of current day
     */
    @Before("end")
    public DateTime getStart() {
        return start;
    }

    public void setStart(DateTime start) {
        this.start = start;
    }

    /**
     * @return end time for requested ticks; defaults to end of current day
     */
    public DateTime getEnd() {
        return end;
    }

    public void setEnd(DateTime end) {
        this.end = end;
    }

    /**
     * @return time series price type.
     */
    public ElementDataType getType() {
        return type;
    }

    public void setType(ElementDataType type) {
        this.type = type;
    }

    @MmInternal
    public boolean isDaysOnly() {
        return this.period.getHours() == 0
                && this.period.getMinutes() == 0
                && this.period.getSeconds() == 0
                && this.period.getMillis() == 0;
    }

    @MmInternal
    public TickType getTickTypeChicago() {
        return this.tickType.getTickTypeChicago();
    }

    public TickImpl.Type getTickType() {
        return tickType;
    }

    public void setInferTickType(boolean inferTickType) {
        this.inferTickType = inferTickType;
    }

    @MmInternal
    public boolean isInferTickType() {
        return this.inferTickType;
    }

    public void setTickType(TickImpl.Type tickType) {
        this.tickType = tickType;
    }

    /**
     * @return whether ticks should be rendered as XML or as base64 encoded binary data that
     * has been encoded using google protocol buffers. Encoded binary data is much more compact
     * and should be used if a large number of (non-aggregated) ticks are requested. For further
     * information about the protocol buffer message format and how to decode that data please
     * contact us.
     */
    public Format getFormat() {
        return format;
    }

    public void setFormat(Format format) {
        this.format = format;
    }

    @MmInternal
    public List<Interval> getTickIntervals() {
        if (getStart().isAfter(getEnd()) || getStart().isAfter(DateTime.now())) {
            return Collections.emptyList();
        }

        if (IntradayUtil.canUseTickHistory(getDuration(), getTickTypeChicago())) {
            return DateUtil.toHistoryIntervals(getStart(), getEnd());
        }
        return DateUtil.getDailyIntervals(getStart(), getEnd());
    }

    @MmInternal
    public Duration getDuration() {
        return this.aggregation != null ? this.aggregation.toStandardDuration() : null;
    }
}
