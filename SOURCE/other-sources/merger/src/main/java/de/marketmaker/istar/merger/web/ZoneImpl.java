/*
 * ZoneImpl.java
 *
 * Created on 15.08.2006 18:05:12
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web;

import de.marketmaker.istar.common.log.LoggingUtil;
import de.marketmaker.istar.domain.instrument.InstrumentNameStrategies;
import de.marketmaker.istar.domain.instrument.InstrumentNameStrategy;
import de.marketmaker.istar.domain.instrument.MarketNameStrategies;
import de.marketmaker.istar.domain.instrument.MarketNameStrategy;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.domain.instrument.QuoteNameStrategies;
import de.marketmaker.istar.domain.instrument.QuoteNameStrategy;
import de.marketmaker.istar.domain.instrument.TickerStrategies;
import de.marketmaker.istar.domain.instrument.TickerStrategy;
import de.marketmaker.istar.domain.profile.Profile;
import de.marketmaker.istar.merger.context.RequestContext;
import de.marketmaker.istar.merger.web.easytrade.MoleculeRequest;
import de.marketmaker.istar.merger.web.easytrade.RequestParserMethod;
import de.marketmaker.istar.merger.web.easytrade.block.MarketStrategy;
import de.marketmaker.istar.merger.web.easytrade.block.QuoteFilter;
import de.marketmaker.istar.merger.web.easytrade.block.QuoteFilters;

import org.slf4j.MDC;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static de.marketmaker.istar.merger.web.HttpRequestUtil.getRequestName;
import static de.marketmaker.istar.merger.web.easytrade.MoleculeRequest.REQUEST_ATTRIBUTE_NAME;

/**
 * A zone implementation that maintains default and fixed parameters for specific
 * zone members as well as generic default and fixed parameters for all members.
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class ZoneImpl implements Zone {
    private final String name;

    private String templateBase;

    private final Map<String, ConcurrentMap<String, String[]>> defaultParameters = newParametersMap();

    private final Map<String, ConcurrentMap<String, String[]>> fixedParameters = newParametersMap();

    private final Map<String, ConcurrentMap<String, Object>> contextObjects =
            new ConcurrentHashMap<>();

    private final Map<String, HandlerInterceptor[]> interceptors =
            new ConcurrentHashMap<>();

    static final String DEFAULT_KEY = "";

    private final Map<String, MarketStrategy> marketStrategies
            = new ConcurrentHashMap<>();

    private QuoteNameStrategy quoteNameStrategy = QuoteNameStrategies.DEFAULT;

    private MarketNameStrategy marketNameStrategy = MarketNameStrategies.DEFAULT;

    private QuoteFilter baseQuoteFilter = QuoteFilters.FILTER_SPECIAL_MARKETS;

    private InstrumentNameStrategy instrumentNameStrategy = InstrumentNameStrategies.DEFAULT;

    private TickerStrategy tickerStrategy = TickerStrategies.DEFAULT;

    private Comparator<Quote> quoteComparator = null;

    public ZoneImpl(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getTemplateBase() {
        return (this.templateBase != null) ? this.templateBase : this.name;
    }

    public String toString() {
        return "Zone[" + this.name + "]";
    }

    public HandlerInterceptor[] getInterceptors(String key) {
        final HandlerInterceptor[] interceptors = this.interceptors.get(key);
        if (interceptors == null) {
            return this.interceptors.get(DEFAULT_KEY);
        }
        return interceptors;
    }

    public RequestContext getRequestContext(HttpServletRequest request, Profile p) {
        final RequestContext result = createRequestContext(request, p);

        result.putAll(getContextObjects(DEFAULT_KEY));

        @SuppressWarnings({"unchecked"})
        final Map<String, Object> requestContextObjects
                = (Map<String, Object>) request.getAttribute(Zone.CONTEXT_ATTRIBUTE);
        if (requestContextObjects != null) {
            result.putAll(requestContextObjects);
        }

        return result;
    }

    @Override
    public ErrorPage getErrorPage(HttpServletRequest request, int errorCode, String errorMessage) {
        ErrorPage ep = (ErrorPage) getContextMap(getRequestName(request)).get("error-page." + errorCode);
        if (ep == null) {
            return null;
        }
        return ep.resolve(errorMessage);
    }

    private RequestContext createRequestContext(HttpServletRequest request, Profile p) {
        RequestContext result = new RequestContext(p, this.marketStrategies);
        final List<Locale> locales = getLocales(request);
        if (locales != null && !locales.isEmpty()) {
            result = result.withLocales(locales);
        }

        final Locale locale = result.getLocale();
        result.setQuoteNameStrategy(this.quoteNameStrategy.with(locale, p));
        result.setInstrumentNameStrategy(this.instrumentNameStrategy.with(locale, p));
        result.setMarketNameStrategy(this.marketNameStrategy.with(locale, p));

        result.setTickerStrategy(this.tickerStrategy);
        result.setQuoteComparator(this.quoteComparator);
        result.setBaseQuoteFilter(this.baseQuoteFilter);
        result.setUniqueId(MDC.get(LoggingUtil.UNIQUE_ID));
        return result;
    }

    private List<Locale> getLocales(HttpServletRequest request) {
        final MoleculeRequest mr = (MoleculeRequest) request.getAttribute(REQUEST_ATTRIBUTE_NAME);
        if (mr != null) {
            return mr.getLocales();
        }
        final String localeParam = request.getParameter("locale");
        if (localeParam != null) {
            return RequestParserMethod.parseLocales(localeParam);
        }
        final HttpSession session = request.getSession(false);
        if (session != null) {
            final String localeAttr = (String) session.getAttribute("locale");
            if (localeAttr != null) {
                return Collections.singletonList(new Locale(localeAttr));
            }
        }
        return null;
    }

    void setMarketStrategy(MarketStrategy marketStrategy) {
        setMarketStrategies(RequestContext.asMap(marketStrategy));
    }

    public MarketStrategy getMarketStrategy() {
        return this.marketStrategies.get(RequestContext.DEFAULT_MARKET_STRATEGY_NAME);
    }

    void setMarketStrategies(Map<String, MarketStrategy> strategies) {
        this.marketStrategies.clear();
        this.marketStrategies.putAll(strategies);
    }

    public void setQuoteComparator(Comparator<Quote> quoteComparator) {
        this.quoteComparator = quoteComparator;
    }

    void setQuoteNameStrategy(QuoteNameStrategy quoteNameStrategy) {
        this.quoteNameStrategy = quoteNameStrategy;
    }

    public void setMarketNameStrategy(MarketNameStrategy marketNameStrategy) {
        this.marketNameStrategy = marketNameStrategy;
    }

    void setInstrumentNameStrategy(InstrumentNameStrategy instrumentNameStrategy) {
        this.instrumentNameStrategy = instrumentNameStrategy;
    }

    void setTickerStrategy(TickerStrategy tickerStrategy) {
        this.tickerStrategy = tickerStrategy;
    }

    public void setBaseQuoteFilter(QuoteFilter baseQuoteFilter) {
        this.baseQuoteFilter = baseQuoteFilter;
    }

    void setTemplateBase(String templateBase) {
        this.templateBase = templateBase;
    }

    public void setInterceptors(String key, HandlerInterceptor[] interceptors) {
        this.interceptors.put(key, interceptors);
    }

    void addDefaultParametersIfAbsent(String key, String name, String[] values) {
        addParametersIfAbsent(key, name, values, this.defaultParameters);
    }

    void addFixedParametersIfAbsent(String key, String name, String[] values) {
        addParametersIfAbsent(key, name, values, this.fixedParameters);
    }

    private void addParametersIfAbsent(String key, String name, String[] values,
            Map<String, ConcurrentMap<String, String[]>> parameters) {
        ConcurrentMap<String, String[]> m = parameters.get(key);
        if (m == null) {
            m = newParameterMap();
            parameters.put(key, m);
        }
        m.putIfAbsent(name, values);
    }

    void addContextObjectIfAbsent(String key, String name, Object value) {
        ConcurrentMap<String, Object> m = this.contextObjects.get(key);
        if (m == null) {
            m = new ConcurrentHashMap<>();
            this.contextObjects.put(key, m);
        }
        m.putIfAbsent(name, value);
    }

    void addContextObject(String key, String name, Object value) {
        ConcurrentMap<String, Object> m = this.contextObjects.get(key);
        if (m == null) {
            m = new ConcurrentHashMap<>();
            this.contextObjects.put(key, m);
        }
        m.put(name, value);
    }

    void addDefaultParameter(String key, String name, String value) {
        addParameter(key, name, value, this.defaultParameters);
    }

    void addFixedParameter(String key, String name, String value) {
        addParameter(key, name, value, this.fixedParameters);
    }

    private void addParameter(String key, String name, String value,
            Map<String, ConcurrentMap<String, String[]>> parameters) {
        ConcurrentMap<String, String[]> m = parameters.get(key);
        if (m == null) {
            m = newParameterMap();
            parameters.put(key, m);
        }
        m.put(name, toArray(value));
    }

    private static String[] toArray(String s) {
        if (s.startsWith("[") && s.endsWith("]")) {
            return StringUtils.commaDelimitedListToStringArray(s.substring(1, s.length() - 1));
        }
        return new String[]{s};
    }

    public Map<String, String[]> getParameterMap(Map<String, String[]> requestParameters,
            String name) {
        final Map<String, String[]> result = new HashMap<>();
        result.putAll(getDefaultParameters(DEFAULT_KEY));
        result.putAll(getDefaultParameters(name));
        result.putAll(requestParameters);
        result.putAll(getFixedParameters(DEFAULT_KEY));
        result.putAll(getFixedParameters(name));
        return result;
    }

    public Map<String, Object> getContextMap(String name) {
        final Map<String, Object> defaultContext = getContextObjects(DEFAULT_KEY);
        final Map<String, Object> context = getContextObjects(name);

        if (defaultContext.isEmpty()) {
            return (context.isEmpty()) ? context : new HashMap<>(context);
        }
        else if (context.isEmpty()) {
            return new HashMap<>(defaultContext);
        }

        final Map<String, Object> result = new HashMap<>();
        result.putAll(defaultContext);
        result.putAll(context);
        return result;
    }

    Map<String, String[]> getFixedParameters(String key) {
        return getParameters(this.fixedParameters, key);
    }

    Map<String, String[]> getDefaultParameters(String key) {
        return getParameters(this.defaultParameters, key);
    }

    Map<String, Object> getContextObjects(String key) {
        if (key != null) {
            final Map<String, Object> result = this.contextObjects.get(key);
            if (result != null) {
                return result;
            }
        }
        return Collections.emptyMap();
    }

    private Map<String, String[]> getParameters(Map<String, ConcurrentMap<String, String[]>> source,
            String key) {
        final Map<String, String[]> result = source.get(key);
        if (result != null) {
            return result;
        }
        return Collections.emptyMap();
    }

    boolean containsKey(String key) {
        return this.fixedParameters.containsKey(key)
                || this.defaultParameters.containsKey(key)
                || this.contextObjects.containsKey(key);
    }

    ZoneImpl copy(String name) {
        final ZoneImpl result = new ZoneImpl(name);
        copyAll(this.defaultParameters, result.defaultParameters);
        copyAll(this.fixedParameters, result.fixedParameters);
        copyAll(this.contextObjects, result.contextObjects);
        result.interceptors.putAll(this.interceptors);
        result.templateBase = this.templateBase;
        result.setMarketStrategies(this.marketStrategies);
        result.quoteComparator = this.quoteComparator;
        result.quoteNameStrategy = this.quoteNameStrategy;
        result.marketNameStrategy = this.marketNameStrategy;
        result.instrumentNameStrategy = this.instrumentNameStrategy;
        result.tickerStrategy = this.tickerStrategy;
        result.baseQuoteFilter = this.baseQuoteFilter;
        return result;
    }

    private static <T> void copyAll(Map<String, ConcurrentMap<String, T>> map,
            Map<String, ConcurrentMap<String, T>> target) {
        for (Map.Entry<String, ConcurrentMap<String, T>> entry : map.entrySet()) {
            target.put(entry.getKey(), new ConcurrentHashMap<>(entry.getValue()));
        }
    }

    private Map<String, ConcurrentMap<String, String[]>> newParametersMap() {
        return new ConcurrentHashMap<>();
    }

    private ConcurrentMap<String, String[]> newParameterMap() {
        return new ConcurrentHashMap<>();
    }
}
