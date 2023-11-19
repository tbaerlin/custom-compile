/*
 * ScreenerPdfProxy.java
 *
 * Created on 15.11.2015 22:15:31
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.misc;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import de.marketmaker.istar.domain.instrument.Instrument;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.domain.profile.Profile;
import de.marketmaker.istar.domainimpl.profile.PmAboProfile;
import de.marketmaker.istar.domainimpl.profile.ProfileFactory;
import de.marketmaker.istar.domainimpl.profile.ProfileProvider;
import de.marketmaker.istar.domainimpl.profile.ProfileRequest;
import de.marketmaker.istar.domainimpl.profile.ProfileResponse;
import de.marketmaker.istar.instrument.IndexCompositionProvider;
import de.marketmaker.istar.instrument.IndexCompositionRequest;
import de.marketmaker.istar.instrument.IndexCompositionResponse;
import de.marketmaker.istar.merger.context.RequestContextHolder;
import de.marketmaker.istar.merger.provider.screener.ScreenerField;
import de.marketmaker.istar.merger.provider.screener.ScreenerProvider;
import de.marketmaker.istar.merger.provider.screener.ScreenerResult;
import de.marketmaker.istar.merger.web.easytrade.block.EasytradeInstrumentProvider;

/**
 * http://localhost:8080/dmxml-1/fusionapp/miscellaneous/screener.pdf?isin=DE0007100000&mmauth=C4D7vq0eKw0K4Qwyixhy0%2BPGUMObq3cmJ%2FHQbuSu
 * @author Thomas Kiesgen
 */
@Controller
public class ScreenerPdfProxy {
    private static final int REF_NUMBER = 100328;  // as told by theScreener.com

    private static final String TOKEN_WORD = "Mk"; // as told by theScreener.com

    private static final Set<String> RESTRICTED_COUNTRIES = new HashSet<>(Arrays.asList("DE", "AT", "CH"));

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @SuppressWarnings("unused")
    public static class Command {
        private String isin;

        private String mmauth;

        private String language = "de";

        public String getIsin() {
            return isin;
        }

        public void setIsin(String isin) {
            this.isin = isin;
        }

        public String getMmauth() {
            return mmauth;
        }

        public void setMmauth(String mmauth) {
            this.mmauth = mmauth;
        }

        public String getLanguage() {
            return language;
        }

        public void setLanguage(String language) {
            this.language = language;
        }
    }

    private EasytradeInstrumentProvider instrumentProvider;

    private ScreenerProvider screenerProvider;

    private ProfileProvider profileProvider;

    private IndexCompositionProvider indexCompositionProvider;

    public void setInstrumentProvider(EasytradeInstrumentProvider instrumentProvider) {
        this.instrumentProvider = instrumentProvider;
    }

    public void setScreenerProvider(
            ScreenerProvider screenerProvider) {
        this.screenerProvider = screenerProvider;
    }

    public void setProfileProvider(ProfileProvider profileProvider) {
        this.profileProvider = profileProvider;
    }

    public void setIndexCompositionProvider(
            IndexCompositionProvider indexCompositionProvider) {
        this.indexCompositionProvider = indexCompositionProvider;
    }

    @RequestMapping(value = "**/screener.pdf")
    protected ModelAndView handle(HttpServletResponse response, Command cmd) throws Exception {
        if (cmd.getMmauth() == null) {
            writeError(response, "Keine Authentifizierung angegeben!");
            return null;
        }

        final ProfileResponse profileResponse = this.profileProvider.getProfile(new ProfileRequest("pmpub:ByLogin", cmd.getMmauth()));
        final Profile profile = profileResponse.getProfile();
        if (!profileResponse.isValid() || !(profile instanceof PmAboProfile)) {
            writeError(response, "Authentifizierung ungültig!");
            return null;
        }
        final PmAboProfile pmAboProfile = (PmAboProfile) profile;


        return RequestContextHolder.callWith(pmAboProfile, () -> handleInternal(response, cmd, pmAboProfile));
    }

    private ModelAndView handleInternal(HttpServletResponse response, Command cmd,
            PmAboProfile profile) throws IOException {
        final Instrument instrument = this.instrumentProvider.identifyByIsinOrWkn(cmd.getIsin());

        final ScreenerResult screener = this.screenerProvider.getScreenerResult(instrument.getId(), "de");

        final boolean accessAllowed = isAccessAllowed(profile, cmd.getIsin(), screener);
        if (!accessAllowed) {
            writeError(response, "Abo fehlt!");
            return null;
        }
        response.setHeader("Pragma", "public");
        response.setHeader("Expires", "0");
        response.setHeader("Cache-Control", "must-revalidate, post-check=0, pre-check=0");
        response.setHeader("Content-Transfer-Encoding", "binary");
        response.setContentType("application/pdf");

        final String url = getURL(cmd.getIsin(), cmd.getLanguage(), screener);

        this.logger.info("<handleInternal> url="+url);

        final InputStream inputStream = new URL(url).openStream();
        IOUtils.copy(inputStream, response.getOutputStream());
        inputStream.close();

        return null;
    }

