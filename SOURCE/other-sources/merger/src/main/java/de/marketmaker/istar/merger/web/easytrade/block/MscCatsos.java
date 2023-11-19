/*
 * StkStammdaten.java
 *
 * Created on 13.07.2006 07:06:22
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.block;

import de.marketmaker.istar.common.validator.NotNull;
import de.marketmaker.istar.common.validator.Size;
import de.marketmaker.istar.domain.instrument.Instrument;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.merger.provider.UnknownSymbolException;
import de.marketmaker.istar.merger.web.HttpRequestUtil;

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
public class MscCatsos extends EasytradeCommandController {
    protected EasytradeInstrumentProvider instrumentProvider;

    public MscCatsos() {
        super(Command.class);
    }

    public void setInstrumentProvider(EasytradeInstrumentProvider instrumentProvider) {
        this.instrumentProvider = instrumentProvider;
    }

    public static class Command {
        private String[] symbol;

        /**
         * @sample 710000
         */
        @NotNull
        @Size(min = 1, max = 100)
        public String[] getSymbol() {
            return symbol;
        }

        public void setSymbol(String[] symbol) {
            this.symbol = HttpRequestUtil.filterParametersWithText(symbol);
        }
    }

    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response,
                                    Object o, BindException errors) {
        final Command cmd = (Command) o;

        final List<Instrument> instruments = new ArrayList<>(cmd.getSymbol().length);
        for (String symbol : cmd.getSymbol()) {
            try {
                instruments.add(this.instrumentProvider.identifyByIsinOrWkn(symbol));
            } catch (UnknownSymbolException e) {
                if (this.logger.isDebugEnabled()) {
                    this.logger.debug("<doHandle> unknown symbol: " + symbol);
                }
            }
        }

        final List<Quote> quotes = new ArrayList<>(instruments.size());

        for (final Instrument instrument : instruments) {
            quotes.add(this.instrumentProvider.getQuote(instrument, null, "market:FFM"));
        }
        final Map<String, Object> model = new HashMap<>();
        model.put("quotes", quotes);
        return new ModelAndView("msccatsos", model);
    }
}