/*
 * MarketStrategyUtils.java
 *
 * Created on 15.01.2008 14:30:44
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.block;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import de.marketmaker.istar.domain.instrument.InstrumentTypeEnum;
import de.marketmaker.istar.domain.instrument.QuoteOrder;
import de.marketmaker.istar.merger.context.RequestContextHolder;

import static de.marketmaker.istar.domain.instrument.InstrumentTypeEnum.CUR;
import static de.marketmaker.istar.domain.instrument.InstrumentTypeEnum.MER;
import static de.marketmaker.istar.merger.context.RequestContext.KEY_RECENTTRADE_QID;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public abstract class MarketStrategyFactory {

    private static final Logger logger = LoggerFactory.getLogger(MarketStrategyFactory.class);

    private static final Pattern STRATEGY_PATTERN
            = Pattern.compile("(market|currency|underlying|order|bisKeyMarket|name)((,[\\w]+(=([\\w]+))?)*):(.+)");

    private static final Map<String, String[]> CURRENCY_TO_PREFERRED_MARKET = new HashMap<String, String[]>() {
        {
            put("USD", new String[]{"N", "Q", "FXVWD"});
            put("CHF", new String[]{"SWX", "VX"});
            put("EUR", new String[]{"FFM", "FFMST", "STG", "EUWAX", "ETR"});
        }
    };

    public static MarketStrategy getStrategy(String market, String marketStrategyStr) {
        return getStrategy((marketStrategyStr != null) ? marketStrategyStr : toStrategyStr(market));
    }

    private static String toStrategyStr(String market) {
        return (market != null) ? ("market,with_prices:" + market) : null;
    }

    public static MarketStrategy getDefaultMarketStrategy() {
        return RequestContextHolder.getRequestContext().getMarketStrategy();
    }

    public static MarketStrategy getDefaultMarketStrategy(String name) {
        return RequestContextHolder.getRequestContext().getMarketStrategy(name);
    }

    public static MarketStrategy getStrategy(String marketStrategy) {
        return byStrategy(marketStrategy, null);
    }

    /**
     * Creates a MarketStrategy by evaluating the marketStrategy parameter
     * @param msDef strategy definition
     * @param ms default strategy; if null, the defaultStrategy will be retrieved from the current
     * {@link de.marketmaker.istar.merger.context.RequestContext}.
     * @return new MarketStrategy
     */
    public static MarketStrategy byStrategy(String msDef, MarketStrategy ms) {
        if (!StringUtils.hasText(msDef)) {
            return (ms != null) ? ms : getDefaultMarketStrategy();
        }

        if ("recenttrade".equals(msDef)) {
            final Long qid = (Long) RequestContextHolder.getRequestContext().get(KEY_RECENTTRADE_QID);
            return new MarketStrategy.Builder().withSelector(new QuoteSelectors.ByQid(qid)).build();
        }

        final Matcher matcher = STRATEGY_PATTERN.matcher(msDef);
        if (!matcher.matches()) {
            return (ms != null) ? ms : getDefaultMarketStrategy(msDef);
        }

        final String strategyName = matcher.group(1);
        final Map<String, String> params = getProperties(matcher.group(2));
        final String value = matcher.group(6);

        if ("name".equals(strategyName)) {
            try {
                Field strategyField = MarketStrategy.class.getField(value.toUpperCase());
                if (strategyField.getType().equals(MarketStrategy.class) && Modifier.isStatic(strategyField.getModifiers())) {
                    return (MarketStrategy) strategyField.get(null);
                }
            } catch (Exception e) {
                logger.info("Requested strategy " + value + " was not found in the class MarketStrategy", e);
                return getDefaultMarketStrategy();
            }
        }

        final MarketStrategy.Builder builder = params.containsKey("no_default")
                ? new MarketStrategy.Builder()
                : new MarketStrategy.Builder((ms != null) ? ms : getDefaultMarketStrategy(params.get("default")));

        if (params.containsKey("with_prices")) {
            builder.withFilters(Collections.singletonList(QuoteFilters.WITH_PRICES));
        }
        else {
            builder.withFilters(Collections.singletonList(QuoteFilters.WITH_VWDSYMBOL));
        }

        if ("order".equals(strategyName)) {
            builder.withSelector(byOrder(value));
        }
        else if ("currency".equals(strategyName)) {
            builder.withSelector(new QuoteSelectors.ByCurrencySymbol(value));
            final String[] markets = CURRENCY_TO_PREFERRED_MARKET.get(value);
            if (markets != null) {
                builder.withSelector(new QuoteSelectors.ByMarketAndCurrencySymbol(
                        StringUtils.arrayToCommaDelimitedString(markets), value));
            }
        }
        else if ("underlying".equals(strategyName)) {
            final Set<String> currencies = new HashSet<>(Arrays.asList(value.split(",")));
            builder.withFilterLast(new QuoteFilters.PreferredCurrencyFilter(currencies));
            builder.withSelector(QuoteSelectors.SELECT_BEST_PM_QUOTE);
            builder.withSelector(new QuoteSelectors.ByType(MER, QuoteSelectors.byMarket("FXVWD,FX,FXX")));
            builder.withSelector(new QuoteSelectors.ByType(CUR, QuoteSelectors.byMarket("FXVWD,FX,FXX")));
        }
        else if ("market".equals(strategyName)) {
            final int currencyIndex = value.indexOf("[");
            if (currencyIndex < 0) {
                builder.withSelector(QuoteSelectors.byMarket(value));
            }
            else {
                final String market = value.substring(0, currencyIndex);
                final String currency = value.substring(currencyIndex + 1, value.length() - 1);

                builder.withSelector(new QuoteSelectors.ByMarketAndCurrencySymbol(market, currency));
            }
        }
        else if ("bisKeyMarket".equals(strategyName)) {
            builder.withSelector(QuoteSelectors.byBisKeyMarket(value));
        }

        return builder.build();
    }

    private static Map<String, String> getProperties(final String s) {
        if (!StringUtils.hasText(s)) {
            return Collections.emptyMap();
        }
        final String[] terms = StringUtils.commaDelimitedListToStringArray(s.substring(1));
        final Map<String, String> result = new HashMap<>();
        for (String term : terms) {
            final String[] keyValue = toKeyValue(term);
            result.put(keyValue[0], keyValue[1]);
        }
        return result;
    }

    private static String[] toKeyValue(String term) {
        final int p = term.indexOf('=');
        return (p == -1)
                ? new String[]{term, term}
                : new String[]{term.substring(0, p), term.substring(p + 1)};
    }

    public static QuoteSelector byOrder(String order) {
        return new QuoteSelectors.ByOrder(QuoteOrder.valueOf(order.toUpperCase()));
    }

    public static QuoteSelector byMarketAndCurrency(String market, String currency) {
        return new QuoteSelectors.ByMarketAndCurrencySymbol(market, currency);
    }

    /**
     * Creates a MarketStrategy that will only apply delegate strategy if the
     * instrument is of the given type (performance optimization)
     * @param type required type for applying delegate strategy
     * @param delegate used to select quote
     * @return composite strategy
     */
    public static QuoteSelector byType(InstrumentTypeEnum type, QuoteSelector delegate) {
        return new QuoteSelectors.ByType(type, delegate);
    }

    public static MarketStrategy defaultStrategy() {
        return MarketStrategy.STANDARD;
    }
}
