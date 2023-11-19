/*
 * RscListeAnalysen.java
 *
 * Created on 29.01.2007 14:32:36
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.block.analyses;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import de.marketmaker.istar.analyses.frontend.AnalysesRequest;
import de.marketmaker.istar.analyses.frontend.AnalysisResponse;
import de.marketmaker.istar.common.validator.RestrictedSet;
import de.marketmaker.istar.domain.profile.Selector;
import de.marketmaker.istar.merger.user.Portfolio;
import de.marketmaker.istar.merger.web.easytrade.ListResult;
import de.marketmaker.istar.merger.web.easytrade.block.AbstractUserListHandler;
import de.marketmaker.istar.merger.web.easytrade.block.UserListListCommand;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class RscListeAnalysen extends AbstractUserListHandler {

    private static final AnalysisResponse EMPTY_RESPONSE = new AnalysisResponse();
    
    public static class Command extends UserListListCommand {
        private String providerId;

        private boolean ignoreAnalysesWithoutRating = true;

        @RestrictedSet("aktiencheck,dpaafx,websim,awp")
        public String getProviderId() {
            return this.providerId;
        }

        public void setProviderId(String providerId) {
            this.providerId = providerId;
        }

        Selector getSelector() {
            return RscCommand.getSelector(this.providerId);
        }

        public boolean isIgnoreAnalysesWithoutRating() {
            return ignoreAnalysesWithoutRating;
        }

        public void setIgnoreAnalysesWithoutRating(boolean ignoreAnalysesWithoutRating) {
            this.ignoreAnalysesWithoutRating = ignoreAnalysesWithoutRating;
        }
    }

    // since we cannot extend UserHandler AND RscAnalysesuchergebnis, use it
    // as a delegate to avoid duplicate code
    private RscFinder rscFinder;

    public RscListeAnalysen() {
        super(Command.class);
    }

    public void setRscFinder(RscFinder rscFinder) {
        this.rscFinder = rscFinder;
    }

    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response,
            Object o, BindException errors) throws Exception {
        final Command cmd = (Command) o;

        final ListResult listResult = this.rscFinder.createListResult(cmd, RscFinder.SORTFIELDS, RscFinder.DEFAULT_SORT_BY);
        final Portfolio p = getPortfolio(cmd);
        final AnalysisResponse analyses = getAnalyses(cmd, listResult, p);

        return this.rscFinder.createResult(analyses, listResult, cmd.getSelector());
    }

    private AnalysisResponse getAnalyses(Command cmd, ListResult listResult,
            Portfolio p) {
        if (p == null || p.getPositions().isEmpty()) {
            return EMPTY_RESPONSE;
        }

        final AnalysesRequest sr = this.rscFinder.createRequest(cmd.getSelector(),
                cmd.isIgnoreAnalysesWithoutRating(), listResult, RscFinder.SORTFIELDS);
        sr.setInstrumentIds(p.getInstrumentIds());

        return this.rscFinder.getAnalyses(sr);
    }
}
