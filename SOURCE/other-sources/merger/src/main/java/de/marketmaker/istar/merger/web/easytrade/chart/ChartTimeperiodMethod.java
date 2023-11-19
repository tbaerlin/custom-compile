/*
 * ChartTimeperiodMethod.java
 *
 * Created on 25.03.2009 13:14:44
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.chart;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Interval;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;

import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.merger.provider.calendar.TradingCalendar;
import de.marketmaker.istar.merger.provider.calendar.TradingCalendarProvider;
import de.marketmaker.istar.merger.provider.calendar.TradingDay;
import de.marketmaker.istar.merger.provider.calendar.TradingSession;
import de.marketmaker.istar.chart.data.TimeperiodDefinitionBuilder;

/**
 * Computes intervals for displaying intraday data. A quote may trade on multiple sessions
 * per day and each of its benchmarks may trade on different sessions.<p>
 * Since computing and combining all these intervals is quite complicated, it has been factored
 * out into this class.
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
class ChartTimeperiodMethod {
    private static final DateTimeZone DTZ = DateTimeZone.getDefault();

    /**
     * The i-th element contains an array of localized times of trading sessions of the i-th benchmark
     */
    private Interval[][] benchmarkIntervals;

    /**
     * The i-th element contains the days on which the i-th benchmark was traded.
     * Each TradingDay interval will cover the same days as {@link #quoteTradingDays},
     * a TradingDay may be null if the benchmark was not traded on
     * a day when the main quote was.
     */
    private TradingDay[][] benchmarkTradingDays;

    /**
     * from the first trading session of any quote to the last
     */
    private Interval interval;

    /**
     * array of localized times of trading sessions of the main quote
     */
    private Interval[] quoteIntervals;

    private final TradingCalendarProvider tradingCalendarProvider;

    /**
     * most recent n days on which the main quote was traded
     */
    private TradingDay[] quoteTradingDays;

    ChartTimeperiodMethod(TradingCalendarProvider tradingCalendarProvider, Quote q,
            Quote[] benchmarks, int numDays) {
        this.tradingCalendarProvider = tradingCalendarProvider;

        this.quoteTradingDays = getTradingDays(q, numDays);
        this.benchmarkTradingDays = new TradingDay[benchmarks.length][];

        for (int i = 0; i < benchmarks.length; i++) {
            final Quote benchmark = benchmarks[i];
            if (benchmark != null) {
                // the benchmark may trade now whereas the latest trading session
                // for the quote may be several days back. Since we need benchmark trading days
                // for the days on which q was traded, go 14 days back 
                this.benchmarkTradingDays[i] = getTradingDays(benchmark, 14);
            }
        }

        computeIntervals();
        this.interval = computeInterval();
    }

    Interval[] getBenchmarkIntervals(int i) {
        return this.benchmarkIntervals[i];
    }

    Interval[] getQuoteIntervals() {
        return this.quoteIntervals;
    }

    Interval getInterval() {
        return this.interval;
    }

    TimeperiodDefinitionBuilder getTimeperiodDefinition() {
        final TimeperiodDefinitionBuilder result =
                new TimeperiodDefinitionBuilder(this.interval.getStart().toLocalDate(),
                        this.interval.getEnd().toLocalDate().plusDays(1), true);

        addDaysWithoutTrading(result);

        for (int i = 0; i < this.quoteTradingDays.length; i++) {
            final Interval session = getDaysInterval(i); // in local time zone
            final LocalDate startDay = session.getStart().toLocalDate();
            final LocalDate endDay = session.getEnd().toLocalDate();
            if (startDay.equals(endDay)) {
                result.addSpecialTime(startDay,
                        session.getStart().toLocalTime(), session.getEnd().toLocalTime());
            }
            else {
                result.addSpecialTime(startDay,
                        session.getStart().toLocalTime(), new LocalTime(23, 59, 59, 999));
                result.addSpecialTime(endDay,
                        new LocalTime(0,0,0), session.getEnd().toLocalTime());
            }
        }

        return result;
    }

    private void addDaysWithoutTrading(TimeperiodDefinitionBuilder tsb) {
        LocalDate ld = this.quoteTradingDays[0].day();
        int i = 1;
        while (i < this.quoteTradingDays.length) {
            ld = ld.plusDays(1);
            if (ld.isBefore(this.quoteTradingDays[i].day())) {
                tsb.addExcludedDay(ld);
                continue;
            }
            i++;
        }
    }

    private Interval computeInterval() {
        Interval result = getInterval(this.quoteTradingDays);
        for (int i = 0; i < this.benchmarkTradingDays.length; i++) {
            final TradingDay[] days = this.benchmarkTradingDays[i];
            if (days == null) {
                continue;
            }
            final Interval benchmarkInterval = getInterval(days);
            if (benchmarkInterval != null) {
                result = union(result, benchmarkInterval);
            }
        }
        return result;
    }

    private void computeIntervals() {
        this.quoteIntervals = computeIntervals(this.quoteTradingDays);
        this.benchmarkIntervals = new Interval[this.benchmarkTradingDays.length][];
        for (int i = 0; i < benchmarkTradingDays.length; i++) {
            if (benchmarkTradingDays[i] != null) {
                this.benchmarkIntervals[i] = computeIntervals(benchmarkTradingDays[i]);
            }
        }
    }

    private Interval[] computeIntervals(TradingDay[] days) {
        final List<Interval> intervals = new ArrayList<>();
        for (TradingDay day : days) {
            if (day == null) {
                continue;
            }
            for (TradingSession session : day.sessions()) {
                intervals.add(session.sessionInterval(DTZ));
            }
        }
        return intervals.toArray(new Interval[intervals.size()]);
    }

    private TradingDay[] filterByQuoteTradingDays(TradingDay[] days) {
        final TradingDay[] result = new TradingDay[this.quoteTradingDays.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = findDayForDate(days, this.quoteTradingDays[i].day());
        }
        return result;
    }

    private TradingDay findDayForDate(TradingDay[] days, final LocalDate date) {
        for (int i = 0; i < days.length; i++) {
            if (days[i].day().equals(date)) {
                return days[i];
            }
        }
        return null;
    }

    private Interval getDaysInterval(int n) {
        final TradingDay day = this.quoteTradingDays[n];
        Interval result = getInterval(day);
        for (TradingDay[] benchmarkTradingDay : this.benchmarkTradingDays) {
            if (benchmarkTradingDay != null && benchmarkTradingDay[n] != null) {
                result = union(result, getInterval(benchmarkTradingDay[n]));
            }
        }
        return result;
    }

    private Interval getInterval(TradingDay day) {
        return getInterval(getSession(day, true), getSession(day, false));
    }

    private Interval getInterval(TradingDay[] days) {
        return getInterval(getSession(days, true), getSession(days, false));
    }

    private Interval getInterval(TradingSession first, TradingSession last) {
        if (first == null || last == null) {
            return null;
        }
        if (first == last) {
            return first.sessionInterval(DTZ);
        }
        return new Interval(first.sessionInterval(DTZ).getStart(), last.sessionInterval(DTZ).getEnd());
    }

    private TradingSession getSession(TradingDay[] days, boolean first) {
        int i = first ? 0 : days.length - 1;
        final int increment = first ? 1 : -1;
        while (i >= 0 && i < days.length) {
            if (days[i] != null) {
                return getSession(days[i], first);
            }
            i += increment;
        }
        return null;
    }

    private TradingSession getSession(TradingDay day, boolean first) {
        if (day == null) {
            return null;
        }
        final TradingSession[] sessions = day.sessions();
        return first ? sessions[0] : sessions[sessions.length - 1];
    }

    private TradingDay[] getTradingDays(Quote q, int numDays) {
        final TradingCalendar tradingCalendar =
                this.tradingCalendarProvider.calendar(q.getMarket());

        final TradingDay[] tds = tradingCalendar.latestTradingDays(q, numDays, DTZ);
        if (this.quoteTradingDays == null) {
            return tds;
        }
        return filterByQuoteTradingDays(tds);
    }

    private Interval union(Interval i1, Interval i2) {
        final DateTime start = i1.getStart().isBefore(i2.getStart()) ? i1.getStart() : i2.getStart();
        final DateTime end = i1.getEnd().isAfter(i2.getEnd()) ? i1.getEnd() : i2.getEnd();
        return new Interval(start, end);
    }
}
