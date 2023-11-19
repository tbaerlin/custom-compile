/*
 * MscQuotes.java
 *
 * Created on 13.07.2006 07:06:22
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.block;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import de.marketmaker.istar.domain.instrument.Instrument;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.domain.profile.Profile;
import de.marketmaker.istar.merger.context.RequestContextHolder;
import de.marketmaker.istar.merger.web.easytrade.ErrorUtils;
import de.marketmaker.istar.merger.web.easytrade.ListCommand;
import de.marketmaker.istar.merger.web.easytrade.ListResult;
import de.marketmaker.istar.merger.web.easytrade.ProfiledInstrument;
import de.marketmaker.istar.merger.web.easytrade.SymbolListCommand;

import static de.marketmaker.istar.domain.instrument.QuoteComparator.BY_MARKETNAME_LBBW;
import static de.marketmaker.istar.domain.instrument.QuoteComparator.BY_VWDFEED_MARKET;

/**
 * Lists an instrument's quotes; only those quotes will be included for which the user has at
 * least the permission to view end-of-day prices.
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class MscQuotes extends EasytradeCommandController {
    private static final SortSupport<Quote> SORT_SUPPORT;

    private static final String DEFAULT_SORT_BY = "default";

    private static final String VOLUME_SORT_BY = "qvsort";

    private static final List<String> SORT_FIELDS;

    static {
        SORT_SUPPORT = SortSupport.createBuilder("default", BY_VWDFEED_MARKET)
                .add("marketVwd", BY_VWDFEED_MARKET)
                .add("marketName", SortSupport.zoneQuoteComparator(BY_MARKETNAME_LBBW))
                .add(VOLUME_SORT_BY, (Comparator<Quote>) null)
                .build();

        SORT_FIELDS = SORT_SUPPORT.getSortNames();
    }

    protected EasytradeInstrumentProvider instrumentProvider;

    public MscQuotes() {
        super(SymbolListCommand.class);
    }

    public void setInstrumentProvider(EasytradeInstrumentProvider instrumentProvider) {
        this.instrumentProvider = instrumentProvider;
    }

    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response,
            Object o, BindException errors) {
        final SymbolListCommand cmd = (SymbolListCommand) o;

        final Quote quote = this.instrumentProvider.getQuote(cmd);

        if (quote == null) {
            ErrorUtils.rejectSymbol(cmd.getSymbol(), errors);
            return null;
        }

        final Instrument instrument = quote.getInstrument();

        final Profile profile = RequestContextHolder.getRequestContext().getProfile();
        final List<Quote> quotes = ProfiledInstrument.quotesWithPrices(instrument, profile);

        final Map<String, Object> model = getModel(cmd, quote, instrument, quotes);
        return new ModelAndView("mscquotes", model);
    }

    protected static Map<String, Object> getModel(ListCommand cmd, Quote quote,
            Instrument instrument, List<Quote> quotes) {
        final ListResult listResult
                = ListResult.create(cmd, SORT_FIELDS, DEFAULT_SORT_BY, quotes.size());

        SORT_SUPPORT.apply(listResult.getSortedBy(), cmd, quotes);
        if (VOLUME_SORT_BY.equals(listResult.getSortedBy()) && listResult.isAscending()) {
            Collections.reverse(quotes);
        }

        listResult.setCount(quotes.size());

        final Map<String, Object> model = new HashMap<>();
        model.put("instrument", instrument);
        model.put("listinfo", listResult);
        model.put("quote", quote);
        model.put("quotes", quotes);
        return model;
    }
}
