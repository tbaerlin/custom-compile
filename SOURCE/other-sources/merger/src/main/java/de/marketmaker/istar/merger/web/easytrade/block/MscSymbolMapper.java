/**
 * Created on 19.04.12 14:45
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author Michael Lösch
 *
 */

package de.marketmaker.istar.merger.web.easytrade.block;

import de.marketmaker.istar.common.validator.NotNull;
import de.marketmaker.istar.common.validator.Size;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.merger.web.easytrade.BaseMultiSymbolCommand;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This Block takes one or more symbols (maximum is 100 symbols in one request) and finds
 * the corresponding symbols, defined by the keysystem(s)
 *
 */

public class MscSymbolMapper extends EasytradeCommandController {

    public static class Command extends BaseMultiSymbolCommand {

        private String[] keysystem;

        /**
         * @sample IE0030606259
         */
        @NotNull
        @Size(min = 1, max = 100)
        public String[] getSymbol() {
            return super.getSymbol();
        }


        /**
         * @return defines the keysystem(s) of the resulting symbols
         * @sample WKN
         */
        @NotNull
        public String[] getKeysystem() {
            return this.keysystem;
        }

        public void setKeysystem(String[] keysystem) {
            this.keysystem = keysystem;
        }
    }

    protected EasytradeInstrumentProvider instrumentProvider;

    public void setInstrumentProvider(EasytradeInstrumentProvider instrumentProvider) {
        this.instrumentProvider = instrumentProvider;
    }

    protected MscSymbolMapper() {
        super(Command.class);
    }

    @Override
    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response,
                                    Object o, BindException errors) throws Exception {
        final Command cmd = (Command) o;

        final String[] symbols = cmd.getSymbol();
        final List<Quote> quotes = this.instrumentProvider.identifyQuotes(Arrays.asList(symbols),
                cmd.getSymbolStrategy(), cmd.getMarket(), cmd.getMarketStrategy());

        final ArrayList<Map<String, String>> symbolsMapList = new ArrayList<>();

        final String[] keysystems = cmd.getKeysystem();

        for (Quote quote : quotes) {
            final HashMap<String, String> symbolsMap = new HashMap<>();
            symbolsMapList.add(symbolsMap);
            for (String keysystem : keysystems) {
                if (quote == null) {
                    symbolsMap.put(keysystem, null);
                    continue;
                }
                final String quoteSymbol = quote.getSymbol(keysystem);
                if (!StringUtils.hasText(quoteSymbol)) {
                    symbolsMap.put(keysystem, quote.getInstrument().getSymbol(keysystem));
                } else {
                    symbolsMap.put(keysystem, quoteSymbol);
                }
            }
        }

        final Map<String, Object> model = new HashMap<>();
        model.put("symbols", symbols);
        model.put("symbolsMapList", symbolsMapList);
        model.put("quotes", quotes);
        return new ModelAndView("mscsymbolmapper", model);
    }
}