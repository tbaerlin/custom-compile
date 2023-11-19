/*
 * HomeExchangeHeuristic.java
 *
 * Created on 27.02.12 15:18
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.instrument.export;

import de.marketmaker.istar.domain.MarketcategoryEnum;
import de.marketmaker.istar.domain.instrument.Instrument;
import de.marketmaker.istar.domain.instrument.InstrumentTypeEnum;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.domainimpl.instrument.InstrumentDp2;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * Utility class to assign a home exchange to instruments for which no home exchange is defined in the mdp.
 * The home exchange is important because it is used by 
 * {@link de.marketmaker.istar.domain.instrument.InstrumentNameStrategies#WM_WP_NAME_KURZ} which in turn
 * is used by {@link SuggestionExporter}. If no home exchange is defined, that instrument name strategy
 * will use an arbitrary quote to resolve the name, and that is not optimal.
 *
 * 
 * @author oflege
 */
public class HomeExchangeHeuristic {
    private static final EnumSet<InstrumentTypeEnum> TYPES = EnumSet.of(
            InstrumentTypeEnum.STK, InstrumentTypeEnum.IND, InstrumentTypeEnum.FND, InstrumentTypeEnum.GNS);

    private static final String[] FUND_MARKETS = new String[]{"FONDS", "FONDNL", "FONDIT", "FONITI"};
    
    private static final String[] FUND_CURRENCIES = new String[]{
            "EUR","USD","CZK","GBP","CHF","PLN","SEK","SGD"
            ,"AUD","CAD","DKK","GBX","HKD","HUF","JPY","NOK","NZD","RUB","TRY"
    };

    private static Map<String, String[]> MARKET_BY_COUNTRY = new HashMap<>();

    static {
        MARKET_BY_COUNTRY.put("BE", new String[]{"BL"});
        MARKET_BY_COUNTRY.put("CA", new String[]{"TO", "Q"});
        MARKET_BY_COUNTRY.put("CH", new String[]{"CH", "SWXFO"});
        MARKET_BY_COUNTRY.put("DE", new String[]{"ETR", "FFM", "MCH", "BLN", "STG"});
        MARKET_BY_COUNTRY.put("ES", new String[]{"ES", "PT", "MAD"});
        MARKET_BY_COUNTRY.put("IT", new String[]{"IT"});
        MARKET_BY_COUNTRY.put("JP", new String[]{"TK"});
        MARKET_BY_COUNTRY.put("PG", new String[]{"AU"});
        MARKET_BY_COUNTRY.put("SG", new String[]{"SIP"});
        MARKET_BY_COUNTRY.put("US", new String[]{"N", "A", "Q"});
    }


    public static void ensureHomeExchange(InstrumentDp2 instrument) {
        if (instrument.getHomeExchange().getId() != 0L) {
            return;
        }
        if (!TYPES.contains(instrument.getInstrumentType())) {
            return;
        }

        final List<Quote> allQuotes = quotesWithVwdSymbol(instrument);
        if (allQuotes.isEmpty()) {
            return;
        }
        
        final Quote homeQuote = getHomeQuote(instrument, allQuotes);
        if (homeQuote != null) {
            instrument.setHomeExchange(homeQuote.getMarket());
            instrument.setHomeExchangeByHeuristic(true);
        }
    }

    private static Quote getHomeQuote(InstrumentDp2 instrument, List<Quote> allQuotes) {
        if (allQuotes.size() == 1) {
            return allQuotes.get(0);
        }

        HashSet<String> countryIsos = getCountryIso(instrument);
        if (instrument.getInstrumentType() == InstrumentTypeEnum.FND) {
            final Quote fundHomeQuote = getFundHomeQuote(allQuotes, countryIsos);
            if (fundHomeQuote != null) {
                return fundHomeQuote;
            }
        }
        for (String countryIso : countryIsos) {
            List<Quote> quotes = byCountry(allQuotes, countryIso);
            if (quotes.size() == 1) {
                return quotes.get(0);
            }

            Quote quote = byBoerse(quotes);
            if (quote == null) {
                quote = byMarketVwdfeed(allQuotes, countryIso);
            }

            if (quote != null) {
                return quote;
            }
        }

        return byBoerse(allQuotes);
    }

