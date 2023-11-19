/*
 * StkKennzahlenBenchmark.java
 *
 * Created on 13.07.2006 07:06:22
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 *
 */
package de.marketmaker.istar.merger.web.easytrade.block;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.Period;
import org.joda.time.YearMonthDay;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import de.marketmaker.istar.common.validator.NotNull;
import de.marketmaker.istar.domain.data.CorporateAction;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.merger.provider.HistoricRatiosProvider;
import de.marketmaker.istar.merger.provider.SymbolQuote;
import de.marketmaker.istar.merger.web.easytrade.DefaultSymbolCommand;

/**
 * This block shows all corporate actions of the company related to its share, which is defined by the given symbol.
 */

public class MscCorporateActions extends EasytradeCommandController {
    private HistoricRatiosProvider historicRatiosProvider;

    protected EasytradeInstrumentProvider instrumentProvider;

    public MscCorporateActions() {
        super(Command.class);
    }

    public void setInstrumentProvider(EasytradeInstrumentProvider instrumentProvider) {
        this.instrumentProvider = instrumentProvider;
    }

    public void setHistoricRatiosProvider(HistoricRatiosProvider historicRatiosProvider) {
        this.historicRatiosProvider = historicRatiosProvider;
    }

    public static class Command extends DefaultSymbolCommand {
        private DateTime start;

        private DateTime end;

        private Period period;

        boolean withFactorizedDividends = false;

        /**
         * @return defines the start of the time range
         */
        @NotNull
        public DateTime getStart() {
            if (this.period != null) {
                return new YearMonthDay().minus(this.period).toDateTimeAtMidnight();
            }

            return start;
        }

        public void setStart(DateTime start) {
            this.start = start;
        }

        /**
         * @return defines the end of the time range
         */
        @NotNull
        public DateTime getEnd() {
            if (this.period != null) {
                return new DateTime();
            }
            return end;
        }

        public void setEnd(DateTime end) {
            this.end = end;
        }

        /**
         * @return defines the time range by a period
         */
        public Period getPeriod() {
            return period;
        }

        public void setPeriod(Period period) {
            this.period = period;
        }

        public boolean isWithFactorizedDividends() {
            return withFactorizedDividends;
        }

        public void setWithFactorizedDividends(boolean withFactorizedDividends) {
            this.withFactorizedDividends = withFactorizedDividends;
        }
    }

    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response,
            Object o, BindException errors) {
        final Command cmd = (Command) o;
        final Quote quote = this.instrumentProvider.getQuote(cmd);

        final List<CorporateAction> cas = getActions(cmd, quote);

        final Map<String, Object> model = new HashMap<>();
        model.put("quote", quote);
        model.put("cas", cas);
        return new ModelAndView("msccorporateactions", model);
    }

    private List<CorporateAction> getActions(Command cmd, Quote quote) {
        if (!cmd.getEnd().isAfter(cmd.getStart())) {
            return Collections.emptyList();
        }
        return this.historicRatiosProvider.getCorporateActions(SymbolQuote.create(quote),
                new Interval(cmd.getStart(), cmd.getEnd()), cmd.isWithFactorizedDividends());
    }
}