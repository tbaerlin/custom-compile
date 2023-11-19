/*
 * PmDpFile.java
 *
 * Created on 07.04.2009 12:12:37
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.misc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import de.marketmaker.istar.domain.KeysystemEnum;
import de.marketmaker.istar.domain.instrument.Instrument;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.domain.profile.Profile;
import de.marketmaker.istar.domainimpl.profile.PmAboProfile;
import de.marketmaker.istar.history.dpfile.DpFileProvider;
import de.marketmaker.istar.history.dpfile.DpFileRequest;
import de.marketmaker.istar.merger.context.RequestContextHolder;
import de.marketmaker.istar.merger.web.HttpRequestUtil;
import de.marketmaker.istar.merger.web.easytrade.SymbolStrategyEnum;
import de.marketmaker.istar.merger.web.easytrade.block.EasytradeInstrumentProvider;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
@Controller
public class PmDpFile {
    private static final Pattern IIDS = Pattern.compile("[0-9]{1,9}(,[0-9]{1,9})*");

    private final Logger logger = LoggerFactory.getLogger(getClass());

    public static class Command {
        private final Set<String> iids = new HashSet<>();

        public void setIid(String[] iid) {
            for (String s : iid) {
                if (IIDS.matcher(s).matches()) {
                    this.iids.addAll(Arrays.asList(s.split(",")));
                }
            }
        }

        public List<String> getIids() {
            return new ArrayList<>(this.iids);
        }
    }

    private EasytradeInstrumentProvider instrumentProvider;

    private DpFileProvider dpFileProvider;

    public void setDpFileProvider(DpFileProvider dpFileProvider) {
        this.dpFileProvider = dpFileProvider;
    }

    public void setInstrumentProvider(EasytradeInstrumentProvider instrumentProvider) {
        this.instrumentProvider = instrumentProvider;
    }

    @RequestMapping("**/pm-public/dpfile.dp")
    protected ModelAndView handleDp(HttpServletRequest request,
            HttpServletResponse response, Command cmd) throws Exception {
        return handle(request, response, cmd, false);
    }

    @RequestMapping("**/pm-public/dpfile.zip")
    protected ModelAndView handleZip(HttpServletRequest request,
            HttpServletResponse response, Command cmd) throws Exception {
        return handle(request, response, cmd, true);
    }

    private ModelAndView handle(HttpServletRequest request,
            HttpServletResponse response, Command cmd, boolean zip) throws Exception {

        if (cmd.iids.isEmpty()) {
            this.logger.info("<handle> no iids in " + HttpRequestUtil.toString(request));
            response.sendError(HttpServletResponse.SC_NO_CONTENT);
            return null;
        }

        final Profile profile = RequestContextHolder.getRequestContext().getProfile();
        if (!(profile instanceof PmAboProfile)) {
            this.logger.error("<handle> expected PmAboProfile, got " + profile
                    + " for " + HttpRequestUtil.toString(request));
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return null;
        }

        final Map<String, Instrument> map
                = this.instrumentProvider.identifyInstrument(cmd.getIids(), SymbolStrategyEnum.IID);

        if (map.isEmpty()) {
            this.logger.warn("<handle> no instruments for " + HttpRequestUtil.toString(request));
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return null;
        }

        final DpFileRequest dpFileRequest = createRequest((PmAboProfile) profile, map, zip);

        final byte[] dpFile = this.dpFileProvider.getDpFile(dpFileRequest);
        if (dpFile == null) {
            this.logger.warn("<handle> no dpFile for " + HttpRequestUtil.toString(request));
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return null;
        }

        response.setContentType(dpFileRequest.isZipFile()
                ? "application/zip" : "application/octet-stream");
        response.setContentLength(dpFile.length);
        FileCopyUtils.copy(dpFile, response.getOutputStream());

        return null;
    }

    private DpFileRequest createRequest(PmAboProfile aboProfile,
            Map<String, Instrument> instruments, boolean zip) {

        final DpFileRequest result = new DpFileRequest(aboProfile.getName(), aboProfile.getAbos());
        result.setZipFile(zip);

        for (Map.Entry<String, Instrument> e : instruments.entrySet()) {
            final long iid = Long.parseLong(e.getKey());
            if (e.getValue() == null) {
                result.addItem(iid, null, null, null, null);
                continue;
            }

            for (Quote quote : e.getValue().getQuotes()) {
                result.addItem(iid, quote.getSymbolVwdcode(), quote.getSymbolMmwkn()
                        , quote.getEntitlement().getEntitlements(KeysystemEnum.MM)
                        , aboProfile.getAboQuality(quote));
            }
        }
        return result;
    }
}
