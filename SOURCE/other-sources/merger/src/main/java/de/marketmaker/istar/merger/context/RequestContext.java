/*
 * RequestContext.java
 *
 * Created on 18.08.2009 16:33:51
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.context;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import de.marketmaker.istar.common.featureflags.FeatureFlags;
import de.marketmaker.istar.domain.instrument.InstrumentNameStrategies;
import de.marketmaker.istar.domain.instrument.InstrumentNameStrategy;
import de.marketmaker.istar.domain.instrument.MarketNameStrategies;
import de.marketmaker.istar.domain.instrument.MarketNameStrategy;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.domain.instrument.QuoteComparator;
import de.marketmaker.istar.domain.instrument.QuoteNameStrategies;
import de.marketmaker.istar.domain.instrument.QuoteNameStrategy;
import de.marketmaker.istar.domain.instrument.TickerStrategies;
import de.marketmaker.istar.domain.instrument.TickerStrategy;
import de.marketmaker.istar.domain.profile.Profile;
import de.marketmaker.istar.instrument.search.SearchRequestResultType;
import de.marketmaker.istar.merger.provider.SharedIntradayContext;
import de.marketmaker.istar.merger.web.easytrade.block.LbbwMarketStrategy;
import de.marketmaker.istar.merger.web.easytrade.block.MarketStrategy;
import de.marketmaker.istar.merger.web.easytrade.block.QuoteFilter;
import de.marketmaker.istar.merger.web.easytrade.block.QuoteFilters;

import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class RequestContext {
    public static final String KEY_RECENTTRADE_QID = "recenttrade.qid";

    public static final String KEY_UNIQE_ID = "__UNIQUE_ID";

    public static final String KEY_NUM_ATOMS = "__NUM_ATOMS";

    public static final MarketStrategy DEFAULT_MARKET_STRATEGY = LbbwMarketStrategy.INSTANCE;

    private static final String INTRADAY_MAP_KEY = "__INTRADAY_MAP";

    private static final String INTRADAY_MAP_DISABLED_KEY = "__INTRADAY_MAP_DISABLED";

    public static final String DEFAULT_MARKET_STRATEGY_NAME = "default";

    private static final String QUOTE_COMPARATOR_KEY = QuoteComparator.class.getName();

    private static final String QUOTE_NAME_STRATEGY_KEY = QuoteNameStrategy.class.getName();

    private static final String INSTRUMENT_NAME_STRATEGY_KEY = InstrumentNameStrategy.class.getName();

    private static final String MARKET_NAME_STRATEGY_KEY = MarketNameStrategy.class.getName();

    private static final String TICKER_STRATEGY_KEY = TickerStrategy.class.getName();

    private static final String BASE_QUOTE_FILTER_KEY = QuoteFilter.class.getName();

    public static final Locale DEFAULT_LOCALE = Locale.GERMAN;

    public static final List<Locale> DEFAULT_LOCALES = singletonList(DEFAULT_LOCALE);

    private final RequestContext parent;

    private final Profile profile;

    private final Map<String, MarketStrategy> marketStrategies;

    private final List<Locale> locales;

    private final SearchRequestResultType searchRequestResultType;

    private final Map<String, Object> map;

    private RequestContext(Profile profile, Map<String, MarketStrategy> marketStrategies,
            List<Locale> locales, SearchRequestResultType searchRequestResultType,
            RequestContext parent) {
        this.profile = profile;
        this.marketStrategies = marketStrategies;
        this.locales = (locales != null && !locales.isEmpty()) ? locales : DEFAULT_LOCALES;
        this.map = null;
        this.parent = parent;
        this.searchRequestResultType = searchRequestResultType;
    }

    private RequestContext(Profile profile, Map<String, MarketStrategy> marketStrategies,
            SearchRequestResultType searchRequestResultType,
            Map<String, Object> map) {
        this.profile = profile;
        this.marketStrategies = marketStrategies;
        this.locales = DEFAULT_LOCALES;
        this.map = map;
        this.parent = null;
        this.searchRequestResultType = searchRequestResultType;
    }

    public RequestContext(Profile profile, MarketStrategy marketStrategy) {
        this(profile, asMap(marketStrategy), null, new HashMap<>());
    }

    public RequestContext(Profile profile, Map<String, MarketStrategy> marketStrategies) {
        this(profile, marketStrategies, null, new HashMap<>());
    }

    public RequestContext withProfile(Profile profile) {
        return new RequestContext(profile, this.marketStrategies, this.locales, this.searchRequestResultType, this);
    }

    public RequestContext withMarketStrategy(MarketStrategy marketStrategy) {
        return new RequestContext(this.profile, asMap(marketStrategy), this.locales, this.searchRequestResultType, this);
    }

    public static Map<String, MarketStrategy> asMap(MarketStrategy marketStrategy) {
        return singletonMap(DEFAULT_MARKET_STRATEGY_NAME, marketStrategy);
    }

    public RequestContext withLocales(List<Locale> locales) {
        return new RequestContext(this.profile, this.marketStrategies, locales, this.searchRequestResultType, this);
    }

    public RequestContext withSearchRequestResultType(SearchRequestResultType type) {
        return new RequestContext(this.profile, this.marketStrategies, locales, type, this);
    }

    public Profile getProfile() {
        return profile;
    }

    public SearchRequestResultType getSearchRequestResultType() {
        return searchRequestResultType;
    }

    /**
     * Returns the default market strategy, that is the strategy specified in a non-strategy-map
     * constructor or the value in the strategy map with the key "{@value #DEFAULT_MARKET_STRATEGY_NAME}",
     * if not null; if no non-null value is available, {@link #DEFAULT_MARKET_STRATEGY} will be
     * returned.
     * @return default market strategy for this context
     */
    public MarketStrategy getMarketStrategy() {
        return getMarketStrategy(DEFAULT_MARKET_STRATEGY_NAME);
    }

    /**
     * Returns a market strategy with the given name, or, if no such strategy is found, the result
     * will be what {@link #getMarketStrategy()} returns.
     * @param name name of strategy to be returned
     * @return market strategy
     */
    public MarketStrategy getMarketStrategy(String name) {
        if (name == null) {
            return getMarketStrategy(DEFAULT_MARKET_STRATEGY_NAME);
        }
        final MarketStrategy result = this.marketStrategies.get(name);
        return (result != null) ? result : DEFAULT_MARKET_STRATEGY;
    }

    /**
     * @return the primary locale.
     */
    public Locale getLocale() {
        return this.locales.get(0);
    }

    /**
     * @return requested locales with primary locale as first element, never null or empty
     */
    public List<Locale> getLocales() {
        return this.locales;
    }

    public boolean containsKey(String key) {
        if (this.parent != null) {
            return this.parent.containsKey(key);
        }
        synchronized (this.map) {
            return this.map.containsKey(key);
        }
    }

    public Object get(String key) {
        if (this.parent != null) {
            return this.parent.get(key);
        }
        synchronized (this.map) {
            return this.map.get(key);
        }
    }

    public Object put(String key, Object value) {
        if (this.parent != null) {
            return this.parent.put(key, value);
        }
        synchronized (this.map) {
            return this.map.put(key, value);
        }
    }

    public Object remove(String key) {
        if (this.parent != null) {
            return this.parent.remove(key);
        }
        synchronized (this.map) {
            return this.map.remove(key);
        }
    }

    public void disableSharedIntradayMap() {
        synchronized (this.map) {
            if (this.map.containsKey(INTRADAY_MAP_KEY)) {
                throw new IllegalStateException();
            }
            this.map.put(INTRADAY_MAP_DISABLED_KEY, Boolean.TRUE);
        }
    }

    public void clearIntradayContext() {
        getIntradayContext(false).clear();
    }

    public SharedIntradayContext getIntradayContext() {
        return getIntradayContext(true);
    }

    private SharedIntradayContext getIntradayContext(boolean create) {
        if (this.parent != null) {
            return this.parent.getIntradayContext(create);
        }
        synchronized (this.map) {
            if (this.map.containsKey(INTRADAY_MAP_DISABLED_KEY)) {
                return SharedIntradayContext.NULL;
            }

            final SharedIntradayContext existing
                    = (SharedIntradayContext) this.map.get(INTRADAY_MAP_KEY);
            if (existing != null) {
                return existing;
            }
            if (!create) {
                return SharedIntradayContext.NULL;
            }
            final SharedIntradayContext result = new SharedIntradayContext();
            this.map.put(INTRADAY_MAP_KEY, result);
            return result;
        }
    }

    public void putAll(Map<String, Object> contextObjects) {
        if (this.parent != null) {
            this.parent.putAll(contextObjects);
            return;
        }

        synchronized (this.map) {
            this.map.putAll(contextObjects);
        }
    }

    public void setUniqueId(String uid) {
        if (uid != null) {
            put(KEY_UNIQE_ID, uid);
        }
    }

    public String getUniqueId() {
        return (String) get(KEY_UNIQE_ID);
    }

    public void setQuoteComparator(Comparator<Quote> comparator) {
        put(QUOTE_COMPARATOR_KEY, comparator);
    }

    public Comparator<Quote> getQuoteComparator() {
        //noinspection unchecked
        return (Comparator<Quote>) get(QUOTE_COMPARATOR_KEY);
    }

    public void setQuoteNameStrategy(QuoteNameStrategy strategy) {
        put(QUOTE_NAME_STRATEGY_KEY, strategy);
    }

    public QuoteNameStrategy getQuoteNameStrategy() {
        final QuoteNameStrategy result = (QuoteNameStrategy) get(QUOTE_NAME_STRATEGY_KEY);
        return (result != null) ? result : QuoteNameStrategies.DEFAULT.with(getLocale(), getProfile());
    }

    public void setInstrumentNameStrategy(InstrumentNameStrategy strategy) {
        put(INSTRUMENT_NAME_STRATEGY_KEY, strategy);
    }

    public InstrumentNameStrategy getInstrumentNameStrategy() {
        final InstrumentNameStrategy result = (InstrumentNameStrategy) get(INSTRUMENT_NAME_STRATEGY_KEY);
        return (result != null) ? result : InstrumentNameStrategies.DEFAULT.with(getLocale(), getProfile());
    }

    public void setMarketNameStrategy(MarketNameStrategy strategy) {
        put(MARKET_NAME_STRATEGY_KEY, strategy);
    }

    public MarketNameStrategy getMarketNameStrategy() {
        final MarketNameStrategy result = (MarketNameStrategy) get(MARKET_NAME_STRATEGY_KEY);
        return (result != null) ? result : MarketNameStrategies.DEFAULT.with(getLocale(), getProfile());
    }

    public void setTickerStrategy(TickerStrategy strategy) {
        put(TICKER_STRATEGY_KEY, strategy);
    }

    public TickerStrategy getTickerStrategy() {
        final TickerStrategy result = (TickerStrategy) get(TICKER_STRATEGY_KEY);
        return (result != null) ? result : TickerStrategies.DEFAULT;
    }

    public void setBaseQuoteFilter(QuoteFilter quoteFiler) {
        put(BASE_QUOTE_FILTER_KEY, quoteFiler);
    }

    public QuoteFilter getBaseQuoteFilter() {
        final QuoteFilter result = (QuoteFilter) get(BASE_QUOTE_FILTER_KEY);
        return (result != null) ? result : QuoteFilters.FILTER_SPECIAL_MARKETS;
    }

    /**
     * @return true iff the language of the primary locale is german.
     */
    public boolean isLocaleLanguageGerman() {
        return getLocale().getLanguage().equals(Locale.GERMAN.getLanguage());
    }

    /**
     * Checks whether a feature is enabled globally <em>and</em> enabled explicitly in this context.
     * Use for features that require to be enabled explicitly for each zone.
     * @param f to be tested
     * @return true iff the flag is enabled <b>and</b> the current zone has a
     * property defined as "<tt>context.<em>&lt;f.name()&gt;</em>=true</tt>"
     */
    public boolean isEnabled(FeatureFlags.Flag f) {
        return FeatureFlags.isEnabled(f) && Boolean.TRUE.equals(get(f.name()));
    }

    /**
     * Checks whether a feature is enabled globally <em>and not</em> explicitly disabled this context.
     * Use for features that are enabled unless they are not explicitly disabled in a zone.
     * @param f to be tested
     * @return true if the flag is enabled <b>and</b> if the current zone does not have a
     * property defined as "<tt>context.<em>&lt;f.name()&gt;</em>=false</tt>"
     */
    public boolean isEnabledAndNotForbidden(FeatureFlags.Flag f) {
        return FeatureFlags.isEnabled(f) && !Boolean.FALSE.equals(get(f.name()));
    }
}
