/*
 * CerKursdatenliste.java
 *
 * Created on 13.07.2006 07:06:22
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.block;

import de.marketmaker.istar.domain.data.HighLow;
import de.marketmaker.istar.domain.data.PriceRecord;
import de.marketmaker.istar.domain.data.PriceRecordComparator;
import de.marketmaker.istar.domain.instrument.Instrument;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.domain.instrument.QuoteComparator;
import de.marketmaker.istar.domain.profile.Profile;
import de.marketmaker.istar.merger.web.easytrade.ProfiledInstrument;
import de.marketmaker.istar.merger.context.RequestContextHolder;
import de.marketmaker.istar.merger.provider.HighLowProvider;
import de.marketmaker.istar.merger.provider.IntradayProvider;
import de.marketmaker.istar.merger.web.easytrade.ListResult;
import de.marketmaker.istar.merger.web.easytrade.SymbolListCommand;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public abstract class AbstractDerivativeKursdatenliste extends EasytradeCommandController {
    private static final SortSupport<Quote> QUOTE_SORT_SUPPORT;

    private static final SortSupport<PriceRecord> PRICE_SORT_SUPPORT;

    private static final String DEFAULT_SORT_BY = "boersenplatz";
    
    private static final List<String> SORT_FIELDS;

    static {
        QUOTE_SORT_SUPPORT = SortSupport.createBuilder(
                "boersenplatz", QuoteComparator.BY_VWDFEED_MARKET)
                .build();

        PRICE_SORT_SUPPORT = SortSupport.createBuilder("kurs", PriceRecordComparator.BY_PRICE)
                .add("differenzRelativ", PriceRecordComparator.BY_CHANGE_PERCENT)
                .add("tageshoch", PriceRecordComparator.BY_HIGH_DAY)
                .add("tagestief", PriceRecordComparator.BY_LOW_DAY)
                .add("kursfeststellungen", PriceRecordComparator.BY_NUMBER_OF_TRADES)
                .add("volumen", PriceRecordComparator.BY_VOLUME_DAY)
                .build();

        SORT_FIELDS = SortSupport.getSortFields(QUOTE_SORT_SUPPORT, PRICE_SORT_SUPPORT);
    }

    protected IntradayProvider intradayProvider;

    protected EasytradeInstrumentProvider instrumentProvider;

    private HighLowProvider highLowProvider;

    protected AbstractDerivativeKursdatenliste() {
        super(SymbolListCommand.class);
    }

    public void setHighLowProvider(HighLowProvider highLowProvider) {
        this.highLowProvider = highLowProvider;
    }

    public void setIntradayProvider(IntradayProvider intradayProvider) {
        this.intradayProvider = intradayProvider;
    }

    public void setInstrumentProvider(EasytradeInstrumentProvider instrumentProvider) {
        this.instrumentProvider = instrumentProvider;
    }

    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response,
            Object o, BindException errors) {
        final SymbolListCommand cmd = (SymbolListCommand) o;

        final Instrument instrument = this.instrumentProvider.getInstrument(cmd);

        final Profile profile = RequestContextHolder.getRequestContext().getProfile();
        final List<Quote> quotes = ProfiledInstrument.quotesWithPrices(instrument, profile);

        final ListResult listResult =
                ListResult.create(cmd, SORT_FIELDS, DEFAULT_SORT_BY, quotes.size());

        final boolean sorted = QUOTE_SORT_SUPPORT.apply(listResult.getSortedBy(), cmd, quotes);

        final Quote benchmarkQuote = this.instrumentProvider.getUnderlyingQuote(instrument, null);

        final ArrayList<Quote> priceQuotes = new ArrayList<>(quotes);
        if (benchmarkQuote != null) {
            priceQuotes.add(benchmarkQuote);
        }

        final List<PriceRecord> priceRecords = this.intradayProvider.getPriceRecords(priceQuotes);
        final PriceRecord priceBenchmark = (benchmarkQuote != null)
                ? priceRecords.remove(priceRecords.size() - 1) : null;

        if (!sorted) {
            PRICE_SORT_SUPPORT.apply(listResult.getSortedBy(), cmd, priceRecords, quotes);
        }

        listResult.setCount(quotes.size());

        final List<HighLow> highLows = this.highLowProvider.getHighLows52W(quotes, priceRecords);

        final Map<String, Object> model = new HashMap<>();
        model.put("quotes", quotes);
        model.put("prices", priceRecords);
        model.put("benchmarkPrice", priceBenchmark);
        model.put("highLows", highLows);
        model.put("listinfo", listResult);
        return new ModelAndView(getTemplateName(), model);
    }

    protected abstract String getTemplateName();
}
