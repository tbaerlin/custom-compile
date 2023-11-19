/*
 * TickHistoryRequest.java
 *
 * Created on 23.08.12 15:14
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.history;

import org.joda.time.Duration;
import org.joda.time.Interval;
import org.joda.time.LocalDate;

import de.marketmaker.istar.common.request.AbstractIstarRequest;
import de.marketmaker.istar.common.util.ByteString;
import de.marketmaker.istar.domain.data.TickType;
import de.marketmaker.istar.feed.vwd.VendorkeyVwd;

/**
 * @author zzhao
 */
public class TickHistoryRequest extends AbstractIstarRequest {

    private static final long serialVersionUID = 8737701584608508891L;

    public static final Duration AGGREGATION = Duration.standardMinutes(1);

    private final String vwdFeed;

    private final String symbol;

    private final Interval interval;

    private final Duration duration;

    private final boolean alignWithStart;

    private final int minTickNum;

    private final TickType tickType;

    public TickHistoryRequest(String symbolIn, Interval interval, Duration duration, int minTickNum,
            boolean alignWithStart, TickType tickType) {
        if (null == symbolIn || null == interval || duration.isShorterThan(AGGREGATION) || minTickNum < 0) {
            throw new IllegalArgumentException("symbol, interval, duration and non-negative min tick num"
             + " symbolIn is '" + symbolIn +"' interval is '" + interval + "' minTickNum is '" + minTickNum +"'");
        }
        final String symbol = symbolIn.toUpperCase();
        final ByteString bs = new ByteString(symbol);
        if (VendorkeyVwd.isKeyWithTypePrefix(bs)) {
            this.vwdFeed = symbol;
            this.symbol = VendorkeyVwd.getInstance(bs).toVwdcode().toString();
        }
        else {
            this.vwdFeed = null;
            this.symbol = symbol;
        }
        this.interval = interval;
        this.duration = duration;
        this.alignWithStart = alignWithStart;
        this.minTickNum = minTickNum;
        this.tickType = tickType;
    }

    public TickHistoryRequest(String symbol, LocalDate from, LocalDate to, Duration duration,
            int minTickNum, TickType tickType) {
        this(symbol, new Interval(from.toDateTimeAtStartOfDay(), to.toDateTimeAtStartOfDay()),
                duration, minTickNum, false, tickType);
    }

    public TickHistoryRequest(String symbol, LocalDate from, LocalDate to, Duration duration,
            TickType tickType) {
        this(symbol, from, to, duration, 0, tickType);
    }

    public TickType getTickType() {
        return tickType;
    }

    public String getVwdCode() {
        return this.symbol;
    }

    public String getVwdFeed() {
        return this.vwdFeed;
    }

    public Interval getInterval() {
        return interval;
    }

    public Duration getDuration() {
        return duration;
    }

    public boolean isAlignWithStart() {
        return alignWithStart;
    }

    public int getMinTickNum() {
        return minTickNum;
    }

    @Override
    protected void appendToString(StringBuilder sb) {
        sb.append(" ").append(this.symbol).append(", ")
                .append(HistoryUtil.DTF_YMDHMS.print(this.interval.getStart()))
                .append(" ~ ").append(HistoryUtil.DTF_YMDHMS.print(this.interval.getEnd()))
                .append(", ").append(this.minTickNum);
    }
}
