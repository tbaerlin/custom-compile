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
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import de.marketmaker.istar.domain.instrument.Instrument;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.domainimpl.profile.PmAboProfile;
import de.marketmaker.istar.merger.context.RequestContextHolder;
import de.marketmaker.istar.merger.web.easytrade.block.EasytradeInstrumentProvider;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 *
 * Some test URLs... make sure to apply the domain name to your machine's name because otherwise
 * you will loose the session after redirection due to the "same origin policy".
 *
 * http://suma.market-maker.de/mmder/start.html?mmauth=211Crmiolbd4B1vL3QxLfGxM9L7fm9V6EIq1K4cs4fAnQSc7KHdCcMuLCDcd2A4%3D
 * http://suma.market-maker.de/mmder/start.html?mmauth=211Crmiolbd4B1vL3QxLfGxM9L7fm9V6EIq1K4cs4fAnQSc7KHdCcMuLCDcd2A4%3D&locale=en
 * http://suma.market-maker.de/mmder/start.html?mmauth=211Crmiolbd4B1vL3QxLfGxM9L7fm9V6EIq1K4cs4fAnQSc7KHdCcMuLCDcd2A4%3D&locale=de
 * http://suma.market-maker.de/mmder/start.html?mmauth=211Crmiolbd4B1vL3QxLfGxM9L7fm9V6EIq1K4cs4fAnQSc7KHdCcMuLCDcd2A4%3D&locale=en&id=142467006
 *
 * http://suma.market-maker.de/mmder/detail.html?mmauth=211Crmiolbd4B1vL3QxLfGxM9L7fm9V6EIq1K4cs4fAnQSc7KHdCcMuLCDcd2A4%3D
 * http://suma.market-maker.de/mmder/detail.html?mmauth=211Crmiolbd4B1vL3QxLfGxM9L7fm9V6EIq1K4cs4fAnQSc7KHdCcMuLCDcd2A4%3D&locale=en
 * http://suma.market-maker.de/mmder/detail.html?mmauth=211Crmiolbd4B1vL3QxLfGxM9L7fm9V6EIq1K4cs4fAnQSc7KHdCcMuLCDcd2A4%3D&locale=de
 * http://suma.market-maker.de/mmder/detail.html?mmauth=211Crmiolbd4B1vL3QxLfGxM9L7fm9V6EIq1K4cs4fAnQSc7KHdCcMuLCDcd2A4%3D&locale=en&id=142467006
 *
 * http://suma.market-maker.de/pmnews/news.html?mmauth=211Crmiolbd4B1vL3QxLfGxM9L7fm9V6EIq1K4cs4fAnQSc7KHdCcMuLCDcd2A4%3D
 * http://suma.market-maker.de/pmnews/news.html?mmauth=211Crmiolbd4B1vL3QxLfGxM9L7fm9V6EIq1K4cs4fAnQSc7KHdCcMuLCDcd2A4%3D&locale=en
 * http://suma.market-maker.de/pmnews/news.html?mmauth=211Crmiolbd4B1vL3QxLfGxM9L7fm9V6EIq1K4cs4fAnQSc7KHdCcMuLCDcd2A4%3D&locale=de
 *
 * http://suma.market-maker.de/pmnews/news-detail.html?id=90577300&dewkn=BASF11&name=BASF%20NA&isin=DE000BASF111&mmauth=211Crmiolbd4B1vL3QxLfGxM9L7fm9V6EIq1K4cs4fAnQSc7KHdCcMuLCDcd2A4%3D
 * http://suma.market-maker.de/pmnews/news-detail.html?id=90577300&dewkn=BASF11&name=BASF%20NA&isin=DE000BASF111&mmauth=211Crmiolbd4B1vL3QxLfGxM9L7fm9V6EIq1K4cs4fAnQSc7KHdCcMuLCDcd2A4%3D&locale=de
 * http://suma.market-maker.de/pmnews/news-detail.html?id=90577300&dewkn=BASF11&name=BASF%20NA&isin=DE000BASF111&mmauth=211Crmiolbd4B1vL3QxLfGxM9L7fm9V6EIq1K4cs4fAnQSc7KHdCcMuLCDcd2A4%3D&locale=en
 *
 */
@Controller
public class PmPublicRedirect {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public static class Command {
        private String type;

