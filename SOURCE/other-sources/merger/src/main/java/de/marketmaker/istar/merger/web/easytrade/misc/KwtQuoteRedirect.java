/*
 * KwtSmartclient.java
 *
 * Created on 07.04.2009 12:12:37
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.misc;

import java.io.IOException;
import java.io.OutputStreamWriter;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.domain.profile.Profile;
import de.marketmaker.istar.domainimpl.profile.VwdProfile;
import de.marketmaker.istar.merger.context.RequestContextHolder;
import de.marketmaker.istar.merger.web.easytrade.SymbolStrategyEnum;
import de.marketmaker.istar.merger.web.easytrade.block.EasytradeInstrumentProvider;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
@Controller
public class KwtQuoteRedirect {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private EasytradeInstrumentProvider instrumentProvider;

    private String baseUrl;

    public void setInstrumentProvider(EasytradeInstrumentProvider instrumentProvider) {
        this.instrumentProvider = instrumentProvider;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    @RequestMapping(value = "/fusion/kwt_quote.html")
    protected void handle(HttpServletResponse response
            , @RequestParam(name = "Xun", required = false) String xun
            , @RequestParam(name = "wkn", required = false) String wkn
            , @RequestParam(name = "bpl", required = false) String bpl
            , @RequestParam(name = "sc", required = false) String sc
    ) throws Exception {

        if (bpl == null || wkn == null || xun == null || !"1".equals(sc)) {
            writeError(response, "Die Parameter-Liste ist nicht vollst&auml;ndig.\n</body></html>");
            return;
        }

        final Profile profile = RequestContextHolder.getRequestContext().getProfile();
        if (!(profile instanceof VwdProfile) || ((VwdProfile) profile).getCreated() == null) {
            writeError(response, "Ungültige Xun.\n</body></html>");
            return;
        }

        final Quote quote;
        try {
            quote = this.instrumentProvider.identifyQuote(wkn, SymbolStrategyEnum.AUTO, bpl, null);
        } catch (Exception exc) {
            this.logger.warn("<handle> unknown wkn: " + wkn);
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        response.sendRedirect(this.baseUrl + "/kwtpub/portrait.html?Xun=" + xun + "&startpage=P_"
                + quote.getInstrument().getInstrumentType().name() + "/" + quote.getId() + ".qid");
    }

    private void writeError(HttpServletResponse response, String message) throws IOException {
        response.setContentType("text/html;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_OK);
        try (OutputStreamWriter w = new OutputStreamWriter(response.getOutputStream(), "UTF-8")) {
            w.write(message);
        }
    }
}
