/*
 * SymbolResolverInterceptor.java
 *
 * Created on 24.01.14 08:36
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.merger.provider.UnknownSymbolException;
import de.marketmaker.istar.merger.web.easytrade.block.EasytradeInstrumentProvider;

/**
 * @author oflege
 */
public class SymbolResolverInterceptor extends HandlerInterceptorAdapter {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private EasytradeInstrumentProvider instrumentProvider;

    public void setInstrumentProvider(EasytradeInstrumentProvider instrumentProvider) {
        this.instrumentProvider = instrumentProvider;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
            Object handler) throws Exception {

        String symbolParameterName = getSymbolParameterName(request);
        String symbol = request.getParameter(symbolParameterName);
        if (symbol == null) {
            this.logger.error("<preHandle> no parameter '" + symbolParameterName + "' for "
                    + HttpRequestUtil.toString(request));
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "missing parameter " + symbolParameterName);
            return false;
        }

        final Quote quote = identifyQuote(symbol);
        if (quote != null) {
            Map<String, String[]> m = request.getParameterMap();
            m.put("_quote_id", new String[]{Long.toString(quote.getId())});
            m.put("_instrument_id", new String[]{Long.toString(quote.getInstrument().getId())});
            m.put("_instrument_type", new String[]{quote.getInstrument().getInstrumentType().name()});
        }

        return true;
    }

    private Quote identifyQuote(String symbol) {
        try {
            return this.instrumentProvider.identifyQuote(symbol, null, null);
        } catch (UnknownSymbolException e) {
            this.logger.warn("<preHandle> no quote for '" + symbol + "'");
            return null;
        }
    }

    private String getSymbolParameterName(HttpServletRequest request) {
        final String result = request.getParameter("symbolParameterName");
        return (result != null) ? result : "symbol";
    }

}
