/*
 * BewSymbol.java
 *
 * Created on 19.05.2010 15:22:19
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.bew;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.util.StringUtils;

import de.marketmaker.istar.domain.instrument.Instrument;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.merger.web.easytrade.ProfiledInstrument;
import de.marketmaker.istar.domain.util.IsinUtil;
import de.marketmaker.istar.instrument.InstrumentRequest;
import de.marketmaker.istar.merger.context.RequestContextHolder;
import de.marketmaker.istar.merger.provider.UnknownSymbolException;
import de.marketmaker.istar.merger.web.easytrade.block.MarketStrategy;
import de.marketmaker.istar.merger.web.easytrade.block.QuoteFilter;
import de.marketmaker.istar.merger.web.easytrade.block.QuoteFilters;
import de.marketmaker.istar.merger.web.easytrade.block.QuoteSelectors;

import static de.marketmaker.istar.instrument.InstrumentRequest.KeyType.*;

/**
 * Represents a row in the request file, may produce one or multiple
 * {@link de.marketmaker.istar.merger.web.bew.ResultItem} objects (multiple if no exchange is
 * specified and data for all available exchanges is requested).
 * @author oflege
 */
class RequestItem {
    private static final Pattern VALOR_PATTERN = Pattern.compile("0{0,3}([0-9]{5,})");

    private static final Pattern TICKER_PATTERN = Pattern.compile("[A-Z].{0,4}");

    private static final List<QuoteFilter> QUOTE_FILTERS
            = Arrays.asList(QuoteFilters.WITH_VWDSYMBOL, QuoteFilters.WITH_PRICES);

    private static final Set<String> OPTION_EXCHANGES
            = Collections.synchronizedSet(new HashSet<>(Arrays.asList(
            "EUX", // Eurex for vwd and BEW/old
            "DTB",
            "EOE", // European Option Exchange (old code)
            "LIF", // Liffe (BEW/old)
            "XCCOMD", // vwd codes for Euronext option exchanges
            "XCOMD",
            "XEQD",
            "XIRD"
    )));

    private static final String MARKET_PREFIX = "MARKETS:";

    private static final String CURRENCY_PREFIX = "CURRENCY:";

    static class Query {
        private String term;

        private InstrumentRequest.KeyType keyType;

        Query(String term, InstrumentRequest.KeyType keyType) {
            this.term = term;
            this.keyType = keyType;
        }

        public String getTerm() {
            return term;
        }

        public InstrumentRequest.KeyType getKeyType() {
            return keyType;
        }
    }

    private String exchange;

    private final String symbol;

    private final String symbolAsRequested;

    private String exchangeAsRequested;

    private String externalFields;

    private Query query;

    private Instrument instrument;

    private String clientsideMapping;

    private SymbolMapping serversideMapping;

    static RequestItem create(String def, String externalFields) {
        if (!StringUtils.hasText(def) || ";".equals(def)) {
            return null;
        }
        final String[] parts = def.split(";");
        return new RequestItem(parts[0], parts.length == 2 ? parts[1] : null, externalFields);
    }


    private RequestItem(String symbol, String exchange, String externalFields) {
        this.symbolAsRequested = symbol;
        this.exchangeAsRequested = exchange;
        this.symbol = symbol.toUpperCase();
        this.exchange = exchange != null ? exchange.toUpperCase() : null;
        this.externalFields = externalFields;
        this.serversideMapping = SymbolMapping.getMapping(symbol);
    }

    public String toString() {
        return "RequestItem[" + symbol + "@" + exchange + "]";
    }

    void assignQuery(String mappedSymbol, String clientsideMapping) {
        this.clientsideMapping = clientsideMapping;
        this.query = createQuery(mappedSymbol);
    }

