/*
 * MarketMapping.java
 *
 * Created on 22.06.2010 14:56:44
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.bew;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StringUtils;

import de.marketmaker.istar.common.util.PropertiesLoader;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.merger.web.easytrade.block.QuoteSelector;
import de.marketmaker.istar.merger.web.easytrade.block.QuoteSelectors;

/**
 * @author oflege
 */
class MarketMapping {
    private static Map<String, String> NUMERIC_MARKETS = new HashMap<>();

    private static Map<String, QuoteSelector> SELECTORS = new HashMap<>();

    private static Map<String, String> MARKET_MAPPINGS = new HashMap<>();

    private static Map<String, String> REVERSE_MARKET_MAPPINGS_BY_SYMBOLSUFFIX = new HashMap<>();

    private static Map<String, String> REVERSE_MARKET_MAPPINGS = new HashMap<>();

    private static final Map<String, String> PREFERRED_CURRENCIES_BY_MARKET
            = new HashMap<>();

    static {
        PREFERRED_CURRENCIES_BY_MARKET.put("VX", "CHF");
        PREFERRED_CURRENCIES_BY_MARKET.put("CH", "CHF");
        PREFERRED_CURRENCIES_BY_MARKET.put("SCO_CH", "CHF");

        final Properties marketmappings = load("marketmappings.properties");
        final Map<String, String> prios = new HashMap<>();
        for (String s : marketmappings.stringPropertyNames()) {
            final String value = marketmappings.getProperty(s).trim();
            final String mappedValue = StringUtils.hasText(value) ? value : s;

            final String[] strings = mappedValue.split(",");
            for (final String string : strings) {
                final String key = strip(string);

                if (key.indexOf(".") > 0) {
                    REVERSE_MARKET_MAPPINGS_BY_SYMBOLSUFFIX.put(key, s);
                    continue;
                }

                if (string.endsWith("*")) {
                    prios.put(key, s);
                }
                else {
                    REVERSE_MARKET_MAPPINGS.put(key, s);
                }
            }

            final String m = strip(mappedValue);
            SELECTORS.put(s, createSelector(m));
            MARKET_MAPPINGS.put(s, m);
        }
        REVERSE_MARKET_MAPPINGS.putAll(prios);

        final Properties sources = load("sources.properties");
        for (String s : sources.stringPropertyNames()) {
            NUMERIC_MARKETS.put(s, sources.getProperty(s).trim());
        }
    }

    private static String strip(String mappedValue) {
        return mappedValue.replaceAll(Pattern.quote("*"), "");
    }

    private static Properties load(String name) {
        try {
            return PropertiesLoader.load(new ClassPathResource(name, MarketMapping.class));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    static QuoteSelector getSelector(String market) {
        final String resolvedMarket = resolve(market);
        final QuoteSelector result = SELECTORS.get(resolvedMarket);
        return result != null ? result : QuoteSelectors.byMarket(resolvedMarket);
    }

    static List<String> getVwdMarketCodes(String market) {
        final String str = getVwdMarketCode(market);
        return (str != null) ? Arrays.asList(str.split(",")) : Collections.<String>emptyList();
    }

    static String getVwdMarketCode(String market) {
        if (NUMERIC_MARKETS.containsKey(market)) {
            return getVwdMarketCode(NUMERIC_MARKETS.get(market));
        }
        final String result = MARKET_MAPPINGS.get(market);
        return (result != null) ? result : market;
    }

    static String getBewOldMarketCode(Quote quote) {
        final String vwdmarket = quote.getSymbolVwdfeedMarket();
        final String currencyIso = quote.getCurrency().getSymbolIso();

        if ("RU".equals(vwdmarket)) {
            if ("USD".equals(currencyIso)) {
                return "RTU";
            }
            return "RTS";
        }

        if ("FONDS".equals(vwdmarket)) {
            final String vwdcode = quote.getSymbolVwdcode();
            final int index = vwdcode.indexOf(".FONDS.");
            final String key = vwdcode.substring(index + 1);
            final String mapping = REVERSE_MARKET_MAPPINGS_BY_SYMBOLSUFFIX.get(key);
            if (mapping != null) {
                return mapping;
            }
        }

        if ("SWXFO".equals(vwdmarket)) {
            final String key = "-" + currencyIso + "." + vwdmarket;
            final String mapping = REVERSE_MARKET_MAPPINGS_BY_SYMBOLSUFFIX.get(key);
            if (mapping != null) {
                return mapping;
            }
        }

        if ("UK".equals(vwdmarket)) {
            if ("GBP".equals(currencyIso)) {
                return "LOG";
            }
            if ("GBX".equals(currencyIso)) {
                return "LON";
            }
        }
        if ("JB".equals(vwdmarket)) {
            if (quote.getCurrency().getId() == 69) {
                return "JNR";
            }
            if (quote.getCurrency().getId() == 1762) {
                return "JNB";
            }
        }

        final String result = REVERSE_MARKET_MAPPINGS.get(vwdmarket);
        return result != null ? result : vwdmarket;
    }

    private static String resolve(String market) {
        if (NUMERIC_MARKETS.containsKey(market)) {
            return NUMERIC_MARKETS.get(market);
        }
        // Zahlensuffix ignorieren
        if (market.matches("[A-Z]+[0-9]")) {
            return market.substring(0, market.length() - 1);
        }
        return market;
    }

    private static QuoteSelector createSelector(String value) {
        final String[] s = value.split(",");
        if (s.length == 1) {
            return doCreateSelector(s[0]);
        }
        return createSelectorGroup(s);
    }

    private static QuoteSelector createSelectorGroup(String[] s) {
        final QuoteSelector[] selectors = new QuoteSelector[s.length];
        for (int i = 0; i < s.length; i++) {
            selectors[i] = doCreateSelector(s[i]);
        }
        return QuoteSelectors.group(selectors);
    }

    private static QuoteSelector doCreateSelector(String ms) {
        final int p = ms.indexOf('.');
        if (ms.startsWith("-")) {
            return new QuoteSelectors.ByMarketAndCurrencySymbol(ms.substring(p + 1), ms.substring(1, p));
        }
        if (p < 0) {
            final String currency = PREFERRED_CURRENCIES_BY_MARKET.get(ms);
            if (currency != null) {
                return QuoteSelectors.group(new QuoteSelectors.ByMarketAndCurrencySymbol(ms, currency),
                        new QuoteSelectors.ByMarketsymbol(ms));
            }

            return new QuoteSelectors.ByMarketsymbol(ms);
        }
        return new QuoteSelectors.BySymbolVwdfeedSuffix(ms);
    }

    public static void main(String[] args) {
        final List<String> markets = Arrays.asList("AU", "BBA", "HK", "JAS", "LMEP", "MSCI", "MSCIEM", "NZX", "OED", "SIM", "SIP", "TK", "WIEN", "XPRA", "XPRAB", "XPRAS");
        for (final Map.Entry<String, String> entry : MARKET_MAPPINGS.entrySet()) {
            for (final String market : markets) {
                if (entry.getValue().indexOf(market) >= 0) {
                    System.out.println(entry.getKey() + "=" + entry.getValue());
                }
            }
        }
    }
}
