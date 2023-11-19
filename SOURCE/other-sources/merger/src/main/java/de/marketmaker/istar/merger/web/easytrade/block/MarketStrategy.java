/*
 * MarketStrategy.java
 *
 * Created on 15.01.2008 14:28:50
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.block;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import net.jcip.annotations.Immutable;

import de.marketmaker.istar.domain.instrument.Instrument;
import de.marketmaker.istar.domain.instrument.InstrumentTypeEnum;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.domain.instrument.QuoteOrder;
import de.marketmaker.istar.merger.provider.UnknownSymbolException;

import static de.marketmaker.istar.merger.web.easytrade.block.LbbwMarketStrategy.*;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
@Immutable
public class MarketStrategy {
    private static final QuoteSelector DEFAULT_FONDS
            = new QuoteSelectors.ByType(InstrumentTypeEnum.FND, QuoteSelectors.group(
            new QuoteSelectors.BySymbolVwdfeedSuffix(".FONDS.EUR"),
            new QuoteSelectors.BySymbolVwdfeedSuffix(".FONDS.CHF"),
            new QuoteSelectors.BySymbolVwdfeedSuffix(".FONDS.USD"),
            new QuoteSelectors.BySymbolVwdfeedSuffix(".FONDS.GBP"),
            new QuoteSelectors.ByMarketsymbol("FONDS")));

    private static final QuoteSelector SWISS_FONDS
            = new QuoteSelectors.ByType(InstrumentTypeEnum.FND, QuoteSelectors.group(
            new QuoteSelectors.BySymbolVwdfeedSuffix(".FONDS.CHF"),
            new QuoteSelectors.BySymbolVwdfeedSuffix(".FONDS.EUR"),
            new QuoteSelectors.BySymbolVwdfeedSuffix(".FONDS.USD"),
            new QuoteSelectors.BySymbolVwdfeedSuffix(".FONDS.GBP"),
            new QuoteSelectors.ByMarketsymbol("FONDS")));

    // prefer Forex and London Fixing for MER, prefer USD over EUR
    private static final QuoteSelector DEFAULT_MER = new QuoteSelectors.ByType(InstrumentTypeEnum.MER,
            QuoteSelectors.group(
                    new QuoteSelectors.ByMarketAndCurrencySymbol("FX", "USD")
                    , new QuoteSelectors.ByMarketAndCurrencySymbol("FX", "EUR")
                    , new QuoteSelectors.ByMarketAndCurrencySymbol("FXVWD", "USD")
                    , new QuoteSelectors.ByMarketAndCurrencySymbol("FXVWD", "EUR")
                    , new QuoteSelectors.ByMarketAndCurrencySymbol("LFIX", "USD")
                    , new QuoteSelectors.ByMarketAndCurrencySymbol("LFIX", "EUR")
            ));

    // CORE-14078 vwd-rimpar prefers to have FFMST prior to STG (after EUWAX was merged into STG)
    private static final QuoteSelector FFMST_OR_EUWAX = QuoteSelectors.group(
        new QuoteSelectors.ByMarketsymbol("FFMST"),
        new QuoteSelectors.ByMarketsymbol("STG"),
        new QuoteSelectors.ByMarketsymbol("EUWAX"));

    public static final MarketStrategy STANDARD = new MarketStrategy.Builder()
            .withFilter(QuoteFilters.WITH_PRICES)
            .withSelectors(Arrays.asList(
                    new QuoteSelectors.ByType(InstrumentTypeEnum.FND, QuoteSelectors.byMarket("FONDS")),
                    QuoteSelectors.HOME_EXCHANGE,
                    QuoteSelectors.SYMBOL_RELEVANCE
            ))
            .build();


    public static final MarketStrategy DZ_BANK = new MarketStrategy.Builder()
            .withFilter(QuoteFilters.WITH_PRICES)
            .withSelectors(Arrays.asList(
                    LBBW_FONDS,
                    MSCI,
                    DEFAULT_MER,
                    new QuoteSelectors.ByOrder(QuoteOrder.VOLUME_DE),
                    EUWAX_OR_FFMST,
                    GERMAN_HOME_EXHANGE_OR_INDEX,
                    LBBW_PREFERRED_EXCHANGES,
                    QuoteSelectors.SELECT_FIRST
            ))
            .build();

    public static final MarketStrategy PORTFOLIO_MANAGER = new MarketStrategy.Builder()
            .withFilter(QuoteFilters.PM_ABO)
            .withSelectors(Arrays.asList(
                    QuoteSelectors.SELECT_BEST_PM_QUOTE,
                    GERMAN_HOME_EXHANGE_OR_INDEX,
                    new QuoteSelectors.ByOrder(QuoteOrder.VOLUME),
                    QuoteSelectors.SELECT_FIRST
            ))
            .build();

    public static final MarketStrategy ITALIAN = new MarketStrategy.Builder()
            .withFilter(QuoteFilters.WITH_PRICES)
            .withSelectors(Arrays.asList(
                    LBBW_FONDS,
                    MSCI,
                    DEFAULT_MER,
                    new QuoteSelectors.ByMarketsymbol("IT,ITMOT"),
                    new QuoteSelectors.ByCountry("IT,ITMOT"),
                    new QuoteSelectors.BySymbolVwdfeedSuffix(".FONDIT.EUR"),
                    new QuoteSelectors.ByOrder(QuoteOrder.VOLUME),
                    EUWAX_OR_FFMST,
                    GERMAN_HOME_EXHANGE_OR_INDEX,
                    LBBW_PREFERRED_EXCHANGES,
                    QuoteSelectors.SELECT_FIRST
            ))
            .build();

    public static final MarketStrategy DUTCH = new MarketStrategy.Builder()
            .withFilter(QuoteFilters.WITH_PRICES)
            .withSelectors(Arrays.asList(
                    LBBW_FONDS,
                    MSCI,
                    DEFAULT_MER,
                    new QuoteSelectors.ByMarketsymbol("NL"),
                    new QuoteSelectors.ByCountry("NL"),
                    new QuoteSelectors.ByOrder(QuoteOrder.VOLUME),
                    EUWAX_OR_FFMST,
                    GERMAN_HOME_EXHANGE_OR_INDEX,
                    LBBW_PREFERRED_EXCHANGES,
                    QuoteSelectors.SELECT_FIRST
            ))
            .build();

    public static final MarketStrategy BELGIAN = new MarketStrategy.Builder()
            .withFilter(QuoteFilters.WITH_PRICES)
            .withSelectors(Arrays.asList(
                    LBBW_FONDS,
                    MSCI,
                    DEFAULT_MER,
                    new QuoteSelectors.ByMarketsymbol("BL"),
                    new QuoteSelectors.ByCountry("BE"),
                    new QuoteSelectors.ByOrder(QuoteOrder.VOLUME),
                    EUWAX_OR_FFMST,
                    GERMAN_HOME_EXHANGE_OR_INDEX,
                    LBBW_PREFERRED_EXCHANGES,
                    QuoteSelectors.SELECT_FIRST
            ))
            .build();

    public static final MarketStrategy FRENCH = new MarketStrategy.Builder()
            .withFilter(QuoteFilters.WITH_PRICES)
            .withSelectors(Arrays.asList(
                    LBBW_FONDS,
                    MSCI,
                    DEFAULT_MER,
                    new QuoteSelectors.ByMarketsymbol("FR"),
                    new QuoteSelectors.ByCountry("FR"),
                    new QuoteSelectors.ByOrder(QuoteOrder.VOLUME),
                    EUWAX_OR_FFMST,
                    GERMAN_HOME_EXHANGE_OR_INDEX,
                    LBBW_PREFERRED_EXCHANGES,
                    QuoteSelectors.SELECT_FIRST
            ))
            .build();

    // used in riskservice.prop
    public static final MarketStrategy RISK_SERVICE = new Builder()
            .withFilter(QuoteFilters.WITH_PRICES)
            .withSelectors(Arrays.asList(
                    new QuoteSelectors.BySymbolVwdcode("EEM.N"),
                    new QuoteSelectors.ByType(InstrumentTypeEnum.FND, QuoteSelectors.group(
                            new QuoteSelectors.ByMarketsymbol("XETF"),
                            new QuoteSelectors.ByMarketsymbol("CH")
                    )), // Mail TReischmann@vwd.com, 2012-12-06
                    LBBW_FONDS,
                    MSCI,
                    DEFAULT_MER,
                    EUWAX_OR_FFMST,
                    GERMAN_HOME_EXHANGE_OR_INDEX,
                    LBBW_PREFERRED_EXCHANGES,
                    new QuoteSelectors.ByMarketsymbol("XCOR"), // Mail TReischmann@vwd.com 2012-07-26
                    new QuoteSelectors.BySymbolVwdfeedRegex(Pattern.compile("5\\.GB[A-O,Q-Z]..\\d+J.BONDS")), // Mail TReischmann@vwd.com 2012-08-24
                    QuoteSelectors.LONGEST_HISTORY,
                    QuoteSelectors.SELECT_FIRST
            ))
            .build();

    public static final MarketStrategy RISK_SERVICE_TEST = new Builder()
            .withFilter(QuoteFilters.WITH_PRICES)
            .withSelectors(Arrays.asList(
                    new QuoteSelectors.BySymbolVwdcode("EEM.N"),
                    new QuoteSelectors.ByType(InstrumentTypeEnum.FND, QuoteSelectors.group(
                            new QuoteSelectors.ByMarketsymbol("XETF"),
                            new QuoteSelectors.ByMarketsymbol("CH")
                    )), // Mail TReischmann@vwd.com, 2012-12-06
                    LBBW_FONDS,
                    MSCI,
                    DEFAULT_MER,
                    EUWAX_OR_FFMST,
                    GERMAN_HOME_EXHANGE_OR_INDEX,
                    LBBW_PREFERRED_EXCHANGES,
                    new QuoteSelectors.ByMarketsymbol("XCOR"), // Mail TReischmann@vwd.com 2012-07-26
                    new QuoteSelectors.BySymbolVwdfeedRegex(Pattern.compile("5\\.GB[A-O,Q-Z]..\\d+J.BONDS")), // Mail TReischmann@vwd.com 2012-08-24
                    QuoteSelectors.LONGEST_HISTORY,
                    QuoteSelectors.SELECT_FIRST
            ))
            .build();

    public static final MarketStrategy RIMPAR = new MarketStrategy.Builder()
        .withFilter(QuoteFilters.WITH_PRICES)
        .withSelectors(Arrays.asList(
            LBBW_FONDS,
            MSCI,
            FFMST_OR_EUWAX,
            GERMAN_HOME_EXHANGE_OR_INDEX,
            LBBW_PREFERRED_EXCHANGES,
            QuoteSelectors.SELECT_FIRST
        ))
        .build();

    public static final MarketStrategy SWISS = new MarketStrategy.Builder()
            .withFilter(QuoteFilters.WITH_PRICES)
            .withSelectors(Arrays.asList(
                    SWISS_FONDS,
                    MSCI,
                    DEFAULT_MER,
                    new QuoteSelectors.ByMarketsymbol("VX"),
                    new QuoteSelectors.ByMarketsymbol("CH"),
                    new QuoteSelectors.ByCountry("CH"),
                    new QuoteSelectors.ByOrder(QuoteOrder.VOLUME),
                    EUWAX_OR_FFMST,
                    GERMAN_HOME_EXHANGE_OR_INDEX,
                    LBBW_PREFERRED_EXCHANGES,
                    QuoteSelectors.SELECT_FIRST
            ))
            .build();

    /**
     * Defined by CORE-16234
     */
    private static final QuoteSelector AGI_MARKET = QuoteSelectors.group(
            new QuoteSelectors.ByMarketsymbol("ETR"),
            new QuoteSelectors.ByMarketsymbol("FFM"),
            new QuoteSelectors.ByMarketsymbol("BLN"),
            new QuoteSelectors.ByMarketsymbol("DDF"),
            new QuoteSelectors.ByMarketsymbol("HBG"),
            new QuoteSelectors.ByMarketsymbol("HNV"),
            new QuoteSelectors.ByMarketsymbol("MCH"),
            new QuoteSelectors.ByMarketsymbol("Q"),
            new QuoteSelectors.ByMarketsymbol("CHF"),
            new QuoteSelectors.ByMarketsymbol("BEKB"),
            new QuoteSelectors.ByMarketsymbol("ZKBZ"));

    public static final MarketStrategy ALLIANZ_GLOBAL_INVESTORS = new MarketStrategy.Builder()
            .withFilter(QuoteFilters.WITH_PRICES)
            .withSelectors(Arrays.asList(
                    AGI_MARKET,
                    new QuoteSelectors.ByType(InstrumentTypeEnum.FND, QuoteSelectors.byMarket("FONDS")),
                    QuoteSelectors.HOME_EXCHANGE,
                    QuoteSelectors.SYMBOL_RELEVANCE
            ))
            .build();

    // from old merger implementation: DefaultWPIdentifier.GERMAN_HOMEEXCHANGE_STRATEGY_MARKETS
    public static final MarketStrategy MERGER_OLD_STRATEGY = new Builder()
            .withFilter(QuoteFilters.WITH_PRICES)
            .withSelectors(Arrays.asList((QuoteSelector)
                    new QuoteSelectors.ByMarketsymbol("FFM"),
                    new QuoteSelectors.ByMarketsymbol("STG"),
                    new QuoteSelectors.ByMarketsymbol("BLN"),
                    new QuoteSelectors.ByMarketsymbol("MCH"),
                    new QuoteSelectors.ByMarketsymbol("DDF"),
                    new QuoteSelectors.ByMarketsymbol("HBG"),
                    new QuoteSelectors.ByMarketsymbol("HNV"),
                    new QuoteSelectors.ByMarketsymbol("BRE"),
                    new QuoteSelectors.ByMarketsymbol("ETR"),
                    new QuoteSelectors.ByMarketsymbol("EEU"),
                    new QuoteSelectors.ByMarketsymbol("EUS"),
                    new QuoteSelectors.ByMarketsymbol("FFMST"),
                    new QuoteSelectors.ByMarketsymbol("EUWAX"),
                    new QuoteSelectors.ByMarketsymbol("FONDS")
                    ))
            .build();

    public static final MarketStrategy FONDFIRST_OLD_STRATEGY = new Builder()
            .withFilter(QuoteFilters.WITH_PRICES)
            .withSelectors(Arrays.asList((QuoteSelector)
                    new QuoteSelectors.ByMarketsymbol("FONDS"),
                    new QuoteSelectors.ByMarketsymbol("FFM"),
                    new QuoteSelectors.ByMarketsymbol("STG"),
                    new QuoteSelectors.ByMarketsymbol("BLN"),
                    new QuoteSelectors.ByMarketsymbol("MCH"),
                    new QuoteSelectors.ByMarketsymbol("DDF"),
                    new QuoteSelectors.ByMarketsymbol("HBG"),
                    new QuoteSelectors.ByMarketsymbol("HNV"),
                    new QuoteSelectors.ByMarketsymbol("BRE"),
                    new QuoteSelectors.ByMarketsymbol("ETR"),
                    new QuoteSelectors.ByMarketsymbol("EEU"),
                    new QuoteSelectors.ByMarketsymbol("EUS"),
                    new QuoteSelectors.ByMarketsymbol("FFMST"),
                    new QuoteSelectors.ByMarketsymbol("EUWAX")
            ))
            .build();

    public static final MarketStrategy EUWAXFIRST_OLD_STRATEGY = new Builder()
            .withFilter(QuoteFilters.WITH_PRICES)
            .withSelectors(Arrays.asList((QuoteSelector)
                    new QuoteSelectors.ByMarketsymbol("EUWAX"),
                    new QuoteSelectors.ByMarketsymbol("FFM"),
                    new QuoteSelectors.ByMarketsymbol("STG"),
                    new QuoteSelectors.ByMarketsymbol("BLN"),
                    new QuoteSelectors.ByMarketsymbol("MCH"),
                    new QuoteSelectors.ByMarketsymbol("DDF"),
                    new QuoteSelectors.ByMarketsymbol("HBG"),
                    new QuoteSelectors.ByMarketsymbol("HNV"),
                    new QuoteSelectors.ByMarketsymbol("BRE"),
                    new QuoteSelectors.ByMarketsymbol("ETR"),
                    new QuoteSelectors.ByMarketsymbol("EEU"),
                    new QuoteSelectors.ByMarketsymbol("EUS"),
                    new QuoteSelectors.ByMarketsymbol("FFMST"),
                    new QuoteSelectors.ByMarketsymbol("FONDS")
            ))
            .build();

    public static final MarketStrategy NEW_LBBW_MARKET_STRATEGY = new MarketStrategy.Builder()
            .withFilter(QuoteFilters.WITH_PRICES)
            .withSelectors(Arrays.asList(
                    LbbwMarketStrategy.LBBW_FONDS,
                    LbbwMarketStrategy.MSCI,
                    QuoteSelectors.SWISS_EXHANGE_OR_INDEX,
                    EUWAX_OR_FFMST,
                    LbbwMarketStrategy.LBBW_PREFERRED_EXCHANGES,
                    QuoteSelectors.SELECT_FIRST
            ))
            .build();


    // Clone from genobroker-in.vm with adaptions for swiss symbols
    // before 17:30 version
    // FFMST,FFM,FFMFO,STG,EUWAX,ETR,EEU,EUS,ETRI,XETF,BLN,FONDS
    public static final MarketStrategy CH_JUL19_GENO1_MARKET_STRATEGY = new MarketStrategy.Builder()
            .withFilter(QuoteFilters.WITH_PRICES)
            .withSelectors(Arrays.asList(
                    QuoteSelectors.SWISS_EXHANGE_OR_INDEX,
                    new QuoteSelectors.ByMarketsymbol("FFMST"),
                    new QuoteSelectors.ByMarketsymbol("FFM"),
                    new QuoteSelectors.ByMarketsymbol("FFMFO"),
                    new QuoteSelectors.ByMarketsymbol("STG"),
                    new QuoteSelectors.ByMarketsymbol("EUWAX"),
                    new QuoteSelectors.ByMarketsymbol("ETR"),
                    new QuoteSelectors.ByMarketsymbol("EEU"),
                    new QuoteSelectors.ByMarketsymbol("EUS"),
                    new QuoteSelectors.ByMarketsymbol("ETRI"),
                    new QuoteSelectors.ByMarketsymbol("XETF"),
                    new QuoteSelectors.ByMarketsymbol("BLN"),
                    new QuoteSelectors.ByMarketsymbol("FONDS")
            ))
            .build();

    // Clone from genobroker-in.vm with adaptions for swiss symbols
    // after 17:30 version
    // FFMST,FFM,FFMFO,STG,EUWAX,BLN,FONDS
    public static final MarketStrategy CH_JUL19_GENO2_MARKET_STRATEGY = new MarketStrategy.Builder()
            .withFilter(QuoteFilters.WITH_PRICES)
            .withSelectors(Arrays.asList(
                    QuoteSelectors.SWISS_EXHANGE_OR_INDEX,
                    new QuoteSelectors.ByMarketsymbol("FFMST"),
                    new QuoteSelectors.ByMarketsymbol("FFM"),
                    new QuoteSelectors.ByMarketsymbol("FFMFO"),
                    new QuoteSelectors.ByMarketsymbol("STG"),
                    new QuoteSelectors.ByMarketsymbol("EUWAX"),
                    new QuoteSelectors.ByMarketsymbol("BLN"),
                    new QuoteSelectors.ByMarketsymbol("FONDS")
            ))
            .build();

    public static final MarketStrategy RHEINLAND_VERSICHERUNGEN = new MarketStrategy.Builder()
        .withFilter(QuoteFilters.WITH_PRICES)
        .withSelectors(Arrays.asList(
            new QuoteSelectors.ByMarketsymbol("STG"),
            new QuoteSelectors.ByMarketsymbol("FFM"),
            new QuoteSelectors.ByMarketsymbol("MCH"),
            new QuoteSelectors.ByMarketsymbol("DDF"),
            new QuoteSelectors.ByMarketsymbol("BLN"),
            new QuoteSelectors.ByMarketsymbol("HBG"),
            new QuoteSelectors.ByMarketsymbol("HNV"),
            new QuoteSelectors.ByMarketsymbol("BRE"),
            new QuoteSelectors.ByMarketsymbol("ETR"),
            new QuoteSelectors.ByMarketsymbol("EEU"),
            new QuoteSelectors.ByMarketsymbol("EUS"),
            new QuoteSelectors.ByMarketsymbol("FFMST"),
            new QuoteSelectors.ByMarketsymbol("EUWAX"),
            new QuoteSelectors.ByMarketsymbol("FONDS")
        ))
        .build();

    public static final class Builder {
        private Deque<QuoteFilter> filters;

        private Deque<QuoteSelector> selectors;

        private Comparator<Quote> comparator;

        public Builder() {
            this.filters = new LinkedList<>();
            this.selectors = new LinkedList<>();
        }

        public Builder(MarketStrategy ms) {
            this.filters = new LinkedList<>(ms.filters);
            this.selectors = new LinkedList<>(ms.selectors);
        }

        public Builder withFilter(QuoteFilter filter) {
            this.filters.addFirst(filter);
            return this;
        }

        public Builder withFilterLast(QuoteFilter filter) {
            this.filters.addLast(filter);
            return this;
        }

        public Builder withFilters(List<QuoteFilter> filters) {
            this.filters = new LinkedList<>(filters);
            return this;
        }

        public Builder withSelector(QuoteSelector selector) {
            this.selectors.addFirst(selector);
            return this;
        }

        public Builder withSelectors(List<QuoteSelector> selectors) {
            this.selectors = new LinkedList<>(selectors);
            return this;
        }

        public Builder withComparator(Comparator<Quote> comparator) {
            this.comparator = comparator;
            return this;
        }

        public MarketStrategy build() {
            return new MarketStrategy(this);
        }
    }

    private final Deque<QuoteFilter> filters;

    private final Deque<QuoteSelector> selectors;

    private final Comparator<Quote> comparator;

    private MarketStrategy(Builder b) {
        this.filters = new LinkedList<>(b.filters);
        this.selectors = new LinkedList<>(b.selectors);
        this.comparator = b.comparator;
    }

    /**
     * Selects a quote from the quotes in the given instrument
     * @param instrument has quotes
     * @return selected quote or null
     */
    public Quote getQuote(Instrument instrument) {
        return getQuote(instrument, getQuotes(instrument));
    }

    public Quote getQuote(Instrument instrument, List<Quote> quotes) {
        List<Quote> tmp = quotes;
        for (final QuoteFilter filter : this.filters) {
            tmp = filter.apply(tmp);
            if (tmp.isEmpty()) {
                break;
            }
        }

        if (this.comparator != null) {
            tmp.sort(this.comparator);
        }

        try {
            for (QuoteSelector selector : this.selectors) {
                final Quote quote = selector.select(instrument, tmp);
                if (quote != null) {
                    return quote;
                }
            }
        } catch (UnknownSymbolException e) {
            // TODO: add some information: e.setMarketStrategy(this);
            throw e;
        }
        return null;
    }


    private List<Quote> getQuotes(Instrument instrument) {
        if (this.comparator != null && this.filters.isEmpty()) {
            // we need a modifiable list for sorting
            return new ArrayList<>(instrument.getQuotes());
        }
        return instrument.getQuotes();
    }
    
    public static String identifyMarketStrategy(MarketStrategy marketStrategy) {
        
        String name = marketStrategy.toString();

        if (marketStrategy == ALLIANZ_GLOBAL_INVESTORS) {name = "ALLIANZ_GLOBAL_INVESTORS";}
        if (marketStrategy == BELGIAN) {name = "BELGIAN";}
        if (marketStrategy == CH_JUL19_GENO1_MARKET_STRATEGY) {name = "CH_JUL19_GENO1_MARKET_STRATEGY";}
        if (marketStrategy == CH_JUL19_GENO2_MARKET_STRATEGY) {name = "CH_JUL19_GENO2_MARKET_STRATEGY";}
        if (marketStrategy == DUTCH) {name = "DUTCH";}
        if (marketStrategy == DZ_BANK) {name = "DZ_BANK";}
        if (marketStrategy == EUWAXFIRST_OLD_STRATEGY) {name = "EUWAXFIRST_OLD_STRATEGY";}
        if (marketStrategy == FONDFIRST_OLD_STRATEGY) {name = "FONDFIRST_OLD_STRATEGY";}
        if (marketStrategy == FRENCH) {name = "FRENCH";}
        if (marketStrategy == ITALIAN) {name = "ITALIAN";}
        if (marketStrategy == MERGER_OLD_STRATEGY) {name = "MERGER_OLD_STRATEGY";}
        if (marketStrategy == NEW_LBBW_MARKET_STRATEGY) {name = "NEW_LBBW_MARKET_STRATEGY";}
        if (marketStrategy == PORTFOLIO_MANAGER) {name = "PORTFOLIO_MANAGER";}
        if (marketStrategy == RIMPAR) {name = "RIMPAR";}
        if (marketStrategy == RISK_SERVICE) {name = "RISK_SERVICE";}
        if (marketStrategy == RISK_SERVICE_TEST) {name = "RISK_SERVICE_TEST";}
        if (marketStrategy == STANDARD) {name = "STANDARD";}
        if (marketStrategy == SWISS) {name = "SWISS";}
        if (marketStrategy == RHEINLAND_VERSICHERUNGEN) {name = "RHEINLAND_VERSICHERUNGEN";}

        return name;
    }
}
