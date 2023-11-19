/*
 * MarketStrategies.java
 *
 * Created on 11.03.2010 09:31:24
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.block;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import de.marketmaker.istar.domain.instrument.Instrument;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.instrument.InstrumentUtil;
import de.marketmaker.istar.merger.util.SymbolUtil;
import de.marketmaker.istar.merger.web.easytrade.MultiSymbolCommand;
import de.marketmaker.istar.merger.web.easytrade.SymbolStrategyEnum;

/**
 * Encapsulates a default MarketStrategy and possibly symbol specific ones as well.
 * @author oflege
 */
public class MarketStrategies {

    private final MarketStrategy defaultStrategy;

    private final Map<String, MarketStrategy> strategies;

    public MarketStrategies(MarketStrategy defaultStrategy,
            Map<String, MarketStrategy> strategies) {
        this.defaultStrategy = defaultStrategy;
        this.strategies = strategies;
    }

    public MarketStrategies(String marketStrategyStr) {
        this(MarketStrategyFactory.getStrategy(marketStrategyStr), null);
    }

    public MarketStrategies(String market, String marketStrategyStr) {
        this(MarketStrategyFactory.getStrategy(market, marketStrategyStr), null);
    }

    public MarketStrategies(MultiSymbolCommand cmd) {
        this(MarketStrategyFactory.getStrategy(cmd.getMarket(), cmd.getMarketStrategy()),
                getStrategies(cmd.getMarketStrategyOverride()));
    }

    private static Map<String, MarketStrategy> getStrategies(String[] items) {
        if (items == null) {
            return null;
        }
        final HashMap<String, MarketStrategy> result = new HashMap<>();
        for (final String item : items) {
            final String[] keyvalue = item.split(Pattern.quote("="));
            if (keyvalue.length == 2) {
                result.put(keyvalue[0], MarketStrategyFactory.getStrategy(keyvalue[1]));
            }
        }
        return result;
    }

    public MarketStrategy forSymbol(String symbol) {
        final MarketStrategy result = getSymbolStrategy(symbol);
        return result != null ? result : this.defaultStrategy;
    }

    public MarketStrategy getDefaultStrategy() {
        return this.defaultStrategy;
    }

    private MarketStrategy getSymbolStrategy(String symbol) {
        return this.strategies != null ? this.strategies.get(symbol) : null;
    }

    public Quote getQuote(String symbol, Instrument instrument, String symbolSuffix) {
        final MarketStrategy result = getSymbolStrategy(symbol);
        if (result != null) {
            final Quote quote = result.getQuote(instrument);
            if (quote != null) {
                return quote;
            }
        }
        final SymbolStrategyEnum strategy = SymbolUtil.guessStrategy(symbol);

        switch (strategy) {
            case VWDCODE:
                return EasytradeInstrumentProvider.getQuoteByVwdcode(instrument, symbol);
            case MMWKN:
                return EasytradeInstrumentProvider.getQuoteByMmwkn(instrument, symbol);
            case BIS_KEY:
                return InstrumentUtil.getQuoteByBisKey(instrument, symbol);
            case VWDCODE_PREFIX:
                return EasytradeInstrumentProvider.getQuoteByVwdcodePrefix(instrument, symbol, symbolSuffix);
            case QID:
                return instrument.getQuote(EasytradeInstrumentProvider.id(symbol));
        }

        return this.defaultStrategy.getQuote(instrument);

    }
}
