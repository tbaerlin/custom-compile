/*
 * KwtSmartclient.java
 *
 * Created on 07.04.2009 12:12:37
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.misc;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;

import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.merger.provider.UnknownSymbolException;
import de.marketmaker.istar.merger.web.easytrade.DefaultSymbolCommand;
import de.marketmaker.istar.merger.web.easytrade.SymbolCommand;
import de.marketmaker.istar.merger.web.easytrade.block.EasytradeInstrumentProvider;

import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
@Controller
public class IqsSymbolLookup {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private EasytradeInstrumentProvider instrumentProvider;

    public void setInstrumentProvider(EasytradeInstrumentProvider instrumentProvider) {
        this.instrumentProvider = instrumentProvider;
    }

    @RequestMapping(value = "/marketmanager/lookup.txt")
    protected void handle(HttpServletResponse response, DefaultSymbolCommand cmd)
            throws Exception {
        if (!StringUtils.hasText(cmd.getSymbol())) {
            response.sendError(SC_BAD_REQUEST, "symbol parameter missing");
            return;
        }

        final Quote quote = getQuote(cmd);

        final String vwdcode = (quote != null) ? quote.getSymbolVwdcode() : null;
        if (vwdcode == null) {
            response.sendError(SC_NOT_FOUND);
        }
        else {
            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType("text/plain;charset=utf-8");
            response.getWriter().print(vwdcode);
        }
    }

    private Quote getQuote(SymbolCommand cmd) {
        try {
            return this.instrumentProvider.getQuote(cmd);
        } catch (UnknownSymbolException e) {
            if (this.logger.isDebugEnabled()) {
                this.logger.debug("<getQuote> no quote for " + cmd);
            }
            return null;
        }
    }
}
