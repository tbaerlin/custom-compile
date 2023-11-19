/*
 * WlWatchlistAnzeigen.java
 *
 * Created on 13.07.2006 07:06:22
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 *
 */
package de.marketmaker.istar.merger.web.easytrade.block;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.marketmaker.istar.domain.data.MasterDataStock;
import de.marketmaker.istar.merger.context.RequestContextHolder;
import de.marketmaker.istar.merger.provider.IsoCurrencyConversionProvider;
import de.marketmaker.istar.merger.provider.stockdata.StockDataProvider;
import org.joda.time.Period;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import de.marketmaker.istar.common.dmxmldocu.MmInternal;
import de.marketmaker.istar.common.util.ArraysUtil;
import de.marketmaker.istar.common.util.CollectionUtils;
import de.marketmaker.istar.common.validator.NotNull;
import de.marketmaker.istar.domain.data.BasicHistoricRatios;
import de.marketmaker.istar.domain.data.HighLow;
import de.marketmaker.istar.domain.data.PriceRecord;
import de.marketmaker.istar.domain.data.PriceRecordComparator;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.domain.instrument.QuoteComparator;
import de.marketmaker.istar.merger.provider.HighLowProvider;
import de.marketmaker.istar.merger.provider.HistoricRatiosProvider;
import de.marketmaker.istar.merger.provider.ProfiledIndexCompositionProvider;
import de.marketmaker.istar.merger.provider.IntradayProvider;
import de.marketmaker.istar.merger.provider.funddata.FundDataProvider;
import de.marketmaker.istar.merger.user.NoSuchPortfolioException;
import de.marketmaker.istar.merger.user.Portfolio;
import de.marketmaker.istar.merger.user.PortfolioPosition;
import de.marketmaker.istar.merger.user.User;
import de.marketmaker.istar.merger.user.UserContext;
import de.marketmaker.istar.merger.web.easytrade.ListCommandWithOptionalPaging;
import de.marketmaker.istar.merger.web.easytrade.ListResult;

import static de.marketmaker.istar.merger.web.easytrade.block.DividendCalculationStrategy.DIV_IN_QUOTE_CURRENCY;
import static de.marketmaker.istar.merger.web.easytrade.block.DividendCalculationStrategy.DIV_LAST_YEAR_IN_QUOTE_CURRENCY;

/**
 * Shows all existing watchlists of an user and the positions of the first listed watchlist (default),
 * or the positions of the watchlist defined by watchlistid
 *
 */

public class WlWatchlistAnzeigen extends UserHandler {
    private static final SortSupport<Quote> QUOTE_SORT_SUPPORT;

    private static final SortSupport<PriceRecord> PRICE_SORT_SUPPORT;

    private static final String DEFAULT_SORT_BY = "boersenplatz";

    private static Map<String, String> SORT_DMXML_CONVERSIONS = new HashMap<>();

    static {
        SORT_DMXML_CONVERSIONS.put("boersenplatz", "marketVwd");
        SORT_DMXML_CONVERSIONS.put("marketName", "marketName");
        SORT_DMXML_CONVERSIONS.put("kurs", "price");
        SORT_DMXML_CONVERSIONS.put("differenzRelativ", "changePercentPeriod");
        SORT_DMXML_CONVERSIONS.put("tageshoch", "highDay");
        SORT_DMXML_CONVERSIONS.put("tagestief", "lowDay");
        SORT_DMXML_CONVERSIONS.put("volumen", "volume");
        SORT_DMXML_CONVERSIONS.put("name", "name");
        SORT_DMXML_CONVERSIONS.put("isin", "isin");
        SORT_DMXML_CONVERSIONS.put("instrumentType", "instrumentType");
        SORT_DMXML_CONVERSIONS.put("instrumentTypeDescription", "instrumentTypeDescription");
    }


    private static final List<String> SORT_FIELDS;

    private static final List<String> SORT_FIELDS_DMXML;

    static {
        QUOTE_SORT_SUPPORT = SortSupport.createBuilder("boersenplatz", QuoteComparator.BY_VWDFEED_MARKET)
                .add("isin", QuoteComparator.BY_ISIN)
                .add("wkn", QuoteComparator.BY_WKN)
                .add("instrumentType", SortSupport.INSTRUMENT_TYPE_THEN_NAME)
                .add("instrumentTypeDescription", SortSupport.INSTRUMENT_TYPE_DESC_THEN_NAME)
                .add("name", SortSupport.QUOTE_NAME)
                .add("marketName", SortSupport.MARKETNAME_THEN_NAME)
                .build();

        PRICE_SORT_SUPPORT = SortSupport.createBuilder("kurs", PriceRecordComparator.BY_PRICE)
                .add("differenzRelativ", PriceRecordComparator.BY_CHANGE_PERCENT)
                .add("tageshoch", PriceRecordComparator.BY_HIGH_DAY)
                .add("tagestief", PriceRecordComparator.BY_LOW_DAY)
                .add("volumen", PriceRecordComparator.BY_VOLUME_DAY)
                .build();

        SORT_FIELDS = SortSupport.getSortFields(PRICE_SORT_SUPPORT, QUOTE_SORT_SUPPORT);

        SORT_FIELDS_DMXML = Collections.unmodifiableList(new ArrayList<>(SORT_DMXML_CONVERSIONS.values()));
    }


