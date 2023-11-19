/*
 * MscFeedSnapshot.java
 *
 * Created on 14.11.2011 22:06:22
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.block;

import de.marketmaker.istar.common.dmxmldocu.MmInternal;
import de.marketmaker.istar.common.util.CollectionUtils;
import de.marketmaker.istar.common.util.DateUtil;
import de.marketmaker.istar.common.validator.RestrictedSet;
import de.marketmaker.istar.common.validator.Size;
import de.marketmaker.istar.domain.data.HighLow;
import de.marketmaker.istar.domain.data.LiteralSnapField;
import de.marketmaker.istar.domain.data.PriceQuality;
import de.marketmaker.istar.domain.data.PriceRecord;
import de.marketmaker.istar.domain.data.SnapField;
import de.marketmaker.istar.domain.data.SnapRecord;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.domainimpl.data.HighLowImpl;
import de.marketmaker.istar.feed.FeedMarket;
import de.marketmaker.istar.feed.VendorkeyUtils;
import de.marketmaker.istar.feed.api.TypedVendorkeysRequest;
import de.marketmaker.istar.feed.api.TypedVendorkeysResponse;
import de.marketmaker.istar.feed.api.VendorkeyListRequest;
import de.marketmaker.istar.feed.api.VendorkeyListResponse;
import de.marketmaker.istar.feed.mdps.MdpsTypeMappings;
import de.marketmaker.istar.feed.snap.SnapRecordUtils;
import de.marketmaker.istar.feed.vwd.PriceRecordVwd;
import de.marketmaker.istar.feed.vwd.VendorkeyVwd;
import de.marketmaker.istar.feed.vwd.VwdFieldDescription;
import de.marketmaker.istar.merger.context.RequestContextHolder;
import de.marketmaker.istar.merger.provider.EntitlementQuoteProvider;
import de.marketmaker.istar.merger.provider.IntradayProvider;
import de.marketmaker.istar.merger.provider.ProfiledIndexCompositionProvider;
import de.marketmaker.istar.merger.util.SymbolUtil;
import de.marketmaker.istar.merger.web.HttpRequestUtil;
import de.marketmaker.istar.merger.web.easytrade.BadRequestException;
import de.marketmaker.istar.merger.web.easytrade.HasListid;
import de.marketmaker.istar.merger.web.easytrade.HasMarketStrategy;
import de.marketmaker.istar.merger.web.easytrade.HasQuote;
import de.marketmaker.istar.merger.web.easytrade.HasSymbolStrategy;
import de.marketmaker.istar.merger.web.easytrade.SymbolStrategyEnum;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

/**
 * Returns either the latest values for a number of vwd feed fields or a standard pricedata
 * record for a set of feed symbols.
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class MscFeedSnapshot extends EasytradeCommandController {
    private static final int CHUNK_SIZE = 5000;

    private static final String MARKET_DEPTH_SUFFIX = FeedMarket.MARKET_DEPTH_SUFFIX.toString();

    private static final String MARKET_DEPTH_TYPE_PREFIX = "12.";

    private static final int CACHE_CAPACITY = 2048;

    @SuppressWarnings("UnusedDeclaration")
    public static class Command implements
            InitializingBean, HasListid, HasSymbolStrategy, HasMarketStrategy {

        private static final boolean DEFAULT_RAW_FIELDS = true;

        private String[] symbol;

        private String market;

        private String regex;

        private boolean intradayQuotes;

        private String[] field;

        private boolean rawFields = DEFAULT_RAW_FIELDS;

        private SymbolStrategyEnum symbolStrategy;

        private String listid;

        private String marketStrategy;

        private boolean realQuotesAllowed;

        private DateTime withUpdateSince;

        @Override
        public void afterPropertiesSet() throws Exception {
            if (!this.intradayQuotes && this.symbol == null && this.market == null && this.listid == null) {
                throw new BadRequestException("symbol, market, or listid needs to be defined");
            }
        }

        /**
         * Request data for all symbols in this market (vwd feed market symbol)
         */
        @RestrictedSet("NIKKEI,CM,CB,HKI,PHIL,PT,BL,NL,FR,FXVWD,BUBA,CHIX,N,Q")
        public String getMarket() {
            return market;
        }

        public void setMarket(String market) {
            this.market = market;
        }

        /**
         * Regex to be applied to vwdsymbols if request is performed by market.
         */
        public String getRegex() {
            return regex;
        }

        public void setRegex(String regex) {
            this.regex = regex;
        }

        /**
         * Request data for these symbols, only used if <tt>market</tt> is undefined.
         * Instrumentdate and Quotedata fields in the response will only be available if data
         * is requested by symbol, if the symbol does not refer to a dedicated market depth
         * symbol (i.e., <tt>710000.ETRMT</tt>), and if instrument/quote data is allowed
         */
        @Size(max = 1000)
        public String[] getSymbol() {
            return symbol;
        }

        public void setSymbol(String[] symbol) {
            this.symbol = HttpRequestUtil.filterParametersWithText(symbol);
        }

        @MmInternal
        public boolean isIntradayQuotes() {
            return intradayQuotes;
        }

        public void setIntradayQuotes(boolean intradayQuotes) {
            this.intradayQuotes = intradayQuotes;
        }

        /**
         * Name or number of requested vwd feed fields, only used if <tt>rawFields</tt> is true;
         * leave undefined to request all allowed fields
         * @sample ADF_Bezahlt
         */
        public String[] getField() {
            return this.field;
        }

        public void setField(String[] field) {
            this.field = field;
        }

        /**
         * If true, individual feed fields can be requested by specifying <tt>field</tt>. If false,
         * the result contains a standard pricedata element for all symbols and <tt>field</tt> is
         * ignored. Default is <tt>{@value #DEFAULT_RAW_FIELDS}</tt>.
         */
        public boolean isRawFields() {
            return rawFields;
        }

        public void setRawFields(boolean rawFields) {
            this.rawFields = rawFields;
        }

        public SymbolStrategyEnum getSymbolStrategy() {
            return symbolStrategy;
        }

        public void setSymbolStrategy(SymbolStrategyEnum symbolStrategy) {
            this.symbolStrategy = symbolStrategy;
        }

        public String getListid() {
            return listid;
        }

        public void setListid(String listid) {
            this.listid = listid;
        }

        public String getMarketStrategy() {
            return marketStrategy;
        }

        public void setMarketStrategy(String marketStrategy) {
            this.marketStrategy = marketStrategy;
        }

        boolean isWithRealQuotes() {
            return this.realQuotesAllowed && !(isIntradayQuotes() || StringUtils.hasText(this.market));
        }

        @MmInternal
        public boolean isRealQuotesAllowed() {
            return this.realQuotesAllowed;
        }

        public void setRealQuotesAllowed(boolean realQuotesAllowed) {
            this.realQuotesAllowed = realQuotesAllowed;
        }

        /**
         * If set this block will only return feed data records with an update equal or younger than
         * the given timestamp.
         */
        public DateTime getWithUpdateSince() {
            return withUpdateSince;
        }

        public void setWithUpdateSince(DateTime withUpdateSince) {
            this.withUpdateSince = withUpdateSince;
        }
    }

    @SuppressWarnings("UnusedDeclaration")
    public static class Item implements HasQuote {
        private final Quote quote;

        private final PriceRecord priceRecord;

        private final HighLow highLow;

        private List<SnapField> fields;

        private Item(Quote quote, PriceRecord priceRecord, HighLow highLow) {
            this.quote = quote;
            this.priceRecord = priceRecord;
            this.highLow = highLow;
        }

        @Override
        public Quote getQuote() {
            return quote;
        }

        public boolean isRealQuote() {
            // entitlement quotes use id 0
            return this.quote.getId() > 0;
        }

        public PriceRecord getPriceRecord() {
            return priceRecord;
        }

        public HighLow getHighLow() {
            return highLow;
        }

        public void setFields(List<SnapField> fields) {
            this.fields = fields;
        }

        public List<SnapField> getFields() {
            return fields;
        }
    }

    private IntradayProvider intradayProvider;

    private EntitlementQuoteProvider entitlementQuoteProvider;

    protected EasytradeInstrumentProvider instrumentProvider;

    private ProfiledIndexCompositionProvider indexCompositionProvider;

    /**
     * small cache that helps to avoid TypedVendorkeysRequests
     */
    private final Map<String, String> vwdcodeToVendorkey = Collections.synchronizedMap(
            new LinkedHashMap<String, String>(CACHE_CAPACITY, 0.76f, true) {
                protected boolean removeEldestEntry(Map.Entry<String, String> eldest) {
                    return size() >= (CACHE_CAPACITY / 4 * 3);
                }
            }
    );

    public MscFeedSnapshot() {
        super(Command.class);
    }

    public void setIndexCompositionProvider(
            ProfiledIndexCompositionProvider indexCompositionProvider) {
        this.indexCompositionProvider = indexCompositionProvider;
    }

    public void setInstrumentProvider(EasytradeInstrumentProvider instrumentProvider) {
        this.instrumentProvider = instrumentProvider;
    }

    public void setIntradayProvider(IntradayProvider intradayProvider) {
        this.intradayProvider = intradayProvider;
    }

    public void setEntitlementQuoteProvider(EntitlementQuoteProvider entitlementQuoteProvider) {
        this.entitlementQuoteProvider = entitlementQuoteProvider;
    }

    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response,
            Object o, BindException errors) {
        final Command cmd = (Command) o;
        final Map<String, Object> model = new HashMap<>();

        final List<Item> items = createItems(cmd);

        model.put("numItems", items.size());
        model.put("isRawFields", cmd.isRawFields());
        model.put("items", items);

        return new ModelAndView("mscfeedsnapshot", model);
    }

    private List<Item> createItems(Command cmd) {
        final Interval interval = DateUtil.getInterval("P1Y");
        final List<VwdFieldDescription.Field> fields = parseFields(cmd.getField());

        final List<Quote> quotes
                = cmd.isWithRealQuotes() ? getQuotes(cmd) : getEntitlementQuotes(cmd);

        final int numKeys = quotes.size();
        final List<Item> items = new ArrayList<>(numKeys);

        for (int i = 0; i < numKeys; i += CHUNK_SIZE) {
            final List<Quote> quotesChunk = quotes.subList(i, Math.min(numKeys, i + CHUNK_SIZE));
            addChunkItems(quotesChunk, cmd, items, fields, interval);
        }
        return items;
    }

    private void addChunkItems(List<Quote> quotes, Command cmd, List<Item> items,
            List<VwdFieldDescription.Field> fields, Interval interval) {
        final List<PriceRecord> priceRecords = this.intradayProvider.getPriceRecords(quotes);

        final DateTime referenceTimestamp = cmd.getWithUpdateSince();
        final int date = referenceTimestamp == null ? Integer.MIN_VALUE : DateUtil.toYyyyMmDd(referenceTimestamp);
        final int time = referenceTimestamp == null ? Integer.MIN_VALUE : referenceTimestamp.getSecondOfDay();

        for (int j = 0; j < quotes.size(); j++) {
            final Quote quote = quotes.get(j);
            final PriceRecord priceRecord = priceRecords.get(j);

            if (priceRecord.getPriceQuality() == PriceQuality.NONE) {
                continue;
            }

            if (referenceTimestamp != null && priceRecord instanceof PriceRecordVwd) {
                final SnapRecord sr = ((PriceRecordVwd) priceRecord).getSnapRecord();
                final int doa = SnapRecordUtils.getDateOfArrival(sr);
                final int toa = SnapRecordUtils.getTimeOfArrival(sr);

                if ((date == doa && toa < time) || doa < date) {
                    continue;
                }
            }

            final HighLowImpl highLow
                    = new HighLowImpl(interval, priceRecord.getHigh52W(), priceRecord.getLow52W());

            final Item item = new Item(quote, priceRecord, highLow);
            items.add(item);

            if (cmd.isRawFields()) {
                final List<SnapField> itemFields = createRawFields(quote, fields, priceRecord);
                item.setFields(itemFields);
            }
        }

        RequestContextHolder.getRequestContext().clearIntradayContext();
    }

    private List<SnapField> createRawFields(Quote quote, List<VwdFieldDescription.Field> fields,
            PriceRecord priceRecord) {
        if (!(priceRecord instanceof PriceRecordVwd)) {
            return Collections.emptyList();
        }
        final SnapRecord snapRecord = ((PriceRecordVwd) priceRecord).getSnapRecord();
        if (fields == null) {
            return getAllFieldsFrom(snapRecord);
        }
        final List<SnapField> result = new ArrayList<>(fields.size());
        for (final VwdFieldDescription.Field field : fields) {
            final SnapField snapField = snapRecord.getField(field.id());
            if (snapField.isDefined()) {
                if (isUndefinedDate(field, snapField)) {
                    continue;
                }
                result.add(snapField);
            }
            else if (field.id() == VwdFieldDescription.DB_SEC_TYPE.id()) {
                final VendorkeyVwd vkey = VendorkeyVwd.getInstance(quote.getSymbolVwdfeed());
                final SnapField secType = LiteralSnapField.createString(
                        VwdFieldDescription.DB_SEC_TYPE.id(),
                        MdpsTypeMappings.getMdpsKeyTypeByVwdType(vkey.getType()));
                result.add(secType);
            }
        }
        return result;
    }

    private boolean isUndefinedDate(VwdFieldDescription.Field field, SnapField snapField) {
        return field.type() == VwdFieldDescription.Type.DATE
                && ((Number) snapField.getValue()).intValue() == 0;
    }

    private List<SnapField> getAllFieldsFrom(SnapRecord snapRecord) {
        Collection<SnapField> all = snapRecord.getSnapFields();
        ArrayList<SnapField> result = new ArrayList<>(all.size());
        for (SnapField sf : all) {
            if (isUndefinedDate(VwdFieldDescription.getField(sf.getId()), sf) || isMmfField(sf)) {
                continue;
            }
            result.add(sf);
        }
        return result;
    }

    private boolean isMmfField(SnapField sf) {
        return sf.getId() >= 2000 && sf.getId() <= 2009;
    }

    private List<Quote> getEntitlementQuotes(Command cmd) {
        final List<String> vendorkeys = getVendorkeys(cmd);
        return this.entitlementQuoteProvider.getQuotes(vendorkeys);
    }

    private List<Quote> getQuotes(Command cmd) {
        if (cmd.getSymbol() != null) {
            final String[] symbol = cmd.getSymbol();
            final SymbolStrategyEnum sse = cmd.getSymbolStrategy() == null && symbol.length > 0
                    ? SymbolUtil.guessStrategy(symbol[0])
                    : cmd.getSymbolStrategy();

            final List<Quote> result = this.instrumentProvider.identifyQuotes(Arrays.asList(symbol), sse, null, null);
            if (sse == SymbolStrategyEnum.VWDCODE) {
                addMarketDepthQuotes(symbol, result);
            }
            CollectionUtils.removeNulls(result);
            return result;
        }
        else if (cmd.getListid() != null) {
            final MscListConstituents.RequestDefinition definition
                    = MscListConstituents.getDefinition(this.instrumentProvider,
                    this.indexCompositionProvider, cmd.getListid(), cmd.getSymbolStrategy(),
                    cmd.getMarketStrategy());

            return definition.getQuotes();
        }
        return Collections.emptyList();
    }

    private void addMarketDepthQuotes(String[] symbol, List<Quote> quotes) {
        for (int i = 0; i < symbol.length; i++) {
            if (quotes.get(i) != null) {
                continue;
            }
            final Matcher m = VendorkeyVwd.KEY_PATTERN.matcher(symbol[i]);
            if (!m.matches() || !m.group(3).endsWith(MARKET_DEPTH_SUFFIX)) {
                continue;
            }
            quotes.set(i, this.entitlementQuoteProvider.getQuote(MARKET_DEPTH_TYPE_PREFIX + symbol[i]));
        }
    }

    private List<String> getVendorkeys(Command cmd) {
        if (cmd.isIntradayQuotes()) {
            return new ArrayList<>(RequestContextHolder.getRequestContext().getIntradayContext().keySet());
        }
        if (cmd.getMarket() != null) {
            return getVendorkeysForMarket(cmd.getMarket(), cmd.getRegex());
        }
        if (cmd.getSymbol() != null && cmd.getSymbol().length > 0) {
            return getQuotesForSymbols(cmd, cmd.getSymbol());
        }
        return Collections.emptyList();
    }

    private List<String> getQuotesForSymbols(Command cmd, String[] symbols) {
        final SymbolStrategyEnum sse = cmd.getSymbolStrategy() == null
                ? SymbolUtil.guessStrategy(symbols[0])
                : cmd.getSymbolStrategy();

        if (sse == SymbolStrategyEnum.VWDCODE) {
            return getVendorkeysForSymbols(symbols);
        }

        final List<Quote> quotes = this.instrumentProvider.identifyQuotes(Arrays.asList(symbols), sse, null, null);
        return quotes
                .stream()
                .filter(Objects::nonNull)
                .map(Quote::getSymbolVwdfeed)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private List<String> getVendorkeysForSymbols(String[] symbols) {
        for (int i = 0; i < symbols.length; i++) {
            symbols[i] = symbols[i].toUpperCase();
        }

        final ArrayList<String> result = new ArrayList<>(symbols.length);

        final TypedVendorkeysRequest request = new TypedVendorkeysRequest();
        for (final String s : symbols) {
            final String vendorkey = toVendorkey(s);
            if (vendorkey == null) {
                if (isAsciiString(s)) {
                    // since all valid keys are ascii strings, non-ascii string would not be found anyway
                    request.add(s);
                }
            }
            else {
                result.add(vendorkey);
            }
        }

        return (result.size() == symbols.length || request.isEmpty())
                ? result : evaluate(request, result);
    }

    private boolean isAsciiString(String s) {
        for (int i = 0, n = s.length(); i < n; i++) {
            final char c = s.charAt(i);
            if (c < 0x20 || c > 0x7F) {
                return false;
            }
        }
        return true;
    }

    private List<String> evaluate(TypedVendorkeysRequest request, ArrayList<String> result) {
        final TypedVendorkeysResponse response
                = this.intradayProvider.getTypesForVwdcodes(request);
        for (String vwdcode : response.getResult().keySet()) {
            final String typed = response.getTyped(vwdcode);
            result.add(typed);
            this.vwdcodeToVendorkey.put(vwdcode, typed);
        }
        return result;
    }

    private String toVendorkey(String vwdcode) {
        return VendorkeyUtils.isWithType(vwdcode) ? vwdcode : this.vwdcodeToVendorkey.get(vwdcode);
    }

    private List<String> getVendorkeysForMarket(final String market, String regex) {
        final VendorkeyListRequest request = new VendorkeyListRequest(market);
        final VendorkeyListResponse response = this.intradayProvider.getVendorkeys(request);

        if (regex == null) {
            return response.getVendorkeys();
        }

        final Matcher matcher = Pattern.compile(regex).matcher("");
        return response.getVendorkeys()
                .stream()
                .filter(vkey -> matcher.reset(vkey).matches())
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private List<VwdFieldDescription.Field> parseFields(String[] fields) {
        if (fields == null || fields.length == 0) {
            return null;
        }
        final List<VwdFieldDescription.Field> result = new ArrayList<>();
        for (final String s : fields) {
            final VwdFieldDescription.Field f = parseField(s);
            if (f == null) {
                throw new BadRequestException("Unknown field '" + s + "'");
            }
            result.add(f);
        }
        return result;
    }

    private VwdFieldDescription.Field parseField(String s) {
        if (s.matches("\\d+")) {
            final int fieldid = Integer.parseInt(s);
            return VwdFieldDescription.getField(fieldid);
        }
        return VwdFieldDescription.getFieldByName(s);
    }

}