    private static HashSet<String> getCountryIso(Instrument instrument) {
        HashSet<String> result = new HashSet<>();
        String fromCountry = instrument.getCountry().getSymbolIso();
        if (fromCountry != null) {
            result.add(fromCountry);            
        }
        if (instrument.getSymbolIsin() != null) {
            result.add(instrument.getSymbolIsin().substring(0, 2));
        }
        return result;
    }


    private static Quote getFundHomeQuote(List<Quote> allQuotes, HashSet<String> countryIsos) {
        for (String market: FUND_MARKETS) {
            List<Quote> quotes = byMarketName(allQuotes, market);
            if (quotes.isEmpty()) {
                continue;
            }
            if (quotes.size() == 1) {
                return quotes.get(0);
            }
            if (countryIsos.contains("CH")) {
                List<Quote> chfQuotes = byVwdCodeSuffix(quotes, market + ".CHF");
                if (chfQuotes.size() == 1) {
                    return chfQuotes.get(0);
                }
            }
            if (countryIsos.contains("SE")) {
                List<Quote> seQuotes = byVwdCodeSuffix(quotes, market + ".SEK");
                if (seQuotes.size() == 1) {
                    return seQuotes.get(0);
                }
            }
            if (countryIsos.contains("GB")) {
                List<Quote> gbQuotes = byVwdCodeSuffix(quotes, market + ".GBP");
                if (gbQuotes.isEmpty()) {
                    gbQuotes = byVwdCodeSuffix(quotes, market + ".GBX");
                }
                if (gbQuotes.size() == 1) {
                    return gbQuotes.get(0);
                }
            }
            for (String currency: FUND_CURRENCIES) {
                List<Quote> curQuotes = byVwdCodeSuffix(quotes, market + "." + currency);
                if (curQuotes.size() == 1) {
                    return curQuotes.get(0);
                }

            }
        }
        return null;
    }

    private static List<Quote> quotesWithVwdSymbol(Instrument instrument) {
        ArrayList<Quote> result = new ArrayList<>();
        for (Quote quote : instrument.getQuotes()) {
            if (quote.getSymbolVwdfeed() != null) {
                result.add(quote);
            }
        }
        return result;
    }

    private static Quote byMarketVwdfeed(List<Quote> quotes, String country) {
        String[] vwdmarkets = MARKET_BY_COUNTRY.get(country);
        if (vwdmarkets == null) {
            return null;
        }
        for (String vwdmarket : vwdmarkets) {
            for (Quote quote : quotes) {
                if (vwdmarket.equals(quote.getMarket().getSymbolVwdfeed())) {
                    return quote;
                }
            }
        }
        return null;
    }

    private static List<Quote> byMarketName(List<Quote> quotes, String name) {
        ArrayList<Quote> result = new ArrayList<>(quotes.size());
        for (Quote quote : quotes) {
            if (name.equals(quote.getMarket().getSymbolVwdfeed())) {
                result.add(quote);
            }
        }
        return result;
    }

    private static List<Quote> byVwdCodeSuffix(List<Quote> quotes, String suffix) {
        ArrayList<Quote> result = new ArrayList<>(quotes.size());
        for (Quote quote : quotes) {
            String vwdcode = quote.getSymbolVwdcode();
            if (vwdcode != null && vwdcode.endsWith(suffix)) {
                result.add(quote);
            }
        }
        return result;
    }

    private static List<Quote> byCountry(List<Quote> quotes, String countryIso) {
        ArrayList<Quote> result = new ArrayList<>();
        for (Quote quote : quotes) {
            if (countryIso.equals(quote.getMarket().getCountry().getSymbolIso())) {
                result.add(quote);
            }
        }
        return result;
    }

    private static Quote byBoerse(List<Quote> quotes) {
        Quote result = null;
        for (Quote quote : quotes) {
            if (quote.getMarket().getMarketcategory() == MarketcategoryEnum.BOERSE) {
                if (result != null) {
                    return null;
                }
                result = quote;
            }
        }
        return result;
    }
}
