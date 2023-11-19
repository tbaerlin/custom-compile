/*
 * NwsListeNachrichten.java
 *
 * Created on 29.01.2007 13:25:12
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.block;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.LocalDate;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import de.marketmaker.istar.common.util.ArraysUtil;
import de.marketmaker.istar.common.util.DateUtil;
import de.marketmaker.istar.common.validator.NotNull;
import de.marketmaker.istar.domain.data.BasicHistoricRatios;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.merger.provider.HistoricRatiosProvider;
import de.marketmaker.istar.merger.provider.PortfolioRatiosRequest;
import de.marketmaker.istar.merger.web.easytrade.SymbolStrategyEnum;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class MscPortfolioRatios extends AbstractUserListHandler {
    public static class Command extends MscPortfolioVaRLight.Command {
        private String[] period;

        /**
         * @return time span for desired ratio data.
         * @sample P1M, P1Y
         */
        @NotNull
        @de.marketmaker.istar.merger.web.easytrade.Period
        public String[] getPeriod() {
            return ArraysUtil.copyOf(this.period);
        }

        public void setPeriod(String[] period) {
            this.period = ArraysUtil.copyOf(period);
        }
    }

    private HistoricRatiosProvider historicRatiosProvider;

    private EasytradeInstrumentProvider instrumentProvider;

    public MscPortfolioRatios() {
        super(Command.class);
    }

    public void setHistoricRatiosProvider(HistoricRatiosProvider historicRatiosProvider) {
        this.historicRatiosProvider = historicRatiosProvider;
    }

    public void setInstrumentProvider(EasytradeInstrumentProvider instrumentProvider) {
        this.instrumentProvider = instrumentProvider;
    }

    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response,
            Object o, BindException errors) throws Exception {
        final Command cmd = (Command) o;

        final PortfolioRatiosRequest pr = buildRequest(cmd.getDate(), cmd.getCurrency(), cmd.getPeriod(),
                cmd.getPosition(), cmd.getSymbolStrategy(), cmd.getMarketStrategy(), this.instrumentProvider);

        final List<BasicHistoricRatios> ratiosResponse = this.historicRatiosProvider.getPortfolioRatios(pr);

        final Map<String, Object> model = new HashMap<>();
        model.put("ratios", ratiosResponse);
        model.put("intervals", MscBasicRatiosMethod.getOutputPeriods(cmd.getPeriod()));
        return new ModelAndView("mscportfolioratios", model);
    }

    public static PortfolioRatiosRequest buildRequest(LocalDate date, String currency,
            String[] periods, MscPortfolioVaRLight.Position[] positions,
            SymbolStrategyEnum symbolStrategy, String marketStrategy,
            EasytradeInstrumentProvider instrumentProvider) {

        final PortfolioRatiosRequest request = new PortfolioRatiosRequest(date, currency, getIntervals(date, periods));

        final List<String> symbols = getSymbols(positions);

        final List<Quote> quotes = instrumentProvider.identifyQuotes(symbols, symbolStrategy, null, marketStrategy);

        final Map<String, Quote> quotesBySymbol = new HashMap<>();
        for (int i = 0; i < quotes.size(); i++) {
            final Quote quote = quotes.get(i);
            final String symbol = symbols.get(i);
            quotesBySymbol.put(symbol, quote);
        }

        for (MscPortfolioVaRLight.Position position : positions) {
            final String symbol = position.getSymbol();
            final Quote quote = quotesBySymbol.get(symbol);
            final BigDecimal quantity = position.getQuantity();

            request.addPosition(symbol, quote, quantity, null);
        }

        return request;
    }

    static List<Interval> getIntervals(LocalDate date, String[] periods) {
        final DateTime reference = date != null ? date.toDateTimeAtStartOfDay() : new DateTime();

        return Stream.of(periods).map(period -> new Interval(reference.minus(DateUtil.getPeriod(period)), reference)).collect(Collectors.toList());
    }

    static List<String> getSymbols(MscPortfolioVaRLight.Position[] positions) {
        final List<String> symbols = new ArrayList<>();
        for (final MscPortfolioVaRLight.Position position : positions) {
            symbols.add(position.getSymbol());
        }
        return symbols;
    }
}
