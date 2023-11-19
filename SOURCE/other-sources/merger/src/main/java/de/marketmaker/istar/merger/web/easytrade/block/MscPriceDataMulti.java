/*
 * StkKursdaten.java
 *
 * Created on 07.07.2006 10:28:22
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.block;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Period;
import org.springframework.aop.support.AopUtils;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import de.marketmaker.istar.common.dmxmldocu.MmInternal;
import de.marketmaker.istar.common.validator.Min;
import de.marketmaker.istar.common.validator.NotNull;
import de.marketmaker.istar.domain.data.HighLow;
import de.marketmaker.istar.domain.data.MasterDataCertificate;
import de.marketmaker.istar.domain.data.MasterDataWarrant;
import de.marketmaker.istar.domain.data.Price;
import de.marketmaker.istar.domain.data.PriceQuality;
import de.marketmaker.istar.domain.data.PriceRecord;
import de.marketmaker.istar.domain.instrument.Instrument;
import de.marketmaker.istar.domain.instrument.InstrumentTypeEnum;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.domain.instrument.QuoteComparator;
import de.marketmaker.istar.domain.profile.Profile;
import de.marketmaker.istar.domainimpl.data.HistoricDataProfiler;
import de.marketmaker.istar.domainimpl.data.HistoricDataProfiler.Entitlement;
import de.marketmaker.istar.domainimpl.data.NullPriceRecord;
import de.marketmaker.istar.domainimpl.data.PriceImpl;
import de.marketmaker.istar.feed.vwd.PriceRecordVwd;
import de.marketmaker.istar.feed.vwd.RestrictedPriceRecordFactory;
import de.marketmaker.istar.merger.context.RequestContextHolder;
import de.marketmaker.istar.merger.provider.HighLowProvider;
import de.marketmaker.istar.merger.provider.IntradayProvider;
import de.marketmaker.istar.merger.provider.IsoCurrencyConversionProvider;
import de.marketmaker.istar.merger.provider.ProviderPreference;
import de.marketmaker.istar.merger.provider.RatiosProvider;
import de.marketmaker.istar.merger.provider.UnknownSymbolException;
import de.marketmaker.istar.merger.provider.calendar.TradingCalendar;
import de.marketmaker.istar.merger.provider.calendar.TradingCalendarProvider;
import de.marketmaker.istar.merger.provider.calendar.TradingDay;
import de.marketmaker.istar.merger.provider.certificatedata.CertificateDataProvider;
import de.marketmaker.istar.merger.provider.certificatedata.WarrantDataProvider;
import de.marketmaker.istar.merger.util.SymbolUtil;
import de.marketmaker.istar.merger.web.easytrade.BaseMultiSymbolCommand;
import de.marketmaker.istar.merger.web.easytrade.ProfiledInstrument;
import de.marketmaker.istar.merger.web.easytrade.SymbolStrategyEnum;
import de.marketmaker.istar.ratios.RatioFieldDescription;
import de.marketmaker.istar.ratios.frontend.DefaultRatioSearchResponse;
import de.marketmaker.istar.ratios.frontend.RatioDataResult;
import de.marketmaker.istar.ratios.frontend.RatioSearchRequest;
import de.marketmaker.istar.ratios.frontend.RatioSearchResponse;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

/**
 * Queries price data for multiple symbols.
 *
 * <p>
 * The following three Requests share the same code but deliver different results:
 * {@see MSC_PriceDataMulti}, {@see MSC_PriceDataExtended}, and {@see MSC_PriceDataSimple}.
 * MSC_PriceDataExtended delivers the same data as the first one, but adds the structure &lt;pricedataExtended&gt;.
 * Consider using {@see MSC_PriceDataMulti} if the additional data of {@see MSC_PriceDataExtended} is not needed.
 * MSC_PriceDataSimple returns only the most important price fields.
 * </p>
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class MscPriceDataMulti extends EasytradeCommandController {
    private static final DateTimeZone DTZ = DateTimeZone.getDefault();

    private static final Period DELAY = Period.minutes(15);

    private static final Period NO_DELAY = Period.minutes(0);

    @SuppressWarnings("UnusedDeclaration")
    public static class Command extends BaseMultiSymbolCommand {

        private boolean onlyEntitledQuotes = false;

        private Period maximumAge;

        private String currency;

        private String filterForCurrency;

        private boolean withAllQuotesForInstrument;

        private String suffixPattern;

        private int maxCountPerSymbol = 0;

        private boolean addLastBidAsk;

        private boolean bisKeyExactMatch = true;

        @MmInternal
        public boolean isAddLastBidAsk() {
            return addLastBidAsk;
        }

        public void setAddLastBidAsk(boolean addLastBidAsk) {
            this.addLastBidAsk = addLastBidAsk;
        }

        @NotNull
        public String[] getSymbol() {
            return super.getSymbol();
        }

        /**
         * @return Restrict to entitled quotes. Default is false.
         */
        public boolean isOnlyEntitledQuotes() {
            return onlyEntitledQuotes;
        }

        public void setOnlyEntitledQuotes(boolean onlyEntitledQuotes) {
            this.onlyEntitledQuotes = onlyEntitledQuotes;
        }

        /**
         * @return A period restricts the returned prices.
         */
        public Period getMaximumAge() {
            return maximumAge;
        }

        public void setMaximumAge(Period maximumAge) {
            this.maximumAge = maximumAge;
        }

        /**
         * @return ISO currency in which the returned prices are converted using the latest currency
         * cross rate. If not set, no currency conversion is performed and the returned prices
         * are in their native currency.
         */
        public String getCurrency() {
            return currency;
        }

        public void setCurrency(String currency) {
            this.currency = currency;
        }

        /**
         * @return A RegEx that restricts the allowed suffix, when <tt>symbolStrategy=VWDCODE_PREFIX</tt> is used.
         * Default value is <tt>null</tt>, which means there is no restriction.
         */
        public String getSuffixPattern() {
            return suffixPattern;
        }

        public void setSuffixPattern(String suffixPattern) {
            this.suffixPattern = suffixPattern;
        }

        /**
         * @return The maximum number of resulting quotes per symbol, when <tt>symbolStrategy=VWDCODE_PREFIX</tt> is used.
         * Defaul value is <tt>0</tt>, which means there is no restriction.
         */
        @Min(0)
        public int getMaxCountPerSymbol() {
            return maxCountPerSymbol;
        }

        public void setMaxCountPerSymbol(int maxCountPerSymbol) {
            this.maxCountPerSymbol = maxCountPerSymbol;
        }

        /**
         * @return Return only quotes with the given currency iso code.
         */
        public String getFilterForCurrency() {
            return filterForCurrency;
        }

        public void setFilterForCurrency(String filterForCurrency) {
            this.filterForCurrency = filterForCurrency;
        }

        /**
         * @return for each instrument defined in the symbol list return all entitled quotes of that instrument.
         */
        public boolean isWithAllQuotesForInstrument() {
            return withAllQuotesForInstrument;
        }

        public void setWithAllQuotesForInstrument(boolean withAllQuotesForInstrument) {
            this.withAllQuotesForInstrument = withAllQuotesForInstrument;
        }

        boolean isInstrumentBased() {
            return (getSymbolStrategy() != null && getSymbolStrategy().isInstrumentStrategy())
                    || Arrays.stream(getSymbol())
                    .map(SymbolUtil::guessStrategy)
                    .allMatch(SymbolStrategyEnum::isInstrumentStrategy);
        }

        boolean isWithAllPrices() {
            return getMaximumAge() != null
                    && StringUtils.hasText(getMarketStrategy())
                    // to support overrides, a major refactoring would be necessary, so that
                    // applyMarketStrategy would know which quotes belonged to which strategy
                    && getMarketStrategyOverride() == null
                    && isInstrumentBased();
        }

        public boolean isBisKeyExactMatch() {
            return this.bisKeyExactMatch;
        }

        public void setBisKeyExactMatch(boolean bisKeyExactMatch) {
            this.bisKeyExactMatch = bisKeyExactMatch;
        }
    }

    private String template = "mscpricedatamulti";

    public void setTemplate(String template) {
        this.template = template;
    }

    private int maxPrefixLookup = 1000;

    public void setMaxPrefixLookup(int maxPrefixLookup) {
        this.maxPrefixLookup = maxPrefixLookup;
    }

    protected IntradayProvider intradayProvider;

    private HighLowProvider highLowProvider;

    protected EasytradeInstrumentProvider instrumentProvider;

    private RatiosProvider ratiosProvider;

    private WarrantDataProvider warrantDataProvider;

    private CertificateDataProvider certificateDataProvider;

    private TradingCalendarProvider tradingCalendarProvider;

    private IsoCurrencyConversionProvider isoCurrencyConversionProvider;

    private final HistoricDataProfiler historicDataProfiler = new HistoricDataProfiler();

    public MscPriceDataMulti() {
        super(Command.class);
    }

    public void setWarrantDataProvider(WarrantDataProvider warrantDataProvider) {
        this.warrantDataProvider = warrantDataProvider;
    }

    public void setCertificateDataProvider(CertificateDataProvider certificateDataProvider) {
        this.certificateDataProvider = certificateDataProvider;
    }

    public void setIntradayProvider(IntradayProvider intradayProvider) {
        this.intradayProvider = intradayProvider;
    }

    public void setInstrumentProvider(EasytradeInstrumentProvider instrumentProvider) {
        this.instrumentProvider = instrumentProvider;
    }

    public void setRatiosProvider(RatiosProvider ratiosProvider) {
        this.ratiosProvider = ratiosProvider;
    }

    public void setHighLowProvider(HighLowProvider highLowProvider) {
        this.highLowProvider = highLowProvider;
    }

    public void setTradingCalendarProvider(TradingCalendarProvider tradingCalendarProvider) {
        this.tradingCalendarProvider = tradingCalendarProvider;
    }

    public void setIsoCurrencyConversionProvider(
            IsoCurrencyConversionProvider isoCurrencyConversionProvider) {
        this.isoCurrencyConversionProvider = isoCurrencyConversionProvider;
    }

    @Override
    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response,
            Object o, BindException errors) {

        final Command cmd = (Command) o;

        final List<Quote> quotes = getQuotes(cmd);
        final List<PriceRecord> prices = getPrices(cmd, quotes);

        if (cmd.getMaximumAge() != null) {
            applyMaximumAge(cmd, quotes, prices);
        }

        final Profile profile = RequestContextHolder.getRequestContext().getProfile();

        final List<Quote> highLowQuotes = new ArrayList<>(quotes);
        for (int i = 0; i < quotes.size(); i++) {
            final Quote quote = quotes.get(i);
            if (quote == null) {
                continue;
            }
            final Entitlement entitlement = this.historicDataProfiler.getEntitlement(profile, quote);
            if (entitlement.isRestricted()) {
                prices.set(i, RestrictedPriceRecordFactory.createPriceRecord(profile, quote, prices.get(i)));
                highLowQuotes.set(i, null);
            }
        }

        if (StringUtils.hasText(cmd.getCurrency())) {
            applyCurrency(cmd.getCurrency(), quotes, prices, getHighLows(quotes, prices));
        }

        final Map<String, Object> model = new HashMap<>();
        model.put("count", quotes.size());
        model.put("quotes", quotes);
        model.put("prices", prices);
        model.put("highLows", getHighLows(highLowQuotes, prices));
        return new ModelAndView(this.template, model);
    }

    private List<HighLow> getHighLows(List<Quote> quotes, List<PriceRecord> prices) {
        if (this.highLowProvider == null) {
            return Collections.<HighLow>nCopies(quotes.size(), null);
        }
        return this.highLowProvider.getHighLows52W(quotes, prices);
    }

    private List<PriceRecord> getPrices(Command cmd, List<Quote> quotes) {
        final List<PriceRecord> prices = this.intradayProvider.getPriceRecords(quotes);

        if (!cmd.isAddLastBidAsk()) {
            return prices;
        }

        final List<PriceRecord> result = new ArrayList<>(prices.size());
        for (int i = 0; i < quotes.size(); i++) {
            result.add(getPriceWithLastBidAsk(prices.get(i)));
        }
        return result;
    }

    private PriceRecord getPriceWithLastBidAsk(PriceRecord pr) {
        if (pr.getClass() != PriceRecordVwd.class) {
            return pr;
        }
        if (isNonNull(pr.getBid()) || isNonNull(pr.getAsk())) {
            return pr;
        }
        BigDecimal lastAsk = pr.getLastAsk().getValue();
        BigDecimal lastBid = pr.getLastBid().getValue();
        return MyPriceRecord.create(pr, lastBid, lastAsk);
    }

    private boolean isNonNull(final Price price) {
        return isNonNull(price.getValue());
    }

    private boolean isNonNull(final BigDecimal value) {
        return value != null && BigDecimal.ZERO.compareTo(value) != 0;
    }

    private void applyCurrency(final String targetCurrency, List<Quote> quotes,
            List<PriceRecord> prices, List<HighLow> highLows) {
        new CurrencyConversionMethod(this.isoCurrencyConversionProvider, targetCurrency)
                .invoke(quotes, prices, highLows);
    }

    private void applyMaximumAge(Command cmd, List<Quote> quotes, List<PriceRecord> prices) {
        for (int i = 0; i < quotes.size(); i++) {
            final Quote quote = quotes.get(i);
            if (quote == null) {
                prices.set(i, NullPriceRecord.INSTANCE);
                continue;
            }

            final PriceRecord pr = prices.get(i);
            if (pr == NullPriceRecord.INSTANCE) {
                continue;
            }

            final DateTime earliestDate = getEarliestDate(cmd, quote, pr);
            if (earliestDate == null) {
                this.logger.warn("<applyMaximumAge> no trading calendar for " + quote);
                continue;
            }

            final DateTime date = getReferenceDate(pr);
            if (date == null || date.isBefore(earliestDate)) {
                if (this.logger.isDebugEnabled()) {
                    this.logger.debug("<applyMaximumAge> price too old for " + quote
                            + (date == null ? " (no date)" : ": " + date + " < " + earliestDate));
                }
                prices.set(i, NullPriceRecord.INSTANCE);
            }
        }
        if (cmd.isWithAllPrices()) {
            applyMarketStrategy(cmd, quotes, prices);
        }
    }

    private void applyMarketStrategy(Command cmd, List<Quote> quotes, List<PriceRecord> prices) {
        final Map<Long, PriceRecord> validPricesByQid = mapValidPricesByQid(quotes, prices);

        final MarketStrategy ms = new MarketStrategies(cmd).getDefaultStrategy();
        final List<Quote> tmpQuotes = new ArrayList<>(quotes);

        quotes.clear();
        prices.clear();

        final Set<Map.Entry<Long, List<Quote>>> slices =
                tmpQuotes.stream().filter(Objects::nonNull)
                        .filter(q -> validPricesByQid.containsKey(q.getId()))
                        .collect(groupingBy(q -> q.getInstrument().getId(), LinkedHashMap::new, toList()))
                        .entrySet();// each slice contains quotes for the same instrument with valid prices

        final Map<Long, List<Quote>> iid2quote = new HashMap<>();
        for (Map.Entry<Long, List<Quote>> slice : slices) {
            iid2quote.put(slice.getKey(), slice.getValue());
        }

        final Set<Long> alreadyHandled = new HashSet<>();

        for (Quote quote : tmpQuotes) {
            if (quote == null) {
                quotes.add(null);
                prices.add(NullPriceRecord.INSTANCE);
                continue;
            }

            final long iid = quote.getInstrument().getId();

            final List<Quote> slice = iid2quote.get(iid);
            final Quote selected = slice != null
                    ? ms.getQuote(slice.get(0).getInstrument(), slice)
                    : null;

            if (!alreadyHandled.contains(iid)) {
                if (selected != null) {
                    quotes.add(selected);
                    prices.add(validPricesByQid.get(selected.getId()));
                }
                else {
                    quotes.add(null);
                    prices.add(NullPriceRecord.INSTANCE);
                }
            }

            alreadyHandled.add(iid);
        }

        /*
        tmpQuotes.stream().filter(Objects::nonNull)
                .filter(q -> validPricesByQid.containsKey(q.getId()))
                .collect(groupingBy(q -> q.getInstrument().getId(), LinkedHashMap::new, toList()))
                .values() // each slice contains quotes for the same instrument with valid prices
                .forEach((List<Quote> slice) -> {
                    Quote selected = ms.getQuote(slice.get(0).getInstrument(), slice);
                    if (selected != null) {
                        quotes.add(selected);
                        prices.add(validPricesByQid.get(selected.getId()));
                    }
                });
                */
    }

    private Map<Long, PriceRecord> mapValidPricesByQid(List<Quote> quotes,
            List<PriceRecord> prices) {
        Map<Long, PriceRecord> byQid = new HashMap<>();
        for (int i = 0; i < quotes.size(); i++) {
            if (quotes.get(i) != null && prices.get(i) != NullPriceRecord.INSTANCE) {
                byQid.put(quotes.get(i).getId(), prices.get(i));
            }
        }
        return byQid;
    }

    private DateTime getReferenceDate(PriceRecord pr) {
        final DateTime pd = pr.getPrice().getDate();
        if (pd != null) {
            return pd;
        }

        final DateTime bd = pr.getBid().getDate();
        if (bd != null) {
            return bd;
        }

        final DateTime ad = pr.getAsk().getDate();
        if (ad != null) {
            return ad;
        }

        final DateTime pbd = pr.getPreviousBid().getDate();
        if (pbd != null) {
            return pbd.plusDays(1).minusSeconds(1); // time part of previous date is zero/midnight, handle as EoD
        }

        final DateTime pad = pr.getPreviousAsk().getDate();
        if (pad != null) {
            return pad.plusDays(1).minusSeconds(1); // time part of previous date is zero/midnight, handle as EoD
        }

        return null;
    }

    private DateTime getEarliestDate(Command cmd, Quote quote, PriceRecord pr) {
        final TradingCalendar cal = this.tradingCalendarProvider.calendar(quote.getMarket());
        final TradingDay[] days = cal.latestTradingDays(quote,
                getDaysToMaximumAge(cmd, pr), getDelayOffset(pr), DTZ);
        return getEarliestDate(days);
    }

    private DateTime getEarliestDate(TradingDay[] days) {
        final TradingDay first = Arrays.stream(days).filter(Objects::nonNull).findFirst().orElse(null);
        if (first == null) {
            return null;
        }
        return first.sessions()[0].sessionInterval(DTZ).getStart();
    }

    private int getDaysToMaximumAge(Command cmd, PriceRecord pr) {
        return cmd.getMaximumAge().getDays() + (pr.getPriceQuality() == PriceQuality.END_OF_DAY ? 1 : 0);
    }

    private Period getDelayOffset(PriceRecord pr) {
        return pr.getPriceQuality() == PriceQuality.DELAYED ? DELAY : NO_DELAY;
    }

    private List<Quote> getQuotes(Command cmd) {
        if (cmd.getSymbolStrategy() == SymbolStrategyEnum.BIS_KEY && cmd.getSymbol()[0].contains(",")) {
            return getSpecialBisKeyQuotes(cmd);
        }

        setMarketStrategyForUnderlying(cmd);

        final boolean allQuotesForInstrument
                = cmd.isWithAllQuotesForInstrument() || cmd.isWithAllPrices();

        final MarketStrategies marketStrategies = allQuotesForInstrument
                ? null : new MarketStrategies(cmd);

        List<Quote> result = cmd.getSymbolStrategy() == SymbolStrategyEnum.VWDCODE_PREFIX
                ? getByPrefix(marketStrategies, cmd.getSymbol(), cmd.getSuffixPattern(), cmd.getMaxCountPerSymbol())
                : getBySymbol(marketStrategies, cmd.getSymbol(), cmd.getSymbolStrategy(), allQuotesForInstrument);

        final String targetCurrency = cmd.getFilterForCurrency();
        if (targetCurrency != null) {
            result = result.stream()
                    .filter(Objects::nonNull)
                    .filter(quote -> targetCurrency.equals(quote.getCurrency().getSymbolIso()))
                    .collect(toList());
        }

        if (cmd.isOnlyEntitledQuotes()) {
            return QuoteFilters.WITH_PRICES.apply(result);
        }

        return result;
    }

    private List<Quote> getSpecialBisKeyQuotes(Command cmd) {
        final String[] symbol = cmd.getSymbol();

        final LinkedHashMap<String, String> map = new LinkedHashMap<>();

        final List<String> wkns = new ArrayList<>();
        final List<Long> qids = new ArrayList<>();

        for (final String s : symbol) {
            final String[] tokens = s.split(Pattern.quote(",")); // Tokens are: <WKN or underlyingWKN>,Eurex-Ticker,bisKey-Prefix,bisKey-Suffix
            if (tokens.length > 3 && StringUtils.hasText(tokens[3])) {
                final InstrumentTypeEnum type = tokens[3].endsWith("_0") ? InstrumentTypeEnum.FUT : InstrumentTypeEnum.OPT;
                final Long qid = getFinderBasedQuote(type, tokens[0], tokens[1], tokens[2], tokens[3], cmd.isBisKeyExactMatch());
                qids.add(qid);

                map.put(s, qid != null ? EasytradeInstrumentProvider.qidSymbol(qid) : null);
            }
            else {
                wkns.add(tokens[0]);
                map.put(s, tokens[0]);
            }
        }

        final List<Quote> derivativeQuotes = this.instrumentProvider.identifyQuotes(qids);
        final Map<String, Instrument> wknBasedInstruments = this.instrumentProvider.identifyInstrument(wkns, SymbolStrategyEnum.WKN);

        final List<Quote> result = new ArrayList<>(symbol.length);

        for (final Map.Entry<String, String> entry : map.entrySet()) {
            if (entry.getValue() == null) {
                result.add(null);
                continue;
            }

            if (entry.getValue().endsWith(EasytradeInstrumentProvider.QID_SUFFIX)) {
                final Long qid = EasytradeInstrumentProvider.id(entry.getValue());
                result.add(quote(derivativeQuotes, qid));
            }
            else {
                final Instrument instrument = wknBasedInstruments.get(entry.getValue());
                final String[] tokens = entry.getKey().split(Pattern.quote(","));

                result.add(quote(instrument, tokens[2]));
            }
        }

        return result;
    }

    private Quote quote(Instrument instrument, String bisKeyPrefix) {
        if (instrument == null) {
            return null;
        }
        for (final Quote quote : instrument.getQuotes()) {
            if (quote.getSymbolBisKey() != null && quote.getSymbolBisKey().startsWith(bisKeyPrefix + "_")) {
                return quote;
            }
        }
        return null;
    }

    private Quote quote(List<Quote> quotes, Long qid) {
        for (final Quote quote : quotes) {
            if (quote == null) {
                continue;
            }
            if (quote.getId() == qid) {
                return quote;
            }
        }
        return null;
    }

    private Long getFinderBasedQuote(InstrumentTypeEnum type, String underlyingWkn,
            String underlyingEurexTicker, String bisKeyPrefix, String bisKeySuffix, boolean bisKeyExactMatch) {

        final Map<String, String> params = StringUtils.hasText(underlyingWkn)
                ? Collections.singletonMap("underlyingWkn", underlyingWkn)
                : Collections.singletonMap("underlyingEurexTicker", "+" + underlyingEurexTicker);// + to signal enum based comparison

        final Long quoteid = getFinderBasedQuote(type, params, bisKeyPrefix, bisKeySuffix, bisKeyExactMatch);
        if (quoteid != null) {
            return quoteid;
        }

        // try special handling for eurex requests
        try {
            final Instrument underlyingProduct = StringUtils.hasText(underlyingWkn)
                    ? this.instrumentProvider.identifyByIsinOrWkn(underlyingWkn)
                    : this.instrumentProvider.identifyQuoteByVwdcode(underlyingEurexTicker + ".DTB").getInstrument();

            final Map<String, String> underlyingParams
                    = Collections.singletonMap(RatioFieldDescription.underlyingProductIid.name(),
                    Long.toString(underlyingProduct.getId()));

            return getFinderBasedQuote(type, underlyingParams, bisKeyPrefix, bisKeySuffix, bisKeyExactMatch);
        } catch (UnknownSymbolException e) {
            if (this.logger.isDebugEnabled()) {
                this.logger.debug("<getFinderBasedQuote> unknown symbol", e);
            }
        }
        return null;
    }

    private Long getFinderBasedQuote(InstrumentTypeEnum type, Map<String, String> underlyingParams,
            String bisKeyPrefix, String bisKeySuffix, boolean bisKeyExactMatch) {

        StringBuilder sb = new StringBuilder(32);
        sb.append("~^").append(bisKeyPrefix).append("_.*_").append(bisKeySuffix).append("$");
        if (!bisKeyExactMatch) {
            sb.deleteCharAt(1).deleteCharAt(sb.length() - 1);
        }
        final String bisKeyFilter = sb.toString();

        final RatioSearchRequest r = new RatioSearchRequest(RequestContextHolder.getRequestContext().getProfile());
        r.setType(type);
        r.addParameters(underlyingParams);
        r.addParameter("bisKey", bisKeyFilter);

        final RatioSearchResponse response = this.ratiosProvider.search(r);
        if (!response.isValid()) {
            return null;
        }
        final List<RatioDataResult> elements = ((DefaultRatioSearchResponse) response).getElements();
        if (elements.size() != 1) {
            this.logger.warn("<getFinderBasedQuote> #results != 1: " + elements.size() + " elements for " + underlyingParams + "/" + bisKeyFilter);
            return null;
        }
        return elements.get(0).getQuoteid();
    }

    private List<Quote> getByPrefix(MarketStrategies marketStrategies, String[] symbols,
            String suffixPattern, int maxCountPerSymbol) {
        final List<Quote> result = new ArrayList<>();

        for (final String symbol : symbols) {
            final List<Quote> list = getByPrefix(marketStrategies, symbol, suffixPattern);
            list.sort(QuoteComparator.BY_VWDCODE);
            result.addAll((maxCountPerSymbol > 0 && list.size() > maxCountPerSymbol) ? list.subList(0, maxCountPerSymbol) : list);
        }

        return result;
    }

    private List<Quote> getByPrefix(MarketStrategies marketStrategies, String symbol,
            String suffixPattern) {
        final List<Quote> result = new ArrayList<>();
        final List<Instrument> instruments = this.instrumentProvider.getByVwdcodePrefix(symbol, this.maxPrefixLookup);
        for (final Instrument instrument : instruments) {
            try {
                final Quote quote = marketStrategies.getQuote(symbol + "*", instrument, suffixPattern);
                if (quote != null) {
                    result.add(quote);
                }
            } catch (Exception ignore) {
                // ignore
            }
        }
        return result;
    }

    private List<Quote> getBySymbol(MarketStrategies marketStrategies, String[] symbol,
            SymbolStrategyEnum symbolStrategy, boolean withAllQuotesForInstrument) {
        final Map<String, Instrument> instrumentsBySymbol
                = this.instrumentProvider.identifyInstrument(Arrays.asList(symbol),
                symbolStrategy);

        final Profile profile = RequestContextHolder.getRequestContext().getProfile();

        final List<Quote> result = new ArrayList<>();

        for (final String s : symbol) {
            final Instrument instrument = instrumentsBySymbol.get(s);
            if (instrument == null) {
                result.add(null);
                continue;
            }

            if (withAllQuotesForInstrument) {
                final List<Quote> qwp = ProfiledInstrument.quotesWithPrices(instrument, profile);
                if (qwp.isEmpty()) {
                    result.add(null);
                }
                else {
                    result.addAll(qwp);
                }
                continue;
            }

            try {
                if (EasytradeInstrumentProvider.usesUnderlyingFunction(s)) {
                    result.add(marketStrategies.getQuote(
                            EasytradeInstrumentProvider.iidSymbol(instrument.getId()), instrument, null));
                }
                else {
                    result.add(marketStrategies.getQuote(s, instrument, null));
                }
            } catch (Exception ignore) {
                result.add(null);
            }
        }
        return result;
    }

    private void setMarketStrategyForUnderlying(Command cmd) {
        if (cmd.getMarketStrategy() != null
                || cmd.getSymbol().length != 1
                || !cmd.getSymbol()[0].contains("underlying")) {
            return;
        }

        final Quote quote = identifyQuote(cmd.getSymbol()[0]);
        if (quote == null) {
            return;
        }

        if (quote.getInstrument().getInstrumentType() == InstrumentTypeEnum.CUR) {
            cmd.setMarketStrategy("market:FXVWD,FX,FXX");
            return;
        }

        final Quote derivative = identifyQuote(getDerivativeSymbol(cmd.getSymbol()[0]));
        if (derivative == null) {
            return;
        }
        if (derivative.getInstrument().getInstrumentType() == InstrumentTypeEnum.CER) {
            final MasterDataCertificate data
                    = this.certificateDataProvider.getMasterData(derivative.getInstrument().getId(), ProviderPreference.VWD);
            cmd.setMarketStrategy("underlying:XXP," + data.getCurrencyStrike());
        }
        else if (derivative.getInstrument().getInstrumentType() == InstrumentTypeEnum.WNT) {
            final MasterDataWarrant data
                    = this.warrantDataProvider.getMasterData(derivative.getInstrument().getId(), ProviderPreference.VWD);
            cmd.setMarketStrategy("underlying:XXP," + data.getCurrencyStrike());
        }
    }

    private String getDerivativeSymbol(final String symbol) {
        return symbol.substring(symbol.indexOf("(") + 1, symbol.indexOf(")"));
    }

    private Quote identifyQuote(final String symbol) {
        return this.instrumentProvider.identifyQuote(symbol, SymbolStrategyEnum.AUTO, null, null);
    }

    /**
     * HACK only for gb-request.xml to overwrite 0 bid/ask values with their respective previous value
     */
    public static class MyPriceRecord implements InvocationHandler {
        private final PriceRecord delegate;

        private final BigDecimal bid;

        private final BigDecimal ask;

        public static PriceRecord create(PriceRecord pr, BigDecimal bid, BigDecimal ask) {
            if (pr == null || pr.getClass() != PriceRecordVwd.class) {
                return pr;
            }
            return (PriceRecord) Proxy.newProxyInstance(PriceRecord.class.getClassLoader(),
                    new Class<?>[]{PriceRecord.class}, new MyPriceRecord(pr, bid, ask));
        }

        private MyPriceRecord(PriceRecord delegate, BigDecimal bid, BigDecimal ask) {
            this.delegate = delegate;
            this.bid = bid;
            this.ask = ask;
        }

        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (AopUtils.isToStringMethod(method)) {
                return "[" + this.delegate.toString()
                        + ", bid=" + (this.bid != null ? this.bid.toPlainString() : null)
                        + ", ask=" + (this.ask != null ? this.ask.toPlainString() : null)
                        + "]";
            }
            if ("getBid".equals(method.getName())) {
                return new PriceImpl(this.bid, null, null, null, PriceQuality.REALTIME);
            }
            if ("getAsk".equals(method.getName())) {
                return new PriceImpl(this.ask, null, null, null, PriceQuality.REALTIME);
            }
            return method.invoke(this.delegate, args);
        }
    }
}
