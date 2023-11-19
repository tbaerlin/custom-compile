/*
 * StkKursdaten.java
 *
 * Created on 07.07.2006 10:28:22
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.block;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.Period;
import org.springframework.validation.BindException;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.ModelAndView;

import de.marketmaker.istar.common.util.PeriodEditor;
import de.marketmaker.istar.common.validator.NotNull;
import de.marketmaker.istar.common.validator.RestrictedSet;
import de.marketmaker.istar.domain.data.TradingPhase;
import de.marketmaker.istar.domain.instrument.Instrument;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.merger.provider.SymbolQuote;
import de.marketmaker.istar.merger.provider.TradingPhaseProvider;
import de.marketmaker.istar.merger.provider.TradingPhaseRequest;
import de.marketmaker.istar.merger.provider.TradingPhaseResponse;
import de.marketmaker.istar.merger.web.easytrade.BaseMultiSymbolCommand;
import de.marketmaker.istar.merger.web.easytrade.EnumEditor;
import de.marketmaker.istar.merger.web.easytrade.SymbolStrategyEnum;

/**
 * Returns trading phases for a (list of) quotes, systems, and strategies.
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class MscTradingPhases extends EasytradeCommandController {

    protected TradingPhaseProvider tradingPhaseProvider;

    protected EasytradeInstrumentProvider instrumentProvider;

    public static class Command extends BaseMultiSymbolCommand {
        private TradingPhase.SignalSystem[] system;

        private TradingPhase.SignalSystem.Strategy[] strategy;

        private TradingPhase.SignalSystem[] lastSignalSystem;

        private TradingPhase.SignalSystem.Strategy[] lastSignalStrategy;

        private Period period;

        @NotNull
        public String[] getSymbol() {
            return super.getSymbol();
        }

        /**
         * List of systems to return in detail.
         */
        @RestrictedSet("macd,momentum,bollinger,gd,tbi,mm")
        public TradingPhase.SignalSystem[] getSystem() {
            return system;
        }

        public void setSystem(TradingPhase.SignalSystem[] system) {
            this.system = system;
        }

        /**
         * Strategies to return in detail; if omitted conservative and speculative phases are returned.
         */
        @RestrictedSet("conservative,speculative")
        public TradingPhase.SignalSystem.Strategy[] getStrategy() {
            return strategy;
        }

        public void setStrategy(TradingPhase.SignalSystem.Strategy[] strategy) {
            this.strategy = strategy;
        }

        /**
         * List of systems to return the last signal for.
         */
        @RestrictedSet("macd,momentum,bollinger,gd,tbi,mm")
        public TradingPhase.SignalSystem[] getLastSignalSystem() {
            return lastSignalSystem;
        }

        public void setLastSignalSystem(TradingPhase.SignalSystem[] lastSignalSystem) {
            this.lastSignalSystem = lastSignalSystem;
        }

        /**
         * Strategies to return the last signal for; if omitted conservative and speculative phases are returned.
         */
        @RestrictedSet("conservative,speculative")
        public TradingPhase.SignalSystem.Strategy[] getLastSignalStrategy() {
            return lastSignalStrategy;
        }

        public void setLastSignalStrategy(TradingPhase.SignalSystem.Strategy[] lastSignalStrategy) {
            this.lastSignalStrategy = lastSignalStrategy;
        }

        /**
         * interval to return trading phases for, ending today.
         */
        public Period getPeriod() {
            return period;
        }

        public void setPeriod(Period period) {
            this.period = period;
        }
    }

    public MscTradingPhases() {
        super(Command.class);
    }

    protected void initBinder(HttpServletRequest httpServletRequest,
            ServletRequestDataBinder binder) throws Exception {
        super.initBinder(httpServletRequest, binder);

        binder.registerCustomEditor(Period.class, new PeriodEditor());
        EnumEditor.register(SymbolStrategyEnum.class, binder);
        EnumEditor.register(TradingPhase.SignalSystem.class, false, binder);
        EnumEditor.register(TradingPhase.SignalSystem.Strategy.class, false, binder);
    }

    public void setInstrumentProvider(EasytradeInstrumentProvider instrumentProvider) {
        this.instrumentProvider = instrumentProvider;
    }

    public void setTradingPhaseProvider(TradingPhaseProvider tradingPhaseProvider) {
        this.tradingPhaseProvider = tradingPhaseProvider;
    }

    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response,
            Object o, BindException errors) {

        final Command cmd = (Command) o;
        final List<Quote> quotes = getQuotes(cmd);

        final List<SymbolQuote> sqs = SymbolQuote.create(quotes);
        final DateTime now = new DateTime();
        final Interval interval = cmd.getPeriod().toDurationTo(now).toIntervalTo(now);

        final EnumSet<TradingPhase.SignalSystem> systems = cmd.getSystem() == null
                ? EnumSet.noneOf(TradingPhase.SignalSystem.class)
                : EnumSet.copyOf(Arrays.asList(cmd.getSystem()));

        final EnumSet<TradingPhase.SignalSystem> lastSignalsSystems = cmd.getLastSignalSystem() == null
                ? EnumSet.noneOf(TradingPhase.SignalSystem.class)
                : EnumSet.copyOf(Arrays.asList(cmd.getLastSignalSystem()));

        final EnumSet<TradingPhase.SignalSystem> allSystems = EnumSet.noneOf(TradingPhase.SignalSystem.class);
        allSystems.addAll(systems);
        allSystems.addAll(lastSignalsSystems);


        final EnumSet<TradingPhase.SignalSystem.Strategy> strategies = cmd.getStrategy() == null
                ? EnumSet.allOf(TradingPhase.SignalSystem.Strategy.class)
                : EnumSet.copyOf(Arrays.asList(cmd.getStrategy()));

        final EnumSet<TradingPhase.SignalSystem.Strategy> lastSignalsStrategies = cmd.getLastSignalStrategy() == null
                ? EnumSet.allOf(TradingPhase.SignalSystem.Strategy.class)
                : EnumSet.copyOf(Arrays.asList(cmd.getLastSignalStrategy()));

        final EnumSet<TradingPhase.SignalSystem.Strategy> allStrategies = EnumSet.noneOf(TradingPhase.SignalSystem.Strategy.class);
        allStrategies.addAll(strategies);
        allStrategies.addAll(lastSignalsStrategies);


        final TradingPhaseRequest tprequest = new TradingPhaseRequest(sqs,
                allSystems.toArray(new TradingPhase.SignalSystem[allSystems.size()]),
                allStrategies.toArray(new TradingPhase.SignalSystem.Strategy[allStrategies.size()]),
                interval, Boolean.FALSE);
        final TradingPhaseResponse tpr = this.tradingPhaseProvider.getTradingPhases(tprequest);

        final List<List<TradingPhase>> tps = new ArrayList<>();
        final List<List<TradingPhase>> lastSignals = new ArrayList<>();

        for (final SymbolQuote quote : sqs) {
            if (quote == null) {
                tps.add(Collections.<TradingPhase>emptyList());
                lastSignals.add(Collections.<TradingPhase>emptyList());
                continue;
            }

            final List<TradingPhase> tpsOfQuote = new ArrayList<>();
            final List<TradingPhase> lastSignalsOfQuote = new ArrayList<>();

            TradingPhase tpOld=null;
            TradingPhase.SignalSystem tpsystemOld = null;
            TradingPhase.SignalSystem.Strategy tpstrategyOld = null;

            final List<TradingPhase> allTps = tpr.getData(quote);
            if (allTps != null) {
                for (final TradingPhase tp : allTps) {
                    final TradingPhase.SignalSystem tpsystem = tp.getSignalSystem();
                    final TradingPhase.SignalSystem.Strategy tpstrategy = tp.getSignalSystemStrategy();

                    if (systems.contains(tpsystem) && strategies.contains(tpstrategy)) {
                        tpsOfQuote.add(tp);
                    }

                    if (lastSignalsSystems.contains(tpsystem) && lastSignalsStrategies.contains(tpstrategy)) {
                        if (tpsystem != tpsystemOld || tpstrategy != tpstrategyOld) {
                            if (tpOld != null) {
                                lastSignalsOfQuote.add(tpOld);
                            }

                            tpsystemOld = tpsystem;
                            tpstrategyOld = tpstrategy;
                        }
                        tpOld = tp;
                    }
                }
                if (tpOld != null) {
                    lastSignalsOfQuote.add(tpOld);
                }
            }
            tps.add(tpsOfQuote);
            lastSignals.add(lastSignalsOfQuote);
        }

        final Map<String, Object> model = new HashMap<>();
        model.put("quotes", quotes);
        model.put("tps", tps);
        model.put("lastSignals", lastSignals);
        return new ModelAndView("msctradingphases", model);
    }

    private List<Quote> getQuotes(Command cmd) {
        final MarketStrategies marketStrategies = new MarketStrategies(cmd);

        final List<String> symbols = Arrays.asList(cmd.getSymbol());
        final Map<String, Instrument> instrumentsBySymbol
                = this.instrumentProvider.identifyInstrument(symbols, cmd.getSymbolStrategy());

        final List<Quote> result = new ArrayList<>();

        for (final String s : symbols) {
            final Instrument instrument = instrumentsBySymbol.get(s);
            if (instrument == null) {
                result.add(null);
                continue;
            }

            result.add(marketStrategies.getQuote(s, instrument, null));
        }

        return result;
    }

}
