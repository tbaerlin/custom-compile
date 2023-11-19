/*
 * RscAnalyse.java
 *
 * Created on 13.07.2006 07:06:22
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.block.analyses;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.joda.time.DateTime;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import de.marketmaker.istar.analyses.frontend.AnalysesRequest;
import de.marketmaker.istar.analyses.frontend.AnalysesServer;
import de.marketmaker.istar.analyses.frontend.AnalysisResponse;
import de.marketmaker.istar.common.validator.Pattern;
import de.marketmaker.istar.domain.data.StockAnalysis;
import de.marketmaker.istar.domain.profile.Profile;
import de.marketmaker.istar.domain.profile.Selector;
import de.marketmaker.istar.merger.context.RequestContextHolder;
import de.marketmaker.istar.merger.provider.NoDataException;
import de.marketmaker.istar.merger.web.easytrade.block.EasytradeCommandController;

/**
 * Returns an analysis for a given id or the latest of all available analyses if no id is given.
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class RscAnalysis extends EasytradeCommandController {

    public static class Command extends RscCommand {

        private String analysisid;

        private boolean useLongId = true;

        @Pattern(regex = "[0-9a-z]+")
        public String getAnalysisid() {
            return analysisid;
        }

        public void setAnalysisid(String analysisid) {
            this.analysisid = analysisid;
        }

        public void setAnalysenid(String analysenid) {
            this.analysisid = analysenid;
        }

        public boolean isUseLongId() {
            return useLongId;
        }

        public void setUseLongId(boolean useLongId) {
            this.useLongId = useLongId;
        }
    }

    private AnalysesServer analysesServer;

    private String disclaimer = ""
            + " Die für das zuvor genannte Finanzinstrument relevanten "
            + "Interessenkonflikte der DZ BANK AG finden Sie hier: "
            + "https://www.dzbank.de/Pflichtangaben";

    public RscAnalysis() {
        super(Command.class);
    }

    public void setAnalysesServer(AnalysesServer analysesServer) {
        this.analysesServer = analysesServer;
    }

    @Override
    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response,
            Object o, BindException errors) {
        final Command cmd = (Command) o;

        final Map<String, Object> model = new HashMap<>();
        StockAnalysis analysis = getAnalysis(cmd);
        model.put("analysis", analysis);
        model.put("useShortId", !cmd.isUseLongId() && "marketmanager".equals(getZoneName(request)));
        if (needsDzBankDisclaimer(analysis)) {
            model.put("disclaimer", disclaimer);
        }

        model.put(RscCommand.getProviderId(cmd.getSelector()), Boolean.TRUE);
        return new ModelAndView("rscanalysis", model);
    }

    // text needs a disclaimer if:
    // analysis is not from DZ AND user is DZ customer AND after 30.06.2016
    private boolean needsDzBankDisclaimer(StockAnalysis analysis) {
        final Profile profile = RequestContextHolder.getRequestContext().getProfile();
        return  analysis != null
                && (profile.isAllowed(Selector.DZ_BANK_USER)
                || profile.isAllowed(Selector.WGZ_BANK_USER)
                || profile.isAllowed(Selector.SON_BANK_USER))
                && DateTime.now().isAfter(DateTime.parse("2016-06-30")
        );
    }

    private StockAnalysis getAnalysis(Command cmd) {
        final AnalysesRequest request = new AnalysesRequest(cmd.getSelector());
        if (cmd.getAnalysisid() != null) {
            request.setAnalysisIds(Collections.singletonList(cmd.getAnalysisid()));
        }
        else {
            request.setCount(1);
        }

        final AnalysisResponse response = this.analysesServer.getAnalyses(request);
        final List<StockAnalysis> analyses = response.getAnalyses();
        if (analyses.isEmpty() || analyses.get(0) == null) {
            throw new NoDataException((cmd.getAnalysisid() == null)
                    ? "no analysis available" : "unknown analysis id: " + cmd.getAnalysisid());
        }
        return analyses.get(0);
    }
}


