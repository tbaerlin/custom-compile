/*
 * QuoteFilters.java
 *
 * Created on 25.11.2009 14:53:07
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.block;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import de.marketmaker.istar.domain.KeysystemEnum;
import de.marketmaker.istar.domain.data.PriceQuality;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.domain.profile.Profile;
import de.marketmaker.istar.domainimpl.profile.PmAboProfile;
import de.marketmaker.istar.merger.context.RequestContext;
import de.marketmaker.istar.merger.context.RequestContextHolder;
import de.marketmaker.istar.merger.provider.UnknownSymbolException;

import static de.marketmaker.istar.merger.web.easytrade.block.EasytradeInstrumentProvider.iidSymbol;

/**
 * @author oflege
 */
public abstract class QuoteFilters {
    private static final Set<String> FILTER_MARKETS = Collections.synchronizedSet(new HashSet<>(
            Arrays.asList("JCF", "JCFON", "JCFKGV", "TRKGV", "OBUS", "OBUFFM", "OBUETR", "KENNZ", "WEIGHT")
    ));

    protected static boolean isFilteredByMarket(Quote quote) {
        return FILTER_MARKETS.contains(quote.getSymbolVwdfeedMarket());
    }

    protected static boolean is1_DTB(Quote quote) {
        final String s = quote.getSymbolVwdfeed();
        return (s != null) && s.startsWith("1.") && "DTB".equals(quote.getSymbolVwdfeedMarket());
    }

    // default market filter for ProfiledInstrument used as default strategy in RequestContext
    public static final QuoteFilter FILTER_SPECIAL_MARKETS
            = quote -> !(isFilteredByMarket(quote) || is1_DTB(quote));

    // special market filter for ProfiledInstrument as alternative strategy to be set in a zone
    // configuration; does NOT filter 1.%.DTB quotes
    // implementation as copy of FILTER_SPECIAL_MARKETS w/o 1.DTB check
    public static final QuoteFilter FILTER_SPECIAL_MARKETS_WITH_1DTB
            = quote -> !isFilteredByMarket(quote);

    public static final QuoteFilter WITH_PRICES = new QuoteFilter() {
        @Override
        public boolean test(Quote quote) {
            RequestContext rc = RequestContextHolder.getRequestContext();
            return doTest(quote, rc.getProfile(), rc.getBaseQuoteFilter());
        }

        protected boolean doTest(Quote quote, Profile p, QuoteFilter qf) {
            return qf.test(quote) && p.getPriceQuality(quote) != PriceQuality.NONE;
        }

        public List<Quote> apply(List<Quote> quotes) {
            if (quotes.isEmpty()) {
                return quotes;
            }

            RequestContext rc = RequestContextHolder.getRequestContext();
            final Profile p = rc.getProfile();
            final QuoteFilter qf = rc.getBaseQuoteFilter();

            final List<Quote> result = quotes.stream()
                    .filter(Objects::nonNull)
                    .filter(q -> doTest(q, p, qf))
                    .collect(Collectors.toList());
            if (result.isEmpty()) {
                throw new UnknownSymbolException("no quote with prices in "
                        + iidSymbol(quotes.get(0).getInstrument().getId()) + " for profile " + p.getName());
            }
            return result;
        }
    };

    public static final QuoteFilter WITH_VWDSYMBOL
            = quote -> StringUtils.hasText(quote.getSymbolVwdfeed());

    public static final QuoteFilter CLASS_ONE_QUOTES = quote -> {
        final String symbol = quote.getSymbolVwdsymbol();
        return StringUtils.hasText(symbol) && !(symbol.endsWith("-BLK") || symbol.endsWith("-OTC"));
    };

    public static final QuoteFilter PM_ABO = new QuoteFilter() {
        @Override
        public boolean test(Quote quote) {
            final Profile p = RequestContextHolder.getRequestContext().getProfile();
            return (p instanceof PmAboProfile) && isWithAbo(((PmAboProfile) p).getAbos(), quote);
        }

        public List<Quote> apply(List<Quote> quotes) {
            final Profile p = RequestContextHolder.getRequestContext().getProfile();

            if (!(p instanceof PmAboProfile)) {
                return Collections.emptyList();
            }
            final Set<String> abos = ((PmAboProfile) p).getAbos();
            return quotes.stream().filter(quote -> isWithAbo(abos, quote)).collect(Collectors.toList());
        }

        protected boolean isWithAbo(Set<String> abos, Quote quote) {
            return CollectionUtils.containsAny(abos, Arrays.asList(quote.getEntitlement().getEntitlements(KeysystemEnum.MM)));
        }
    };

    public static final CountryFilter MARKET_COUNTRY_DE = new CountryFilter("DE");

    private abstract static class SetBasedQuoteFilter implements QuoteFilter {
        private final Set<String> accepable;

        private boolean allAsDefault;

        public SetBasedQuoteFilter(Set<String> accepable, boolean allAsDefault) {
            this.accepable = accepable;
            this.allAsDefault = allAsDefault;
        }

        @Override
        public boolean test(Quote quote) {
            return this.accepable.contains(getProperty(quote));
        }

        public List<Quote> apply(List<Quote> quotes) {
            final List<Quote> result = QuoteFilter.super.apply(quotes);
            return result.isEmpty() && this.allAsDefault ? quotes : result;
        }

        protected abstract String getProperty(Quote quote);
    }

    public static class PreferredCurrencyFilter extends SetBasedQuoteFilter {
        public PreferredCurrencyFilter(Set<String> accepable) {
            super(accepable, true);
        }

        public PreferredCurrencyFilter(String... accepable) {
            super(new HashSet<>(Arrays.asList(accepable)), true);
        }

        @Override
        protected String getProperty(Quote quote) {
            return quote.getCurrency().getSymbolIso();
        }
    }

    public static class CurrencyFilter extends SetBasedQuoteFilter {
        public CurrencyFilter(Set<String> accepable) {
            super(accepable, false);
        }

        public CurrencyFilter(String... acceptable) {
            super(new HashSet<>(Arrays.asList(acceptable)), false);
        }

        @Override
        protected String getProperty(Quote quote) {
            return quote.getCurrency().getSymbolIso();
        }
    }

    public static class CountryFilter extends SetBasedQuoteFilter {
        public CountryFilter(Set<String> accepable) {
            super(accepable, false);
        }

        public CountryFilter(String... accepable) {
            super(new HashSet<>(Arrays.asList(accepable)), true);
        }

        @Override
        protected String getProperty(Quote quote) {
            return quote.getMarket().getCountry().getSymbolIso();
        }
    }

    public static class SymbolVwdfeedFilter extends SetBasedQuoteFilter {
        public SymbolVwdfeedFilter(Set<String> accepable) {
            super(accepable, false);
        }

        public SymbolVwdfeedFilter(String... accepable) {
            super(new HashSet<>(Arrays.asList(accepable)), false);
        }

        @Override
        protected String getProperty(Quote quote) {
            return quote.getMarket().getSymbolVwdfeed();
        }
    }

    public static QuoteFilter create(String s) {
        // if more filters are added -> see MscPriceDatas.Command.getFilter()
        if ("with_prices".equals(s)) {
            return WITH_PRICES;
        }
        if ("with_vwdsymbol".equals(s)) {
            return WITH_VWDSYMBOL;
        }
        if ("market_country_de".equals(s)) {
            return MARKET_COUNTRY_DE;
        }
        throw new QuoteFilterException(s);
    }

}
