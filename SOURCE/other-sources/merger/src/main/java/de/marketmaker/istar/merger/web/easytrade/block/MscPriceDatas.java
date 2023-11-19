/*
 * MscPriceDatas.java
 *
 * Created on 13.07.2006 07:06:22
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.block;

import de.marketmaker.istar.domainimpl.data.HistoricDataProfiler;
import de.marketmaker.istar.domainimpl.data.HistoricDataProfiler.Entitlement;
import de.marketmaker.istar.feed.vwd.RestrictedPriceRecordFactory;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.joda.time.DateTime;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import de.marketmaker.istar.common.dmxmldocu.MmInternal;
import de.marketmaker.istar.common.validator.RestrictedSet;
import de.marketmaker.istar.domain.data.HighLow;
import de.marketmaker.istar.domain.data.PriceRecord;
import de.marketmaker.istar.domain.data.PriceRecordComparator;
import de.marketmaker.istar.domain.instrument.MarketNameStrategies;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.domain.instrument.QuoteComparator;
import de.marketmaker.istar.domain.profile.Profile;
import de.marketmaker.istar.merger.context.RequestContextHolder;
import de.marketmaker.istar.merger.provider.HighLowProvider;
import de.marketmaker.istar.merger.provider.IntradayProvider;
import de.marketmaker.istar.merger.web.easytrade.ListResult;
import de.marketmaker.istar.merger.web.easytrade.ProfiledInstrument;
import de.marketmaker.istar.merger.web.easytrade.SymbolListCommand;

import static de.marketmaker.istar.domain.instrument.QuoteComparator.BY_MARKETNAME_LBBW;
import static de.marketmaker.istar.domain.instrument.QuoteComparator.BY_VWDFEED_MARKET;
import static de.marketmaker.istar.merger.context.RequestContext.KEY_RECENTTRADE_QID;

/**
 * Returns a quotation of current prices and bids for a financial instrument at all markets where the instrument is traded.
 * Only those markets are returned that are allowed by the user profile.
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class MscPriceDatas extends EasytradeCommandController {
    private static final SortSupport<Quote> QUOTE_SORT_SUPPORT;

    private static final SortSupport<PriceRecord> PRICE_SORT_SUPPORT;

    private static final String DEFAULT_SORT_BY = "default";

    private static final List<String> SORT_FIELDS;

    private static final HistoricDataProfiler HISTORIC_DATA_PROFILER = new HistoricDataProfiler();

    static {
        QUOTE_SORT_SUPPORT = SortSupport.createBuilder("marketVwd", BY_VWDFEED_MARKET)
                .add("marketName", QuoteComparator.byMarketName(MarketNameStrategies.DEFAULT))
                .add("default", SortSupport.zoneQuoteComparator(BY_MARKETNAME_LBBW))
                .build();

        PRICE_SORT_SUPPORT = SortSupport.createBuilder("price", PriceRecordComparator.BY_PRICE)
                .add("highDay", PriceRecordComparator.BY_HIGH_DAY)
                .add("lowDay", PriceRecordComparator.BY_LOW_DAY)
                .add("changePercent", PriceRecordComparator.BY_CHANGE_PERCENT)
                .add("numberOfTrades", PriceRecordComparator.BY_NUMBER_OF_TRADES)
                .add("volume", PriceRecordComparator.BY_VOLUME_DAY)
                .add("date", PriceRecordComparator.BY_LATEST_TRADE)
                .add("askVolume", PriceRecordComparator.BY_ASK_VOLUME)
                .add("bidVolume", PriceRecordComparator.BY_BID_VOLUME)
                .add("turnoverDay", PriceRecordComparator.BY_TURNOVER_DAY).build();

        SORT_FIELDS = SortSupport.getSortFields(QUOTE_SORT_SUPPORT, PRICE_SORT_SUPPORT);
    }

    public static class Command extends SymbolListCommand {
        private String filter;

        /**
         * Return only a subset of all quotes.
         * Valid filters are with_prices, with_vwdsymbol, market_country_de.
         */
        @RestrictedSet("with_prices,with_vwdsymbol,market_country_de") // see QuoteFilters.create(String s)
        public String getFilter() {
            return filter;
        }

        public void setFilter(String filter) {
            this.filter = filter;
        }

        @MmInternal
        public String getMarket() {
            return super.getMarket();
        }
    }

    private HighLowProvider highLowProvider;

    protected IntradayProvider intradayProvider;

    protected EasytradeInstrumentProvider instrumentProvider;

    public MscPriceDatas() {
        super(Command.class);
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

    @Override
    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response,
            Object o, BindException errors) {
        final Command cmd = (Command) o;

        final Quote quote = this.instrumentProvider.getQuote(cmd);
        if (quote == null) {
            errors.reject("quote.unknown", "no quote found");
            return null;
        }

        final List<Quote> quotes = getQuotes(quote, cmd);

        if (cmd.getFilter() == null && !quotes.contains(quote)) {
            errors.reject("quote.notallowed", "no allowed quote found");
            return null;
        }

        final ListResult listResult
                = ListResult.create(cmd, SORT_FIELDS, DEFAULT_SORT_BY, quotes.size());

        boolean sorted = QUOTE_SORT_SUPPORT.apply(listResult.getSortedBy(), cmd, quotes);

        List<PriceRecord> priceRecords = this.intradayProvider.getPriceRecords(quotes);

        final Profile profile = RequestContextHolder.getRequestContext().getProfile();
        final Entitlement entitlement = HISTORIC_DATA_PROFILER.getEntitlement(profile, quote);
        if (entitlement.isRestricted()) {
            priceRecords =
                    priceRecords.stream()
                            .map(p -> RestrictedPriceRecordFactory.createPriceRecord(profile, quote, p))
                            .collect(Collectors.toList());
        }

        if (!sorted && !quotes.isEmpty()) {
            PRICE_SORT_SUPPORT.apply(listResult.getSortedBy(), cmd, priceRecords, quotes);
        }

        final List<Quote> highLowQuotes =
                quotes.stream()
                        .map(q -> HISTORIC_DATA_PROFILER.getEntitlement(profile, q).isRestricted() ? null : q)
                        .collect(Collectors.toList());

        listResult.setCount(quotes.size());

        // todo: HACK for fww => improve to general solution
        setNewestQid(quotes, priceRecords);

        final Map<String, Object> model = new HashMap<>();
        model.put("instrument", quote.getInstrument());
        model.put("quote", quote);
        model.put("quotes", quotes);
        model.put("selecteds", getSelectedList(quote, quotes));
        model.put("prices", priceRecords);
        model.put("highLows", getHighLows(highLowQuotes, priceRecords));
        model.put("listinfo", listResult);

        return new ModelAndView("mscpricedatas", model);
    }

    private List<HighLow> getHighLows(List<Quote> quotes, List<PriceRecord> priceRecords) {
        return this.highLowProvider.getHighLows52W(quotes, priceRecords);
    }

    private List<Boolean> getSelectedList(Quote quote, List<Quote> quotes) {
        return quotes.stream().map(quote::equals).collect(Collectors.toList());
    }

    private List<Quote> getQuotes(Quote quote, Command cmd) {
        final Profile profile = RequestContextHolder.getRequestContext().getProfile();
        final List<Quote> result = ProfiledInstrument.quotesWithPrices(quote.getInstrument(), profile);
        if (StringUtils.hasText(cmd.getFilter())) {
            return QuoteFilters.create(cmd.getFilter()).apply(result);
        }
        return result;
    }

    private void setNewestQid(List<Quote> quotes, List<PriceRecord> priceRecords) {
        DateTime newest = null;
        Quote quote = null;
        for (int i = 0; i < quotes.size(); i++) {
            final DateTime date = priceRecords.get(i).getPrice().getDate();
            if (date != null && (newest == null || newest.isBefore(date))) {
                newest = date;
                quote = quotes.get(i);
            }
        }
        if (quote != null) {
            RequestContextHolder.getRequestContext().put(KEY_RECENTTRADE_QID, quote.getId());
        }
    }
}
