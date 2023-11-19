/*
 * WebSimChart.java
 *
 * Created on 20.04.12 11:30
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.chart;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import de.marketmaker.istar.analyses.frontend.AnalysesServer;
import de.marketmaker.istar.analyses.frontend.AnalysisImageRequest;
import de.marketmaker.istar.analyses.frontend.AnalysisImageResponse;

import static de.marketmaker.istar.domain.profile.Selector.WEB_SIM_ANALYSES;

/**
 * @author oflege
 */
@Controller
public class WebSimChart {

    private AnalysesServer analysesServer;

    public void setAnalysesServer(AnalysesServer analysesServer) {
        this.analysesServer = analysesServer;
    }


    @RequestMapping("**/websim.???")
    protected void handle(HttpServletRequest request, HttpServletResponse response,
            @RequestParam("id") String id) throws Exception {
        if (!StringUtils.hasText(id)) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        final byte[] data = getImage(id);

        if (data == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        final String uri = request.getRequestURI();
        response.setContentType("image/" + uri.substring(uri.lastIndexOf('.') + 1));
        response.setContentLength(data.length);
        response.getOutputStream().write(data);
    }

    private byte[] getImage(String id) {
        final AnalysisImageRequest request = new AnalysisImageRequest(WEB_SIM_ANALYSES, id);
        final AnalysisImageResponse response = this.analysesServer.getImage(request);
        return response.getData();
    }
}