    private static final String VIEW = "wlwatchlistanzeigen";

    private EasytradeInstrumentProvider instrumentProvider;

    protected IntradayProvider intradayProvider;

    private HighLowProvider highLowProvider;

    private HistoricRatiosProvider historicRatiosProvider;

    private FundDataProvider fundDataProvider;

    private ProfiledIndexCompositionProvider indexCompositionProvider;

    private StockDataProvider stockDataProvider;

    private IsoCurrencyConversionProvider currencyConverter;

    public void setHighLowProvider(HighLowProvider highLowProvider) {
        this.highLowProvider = highLowProvider;
    }

    public void setIntradayProvider(IntradayProvider intradayProvider) {
        this.intradayProvider = intradayProvider;
    }

    public void setInstrumentProvider(EasytradeInstrumentProvider instrumentProvider) {
        this.instrumentProvider = instrumentProvider;
    }

    public void setHistoricRatiosProvider(HistoricRatiosProvider historicRatiosProvider) {
        this.historicRatiosProvider = historicRatiosProvider;
    }

    public void setFundDataProvider(FundDataProvider fundDataProvider) {
        this.fundDataProvider = fundDataProvider;
    }

    public void setIndexCompositionProvider(ProfiledIndexCompositionProvider indexCompositionProvider) {
        this.indexCompositionProvider = indexCompositionProvider;
    }

    public void setStockDataProvider(StockDataProvider stockDataProvider) {
        this.stockDataProvider = stockDataProvider;
    }

    public void setCurrencyConversionProvider(IsoCurrencyConversionProvider currencyConversionProvider) {
        this.currencyConverter = currencyConversionProvider;
    }

    @SuppressWarnings("UnusedDeclaration")
    public static final class Command extends ListCommandWithOptionalPaging implements UserCommand {
        private String userid;

        private Long watchlistid;

        private String watchlistName;

        private Long companyid;

        private String[] period;

        private Period aggregation;

        private boolean extendedPriceData = false;

        @de.marketmaker.istar.merger.web.easytrade.Period
        public String[] getPeriod() {
            return ArraysUtil.copyOf(this.period);
        }

        public void setPeriod(String[] period) {
            this.period = ArraysUtil.copyOf(period);
        }

        public Period getAggregation() {
            return aggregation;
        }

        public void setAggregation(Period aggregation) {
            this.aggregation = aggregation;
        }

        @NotNull
        public Long getCompanyid() {
            return companyid;
        }

        public void setCompanyid(Long companyid) {
            this.companyid = companyid;
        }

        @NotNull
        public String getUserid() {
            return userid;
        }

        public void setUserid(String userid) {
            this.userid = userid;
        }

        public Long getWatchlistid() {
            return watchlistid;
        }

        public void setWatchlistid(Long watchlistid) {
            this.watchlistid = watchlistid;
        }

        @MmInternal
        public String getWatchlistName() {
            return watchlistName;
        }

        public void setWatchlistName(String watchlistName) {
            this.watchlistName = watchlistName;
        }

        /**
         * If <code>true</code> return {@link de.marketmaker.iview.dmxml.PriceDataExtended} for each element.
         */
        public boolean isExtendedPriceData() {
            return extendedPriceData;
        }

        public void setExtendedPriceData(boolean extendedPriceData) {
            this.extendedPriceData = extendedPriceData;
        }
    }