    private boolean isAccessAllowed(PmAboProfile pmAboProfile, String isin,
            ScreenerResult screener) {
        final Set<String> abos = pmAboProfile.getAbos();
        if (abos.contains("J")) {
            return true;
        }

        if (!(abos.contains("G") || abos.contains("WBxARR"))) {
            return false;
        }

        final String country = getField(screener.getBaseFields(), "country", null);
        return RESTRICTED_COUNTRIES.contains(country)
                || checkIndex(isin, "965815.STX")
                || checkIndex(isin, "DJI.DJ");
    }

    private boolean checkIndex(String isin, String indexSymbol) {
        final Profile p = ProfileFactory.valueOf(true);
        try {
            return RequestContextHolder.callWith(p, () -> doCheckIndex(isin, indexSymbol));
        } catch (Exception e) {
            this.logger.warn("<checkIndex> failed", e);
        }
        return false;
    }

    private boolean doCheckIndex(String isin, String indexSymbol) {
        final Quote quote = this.instrumentProvider.identifyQuoteByVwdcode(indexSymbol);
        if (quote == null) {
            this.logger.warn("<doCheckIndex> unknown vwd code: " + indexSymbol);
            return false;
        }
        final IndexCompositionResponse indexComposition = this.indexCompositionProvider.getIndexComposition(new IndexCompositionRequest(quote.getId()));
        if (!indexComposition.isValid()) {
            this.logger.warn("<doCheckIndex> indexComposition for " + indexSymbol + " is invalid");
            return false;
        }

        final List<Instrument> instruments = this.instrumentProvider.identifyInstruments(indexComposition.getIndexComposition().getIids());
        for (Instrument instrument : instruments) {
            if (isin.equals(instrument.getSymbolIsin())) {
                return true;
            }
        }
        return false;
    }

    private String getURL(String isin, String language, ScreenerResult screener) {
        return "http://corp.thescreener.com/asp/partners/MarketMaker_reports.asp"
                + "?pToken=" + getToken()
                + "&pISIN=" + isin
                + "&pMkt=" + getField(screener.getBaseFields(), "market", isin.substring(0, 2))
                + "&pCcy=" + getField(screener.getBaseFields(), "currency", "EUR")
                + "&pLang=" + language;
    }

    private String getField(List<ScreenerField> fields, String name, String defaultValue) {
        for (ScreenerField field : fields) {
            if (name.equals(field.getName())) {
                return (String) field.getValue();
            }
        }
        return defaultValue;
    }

    private String getToken() {
        final Calendar calendar = Calendar.getInstance();
        final int calendarValue = calendar.get(Calendar.DAY_OF_MONTH) + calendar.get(Calendar.MONTH);
        final int number = ((REF_NUMBER % 1000) - 1 + 100) - calendarValue;
        final char[] c = TOKEN_WORD.toCharArray();
        final StringBuilder result = new StringBuilder();
        for (int i = 0; i < c.length; i++) {
            if (i > 0) {
                result.append('.');
            }
            result.append(number ^ (int) c[i]);
        }
        return result.toString();
    }

    private void writeError(HttpServletResponse response, String message) throws IOException {
        response.setContentType("text/html;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_OK);
        final OutputStreamWriter writer = new OutputStreamWriter(response.getOutputStream(), "UTF-8");
        writer.write("<html><body>" + message + "</body></html>");
        writer.close();
    }

    public static void main(String[] args) {
        ScreenerPdfProxy spp = new ScreenerPdfProxy();

        System.out.println("getToken() = " + spp.getToken());
        System.out.println("URL " + spp.getURL("DE0007100000", "de", new ScreenerResult(20665, "de")));

        System.out.println("URL " + spp.getURL("FR0000071946", "de", new ScreenerResult(22823, "de")));
        System.out.println("URL " + spp.getURL("FR0000063737", "de", new ScreenerResult(22418, "de")));
        System.out.println("URL " + spp.getURL("FR0004036036", "de", new ScreenerResult(186238, "de")));
        System.out.println("URL " + spp.getURL("NZCMOE0001S7", "de", new ScreenerResult(609885, "de")));
        System.out.println("URL " + spp.getURL("FR0000034639", "de", new ScreenerResult(16247, "de")));

        System.out.println("URL " + spp.getURL("FR0000130650", "de", new ScreenerResult(19940, "de")));
        System.out.println("URL " + spp.getURL("FR0010259150", "de", new ScreenerResult(1662646, "de")));
        System.out.println("URL " + spp.getURL("FR0000120495", "de", new ScreenerResult(14103, "de")));
        System.out.println("URL " + spp.getURL("FR0000050809", "de", new ScreenerResult(16598, "de")));
    }
}
