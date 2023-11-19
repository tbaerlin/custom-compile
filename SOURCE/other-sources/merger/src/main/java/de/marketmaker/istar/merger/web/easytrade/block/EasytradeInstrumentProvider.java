/*
 * EasytradeInstrumentProvider.java
 *
 * Created on 04.08.2006 10:23:19
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.block;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import de.marketmaker.istar.common.util.CollectionUtils;
import de.marketmaker.istar.domain.instrument.Derivative;
import de.marketmaker.istar.domain.instrument.Instrument;
import de.marketmaker.istar.domain.instrument.InstrumentTypeEnum;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.domainimpl.instrument.NullQuote;
import de.marketmaker.istar.instrument.IndexCompositionResponse;
import de.marketmaker.istar.instrument.InstrumentUtil;
import de.marketmaker.istar.instrument.search.SearchMetaResponse;
import de.marketmaker.istar.instrument.search.SearchResponse;
import de.marketmaker.istar.merger.context.RequestContextHolder;
import de.marketmaker.istar.merger.provider.InstrumentProvider;
import de.marketmaker.istar.merger.provider.InstrumentProviderImpl;
import de.marketmaker.istar.merger.provider.ProfiledIndexCompositionProvider;
import de.marketmaker.istar.merger.provider.UnknownSymbolException;
import de.marketmaker.istar.merger.util.SymbolUtil;
import de.marketmaker.istar.merger.web.easytrade.SymbolCommand;
import de.marketmaker.istar.merger.web.easytrade.SymbolStrategyEnum;


/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class EasytradeInstrumentProvider {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private InstrumentProvider instrumentProvider;

    private ProfiledIndexCompositionProvider indexCompositionProvider;

    public static final String IID_SUFFIX = ".iid";

    public static final String QID_SUFFIX = ".qid";

    private static final String PREFIX_UNDERLYING = "underlying(";

    private static Pattern BIS_KEY_PATTERN = Pattern.compile("[0-9]+(_[0-9]+){3}");


    public void setInstrumentProvider(InstrumentProvider instrumentProvider) {
        this.instrumentProvider = instrumentProvider;
    }

    public void setIndexCompositionProvider(
            ProfiledIndexCompositionProvider indexCompositionProvider) {
        this.indexCompositionProvider = indexCompositionProvider;
    }

    public Quote getQuote(SymbolCommand command) {
        return identifyQuote(command.getSymbol(), command.getSymbolStrategy(), command.getMarket(), command.getMarketStrategy());
    }

    public Instrument identifyInstrument(SymbolCommand cmd) {
        return identifyInstrument(cmd.getSymbol(), cmd.getSymbolStrategy());
    }

    public Quote identifyQuoteByVwdcode(String symbol) {
        return this.instrumentProvider.identifyByVwdcode(symbol);
    }

    public Instrument getInstrument(SymbolCommand command) {
        final Instrument instrument = identifyInstrument(command.getSymbol(), command.getSymbolStrategy());

        if (instrument == null) {
            throw new UnknownSymbolException("invalid symbol: '" + command.getSymbol() + "'");
        }
        return instrument;
    }

    public static String iidSymbol(Long id) {
        return id + IID_SUFFIX;
    }

    public static String qidSymbol(Long id) {
        return id + QID_SUFFIX;
    }

    public static Long id(String symbol) {
        return Long.parseLong(idString(symbol));
    }

    public static String idString(String symbol) {
        if (symbol.endsWith(QID_SUFFIX)) {
            return removeSuffix(symbol, QID_SUFFIX);
        }
        else if (symbol.endsWith(IID_SUFFIX)) {
            return removeSuffix(symbol, IID_SUFFIX);
        }
        return symbol;
    }

    private static String removeSuffix(String s, String suffix) {
        return s.substring(0, s.length() - suffix.length());
    }

    public List<Instrument> identifyInstruments(List<Long> iids) {
        return this.instrumentProvider.identifyInstruments(iids);
    }

    public SearchMetaResponse getSearchMetadata() {
        return this.instrumentProvider.getMetadata();
    }

    public SearchResponse simpleSearch(SimpleSearchCommand cmd) {
        return this.instrumentProvider.simpleSearch(cmd);
    }

    public List<Quote> identifyQuotes(Collection<Long> qids) {
        if (qids == null) {
            return Collections.emptyList();
        }
        final List<String> symbols = new ArrayList<>(qids.size());
        for (Long qid : qids) {
            symbols.add(qidSymbol(qid));
        }
        return identifyQuotes(symbols, SymbolStrategyEnum.QID, null, null);
    }

    public Instrument identifyInstrument(String symbol, SymbolStrategyEnum symbolStrategy) {
        if (!StringUtils.hasText(symbol)) {
            throw new UnknownSymbolException("invalid symbol: '" + symbol + "'");
        }

        if (!usesUnderlyingFunction(symbol)) {
            return identifyInstrumentImpl(symbol, symbolStrategy);
        }

        final Instrument instrument = identifyInstrument(SymbolUtil.extractSymbol(symbol), symbolStrategy);

        if (instrument == null || !(instrument instanceof Derivative)) {
            return null;
        }

        final Derivative derivative = (Derivative) instrument;

        final long underlyingid = derivative.getUnderlyingId();
        if (underlyingid <= 0L) {
            return null;
        }

        return identifyInstrument(iidSymbol(underlyingid), null);
    }

    static boolean usesUnderlyingFunction(String symbol) {
        return symbol.startsWith(PREFIX_UNDERLYING);
    }

    private Instrument identifyInstrumentImpl(String symbol, SymbolStrategyEnum symbolStrategy) {
        if (!isSpecific(symbolStrategy)) {
            return SymbolUtil.identifyInstrument(symbol, SymbolUtil.guessStrategy(symbol), this.instrumentProvider);
        }
        return SymbolUtil.identifyInstrument(symbol, symbolStrategy, this.instrumentProvider);
    }

    public Instrument identifyByIsinOrWkn(String symbol) {
        return this.instrumentProvider.identifyByIsinOrWkn(symbol);
    }

    public Quote identifyQuote(String symbol, SymbolStrategyEnum symbolStrategy, String market,
            String marketStrategy) {
        return identifyQuote(symbol, symbolStrategy,
                new MarketStrategies(market, marketStrategy).forSymbol(symbol));
    }

    public Quote identifyQuote(String symbol, SymbolStrategyEnum symbolStrategy,
            MarketStrategy marketStrategy) {

        final SymbolStrategyEnum strategy = resolveStrategy(symbolStrategy, symbol);

        final Instrument instrument = identifyInstrument(symbol, strategy);
        if (instrument == null) {
            throw new UnknownSymbolException("invalid symbol: '" + symbol + "'");
        }

        if (usesUnderlyingFunction(symbol)) {
            // always return default quote
            return getQuote(instrument, marketStrategy);
        }

        return mapQuote(symbol, strategy, marketStrategy, instrument);
    }

    public List<Quote> identifyQuotes(List<String> symbols, SymbolStrategyEnum symbolStrategy,
            String market, String marketStrategy) {
        return identifyQuotes(symbols, symbolStrategy, new MarketStrategies(market, marketStrategy));
    }

    public List<Quote> identifyQuotes(List<String> symbols, SymbolStrategyEnum symbolStrategy,
            MarketStrategies marketStrategies) {
        if (symbols == null || symbols.isEmpty()) {
            return Collections.emptyList();
        }

        if (usesUnderlyingFunction(symbols)) {
            final List<Quote> result = new ArrayList<>(symbols.size());
            for (final String symbol : symbols) {
                result.add(identifyQuote(symbol, symbolStrategy, marketStrategies.forSymbol(symbol)));
            }
            return result;
        }

        final Map<SymbolStrategyEnum, List<String>> symbolsByStrategy
                = getSymbolsByStrategy(symbols, symbolStrategy);

        final Map<String, Quote> quotes = new HashMap<>();
        final Map<String, Instrument> instruments = new HashMap<>();

        for (Map.Entry<SymbolStrategyEnum, List<String>> e : symbolsByStrategy.entrySet()) {
            final Map<String, Instrument> instrumentMap
                    = this.instrumentProvider.identifyInstruments(e.getValue(), e.getKey());
            instruments.putAll(instrumentMap);
            quotes.putAll(mapQuotes(instrumentMap, e.getKey(), marketStrategies));
        }

        final List<Quote> result = new ArrayList<>(symbols.size());
        for (final String symbol : symbols) {
            result.add(quotes.get(symbol));
        }

        if (this.logger.isDebugEnabled() && result.size() != symbols.size()) {
            final StringBuilder sb = new StringBuilder(Math.max(100, symbols.size() * 16));
            sb.append("<identifyQuotes> incomplete: profile=")
                    .append(RequestContextHolder.getRequestContext().getProfile().getName());
            for (String symbol : symbols) {
                sb.append(" ").append(symbol).append(":")
                        .append(instruments.containsKey(symbol) ? 'I' : '-')
                        .append(quotes.containsKey(symbol) ? 'Q' : '-');
            }
            this.logger.debug(sb.toString());
        }

        return result;
    }

    private Map<SymbolStrategyEnum, List<String>> getSymbolsByStrategy(List<String> symbols,
            SymbolStrategyEnum symbolStrategy) {
        final Map<SymbolStrategyEnum, List<String>> symbolsByStrategy;
        if (isSpecific(symbolStrategy)) {
            symbolsByStrategy = Collections.singletonMap(symbolStrategy, symbols);
        }
        else {
            symbolsByStrategy = new EnumMap<>(SymbolStrategyEnum.class);
            for (String symbol : symbols) {
                final SymbolStrategyEnum anEnum = SymbolUtil.guessStrategy(symbol);
                if (!symbolsByStrategy.containsKey(anEnum)) {
                    symbolsByStrategy.put(anEnum, new ArrayList<String>());
                }
                symbolsByStrategy.get(anEnum).add(symbol);
            }
        }
        return symbolsByStrategy;
    }

    private SymbolStrategyEnum resolveStrategy(SymbolStrategyEnum symbolStrategy,
            String symbol) {
        return isSpecific(symbolStrategy) ? symbolStrategy : SymbolUtil.guessStrategy(symbol);
    }

    /**
     * For each entry in instruments, extract a quote from the value and put it in the result
     * map under the same symbol
     */
    private Map<String, Quote> mapQuotes(Map<String, Instrument> instruments,
            SymbolStrategyEnum strategy, MarketStrategies marketStrategies) {

        final Map<String, Quote> result = new HashMap<>();

        for (Map.Entry<String, Instrument> entry : instruments.entrySet()) {
            try {
                final String symbol = entry.getKey();
                final Quote q = mapQuote(symbol, strategy, marketStrategies.forSymbol(symbol), entry.getValue());
                if (q != null) {
                    result.put(symbol, q);
                }
            } catch (UnknownSymbolException e) {
                if (this.logger.isDebugEnabled()) {
                    this.logger.debug("<mapQuotes> no quote for " + iidSymbol(entry.getValue().getId()), e);
                }
            }
        }
        return result;
    }

    private Quote mapQuote(String symbol, SymbolStrategyEnum strategy,
            MarketStrategy marketStrategy, Instrument instrument) {
        switch (strategy) {
            case QID:
                return instrument.getQuote(id(symbol));
            case VWDCODE:
                return getQuoteByVwdcode(instrument, symbol);
            case MMWKN:
                return getQuoteByMmwkn(instrument, symbol);
            case BIS_KEY:
                return InstrumentUtil.getQuoteByBisKey(instrument, symbol);
            default:
                return getQuote(instrument, marketStrategy);
        }
    }

    private boolean usesUnderlyingFunction(List<String> symbols) {
        for (final String symbol : symbols) {
            if (usesUnderlyingFunction(symbol)) {
                return true;
            }
        }
        return false;
    }

    static Quote getQuoteByVwdcode(Instrument instrument, String vwdcode) {
        for (final Quote quote : instrument.getQuotes()) {
            if (InstrumentProviderImpl.isMatchingVwdCode(vwdcode, quote)) {
                return quote;
            }
        }
        return null;
    }

    static Quote getQuoteByMmwkn(Instrument instrument, String vwdcode) {
        for (final Quote quote : instrument.getQuotes()) {
            if (vwdcode.equalsIgnoreCase(quote.getSymbolMmwkn())) {
                return quote;
            }
        }
        return null;
    }

    static Quote getQuoteByVwdcodePrefix(Instrument instrument, String vwdcodePrefix,
            String symbolSuffix) {
        final String prefix = vwdcodePrefix.endsWith("*")
                ? vwdcodePrefix.substring(0, vwdcodePrefix.length() - 1)
                : vwdcodePrefix;
        for (final Quote quote : instrument.getQuotes()) {
            if (quote.getSymbolVwdcode() != null
                    && quote.getSymbolVwdcode().startsWith(prefix)
                    && (symbolSuffix == null || quote.getSymbolVwdcode().substring(prefix.length()).matches(symbolSuffix))) {
                return quote;
            }
        }
        return null;
    }

    private long getId(String symbol, String suffix) {
        try {
            return symbol.endsWith(suffix)
                    ? Long.parseLong(symbol.substring(0, symbol.length() - suffix.length()))
                    : Long.parseLong(symbol);
        } catch (NumberFormatException e) {
            throw new UnknownSymbolException("invalid symbol: '" + symbol + "'");
        }
    }

    public Quote getQuote(Instrument instrument, MarketStrategy strategy) {
        if (instrument.getQuotes().isEmpty()) {
            return null;
        }
        if (strategy == null) {
            return getQuote(instrument, MarketStrategyFactory.getDefaultMarketStrategy());
        }
        return strategy.getQuote(instrument);
    }

    public Quote getQuote(Instrument instrument, String market, String marketStrategy) {
        return getQuote(instrument, MarketStrategyFactory.getStrategy(market, marketStrategy));
    }

    public Quote getUnderlyingQuote(Instrument instrument, String marketStrategy) {
        if (!(instrument instanceof Derivative)) {
            return null;
        }

        final long underlyingId = ((Derivative) instrument).getUnderlyingId();
        if (underlyingId <= 0) {
            return null;
        }

        Instrument benchmark = null;
        try {
            benchmark = identifyInstrument(iidSymbol(underlyingId), null);
            return getQuote(benchmark, null);
        } catch (UnknownSymbolException e) {
            if (benchmark != null) {
                return NullQuote.create(benchmark);
            }

            if (this.logger.isDebugEnabled()) {
                this.logger.debug("<getUnderlyingQuote> no benchmark for "
                        + iidSymbol(instrument.getId())
                        + " (benchmark=" + iidSymbol(underlyingId) + ")?", e);
            }
            return null;
        }
    }

    public List<Quote> getIndexQuotes(String indexQid) {
        if (this.indexCompositionProvider == null) {
            this.logger.warn("<getIndexQuotes> no indexCompositionProvider set");
            return Collections.emptyList();
        }

        if (indexQid == null || !indexQid.endsWith(QID_SUFFIX)) {
            throw new UnknownSymbolException("invalid quote symbol: '" + indexQid + "'");
        }

        final long qid = getId(indexQid, QID_SUFFIX);
        IndexCompositionResponse response
                = this.indexCompositionProvider.getIndexCompositionByQid(qid);

        if (!response.isValid()) {
            this.logger.warn("<getIndexQuotes> no quotes for index: " + indexQid);
            return Collections.emptyList();
        }

        final List<Long> positions = response.getIndexComposition().getQids();
        final List<Quote> quotes = identifyQuotes(positions);
        CollectionUtils.removeNulls(quotes);
        return quotes;
    }

    /**
     * Convenience method to get only the instrumentIds for a list of symbols
     * @param symbols Array of symbols to identify
     * @param symbolStrategy Symbol strategy to use
     * @return List of instrumentIds (never null)
     */
    public List<Long> identifyInstrumentIds(String[] symbols, SymbolStrategyEnum symbolStrategy) {
        return this.identifyInstrumentIds(Arrays.asList(symbols), symbolStrategy);
    }

    /**
     * Convenience method to get only the instrumentIds for a list of symbols
     * @param symbols List of symbols to identify
     * @param symbolStrategy Symbol strategy to use
     * @return List of instrumentIds (never null)
     */
    public List<Long> identifyInstrumentIds(List<String> symbols, SymbolStrategyEnum symbolStrategy) {

        final Map<String, Instrument> map
                = this.identifyInstrument(symbols, symbolStrategy);
        return map.values()
                        .stream()
                        .filter(Objects::nonNull)
                        .map(Instrument::getId)
                        .collect(Collectors.toList());
    }

    /**
     * Returns a map of symbols mapped to instrument objects.
     * @param symbols specify instruments to be identified
     * @param symbolStrategy defines how symbols should be interpreted; if null or
     * {@link de.marketmaker.istar.merger.web.easytrade.SymbolStrategyEnum#AUTO}, each symbol
     * will be inspected to guess the most appropriate strategy.
     * @return map with entries for all symbols that could be identified, may be empty and|or
     * unmodifiable.
     */
    public Map<String, Instrument> identifyInstrument(List<String> symbols,
            SymbolStrategyEnum symbolStrategy) {
        if (symbols == null || symbols.isEmpty()) {
            return Collections.emptyMap();
        }

        if (usesUnderlyingFunction(symbols)) {
            final Map<String, Instrument> result = new HashMap<>();
            for (final String symbol : symbols) {
                final Instrument instrument = identifyInstrument(symbol, symbolStrategy);
                if (instrument != null) {
                    result.put(symbol, instrument);
                }
            }
            return result;
        }

        if (isSpecific(symbolStrategy)) {
            return this.instrumentProvider.identifyInstruments(symbols, symbolStrategy);
        }

        final Map<SymbolStrategyEnum, List<String>> symbolsByStrategy
                = new EnumMap<>(SymbolStrategyEnum.class);

        for (String symbol : symbols) {
            final SymbolStrategyEnum sse = SymbolUtil.guessStrategy(symbol);
            List<String> list = symbolsByStrategy.get(sse);
            if (list == null) {
                list = new ArrayList<>();
                symbolsByStrategy.put(sse, list);
            }
            list.add(symbol);
        }

        final Map<String, Instrument> result = new HashMap<>();
        for (Map.Entry<SymbolStrategyEnum, List<String>> entry : symbolsByStrategy.entrySet()) {
            result.putAll(identifyInstrument(entry.getValue(), entry.getKey()));
        }
        return result;
    }

    private boolean isSpecific(SymbolStrategyEnum symbolStrategy) {
        return symbolStrategy != null && symbolStrategy != SymbolStrategyEnum.AUTO;
    }

    public List<Instrument> getDerivates(long iid, int maxNumResults, InstrumentTypeEnum... type) {
        return this.instrumentProvider.getDerivates(iid, maxNumResults, type);
    }

    public List<Instrument> getByVwdcodePrefix(String vwdcodePrefix, int maxNumResults,
            InstrumentTypeEnum... type) {
        return this.instrumentProvider.getByVwdcodePrefix(vwdcodePrefix, maxNumResults, type);
    }
}