    private RequestItem.Query createQuery(String mappedSymbol) {
        if (this.serversideMapping != null) {
            return new Query(this.serversideMapping.getVwdcode(), VWDCODE);
        }
        if (isOptionExchange() || !StringUtils.hasText(getExchange()) && isOption()) {
            final String optionSymbol = OptionFutureMapping.getTicker(getSymbol());
            return optionSymbol != null
                    ? new Query(optionSymbol, EUREXTICKER)
                    : createDummyQuery();
        }
        if (IsinUtil.isIsin(mappedSymbol)) {
            return new Query(mappedSymbol, ISIN);
        }
//        final Matcher cMatcher = CROSSRATE_PATTERN.matcher(mappedSymbol);
//        if (cMatcher.matches()) {
//            return new Query(getCrossrateCode(cMatcher.group(1), cMatcher.group(2)), VWDCODE);
//        }
        final Matcher vMatcher = VALOR_PATTERN.matcher(mappedSymbol);
        if (vMatcher.matches()) {
            return new Query(vMatcher.group(1), VALOR);
        }
        if (isVwdcode(mappedSymbol)) {
//            final int first = mappedSymbol.indexOf(".");
//            final int second = mappedSymbol.indexOf(".", first + 1);
//            this.exchange = mappedSymbol.substring(first + 1, second > 0 ? second : mappedSymbol.length());
            return new Query(mappedSymbol, VWDCODE);
        }

        if ("SWX".equals(this.exchange) || "VTX".equals(this.exchange)) {
            return new Query(mappedSymbol + "." + ("SWX".equals(this.exchange) ? "CH" : "VX"), VWDCODE);
        }

        final Matcher tMatcher = TICKER_PATTERN.matcher(mappedSymbol);
        if (tMatcher.matches()) {
            final List<String> vwdmarkets = MarketMapping.getVwdMarketCodes(getExchange());
            final StringBuilder sb = new StringBuilder();
            for (String vwdmarket : vwdmarkets) {
                sb.append(mappedSymbol.replaceAll(Pattern.quote("."), "_").replaceAll(Pattern.quote("'"), "-")).append(".").append(vwdmarket).append(" ");
            }
            return new Query(sb.toString(), VWDCODE);
        }

        // dummy item that will return null instrument
        return createDummyQuery();
    }

    private Query createDummyQuery() {
        return new Query("0", IID);
    }

    private boolean isVwdcode(String symbol) {
        return symbol.indexOf(".") > 0;
    }

//    private String getCrossrateCode(String from, String to) {
//        final StringBuilder sb = new StringBuilder();
//        if (!"USD".equals(from)) {
//            sb.append(from);
//        }
//        sb.append(to).append(".").append(MarketMapping.getVwdMarketCode(this.exchange));
//        return sb.toString();
//    }

    public String getSymbol() {
        return symbol;
    }

    public String getMappedSymbol() {
        return (this.clientsideMapping != null) ? this.clientsideMapping : this.symbol;
    }

    public String getExchange() {
        return exchange;
    }

    public String getSymbolAsRequested() {
        return symbolAsRequested;
    }

    public String getExchangeAsRequested() {
        return exchangeAsRequested;
    }

    public String getExternalFields() {
        return externalFields;
    }

    public String getClientsideMapping() {
        return clientsideMapping;
    }

    public void setQuery(Query query) {
        this.query = query;
    }

    public String getQueryTerm() {
        return this.query.getTerm();
    }

    public InstrumentRequest.KeyType getKeyType() {
        return this.query.getKeyType();
    }

    SymbolMapping getServersideMapping() {
        return this.serversideMapping;
    }

    boolean isOption() {
        return OptionFutureMapping.isOptionOrFuture(getMappedSymbol(), this.exchange);
    }

    boolean isOptionExchange() {
        return OPTION_EXCHANGES.contains(this.exchange);
    }

    public List<ResultItem> createResultItems(Instrument instrument) {
        if (instrument == null) {
            return Collections.singletonList(new ResultItem(this, null));
        }
        this.instrument = instrument;

        final List<Quote> quotes = getQuotes();
        if (quotes.isEmpty()) {
            return Collections.singletonList(new ResultItem(this, null));
        }

        final ArrayList<ResultItem> result = new ArrayList<>(quotes.size());
        for (Quote quote : quotes) {
            result.add(new ResultItem(this, quote));
        }
        return result;
    }

    private boolean isListBased() {
        return this.exchange != null && this.exchange.startsWith("[") && this.exchange.endsWith("]");
    }

    private boolean isMatchingVwdcode(Quote quote) {
        final String c = quote.getSymbolVwdcode();
        return StringUtils.hasText(c)
                && (c.equals(this.symbol)
                || c.equals(this.clientsideMapping)
                || (this.serversideMapping != null && c.equals(this.serversideMapping.getVwdcode())));
    }