    public WlWatchlistAnzeigen() {
        super(Command.class);
    }


    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response,
            Object o, BindException errors) {
        final Command cmd = (Command) o;

        final UserContext userContext = getUserContext(cmd);

        final User user = userContext.getUser();
        final Long wlid = getWatchlistId(user, cmd.getWatchlistName(), cmd.getWatchlistid());

        checkSortBy(cmd);

        final Map<String, Object> model = new HashMap<>();
        model.put("extendedPriceData", cmd.isExtendedPriceData());

        final List<Portfolio> watchlists = user.getWatchlists();
        model.put("watchlists", watchlists);
        if (watchlists.isEmpty()) {
            model.put("positions", Collections.emptyList());
            model.put("listinfo", ListResult.create(cmd, SORT_FIELDS, DEFAULT_SORT_BY, 0));
            model.put("dmxmlsortedby", SORT_DMXML_CONVERSIONS.get(cmd.getSortBy()));
            model.put("dmxmlfields", SORT_FIELDS_DMXML);
            return new ModelAndView(VIEW, model);
        }

        model.put("watchlists", watchlists);

        final Portfolio watchlist;
        if (wlid == null) {
            watchlist = watchlists.get(0);
        } else {
            watchlist = user.getWatchlist(wlid);
            if (watchlist == null) {
                errors.rejectValue("watchlistid", NoSuchPortfolioException.USER_PORTFOLIOID_INVALID,
                        new Object[]{wlid}, "Invalid watchlistid: " + wlid);
                return null;
            }
        }
        final List<PortfolioPosition> positions = new ArrayList<>(watchlist.getPositions());
        model.put("positions", positions);
        if (positions.isEmpty()) {
            model.put("positions", Collections.emptyList());
            model.put("watchlist", watchlist);
            model.put("listinfo", ListResult.create(cmd, SORT_FIELDS, DEFAULT_SORT_BY, 0));
            model.put("dmxmlsortedby", SORT_DMXML_CONVERSIONS.get(cmd.getSortBy()));
            model.put("dmxmlfields", SORT_FIELDS_DMXML);
            return new ModelAndView(VIEW, model);
        }

        final List<Quote> quotes = getQuotes(positions);
        model.put("quotes", quotes);

        final List<PriceRecord> priceRecords = this.intradayProvider.getPriceRecords(quotes);
        model.put("prices", priceRecords);

        final List<HighLow> highLows = this.highLowProvider.getHighLows52W(quotes, priceRecords);
        model.put("highLows", highLows);

        final List<List<BasicHistoricRatios>> ratios = new ArrayList<>(quotes.size());
        if (cmd.getPeriod() != null) {
            for (int i = 0; i < quotes.size(); i++) {
                final Quote quote = quotes.get(i);
                final PriceRecord pr = priceRecords.get(i);

                final MscBasicRatiosMethod method = new MscBasicRatiosMethod(quote, pr,
                        cmd.getPeriod(), cmd.getAggregation(),
                        this.instrumentProvider, this.indexCompositionProvider, this.fundDataProvider,
                        this.intradayProvider, this.historicRatiosProvider);

                final List<BasicHistoricRatios> qr = method.invoke();
                ratios.add(qr);
            }
            model.put("intervals", MscBasicRatiosMethod.getOutputPeriods(cmd.getPeriod()));
        } else {
            ratios.addAll(Collections.nCopies(quotes.size(), Collections.<BasicHistoricRatios>emptyList()));
            model.put("intervals", Collections.emptyList());
        }
        model.put("ratios", ratios);

        final ListResult listResult = ListResult.create(cmd, SORT_FIELDS, DEFAULT_SORT_BY, quotes.size());

        final String sortedBy = listResult.getSortedBy();
        
        if (!QUOTE_SORT_SUPPORT.apply(sortedBy, cmd, quotes, priceRecords, positions, highLows, ratios)) {
            PRICE_SORT_SUPPORT.apply(sortedBy, cmd, priceRecords, quotes, positions, highLows, ratios);
        }

        listResult.setCount(quotes.size());

        // get static data to calculate dividends
        if (cmd.isExtendedPriceData() && stockDataProvider!= null && currencyConverter != null) {
            List<Long> iids = quotes.stream().map(q -> q.getInstrument().getId()).collect(Collectors.toList());
            List<MasterDataStock> data = stockDataProvider.getMasterData(iids,
                RequestContextHolder.getRequestContext().getProfile()); // returns NullMasterDataStock for non STKs

            final int items = data.size();
            List<BigDecimal> divCurrentYear = new ArrayList<>(items);
            List<BigDecimal> divLastYear = new ArrayList<>(items);
            for (int i = 0; i < items; i++) {
                divCurrentYear.add(DIV_IN_QUOTE_CURRENCY.calculate(currencyConverter, quotes.get(i), data.get(i)));
                divLastYear.add(DIV_LAST_YEAR_IN_QUOTE_CURRENCY.calculate(currencyConverter, quotes.get(i), data.get(i)));
            }
            model.put("divCurrentYear", divCurrentYear);
            model.put("divLastYear", divLastYear);
        }

        model.put("notes", watchlist.getNotes());
        model.put("watchlist", watchlist);
        model.put("listinfo", listResult);
        model.put("dmxmlsortedby", SORT_DMXML_CONVERSIONS.get(sortedBy));
        model.put("dmxmlfields", SORT_FIELDS_DMXML);

        return new ModelAndView(VIEW, model);
    }

    private List<Quote> getQuotes(List<PortfolioPosition> positions) {
        final List<Long> qids = new ArrayList<>(positions.size());
        for (final PortfolioPosition position : positions) {
            qids.add(position.getQid());
        }

        final List<Quote> quotes = this.instrumentProvider.identifyQuotes(qids);

        CollectionUtils.removeNulls(quotes, positions);
        return quotes;
    }

    /**
     * helper for converting possibly english sorting column names (from dmxml) to postbank slang german column names.
     */
    private void checkSortBy(Command cmd) {
        if (SORT_DMXML_CONVERSIONS.containsKey(cmd.getSortBy())) {
            return;
        }

        for (final Map.Entry<String, String> entry : SORT_DMXML_CONVERSIONS.entrySet()) {
            if (entry.getValue().equals(cmd.getSortBy())) {
                cmd.setSortBy(entry.getKey());
                return;
            }
        }
    }
}