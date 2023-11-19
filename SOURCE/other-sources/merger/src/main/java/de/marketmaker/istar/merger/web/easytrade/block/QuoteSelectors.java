/*
 * QuoteSelectors.java
 *
 * Created on 25.11.2009 15:00:45
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.block;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import de.marketmaker.istar.domain.KeysystemEnum;
import de.marketmaker.istar.domain.Market;
import de.marketmaker.istar.domain.instrument.Instrument;
import de.marketmaker.istar.domain.instrument.InstrumentTypeEnum;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.domain.instrument.QuoteOrder;
import de.marketmaker.istar.feed.vwd.VendorkeyVwd;
import de.marketmaker.istar.instrument.InstrumentUtil;

/**
 * @author oflege
 */
public abstract class QuoteSelectors {
    public static final QuoteSelector SELECT_FIRST = new QuoteSelector() {
        @Override
        public String toString() {
            return "SelectFirst";
        }

        public Quote select(Instrument instrument, List<Quote> quotes) {
            if (quotes.isEmpty()) {
                return null;
            }
            return quotes.get(0);
        }
    };

    public static final QuoteSelector SELECT_BEST_PM_QUOTE = new QuoteSelector() {
        @Override
        public String toString() {
            return "SelectBestPmQuote";
        }

        public Quote select(Instrument instrument, List<Quote> quotes) {
            if (quotes.isEmpty()) {
                return null;
            }

            final String defaultMmwkn = instrument.getSymbol(KeysystemEnum.DEFAULTMMSYMBOL);
            if (defaultMmwkn != null) {
                for (final Quote quote : quotes) {
                    if (defaultMmwkn.equals(quote.getSymbolMmwkn())) {
                        return quote;
                    }
                }
            }

            return null;
        }
    };

    /**
     * Selects a quote that is an MSCI index if available
     */
    public static final QuoteSelector MSCI_INDEX = new QuoteSelector() {
        @Override
        public String toString() {
            return "MsciIndex";
        }

        public Quote select(Instrument instrument, List<Quote> quotes) {
            if (instrument.getInstrumentType() != InstrumentTypeEnum.IND) {
                return null;
            }
            if (!isMSCI(instrument)) {
                return null;
            }
            for (Quote quote : quotes) {
                if (quote.getSymbolVwdfeed() == null) {
                    continue;
                }

                final VendorkeyVwd vkey = VendorkeyVwd.getInstance(quote.getSymbolVwdfeed());
                if (vkey.getType() == 6 && vkey.getSymbol().toString().endsWith("_PI_USD")) {
                    return quote;
                }
            }
            for (Quote quote : quotes) {
                if (quote.getSymbolVwdfeed() == null) {
                    continue;
                }

                final VendorkeyVwd vkey = VendorkeyVwd.getInstance(quote.getSymbolVwdfeed());
                if (vkey.getType() == 6 && vkey.getSymbol().toString().contains("_PI_")) {
                    return quote;
                }
            }

            for (Quote quote : quotes) {
                if (quote.getSymbolVwdfeed() != null && quote.getSymbolVwdfeed().startsWith("6.")) {
                    return quote;
                }
            }
            return null;
        }

        private boolean isMSCI(Instrument instrument) {
            for (Quote q : instrument.getQuotes()) {
                if (InstrumentUtil.isMSCIFeedsymbol(q.getSymbolVwdfeed())) {
                    return true;
                }
            }
            return false;
        }
    };

    /**
     * Selects the quote for the instrument's home exchange if available
     */
    public static final QuoteSelector HOME_EXCHANGE = (instrument, quotes) -> {
        final Market homeExchange = instrument.getHomeExchange();
        return Objects.isNull(homeExchange) ? null :
                quotes.stream().filter(Objects::nonNull)
                        .filter(q -> homeExchange.getName().equals(q.getMarket().getName()))
                        .findFirst().orElse(null);
    };

