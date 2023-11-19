/*
 * KwtSmartclient.java
 *
 * Created on 07.04.2009 12:12:37
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.misc;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.merger.web.easytrade.DefaultSymbolCommand;
import de.marketmaker.istar.merger.web.easytrade.block.EasytradeInstrumentProvider;

import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
@Controller
public class QuoteRedirect {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private EasytradeInstrumentProvider instrumentProvider;

    private final Matcher matcher = Pattern.compile("/dmxml-1/(.*)/").matcher("");

    public void setInstrumentProvider(EasytradeInstrumentProvider instrumentProvider) {
        this.instrumentProvider = instrumentProvider;
    }

    @RequestMapping(value = "**/quote.html")
    protected void handle(HttpServletRequest request,
            HttpServletResponse response, DefaultSymbolCommand cmd) throws Exception {
        final Quote quote;
        try {
            quote = this.instrumentProvider.getQuote(cmd);
        }
        catch (Exception exc) {
            this.logger.warn("<handle> unknown quote: " + cmd.getSymbol());
            response.sendError(SC_NOT_FOUND);
            return;
        }

        this.matcher.reset(request.getRequestURI()).find();

        response.sendRedirect("/" + this.matcher.group(1) + "/portrait.html#P_"
                + quote.getInstrument().getInstrumentType().name() + "/" + quote.getId() + ".qid");
    }

//    private void writeError(HttpServletResponse response, String message) throws IOException {
//        response.setContentType("text/html;charset=UTF-8");
//        response.setStatus(HttpServletResponse.SC_OK);
//        final OutputStreamWriter writer = new OutputStreamWriter(response.getOutputStream(), "UTF-8");
//        writer.write(message);
//        writer.close();
//    }
}
