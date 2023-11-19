/*
 * MscListConstituents.java
 *
 * Created on 13.07.2006 07:06:22
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.block;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.util.StringUtils;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import de.marketmaker.istar.common.util.CollectionUtils;
import de.marketmaker.istar.common.validator.NotNull;
import de.marketmaker.istar.domain.Language;
import de.marketmaker.istar.domain.data.PriceQuality;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.domain.instrument.QuoteComparator;
import de.marketmaker.istar.domain.instrument.QuoteNameStrategy;
import de.marketmaker.istar.domain.profile.Profile;
import de.marketmaker.istar.domainimpl.data.IndexComposition;
import de.marketmaker.istar.instrument.IndexCompositionResponse;
import de.marketmaker.istar.merger.context.RequestContextHolder;
import de.marketmaker.istar.merger.provider.ProfiledIndexCompositionProvider;
import de.marketmaker.istar.merger.web.easytrade.HasListid;
import de.marketmaker.istar.merger.web.easytrade.HasMarketStrategy;
import de.marketmaker.istar.merger.web.easytrade.HasOnlyEntitledQuotes;
import de.marketmaker.istar.merger.web.easytrade.HasSymbolStrategy;
import de.marketmaker.istar.merger.web.easytrade.ListCommandWithOptionalPaging;
import de.marketmaker.istar.merger.web.easytrade.ListResult;
import de.marketmaker.istar.merger.web.easytrade.SymbolStrategyEnum;

import static de.marketmaker.istar.merger.web.easytrade.block.EasytradeInstrumentProvider.qidSymbol;

/**
 * Return all instruments that a given list or index consists of.
 * The result only contains <tt>instrumentdata</tt> and <tt>quotedata</tt>. There is no price data returned.
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class MscListConstituents extends EasytradeCommandController {
    private static final String DEFAULT_SORT_BY = "name";

    private static final SortSupport<Quote> QUOTE_SORT_SUPPORT;

    static {
        QUOTE_SORT_SUPPORT = SortSupport.createBuilder("name", SortSupport.QUOTE_NAME)
                .add("marketVwd", QuoteComparator.BY_VWDFEED_MARKET)
                .add("currencyIso", QuoteComparator.BY_CURRENCY_ISO)
                .add("isin", QuoteComparator.BY_ISIN)
                .add("wkn", QuoteComparator.BY_WKN)
                .add("none", (Comparator<Quote>) null)
                .build();
    }

    private static final List<String> QID_SORT_FIELDS = SortSupport.getSortFields(QUOTE_SORT_SUPPORT);

    public MscListConstituents() {
        super(Command.class);
    }

    public static class Command extends ListCommandWithOptionalPaging implements HasListid,
            HasSymbolStrategy, HasMarketStrategy, HasOnlyEntitledQuotes {
        private SymbolStrategyEnum symbolStrategy;

        private String listid;

        private String marketStrategy;

        private boolean onlyEntitledQuotes = false;

        @NotNull
        public String getListid() {
            return listid;
        }

        public void setListid(String listid) {
            this.listid = listid;
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
    }

    protected EasytradeInstrumentProvider instrumentProvider;

    private ProfiledIndexCompositionProvider indexCompositionProvider;

    public void setIndexCompositionProvider(ProfiledIndexCompositionProvider indexCompositionProvider) {
        this.indexCompositionProvider = indexCompositionProvider;
    }

    public void setInstrumentProvider(EasytradeInstrumentProvider instrumentProvider) {
        this.instrumentProvider = instrumentProvider;
    }

    static RequestDefinition getDefinition(EasytradeInstrumentProvider ip,
            ProfiledIndexCompositionProvider icp, String listid, SymbolStrategyEnum symbolStrategy,
            String marketStrategy) {
        final String name;
        final List<Quote> defQuotes;
        final List<String> itemnames;

        final QuoteNameStrategy qns
                = RequestContextHolder.getRequestContext().getQuoteNameStrategy();

        if (listid.endsWith(EasytradeInstrumentProvider.QID_SUFFIX)) {
            final Quote indexQuote = ip.identifyQuote(listid, null, null, null);
            defQuotes = ip.getIndexQuotes(qidSymbol(indexQuote.getId()));
            name = qns.getName(indexQuote);
            itemnames = Collections.nCopies(defQuotes.size(), null);
        }
        else if (symbolStrategy != null ||
                listid.endsWith(EasytradeInstrumentProvider.IID_SUFFIX)) {
            final Quote quote = ip.identifyQuote(listid, symbolStrategy, null, null);
            defQuotes = ip.getIndexQuotes(qidSymbol(quote.getId()));
            name = qns.getName(quote);
            itemnames = Collections.nCopies(defQuotes.size(), null);
        }
        else {
            final IndexComposition definition = getIndexComposition(icp, listid);
            final List<Long> quoteids = definition.getQids();

            itemnames = new ArrayList<>(quoteids.size());
            final Language lang = Language.valueOf(RequestContextHolder.getRequestContext().getLocale());
            for (final Long quoteid : quoteids) {
                itemnames.add(definition.getLocalizedName(quoteid, lang));
            }

            defQuotes = ip.identifyQuotes(quoteids);
            name = definition.getName();
        }

        CollectionUtils.removeNulls(defQuotes, itemnames);

        final List<Quote> quotes;

        if (StringUtils.hasText(marketStrategy)) {
            quotes = new ArrayList<>(defQuotes.size());
            for (Quote quote : defQuotes) {
                quotes.add(ip.getQuote(quote.getInstrument(), null, marketStrategy));
            }
        }
        else {
            quotes = defQuotes;
        }

        final List<String> names = new ArrayList<>(itemnames.size());
        for (int i = 0; i < defQuotes.size(); i++) {
            final Quote quote = defQuotes.get(i);
            final String itemname = itemnames.get(i);
            names.add(itemname == null ? qns.getName(quote) : itemname);
        }

        // TODO: use name schema according to customer
        return new RequestDefinition(name, quotes, names);
    }

    private static IndexComposition getIndexComposition(ProfiledIndexCompositionProvider icp, final String listid) {
        IndexCompositionResponse response = icp.getIndexCompositionByName(listid);
        if (!response.isValid()) {
            return IndexComposition.createEmpty(listid);
        }
        return response.getIndexComposition();
    }


    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response,
            Object o, BindException errors) {
        final Command cmd = (Command) o;

        final RequestDefinition definition = getDefinition(this.instrumentProvider, this.indexCompositionProvider, cmd.getListid(), cmd.getSymbolStrategy(), cmd.getMarketStrategy());
        final List<Quote> quotes = definition.getQuotes();
        final List<String> itemnames = definition.getItemnames();

        final Profile profile = RequestContextHolder.getRequestContext().getProfile();
        if (cmd.isOnlyEntitledQuotes()) {
            for (int i = 0; i < quotes.size(); i++) {
                final Quote quote = quotes.get(i);
                if (profile.getPriceQuality(quote) == PriceQuality.NONE) {
                    quotes.set(i, null);
                    itemnames.set(i, null);
                }
            }
            CollectionUtils.removeNulls(quotes);
            CollectionUtils.removeNulls(itemnames);
        }

        final ListResult listResult = ListResult.create(cmd, QID_SORT_FIELDS, DEFAULT_SORT_BY, quotes.size());

        QUOTE_SORT_SUPPORT.apply(listResult.getSortedBy(), cmd, quotes, itemnames);

        listResult.setCount(quotes.size());

        final Map<String, Object> model = new HashMap<>();
        model.put("quotes", quotes);
        model.put("itemnames", itemnames);
        model.put("listinfo", listResult);
        model.put("listname", definition.getListname());
        return new ModelAndView("msclistconstituents", model);
    }

    static class RequestDefinition {
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
}
