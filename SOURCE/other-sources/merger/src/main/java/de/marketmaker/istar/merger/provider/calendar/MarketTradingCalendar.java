/*
 * TradingCalendarImpl.java
 *
 * Created on 11.09.2006 10:50:14
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.calendar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Interval;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.joda.time.Period;

import de.marketmaker.istar.domain.instrument.Quote;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class MarketTradingCalendar implements TradingCalendar {
    static final int DEFAULT_CALENDAR_MARKET_ID = 0;

    private final long marketid;

    private final MarketHolidayProvider marketHolidayProvider;

    private final DateTimeZone zone;

    private final BitSet regularTradingDays;

    private final List<Session> regularTradingSessions = new ArrayList<>();

    private Map<Long, List<Session>> quoteTradingSessions = null;

    private static final Period EMPTY_PERIOD = Period.minutes(0);

    private static class Session {
        private final LocalTime start;

        private final LocalTime end;

        public Session(LocalTime start, LocalTime end) {
            this.start = start;
            this.end = end;
        }

        public String toString() {
            return "Session[" + this.start + " - " + this.end + "]";
        }
    }

    private static class TradingDayImpl implements TradingDay {
        private final TradingSession[] sessions;

        private final LocalDate day;

        public TradingDayImpl(LocalDate day, TradingSession[] sessions) {
            this.day = day;
            this.sessions = sessions;
        }

        public LocalDate day() {
            return this.day;
        }

        public TradingSession[] sessions() {
            return this.sessions;
        }

        public String toString() {
            return "TradingDay[" + day + ", sessions=" + Arrays.toString(this.sessions()) + "]";
        }
    }

    private static class TradingSessionImpl implements TradingSession {
        private final DateTime start;

        private final DateTime end;

        public TradingSessionImpl(DateTime start, DateTime end) {
            this.start = start;
            this.end = end;
        }

        public Interval sessionInterval() {
            return new Interval(this.start, this.end);
        }

        public Interval sessionInterval(DateTimeZone dtz) {
            return new Interval(this.start.toDateTime(dtz), this.end.toDateTime(dtz));
        }

        public String toString() {
            return "Session[" + start + " - " + end + "]";
        }
    }

    public MarketTradingCalendar(long marketid, BitSet regularTradingDays,
            DateTimeZone dateTimeZone, MarketHolidayProvider marketHolidayProvider) {
        this.regularTradingDays = regularTradingDays;
        this.zone = dateTimeZone;
        this.marketid = marketid;
        this.marketHolidayProvider = marketHolidayProvider;
    }

    public DateTimeZone dateTimeZone() {
        return this.zone;
    }


    public String toString() {
        return "TradingCalendarImpl[" + this.dateTimeZone()
                + ", marketid=" + this.marketid
                + ", regular days=" + this.regularTradingDays
                + ", regular sessions=" + this.regularTradingSessions
                + "]";
    }

    public TradingDay[] latestTradingDays(Quote q, int n, DateTimeZone dtz) {
        return latestTradingDays(q, n, EMPTY_PERIOD, dtz);
    }

    public TradingDay[] latestTradingDays(Quote q, int n, DateTimeZone dtz, DateTime reference) {
        return latestTradingDays(q, n, EMPTY_PERIOD, dtz, reference);
    }

    public TradingDay[] latestTradingDays(Quote q, int n, Period delayOffset, DateTimeZone dtz) {
        final DateTime now = new DateTime(dtz);
        return latestTradingDays(q, n, delayOffset, dtz, now);
    }

    protected TradingDay[] latestTradingDays(Quote q, int n, Period delayOffset, DateTimeZone dtz,
            DateTime now) {
        if (this.marketid != DEFAULT_CALENDAR_MARKET_ID && q.getMarket().getId() != this.marketid) {
            throw new IllegalArgumentException("Quote " + q.getId() + " invalid for this market " + this.marketid);
        }
        final TradingDay[] result = new TradingDay[n];
        int i = result.length;

        for (LocalDate day = now.toLocalDate(); i > 0; day = day.minusDays(1)) {
            final TradingDay tradingDay = getTradingDay(day, q);
            if (tradingDay == null) {
                continue;
            }
            if (i == result.length
                    && tradingDay.sessions()[0].sessionInterval(dtz).getStart().isAfter(now.minus(delayOffset))) {
                // today's first session has not yet started, so today not in result
                continue;
            }
            result[--i] = tradingDay;
        }

        return result;
    }

    public TradingDay getTradingDay(LocalDate day, Quote q) {
        if (!this.regularTradingDays.get(day.getDayOfWeek())) {
            return null;
        }
        if (this.marketHolidayProvider.isHoliday(day, this.marketid)) {
            return null;
        }

        final List<Session> tradingSessions = getTradingSessions(q);
        final TradingSession[] sessions = new TradingSession[tradingSessions.size()];
        for (int j = 0; j < sessions.length; j++) {
            final Session s = tradingSessions.get(j);
            sessions[j] = new TradingSessionImpl(day.toDateTime(s.start, this.zone), day.toDateTime(s.end, this.zone));
        }
        return new TradingDayImpl(day, sessions);
    }

    private List<Session> getTradingSessions(Quote q) {
        if (this.quoteTradingSessions == null) {
            return this.regularTradingSessions;
        }
        final List<Session> quoteSessions = this.quoteTradingSessions.get(q.getId());
        return quoteSessions != null ? quoteSessions : this.regularTradingSessions;
    }

    void addRegularTradingSession(LocalTime startTime, LocalTime endTime) {
        this.regularTradingSessions.add(new Session(startTime, endTime));
    }

    void addQuoteTradingSession(Long qid, LocalTime startTime, LocalTime endTime) {
        if (this.quoteTradingSessions == null) {
            this.quoteTradingSessions = new HashMap<>();
        }
        List<Session> sessionList = this.quoteTradingSessions.get(qid);
        if (sessionList == null) {
            sessionList = new ArrayList<>();
            this.quoteTradingSessions.put(qid, sessionList);
        }
        sessionList.add(new Session(startTime, endTime));
    }
}
