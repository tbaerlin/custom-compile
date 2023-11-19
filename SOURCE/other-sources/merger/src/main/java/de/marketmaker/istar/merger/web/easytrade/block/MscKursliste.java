/*
 * MscKursliste.java
 *
 * Created on 13.07.2006 07:06:22
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.block;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.Callable;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.joda.time.Interval;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import de.marketmaker.istar.common.dmxmldocu.MmInternal;
import de.marketmaker.istar.common.util.DateUtil;
import de.marketmaker.istar.common.util.TimeTaker;
import de.marketmaker.istar.domain.Language;
import de.marketmaker.istar.domain.data.BasicHistoricRatios;
import de.marketmaker.istar.domain.data.HighLow;
import de.marketmaker.istar.domain.data.PriceQuality;
import de.marketmaker.istar.domain.data.PriceRecord;
import de.marketmaker.istar.domain.data.PriceRecordComparator;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.domain.instrument.QuoteComparator;
import de.marketmaker.istar.domain.instrument.QuoteNameStrategy;
import de.marketmaker.istar.domain.profile.Profile;
import de.marketmaker.istar.domainimpl.data.HighLowImpl;
import de.marketmaker.istar.domainimpl.data.IndexComposition;
import de.marketmaker.istar.domainimpl.data.NullPriceRecord;
import de.marketmaker.istar.domainimpl.profile.ProfileFactory;
import de.marketmaker.istar.instrument.IndexCompositionResponse;
import de.marketmaker.istar.merger.MergerException;
import de.marketmaker.istar.merger.context.RequestContextHolder;
import de.marketmaker.istar.merger.provider.HighLowProvider;
import de.marketmaker.istar.merger.provider.HistoricRatiosProvider;
import de.marketmaker.istar.merger.provider.IntradayProvider;
import de.marketmaker.istar.merger.provider.ProfiledIndexCompositionProvider;
import de.marketmaker.istar.merger.provider.SymbolQuote;
import de.marketmaker.istar.merger.web.HttpRequestUtil;
import de.marketmaker.istar.merger.web.easytrade.BadRequestException;
import de.marketmaker.istar.merger.web.easytrade.HasListid;
import de.marketmaker.istar.merger.web.easytrade.HasMarketStrategy;
import de.marketmaker.istar.merger.web.easytrade.HasOnlyEntitledQuotes;
import de.marketmaker.istar.merger.web.easytrade.HasPartitioning;
import de.marketmaker.istar.merger.web.easytrade.HasSymbolArray;
import de.marketmaker.istar.merger.web.easytrade.HasSymbolStrategy;
import de.marketmaker.istar.merger.web.easytrade.ListCommandWithOptionalPaging;
import de.marketmaker.istar.merger.web.easytrade.ListResult;
import de.marketmaker.istar.merger.web.easytrade.Period;
import de.marketmaker.istar.merger.web.easytrade.ProfiledInstrument;
import de.marketmaker.istar.merger.web.easytrade.SymbolStrategyEnum;
import de.marketmaker.istar.ratios.Partition;
import de.marketmaker.istar.ratios.PartitionUtil;

import static de.marketmaker.istar.common.util.CollectionUtils.nCopiesRemovable;
import static de.marketmaker.istar.common.util.CollectionUtils.removeNulls;
import static de.marketmaker.istar.merger.web.easytrade.block.EasytradeInstrumentProvider.qidSymbol;

/**
 * Returns price data for a set of quotes.
 * The set of quotes is specified by
 * <ul>
 * <li>the quote or instrument symbol of an index (parameter <tt>listid</tt>)</li>
 * <li>a list id as defined in MDP (parameter <tt>listid</tt>)</li>
 * <li>an array of symbols (parameter <tt>symbol</tt>)</li>
 * </ul>
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class MscKursliste extends EasytradeCommandController {
    //    private final Logger logger = LoggerFactory.getLogger(getClass());

    private static final String DEFAULT_SORT_BY = "name";

    private static final String PCT_PERFORMANCE_SORT_BY = "changePercent";

    private static final String NET_PERFORMANCE_SORT_BY = "changeNet";

    private static final SortSupport<Quote> QUOTE_SORT_SUPPORT;

    private static final SortSupport<PriceRecord> PRICE_SORT_SUPPORT;

    private static final SortSupport<BigDecimal> DECIMAL_SORT_SUPPORT;

    static {
        QUOTE_SORT_SUPPORT = SortSupport.createBuilder("name", SortSupport.QUOTE_NAME)
                .add("marketVwd", QuoteComparator.BY_VWDFEED_MARKET)
                .add("currencyIso", QuoteComparator.BY_CURRENCY_ISO)
                .add("isin", QuoteComparator.BY_ISIN)
                .add("wkn", QuoteComparator.BY_WKN)
                .add("none", (Comparator<Quote>) null)
                .build();

        PRICE_SORT_SUPPORT = SortSupport.createBuilder("price", PriceRecordComparator.BY_PRICE)
                .add("volume", PriceRecordComparator.BY_VOLUME_DAY)
                .add("turnoverDay", PriceRecordComparator.BY_TURNOVER_DAY)
                .add("date", PriceRecordComparator.BY_LATEST_TRADE)
                .build();

        DECIMAL_SORT_SUPPORT
                = SortSupport.createBuilder(PCT_PERFORMANCE_SORT_BY, SortSupport.COMPARATOR_BIGDECIMAL)
                .add(NET_PERFORMANCE_SORT_BY, SortSupport.COMPARATOR_BIGDECIMAL)
                .build();
    }

    private static final List<String> QID_SORT_FIELDS;
    //private static final String LISTNAME_PREFIX = "lbbw";

    static {
        QID_SORT_FIELDS = SortSupport.getSortFields(QUOTE_SORT_SUPPORT, PRICE_SORT_SUPPORT,
                DECIMAL_SORT_SUPPORT);
    }

    public static class Command extends ListCommandWithOptionalPaging implements InitializingBean,
            HasSymbolArray, HasListid, HasSymbolStrategy, HasMarketStrategy, HasOnlyEntitledQuotes,
            HasPartitioning {
        public static final String DEFAULT_PERIOD = "P1D";

        private SymbolStrategyEnum symbolStrategy;

        private String listid;

        private String[] symbol;

        private String period = DEFAULT_PERIOD;

        private String marketStrategy;

        private boolean onlyEntitledQuotes = false;

        private boolean withOptionalMarkets = false;

        private boolean partition = false;

        @Override
        public void afterPropertiesSet() throws Exception {
            if (this.listid == null && this.symbol == null) {
                throw new BadRequestException("listid and symbol are both null");
            }
        }

        public String getListid() {
            return listid;
        }

        public void setListid(String listid) {
            this.listid = listid;
        }

        public String[] getSymbol() {
            return symbol;
        }

        public void setSymbol(String[] symbol) {
            this.symbol = HttpRequestUtil.filterParametersWithText(symbol);
        }

        /**
         * Specifies the period for computation of the fields <tt>changeNet</tt> and <tt>changePercent</tt>.
         * The period must be specified in <a href="http://en.wikipedia.org/wiki/ISO_8601#Durations" target="_blank">ISO-8601</a> format.
         * Default value is {@value #DEFAULT_PERIOD}.
         */
        @Period
        public String getPeriod() {
            return period;
        }

        public void setPeriod(String period) {
            this.period = period;
        }

        public SymbolStrategyEnum getSymbolStrategy() {
            return symbolStrategy;
        }

        public void setSymbolStrategy(SymbolStrategyEnum symbolStrategy) {
            this.symbolStrategy = symbolStrategy;
        }

        public String getMarketStrategy() {
            return marketStrategy;
        }

        public void setMarketStrategy(String marketStrategy) {
            this.marketStrategy = marketStrategy;
        }

        public boolean isOnlyEntitledQuotes() {
            return onlyEntitledQuotes;
        }

        public void setOnlyEntitledQuotes(boolean onlyEntitledQuotes) {
            this.onlyEntitledQuotes = onlyEntitledQuotes;
        }

        /**
         * @return If <tt>true</tt> a list of optional markets is appended to the response.
         */
        public boolean isWithOptionalMarkets() {
            return withOptionalMarkets;
        }

        public void setWithOptionalMarkets(boolean withOptionalMarkets) {
            this.withOptionalMarkets = withOptionalMarkets;
        }

        @Override
        public void setPartition(boolean partition) {
            this.partition = partition;
        }

        /**
         * @return if result should be partitioned instead of paginated. Currently, only alphabetic
         * partitioning is supported. To use alphabetic partitioning, set request parameters
         * <code>sortBy=name</code> and <code>partition=true</code>.
         */
        @Override
        @MmInternal
        public boolean isPartition() {
            return partition;
        }
    }

    protected IntradayProvider intradayProvider;

    protected EasytradeInstrumentProvider instrumentProvider;

    private HighLowProvider highLowProvider;

    private HistoricRatiosProvider historicRatiosProvider;

    private ProfiledIndexCompositionProvider indexCompositionProvider;

    public MscKursliste() {
        super(Command.class);
    }

    public void setIndexCompositionProvider(
            ProfiledIndexCompositionProvider indexCompositionProvider) {
        this.indexCompositionProvider = indexCompositionProvider;
    }

    public void setHistoricRatiosProvider(HistoricRatiosProvider historicRatiosProvider) {
        this.historicRatiosProvider = historicRatiosProvider;
    }

    public void setIntradayProvider(IntradayProvider intradayProvider) {
        this.intradayProvider = intradayProvider;
    }

    public void setInstrumentProvider(EasytradeInstrumentProvider instrumentProvider) {
        this.instrumentProvider = instrumentProvider;
    }

    public void setHighLowProvider(HighLowProvider highLowProvider) {
        this.highLowProvider = highLowProvider;
    }

    private RequestDefinition getDefinition(final Command cmd) {
        final String name;
        final List<Quote> defQuotes;
        final List<String> itemnames;
        final String marketStrategy;

        if (cmd.getListid() != null) {
            if (cmd.getListid().endsWith(EasytradeInstrumentProvider.QID_SUFFIX)) {
                final Quote indexQuote = this.instrumentProvider.identifyQuote(cmd.getListid(), null, null, null);
                defQuotes = this.instrumentProvider.getIndexQuotes(qidSymbol(indexQuote.getId()));
                name = indexQuote.getInstrument().getName();
                itemnames = nCopiesRemovable(defQuotes.size(), null);
                marketStrategy = cmd.getMarketStrategy();
            }
            else if (cmd.getSymbolStrategy() != null ||
                    cmd.getListid().endsWith(EasytradeInstrumentProvider.IID_SUFFIX)) {
                final Quote quote;
                try {
                    // use all-Profile for identification of index instrument
                    quote = RequestContextHolder.callWith(ProfileFactory.valueOf(true), new Callable<Quote>() {
                        public Quote call() throws Exception {
                            return instrumentProvider.identifyQuote(cmd.getListid(), cmd.getSymbolStrategy(), null, null);
                        }
                    });
                } catch (MergerException e) {
                    throw e;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                defQuotes = this.instrumentProvider.getIndexQuotes(quote.getId() + ".qid");
                name = quote.getInstrument().getName();
                itemnames = nCopiesRemovable(defQuotes.size(), null);
                marketStrategy = cmd.getMarketStrategy();
            }
            else {
                final IndexComposition definition = getIndexComposition(cmd.getListid());

                final List<Long> quoteids = definition.getQids();
                itemnames = new ArrayList<>(quoteids.size());
                final Language lang = Language.valueOf(RequestContextHolder.getRequestContext().getLocale());
                for (final Long quoteid : quoteids) {
                    itemnames.add(definition.getLocalizedName(quoteid, lang));
                }

                defQuotes = this.instrumentProvider.identifyQuotes(quoteids);
                name = definition.getName();
                marketStrategy = cmd.getMarketStrategy() != null ? cmd.getMarketStrategy() : definition.getMarketStrategy();
            }
        }
        else {
            defQuotes = this.instrumentProvider.identifyQuotes(Arrays.asList(cmd.getSymbol()), null, null, null);
            name = "";
            itemnames = nCopiesRemovable(defQuotes.size(), null);
            marketStrategy = cmd.getMarketStrategy();
        }

        removeNulls(defQuotes, itemnames);

        final QuoteNameStrategy quoteNameStrategy
                = RequestContextHolder.getRequestContext().getQuoteNameStrategy();
        final List<String> names = new ArrayList<>(itemnames.size());
        for (int i = 0; i < defQuotes.size(); i++) {
            final Quote quote = defQuotes.get(i);
            final String itemname = itemnames.get(i);
            names.add(itemname == null ? quoteNameStrategy.getName(quote) : itemname);
        }

        final List<Quote> quotes;
        if (StringUtils.hasText(marketStrategy)) {
            quotes = new ArrayList<>(defQuotes.size());
            for (Quote quote : defQuotes) {
                quotes.add(this.instrumentProvider.getQuote(quote.getInstrument(), null, marketStrategy));
            }
        }
        else {
            quotes = defQuotes;
        }

        if (cmd.isOnlyEntitledQuotes()) {
            final Profile profile = RequestContextHolder.getRequestContext().getProfile();
            for (int i = 0; i < quotes.size(); i++) {
                final Quote quote = quotes.get(i);
                if (quote == null) {
                    continue;
                }
                if (profile.getPriceQuality(quote) == PriceQuality.NONE) {
                    quotes.set(i, null);
                    names.set(i, null);
                }
            }
        }

        removeNulls(quotes, names);

        // TODO: use name schema according to customer
        return new RequestDefinition(name, quotes, names);
    }

    private IndexComposition getIndexComposition(final String listid) {
        IndexCompositionResponse response
                = this.indexCompositionProvider.getIndexCompositionByName(listid);
        if (!response.isValid()) {
            return IndexComposition.createEmpty(listid);
        }
        return response.getIndexComposition();
    }

    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response,
            Object o, BindException errors) {
        final Command cmd = (Command) o;

        final RequestDefinition definition = getDefinition(cmd);
        final List<Quote> quotes = definition.getQuotes();
        final List<String> itemnames = definition.getItemnames();

        List<Partition> partitions = new ArrayList<>();
        if ("name".equals(cmd.getSortBy()) && cmd.isPartition()) {
            partitions = PartitionUtil.partition(itemnames);
        }

        final Map<String, OptionalMarketInfo> optionalMarkets = cmd.isWithOptionalMarkets()
                ? getMarketMap(quotes)
                : null;

        final ListResult listResult = ListResult.create(cmd, QID_SORT_FIELDS, DEFAULT_SORT_BY, quotes.size());

        boolean sorted = QUOTE_SORT_SUPPORT.apply(partitions, listResult.getSortedBy(), cmd, quotes, itemnames);

        final List<PriceRecord> prices = this.intradayProvider.getPriceRecords(quotes);

        if (!sorted) {
            sorted = PRICE_SORT_SUPPORT.apply(listResult.getSortedBy(), cmd, prices, quotes, itemnames);
        }

        final List<BigDecimal> changeNets = new ArrayList<>(quotes.size());
        final List<BigDecimal> changePercents = new ArrayList<>(quotes.size());

        final String s = cmd.getPeriod().toUpperCase();
        final String period = s.startsWith("P") ? s : "P" + s;
        final Interval interval = DateUtil.getInterval(period);

        for (int i = 0; i < quotes.size(); i++) {
            final Quote quote = quotes.get(i);
            final PriceRecord pr = prices.get(i);

            if ("P1D".equals(period)) {
                changeNets.add(pr.getChangeNet());
                changePercents.add(pr.getChangePercent());
            }
            else {
                final BasicHistoricRatios perf =
                        this.historicRatiosProvider.getBasicHistoricRatios(SymbolQuote.create(quote), null, Arrays.asList(interval)).get(0).copy(pr, NullPriceRecord.INSTANCE);
                changeNets.add(perf.getChangeNet());
                changePercents.add(perf.getPerformance());
            }
        }

        if (!sorted) {
            if (PCT_PERFORMANCE_SORT_BY.equals(listResult.getSortedBy())) {
                DECIMAL_SORT_SUPPORT.apply(listResult.getSortedBy(), cmd,
                        changePercents, changeNets, prices, quotes, itemnames);
            }
            else if (NET_PERFORMANCE_SORT_BY.equals(listResult.getSortedBy())) {
                DECIMAL_SORT_SUPPORT.apply(listResult.getSortedBy(), cmd,
                        changeNets, changePercents, prices, quotes, itemnames);
            }
        }

        listResult.setCount(quotes.size());

        final List<HighLow> highLows = getHighLows(quotes, prices, period, interval);

        final Map<String, Object> model = new HashMap<>();
        model.put("quotes", quotes);
        model.put("itemnames", itemnames);
        model.put("prices", prices);
        model.put("changeNets", changeNets);
        model.put("changePercents", changePercents);
        model.put("highLows", highLows);
        // this is a hack: normally, we want to use the default period (P1D), as that gives us
        // the correct changeNets/Percents, but we still need 52w high/low: request with explicit period:
        model.put("highLows1y", getHighLows(quotes, prices, "P1Y", null));
        model.put("listinfo", listResult);
        model.put("listname", definition.getListname());
        model.put("optionalMarkets", optionalMarkets);
        model.put("partitions", partitions);
        return new ModelAndView("msckursliste", model);
    }

    private List<HighLow> getHighLows(List<Quote> quotes, List<PriceRecord> prices, String period,
            Interval interval) {
        if ("P1Y".equalsIgnoreCase(period) || ("P52W".equalsIgnoreCase(period))) {
            return this.highLowProvider.getHighLows52W(quotes, prices);
        }
        final List<Interval> intervals = Arrays.asList(interval);
        final List<HighLow> result = new ArrayList<>(quotes.size());
        for (int i = 0; i < quotes.size(); i++) {
            final Quote quote = quotes.get(i);
            final PriceRecord pr = prices.get(i);
            if ("P1D".equals(period)) {
                result.add(new HighLowImpl(interval, pr.getHighDay(), pr.getLowDay()));
            }
            else {
                result.add(this.highLowProvider.getHighLow(SymbolQuote.create(quote), intervals).get(0).copy(pr));
            }
        }
        return result;
    }

    private Map<String, OptionalMarketInfo> getMarketMap(List<Quote> quotes) {
        final TimeTaker tt = new TimeTaker();

        final Map<String, OptionalMarketInfo> markets = new TreeMap<>();

        final Profile profile = RequestContextHolder.getRequestContext().getProfile();

        for (final Quote quote : quotes) {
            for (final Quote innerQuote : ProfiledInstrument.quotesWithPrices(quote.getInstrument(), profile)) {
                final String name = getMarketName(innerQuote);

                OptionalMarketInfo marketInfo = markets.get(name);
                if (marketInfo == null) {
                    marketInfo = new OptionalMarketInfo(name, getEscapedMarket(innerQuote));
                    markets.put(name, marketInfo);
                }
                marketInfo.incCount();
            }
        }

        this.logger.debug("<doHandle> optionalMarkets: " + markets + ", took " + tt);
        return markets;
    }

    private String getMarketName(Quote quote) {
        if (isXetra(quote.getSymbolVwdfeedMarket())) {
            return "XETRA";
        }
        if (isLondon(quote.getSymbolVwdfeedMarket())) {
            return "London";
        }
        if (isSWX(quote.getSymbolVwdfeedMarket())) {
            return "SWX";
        }
        return quote.getMarket().getName();
    }

    private String getEscapedMarket(Quote quote) {
        final String market = quote.getSymbolVwdfeedMarket();
        if (isXetra(market)) {
            return "market:ETR,EEU,EUS";
        }
        if (isLondon(market)) {
            return "market:UK,UKINT";
        }
        if (isSWX(market)) {
            return "market:CH,CHOTC";
        }
        return "market:" + market;
    }

    private boolean isXetra(String market) {
        return "ETR".equals(market)
                || "EEU".equals(market)
                || "EUS".equals(market);
    }

    private boolean isLondon(String market) {
        return "UK".equals(market) || "UKINT".equals(market);
    }

    private boolean isSWX(String market) {
        return "CH".equals(market) || "CHOTC".equals(market);
    }

    private static class RequestDefinition {
        private final String listname;

        private final List<Quote> quotes;

        private final List<String> itemnames;

        public RequestDefinition(String listname, List<Quote> quotes, List<String> itemnames) {
            this.listname = listname;
            this.quotes = quotes;
            this.itemnames = itemnames;
        }

        public String getListname() {
            return listname;
        }

        public List<Quote> getQuotes() {
            return quotes;
        }

        public List<String> getItemnames() {
            return itemnames;
        }
    }

    public static class OptionalMarketInfo {
        private final String name;

        private final String marketStrategy;

        private int count;

        public OptionalMarketInfo(String name, String marketStrategy) {
            this.name = name;
            this.marketStrategy = marketStrategy;
        }

        public void incCount() {
            this.count++;
        }

        public String getName() {
            return name;
        }

        public String getMarketStrategy() {
            return marketStrategy;
        }

        public int getCount() {
            return count;
        }

        @Override
        public String toString() {
            return "OptionalMarketInfo{" +
                    "name='" + name + '\'' +
                    ", marketStrategy='" + marketStrategy + '\'' +
                    ", count=" + count +
                    '}';
        }
    }
}