    private List<Quote> getQuotes() {
        try {
            if (isListBased()) {
                return getQuoteList();
            }

            if (this.serversideMapping != null
                    && this.serversideMapping.getVwdcode() != null
                    && StringUtils.hasText(this.exchange)) {
                final Quote q = getQuoteForExchange();
                if (q != null) {
                    return Collections.singletonList(q);
                }
            }

            // expect symbol (mapped or original => this.clientsideMapping) to be a vwdcode
            if (isItemPerVwdcode()
                    || isVwdcodeSymbol(this.clientsideMapping)
                    || (this.serversideMapping != null && this.serversideMapping.getVwdcode() != null)) {
                for (final Quote quote : this.instrument.getQuotes()) {
                    if (isMatchingVwdcode(quote)) {
                        return Collections.singletonList(quote);
                    }
                }
                return Collections.emptyList();
            }

            if (StringUtils.hasText(this.exchange)) {
                final Quote q = getQuoteForExchange();
                return (q != null) ? Collections.singletonList(q) : Collections.<Quote>emptyList();
            }

            return getFilteredQuotes();
        } catch (UnknownSymbolException e) {
            // ignore
        }
        return Collections.emptyList();
    }

    public boolean isItemPerVwdcode() {
        return "!VWD".equals(this.exchange);
    }

    private List<Quote> getQuoteList() {
        final String[] elements = this.exchange.substring(1, this.exchange.length() - 1).split(Pattern.quote("&&"));

        if (elements.length == 1 && !StringUtils.hasText(elements[0])) {
            return ProfiledInstrument.quotesWithPrices(this.instrument, RequestContextHolder.getRequestContext().getProfile());
        }

        final Set<String> markets = new HashSet<>();
        final Set<String> currencies = new HashSet<>();
        for (final String element : elements) {
            if (element.trim().startsWith(MARKET_PREFIX)) {
                final String[] items = element.trim().substring(MARKET_PREFIX.length()).split(Pattern.quote(","));
                markets.addAll(Arrays.asList(items));
            }
            if (element.trim().startsWith(CURRENCY_PREFIX)) {
                final String[] items = element.trim().substring(CURRENCY_PREFIX.length()).split(Pattern.quote(","));
                currencies.addAll(Arrays.asList(items));
            }
        }


        final List<Quote> result = new ArrayList<>();
        for (final String market : markets) {
            final MarketStrategy.Builder builder = new MarketStrategy.Builder()
                    .withFilters(QUOTE_FILTERS);

            if (!currencies.isEmpty()) {
                builder.withFilter(new QuoteFilters.CurrencyFilter(currencies));
            }

            final Quote quote = builder.withSelector(QuoteSelectors.byMarket(market)).build()
                    .getQuote(this.instrument);
            if (quote != null) {
                result.add(quote);
            }
        }
        return result;
    }

    private boolean isVwdcodeSymbol(String s) {
        return s != null && s.indexOf(".") > 0;
    }

    private List<Quote> getFilteredQuotes() {
        List<Quote> result = this.instrument.getQuotes();
        for (final QuoteFilter filter : QUOTE_FILTERS) {
            result = filter.apply(result);
        }
        return result;
    }

    private Quote getQuoteForExchange() {
        final MarketStrategy.Builder builder = new MarketStrategy.Builder()
                .withFilters(QUOTE_FILTERS);

        if ("RTS".equals(this.exchange)) { // Moskau in Rubel
            builder.withFilter(new QuoteFilters.PreferredCurrencyFilter("RUB"));
        }
        if ("RTU".equals(this.exchange)) { // Moskau in USD
            builder.withFilter(new QuoteFilters.PreferredCurrencyFilter("USD"));
        }

        if (this.exchange.startsWith("FONDS.")) {
            final String[] tokens = this.exchange.split(Pattern.quote("."));
            builder.withFilter(new QuoteFilters.CurrencyFilter(tokens[1]));
            builder.withSelector(QuoteSelectors.byMarket(tokens[0]));
        }
        else {
            builder.withSelector(MarketMapping.getSelector(this.exchange));
        }

        return builder.build().getQuote(this.instrument);
    }


    public void appendTo(StringBuilder sb) {
        sb.append(getQueryTerm()).append("/").append(getKeyType());
        sb.append(" => ");
        sb.append(this.instrument);
        if (this.instrument != null) {
            appendQuotesTo(sb);
            if (StringUtils.hasText(this.exchange)) {
                sb.append(", ").append(MarketMapping.getSelector(this.exchange));
            }
        }
    }

    private void appendQuotesTo(StringBuilder sb) {
        sb.append(", quotes=");
        final List<Quote> quotes;
        try {
            quotes = getFilteredQuotes();
        } catch (UnknownSymbolException e) {
            sb.append("_none_with_prices_]");
            return;
        }
        String sep = "[";
        for (Quote q : quotes) {
            sb.append(sep).append(q.getId()).append(".qid/").append(q.getSymbolVwdfeed())
                    .append("/").append(q.getCurrency().getSymbolIso());
            sep = ", ";
        }
        sb.append("]");
    }
}
