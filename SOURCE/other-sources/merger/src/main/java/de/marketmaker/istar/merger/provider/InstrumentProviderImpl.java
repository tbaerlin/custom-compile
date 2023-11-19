/*
 * InstrumentProviderImpl.java
 *
 * Created on 05.07.2006 13:51:22
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider;

import static de.marketmaker.istar.domain.profile.Profile.Aspect.PRICE;
import static de.marketmaker.istar.feed.vwd.EntitlementProviderVwd.KAG_MARKET_ENTITLEMENT;
import static de.marketmaker.istar.instrument.export.ScoachInstrumentAdaptor.BIS_KEY_PREFIX_FFM;
import static de.marketmaker.istar.instrument.export.ScoachInstrumentAdaptor.BIS_KEY_PREFIX_FFMST;
import static de.marketmaker.istar.instrument.export.ScoachInstrumentAdaptor.replaceBisKeyMarketPrefix;

import de.marketmaker.istar.common.featureflags.FeatureFlags;
import de.marketmaker.istar.domain.KeysystemEnum;
import de.marketmaker.istar.domain.data.SuggestedInstrument;
import de.marketmaker.istar.domain.instrument.Instrument;
import de.marketmaker.istar.domain.instrument.InstrumentTypeEnum;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.domain.profile.Selector;
import de.marketmaker.istar.domain.util.IsinUtil;
import de.marketmaker.istar.instrument.CacheableInstrumentResponse;
import de.marketmaker.istar.instrument.IndexConstants;
import de.marketmaker.istar.instrument.InstrumentRequest;
import de.marketmaker.istar.instrument.InstrumentResponse;
import de.marketmaker.istar.instrument.InstrumentServer;
import de.marketmaker.istar.instrument.InstrumentUtil;
import de.marketmaker.istar.instrument.search.InstrumentSearcherImpl;
import de.marketmaker.istar.instrument.search.SearchMetaRequest;
import de.marketmaker.istar.instrument.search.SearchMetaResponse;
import de.marketmaker.istar.instrument.search.SearchRequestResultType;
import de.marketmaker.istar.instrument.search.SearchRequestStringBased;
import de.marketmaker.istar.instrument.search.SearchResponse;
import de.marketmaker.istar.instrument.search.SuggestRequest;
import de.marketmaker.istar.instrument.search.SuggestResponse;
import de.marketmaker.istar.instrument.search.ValidationRequest;
import de.marketmaker.istar.instrument.search.ValidationResponse;
import de.marketmaker.istar.merger.context.RequestContext;
import de.marketmaker.istar.merger.context.RequestContextHolder;
import de.marketmaker.istar.merger.web.easytrade.SymbolStrategyEnum;
import de.marketmaker.istar.merger.web.easytrade.block.EasytradeInstrumentProvider;
import de.marketmaker.istar.merger.web.easytrade.block.SimpleSearchCommand;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.LongStream;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class InstrumentProviderImpl implements InstrumentProvider {
    private static final List<String> NAME_FIELDS = Arrays.asList(
            IndexConstants.FIELDNAME_NAME,
            IndexConstants.FIELDNAME_ALIAS,
            KeysystemEnum.WM_WP_NAME.name().toLowerCase(),
            KeysystemEnum.WM_WP_NAME_KURZ.name().toLowerCase(),
            KeysystemEnum.WM_WP_NAME_ZUSATZ.name().toLowerCase()
    );

    private static final List<String> TICKER_FIELDS = Arrays.asList(
            KeysystemEnum.TICKER.name().toLowerCase(),
            KeysystemEnum.EUREXTICKER.name().toLowerCase()
    );

    private static final List<String> DEFAULT_FIELDS = new ArrayList<>();

    private static final String FFM_SUFFIX = ".FFM";

    private static final String FFMST_ST_SUFFIX = "ST";

    // see https://lucene.apache.org/core/2_9_4/queryparsersyntax.html
    private static final Pattern LUCENE_ESCAPE_CHARS
            = Pattern.compile("[-+&|!\\(\\)\\{}\\[\\]^\"~*?:\\\\]");

    static {
        DEFAULT_FIELDS.addAll(NAME_FIELDS);

        DEFAULT_FIELDS.addAll(Arrays.asList(
            IndexConstants.ISIN,
            IndexConstants.WKN,
            IndexConstants.VWDCODE,
            IndexConstants.INFRONT_ID
        ));

        DEFAULT_FIELDS.addAll(TICKER_FIELDS);
    }

    private Ehcache instrumentCache;

    /**
     * timestamp of the most recent instrument update; the instrumentCache will never contain any
     * instrument from an index with a lower timestamp value
     */
    private AtomicLong cacheTimestamp = new AtomicLong(0);

    private InstrumentServer instrumentServer;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private Ehcache symbolCache;

    private static final int MAX_NUM_SEARCH_RESULTS = 1000;

    public SearchMetaResponse getMetadata() {
        try {
            return this.instrumentServer.getMetaData(new SearchMetaRequest());
        } catch (Exception e) {
            this.logger.error("<getMetadata> failed", e);
        }
        return null;
    }

    public List<SuggestedInstrument> getSuggestions(String query, int limit, String strategy) {
        final RequestContext requestContext = RequestContextHolder.getRequestContext();

        final SuggestRequest request
                = new SuggestRequest(requestContext.getProfile(), requestContext.getInstrumentNameStrategy());
        request.setQuery(query);
        request.setLimit(limit);
        request.setStrategy(strategy);

        try {
            final SuggestResponse response = this.instrumentServer.getSuggestions(request);
            if (response.isValid()) {
                return response.getSuggestions();
            }
        } catch (Exception e) {
            this.logger.error("<getSuggestions> failed for " + request, e);
        }
        return Collections.emptyList();
    }

    public Instrument identifyByIid(Long iid) {
        return identifyInstrumentBySymbol(EasytradeInstrumentProvider.iidSymbol(iid), SymbolStrategyEnum.IID);
    }

    public Instrument identifyByIsin(String isin) {
        return identifyByIsinOrWkn(isin);
    }

    public Instrument identifyByIsinOrWkn(String symbol) {
        final boolean isISIN = symbol.length() == IsinUtil.ISIN_LENGTH;
        final SymbolStrategyEnum strategy = isISIN ? SymbolStrategyEnum.ISIN : SymbolStrategyEnum.WKN;
        return identifyInstrumentBySymbol(symbol, strategy);
    }

    public Instrument identifyByQid(Long qid) {
        final String symbol = EasytradeInstrumentProvider.qidSymbol(qid);
        return identifyInstrumentBySymbol(symbol, SymbolStrategyEnum.QID);
    }

    public Quote identifyByVwdcode(String symbol) {
        final Instrument instrument = identifyInstrumentBySymbol(symbol, SymbolStrategyEnum.VWDCODE);
        for (final Quote quote : instrument.getQuotes()) {
            if (isMatchingVwdCode(symbol, quote)) {
                return quote;
            }
        }
        throw new UnknownSymbolException("unknown vwdcode '" + symbol + "'");
    }

    public Quote identifyByInfrontId(String symbol) {
        final Instrument instrument = identifyInstrumentBySymbol(symbol, SymbolStrategyEnum.INFRONT_ID);
        for (final Quote quote : instrument.getQuotes()) {
            if (symbol.equals(quote.getSymbolInfrontId())) {
                return quote;
            }
        }
        throw new UnknownSymbolException("unknown infrontid '" + symbol + "'");
    }

     public static boolean isMatchingVwdCode(String vwdcode, Quote quote) {
        final String quoteSymbol = quote.getSymbolVwdcode();
        if (vwdcode.endsWith(FFM_SUFFIX) && RequestContextHolder.getRequestContext().isEnabled(FeatureFlags.Flag.REQUEST_FFMST_WITH_FFM)) {
            return vwdcode.equalsIgnoreCase(quoteSymbol) || (vwdcode + FFMST_ST_SUFFIX).equalsIgnoreCase(quoteSymbol);
        }
        return vwdcode.equalsIgnoreCase(quoteSymbol);
    }

    @Override
    public Quote identifyByBisKey(String symbol) {
        final Instrument instrument = identifyInstrumentBySymbol(symbol, SymbolStrategyEnum.BIS_KEY);
        final Quote quote = InstrumentUtil.getQuoteByBisKey(instrument, symbol);
        if (quote == null) {
            throw new UnknownSymbolException("unknown bis key '" + symbol + "'");
        }
        return quote;
    }

    public Quote identifyByMmwkn(String symbol) {
        final Instrument instrument = identifyInstrumentBySymbol(symbol, SymbolStrategyEnum.MMWKN);
        for (final Quote quote : instrument.getQuotes()) {
            if (symbol.equalsIgnoreCase(quote.getSymbolMmwkn())) {
                return quote;
            }
        }
        throw new UnknownSymbolException("unknown mmwkn '" + symbol + "'");
    }

    public Quote identifyByVwdfeed(String vendorkey) {
        return identifyByVwdcode(vendorkey.substring(vendorkey.indexOf('.') + 1));
    }

    public Instrument identifyByWkn(String wkn) {
        return identifyByIsinOrWkn(wkn);
    }

    public Map<String, Instrument> identifyInstruments(List<String> symbols,
            SymbolStrategyEnum strategy) {
        if (symbols.isEmpty()) {
            return Collections.emptyMap();
        }
        final List<String> uniqueSymbols = uniq(symbols);

        switch (strategy) {
            case IID:
                // speedup by using instrument cache
                return identifyInstrumentsByIid(uniqueSymbols);
            case QID:
            case ISIN:
            case WKN:
            case MMWKN:
            case VWDCODE:
            case BIS_KEY:
            case INFRONT_ID:
                // speedup by using symbol cache
                return identifyInstrumentsBySymbol(uniqueSymbols, strategy);
            default:
                // should never happen
                throw new IllegalArgumentException("Invalid strategy " + strategy);
        }
    }

    public List<Instrument> identifyInstruments(List<Long> iids) {
        final Set<String> requestIids = new HashSet<>(iids.size());
        final List<Instrument> result = new ArrayList<>(iids.size());
        for (Long iid : iids) {
            final Instrument instrument = getInstrumentFromCache(iid);
            result.add(instrument);
            if (instrument == null && iid != 0L) {
                requestIids.add(Long.toString(iid));
            }
        }

        if (requestIids.isEmpty()) {
            return result;
        }

        final InstrumentRequest ir = new InstrumentRequest();
        ir.addItems(requestIids, InstrumentRequest.KeyType.IID);
        final InstrumentResponse response;
        try {
            response = this.instrumentServer.identifyNew(ir);
            if (response.isValid()) {
                addToInstrumentCache(response);
                final Map<String, Instrument> instruments
                        = mapBySymbol(response.getInstruments(), SymbolStrategyEnum.IID);
                for (int i = 0; i < result.size(); i++) {
                    if (result.get(i) == null) {
                        result.set(i, instruments.get(EasytradeInstrumentProvider.iidSymbol(iids.get(i))));
                    }
                }
                return result;
            }
        } catch (Exception e) {
            this.logger.error("<identify> failed for " + requestIids, e);
        }

        // TODO: return a list of nulls or NullInstruments?
        return null;
    }

    @Override
    public Collection<Long> validate(Collection<Long> iids) {
        final ValidationResponse response
                = this.instrumentServer.validate(new ValidationRequest(iids));
        if (response.isValid()) {
            return LongStream.of(response.getInvalidIids())
                    .mapToObj(Long::new)
                    .collect(Collectors.toSet());
        }
        return Collections.emptySet();
    }


    public void setInstrumentCache(Ehcache instrumentCache) {
        this.instrumentCache = instrumentCache;
        this.logger.info("<setInstrumentCache> for #" + getMaxElementsInMemory(this.instrumentCache));
    }

    private int getMaxElementsInMemory(final Ehcache cache) {
        return (int) cache.getCacheConfiguration().getMaxEntriesLocalHeap();
    }

    public void setInstrumentServer(InstrumentServer instrumentServer) {
        this.instrumentServer = instrumentServer;
    }

    public void setSymbolCache(Ehcache symbolCache) {
        this.symbolCache = symbolCache;
        this.logger.info("<setSymbolCache> for #" + getMaxElementsInMemory(this.symbolCache));
    }

    public SearchResponse simpleSearch(SimpleSearchCommand cmd) {
        final SearchRequestStringBased sr = new SearchRequestStringBased();
        sr.setSearchExpression(cmd.getSearchstring());
        sr.setSearchSteps(getSearchSteps(cmd.getStrategy()));
        sr.setSearchConstraints(getSearchConstraints(cmd.getMarkets(), cmd.getCurrencies()));
        sr.setDefaultFields(getSearchFields(cmd.getSearchfields(), cmd.getAdditionalSearchfields()));
        sr.setFilterBlacklistMarkets(true);
        sr.setMaxNumResults(cmd.getMaxSize());
        sr.setCountInstrumentResults(cmd.isCountInstrumentResults());
        sr.setCountTypes(cmd.getCountTypes());
        sr.setFilterTypes(cmd.getTypes());
        sr.setFilterOpraMarkets(cmd.isFilterOpra());

        // setting key noProfileForSearch in zone context triggers profile independent search using all existing quotes
        if (RequestContextHolder.getRequestContext().containsKey("noProfileForSearch")) {
            sr.setResultType(SearchRequestResultType.QUOTE_WITH_VWDSYMBOL_AND_INSTRUMENT_WITH_ISIN);
        }
        else {
            sr.setProfile(RequestContextHolder.getRequestContext().getProfile());
        }

        sr.setUsePaging(true);
        sr.setPagingOffset(cmd.getOffset());
        sr.setPagingCount(cmd.getCount());
        sr.setSortFields(cmd.getSortFields());

        try {
            return this.instrumentServer.simpleSearchNew(sr);
        } catch (Exception e) {
            this.logger.error("<simpleSearch> failed for " + sr, e);
            throw new InternalFailure("instrument search failed");
        }
    }

    private void addIidForSymbol(String symbol, SymbolStrategyEnum strategy, Long iid) {
        if (this.symbolCache == null) {
            return;
        }
        this.symbolCache.put(new Element(getSymbolCacheKey(symbol, strategy), iid));
    }

    private void addToInstrumentCache(CacheableInstrumentResponse response) {
        if (this.instrumentCache == null) {
            return;
        }

        final long ts = response.getInstrumentUpdateTimestamp();

        long current;
        do {
            current = cacheTimestamp.get();
            if (ts < current) {
                return;
            }
            if (ts > current && cacheTimestamp.compareAndSet(current, ts)) {
                this.instrumentCache.removeAll();
                break;
            }
        } while (ts != current);

        addToInstrumentCache(response.getInstruments());
        addToInstrumentCache(response.getUnderlyings().values());

        if (ts < cacheTimestamp.get()) {
            // we filled the cache while a new index timestamp appeared, remove "our" data
            removeFromInstrumentCache(response.getInstruments());
            removeFromInstrumentCache(response.getUnderlyings().values());
        }
    }

    private void addToInstrumentCache(Collection<Instrument> instruments) {
        instruments.stream()
                .filter(Objects::nonNull)
                .forEach(instrument -> this.instrumentCache.put(new Element(instrument.getId(), instrument)));
    }

    private void removeFromInstrumentCache(Collection<Instrument> instruments) {
        instruments.stream()
                .filter(Objects::nonNull)
                .forEach(instrument -> this.instrumentCache.remove(instrument.getId()));
    }

    private void appendConstraints(StringBuilder sb, String field, Collection<String> items) {
        if (items == null || items.isEmpty()) {
            return;
        }
        sb.append(" +").append(field).append(":(");
        for (String item : items) {
            sb.append(" ").append(item);
        }
        sb.append(")");
    }

    private Map<String, Instrument> searchInstrumentsBySymbol(List<String> symbols,
            SymbolStrategyEnum symbolStrategy) {
        if (symbols.size() <= MAX_NUM_SEARCH_RESULTS) {
            return doSearchInstrumentsBySymbol(symbols, symbolStrategy);
        }

        final Map<String, Instrument> result = new HashMap<>(symbols.size() * 2);
        for (int i = 0; i < symbols.size(); i += MAX_NUM_SEARCH_RESULTS) {
            final int toIndex = Math.min(symbols.size(), i + MAX_NUM_SEARCH_RESULTS);
            final Map<String, Instrument> subResult
                    = doSearchInstrumentsBySymbol(symbols.subList(i, toIndex), symbolStrategy);
            result.putAll(subResult);
        }
        return result;
    }

    private String buildQuery(String field, String value, InstrumentTypeEnum... type) {
        final StringBuilder sb = new StringBuilder();
        sb.append("+").append(field).append(":").append(value);
        if (type != null && type.length > 0) {
            sb.append(" +type:(");
            for (final InstrumentTypeEnum t : type) {
                sb.append(" ").append(t.name().toLowerCase());
            }
            sb.append(")");
        }
        return sb.toString();
    }

    public List<Instrument> getDerivates(long iid, int maxNumResults, InstrumentTypeEnum... type) {
        return getByQuery(buildQuery("underlyingid", Long.toString(iid), type), maxNumResults);
    }

    public List<Instrument> getByVwdcodePrefix(String vwdcodePrefix, int maxNumResults,
            InstrumentTypeEnum... type) {
        return getByQuery(buildQuery("vwdcode", vwdcodePrefix + "*", type), maxNumResults);
    }

    private List<Instrument> getByQuery(final String query, int maxNumResults) {
        final SearchRequestStringBased sr = new SearchRequestStringBased();
        sr.setSearchExpression(query);
        sr.setMaxNumResults(maxNumResults);
        try {
            final SearchResponse response = this.instrumentServer.searchNew(sr);
            if (!response.isValid()) {
                return Collections.emptyList();
            }
            return response.getInstruments();
        } catch (Exception e) {
            this.logger.warn("<getByQuery> failed for " + query, e);
            throw new UnknownSymbolException("query " + query + " failed");
        }
    }

    private Map<String, Instrument> doSearchInstrumentsBySymbol(List<String> symbols,
            SymbolStrategyEnum symbolStrategy) {
        final String expression = getSearchExpression(symbols, symbolStrategy);

        final SearchRequestStringBased sr = new SearchRequestStringBased();
        final SearchRequestResultType resultType = getResultType();
        if (resultType != null) {
            sr.setResultType(resultType);
        }

        sr.setSearchExpression(expression);
        sr.setMaxNumResults(symbols.size());
        final SearchResponse response;
        try {
            response = this.instrumentServer.searchNew(sr);
            if (!response.isValid() || response.getInstruments().isEmpty()) {
                return Collections.emptyMap();
            }
            addToInstrumentCache(response);
        } catch (Exception e) {
            this.logger.error("<identifyInstruments> failed for symbols " + symbols + " and strategy " + symbolStrategy.name(), e);
            throw new UnknownSymbolException("unknown symbols:" + symbols + " and strategy " + symbolStrategy);
        }

        return mapResultToRequestSymbols(symbols, symbolStrategy, response.getInstruments());
    }

    private SearchRequestResultType getResultType() {
        final RequestContext requestContext = RequestContextHolder.getRequestContext();
        if (requestContext != null) {
            final SearchRequestResultType srrt = requestContext.getSearchRequestResultType();
            if (srrt != null) {
                return srrt;
            }
            if (requestContext.getProfile().isAllowed(PRICE, KAG_MARKET_ENTITLEMENT)) {
                return SearchRequestResultType.QUOTE_WITH_MMSYMBOL_OR_VWDSYMBOL;
            }
        }
        return null;
    }

    private Map<String, Instrument> mapResultToRequestSymbols(List<String> symbols,
            SymbolStrategyEnum symbolStrategy, List<Instrument> instruments) {
        final Map<String, Instrument> map = mapBySymbol(instruments, symbolStrategy);

        final Map<String, Instrument> result = new HashMap<>();
        for (final String symbol : symbols) {
            final Instrument selected = getInstrument(map, symbol, symbolStrategy);
            if (selected != null) {
                result.put(symbol, selected);
            }
        }

        return result;
    }

    private Instrument getInstrument(Map<String, Instrument> map, String symbol,
            SymbolStrategyEnum symbolStrategy) {
        final Instrument result = map.get(keyForMapping(symbol, symbolStrategy));

        // ISTAR-395
        if (result == null && symbolStrategy == SymbolStrategyEnum.BIS_KEY
                && symbol.startsWith(BIS_KEY_PREFIX_FFM)) {
            final String ffmstSymbol = replaceBisKeyMarketPrefix(symbol, BIS_KEY_PREFIX_FFMST);
            return getInstrument(map, ffmstSymbol, symbolStrategy);
        }

        // ISTAR-685
        if (result == null && symbolStrategy == SymbolStrategyEnum.VWDCODE && symbol.endsWith(FFM_SUFFIX) &&
                RequestContextHolder.getRequestContext().isEnabled(FeatureFlags.Flag.REQUEST_FFMST_WITH_FFM)) {
            return getInstrument(map, symbol + FFMST_ST_SUFFIX, symbolStrategy);
        }

        return result;
    }

    private String keyForMapping(String symbol, SymbolStrategyEnum strategy) {
        switch (strategy) {
            case IID:
                return ensureSuffix(symbol, EasytradeInstrumentProvider.IID_SUFFIX);
            case QID:
                return ensureSuffix(symbol, EasytradeInstrumentProvider.QID_SUFFIX);
            default:
                return symbol.toUpperCase();
        }
    }

    private String ensureSuffix(String symbol, String suffix) {
        return symbol.endsWith(suffix) ? symbol : (symbol + suffix);
    }

    private List<String> getSearchFields(Collection<String> searchfields,
            Collection<String> additionalSearchfields) {
        final List<String> result = getSearchFields(searchfields);

        if (additionalSearchfields == null || additionalSearchfields.isEmpty()) {
            return result;
        }

        result.addAll(
                additionalSearchfields.stream()
                        .map(String::toLowerCase)
                        .collect(Collectors.toList())
        );

        return result;
    }

    private List<String> getSearchFields(Collection<String> searchfields) {
        if (searchfields == null || searchfields.isEmpty()) {
            return getDefaultSearchFields();
        }

        final List<String> result = new ArrayList<>();
        for (final String searchfield : searchfields) {
            if ("names".equals(searchfield)) {
                result.addAll(NAME_FIELDS);
            }
            else if ("tickers".equals(searchfield)) {
                result.addAll(TICKER_FIELDS);
            }
            else {
                result.add(searchfield.toLowerCase());
            }
        }
        return result;
    }

    private List<String> getDefaultSearchFields() {
        ArrayList<String> result = new ArrayList<>(DEFAULT_FIELDS);
        if (FeatureFlags.Flag.NEW_WP_NAMES.isEnabled()) {
            if (RequestContextHolder.getRequestContext().getProfile().isAllowed(Selector.ANY_VWD_TERMINAL_PROFILE)) {
                result.add(IndexConstants.FIELDNAME_NAME_COST);
            }
            else {
                result.add(IndexConstants.FIELDNAME_NAME_FREE);
            }
        }
        return result;
    }

    private Long getIidForSymbol(String symbol, SymbolStrategyEnum strategy) {
        if (this.symbolCache == null) {
            return null;
        }
        final Element element = this.symbolCache.get(getSymbolCacheKey(symbol, strategy));
        return (Long) ((element != null) ? element.getValue() : null);
    }

    private Instrument getInstrumentFromCache(Long iid) {
        if (this.instrumentCache == null) {
            return null;
        }
        final Element element = this.instrumentCache.get(iid);
        return (Instrument) ((element != null) ? element.getValue() : null);
    }

    private String getSearchConstraints(Collection<String> markets, Collection<String> currencies) {
        final StringBuilder stb = new StringBuilder();
        appendConstraints(stb, "market", markets);
        appendConstraints(stb, "currency", currencies);
        return stb.toString();
    }

    private String getSearchExpression(List<String> symbols, SymbolStrategyEnum symbolStrategy) {
        final String fieldname = getSearchFieldForSymbolStrategy(symbolStrategy);

        final StringBuilder sb = new StringBuilder(symbols.size() * 10);
        sb.append(fieldname).append(":(");

        for (final String symbol : symbols) {
            sb.append(getQuerySymbol(symbol, symbolStrategy)).append(" ");
        }

        sb.append(")");
        return sb.toString();
    }

    static String getQuerySymbol(String symbol, SymbolStrategyEnum symbolStrategy) {
        if (symbolStrategy == SymbolStrategyEnum.IID || symbolStrategy == SymbolStrategyEnum.QID) {
            final String id = EasytradeInstrumentProvider.idString(symbol);
            return id.startsWith("-") ? ("\\" + id) : id;
        }
        if (symbol.endsWith(FFM_SUFFIX)
                && symbolStrategy == SymbolStrategyEnum.VWDCODE
                && RequestContextHolder.getRequestContext().isEnabled(FeatureFlags.Flag.REQUEST_FFMST_WITH_FFM)) {
            return getQuerySymbol(symbol + " " + symbol + FFMST_ST_SUFFIX, symbolStrategy);
        }
        return LUCENE_ESCAPE_CHARS.matcher(symbol).replaceAll("\\\\$0");
    }

    private String getSearchFieldForSymbolStrategy(SymbolStrategyEnum symbolStrategy) {
        switch (symbolStrategy) {
            case IID:
                return IndexConstants.FIELDNAME_IID;
            case ISIN:
                return KeysystemEnum.ISIN.name().toLowerCase();
            case QID:
                return IndexConstants.FIELDNAME_QID;
            case VWDCODE:
                return KeysystemEnum.VWDCODE.name().toLowerCase();
            case MMWKN:
                return KeysystemEnum.MMWKN.name().toLowerCase();
            case WKN:
                return KeysystemEnum.WKN.name().toLowerCase();
            case BIS_KEY:
                return KeysystemEnum.BIS_KEY.name().toLowerCase();
            case INFRONT_ID:
                return KeysystemEnum.INFRONT_ID.name().toLowerCase();
            default:
                throw new IllegalArgumentException("invalid strategy: " + symbolStrategy.name());
        }
    }

    private EnumSet<InstrumentSearcherImpl.SIMPLESEARCH_STEPS> getSearchSteps(
            StrategyEnum strategy) {
        if (strategy == null) {
            return null;
        }
        switch (strategy) {
            case EXACT:
                return EnumSet.of(InstrumentSearcherImpl.SIMPLESEARCH_STEPS.EXACT);
            case TOLERANT:
                return EnumSet.of(
                        InstrumentSearcherImpl.SIMPLESEARCH_STEPS.FUZZY,
                        InstrumentSearcherImpl.SIMPLESEARCH_STEPS.FUZZIER,
                        InstrumentSearcherImpl.SIMPLESEARCH_STEPS.FUZZIEST
                );
            case DEFAULT: // intentional fall-through
            default:
                return null;
        }
    }

    private String getSymbolCacheKey(String symbol, SymbolStrategyEnum strategy) {
        return strategy.ordinal() + ":" + symbol;
    }

    private Instrument identifyInstrumentBySymbol(String symbol, SymbolStrategyEnum strategy) {
        final Instrument result
                = identifyInstruments(Collections.singletonList(symbol), strategy).get(symbol);
        if (result == null) {
            throw new UnknownSymbolException("unknown symbol: '" + symbol + "' for " + strategy);
        }
        return result;
    }

    private Map<String, Instrument> identifyInstrumentsByIid(List<String> symbols) {
        final List<Instrument> instruments = identifyInstruments(toIds(symbols));
        return mapSymbolsToInstruments(symbols, instruments);
    }

    /**
     * Identify instruments for a list of symbols, uses the symbolsCache
     * @param symbols list of symbols
     * @param strategy how to interpret symbols
     * @return map which maps symbols to the respective instrument
     */
    private Map<String, Instrument> identifyInstrumentsBySymbol(List<String> symbols,
            final SymbolStrategyEnum strategy) {
        final List<String> requestSymbols = new ArrayList<>(symbols.size());
        final List<Long> requestIids = new ArrayList<>(symbols.size());
        for (String symbol : symbols) {
            final Long iid = getIidForSymbol(symbol, strategy);
            if (iid != null) {
                requestIids.add(iid);
            }
            else {
                requestSymbols.add(symbol);
                requestIids.add(0L);
            }
        }

        final Map<String, Instrument> result = new HashMap<>();

        if (requestSymbols.size() != symbols.size()) { // at least one value != 0L in requestIids
            final List<Instrument> instruments = identifyInstruments(requestIids);
            if (instruments != null) {
                for (int i = 0; i < symbols.size(); i++) {
                    if (instruments.get(i) != null) {
                        result.put(symbols.get(i), instruments.get(i));
                    }
                }
            }
        }

        if (!requestSymbols.isEmpty()) {
            final Map<String, Instrument> requestedSymbolMap
                    = searchInstrumentsBySymbol(requestSymbols, strategy);
            for (Map.Entry<String, Instrument> entry : requestedSymbolMap.entrySet()) {
                addIidForSymbol(entry.getKey(), strategy, entry.getValue().getId());
            }
            result.putAll(requestedSymbolMap);
        }

        if (this.logger.isDebugEnabled() && symbols.size() != result.size()) {
            final StringBuilder sb = new StringBuilder(Math.max(100, symbols.size() * 16));
            sb.append("<identifyInstrumentsBySymbol> incomplete: ");
            for (int i = 0; i < symbols.size(); i++) {
                final String s = symbols.get(i);
                sb.append(" ").append(s).append(":").append(requestIids.get(i)).append(";")
                        .append(result.containsKey(s) ? "I" : "-");
            }
            this.logger.debug(sb.toString());
        }

        return result;
    }

    private Map<String, Instrument> mapSymbolsToInstruments(List<String> symbols,
            List<Instrument> instruments) {
        final Map<String, Instrument> result = new HashMap<>();
        if (instruments != null) {
            for (int i = 0; i < instruments.size(); i++) {
                if (instruments.get(i) != null) {
                    result.put(symbols.get(i), instruments.get(i));
                }
            }
        }
        return result;
    }

    private Map<String, Instrument> mapBySymbol(List<Instrument> instruments,
            SymbolStrategyEnum symbolStrategy) {
        final Map<String, Instrument> result = new HashMap<>(instruments.size() * 2);
        for (Instrument instrument : instruments) {
            if (instrument == null) {
                continue;
            }
            switch (symbolStrategy) {
                case IID:
                    result.put(EasytradeInstrumentProvider.iidSymbol(instrument.getId()), instrument);
                    break;
                case ISIN:
                    if (instrument.getSymbolIsin() != null) {
                        result.put(instrument.getSymbolIsin(), instrument);
                    }
                    break;
                case QID:
                    for (final Quote quote : instrument.getQuotes()) {
                        result.put(EasytradeInstrumentProvider.qidSymbol(quote.getId()), instrument);
                    }
                    break;
                case VWDCODE:
                    instrument.getQuotes()
                            .stream()
                            .map(Quote::getSymbolVwdcode)
                            .filter(Objects::nonNull)
                            .forEach(symbol -> result.put(symbol, instrument));
                    break;
                case MMWKN:
                    instrument.getQuotes()
                            .stream()
                            .map(Quote::getSymbolMmwkn)
                            .filter(Objects::nonNull)
                            .forEach(symbol -> result.put(symbol, instrument));
                    break;
                case BIS_KEY:
                    instrument.getQuotes()
                            .stream()
                            .map(Quote::getSymbolBisKey)
                            .filter(Objects::nonNull)
                            .forEach(symbol -> result.put(symbol, instrument));
                    break;
                case WKN:
                    if (instrument.getSymbolWkn() != null) {
                        result.put(instrument.getSymbolWkn(), instrument);
                    }
                    break;
                case INFRONT_ID:
                    instrument.getQuotes()
                        .stream()
                        .map(Quote::getSymbolInfrontId)
                        .filter(Objects::nonNull)
                        .forEach(s -> result.put(s, instrument));
                    break;
                default:
                    throw new IllegalArgumentException("unknown strategy: " + symbolStrategy.name());
            }
        }
        return result;
    }

    private List<Long> toIds(List<String> symbols) {
        return symbols.stream()
                        .map(EasytradeInstrumentProvider::id)
                        .collect(Collectors.toList());
    }

    private List<String> uniq(List<String> symbols) {
        return new ArrayList<>(new HashSet<>(symbols));
    }
}
