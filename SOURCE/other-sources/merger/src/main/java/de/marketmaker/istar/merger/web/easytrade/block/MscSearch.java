/*
 * MscSuchergebnis.java
 *
 * Created on 13.07.2006 07:06:22
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.block;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindException;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.ModelAndView;

import de.marketmaker.istar.common.dmxmldocu.MmInternal;
import de.marketmaker.istar.common.validator.NotNull;
import de.marketmaker.istar.domain.KeysystemEnum;
import de.marketmaker.istar.domain.data.PriceRecord;
import de.marketmaker.istar.domain.data.PriceRecordComparator;
import de.marketmaker.istar.domain.instrument.Instrument;
import de.marketmaker.istar.domain.instrument.InstrumentTypeEnum;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.domain.instrument.QuoteComparator;
import de.marketmaker.istar.merger.web.easytrade.ProfiledInstrument;
import de.marketmaker.istar.domainimpl.data.NullPriceRecord;
import de.marketmaker.istar.instrument.IndexConstants;
import de.marketmaker.istar.instrument.search.SearchResponse;
import de.marketmaker.istar.merger.context.RequestContextHolder;
import de.marketmaker.istar.merger.provider.InstrumentProvider;
import de.marketmaker.istar.merger.web.easytrade.EnumEditor;
import de.marketmaker.istar.merger.web.easytrade.ListCommand;
import de.marketmaker.istar.merger.web.easytrade.ListResult;
import de.marketmaker.istar.ratios.frontend.MinMaxAvgRatioSearchResponse;
import de.marketmaker.istar.ratios.frontend.MinMaxAvgVisitor;
import de.marketmaker.istar.ratios.frontend.RatioSearchRequest;

/**
 * Search for instruments/quotes based on names, symbols, instrument type, etc. The following
 * four blocks provide a similar service:
 * <table border=1>
 * <tr><th>block</th><th>finds/counts matching</th><th>result includes latest price</th></tr>
 * <tr><td>{@see MSC_BasicSearch}</td><td>instruments</td><td>no</td></tr>
 * <tr><td>{@see MSC_InstrumentPriceSearch}</td><td>instruments</td><td>yes</td></tr>
 * <tr><td>{@see MSC_BasicQuoteSearch}</td><td>quotes</td><td>no</td></tr>
 * <tr><td>{@see MSC_QuotePriceSearch}</td><td>quotes</td><td>yes</td></tr>
 * </table>
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class MscSearch extends AbstractFindersuchergebnis {

    protected static class SearchResult {
        private final List<Quote> quotes;

        private final SearchResponse response;

        public SearchResult(List<Quote> quotes, SearchResponse response) {
            this.quotes = quotes;
            this.response = response;
        }

        public List<Quote> getQuotes() {
            return quotes;
        }

        public SearchResponse getResponse() {
            return response;
        }
    }

    protected static final int MAX_SIZE = 500;

    private static final String SORT_FIELD = "relevance";

    protected final static InstrumentTypeEnum[] DEFAULT_COUNT_TYPES = new InstrumentTypeEnum[]{
            InstrumentTypeEnum.STK,
            InstrumentTypeEnum.FND,
            InstrumentTypeEnum.BND,
            InstrumentTypeEnum.CER,
            InstrumentTypeEnum.WNT
    };

    private static final SortSupport<Quote> QUOTE_SORT_SUPPORT
            = SortSupport.createBuilder("name", SortSupport.QUOTE_NAME)
            .add("wkn", QuoteComparator.BY_WKN)
            .add("marketName", SortSupport.MARKETNAME_THEN_NAME)
            .build();

    private static final SortSupport<PriceRecord> PRICE_SORT_SUPPORT
            = SortSupport.createBuilder("price", PriceRecordComparator.BY_PRICE)
            .add("date", PriceRecordComparator.BY_LATEST_TRADE)
            .add("changeNet", PriceRecordComparator.BY_CHANGE_NET)
            .add("changePercent", PriceRecordComparator.BY_CHANGE_PERCENT)
            .build();

    /**
     * Currently used only internally, NOT in ListResult
     */
    private static final List<String> SORT_FIELDS = Arrays.asList(
            IndexConstants.FIELDNAME_SORT_INSTRUMENT_DEFAULT,
            IndexConstants.FIELDNAME_NUM_INDEXED,
            IndexConstants.FIELDNAME_SORT_QUOTE_DEFAULT,
            IndexConstants.FIELDNAME_SORT_QUOTE_VOLUME,
            IndexConstants.FIELDNAME_SORT_QUOTE_VOLUME_PREFER_DE,
            IndexConstants.FIELDNAME_SORT_QUOTE_PREFER_CH,
            IndexConstants.FIELDNAME_SORT_QUOTE_PREFER_IT,
            IndexConstants.FIELDNAME_SORT_QUOTE_PREFER_NL,
            IndexConstants.FIELDNAME_SORT_QUOTE_PREFER_BE,
            IndexConstants.FIELDNAME_SORT_QUOTE_PREFER_FR
    );

    private final static EnumSet<InstrumentTypeEnum> COUNT_TYPES
            = EnumSet.copyOf(Arrays.asList(DEFAULT_COUNT_TYPES));

    private final static EnumSet<InstrumentTypeEnum> MSC_TYPES = EnumSet.complementOf(COUNT_TYPES);

    private boolean withPrices = false;

    private boolean countInstrumentResults = true;

    private String template;

    public MscSearch() {
        super(Command.class);
    }

    @Override
    protected void initBinder(HttpServletRequest httpServletRequest,
            ServletRequestDataBinder binder) throws Exception {
        super.initBinder(httpServletRequest, binder);
        EnumEditor.register(InstrumentProvider.StrategyEnum.class, binder);
        EnumEditor.register(KeysystemEnum.class, binder);
    }

    public void setWithPrices(boolean withPrices) {
        this.withPrices = withPrices;
    }

    public void setCountInstrumentResults(boolean countInstrumentResults) {
        this.countInstrumentResults = countInstrumentResults;
    }

    public void setTemplate(String template) {
        this.template = template;
    }

    @SuppressWarnings("UnusedDeclaration")
    public static class Command extends ListCommand implements InitializingBean {
        private String searchstring;

        private String instype;

        private String[] currency;

        private InstrumentTypeEnum[] countType;

        private InstrumentTypeEnum[] filterType;

        private boolean filterMSC = false;

        private boolean filterOpra = true;

        private String[] market;

        private boolean withQuotes = false;

        private String marketStrategy;

        private InstrumentProvider.StrategyEnum strategy = InstrumentProvider.StrategyEnum.DEFAULT;

        private boolean oldCountAndFilterStyle = true;

        private String[] searchfield;

        private String[] additionalSearchfield;

        private InstrumentTypeEnum filterForUnderlyingsForType;

        private Boolean filterForUnderlyingsOfLeverageProducts;

        private boolean withUnderlyings = false;

        @Override
        public void afterPropertiesSet() throws Exception {
            if (this.market == null) {
                return;
            }

            for (final String market : this.market) {
                if (market.contains(",")) {
                    expandMarkets();
                    break;
                }
            }
        }

        private void expandMarkets() {
            final Set<String> all = new HashSet<>();
            for (final String market : this.market) {
                //noinspection unchecked
                final Set<String> c = StringUtils.commaDelimitedListToSet(market);
                all.addAll(c);
            }
            this.market = all.toArray(new String[all.size()]);
        }

        @MmInternal
        public String getInstype() {
            return instype;
        }

        public void setInstype(String instype) {
            this.instype = instype;
        }

        /**
         * If defined, the result will contain counts for matching instruments/quotes with the
         * respective types. The counts of all matches for instrument types not specified here
         * will be returned in under the pseudo instrument type <tt>MSC</tt>.
         */
        public InstrumentTypeEnum[] getCountType() {
            return countType;
        }

        public void setCountType(InstrumentTypeEnum[] countType) {
            this.countType = countType;
        }

        /**
         * If defined, the total result count will be limited to matches of instruments/quotes
         * with these types. Note that the instrument types specified here must be a subset
         * of the instrument types specified as <tt>countType</tt>.
         */
        public InstrumentTypeEnum[] getFilterType() {
            return filterType;
        }

        public void setFilterType(InstrumentTypeEnum[] filterType) {
            this.filterType = filterType;
        }

        /**
         * limit search result to quotes or instruments with a quote at any of the given markets;
         * each market is specified using it's vwd symbol (e.g., ETR for Xetra). Many market can be
         * specified as multiple parameters or as a comma-separated list of markets in one market
         * parameter.
         */
        public String[] getMarket() {
            return market;
        }

        public void setMarket(String[] market) {
            this.market = market;
        }

        /**
         * limit search result to quotes or instruments with a quote listed in any of the given currencies;
         * each currency is specified using it's 3 letter iso symbol (e.g., EUR, USD, CHF).
         */
        public String[] getCurrency() {
            return currency;
        }

        public void setCurrency(String[] currency) {
            this.currency = currency;
        }

        /**
         * only for MSC_BasicSearch: if true, the result will contain not only the reference quote
         * but all other quotes as well.
         */
        public boolean isWithQuotes() {
            return withQuotes;
        }

        public void setWithQuotes(boolean withQuotes) {
            this.withQuotes = withQuotes;
        }

        public void setMarketStrategy(String marketStrategy) {
            this.marketStrategy = marketStrategy;
        }

        /**
         * @return name of market strategy used to determine the reference quote for instruments
         * in the search result; if undefined, a client specific default strategy will be used.
         */
        public String getMarketStrategy() {
            return marketStrategy;
        }

        /**
         * @return describes what should be found, possibly multiple words.
         * @sample 710000
         * @sample thyssen krupp
         * @sample microsotf
         */
        @NotNull
        public String getSearchstring() {
            return searchstring;
        }

        public void setSearchstring(String searchstring) {
            this.searchstring = searchstring;
        }

        /**
         * how the <tt>searchstring</tt> should be matched against instruments/quotes.
         * <dl>
         * <dt><tt>EXACT</tt></dt>
         * <dd>return exact matches</dd>
         * <dt><tt>TOLERANT</tt></dt>
         * <dd>return tolerant matches</dd>
         * <dt><tt>DEFAULT</tt> (= default value)</dt>
         * <dd>If exact matches exist, return those; otherwise, return tolerant matches</dd>
         * </dl>
         * For tolerant matching, instruments/quotes will be matched against the original search
         * string and <em>similar</em> strings; this allows to find results even if the original
         * search string contained a typo. Note, however, that similar strings will only be matched
         * against instrument/quote names, not symbols. Therefore, a TOLERANT search for de0007100001
         * will not find the instrument with ISIN de0007100000.
         * @return match strategy
         */
        public InstrumentProvider.StrategyEnum getStrategy() {
            return strategy;
        }

        public void setStrategy(InstrumentProvider.StrategyEnum strategy) {
            this.strategy = strategy;
        }

        @MmInternal
        public boolean isOldCountAndFilterStyle() {
            return oldCountAndFilterStyle;
        }

        public void setOldCountAndFilterStyle(boolean oldCountAndFilterStyle) {
            this.oldCountAndFilterStyle = oldCountAndFilterStyle;
        }

        @MmInternal
        public boolean isFilterMSC() {
            return filterMSC;
        }

        public void setFilterMSC(boolean filterMSC) {
            this.filterMSC = filterMSC;
        }

        public boolean isFilterOpra() {
            return filterOpra;
        }

        public void setFilterOpra(boolean filterOpra) {
            this.filterOpra = filterOpra;
        }

        @MmInternal
        public String[] getSearchfield() {
            return searchfield;
        }

        public void setSearchfield(String[] searchfield) {
            this.searchfield = searchfield;
        }

        @MmInternal
        public String[] getAdditionalSearchfield() {
            return additionalSearchfield;
        }

        public void setAdditionalSearchfield(String[] additionalSearchfield) {
            this.additionalSearchfield = additionalSearchfield;
        }

        /**
         * limits result to (quotes for) instruments that are underlyings of instruments
         * of the specified types
         */
        public InstrumentTypeEnum getFilterForUnderlyingsForType() {
            return filterForUnderlyingsForType;
        }

        public void setFilterForUnderlyingsForType(InstrumentTypeEnum filterForUnderlyingsForType) {
            this.filterForUnderlyingsForType = filterForUnderlyingsForType;
        }

        public void setFilterForUnderlyingsOfLeverageProducts(
                Boolean filterForUnderlyingsOfLeverageProducts) {
            this.filterForUnderlyingsOfLeverageProducts = filterForUnderlyingsOfLeverageProducts;
        }

        /**
         * in case of filterForUnderlyingsForType==CER: i) null: search for all underlyings,
         * ii) false: search for non-leverage product underlyings,
         * iii) true: only underlyings for leverage products of customer/issuer DZ BANK are relevant
         */
        @MmInternal
        public Boolean getFilterForUnderlyingsOfLeverageProducts() {
            return filterForUnderlyingsOfLeverageProducts;
        }

        /**
         * includes also underlying instrument data into result for derivative instruments
         */
        public boolean isWithUnderlyings() {
            return withUnderlyings;
        }

        public void setWithUnderlyings(boolean withUnderlyings) {
            this.withUnderlyings = withUnderlyings;
        }
    }

    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response,
            Object o, BindException errors) {
        final Command cmd = (Command) o;
        if (!validateCommand(cmd, errors)) {
            return null;
        }

        final EnumSet<InstrumentTypeEnum> filterTypes = getFilterTypes(cmd);
        final SearchResult result = search(cmd, filterTypes);

        final List<Quote> quotes = result.getQuotes();

        if (cmd.getFilterForUnderlyingsForType() != null) {
            filterForUnderlyings(cmd.getFilterForUnderlyingsForType(), cmd.getFilterForUnderlyingsOfLeverageProducts(), quotes);
        }

        final int total = result.getResponse().getTotalTypesCount();

        final ListResult listResult = ListResult.create(cmd, Collections.<String>emptyList(), SORT_FIELD, total);

        // instrumentProvider already returns a clipped list, just sort it
        boolean sorted = QUOTE_SORT_SUPPORT.applySort(cmd.getSortBy(), cmd, quotes);

        final List<PriceRecord> priceRecords = getPriceRecords(quotes);

        if (!sorted) {
            sorted = PRICE_SORT_SUPPORT.applySort(cmd.getSortBy(), cmd, priceRecords, quotes);
        }
        if (sorted) {
            listResult.setSort(cmd.getSortBy(), cmd.isAscending());
        }
        listResult.setCount(quotes.size());

        final int totalSelected = getTotalSelected(cmd, total, result.getResponse());

        final Map<String, Object> model = new HashMap<>();
        model.put("total", total);
        model.put("totalSelected", totalSelected);
        model.put("uniqueResult", result.getResponse().getInstrumentCount() == 1);

        listResult.setTotalCount(totalSelected);
        model.put("listinfo", listResult);
        model.put("priceRecords", priceRecords);
        model.put("withQuotes", cmd.isWithQuotes());
        if (cmd.isWithQuotes()) {
            model.put("quotesByIid", mapQuotesByIid(cmd, quotes));
        }
        model.put("quotes", quotes);

        if (cmd.isWithUnderlyings()) {
            retrieveUnderlyings(model, quotes);
        }

        final Map<String, Integer> counts = new LinkedHashMap<>();
        final InstrumentTypeEnum[] types = getTypes(cmd);
        for (InstrumentTypeEnum typeEnum : types) {
            model.put("total" + typeEnum.name(), result.getResponse().getTypeCounts().get(typeEnum));
            counts.put(typeEnum.name(), result.getResponse().getTypeCounts().get(typeEnum));
        }
        counts.put("MSC", types.length == 0
                ? result.getResponse().getTotalTypesCount()
                : result.getResponse().getRemainingTypesCount());
        model.put("totalMSC", result.getResponse().getRemainingTypesCount());
        model.put("counts", counts);

        return new ModelAndView(this.template, model);
    }

    private void retrieveUnderlyings(Map<String, Object> model, List<Quote> quotes) {
        final RetrieveUnderlyingsMethod ru = new RetrieveUnderlyingsMethod(quotes,
                this.instrumentProvider, null, this.underlyingShadowProvider);

        final List<Quote> uqs = ru.getUnderlyingQuotes();

        final Map<String, String> iid2uiid = new HashMap<>();
        final Map<Long, Quote> uiid2uq = new LinkedHashMap<>();

        for (int i = 0; i < quotes.size(); i++) {
            final Quote quote = quotes.get(i);
            final Quote uq = uqs.get(i);
            if (uq != null) {
                iid2uiid.put(Long.toString(quote.getInstrument().getId()), Long.toString(uq.getInstrument().getId()));

                if (!uiid2uq.containsKey(uq.getInstrument().getId())) {
                    uiid2uq.put(uq.getInstrument().getId(), uq);
                }
            }
        }

        model.put("iid2uiid", iid2uiid);
        model.put("uquotes", uiid2uq.values());
    }


    private void filterForUnderlyings(InstrumentTypeEnum type,
            Boolean filterForUnderlyingsOfLeverageProducts, List<Quote> quotes) {
        final Map<Long, Set<Long>> iidToShadows = getIidToShadows(quotes);

        final StringBuilder sb = new StringBuilder();
        final Set<Long> union = new HashSet<>();
        for (Set<Long> iids : iidToShadows.values()) {
            union.addAll(iids);
        }
        for (final Long iid : union) {
            if (sb.length() > 0) {
                sb.append("@");
            }
            sb.append(iid);
        }

        final RatioSearchRequest mmaRequest = new RatioSearchRequest(RequestContextHolder.getRequestContext().getProfile(),
                RequestContextHolder.getRequestContext().getLocales());
        mmaRequest.setType(type);
        mmaRequest.setVisitorClass(MinMaxAvgVisitor.class);
        final Map<String, String> mmaParameters = new HashMap<>();
        mmaParameters.put("underlyingiid", sb.toString());
        mmaRequest.setParameters(mmaParameters);
        mmaParameters.put(MinMaxAvgVisitor.KEY_GROUP_BY, "underlyingiid");


        final MinMaxAvgRatioSearchResponse mmaResponse
                = (MinMaxAvgRatioSearchResponse) this.ratiosProvider.search(mmaRequest);
        final Map<Integer, Map<String, Map<String, MinMaxAvgRatioSearchResponse.MinMaxAvg>>> mmaResult
                = mmaResponse.getResult();
        final Map<String, Map<String, MinMaxAvgRatioSearchResponse.MinMaxAvg>> mmaMap = mmaResult.get(0);

        final List<String> rows = new ArrayList<>();
        if (mmaMap != null) {
            rows.addAll(mmaMap.keySet());
        }


        final Set<Long> uiids = new HashSet<>();
        for (final String item : rows) {
            uiids.add(Long.parseLong(item));
        }

        final boolean simpleTypes = type == InstrumentTypeEnum.CER || type == InstrumentTypeEnum.WNT;

        // CER || WNT => filter only direct underlying results
        // CER && restrictToLeverageProductUnderlyingsDzbank => filter if no leverage product underlying
        // others => filter if hit at all
        for (final Iterator<Quote> it = quotes.iterator(); it.hasNext(); ) {
            final Instrument instrument = it.next().getInstrument();
            final long searchResultIid = instrument.getId();

            if (simpleTypes) {
                // here: type == CER || WNT
                if (uiids.contains(searchResultIid)) {
                    if (type != InstrumentTypeEnum.CER || filterForUnderlyingsOfLeverageProducts != Boolean.TRUE
                            || isLeverageProductUnderlyingDzbank(instrument)) {
                        continue;
                    }
                }
            }
            else {
                final Set<Long> shadows = iidToShadows.get(searchResultIid);
                if (!Collections.disjoint(shadows, uiids)) {
                    continue;
                }
            }

            it.remove();
        }
    }

    private boolean isLeverageProductUnderlyingDzbank(Instrument instrument) {
        final List<Quote> quotes = ProfiledInstrument.quotesWithPrices(instrument, RequestContextHolder.getRequestContext().getProfile());
        for (final Quote quote : quotes) {
            if (quote.getContentFlags().isLeverageProductUnderlyingDzbank()) {
                return true;
            }
        }
        return false;
    }

    private Map<Long, Set<Long>> getIidToShadows(List<Quote> quotes) {
        final Map<Long, Set<Long>> iidToShadows = new HashMap<>();

        for (final Quote quote : quotes) {
            final Instrument instrument = quote.getInstrument();
            if (!iidToShadows.containsKey(instrument.getId())) {
                final Set<Long> shadows = getUnderlyingIids(instrument, true);
                iidToShadows.put(instrument.getId(), shadows);
            }
        }
        return iidToShadows;
    }


    private Map<String, List<Quote>> mapQuotesByIid(Command cmd, List<Quote> quotes) {
        final Map<String, List<Quote>> result = new HashMap<>();
        for (final Quote quote : quotes) {
            final Instrument instrument = quote.getInstrument();
            result.put(Long.toString(instrument.getId()), resolveQuotes(cmd, instrument));
        }
        return result;
    }

    private List<Quote> resolveQuotes(Command cmd, Instrument instrument) {
        if (cmd.getMarketStrategy() != null) {
            return Collections.singletonList(getQuote(instrument, cmd));
        }
        final List<Quote> result = ProfiledInstrument.quotesWithPrices(instrument,
                RequestContextHolder.getRequestContext().getProfile());
        result.sort(QuoteComparator.BY_VWDFEED_MARKET);
        return result;
    }

    private InstrumentTypeEnum[] getTypes(Command cmd) {
        if (cmd.isOldCountAndFilterStyle()) {
            return DEFAULT_COUNT_TYPES;
        }
        if (cmd.getCountType() == null) {
            return new InstrumentTypeEnum[0];
        }
        return cmd.getCountType();
    }

    private boolean validateCommand(Command cmd, BindException errors) {
        if (cmd.isOldCountAndFilterStyle() || cmd.getFilterType() == null) {
            return true;
        }
        if (cmd.getCountType() == null) {
            errors.reject("search.failed", "count types must contain filter types");
            return false;
        }
        final EnumSet<InstrumentTypeEnum> filter = toEnumSet(cmd.getFilterType());
        final EnumSet<InstrumentTypeEnum> count = toEnumSet(cmd.getCountType());
        if (!count.containsAll(filter)) {
            errors.reject("search.failed", "count types must contain filter types");
            return false;
        }
        return true;
    }

    private List<PriceRecord> getPriceRecords(List<Quote> quotes) {
        if (this.withPrices) {
            return this.intradayProvider.getPriceRecords(quotes);
        }
        return new ArrayList<>(Collections.nCopies(quotes.size(),
                NullPriceRecord.INSTANCE));
    }

    private int getTotalSelected(Command cmd, int total, SearchResponse sr) {
        if (cmd.isOldCountAndFilterStyle()) {
            if (!StringUtils.hasText(cmd.getInstype())) {
                return total;
            }
            if ("MSC".equals(cmd.getInstype())) {
                return sr.getRemainingTypesCount();
            }
            final InstrumentTypeEnum key = InstrumentTypeEnum.valueOf(cmd.getInstype());
            if (sr.getTypeCounts().containsKey(key)) {
                return sr.getTypeCounts().get(key);
            }
            return 0;
        }

        if (cmd.isFilterMSC()) {
            int result = total;
            for (InstrumentTypeEnum type : toEnumSet(cmd.getCountType())) {
                final Integer typeCount = sr.getTypeCounts().get(type);
                if (typeCount != null) {
                    result -= typeCount;
                }
            }
            return result;
        }

        if (cmd.getFilterType() == null) {
            return total;
        }

        int count = 0;
        for (final InstrumentTypeEnum type : cmd.getFilterType()) {
            final Integer typeCount = sr.getTypeCounts().get(type);
            if (typeCount != null) {
                count += typeCount;
            }
        }
        return count;
    }

    private EnumSet<InstrumentTypeEnum> getFilterTypes(Command cmd) {
        if (cmd.isOldCountAndFilterStyle()) {
            if (StringUtils.hasText(cmd.getInstype())) {
                if ("MSC".equals(cmd.getInstype())) {
                    return MSC_TYPES;
                }
                return EnumSet.of(InstrumentTypeEnum.valueOf(cmd.getInstype()));
            }
        }
        else if (cmd.isFilterMSC()) {
            return EnumSet.complementOf(toEnumSet(cmd.getCountType()));
        }
        else if (cmd.getFilterType() != null) {
            return toEnumSet(cmd.getFilterType());
        }
        return null;
    }

    private static EnumSet<InstrumentTypeEnum> toEnumSet(InstrumentTypeEnum[] enums) {
        if (enums == null || enums.length == 0) {
            return EnumSet.noneOf(InstrumentTypeEnum.class);
        }
        return EnumSet.copyOf(Arrays.<InstrumentTypeEnum>asList(enums));
    }

    private SearchResult search(Command cmd, EnumSet<InstrumentTypeEnum> filterTypes) {
        final SimpleSearchCommand ssc = toSearchCommand(cmd, filterTypes);

        final SearchResponse sr = this.instrumentProvider.simpleSearch(ssc);

        final List<Quote> quotes = getQuotes(cmd, ssc, sr);
        return new SearchResult(quotes, sr);
    }

    private SimpleSearchCommand toSearchCommand(Command cmd,
            EnumSet<InstrumentTypeEnum> filterTypes) {
        final SimpleSearchCommand result = new SimpleSearchCommand(cmd.getSearchstring(),
                asList(cmd.getSearchfield()),
                asList(cmd.getAdditionalSearchfield()),
                cmd.getStrategy(),
                getCountTypes(cmd),
                filterTypes,
                asList(cmd.getMarket()),
                asList(cmd.getCurrency()),
                cmd.getOffset(), cmd.getAnzahl(), MAX_SIZE, this.countInstrumentResults);
        result.setFilterOpra(cmd.isFilterOpra());
        if (!StringUtils.hasText(cmd.getSortBy())) {
            return result;
        }
        final List<ListResult.Sort> sorts = ListResult.parseSortBy(cmd, SORT_FIELDS);
        if (sorts != null && !sorts.isEmpty()) {
            final String[] sortFields = new String[sorts.size()];
            for (int i = 0; i < sorts.size(); i++) {
                ListResult.Sort sort = sorts.get(i);
                sortFields[i] = sort.getName(); // TODO: ignore descending?
            }
            result.setSortFields(sortFields);
        }
        return result;
    }

    private EnumSet<InstrumentTypeEnum> getCountTypes(Command cmd) {
        return cmd.getCountType() != null
                ? EnumSet.copyOf(Arrays.asList(cmd.getCountType())) : COUNT_TYPES;
    }

    private List<String> asList(String... s) {
        return (s != null) ? Arrays.asList(s) : null;
    }

    private List<Quote> getQuotes(Command cmd, SimpleSearchCommand ssc, SearchResponse sr) {
        if (!this.countInstrumentResults || ssc.isSearchForSortedQuotes()) {
            return sr.getQuotes();
        }

        // gwt frontend uses instrument search even if it searches by vwdcode and expects only a
        // single result; adapting the strategy with a vwdcode selector helps to find the
        // quote for the selected symbol
        MarketStrategy ms = MarketStrategyFactory.getStrategy(null, cmd.getMarketStrategy());
        if (sr.getInstruments().size() == 1 && cmd.getSearchstring().contains(".")) {
            ms = new MarketStrategy.Builder(ms)
                    .withSelector(new QuoteSelectors.BySymbolVwdcode(cmd.getSearchstring()))
                    .build();
        }

        final List<Quote> result = new ArrayList<>();
        for (final Instrument i : sr.getInstruments()) {
            Quote q = getQuote(i, ms);
            if (q != null) {
                result.add(q);
            }
            else {
                // TODO: is this correct, do we really want to adapt the counts?
                sr.setNumTotalHits(sr.getNumTotalHits() - 1);
                sr.setInstrumentCount(sr.getInstrumentCount() - 1);
                Map<InstrumentTypeEnum, Integer> tc = sr.getTypeCounts();
                tc.put(i.getInstrumentType(), tc.get(i.getInstrumentType()) - 1);
            }
        }
        return result;
    }

    private Quote getQuote(Instrument i, Command cmd) {
        return this.instrumentProvider.getQuote(i, null, cmd.getMarketStrategy());
    }

    private Quote getQuote(Instrument i, MarketStrategy ms) {
        return this.instrumentProvider.getQuote(i, ms);
    }
}