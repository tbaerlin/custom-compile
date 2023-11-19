/*
 * StkTopFlop.java
 *
 * Created on 13.07.2006 07:06:22
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.block;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import de.marketmaker.istar.common.util.CollectionUtils;
import de.marketmaker.istar.common.util.DateUtil;
import de.marketmaker.istar.common.validator.RestrictedSet;
import de.marketmaker.istar.domain.data.PriceRecord;
import de.marketmaker.istar.domain.data.PriceRecordComparator;
import de.marketmaker.istar.domain.data.PriceRecordFund;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.domainimpl.data.IndexComposition;
import de.marketmaker.istar.instrument.IndexCompositionResponse;
import de.marketmaker.istar.merger.provider.ProfiledIndexCompositionProvider;
import de.marketmaker.istar.merger.provider.IntradayProvider;
import de.marketmaker.istar.merger.provider.UnknownSymbolException;
import de.marketmaker.istar.merger.web.HttpRequestUtil;
import de.marketmaker.istar.merger.web.easytrade.MultiListSorter;
import de.marketmaker.istar.merger.web.easytrade.SymbolStrategyEnum;

/**
 * Returns the first (top) and last (flop) quotes in a given set of quotes that are
 * ordered by a given sortfield.
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class StkTopFlop extends EasytradeCommandController {

    protected IntradayProvider intradayProvider;

    protected ProfiledIndexCompositionProvider indexCompositionProvider;

    protected EasytradeInstrumentProvider instrumentProvider;

    public void setIntradayProvider(IntradayProvider intradayProvider) {
        this.intradayProvider = intradayProvider;
    }

    public void setIndexCompositionProvider(ProfiledIndexCompositionProvider indexCompositionProvider) {
        this.indexCompositionProvider = indexCompositionProvider;
    }

    public void setInstrumentProvider(EasytradeInstrumentProvider instrumentProvider) {
        this.instrumentProvider = instrumentProvider;
    }

    public static final class Command {
        private String[] symbol;

        private SymbolStrategyEnum symbolStrategy;

        private String listid;

        private String sortfield;

        private int countTop = 5;

        private int countFlop = 5;

        private String marketStrategy;

        private boolean excludeOld = false;

        /**
         * Symbols of the quotes that should be examined
         * <b>Ignored</b> if listid is defined
         */
        public String[] getSymbol() {
            return symbol;
        }

        public void setSymbol(String[] symbol) {
            this.symbol = HttpRequestUtil.filterParametersWithText(symbol);
        }

        /**
         * @return number of worst performing quotes to be returned
         */
        public int getCountFlop() {
            return countFlop;
        }

        public void setCountFlop(int countFlop) {
            this.countFlop = countFlop;
        }

        /**
         * @return number of best performing quotes to be returned
         */
        public int getCountTop() {
            return countTop;
        }

        public void setCountTop(int countTop) {
            this.countTop = countTop;
        }

        /**
         * @return type of the values in <tt>symbol</tt>
         */
        public SymbolStrategyEnum getSymbolStrategy() {
            return symbolStrategy;
        }

        public void setSymbolStrategy(SymbolStrategyEnum symbolStrategy) {
            this.symbolStrategy = symbolStrategy;
        }

        /**
         * @return id of a list that defines the quotes to be considered
         */
        public String getListid() {
            return listid;
        }

        public void setListid(String listid) {
            this.listid = listid;
        }

        /**
         * If <tt>symbol</tt> contains instrument-specific symbols, this strategy is used to
         * infer the appropiates quotes. If undefined, a customer specific default strategy is used.
         */
        public String getMarketStrategy() {
            return marketStrategy;
        }

        public void setMarketStrategy(String marketStrategy) {
            this.marketStrategy = marketStrategy;
        }

        /**
         * @return override sort field for quote order; should only be defined to override the
         *         default order, which is based on the change of the current price wrt. the previous
         *         close in percent.
         */
        @RestrictedSet("volume,numberOfTrades")
        public String getSortfield() {
            return sortfield;
        }

        public void setSortfield(String sortfield) {
            this.sortfield = sortfield;
        }

        /**
         * @return whether to consider only quotes with prices on the most
         *         recent day for which any of those quotes has a price.
         */
        public boolean isExcludeOld() {
            return excludeOld;
        }

        public void setExcludeOld(boolean excludeOld) {
            this.excludeOld = excludeOld;
        }
    }

    public StkTopFlop() {
        super(Command.class);
    }

    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response,
            Object o, BindException errors) {
        final Command cmd = (Command) o;

        final Map<String, Object> model = new HashMap<>();

        List<Quote> quotes = getQuotes(cmd, model);
        CollectionUtils.removeNulls(quotes);

        List<PriceRecord> prices = this.intradayProvider.getPriceRecords(quotes);

        if (cmd.isExcludeOld()) {
            final boolean[] allOnSameDay = new boolean[1];
            LocalDate day = getMostRecentPriceDay(prices, allOnSameDay);
            if (!allOnSameDay[0]) {
                Iterator<PriceRecord> pit = prices.iterator();
                Iterator<Quote> qit = quotes.iterator();
                while (pit.hasNext()) {
                    final PriceRecord pr = pit.next();
                    qit.next();
                    if (!Objects.equals(day, getPriceDay(pr))) {
                        pit.remove();
                        qit.remove();
                    }
                }
            }
        }

        final Comparator c = getComparator(cmd);
        new MultiListSorter(c, true).sort(prices, quotes);

        final int countTop = Math.min(cmd.getCountTop(), quotes.size());
        final int countFlop = Math.min(cmd.getCountFlop(), quotes.size());

        final List<Quote> tfQuotes = getTopFlop(quotes, countTop, countFlop);
        final List<PriceRecord> tfPrices = getTopFlop(prices, countTop, countFlop);

        final List<Boolean> fundPrices = new ArrayList<>(prices.size());
        for (final PriceRecord price : tfPrices) {
            fundPrices.add(price instanceof PriceRecordFund);
        }

        final int[] numDownUnchUp = getNumDownUnchUp(prices);

        model.put("fundPrices", fundPrices);
        model.put("quotes", tfQuotes);
        model.put("prices", tfPrices);
        model.put("numDown", numDownUnchUp[0]);
        model.put("numUnchanged", numDownUnchUp[1]);
        model.put("numUp", numDownUnchUp[2]);
        return new ModelAndView("stktopflop", model);
    }

    private LocalDate getPriceDay(PriceRecord pr) {
        if (pr == null || pr.getPrice() == null) {
            return null;
        }
        final DateTime date = pr.getPrice().getDate();
        return (date != null) ? date.toLocalDate() : null;
    }

    private LocalDate getMostRecentPriceDay(List<PriceRecord> prices, boolean[] allOnSameDay) {
        allOnSameDay[0] = true;
        LocalDate result = null;
        for (PriceRecord pr : prices) {
            LocalDate ld = getPriceDay(pr);
            if (ld == null) {
                allOnSameDay[0] |= (result != null);
            }
            else if (result == null) {
                result = ld;
            }
            else if (!ld.equals(result)) {
                result = DateUtil.max(ld, result);
                allOnSameDay[0] = false;
            }
        }
        return result;
    }

    private <V> List<V> getTopFlop(List<V> source, int countTop, int countFlop) {
        final ArrayList<V> result = new ArrayList<>(countTop + countFlop);
        result.addAll(source.subList(0, countTop));
        result.addAll(source.subList(source.size() - countFlop, source.size()));
        return result;
    }

    private List<Quote> getQuotes(Command cmd, Map<String, Object> model) {
        if (cmd.getListid() != null) {
            return getByListid(cmd, model);
        }
        else if (cmd.getSymbol() != null) {
            return this.instrumentProvider.identifyQuotes(Arrays.asList(cmd.getSymbol()),
                    cmd.getSymbolStrategy(), null, cmd.getMarketStrategy());
        }
        return Collections.emptyList();
    }

    private List<Quote> getByListid(Command cmd, Map<String, Object> model) {
        final List<Quote> defQuotes = getDefQuotes(cmd, model);
        if (!StringUtils.hasText(cmd.getMarketStrategy())) {
            return defQuotes;
        }
        final List<Quote> result = new ArrayList<>(defQuotes.size());
        for (Quote quote : defQuotes) {
            result.add(this.instrumentProvider.getQuote(quote.getInstrument(), null, cmd.getMarketStrategy()));
        }
        return result;
    }

    private List<Quote> getDefQuotes(Command cmd, Map<String, Object> model) {
        try {
            final Quote quote = this.instrumentProvider.identifyQuote(cmd.getListid(), cmd.getSymbolStrategy(), null, null);
            model.put("indexQuote", quote);
            return this.instrumentProvider.getIndexQuotes(quote.getId() + ".qid");
        } catch (UnknownSymbolException e) {
            IndexCompositionResponse response
                    = this.indexCompositionProvider.getIndexCompositionByName(cmd.getListid());
            if (!response.isValid()) {
                throw e;
            }
            final IndexComposition source = response.getIndexComposition();
            return this.instrumentProvider.identifyQuotes(source.getQids());
        }
    }

    private int[] getNumDownUnchUp(List<PriceRecord> prices) {
        final int[] result = new int[3];
        for (final PriceRecord price : prices) {
            if (price == null || price.getChangeNet() == null) {
                result[1]++;
            }
            else {
                result[price.getChangeNet().signum() + 1]++;
            }
        }
        return result;
    }

    private Comparator getComparator(Command cmd) {
        if ("volume".equals(cmd.getSortfield())) {
            return PriceRecordComparator.BY_VOLUME_DAY;
        }
        if ("numberOfTrades".equals(cmd.getSortfield())) {
            return PriceRecordComparator.BY_NUMBER_OF_TRADES;
        }
        return PriceRecordComparator.BY_CHANGE_PERCENT;
    }
}
