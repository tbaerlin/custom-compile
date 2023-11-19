/*
 * RscAnalysesuchkriterien.java
 *
 * Created on 13.07.2006 07:06:22
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.block.analyses;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import de.marketmaker.istar.analyses.frontend.AnalysesMetaRequest;
import de.marketmaker.istar.analyses.frontend.AnalysesMetaResponse;
import de.marketmaker.istar.analyses.frontend.AnalysesServer;
import de.marketmaker.istar.domain.profile.Selector;
import de.marketmaker.istar.merger.web.easytrade.block.EasytradeCommandController;

/**
 * Returns meta data for {@see RscFinder}, such as lists of analyst names that can be used to
 * create a finder form. The detailed content of the result is specific for the selected
 * analyses provider.
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class RscFinderMetadata extends EasytradeCommandController {

    public static final String TEMPLATE = "rscfindermetadata";

    private AnalysesServer analysesServer;

    public RscFinderMetadata() {
        super(RscCommand.class);
    }

    public void setAnalysesServer(AnalysesServer analysesServer) {
        this.analysesServer = analysesServer;
    }

    @Override
    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response,
            Object o, BindException errors) throws Exception {
        RscCommand cmd = (RscCommand) o;
        final Selector s = cmd.getSelector();
        AnalysesMetaRequest metaRequest = new AnalysesMetaRequest(s, cmd.isIgnoreAnalysesWithoutRating());
        AnalysesMetaResponse metaResponse = this.analysesServer.getMetaData(metaRequest);
        return new ModelAndView(RscFinderMetadata.TEMPLATE, metaResponse.getData());
    }

}
