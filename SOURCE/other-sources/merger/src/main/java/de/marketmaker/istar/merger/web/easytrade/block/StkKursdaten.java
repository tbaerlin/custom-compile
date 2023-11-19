/*
 * StkKursdaten.java
 *
 * Created on 07.07.2006 10:28:22
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.block;

import java.util.Arrays;
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
import de.marketmaker.istar.merger.provider.HighLowProvider;
import de.marketmaker.istar.merger.provider.IntradayProvider;
import de.marketmaker.istar.merger.web.easytrade.DefaultSymbolCommand;
import de.marketmaker.istar.merger.web.easytrade.SymbolCommand;

/**
 * Returns a quotation of current prices and bids for a financial instrument symbol.
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
@Deprecated
public class StkKursdaten extends EasytradeCommandController {

    protected IntradayProvider intradayProvider;

    protected EasytradeInstrumentProvider instrumentProvider;

    private HighLowProvider highLowProvider;

    public void setIntradayProvider(IntradayProvider intradayProvider) {
        this.intradayProvider = intradayProvider;
    }

    public void setInstrumentProvider(EasytradeInstrumentProvider instrumentProvider) {
        this.instrumentProvider = instrumentProvider;
    }

    public void setHighLowProvider(HighLowProvider highLowProvider) {
        this.highLowProvider = highLowProvider;
    }

    public StkKursdaten() {
        super(DefaultSymbolCommand.class);
    }

    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response,
            Object o, BindException errors) {

        final SymbolCommand cmd = (SymbolCommand) o;
        final Quote quote = this.instrumentProvider.getQuote(cmd);
        final List<Quote> quotes = Arrays.asList(quote);

        final List<PriceRecord> priceRecords = this.intradayProvider.getPriceRecords(quotes);
        final PriceRecord price = priceRecords.get(0);

        final HighLow highLow = this.highLowProvider.getHighLow52W(quote, price);

        final Map<String, Object> model = new HashMap<>();
        model.put("quote", quote);
        model.put("price", price);
        model.put("highLow", highLow);

        return new ModelAndView("stkkursdaten", model);
    }
}