        private String mmauth;

        private Long id;

        private String dpwkn;

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getMmauth() {
            return mmauth;
        }

        public void setMmauth(String mmauth) {
            this.mmauth = mmauth;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getDpwkn() {
            return dpwkn;
        }

        public void setDpwkn(String dpwkn) {
            this.dpwkn = dpwkn;
        }
    }

    private EasytradeInstrumentProvider instrumentProvider;

    private String baseUrl;

    public void setInstrumentProvider(EasytradeInstrumentProvider instrumentProvider) {
        this.instrumentProvider = instrumentProvider;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    @RequestMapping(value = "**/pm-public/redirect.html")
    protected ModelAndView handle(HttpServletRequest request,
            HttpServletResponse response, Command cmd) throws Exception {

        final Locale locale = RequestContextHolder.getRequestContext().getLocale();

        if (cmd.getMmauth() == null) {
            writeError(response, "Keine Authentifizierung angegeben!");
            return null;
        }

        final HttpSession session = request.getSession(true);
        session.setAttribute("mmauth", cmd.getMmauth());

        if ("news".equals(cmd.getType())) {
            redirectToNews(response, cmd, locale);
        }
        else {
            redirectToApp(response, cmd, locale);
        }
        return null;
    }

    private void redirectToApp(HttpServletResponse response, Command cmd, Locale locale) throws IOException {
        final String localeParameter = "?locale=" + locale.getLanguage();

        if (cmd.getId() == null) {
            response.sendRedirect(this.baseUrl + "/index.html" + localeParameter);
            return;
        }

        try {
            final Instrument instrument
                    = this.instrumentProvider.identifyInstruments(Arrays.asList(cmd.getId())).get(0);
            final Quote quote = getQuote(instrument, cmd.getDpwkn());

            response.sendRedirect(this.baseUrl + "/index.html" + localeParameter + "#P_"
                    + quote.getInstrument().getInstrumentType().name() + "/" + quote.getId() + ".qid");

        } catch (Exception exc) {
            this.logger.warn("<handle> unknown iid: " + cmd.getId());
            writeError(response, "Unbekanntes Symbol!");
        }
    }

    private void redirectToNews(HttpServletResponse response, Command cmd, Locale locale) throws IOException {
        final PmAboProfile profile
                = (PmAboProfile) RequestContextHolder.getRequestContext().getProfile();

        final String localeParameter = "locale=" + locale.getLanguage();

        if (!profile.isWisoBasicAbo() && cmd.getId() == null) {
            response.sendRedirect(this.baseUrl + "/index.html?" + localeParameter + "#N_UB");
            return;
        }

        final StringBuilder url = new StringBuilder(this.baseUrl.length() + 100)
                .append(this.baseUrl)
                .append("/news.html?authentication=")
                .append(URLEncoder.encode(cmd.getMmauth(), "UTF-8"))
                .append("&").append(localeParameter);

        if (cmd.getId() != null) {
            final Instrument instrument;
            try {
                instrument = this.instrumentProvider.identifyInstruments(Arrays.asList(cmd.getId())).get(0);
            } catch (Exception exc) {
                this.logger.warn("<redirectToNews> unknown iid: " + cmd.getId());
                writeError(response, "Unbekanntes Symbol!");
                return;
            }

            if (instrument == null) {
                this.logger.warn("<redirectToNews> unknown instrument: " + cmd.getId());
                writeError(response, "Unbekanntes Symbol!");
                return;
            }
            url.append("&symbol=").append(instrument.getId()).append(".iid");
        }

        response.sendRedirect(url.toString());
    }

    private Quote getQuote(Instrument instrument, String dpwkn) {
        if (dpwkn != null) {
            for (final Quote quote : instrument.getQuotes()) {
                if (dpwkn.equals(quote.getSymbolMmwkn())) {
                    return quote;
                }
            }
        }
        return instrument.getQuotes().get(0);
    }

    private void writeError(HttpServletResponse response, String message) throws IOException {
        response.setContentType("text/html;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_OK);
        final OutputStreamWriter writer = new OutputStreamWriter(response.getOutputStream(), "UTF-8");
        writer.write("<html><body>" + message + "</body></html>");
        writer.close();
    }
}
