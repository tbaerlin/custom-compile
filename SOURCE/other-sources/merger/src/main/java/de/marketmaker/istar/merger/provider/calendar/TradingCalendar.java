/*
 * TradingCalendar.java
 *
 * Created on 11.09.2006 10:02:36
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.calendar;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.joda.time.Period;

import de.marketmaker.istar.domain.instrument.Quote;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public interface TradingCalendar {
    /**
     * The time zone for the calendar.
     * @return .
     */
    DateTimeZone dateTimeZone();

    /**
     * Returns specifications for the latest n trading days. Today will only be included
     * if today's trading already started.
     * @param quote object for which TradingDays are requested
     * @param n number of days.
     * @param dtz target time zone for result
     * @return latest n trading days.
     */
    TradingDay[] latestTradingDays(Quote quote, int n, DateTimeZone dtz);

    /**
     * Returns specifications for the latest n trading days. Today will only be included
     * if today's first trading session started at least delayOffset ago. Useful if you
     * deal with delayed data and do not want to show today's chart until some delayed data
     * might be available.
     * @param quote object for which TradingDays are requested
     * @param n number of days
     * @param delayOffset .
     * @param dtz target time zone for result
     * @return latest n trading days
     */
    TradingDay[] latestTradingDays(Quote quote, int n, Period delayOffset, DateTimeZone dtz);

    /**
     * Returns specifiactions for the given day.
     * If the given day is not a trading day, null is returned.
     *
     * @param day The day for which the TradingDay is requested
     * @param q The Quote for which the TradingDay is requested
     * @return The TradingDay data for the given day and quote or null, if the given day is not a trading day.
     */
    TradingDay getTradingDay(LocalDate day, Quote q);

    TradingDay[] latestTradingDays(Quote q, int n, DateTimeZone dtz, DateTime reference);
}