    /**
     * If the swiss home exchange cannot be found check the first available swiss exchange (ordered by market volume).
     */
    public static final QuoteSelector SWISS_EXHANGE_OR_INDEX = (instrument, quotes) -> {
        final String swissIso = "CH";

        final Quote quote = QuoteSelectors.HOME_EXCHANGE.select(instrument, quotes);
        if (quote != null &&
                (swissIso.equals(getSymbolIso(quote.getMarket()))
                        || instrument.getInstrumentType() == InstrumentTypeEnum.IND)) {
            return quote;
        }

        final Market homeExchange = instrument.getHomeExchange();
        if (Objects.nonNull(homeExchange) && swissIso.equals(getSymbolIso(homeExchange))) {
            return quotes.stream().filter(q -> swissIso.equals(getSymbolIso(q.getMarket()))).findFirst().orElse(null);
        }
        return null;
    };

    /**
     * Prefers symbols that have both a vwdfeed and an mmwkn symbol, always returns a quote
     */
    public static final QuoteSelector SYMBOL_RELEVANCE = new QuoteSelector() {
        @Override
        public String toString() {
            return "SymbolRelevance";
        }

        private final Comparator<Quote> cmp = new Comparator<Quote>() {
            public int compare(Quote o1, Quote o2) {
                return getPrio(o2) - getPrio(o1);
            }

            private int getPrio(Quote quote) {
                return getPrio(quote.getSymbolVwdfeed()) + getPrio(quote.getSymbolMmwkn());
            }

            private int getPrio(String s) {
                return StringUtils.hasText(s) ? 1 : 0;
            }
        };

        public Quote select(Instrument instrument, List<Quote> quotes) {
            if (quotes.isEmpty()) {
                return null;
            }
            final List<Quote> tmp = new ArrayList<>(quotes);
            tmp.sort(this.cmp);
            return tmp.get(0);
        }
    };

    /**
     * Prefers the symbol with the longest price history, if any
     */
    public static final QuoteSelector LONGEST_HISTORY = new QuoteSelector() {
        @Override
        public String toString() {
            return "LongestHistory";
        }

        private final Comparator<Quote> cmp = new Comparator<Quote>() {
            public int compare(Quote o1, Quote o2) {
                return Integer.compare(getPrio(o1), getPrio(o2));
            }

            private int getPrio(Quote quote) {
                final int date = quote.getFirstHistoricPriceYyyymmdd();
                return (date > 0) ? date : Integer.MAX_VALUE;
            }
        };

        public Quote select(Instrument instrument, List<Quote> quotes) {
            if (quotes.isEmpty()) {
                return null;
            }
            final List<Quote> tmp = new ArrayList<>(quotes);
            tmp.sort(this.cmp);
            Quote candidate = tmp.get(0);
            return (candidate.getFirstHistoricPriceYyyymmdd() > 0) ? candidate : null;
        }
    };


    public static class ByQid implements QuoteSelector {
        private final long qid;

        public ByQid(long qid) {
            this.qid = qid;
        }

        public Quote select(Instrument instrument, List<Quote> quotes) {
            for (Quote quote : quotes) {
                if (this.qid == quote.getId()) {
                    return quote;
                }
            }
            return null;
        }

        public String toString() {
            return "ByQid[" + this.qid + "]";
        }
    }

    public static class ByOrder implements QuoteSelector {
        private final QuoteOrder order;

        public ByOrder(QuoteOrder order) {
            this.order = order;
        }

        public Quote select(Instrument instrument, List<Quote> quotes) {
            Quote result = null;
            int n = 0; // smallest non-zero order yet found
            for (Quote quote : quotes) {
                final int quoteOrder = quote.getOrder(this.order);
                if (quoteOrder > 0) {
                    if (n == 0 || quoteOrder < n) {
                        n = quoteOrder;
                        result = quote;
                    }
                }
            }
            return (n > 0) ? result : null;
        }

        public String toString() {
            return "ByOrder[" + this.order + "]";
        }
    }

    public static class ByMarketid implements QuoteSelector {
        private final long marketid;

        public ByMarketid(long marketid) {
            this.marketid = marketid;
        }

        public Quote select(Instrument instrument, List<Quote> quotes) {
            for (Quote quote : quotes) {
                if (quote.getMarket().getId() == this.marketid) {
                    return quote;
                }
            }
            return null;
        }

