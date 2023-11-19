/*
 * AbstractDerivativeKursdaten.java
 *
 * Created on 13.07.2006 07:06:22
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.block;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import de.marketmaker.istar.domain.data.HighLow;
import de.marketmaker.istar.domain.data.PriceRecord;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.domainimpl.data.NullPriceRecord;
import de.marketmaker.istar.merger.provider.HighLowProvider;
import de.marketmaker.istar.merger.provider.IntradayProvider;
import de.marketmaker.istar.merger.web.easytrade.DefaultSymbolCommand;
import de.marketmaker.istar.merger.web.easytrade.SymbolCommand;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public abstract class AbstractDerivativeKursdaten extends EasytradeCommandController {
    protected IntradayProvider intradayProvider;

    protected EasytradeInstrumentProvider instrumentProvider;

    protected AbstractDerivativeKursdaten() {
        super(DefaultSymbolCommand.class);
    }

    public void setIntradayProvider(IntradayProvider intradayProvider) {
        this.intradayProvider = intradayProvider;
    }

    public void setInstrumentProvider(EasytradeInstrumentProvider instrumentProvider) {
        this.instrumentProvider = instrumentProvider;
    }

    private HighLowProvider highLowProvider;

    public void setHighLowProvider(HighLowProvider highLowProvider) {
        this.highLowProvider = highLowProvider;
    }

    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response,
                                    Object o, BindException errors) {
        final Quote quote = this.instrumentProvider.getQuote((SymbolCommand) o);

        final Quote benchmarkQuote = this.instrumentProvider.getUnderlyingQuote(quote.getInstrument(), null);

        final List<Quote> priceQuotes = new ArrayList<>(2);
        priceQuotes.add(quote);
        if (benchmarkQuote != null) {
            priceQuotes.add(benchmarkQuote);
        }
        final List<PriceRecord> priceRecords = this.intradayProvider.getPriceRecords(priceQuotes);
        final PriceRecord price = priceRecords.get(0);
        final PriceRecord priceBenchmark = benchmarkQuote != null ? priceRecords.get(1) : NullPriceRecord.INSTANCE;

        final HighLow highLow = this.highLowProvider.getHighLow52W(quote, price);

        final Map<String, Object> model = new HashMap<>();
        model.put("quote", quote);
        model.put("price", price);
        model.put("benchmarkPrice", priceBenchmark);
        model.put("highLow", highLow);
        return new ModelAndView(getTemplateName(), model);
    }

    protected abstract String getTemplateName();
}
