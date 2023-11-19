/*
 * StkKursdaten.java
 *
 * Created on 07.07.2006 10:28:22
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.block;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Interval;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.merger.provider.calendar.TradingCalendar;
import de.marketmaker.istar.merger.provider.calendar.TradingCalendarProvider;
import de.marketmaker.istar.merger.provider.calendar.TradingDay;
import de.marketmaker.istar.merger.provider.calendar.TradingSession;
import de.marketmaker.istar.merger.web.easytrade.DefaultSymbolCommand;

/**
 * Returns information about when a particular quote can be traded at the quote's exchange.
 *
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class MscTradingHours extends EasytradeCommandController {

    protected EasytradeInstrumentProvider instrumentProvider;

    private TradingCalendarProvider tradingCalendarProvider;

    public void setTradingCalendarProvider(TradingCalendarProvider tradingCalendarProvider) {
        this.tradingCalendarProvider = tradingCalendarProvider;
    }

    public void setInstrumentProvider(EasytradeInstrumentProvider instrumentProvider) {
        this.instrumentProvider = instrumentProvider;
    }

    public static class Command extends DefaultSymbolCommand {
        private int numDays = 1;

        private boolean currentDayAsEndOfDay = false;

        /**
         * Request trading hours for the past <tt>num</tt> trading days, default is <tt>1</tt>,
         * which means <em>today</em>. Non-trading days (e.g. weekends for most quotes)
         * are not counted.
         */
        public int getNumDays() {
            return numDays;
        }

        public void setNumDays(int numDays) {
            this.numDays = numDays;
        }

        /**
         * If true, all trading sessions up to the end of the current day are returned, otherwise
         * the current day's sessions will only the returned if the first of them already started.
         * If the request's profile provides only delayed access to the requested quote, the session
         * will be seen as not yet started within the delay period (e.g., with a 15min delay,
         * a session that starts at 09:00 will first be included in the response at 09:15 -- this
         * is done so that clients requesting data for users with delayed permission can still show
         * yesterday's data as long as today's data is not visible to the user)
         */
        public boolean isCurrentDayAsEndOfDay() {
            return currentDayAsEndOfDay;
        }

        public void setCurrentDayAsEndOfDay(boolean currentDayAsEndOfDay) {
            this.currentDayAsEndOfDay = currentDayAsEndOfDay;
        }
    }

    public MscTradingHours() {
        super(Command.class);
    }

    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response,
            Object o, BindException errors) {

        final Command cmd = (Command) o;
        final Quote quote = this.instrumentProvider.getQuote(cmd);

        final Map<String, Object> model = new HashMap<>();
        model.put("quote", quote);

        final TradingCalendar tradingCalendar = this.tradingCalendarProvider.calendar(quote.getMarket());
        final DateTimeZone dtz = DateTimeZone.getDefault();
        final DateTime reference = cmd.isCurrentDayAsEndOfDay()
                ? new DateTime(dtz).plusDays(1).withTimeAtStartOfDay().minusSeconds(1)
                : new DateTime(dtz);
        final TradingDay[] tradingDays = tradingCalendar.latestTradingDays(quote, cmd.getNumDays(), dtz, reference);

        final List<Interval> intervals = new ArrayList<>(cmd.getNumDays());
        for (int i = 0; i < cmd.getNumDays(); i++) {
            for (final TradingSession session : tradingDays[i].sessions()) {
                final Interval interval = session.sessionInterval(dtz);
                intervals.add(interval);
            }
        }
        model.put("intervals", intervals);

        return new ModelAndView("msctradinghours", model);
    }
}