        public String toString() {
            return "ByMarketid[" + this.marketid + "]";
        }
    }


    public static class ByMarketsymbol implements QuoteSelector {
        protected final List<String> marketsymbols;

        public ByMarketsymbol(String marketsymbol) {
            this.marketsymbols = Arrays.asList(StringUtils.commaDelimitedListToStringArray(marketsymbol));
        }

        public Quote select(Instrument instrument, List<Quote> quotes) {
            for (String marketsymbol : this.marketsymbols) {
                for (Quote quote : quotes) {
                    if (isAcceptable(quote, marketsymbol)) {
                        return quote;
                    }
                }
            }
            return null;
        }

        protected boolean isAcceptable(Quote quote, String marketsymbol) {
            return marketsymbol.equals(quote.getSymbolVwdfeedMarket())
                    || marketsymbol.equals(quote.getMarket().getSymbolMm())
                    || marketsymbol.equals(quote.getSymbolMicOperating())
                    || marketsymbol.equals(quote.getSymbolMicSegment());
        }

        public String toString() {
            return getClass().getName() + "[" + this.marketsymbols + "]";
        }
    }

    public static class ByBisKeyMarketsymbol extends ByMarketsymbol {
        public ByBisKeyMarketsymbol(String marketsymbol) {
            super(marketsymbol);
        }

        @Override
        protected boolean isAcceptable(Quote quote, String marketsymbol) {
            return marketsymbol.equals(quote.getSymbolBisKeyMarket());
        }
    }

    public static class ByCurrencySymbol implements QuoteSelector {
        protected final List<String> isoSymbols;

        public ByCurrencySymbol(String symbol) {
            this.isoSymbols = Arrays.asList(StringUtils.commaDelimitedListToStringArray(symbol));
        }

        public Quote select(Instrument instrument, List<Quote> quotes) {
            for (String isoSymbol : this.isoSymbols) {
                for (Quote quote : quotes) {
                    if (isAcceptable(quote, isoSymbol)) {
                        return quote;
                    }
                }
            }
            return null;
        }

        protected static boolean isAcceptable(Quote quote, String isoSymbol) {
            return isoSymbol.equals(quote.getCurrency().getSymbolIso());
        }

        public String toString() {
            return "ByCurrencySymbol[" + this.isoSymbols + "]";
        }
    }

    public static class ByMarketAndCurrencySymbol extends ByMarketsymbol {
        protected final List<String> isoSymbols;

        public ByMarketAndCurrencySymbol(String marketsymbol, String currencysymbol) {
            super(marketsymbol);
            this.isoSymbols = Arrays.asList(StringUtils.commaDelimitedListToStringArray(currencysymbol));
        }

        @Override
        protected boolean isAcceptable(Quote quote, String marketsymbol) {
            if (!super.isAcceptable(quote, marketsymbol)) {
                return false;
            }
            for (String isoSymbol : this.isoSymbols) {
                if (ByCurrencySymbol.isAcceptable(quote, isoSymbol)) {
                    return true;
                }
            }
            return false;
        }

        public String toString() {
            return "ByMarketAndCurrencySymbol[" + this.marketsymbols + "/" + this.isoSymbols + "]";
        }
    }

    public static class ByCountry implements QuoteSelector {
        private final String country;

        public ByCountry(String country) {
            this.country = country;
        }

        @Override
        public Quote select(Instrument instrument, List<Quote> quotes) {
            for (Quote quote : quotes) {
                if (this.country.equals(getSymbolIso(quote.getMarket()))) {
                    return quote;
                }
            }
            return null;
        }

        public String toString() {
            return "ByCountry[" + this.country + "]";
        }
    }

    public static class ByType implements QuoteSelector {
        private final InstrumentTypeEnum type;

        private final QuoteSelector delegate;

        public ByType(InstrumentTypeEnum type, QuoteSelector delegate) {
            this.type = type;
            this.delegate = delegate;
        }

        public Quote select(Instrument instrument, List<Quote> quotes) {
            if (instrument.getInstrumentType() != this.type) {
                return null;
            }
            return this.delegate.select(instrument, quotes);
        }

