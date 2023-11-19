/*
 * StkKursdaten.java
 *
 * Created on 07.07.2006 10:28:22
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.block;

import de.marketmaker.istar.domain.data.OrderbookData;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.merger.provider.IntradayProvider;
import de.marketmaker.istar.merger.web.easytrade.DefaultSymbolCommand;
import de.marketmaker.istar.merger.web.easytrade.SymbolCommand;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * Returns the current orderbook for a given quote which contains the <em>n</em> best bids and
 * the <em>m</em> best asks.
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class MscOrderbook extends EasytradeCommandController {

    protected IntradayProvider intradayProvider;

    protected EasytradeInstrumentProvider instrumentProvider;

    public MscOrderbook() {
        super(DefaultSymbolCommand.class);
    }

    public void setIntradayProvider(IntradayProvider intradayProvider) {
        this.intradayProvider = intradayProvider;
    }

    public void setInstrumentProvider(EasytradeInstrumentProvider instrumentProvider) {
        this.instrumentProvider = instrumentProvider;
    }

    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response,
                                    Object o, BindException errors) {

        final SymbolCommand cmd = (SymbolCommand) o;
        final Quote quote = this.instrumentProvider.getQuote(cmd);
        final OrderbookData data = this.intradayProvider.getOrderbook(quote);

        final Map<String, Object> model = new HashMap<>();
        model.put("quote", quote);
        model.put("data", data);
        if (data.getQuote() != null) {
            model.put("vwdcode", data.getQuote().getSymbolVwdcode());            
        }
        return new ModelAndView("mscorderbook", model);
    }
}