        public String toString() {
            return "ByType[" + this.type + " " + this.delegate + "]";
        }
    }


    public static class BySymbolVwdcode implements QuoteSelector {
        private final String vwdcode;

        public BySymbolVwdcode(String vwdcode) {
            this.vwdcode = vwdcode;
        }

        public Quote select(Instrument instrument, List<Quote> quotes) {
            for (final Quote quote : quotes) {
                if (vwdcode.equals(quote.getSymbolVwdcode())) {
                    return quote;
                }
            }
            return null;
        }

        @Override
        public String toString() {
            return ClassUtils.getShortName(getClass()) + "[" + this.vwdcode + "]";
        }
    }

    public static class BySymbolVwdfeedSuffix implements QuoteSelector {
        private final String suffix;

        public BySymbolVwdfeedSuffix(String suffix) {
            this.suffix = suffix;
        }

        public Quote select(Instrument instrument, List<Quote> quotes) {
            for (final Quote quote : quotes) {
                if (endsWithSuffix(quote.getSymbolVwdfeed())) {
                    return quote;
                }
            }
            return null;
        }

        private boolean endsWithSuffix(final String symbolVwdfeed) {
            return symbolVwdfeed != null && symbolVwdfeed.endsWith(this.suffix);
        }

        @Override
        public String toString() {
            return ClassUtils.getShortName(getClass()) + "[" + this.suffix + "]";
        }
    }

    public static class BySymbolVwdfeedRegex implements QuoteSelector {
        private final Pattern pattern;

        public BySymbolVwdfeedRegex(Pattern pattern) {
            this.pattern = pattern;
        }

        public Quote select(Instrument instrument, List<Quote> quotes) {
            for (final Quote quote : quotes) {
                if (matches(quote.getSymbolVwdfeed())) {
                    return quote;
                }
            }
            return null;
        }

        private boolean matches(final String symbolVwdfeed) {
            return symbolVwdfeed != null && this.pattern.matcher(symbolVwdfeed).matches();
        }

        @Override
        public String toString() {
            return ClassUtils.getShortName(getClass()) + "[" + this.pattern + "]";
        }
    }

    public static class BySymbolSuffix implements QuoteSelector {
        private final String suffix;

        public BySymbolSuffix(String suffix) {
            this.suffix = suffix;
        }

        public Quote select(Instrument instrument, List<Quote> quotes) {
            for (final Quote quote : quotes) {
                final String vwdfeed = quote.getSymbolVwdfeed();
                if (vwdfeed == null) {
                    continue;
                }
                final VendorkeyVwd vkey = VendorkeyVwd.getInstance(vwdfeed);
                if (endsWithSuffix(vkey.getSymbol().toString())) {
                    return quote;
                }
            }
            return null;
        }

        private boolean endsWithSuffix(final String s) {
            return s != null && s.endsWith(this.suffix);
        }

        @Override
        public String toString() {
            return ClassUtils.getShortName(getClass()) + "[" + this.suffix + "]";
        }
    }


    private static class ListSelector implements QuoteSelector {
        private final List<QuoteSelector> selectors = new ArrayList<>();

        @Override
        public String toString() {
            return ClassUtils.getShortName(getClass()) + "[" + this.selectors + "]";
        }

        private ListSelector(QuoteSelector... selectors) {
            this.selectors.addAll(Arrays.asList(selectors));
        }

        public Quote select(Instrument instrument, List<Quote> quotes) {
            for (QuoteSelector selector : selectors) {
                final Quote quote = selector.select(instrument, quotes);
                if (quote != null) {
                    return quote;
                }
            }
            return null;
        }
    }

    public static QuoteSelector byMarket(String market) {
        if (market.startsWith("id:")) {
            return new ByMarketid(Integer.parseInt(market.substring(3)));
        }
        return new ByMarketsymbol(market);
    }

    public static QuoteSelector byBisKeyMarket(String market) {
        return new ByBisKeyMarketsymbol(market);
    }

    public static QuoteSelector group(QuoteSelector... selectors) {
        return new ListSelector(selectors);
    }

    private static String getSymbolIso(Market market) {
        return market.getCountry().getSymbolIso();
    }
}
